import React, {useEffect, useState} from 'react';
import {Button, Card, Form, Input, Select, Space, Table, Tag, Tooltip} from 'antd';
import {ReloadOutlined, SearchOutlined} from '@ant-design/icons';
import type {ColumnsType} from 'antd/es/table';
import {getBatchPage} from '../../api/message';
import ChannelIcon from '../channel/components/ChannelIcon';

const BatchList: React.FC = () => {
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
            const res = await getBatchPage({current: page, size, ...values});
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

    const columns: ColumnsType<any> = [
        {
            title: 'ID',
            dataIndex: 'id',
            key: 'id',
            width: 80,
        },
        {
            title: '批次号',
            dataIndex: 'batchNo',
            key: 'batchNo',
        },
        {
            title: '应用ID',
            dataIndex: 'appId',
            key: 'appId',
            width: 80,
        },
        {
            title: '模板',
            dataIndex: 'templateName',
            key: 'templateName',
            render: (text, record) => (
                <Tooltip title={`ID: ${record.templateId}`}>
                    {text || record.templateId}
                </Tooltip>
            )
        },
        {
            title: '渠道',
            dataIndex: 'channelName',
            key: 'channelName',
            render: (text, record) => (
                <Tooltip title={`ID: ${record.channelId}`}>
                    <Space align="center">
                        <ChannelIcon type={record.channelType} size={16}/>
                        <span>{text || record.channelId}</span>
                    </Space>
                </Tooltip>
            )
        },
        {
            title: '总数',
            dataIndex: 'totalCount',
            key: 'totalCount',
        },
        {
            title: '成功',
            dataIndex: 'successCount',
            key: 'successCount',
            render: (text) => <span style={{color: 'green'}}>{text}</span>
        },
        {
            title: '失败',
            dataIndex: 'failCount',
            key: 'failCount',
            render: (text) => <span style={{color: 'red'}}>{text}</span>
        },
        {
            title: '状态',
            dataIndex: 'status',
            key: 'status',
            render: (status) => {
                // 0处理中 10全部成功 20部分成功 30全部失败
                const map: Record<number, any> = {
                    0: {color: 'blue', text: '处理中'},
                    10: {color: 'green', text: '全部成功'},
                    20: {color: 'orange', text: '部分成功'},
                    30: {color: 'red', text: '全部失败'},
                };
                const config = map[status] || {color: 'default', text: '未知'};
                return <Tag color={config.color}>{config.text}</Tag>;
            }
        },
        {
            title: '创建时间',
            dataIndex: 'createdAt',
            key: 'createdAt',
        },
    ];

    return (
        <Card variant="borderless" style={{borderRadius: 12}}>
            <Form form={form} layout="inline" style={{marginBottom: 24}}>
                <Form.Item name="batchNo" label="批次号">
                    <Input placeholder="请输入批次号" allowClear onPressEnter={handleSearch}/>
                </Form.Item>
                <Form.Item name="status" label="状态">
                    <Select placeholder="请选择状态" allowClear style={{width: 120}}>
                        <Select.Option value={0}>处理中</Select.Option>
                        <Select.Option value={10}>全部成功</Select.Option>
                        <Select.Option value={20}>部分成功</Select.Option>
                        <Select.Option value={30}>全部失败</Select.Option>
                    </Select>
                </Form.Item>
                <Form.Item>
                    <Space>
                        <Button type="primary" icon={<SearchOutlined/>} onClick={handleSearch}>查询</Button>
                        <Button icon={<ReloadOutlined/>} onClick={handleReset}>重置</Button>
                    </Space>
                </Form.Item>
            </Form>
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
    );
};

export default BatchList;
