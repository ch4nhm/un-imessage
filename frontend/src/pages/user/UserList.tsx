import React, { useEffect, useState } from 'react';
import { Table, Button, Space, Modal, Form, Input, message, Popconfirm, Card } from 'antd';
import { EditOutlined, KeyOutlined, DeleteOutlined } from '@ant-design/icons';
import type { ColumnsType } from 'antd/es/table';
import { getUserPage, createUser, updateUser, deleteUser, resetPassword, type User } from '../../api/user';

const UserList: React.FC = () => {
  const [loading, setLoading] = useState(false);
  const [data, setData] = useState<User[]>([]);
  const [total, setTotal] = useState(0);
  const [current, setCurrent] = useState(1);
  const [pageSize, setPageSize] = useState(10);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [isResetModalOpen, setIsResetModalOpen] = useState(false);
  const [editingId, setEditingId] = useState<number | null>(null);
  const [form] = Form.useForm();
  const [resetForm] = Form.useForm();

  const loadData = async (page = current, size = pageSize) => {
    setLoading(true);
    try {
      const res = await getUserPage({ current: page, size });
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

  const handleEdit = (record: User) => {
    setEditingId(record.id);
    form.setFieldsValue(record);
    setIsModalOpen(true);
  };

  const handleDelete = async (id: number) => {
    try {
      await deleteUser(id);
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
        await updateUser(editingId, values);
        message.success('更新成功');
      } else {
        await createUser(values);
        message.success('创建成功');
      }
      setIsModalOpen(false);
      loadData();
    } catch (error) {
      console.error(error);
    }
  };

  const handleResetPassword = (record: User) => {
    setEditingId(record.id);
    resetForm.resetFields();
    setIsResetModalOpen(true);
  };

  const handleResetOk = async () => {
    try {
      const values = await resetForm.validateFields();
      if (editingId) {
        await resetPassword(editingId, values);
        message.success('密码重置成功');
        setIsResetModalOpen(false);
      }
    } catch (error) {
      console.error(error);
    }
  };

  const columns: ColumnsType<User> = [
    {
      title: 'ID',
      dataIndex: 'id',
      key: 'id',
    },
    {
      title: '用户名',
      dataIndex: 'username',
      key: 'username',
    },
    {
      title: '昵称',
      dataIndex: 'nickname',
      key: 'nickname',
    },
    {
      title: '邮箱',
      dataIndex: 'email',
      key: 'email',
    },
    {
      title: '手机号',
      dataIndex: 'phone',
      key: 'phone',
    },
    {
      title: '创建时间',
      dataIndex: 'createTime',
      key: 'createTime',
    },
    {
      title: '操作',
      key: 'action',
      fixed: 'right',
      width: 250,
      render: (_, record) => (
        <Space size="middle">
          <Button type="link" icon={<EditOutlined />} onClick={() => handleEdit(record)}>编辑</Button>
          <Button type="link" icon={<KeyOutlined />} onClick={() => handleResetPassword(record)}>重置密码</Button>
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
          新增用户
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
        title={editingId ? '编辑用户' : '新增用户'}
        open={isModalOpen}
        onOk={handleOk}
        onCancel={() => setIsModalOpen(false)}
      >
        <Form form={form} layout="vertical">
          <Form.Item name="username" label="用户名" rules={[{ required: true }]}>
            <Input disabled={!!editingId} />
          </Form.Item>
          {!editingId && (
            <Form.Item name="password" label="密码" rules={[{ required: true }]}>
              <Input.Password />
            </Form.Item>
          )}
          <Form.Item name="nickname" label="昵称">
            <Input />
          </Form.Item>
          <Form.Item name="email" label="邮箱">
            <Input />
          </Form.Item>
          <Form.Item name="phone" label="手机号">
            <Input />
          </Form.Item>
        </Form>
      </Modal>
      
      <Modal
        title="重置密码"
        open={isResetModalOpen}
        onOk={handleResetOk}
        onCancel={() => setIsResetModalOpen(false)}
      >
        <Form form={resetForm} layout="vertical">
            <Form.Item name="password" label="新密码" rules={[{ required: true }]}>
              <Input.Password />
            </Form.Item>
        </Form>
      </Modal>
    </Card>
  );
};

export default UserList;
