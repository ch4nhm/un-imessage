import React, { useEffect, useState } from 'react';
import {
  Table, Button, Space, Modal, Form, Input, InputNumber, message,
  Popconfirm, Tag, Card, Typography, Tooltip, Drawer, Descriptions, List
} from 'antd';
import { CopyOutlined, BarChartOutlined, LinkOutlined, PoweroffOutlined, DeleteOutlined } from '@ant-design/icons';
import type { ColumnsType } from 'antd/es/table';
import {
  getShortUrlPage, createShortUrl, disableShortUrl, enableShortUrl,
  deleteShortUrl, getShortUrlStats
} from '../../api/shortUrl';
import type { ShortUrl, ShortUrlStats } from '../../api/shortUrl';

const { Text, Link } = Typography;

const ShortUrlList: React.FC = () => {
  const [loading, setLoading] = useState(false);
  const [data, setData] = useState<ShortUrl[]>([]);
  const [total, setTotal] = useState(0);
  const [current, setCurrent] = useState(1);
  const [pageSize, setPageSize] = useState(10);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [isStatsOpen, setIsStatsOpen] = useState(false);
  const [statsData, setStatsData] = useState<ShortUrlStats | null>(null);
  const [statsLoading, setStatsLoading] = useState(false);
  const [form] = Form.useForm();

  // 搜索条件
  const [searchCode, setSearchCode] = useState('');
  const [searchUrl, setSearchUrl] = useState('');

  const loadData = async (page = current, size = pageSize) => {
    setLoading(true);
    try {
      const res = await getShortUrlPage({
        current: page,
        size,
        shortCode: searchCode || undefined,
        originalUrl: searchUrl || undefined,
      });
      setData(res.records);
      setTotal(res.total);
      setCurrent(page);
      setPageSize(size);
    } catch (error) {
      console.error(error);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadData();
  }, []);

  const handleCreate = () => {
    form.resetFields();
    setIsModalOpen(true);
  };

  const handleOk = async () => {
    try {
      const values = await form.validateFields();
      await createShortUrl({
        url: values.url,
        customCode: values.customCode || undefined,
        ttl: values.ttl ? values.ttl * 3600 : undefined, // 转换为秒
      });
      message.success('创建成功');
      setIsModalOpen(false);
      loadData();
    } catch (error: any) {
      console.error(error);
      if (error?.response?.data?.message) {
         message.error(error.response.data.message);
      }
    }
  };

  const handleToggleStatus = async (record: ShortUrl) => {
    try {
      if (record.status === 1) {
        await disableShortUrl(record.shortCode);
        message.success('已禁用');
      } else {
        await enableShortUrl(record.shortCode);
        message.success('已启用');
      }
      loadData();
    } catch (error) {
      console.error(error);
    }
  };

  const handleDelete = async (shortCode: string) => {
    try {
      await deleteShortUrl(shortCode);
      message.success('删除成功');
      loadData();
    } catch (error) {
      console.error(error);
    }
  };

  const handleViewStats = async (shortCode: string) => {
    setStatsLoading(true);
    setIsStatsOpen(true);
    try {
      const stats = await getShortUrlStats(shortCode);
      setStatsData(stats);
    } catch (error) {
      console.error(error);
    } finally {
      setStatsLoading(false);
    }
  };

  const copyToClipboard = async (text: string) => {
    try {
      if (navigator.clipboard && window.isSecureContext) {
        await navigator.clipboard.writeText(text);
      } else {
        // fallback for non-secure contexts
        const textArea = document.createElement('textarea');
        textArea.value = text;
        textArea.style.position = 'fixed';
        textArea.style.left = '-9999px';
        document.body.appendChild(textArea);
        textArea.select();
        document.execCommand('copy');
        document.body.removeChild(textArea);
      }
      message.success('已复制到剪贴板');
    } catch (err) {
      console.error('复制失败:', err);
      message.error('复制失败，请手动复制');
    }
  };

  const columns: ColumnsType<ShortUrl> = [
    {
      title: '短链码',
      dataIndex: 'shortCode',
      key: 'shortCode',
      width: 140,
      render: (code) => (
        <Space size={4}>
          <Tag color="purple" style={{ margin: 0, fontFamily: 'monospace', fontSize: 13 }}>
            {code}
          </Tag>
          <Tooltip title="复制短链码">
            <Button
              type="text"
              size="small"
              icon={<CopyOutlined style={{ fontSize: 12, color: '#8c8c8c' }} />}
              onClick={() => copyToClipboard(code)}
              style={{ padding: '0 4px', height: 22 }}
            />
          </Tooltip>
        </Space>
      ),
    },
    {
      title: '短链接',
      dataIndex: 'shortUrl',
      key: 'shortUrl',
      width: 320,
      render: (url) => (
        <Space size={4} style={{ display: 'flex', alignItems: 'center' }}>
          <LinkOutlined style={{ color: '#1890ff', fontSize: 14 }} />
          <Link 
            href={url} 
            target="_blank" 
            style={{ 
              maxWidth: 240, 
              overflow: 'hidden', 
              textOverflow: 'ellipsis', 
              whiteSpace: 'nowrap',
              display: 'inline-block'
            }}
          >
            {url}
          </Link>
          <Tooltip title="复制短链接">
            <Button
              type="text"
              size="small"
              icon={<CopyOutlined style={{ fontSize: 12, color: '#8c8c8c' }} />}
              onClick={() => copyToClipboard(url)}
              style={{ padding: '0 4px', height: 22 }}
            />
          </Tooltip>
        </Space>
      ),
    },
    {
      title: '原始链接',
      dataIndex: 'originalUrl',
      key: 'originalUrl',
      ellipsis: true,
      render: (url) => (
        <Space size={4} style={{ display: 'flex', alignItems: 'center' }}>
          <Tooltip title={url}>
            <Link 
              href={url} 
              target="_blank" 
              style={{ 
                maxWidth: 280, 
                overflow: 'hidden', 
                textOverflow: 'ellipsis', 
                whiteSpace: 'nowrap',
                display: 'inline-block',
                color: '#595959'
              }}
            >
              {url}
            </Link>
          </Tooltip>
          <Tooltip title="复制原始链接">
            <Button
              type="text"
              size="small"
              icon={<CopyOutlined style={{ fontSize: 12, color: '#8c8c8c' }} />}
              onClick={() => copyToClipboard(url)}
              style={{ padding: '0 4px', height: 22 }}
            />
          </Tooltip>
        </Space>
      ),
    },
    {
      title: '点击量',
      dataIndex: 'clickCount',
      key: 'clickCount',
      width: 90,
      align: 'center',
      render: (count) => (
        <Tag 
          color={count > 100 ? 'gold' : count > 10 ? 'blue' : 'default'} 
          style={{ minWidth: 40, textAlign: 'center' }}
        >
          {count}
        </Tag>
      ),
    },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      width: 80,
      align: 'center',
      render: (status) =>
        status === 1 ? (
          <Tag color="success" style={{ margin: 0 }}>启用</Tag>
        ) : (
          <Tag color="error" style={{ margin: 0 }}>禁用</Tag>
        ),
    },
    {
      title: '过期时间',
      dataIndex: 'expireAt',
      key: 'expireAt',
      width: 170,
      render: (time) => 
        time ? (
          <Text style={{ fontSize: 13 }}>{time}</Text>
        ) : (
          <Tag color="cyan" style={{ margin: 0 }}>永不过期</Tag>
        ),
    },
    {
      title: '创建时间',
      dataIndex: 'createdAt',
      key: 'createdAt',
      width: 170,
      render: (time) => <Text type="secondary" style={{ fontSize: 13 }}>{time}</Text>,
    },
    {
      title: '操作',
      key: 'action',
      fixed: 'right',
      width: 200,
      render: (_, record) => (
        <Space size="small">
          <Button
            type="link"
            size="small"
            icon={<BarChartOutlined />}
            onClick={() => handleViewStats(record.shortCode)}
          >
            统计
          </Button>
          <Button type="link" size="small" icon={<PoweroffOutlined />} onClick={() => handleToggleStatus(record)}>
            {record.status === 1 ? '禁用' : '启用'}
          </Button>
          <Popconfirm title="确定删除吗?" onConfirm={() => handleDelete(record.shortCode)}>
            <Button type="link" size="small" danger icon={<DeleteOutlined />}>
              删除
            </Button>
          </Popconfirm>
        </Space>
      ),
    },
  ];

  return (
    <Card variant="borderless" style={{ borderRadius: 12 }}>
      <div style={{ marginBottom: 16, display: 'flex', justifyContent: 'space-between' }}>
        <Space>
          <Input
            placeholder="短链码"
            value={searchCode}
            onChange={(e) => setSearchCode(e.target.value)}
            style={{ width: 150 }}
            allowClear
          />
          <Input
            placeholder="原始链接"
            value={searchUrl}
            onChange={(e) => setSearchUrl(e.target.value)}
            style={{ width: 250 }}
            allowClear
          />
          <Button type="primary" onClick={() => loadData(1)}>
            搜索
          </Button>
          <Button onClick={() => { setSearchCode(''); setSearchUrl(''); loadData(1); }}>
            重置
          </Button>
        </Space>
        <Button type="primary" onClick={handleCreate}>
          创建短链接
        </Button>
      </div>

      <Table
        columns={columns}
        dataSource={data}
        rowKey="shortCode"
        loading={loading}
        scroll={{ x: 'max-content' }}
        pagination={{
          current,
          pageSize,
          total,
          showSizeChanger: true,
          showTotal: (t) => `共 ${t} 条`,
          onChange: (page, size) => loadData(page, size),
        }}
      />

      {/* 创建短链接弹窗 */}
      <Modal
        title="创建短链接"
        open={isModalOpen}
        onOk={handleOk}
        onCancel={() => setIsModalOpen(false)}
        width={500}
      >
        <Form form={form} layout="vertical">
          <Form.Item
            name="url"
            label="原始链接"
            rules={[
              { required: true, message: '请输入原始链接' },
              { type: 'url', message: '请输入有效的URL' },
            ]}
          >
            <Input placeholder="https://example.com/very/long/url" />
          </Form.Item>
          <Form.Item
            name="customCode"
            label="自定义短链码"
            rules={[
              { min: 4, max: 10, message: '长度需在4-10位之间' },
              { pattern: /^[a-zA-Z0-9]*$/, message: '只能包含字母和数字' },
            ]}
          >
            <Input placeholder="可选，留空自动生成" />
          </Form.Item>
          <Form.Item name="ttl" label="有效期(小时)">
            <InputNumber min={0} placeholder="0表示永不过期" style={{ width: '100%' }} />
          </Form.Item>
        </Form>
      </Modal>

      {/* 统计信息抽屉 */}
      <Drawer
        title="短链接统计"
        placement="right"
        styles={{ wrapper: { width: 500 } }}
        open={isStatsOpen}
        onClose={() => setIsStatsOpen(false)}
      >
        {statsLoading ? (
          <div style={{ textAlign: 'center', padding: 50 }}>加载中...</div>
        ) : statsData ? (
          <>
            <Descriptions column={1} bordered size="small">
              <Descriptions.Item label="短链码">
                <Text code>{statsData.shortCode}</Text>
              </Descriptions.Item>
              <Descriptions.Item label="原始链接">
                <Link href={statsData.originalUrl} target="_blank" style={{ wordBreak: 'break-all' }}>
                  {statsData.originalUrl}
                </Link>
              </Descriptions.Item>
              <Descriptions.Item label="总点击量">
                <Tag color="blue">{statsData.totalClicks}</Tag>
              </Descriptions.Item>
              <Descriptions.Item label="今日点击">
                <Tag color="green">{statsData.todayClicks}</Tag>
              </Descriptions.Item>
              <Descriptions.Item label="创建时间">{statsData.createdAt}</Descriptions.Item>
            </Descriptions>

            <div style={{ marginTop: 24 }}>
              <Text strong>最近访问记录</Text>
              <List
                size="small"
                dataSource={statsData.recentAccess}
                style={{ marginTop: 12 }}
                renderItem={(item) => (
                  <List.Item>
                    <List.Item.Meta
                      title={<Text type="secondary">{item.accessTime}</Text>}
                      description={
                        <>
                          <div>IP: {item.ip}</div>
                          <div style={{ fontSize: 12, color: '#999', wordBreak: 'break-all' }}>
                            {item.userAgent?.substring(0, 100)}...
                          </div>
                        </>
                      }
                    />
                  </List.Item>
                )}
              />
            </div>
          </>
        ) : null}
      </Drawer>
    </Card>
  );
};

export default ShortUrlList;
