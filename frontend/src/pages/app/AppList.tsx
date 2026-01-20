import React, { useEffect, useState } from 'react';
import { Table, Button, Space, Modal, Form, Input, message, Popconfirm, Tag, Card } from 'antd';
import { EditOutlined, KeyOutlined, DeleteOutlined } from '@ant-design/icons';
import type { ColumnsType } from 'antd/es/table';
import { getAppPage, createApp, updateApp, deleteApp, resetAppSecret } from '../../api/app';
import type { App } from '../../api/app';

const AppList: React.FC = () => {
  const [loading, setLoading] = useState(false);
  const [data, setData] = useState<App[]>([]);
  const [total, setTotal] = useState(0);
  const [current, setCurrent] = useState(1);
  const [pageSize, setPageSize] = useState(10);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [editingId, setEditingId] = useState<number | null>(null);
  const [form] = Form.useForm();

  const loadData = async (page = current, size = pageSize) => {
    setLoading(true);
    try {
      const res = await getAppPage({ current: page, size });
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
    setIsModalOpen(true);
  };

  const handleEdit = (record: App) => {
    setEditingId(record.id);
    form.setFieldsValue(record);
    setIsModalOpen(true);
  };

  const handleDelete = async (id: number) => {
    try {
      await deleteApp(id);
      message.success('删除成功');
      loadData();
    } catch (error) {
      console.error(error);
    }
  };

  const handleOk = async () => {
    try {
      const values = await form.validateFields();
      if (editingId) {
        await updateApp(editingId, values);
        message.success('更新成功');
      } else {
        await createApp(values);
        message.success('创建成功');
      }
      setIsModalOpen(false);
      loadData();
    } catch (error) {
      console.error(error);
    }
  };

  const handleResetSecret = async (id: number) => {
      try {
          await resetAppSecret(id);
          message.success('密钥重置成功');
          loadData();
      } catch (error) {
          console.error(error);
      }
  }

  const columns: ColumnsType<App> = [
    {
      title: 'ID',
      dataIndex: 'id',
      key: 'id',
      width: 80,
    },
    {
      title: '应用名称',
      dataIndex: 'appName',
      key: 'appName',
    },
    {
      title: 'App Key',
      dataIndex: 'appKey',
      key: 'appKey',
    },
    {
        title: 'App Secret',
        dataIndex: 'appSecret',
        key: 'appSecret',
        render: (text) => <Tag>{text}</Tag>
    },
    {
      title: '描述',
      dataIndex: 'description',
      key: 'description',
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
          <Popconfirm title="确定重置密钥吗?" onConfirm={() => handleResetSecret(record.id)}>
            <Button type="link" icon={<KeyOutlined />}>重置密钥</Button>
          </Popconfirm>
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
          新增应用
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
        title={editingId ? '编辑应用' : '新增应用'}
        open={isModalOpen}
        onOk={handleOk}
        onCancel={() => setIsModalOpen(false)}
      >
        <Form form={form} layout="vertical">
          <Form.Item name="appName" label="应用名称" rules={[{ required: true }]}>
            <Input />
          </Form.Item>
          <Form.Item name="description" label="描述">
            <Input.TextArea />
          </Form.Item>
        </Form>
      </Modal>
    </Card>
  );
};

export default AppList;
