package com.unimessage.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.unimessage.common.Result;
import com.unimessage.dto.SysRecipientGroupDto;
import com.unimessage.entity.SysRecipientGroup;
import com.unimessage.entity.SysRecipientGroupRelation;
import com.unimessage.mapper.SysRecipientGroupMapper;
import com.unimessage.mapper.SysRecipientGroupRelationMapper;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 接收者分组管理控制器
 *
 * @author 海明
 * @since 2025-12-11
 */
@RestController
@RequestMapping("/api/v1/recipient-group")
public class SysRecipientGroupController {

    @Resource
    private SysRecipientGroupMapper groupMapper;

    @Resource
    private SysRecipientGroupRelationMapper relationMapper;

    /**
     * 分页查询分组列表
     */
    @GetMapping("/page")
    public Result<IPage<SysRecipientGroup>> page(@RequestParam(defaultValue = "1") Integer current,
                                                 @RequestParam(defaultValue = "10") Integer size,
                                                 @RequestParam(required = false) String groupName) {
        Page<SysRecipientGroup> page = new Page<>(current, size);
        LambdaQueryWrapper<SysRecipientGroup> wrapper = new LambdaQueryWrapper<>();
        if (groupName != null && !groupName.isEmpty()) {
            wrapper.like(SysRecipientGroup::getName, groupName);
        }
        wrapper.orderByDesc(SysRecipientGroup::getCreatedAt);
        return Result.success(groupMapper.selectPage(page, wrapper));
    }

    /**
     * 查询所有分组
     */
    @GetMapping("/list")
    public Result<List<SysRecipientGroup>> list() {
        LambdaQueryWrapper<SysRecipientGroup> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(SysRecipientGroup::getCreatedAt);
        return Result.success(groupMapper.selectList(wrapper));
    }

    /**
     * 根据ID查询分组
     */
    @GetMapping("/{id}")
    public Result<SysRecipientGroup> getById(@PathVariable Long id) {
        return Result.success(groupMapper.selectById(id));
    }

    /**
     * 查询分组关联的接收者ID列表
     */
    @GetMapping("/{id}/recipients")
    public Result<List<Long>> getRecipientIds(@PathVariable Long id) {
        LambdaQueryWrapper<SysRecipientGroupRelation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysRecipientGroupRelation::getGroupId, id);
        List<SysRecipientGroupRelation> relations = relationMapper.selectList(wrapper);
        List<Long> recipientIds = relations.stream()
                .map(SysRecipientGroupRelation::getRecipientId)
                .collect(Collectors.toList());
        return Result.success(recipientIds);
    }

    /**
     * 创建分组
     */
    @PostMapping
    @Transactional(rollbackFor = Exception.class)
    public Result<SysRecipientGroup> create(@RequestBody SysRecipientGroupDto groupDto) {
        SysRecipientGroup group = new SysRecipientGroup();
        BeanUtils.copyProperties(groupDto, group);

        group.setCreatedAt(LocalDateTime.now());
        group.setUpdatedAt(LocalDateTime.now());
        groupMapper.insert(group);

        // Handle relations
        if (groupDto.getRecipientIds() != null && !groupDto.getRecipientIds().isEmpty()) {
            for (Long recipientId : groupDto.getRecipientIds()) {
                SysRecipientGroupRelation relation = new SysRecipientGroupRelation();
                relation.setGroupId(group.getId());
                relation.setRecipientId(recipientId);
                relationMapper.insert(relation);
            }
        }

        return Result.success(group);
    }

    /**
     * 更新分组
     */
    @PutMapping("/{id}")
    @Transactional(rollbackFor = Exception.class)
    public Result<SysRecipientGroup> update(@PathVariable Long id, @RequestBody SysRecipientGroupDto groupDto) {
        SysRecipientGroup group = groupMapper.selectById(id);
        if (group == null) {
            return Result.fail("分组不存在");
        }

        BeanUtils.copyProperties(groupDto, group);
        group.setId(id);
        group.setUpdatedAt(LocalDateTime.now());

        groupMapper.updateById(group);

        // Handle relations if provided (full replacement)
        if (groupDto.getRecipientIds() != null) {
            // Delete existing
            LambdaQueryWrapper<SysRecipientGroupRelation> deleteWrapper = new LambdaQueryWrapper<>();
            deleteWrapper.eq(SysRecipientGroupRelation::getGroupId, id);
            relationMapper.delete(deleteWrapper);

            // Insert new
            for (Long recipientId : groupDto.getRecipientIds()) {
                SysRecipientGroupRelation relation = new SysRecipientGroupRelation();
                relation.setGroupId(id);
                relation.setRecipientId(recipientId);
                relationMapper.insert(relation);
            }
        }

        return Result.success(group);
    }

    /**
     * 删除分组
     */
    @DeleteMapping("/{id}")
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> delete(@PathVariable Long id) {
        // Delete relations first
        LambdaQueryWrapper<SysRecipientGroupRelation> deleteWrapper = new LambdaQueryWrapper<>();
        deleteWrapper.eq(SysRecipientGroupRelation::getGroupId, id);
        relationMapper.delete(deleteWrapper);

        // Delete group
        groupMapper.deleteById(id);
        return Result.success();
    }
}