import React, {useEffect, useState} from 'react';
import {Button, Card, Form, Input, Select, Space, Table, Tag, Tooltip, Modal} from 'antd';
import {ReloadOutlined, SearchOutlined, EyeOutlined} from '@ant-design/icons';
import type {ColumnsType} from 'antd/es/table';
import {getBatchPage, getDetailByBatchId} from '../../api/message';
import ChannelIcon from '../channel/components/ChannelIcon';

const BatchList: React.FC = () => {
    const [form] = Form.useForm();
    const [loading, setLoading] = useState(false);
    const [data, setData] = useState<any[]>([]);
    const [total, setTotal] = useState(0);
    const [current, setCurrent] = useState(1);
    const [pageSize, setPageSize] = useState(10);

    // 批次详情模态框相关状态
    const [detailModalOpen, setDetailModalOpen] = useState(false);
    const [detailLoading, setDetailLoading] = useState(false);
    const [detailData, setDetailData] = useState<any[]>([]);
    const [detailTotal, setDetailTotal] = useState(0);
    const [detailCurrent, setDetailCurrent] = useState(1);
    const [detailPageSize, setDetailPageSize] = useState(10);
    const [selectedBatch, setSelectedBatch] = useState<any>(null);

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

    // 查看批次详情
    const handleViewDetail = async (record: any) => {
        setSelectedBatch(record);
        setDetailModalOpen(true);
        setDetailCurrent(1);
        await loadDetailData(record.id, 1, detailPageSize);
    };

    // 加载批次详情数据
    const loadDetailData = async (batchId: number, page = detailCurrent, size = detailPageSize) => {
        setDetailLoading(true);
        try {
            const res = await getDetailByBatchId(batchId, { current: page, size });
            setDetailData(res.records);
            setDetailTotal(res.total);
            setDetailCurrent(page);
            setDetailPageSize(size);
        } catch (error) {
            console.error(error);
        } finally {
            setDetailLoading(false);
        }
    };

    // 关闭详情模态框
    const handleDetailModalClose = () => {
        setDetailModalOpen(false);
        setSelectedBatch(null);
        setDetailData([]);
        setDetailTotal(0);
        setDetailCurrent(1);
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
            title: '应用',
            dataIndex: 'appName',
            key: 'appName',
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
        {
            title: '操作',
            key: 'action',
            fixed: 'right',
            width: 100,
            render: (_, record) => (
                <Button 
                    type="link" 
                    icon={<EyeOutlined />}
                    onClick={() => handleViewDetail(record)}
                >
                    详情
                </Button>
            ),
        },
    ];

    // 批次详情表格列定义
    const detailColumns: ColumnsType<any> = [
        {
            title: 'ID',
            dataIndex: 'id',
            key: 'id',
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
            render: (text) => text || '-'
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
            render: (text) => text || '-'
        },
    ];

    return (
        <>
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

            {/* 批次详情模态框 */}
            <Modal
                title={`批次详情 - ${selectedBatch?.batchNo || ''}`}
                open={detailModalOpen}
                onCancel={handleDetailModalClose}
                footer={null}
                width={1200}
                destroyOnClose
            >
                <div style={{ marginBottom: 16 }}>
                    <Space>
                        <span>批次ID: {selectedBatch?.id}</span>
                        <span>总数: {selectedBatch?.totalCount}</span>
                        <span>成功: <span style={{color: 'green'}}>{selectedBatch?.successCount}</span></span>
                        <span>失败: <span style={{color: 'red'}}>{selectedBatch?.failCount}</span></span>
                    </Space>
                </div>
                <Table
                    columns={detailColumns}
                    dataSource={detailData}
                    rowKey="id"
                    loading={detailLoading}
                    scroll={{x: 'max-content'}}
                    pagination={{
                        current: detailCurrent,
                        pageSize: detailPageSize,
                        total: detailTotal,
                        onChange: (page, size) => selectedBatch && loadDetailData(selectedBatch.id, page, size),
                        showSizeChanger: true,
                        showQuickJumper: true,
                        showTotal: (total, range) => `第 ${range[0]}-${range[1]} 条，共 ${total} 条`,
                    }}
                />
            </Modal>
        </>
    );
};

export default BatchList;
