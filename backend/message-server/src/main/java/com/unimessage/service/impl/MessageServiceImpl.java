package com.unimessage.service.impl;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.toolkit.Db;
import com.unimessage.cache.CacheService;
import com.unimessage.constant.CacheKeyConstants;
import com.unimessage.context.AppContext;
import com.unimessage.dto.MqMessage;
import com.unimessage.dto.SendRequest;
import com.unimessage.dto.SendResponse;
import com.unimessage.entity.*;
import com.unimessage.enums.BatchStatus;
import com.unimessage.enums.ChannelType;
import com.unimessage.enums.DetailStatus;
import com.unimessage.handler.ChannelHandler;
import com.unimessage.handler.ChannelHandlerFactory;
import com.unimessage.mapper.LogMsgBatchMapper;
import com.unimessage.mapper.SysChannelMapper;
import com.unimessage.mapper.SysRecipientMapper;
import com.unimessage.mapper.SysTemplateMapper;
import com.unimessage.mq.producer.MqProducer;
import com.unimessage.service.MessageService;
import com.unimessage.service.RateLimiterService;
import com.unimessage.util.UserIdUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

/**
 * 消息发送服务实现类
 *
 * @author 海明
 * @since 2025-12-04
 */
@Slf4j
@Service
public class MessageServiceImpl implements MessageService {

    @Resource
    private SysTemplateMapper templateMapper;
    @Resource
    private SysChannelMapper channelMapper;
    @Resource
    private LogMsgBatchMapper batchMapper;
    @Resource
    private SysRecipientMapper recipientMapper;
    @Resource
    private ChannelHandlerFactory handlerFactory;
    @Resource
    private MqProducer mqProducer;
    @Resource
    private RateLimiterService rateLimiterService;
    @Resource
    private CacheService cacheService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SendResponse send(SendRequest request) {
        // 幂等性校验：如果提供了 bizId，检查是否已处理过
        if (request.getBizId() != null && !request.getBizId().isEmpty()) {
            String dedupeKey = cacheService.buildKey(CacheKeyConstants.RATE_LIMIT_TEMPLATE, "dedupe:", request.getBizId());
            // 尝试设置，如果已存在则返回 false
            if (!cacheService.setIfAbsent(dedupeKey, "1", 24 * 60 * 60)) {
                log.warn("重复请求被拦截, bizId={}", request.getBizId());
                // 查询已有的批次号返回
                return SendResponse.fail("重复请求，bizId 已处理: " + request.getBizId());
            }
        }

        SysTemplate template = getTemplate(request.getTemplateCode());
        if (template == null) {
            return SendResponse.fail("模板不存在: " + request.getTemplateCode());
        }
        if (template.getStatus() != 1) {
            return SendResponse.fail("模板已禁用");
        }

        // 检查频率限制 (基于 App + Template)
        if (template.getRateLimit() != null && template.getRateLimit() > 0) {
            Long appId = AppContext.getCurrentAppId();
            // Key: uni-message:rate-limit:template:{appId}:{code}
            String limitKey = cacheService.buildKey(CacheKeyConstants.RATE_LIMIT_TEMPLATE,
                    (appId != null ? appId.toString() : "0"), ":", template.getCode());

            if (!rateLimiterService.tryAcquire(limitKey, template.getRateLimit(), 1)) {
                return SendResponse.fail("发送频率超限，请稍后重试");
            }
        }

        SysChannel channel = getChannel(template.getChannelId());
        if (channel == null) {
            return SendResponse.fail("渠道不可用");
        }

        Map<String, String> finalRecipientMap = resolveRecipients(request, template, channel);
        if (finalRecipientMap.isEmpty()) {
            return SendResponse.fail("未指定接收者，且模板未关联有效的接收人或分组");
        }
        request.setRecipients(new ArrayList<>(finalRecipientMap.keySet()));

        if (handlerFactory.getHandler(channel.getType()) == null) {
            return SendResponse.fail("未找到该渠道的处理器: " + channel.getType());
        }

        return createAndPushBatch(request, template, channel, finalRecipientMap);
    }

    private SysTemplate getTemplate(String code) {
        LambdaQueryWrapper<SysTemplate> query = new LambdaQueryWrapper<>();
        query.eq(SysTemplate::getCode, code);
        return templateMapper.selectOne(query);
    }

    private SysChannel getChannel(Long id) {
        SysChannel channel = channelMapper.selectById(id);
        return (channel != null && channel.getStatus() == 1) ? channel : null;
    }

