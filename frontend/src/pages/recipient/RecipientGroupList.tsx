import React, { useState, useEffect } from 'react';
import { Table, Card, Button, Input, Modal, Form, message, Space, Popconfirm, Tag, Select } from 'antd';
import { PlusOutlined, EditOutlined, DeleteOutlined, SearchOutlined } from '@ant-design/icons';
import type { ColumnsType } from 'antd/es/table';
import { 
  getRecipientGroupPage, 
  createRecipientGroup, 
  updateRecipientGroup, 
  deleteRecipientGroup,
  getGroupRecipientIds
} from '../../api/recipientGroup';
import type { RecipientGroup } from '../../api/recipientGroup';
import { getRecipientList } from '../../api/recipient';
import type { Recipient } from '../../api/recipient';

const RecipientGroupList: React.FC = () => {
  const [loading, setLoading] = useState(false);
  const [data, setData] = useState<RecipientGroup[]>([]);
  const [total, setTotal] = useState(0);
  const [current, setCurrent] = useState(1);
  const [pageSize, setPageSize] = useState(10);
  
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [editingId, setEditingId] = useState<number | null>(null);
  const [form] = Form.useForm();
  
  const [searchName, setSearchName] = useState('');
  
  // For recipient selection
  const [recipients, setRecipients] = useState<Recipient[]>([]);
  const [loadingRecipients, setLoadingRecipients] = useState(false);

  const loadData = async (page = current, size = pageSize) => {
    setLoading(true);
    try {
      const res = await getRecipientGroupPage({ 
        current: page, 
        size,
        groupName: searchName
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

  const loadRecipients = async () => {
      setLoadingRecipients(true);
      try {
          const res = await getRecipientList();
          setRecipients(res as any);
      } catch (error) {
          console.error(error);
      } finally {
          setLoadingRecipients(false);
      }
  };

  useEffect(() => {
    loadData();
    loadRecipients();
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

  const handleEdit = async (record: RecipientGroup) => {
    setEditingId(record.id);
    form.setFieldsValue(record);
    
    // Fetch associated recipients
    try {
        const ids = await getGroupRecipientIds(record.id);
        form.setFieldValue('recipientIds', ids);
    } catch (e) {
        console.error(e);
    }
    
    setIsModalOpen(true);
  };

  const handleDelete = async (id: number) => {
    try {
      await deleteRecipientGroup(id);
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
        await updateRecipientGroup(editingId, values);
        message.success('更新成功');
      } else {
        await createRecipientGroup(values);
        message.success('创建成功');
      }
      setIsModalOpen(false);
      loadData();
    } catch (error) {
      console.error(error);
    }
  };

  const columns: ColumnsType<RecipientGroup> = [
    {
      title: 'ID',
      dataIndex: 'id',
      width: 80,
    },
    {
      title: '分组名称',
      dataIndex: 'groupName',
    },
    {
      title: '描述',
      dataIndex: 'description',
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
            placeholder="请输入分组名称" 
            value={searchName}
            onChange={e => setSearchName(e.target.value)}
            style={{ width: 200 }}
          />
          <Button type="primary" icon={<SearchOutlined />} onClick={handleSearch}>
            查询
          </Button>
        </Space>
        <Button type="primary" icon={<PlusOutlined />} onClick={handleCreate}>
          新增分组
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
        title={editingId ? '编辑分组' : '新增分组'}
        open={isModalOpen}
        onOk={handleOk}
        onCancel={() => setIsModalOpen(false)}
        width={600}
      >
        <Form form={form} layout="vertical">
          <Form.Item name="groupName" label="分组名称" rules={[{ required: true }]}>
            <Input />
          </Form.Item>
          <Form.Item name="description" label="描述">
            <Input.TextArea />
          </Form.Item>
          <Form.Item name="recipientIds" label="包含接收者">
            <Select 
                mode="multiple" 
                placeholder="请选择接收者"
                loading={loadingRecipients}
                optionFilterProp="children"
                filterOption={(input, option) => 
                    (option?.label ?? '').toString().toLowerCase().includes(input.toLowerCase())
                }
                options={recipients.map(r => ({
                    label: `${r.name} (${r.mobile || r.email || r.userId || '无联系方式'})`,
                    value: r.id
                }))}
            />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
};

export default RecipientGroupList;
