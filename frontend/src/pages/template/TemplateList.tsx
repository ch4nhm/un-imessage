import React, {useEffect, useState} from 'react';
import {Button, Card, Drawer, Form, Input, InputNumber, message, Popconfirm, Select, Space, Table, Tag} from 'antd';
import {DeleteOutlined, EditOutlined, PoweroffOutlined} from '@ant-design/icons';
import type {ColumnsType} from 'antd/es/table';
import type {Template} from '../../api/template';
import {
    createTemplate,
    deleteTemplate,
    getTemplatePage,
    updateTemplate,
    updateTemplateStatus
} from '../../api/template';
import type {Channel} from '../../api/channel';
import {getChannelList} from '../../api/channel';
import type {RecipientGroup} from '../../api/recipientGroup';
import {getRecipientGroupList} from '../../api/recipientGroup';

import type {Recipient} from '../../api/recipient';
import {getRecipientList} from '../../api/recipient';
import type {App} from '../../api/app';
import {getAppList} from '../../api/app';
import ChannelIcon from '../channel/components/ChannelIcon';

const TemplateList: React.FC = () => {
    const [loading, setLoading] = useState(false);
    const [data, setData] = useState<Template[]>([]);
    const [total, setTotal] = useState(0);
    const [current, setCurrent] = useState(1);
    const [pageSize, setPageSize] = useState(10);
    const [channels, setChannels] = useState<Channel[]>([]);
    const [groups, setGroups] = useState<RecipientGroup[]>([]);
    const [recipients, setRecipients] = useState<Recipient[]>([]);
    const [apps, setApps] = useState<App[]>([]);

    const [isModalOpen, setIsModalOpen] = useState(false);
    const [editingId, setEditingId] = useState<number | null>(null);
    const [form] = Form.useForm();

    const loadData = async (page = current, size = pageSize) => {
        setLoading(true);
        try {
            const res = await getTemplatePage({current: page, size});
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

    const loadChannels = async () => {
        try {
            const res = await getChannelList();
            setChannels(res as any);
        } catch (error) {
            console.error(error);
        }
    }

    const loadGroups = async () => {
        try {
            const res = await getRecipientGroupList();
            setGroups(res as any);
        } catch (error) {
            console.error(error);
        }
    }

    const loadRecipients = async () => {
        try {
            const res = await getRecipientList();
            setRecipients(res as any);
        } catch (error) {
            console.error(error);
        }
    }

    const loadApps = async () => {
        try {
            const res = await getAppList();
            setApps(res as any);
        } catch (error) {
            console.error(error);
        }
    }

    useEffect(() => {
        loadData();
        loadChannels();
        loadGroups();
        loadRecipients();
        loadApps();
    }, []);

    const handleCreate = () => {
        setEditingId(null);
        form.resetFields();
        setIsModalOpen(true);
    };

    const handleEdit = (record: Template) => {
        setEditingId(record.id);
        // Convert comma separated string to array for Select
        const formValues = {
            ...record,
            recipientGroupIds: record.recipientGroupIds ? record.recipientGroupIds.split(',').map(Number) : [],
            recipientIds: record.recipientIds ? record.recipientIds.split(',').map(Number) : []
        };
        form.setFieldsValue(formValues);
        setIsModalOpen(true);
    };

    const handleDelete = async (id: number) => {
        try {
            await deleteTemplate(id);
            message.success('删除成功');
            loadData();
        } catch (error) {
            console.error(error);
        }
    };

    const handleStatusChange = async (record: Template) => {
        try {
            const newStatus = record.status === 1 ? 0 : 1;
            await updateTemplateStatus(record.id, newStatus);
            message.success('状态更新成功');
            loadData();
        } catch (error) {
            console.error(error);
        }
    };

    const handleOk = async () => {
        try {
            const values = await form.validateFields();
            if (editingId) {
                await updateTemplate(editingId, values);
                message.success('更新成功');
            } else {
                await createTemplate(values);
                message.success('创建成功');
            }
            setIsModalOpen(false);
            loadData();
        } catch (error) {
            console.error(error);
        }
    };

    const columns: ColumnsType<Template> = [
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
            title: '模板编码',
            dataIndex: 'code',
            key: 'code',
        },
        {
            title: '所属应用',
            dataIndex: 'appId',
            key: 'appId',
            render: (appId) => {
                if (!appId) return <Tag>公共模板</Tag>;
                const app = apps.find(a => a.id === appId);
                return app ? app.appName : appId;
            }
        },
        {
            title: '渠道',
            dataIndex: 'channelId',
            key: 'channelId',
            render: (channelId) => {
                const channel = channels.find(c => c.id === channelId);
                return channel ? (
                    <Space align="center">
                        <ChannelIcon type={channel.type} size={16}/>
                        <span>{channel.name}</span>
                    </Space>
                ) : channelId;
            }
        },
        {
            title: '限流(TPS)',
            dataIndex: 'rateLimit',
            key: 'rateLimit',
            render: (val) => val ? val : '-',
            width: 100,
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
            width: 200,
            render: (_, record) => (
                <Space size="small">
                    <Button type="link" icon={<EditOutlined/>} onClick={() => handleEdit(record)}>编辑</Button>
                    <Button type="link" icon={<PoweroffOutlined/>} onClick={() => handleStatusChange(record)}>
                        {record.status === 1 ? '禁用' : '启用'}
                    </Button>
                    <Popconfirm title="确定删除吗?" onConfirm={() => handleDelete(record.id)}>
                        <Button type="link" danger icon={<DeleteOutlined/>}>删除</Button>
                    </Popconfirm>
                </Space>
            ),
        },
    ];

    return (
        <Card variant="borderless" style={{borderRadius: 12}}>
            <div style={{marginBottom: 16}}>
                <Button type="primary" onClick={handleCreate}>
                    新增模板
                </Button>
            </div>
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
            <Drawer
                title={editingId ? '编辑模板' : '新增模板'}
                open={isModalOpen}
                onClose={() => setIsModalOpen(false)}
                width={700}
                extra={
                    <Space>
                        <Button onClick={() => setIsModalOpen(false)}>取消</Button>
                        <Button type="primary" onClick={handleOk}>
                            {editingId ? '更新' : '创建'}
                        </Button>
                    </Space>
                }
            >
                <Form form={form} layout="vertical">
                    <Form.Item name="name" label="模板名称" rules={[{required: true}]}>
                        <Input/>
                    </Form.Item>
                    <Form.Item name="code" label="模板代码" rules={[{required: true}]}>
                        <Input placeholder="唯一标识，如 LOGIN_CODE"/>
                    </Form.Item>
                    <Form.Item name="appId" label="所属应用" tooltip="留空表示公共模板，所有应用可用">
                        <Select
                            allowClear
                            placeholder="留空表示公共模板"
                            options={apps.map(a => ({label: a.appName, value: a.id}))}
                        />
                    </Form.Item>
                    <Form.Item name="channelId" label="发送渠道" rules={[{required: true}]}>
                        <Select>
                            {channels.map(c => (
                                <Select.Option key={c.id} value={c.id}>
                                    <Space align="center">
                                        <ChannelIcon type={c.type} size={16}/>
                                        <span>{c.name}</span>
                                    </Space>
                                </Select.Option>
                            ))}
                        </Select>
                    </Form.Item>

                    <Form.Item name="thirdPartyId" label="第三方模板ID (可选)">
                        <Input placeholder="如阿里云短信模板ID、微信模板ID"/>
                    </Form.Item>

                    <Form.Item name="recipientGroupIds" label="默认接收组 (可选)">
                        <Select
                            mode="multiple"
                            allowClear
                            options={groups.map(g => ({label: g.name, value: g.id}))}
                            placeholder="选择关联的接收者分组"
                        />
                    </Form.Item>

                    <Form.Item name="recipientIds" label="默认接收者 (可选)">
                        <Select
                            mode="multiple"
                            allowClear
                            options={recipients.map(r => ({
                                label: `${r.name} (${r.mobile || r.email || '无联系方式'})`,
                                value: r.id
                            }))}
                            placeholder="选择关联的独立接收者"
                            filterOption={(input, option) =>
                                (option?.label ?? '').toString().toLowerCase().includes(input.toLowerCase())
                            }
                        />
                    </Form.Item>

                    <Form.Item name="msgType" label="消息类型" initialValue={1}>
                        <Select options={[
                            {label: '通知', value: 1},
                            {label: '验证码', value: 2},
                            {label: '营销', value: 3},
                        ]}/>
                    </Form.Item>
                    <Form.Item name="title" label="消息标题" rules={[{required: true}]}>
                        <Input/>
                    </Form.Item>
                    <Form.Item name="content" label="消息内容" rules={[{required: true}]}>
                        <Input.TextArea rows={6} placeholder="可以使用 ${code} 作为变量占位符"/>
                    </Form.Item>

                    <Form.Item name="variables" label="变量列表 (可选)">
                        <Input placeholder='JSON数组格式，如 ["code", "name"]'/>
                    </Form.Item>

                    <Form.Item name="deduplicationConfig" label="去重配置 (可选)">
                        <Input.TextArea rows={2} placeholder='JSON格式配置，如 {"interval": 60, "count": 1}'/>
                    </Form.Item>

                    <Form.Item name="rateLimit" label="频率限制 (TPS)" tooltip="每秒最大请求数，0或空表示不限制">
                        <InputNumber style={{width: '100%'}} min={0} placeholder="例如: 10"/>
                    </Form.Item>
                </Form>
            </Drawer>
        </Card>
    );
};

export default TemplateList;