    private Map<String, String> resolveRecipients(SendRequest request, SysTemplate template, SysChannel channel) {
        List<String> requestRecipients = request.getRecipients();
        if (requestRecipients != null && !requestRecipients.isEmpty()) {
            Map<String, String> map = new HashMap<>(requestRecipients.size());
            for (String r : requestRecipients) {
                // 手动指定时暂无名称
                map.put(r, "");
            }
            return map;
        }

        Map<String, String> recipientMap = new HashMap<>();
        // 1. 从分组获取
        if (template.getRecipientGroupIds() != null && !template.getRecipientGroupIds().isEmpty()) {
            addRecipientsFromGroups(recipientMap, template.getRecipientGroupIds(), channel.getType());
        }

        // 2. 从独立接收者列表获取
        if (template.getRecipientIds() != null && !template.getRecipientIds().isEmpty()) {
            addRecipientsFromIds(recipientMap, template.getRecipientIds(), channel.getType());
        }

        return recipientMap;
    }

    private void addRecipientsFromGroups(Map<String, String> recipientMap, String groupIdsStr, String channelType) {
        String[] groupIds = groupIdsStr.split(",");
        for (String groupIdStr : groupIds) {
            try {
                Long groupId = Long.parseLong(groupIdStr.trim());
                List<SysRecipient> groupMembers = recipientMapper.selectByGroupId(groupId);
                if (groupMembers != null && !groupMembers.isEmpty()) {
                    recipientMap.putAll(extractRecipientMap(groupMembers, channelType));
                }
            } catch (NumberFormatException e) {
                log.error("Invalid group ID format: {}", groupIdStr);
            }
        }
    }

    private void addRecipientsFromIds(Map<String, String> recipientMap, String idsStr, String channelType) {
        String[] recipientIds = idsStr.split(",");
        List<Long> idList = new ArrayList<>();
        for (String idStr : recipientIds) {
            try {
                idList.add(Long.parseLong(idStr.trim()));
            } catch (NumberFormatException e) {
                log.error("Invalid recipient ID format: {}", idStr);
            }
        }

        if (!idList.isEmpty()) {
            List<SysRecipient> recipients = recipientMapper.selectBatchIds(idList);
            if (recipients != null && !recipients.isEmpty()) {
                recipientMap.putAll(extractRecipientMap(recipients, channelType));
            }
        }
    }

    private SendResponse createAndPushBatch(SendRequest request, SysTemplate template, SysChannel channel, Map<String, String> recipientMap) {
        String batchNo = UUID.randomUUID().toString().replace("-", "");
        LogMsgBatch batch = new LogMsgBatch();
        batch.setBatchNo(batchNo);
        batch.setAppId(AppContext.getCurrentAppId() != null ? AppContext.getCurrentAppId() : 0L);
        batch.setTemplateId(template.getId());
        batch.setTemplateName(template.getName());
        batch.setChannelId(channel.getId());
        batch.setChannelName(channel.getName());
        batch.setChannelType(channel.getType());
        batch.setMsgType(template.getMsgType());
        batch.setTitle(template.getTitle());
        batch.setContent(template.getContent());
        batch.setContentParams(JSON.toJSONString(request.getParams()));
        batch.setTotalCount(recipientMap.size());
        batch.setSuccessCount(0);
        batch.setFailCount(0);
        batch.setStatus(BatchStatus.PENDING.getCode());
        batch.setCreatedAt(LocalDateTime.now());

        batchMapper.insert(batch);

        try {
            // Push to Redis MQ
            MqMessage message = new MqMessage(batch.getId(), request, recipientMap);
            mqProducer.send(message);
        } catch (Exception e) {
            log.error("Push to MQ failed", e);
            throw new RuntimeException("消息入队失败", e);
        }

        return SendResponse.success(batchNo);
    }

    /**
     * 根据渠道类型从接收者实体中提取对应的联系方式
     *
     * @return Map<Contact, Name>
     */
    private Map<String, String> extractRecipientMap(List<SysRecipient> recipients, String channelType) {
        if (recipients == null || recipients.isEmpty()) {
            return Collections.emptyMap();
        }

        ChannelType type = ChannelType.fromCode(channelType);
        if (type == null) {
            return Collections.emptyMap();
        }

        Map<String, String> result = new HashMap<>(16);
        for (SysRecipient r : recipients) {
            String contact = switch (type) {
                case SMS, TENCENT_SMS, TWILIO -> r.getMobile();
                case EMAIL -> r.getEmail();
                case WECHAT_OFFICIAL, WECHAT_WORK, DINGTALK, FEISHU, TELEGRAM, SLACK, WEBHOOK -> {
                    // 从JSON格式的userId中提取对应渠道的用户ID
                    String userId = extractUserIdByChannelType(r.getUserId(), channelType);
                    // 如果没有对应渠道的userId，回退到手机号
                    yield userId != null ? userId : r.getMobile();
                }
            };

            if (contact != null && !contact.isEmpty()) {
                result.put(contact, r.getName());
            }
        }
        return result;
    }

