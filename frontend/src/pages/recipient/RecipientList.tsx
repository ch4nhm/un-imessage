import React, {useEffect, useState} from 'react';
import {Button, Card, Divider, Form, Input, message, Modal, Popconfirm, Select, Space, Table, Tag} from 'antd';
import {DeleteOutlined, EditOutlined, MinusCircleOutlined, PlusOutlined, SearchOutlined} from '@ant-design/icons';
import type {ColumnsType} from 'antd/es/table';
import type {Recipient, UserIdItem} from '../../api/recipient';
import {createRecipient, deleteRecipient, getRecipientPage, updateRecipient} from '../../api/recipient';
import ChannelIcon from '../channel/components/ChannelIcon';

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

    // 渠道类型选项
    const CHANNEL_TYPES = [
        {label: '微信服务号', value: 'WECHAT_OFFICIAL'},
        {label: '企业微信', value: 'WECHAT_WORK'},
        {label: '钉钉', value: 'DINGTALK'},
        {label: '飞书', value: 'FEISHU'},
        {label: 'Telegram', value: 'TELEGRAM'},
        {label: 'Slack', value: 'SLACK'},
    ];

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
        // 设置默认的 userIds 数组
        form.setFieldsValue({
            userIds: [{channelType: '', userId: ''}]
        });
        setIsModalOpen(true);
    };

    const handleEdit = (record: Recipient) => {
        setEditingId(record.id);

        // 解析 userId JSON 字符串为表单数据
        let userIds: UserIdItem[] = [];
        if (record.userId) {
            try {
                const userIdObj = JSON.parse(record.userId);
                userIds = Object.entries(userIdObj).map(([channelType, userId]) => ({
                    channelType,
                    userId: userId as string
                }));
            } catch (e) {
                console.warn('解析 userId JSON 失败:', e);
            }
        }

        form.setFieldsValue({
            ...record,
            userIds: userIds.length > 0 ? userIds : [{channelType: '', userId: ''}]
        });
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

            // 将 userIds 数组转换为 JSON 字符串
            let userId = '';
            if (values.userIds && values.userIds.length > 0) {
                const userIdObj: Record<string, string> = {};
                values.userIds.forEach((item: UserIdItem) => {
                    if (item.channelType && item.userId) {
                        userIdObj[item.channelType] = item.userId;
                    }
                });
                userId = Object.keys(userIdObj).length > 0 ? JSON.stringify(userIdObj) : '';
            }

            const payload = {
                ...values,
                userId,
                userIds: undefined // 移除临时字段
            };

            if (editingId) {
                await updateRecipient(editingId, payload);
                message.success('更新成功');
            } else {
                await createRecipient(payload);
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
            title: '渠道配置信息',
            dataIndex: 'userId',
            ellipsis: true,
            render: (userId: string) => {
                if (!userId) return '-';
                try {
                    const userIdObj = JSON.parse(userId);
                    const entries = Object.entries(userIdObj);
                    if (entries.length === 0) return '-';

                    return (
                        <div>
                            {entries.map(([channelType, id]) => {
                                const channel = CHANNEL_TYPES.find(c => c.value === channelType);
                                return (
                                    <div key={channelType}
                                         style={{display: 'flex', alignItems: 'center', marginBottom: '4px'}}>
                                        <ChannelIcon type={channelType} size={14}/>
                                        <Tag color="blue"
                                             style={{margin: '0 4px'}}>{channel?.label || channelType}</Tag>
                                        <span>{id as string}</span>
                                    </div>
                                );
                            })}
                        </div>
                    );
                } catch (e) {
                    return userId; // 如果不是 JSON 格式，直接显示原值
                }
            }
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
                        icon={<EditOutlined/>}
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
                        <Button type="text" danger icon={<DeleteOutlined/>}>删除</Button>
                    </Popconfirm>
                </Space>
            ),
        },
    ];

    return (
        <div>
            <div style={{marginBottom: 16, display: 'flex', justifyContent: 'space-between'}}>
                <Space>
                    <Input
                        placeholder="请输入姓名"
                        value={searchName}
                        onChange={e => setSearchName(e.target.value)}
                        style={{width: 200}}
                    />
                    <Input
                        placeholder="请输入手机号"
                        value={searchMobile}
                        onChange={e => setSearchMobile(e.target.value)}
                        style={{width: 200}}
                    />
                    <Button type="primary" icon={<SearchOutlined/>} onClick={handleSearch}>
                        查询
                    </Button>
                </Space>
                <Button type="primary" icon={<PlusOutlined/>} onClick={handleCreate}>
                    新增接收者
                </Button>
            </div>

            <Card variant="borderless" style={{borderRadius: 12}}>
                <Table
                    columns={columns}
                    dataSource={data}
                    rowKey="id"
                    loading={loading}
                    scroll={{x: 'max-content'}}
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
                width={600}
            >
                <Form form={form} layout="vertical">
                    <Form.Item name="name" label="姓名" rules={[{required: true}]}>
                        <Input/>
                    </Form.Item>
                    <Form.Item name="mobile" label="手机号">
                        <Input/>
                    </Form.Item>
                    <Form.Item name="email" label="邮箱">
                        <Input/>
                    </Form.Item>

                    <Divider>多渠道ID配置</Divider>
                    <div style={{marginBottom: 16, color: '#666', fontSize: '12px'}}>
                        配置不同渠道的接收者标识，如微信OpenID、企微/钉钉/飞书的UserID等
                    </div>
                    <Form.List name="userIds">
                        {(fields, {add, remove}) => (
                            <>
                                {fields.map(({key, name, ...restField}) => (
                                    <Space key={key} style={{display: 'flex', marginBottom: 8}} align="baseline">
                                        <Form.Item
                                            {...restField}
                                            name={[name, 'channelType']}
                                            rules={[{required: true, message: '请选择渠道类型'}]}
                                            style={{width: 150}}
                                        >
                                            <Select placeholder="选择渠道类型">
                                                {CHANNEL_TYPES.map(channel => (
                                                    <Select.Option key={channel.value} value={channel.value}>
                                                        <div style={{display: 'flex', alignItems: 'center'}}>
                                                            <ChannelIcon type={channel.value} size={16}/>
                                                            <span style={{marginLeft: '6px'}}>{channel.label}</span>
                                                        </div>
                                                    </Select.Option>
                                                ))}
                                            </Select>
                                        </Form.Item>
                                        <Form.Item
                                            {...restField}
                                            name={[name, 'userId']}
                                            rules={[{required: true, message: '请输入用户ID'}]}
                                            style={{flex: 1}}
                                        >
                                            <Input placeholder="输入用户ID"/>
                                        </Form.Item>
                                        <MinusCircleOutlined onClick={() => remove(name)}/>
                                    </Space>
                                ))}
                                <Form.Item>
                                    <Button type="dashed" onClick={() => add()} block>
                                        + 添加渠道用户ID
                                    </Button>
                                </Form.Item>
                            </>
                        )}
                    </Form.List>
                </Form>
            </Modal>
        </div>
    );
};

export default RecipientList;
