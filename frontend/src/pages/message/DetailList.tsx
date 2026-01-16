import React, { useEffect, useState } from 'react';
import { Table, Button, Tag, message, Card, Form, Input, Select, Space, Typography } from 'antd';

const { Paragraph } = Typography;
import { SearchOutlined, ReloadOutlined } from '@ant-design/icons';
import type { ColumnsType } from 'antd/es/table';
import { getDetailPage, retryMessage } from '../../api/message';

const DetailList: React.FC = () => {
  const [form] = Form.useForm();
  const [loading, setLoading] = useState(false);
  const [data, setData] = useState<any[]>([]);
  const [total, setTotal] = useState(0);
  const [current, setCurrent] = useState(1);
  const [pageSize, setPageSize] = useState(10);

  const loadData = async (page = current, size = pageSize) => {
    setLoading(true);
    try {
      const values = form.getFieldsValue();
      const res = await getDetailPage({ current: page, size, ...values });
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

  const handleSearch = () => {
    setCurrent(1);
    loadData(1, pageSize);
  };

  const handleReset = () => {
    form.resetFields();
    handleSearch();
  };

  const handleRetry = async (id: number) => {
    try {
      await retryMessage(id);
      message.success('已提交重试');
      loadData();
    } catch (error) {
      console.error(error);
    }
  };

  const columns: ColumnsType<any> = [
    {
      title: 'ID',
      dataIndex: 'id',
      key: 'id',
      width: 80,
    },
    {
      title: '批次ID',
      dataIndex: 'batchId',
      key: 'batchId',
      width: 80,
    },
    {
      title: '接收者',
      dataIndex: 'recipient',
      key: 'recipient',
    },
    {
        title: '姓名',
        dataIndex: 'recipientName',
        key: 'recipientName',
        render: (text) => text || '-'
    },
    {
        title: '发送内容',
        dataIndex: 'content',
        key: 'content',
        width: 250,
        render: (text) => text ? (
          <Paragraph 
            ellipsis={{ rows: 2, expandable: true, symbol: '展开' }} 
            style={{ marginBottom: 0, fontSize: 12 }}
            copyable
          >
            {text}
          </Paragraph>
        ) : '-'
    },
    {
        title: '状态',
        dataIndex: 'status',
        key: 'status',
        render: (status) => {
            // 10发送中 20发送成功 30发送失败
            const map: Record<number, any> = {
                10: { color: 'blue', text: '发送中' },
                20: { color: 'green', text: '成功' },
                30: { color: 'red', text: '失败' },
            };
            const config = map[status] || { color: 'default', text: '未知' };
            return <Tag color={config.color}>{config.text}</Tag>;
        }
    },
    {
      title: '失败原因',
      dataIndex: 'errorMsg',
      key: 'errorMsg',
      width: 200,
      render: (text) => text ? (
        <Paragraph 
          ellipsis={{ rows: 2, expandable: true, symbol: '展开' }} 
          style={{ marginBottom: 0, fontSize: 12 }}
          copyable
        >
          {text}
        </Paragraph>
      ) : '-'
    },
    {
      title: '重试次数',
      dataIndex: 'retryCount',
      key: 'retryCount',
    },
    {
      title: '发送时间',
      dataIndex: 'sendTime',
      key: 'sendTime',
    },
    {
      title: '操作',
      key: 'action',
      fixed: 'right',
      width: 100,
      render: (_, record) => (
        <Button 
          type="link" 
          disabled={record.status !== 30}
          onClick={() => handleRetry(record.id)}
        >
          重试
        </Button>
      ),
    },
  ];

  return (
    <Card variant="borderless" style={{ borderRadius: 12 }}>
      <Form form={form} layout="inline" style={{ marginBottom: 24 }}>
        <Form.Item name="batchId" label="批次ID">
            <Input placeholder="请输入批次ID" allowClear onPressEnter={handleSearch} />
        </Form.Item>
        <Form.Item name="recipient" label="接收者">
            <Input placeholder="请输入手机号/邮箱/ID" allowClear onPressEnter={handleSearch} />
        </Form.Item>
        <Form.Item name="status" label="状态">
            <Select placeholder="请选择状态" allowClear style={{ width: 120 }}>
                <Select.Option value={10}>发送中</Select.Option>
                <Select.Option value={20}>成功</Select.Option>
                <Select.Option value={30}>失败</Select.Option>
            </Select>
        </Form.Item>
        <Form.Item>
            <Space>
                <Button type="primary" icon={<SearchOutlined />} onClick={handleSearch}>查询</Button>
                <Button icon={<ReloadOutlined />} onClick={handleReset}>重置</Button>
            </Space>
        </Form.Item>
      </Form>
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
    </Card>
  );
};

export default DetailList;