    /**
     * 从JSON格式的userId字符串中提取指定渠道类型的用户ID
     *
     * @param userIdJson  JSON格式的用户ID字符串
     * @param channelType 渠道类型
     * @return 对应渠道的用户ID，如果不存在则返回null
     */
    private String extractUserIdByChannelType(String userIdJson, String channelType) {
        return UserIdUtil.getUserId(userIdJson, channelType);
    }

    @Override
    public void processBatch(MqMessage message) {
        // 幂等性校验：防止同一批次被重复处理
        String processKey = cacheService.buildKey(CacheKeyConstants.RATE_LIMIT_TEMPLATE, "batch:process:", message.getBatchId().toString());
        if (!cacheService.setIfAbsent(processKey, "1", 24 * 60 * 60)) {
            log.warn("批次已处理，跳过重复消费, batchId={}", message.getBatchId());
            return;
        }

        LogMsgBatch batch = batchMapper.selectById(message.getBatchId());
        if (batch == null) {
            log.error("Batch not found: {}", message.getBatchId());
            return;
        }

        // 检查批次状态，如果已经不是 PENDING 状态，说明已处理过
        if (!batch.getStatus().equals(BatchStatus.PENDING.getCode())) {
            log.warn("批次状态非待处理，跳过, batchId={}, status={}", message.getBatchId(), batch.getStatus());
            return;
        }

        SysTemplate template = templateMapper.selectById(batch.getTemplateId());
        if (template == null) {
            log.error("Template not found: {}", batch.getTemplateId());
            return;
        }

        SysChannel channel = channelMapper.selectById(batch.getChannelId());
        if (channel == null) {
            log.error("Channel not found: {}", batch.getChannelId());
            return;
        }

        ChannelHandler handler = handlerFactory.getHandler(channel.getType());
        if (handler == null) {
            log.error("Handler not found: {}", channel.getType());
            return;
        }

        List<LogMsgDetail> details = createDetails(message, batch);
        if (details.isEmpty()) {
            return;
        }

        sendToRecipients(details, handler, channel, template, message.getRequest().getParams(), batch);
    }

    private List<LogMsgDetail> createDetails(MqMessage message, LogMsgBatch batch) {
        List<LogMsgDetail> details = new ArrayList<>();
        Map<String, String> nameMap = message.getRecipientNames() != null ? message.getRecipientNames() : Collections.emptyMap();
        LocalDateTime now = LocalDateTime.now();

        for (String recipient : message.getRequest().getRecipients()) {
            LogMsgDetail detail = new LogMsgDetail();
            detail.setBatchId(batch.getId());
            detail.setRecipient(recipient);
            detail.setRecipientName(nameMap.get(recipient));
            detail.setStatus(DetailStatus.SENDING.getCode());
            detail.setCreatedAt(now);
            details.add(detail);
        }

        try {
            Db.saveBatch(details);
            return details;
        } catch (Exception e) {
            log.error("Batch insert details failed", e);
            batch.setStatus(BatchStatus.FAIL.getCode());
            batchMapper.updateById(batch);
            return Collections.emptyList();
        }
    }

    private void sendToRecipients(List<LogMsgDetail> details, ChannelHandler handler, SysChannel channel,
                                  SysTemplate template, Map<String, Object> params, LogMsgBatch batch) {
        int success = 0;
        int fail = 0;

        for (LogMsgDetail detail : details) {
            try {
                boolean result = handler.send(channel, template, detail, params);
                if (result) {
                    detail.setStatus(DetailStatus.SUCCESS.getCode());
                    success++;
                } else {
                    detail.setStatus(DetailStatus.FAIL.getCode());
                    fail++;
                }
            } catch (Exception e) {
                log.error("发送异常: recipient={}", detail.getRecipient(), e);
                detail.setStatus(DetailStatus.FAIL.getCode());
                detail.setErrorMsg(e.getMessage());
                fail++;
            }
            detail.setSendTime(LocalDateTime.now());
        }

        // 批量更新详情状态，提升性能
        Db.updateBatchById(details);

        updateBatchStatus(batch, success, fail);
    }

    private void updateBatchStatus(LogMsgBatch batch, int success, int fail) {
        batch.setSuccessCount(success);
        batch.setFailCount(fail);
        if (fail == 0) {
            batch.setStatus(BatchStatus.SUCCESS.getCode());
        } else if (success == 0) {
            batch.setStatus(BatchStatus.FAIL.getCode());
        } else {
            batch.setStatus(BatchStatus.PARTIAL_SUCCESS.getCode());
        }
        batchMapper.updateById(batch);
    }

    @Override
    public boolean retry(Long detailId) {
        // 重试逻辑暂不修改，因为它依赖已有的 detail 记录
        return false;
    }
}
