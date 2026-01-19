import React, { useEffect, useState } from 'react';
import { Table, Button, Space, Modal, Form, Input, Select, message, Popconfirm, Tag, Card } from 'antd';
import { EditOutlined, PoweroffOutlined, ExperimentOutlined, DeleteOutlined } from '@ant-design/icons';
import type { ColumnsType } from 'antd/es/table';
import { getChannelPage, createChannel, updateChannel, deleteChannel, updateChannelStatus, testChannel } from '../../api/channel';
import type { Channel } from '../../api/channel';
import ChannelForm from './components/ChannelForm';
import ChannelIcon from './components/ChannelIcon';

const CHANNEL_TYPES = [
  { label: '阿里云短信', value: 'SMS', icon: 'SMS' },
  { label: '邮件', value: 'EMAIL', icon: 'EMAIL' },
  { label: '微信服务号', value: 'WECHAT_OFFICIAL', icon: 'WECHAT_OFFICIAL' },
  { label: '企业微信', value: 'WECHAT_WORK', icon: 'WECHAT_WORK' },
  { label: '钉钉', value: 'DINGTALK', icon: 'DINGTALK' },
  { label: '飞书', value: 'FEISHU', icon: 'FEISHU' },
  { label: 'Telegram', value: 'TELEGRAM', icon: 'TELEGRAM' },
  { label: 'Slack', value: 'SLACK', icon: 'SLACK' },
  { label: '腾讯云短信', value: 'TENCENT_SMS', icon: 'TENCENT_SMS' },
  { label: 'Twilio', value: 'TWILIO', icon: 'TWILIO' },
  { label: 'Webhook', value: 'WEBHOOK', icon: 'WEBHOOK' },
];

const ChannelList: React.FC = () => {
  const [loading, setLoading] = useState(false);
  const [data, setData] = useState<Channel[]>([]);
  const [total, setTotal] = useState(0);
  const [current, setCurrent] = useState(1);
  const [pageSize, setPageSize] = useState(10);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [editingId, setEditingId] = useState<number | null>(null);
  const [form] = Form.useForm();
  const [selectedType, setSelectedType] = useState<string>('');

  const loadData = async (page = current, size = pageSize) => {
    setLoading(true);
    try {
      const res = await getChannelPage({ current: page, size });
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
    setEditingId(null);
    form.resetFields();
    setSelectedType('');
    setIsModalOpen(true);
  };

  const handleEdit = (record: Channel) => {
    setEditingId(record.id);
    const config = JSON.parse(record.configJson || '{}');
    form.setFieldsValue({
      ...record,
      ...config
    });
    setSelectedType(record.type);
    setIsModalOpen(true);
  };

  const handleDelete = async (id: number) => {
    try {
      await deleteChannel(id);
      message.success('删除成功');
      loadData();
    } catch (error) {
      console.error(error);
    }
  };

  const handleStatusChange = async (record: Channel) => {
    try {
      const newStatus = record.status === 1 ? 0 : 1;
      await updateChannelStatus(record.id, newStatus);
      message.success('状态更新成功');
      loadData();
    } catch (error) {
      console.error(error);
    }
  };

  const handleOk = async () => {
    try {
      const values = await form.validateFields();
      // Extract config fields
      const { name, type, provider, ...config } = values;
      const configJson = JSON.stringify(config);
      
      const payload = {
        name,
        type,
        provider: provider || 'default', // Default provider if not specified
        configJson
      };

      if (editingId) {
        await updateChannel(editingId, payload);
        message.success('更新成功');
      } else {
        await createChannel(payload);
        message.success('创建成功');
      }
      setIsModalOpen(false);
      loadData();
    } catch (error) {
      console.error(error);
    }
  };

  const handleTest = async (id: number) => {
      try {
          const res = await testChannel(id);
          message.info(res as any); // backend returns string "测试功能待实现" inside Result
      } catch (error) {
          console.error(error);
      }
  }

  const columns: ColumnsType<Channel> = [
    {
      title: 'ID',
      dataIndex: 'id',
      key: 'id',
      width: 60,
    },
    {
      title: '名称',
      dataIndex: 'name',
      key: 'name',
    },
    {
      title: '类型',
      dataIndex: 'type',
      key: 'type',
      render: (type) => {
          const found = CHANNEL_TYPES.find(t => t.value === type);
          return (
            <Space align="center">
              <ChannelIcon type={type} size={18} />
              <span>{found ? found.label : type}</span>
            </Space>
          );
      }
    },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      render: (status) => status === 1 ? <Tag color="green">启用</Tag> : <Tag color="red">禁用</Tag>
    },
    {
      title: '创建时间',
      dataIndex: 'createdAt',
      key: 'createdAt',
    },
    {
      title: '操作',
      key: 'action',
      fixed: 'right',
      width: 250,
      render: (_, record) => (
        <Space size="middle">
          <Button type="link" icon={<EditOutlined />} onClick={() => handleEdit(record)}>编辑</Button>
          <Button type="link" icon={<PoweroffOutlined />} onClick={() => handleStatusChange(record)}>
            {record.status === 1 ? '禁用' : '启用'}
          </Button>
          <Button type="link" icon={<ExperimentOutlined />} onClick={() => handleTest(record.id)}>测试</Button>
          <Popconfirm title="确定删除吗?" onConfirm={() => handleDelete(record.id)}>
            <Button type="link" danger icon={<DeleteOutlined />}>删除</Button>
          </Popconfirm>
        </Space>
      ),
    },
  ];

  return (
    <Card variant="borderless" style={{ borderRadius: 12 }}>
      <div style={{ marginBottom: 16 }}>
        <Button type="primary" onClick={handleCreate}>
          新增渠道
        </Button>
      </div>
      <Table
        columns={columns}
        dataSource={data}
        rowKey="id"
        loading={loading}
        scroll={{ x: 'max-content' }}
        pagination={{
          current,
          pageSize,
          total,
          onChange: (page, size) => loadData(page, size),
        }}
      />
      <Modal
        title={editingId ? '编辑渠道' : '新增渠道'}
        open={isModalOpen}
        onOk={handleOk}
        onCancel={() => setIsModalOpen(false)}
        width={600}
      >
        <Form form={form} layout="vertical">
          <Form.Item name="name" label="渠道名称" rules={[{ required: true }]}>
            <Input />
          </Form.Item>
          <Form.Item name="type" label="渠道类型" rules={[{ required: true }]}>
            <Select 
              onChange={(value) => setSelectedType(value)}
              disabled={!!editingId}
              optionLabelProp="label"
            >
              {CHANNEL_TYPES.map(channel => (
                <Select.Option 
                  key={channel.value} 
                  value={channel.value}
                  label={
                    <Space align="center">
                      <ChannelIcon type={channel.icon} size={16} />
                      <span>{channel.label}</span>
                    </Space>
                  }
                >
                  <Space align="center">
                    <ChannelIcon type={channel.icon} size={16} />
                    <span>{channel.label}</span>
                  </Space>
                </Select.Option>
              ))}
            </Select>
          </Form.Item>
          
          {selectedType && <ChannelForm type={selectedType} />}

        </Form>
      </Modal>
    </Card>
  );
};

export default ChannelList;
