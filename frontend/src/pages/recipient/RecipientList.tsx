import React, { useState, useEffect } from 'react';
import { Table, Card, Button, Input, Modal, Form, message, Space, Popconfirm, Tag } from 'antd';
import { PlusOutlined, EditOutlined, DeleteOutlined, SearchOutlined } from '@ant-design/icons';
import type { ColumnsType } from 'antd/es/table';
import { 
  getRecipientPage, 
  createRecipient, 
  updateRecipient, 
  deleteRecipient
} from '../../api/recipient';
import type { Recipient } from '../../api/recipient';

const RecipientList: React.FC = () => {
  const [loading, setLoading] = useState(false);
  const [data, setData] = useState<Recipient[]>([]);
  const [total, setTotal] = useState(0);
  const [current, setCurrent] = useState(1);
  const [pageSize, setPageSize] = useState(10);
  
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [editingId, setEditingId] = useState<number | null>(null);
  const [form] = Form.useForm();
  
  const [searchName, setSearchName] = useState('');
  const [searchMobile, setSearchMobile] = useState('');

  const loadData = async (page = current, size = pageSize) => {
    setLoading(true);
    try {
      const res = await getRecipientPage({ 
        current: page, 
        size,
        name: searchName,
        mobile: searchMobile
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

  const handleSearch = () => {
    setCurrent(1);
    loadData(1, pageSize);
  };

  const handleCreate = () => {
    setEditingId(null);
    form.resetFields();
    setIsModalOpen(true);
  };

  const handleEdit = (record: Recipient) => {
    setEditingId(record.id);
    form.setFieldsValue(record);
    setIsModalOpen(true);
  };

  const handleDelete = async (id: number) => {
    try {
      await deleteRecipient(id);
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
        await updateRecipient(editingId, values);
        message.success('更新成功');
      } else {
        await createRecipient(values);
        message.success('创建成功');
      }
      setIsModalOpen(false);
      loadData();
    } catch (error) {
      console.error(error);
    }
  };

  const columns: ColumnsType<Recipient> = [
    {
      title: 'ID',
      dataIndex: 'id',
      width: 80,
    },
    {
      title: '姓名',
      dataIndex: 'name',
    },
    {
      title: '手机号',
      dataIndex: 'mobile',
    },
    {
      title: '邮箱',
      dataIndex: 'email',
    },
    {
      title: 'OpenId',
      dataIndex: 'openId',
      ellipsis: true,
    },
    {
      title: 'UserId',
      dataIndex: 'userId',
      ellipsis: true,
    },
    {
      title: '状态',
      dataIndex: 'status',
      width: 100,
      render: (status: number) => (
        status === 1 ? <Tag color="success">启用</Tag> : <Tag color="error">禁用</Tag>
      ),
    },
    {
      title: '创建时间',
      dataIndex: 'createdAt',
      width: 180,
    },
    {
      title: '操作',
      key: 'action',
      width: 150,
      fixed: 'right',
      render: (_, record) => (
        <Space size="middle">
          <Button 
            type="text" 
            icon={<EditOutlined />} 
            onClick={() => handleEdit(record)}
          >
            编辑
          </Button>
          <Popconfirm
            title="确定要删除吗？"
            onConfirm={() => handleDelete(record.id)}
            okText="确定"
            cancelText="取消"
          >
            <Button type="text" danger icon={<DeleteOutlined />}>删除</Button>
          </Popconfirm>
        </Space>
      ),
    },
  ];

  return (
    <div>
      <div style={{ marginBottom: 16, display: 'flex', justifyContent: 'space-between' }}>
        <Space>
          <Input 
            placeholder="请输入姓名" 
            value={searchName}
            onChange={e => setSearchName(e.target.value)}
            style={{ width: 200 }}
          />
          <Input 
            placeholder="请输入手机号" 
            value={searchMobile}
            onChange={e => setSearchMobile(e.target.value)}
            style={{ width: 200 }}
          />
          <Button type="primary" icon={<SearchOutlined />} onClick={handleSearch}>
            查询
          </Button>
        </Space>
        <Button type="primary" icon={<PlusOutlined />} onClick={handleCreate}>
          新增接收者
        </Button>
      </div>

      <Card variant="borderless" style={{ borderRadius: 12 }}>
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

      <Modal
        title={editingId ? '编辑接收者' : '新增接收者'}
        open={isModalOpen}
        onOk={handleOk}
        onCancel={() => setIsModalOpen(false)}
      >
        <Form form={form} layout="vertical">
          <Form.Item name="name" label="姓名" rules={[{ required: true }]}>
            <Input />
          </Form.Item>
          <Form.Item name="mobile" label="手机号">
            <Input />
          </Form.Item>
          <Form.Item name="email" label="邮箱">
            <Input />
          </Form.Item>
          <Form.Item name="openId" label="微信OpenID">
            <Input />
          </Form.Item>
          <Form.Item name="userId" label="企业用户ID (钉钉/飞书/企微)">
            <Input />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
};

export default RecipientList;
