import React, { useEffect, useState } from 'react';
import { Typography, Card, Row, Col } from 'antd';
import { 
    AppstoreOutlined, 
    MessageOutlined, 
    UserOutlined, 
    CheckCircleOutlined 
} from '@ant-design/icons';
import ReactECharts from 'echarts-for-react';
import type { EChartsOption } from 'echarts';
import { getDashboardStats, type DashboardStats } from '../api/dashboard';
import ChannelIcon from "./channel/components/ChannelIcon"

const { Title, Paragraph } = Typography;

const StatCard = ({ title, value, icon, color, suffix }: any) => (
    <Card 
        variant="borderless"
        hoverable
        style={{ height: '100%', borderRadius: 12 }}
        styles={{ body: { padding: '20px 24px' } }}
    >
        <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
            <div>
                <div style={{ color: '#8c8c8c', fontSize: 14, marginBottom: 4 }}>{title}</div>
                <div style={{ fontSize: 24, fontWeight: 600, color: '#1f1f1f' }}>
                    {value}
                    {suffix && <span style={{ fontSize: 16, marginLeft: 4 }}>{suffix}</span>}
                </div>
            </div>
            <div style={{
                width: 48,
                height: 48,
                borderRadius: 12,
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                background: `${color}15`,
                color: color,
                fontSize: 24
            }}>
                {icon}
            </div>
        </div>
    </Card>
);

const Dashboard: React.FC = () => {
    const userStr = localStorage.getItem('user');
    const user = userStr ? JSON.parse(userStr) : {};
    const [stats, setStats] = useState<DashboardStats>({
        appCount: 0,
        msgCount: 0,
        userCount: 0,
        successRate: 0,
        trend: [],
        channelDist: [],
        statusDist: []
    });

    useEffect(() => {
        loadStats();
    }, []);

    const loadStats = async () => {
        try {
            const data = await getDashboardStats();
            setStats(data);
        } catch (error) {
            console.error(error);
        }
    };

    // 状态颜色映射
    const getStatusColor = (status: string) => {
        if (status.includes('成功') || status.includes('success')) return '#10B981'; // 绿色
        if (status.includes('失败') || status.includes('fail')) return '#EF4444'; // 红色
        if (status.includes('发送中') || status.includes('pending')) return '#3B82F6'; // 蓝色
        return '#8B5CF6'; // 紫色作为默认
    };

    // 现代配色方案
    const modernPalette = [
        '#6366f1', '#8b5cf6', '#ec4899', '#f43f5e', '#10b981', '#3b82f6', '#f59e0b', '#06b6d4'
    ];

    const lineOption: EChartsOption = {
        color: ['#6366f1'],
        tooltip: {
            trigger: 'axis',
            backgroundColor: 'rgba(255, 255, 255, 0.9)',
            borderColor: '#e5e7eb',
            borderWidth: 1,
            textStyle: { color: '#374151' },
            padding: [10, 14],
            extraCssText: 'box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1), 0 2px 4px -1px rgba(0, 0, 0, 0.06); borderRadius: 8px;'
        },
        grid: { left: '1%', right: '1%', top: '15%', bottom: '0%', containLabel: true },
        xAxis: {
            type: 'category',
            boundaryGap: false,
            axisLine: { show: false },
            axisTick: { show: false },
            axisLabel: { color: '#9ca3af', margin: 12, fontSize: 11 },
            data: (stats.trend || []).map((t: any) => t.name)
        },
        yAxis: {
            type: 'value',
            axisLine: { show: false },
            axisTick: { show: false },
            splitLine: { lineStyle: { type: 'dashed', color: '#f3f4f6' } },
            axisLabel: { color: '#9ca3af', fontSize: 11 }
        },
        series: [
            {
                name: '发送量',
                type: 'line',
                smooth: true,
                showSymbol: false,
                symbolSize: 6,
                itemStyle: {
                    color: '#6366f1',
                    borderWidth: 2,
                    borderColor: '#fff'
                },
                lineStyle: { width: 3, shadowColor: 'rgba(99, 102, 241, 0.3)', shadowBlur: 10 },
                areaStyle: {
                    color: {
                        type: 'linear',
                        x: 0, y: 0, x2: 0, y2: 1,
                        colorStops: [
                            { offset: 0, color: 'rgba(99, 102, 241, 0.2)' },
                            { offset: 1, color: 'rgba(99, 102, 241, 0)' }
                        ]
                    }
                },
                data: (stats.trend || []).map((t: any) => t.value)
            }
        ]
    };

    const channelPieOption: EChartsOption = {
        color: modernPalette,
        tooltip: {
            trigger: 'item',
            backgroundColor: 'rgba(255, 255, 255, 0.9)',
            borderColor: '#e5e7eb',
            textStyle: { color: '#374151' },
            padding: [8, 12],
            extraCssText: 'box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1); borderRadius: 8px;'
        },
        legend: {
            orient: 'vertical',
            right: 0,
            top: 'middle',
            itemGap: 12,
            itemWidth: 8,
            itemHeight: 8,
            textStyle: { color: '#4b5563', fontSize: 12 },
            icon: 'circle'
        },
        series: [
            {
                name: '渠道分布',
                type: 'pie',
                radius: ['50%', '70%'],
                center: ['35%', '50%'],
                avoidLabelOverlap: false,
                itemStyle: {
                    borderRadius: 8,
                    borderColor: '#fff',
                    borderWidth: 3
                },
                label: {
                    show: false,
                    position: 'center'
                },
                emphasis: {
                    label: {
                        show: true,
                        fontSize: 16,
                        fontWeight: 'bold',
                        color: '#374151',
                        formatter: '{b}\n{d}%'
                    },
                    scale: true,
                    scaleSize: 8
                },
                labelLine: { show: false },
                data: (stats.channelDist || []).map((d: any) => ({ name: d.name, value: d.value }))
            }
        ]
    };

    const statusPieOption: EChartsOption = {
        tooltip: {
            trigger: 'item',
            backgroundColor: 'rgba(255, 255, 255, 0.9)',
            borderColor: '#e5e7eb',
            textStyle: { color: '#374151' },
            padding: [8, 12],
            extraCssText: 'box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1); borderRadius: 8px;'
        },
        legend: {
            orient: 'vertical',
            right: 0,
            top: 'middle',
            itemGap: 12,
            itemWidth: 8,
            itemHeight: 8,
            textStyle: { color: '#4b5563', fontSize: 12 },
            icon: 'circle'
        },
        series: [
            {
                name: '发送状态',
                type: 'pie',
                radius: ['50%', '70%'],
                center: ['35%', '50%'],
                avoidLabelOverlap: false,
                itemStyle: {
                    borderRadius: 8,
                    borderColor: '#fff',
                    borderWidth: 3
                },
                label: {
                    show: false,
                    position: 'center'
                },
                emphasis: {
                    label: {
                        show: true,
                        fontSize: 16,
                        fontWeight: 'bold',
                        color: '#374151',
                        formatter: '{b}\n{c}'
                    },
                    scale: true,
                    scaleSize: 8
                },
                data: (stats.statusDist || []).map((d: any) => ({
                    name: d.name,
                    value: d.value,
                    itemStyle: { color: getStatusColor(d.name) }
                }))
            }
        ]
    };

    const getGreeting = () => {
        const hour = new Date().getHours();
        if (hour < 6) return '凌晨好';
        if (hour < 9) return '早上好';
        if (hour < 12) return '上午好';
        if (hour < 14) return '中午好';
        if (hour < 17) return '下午好';
        if (hour < 19) return '傍晚好';
        return '晚上好';
    };

    return (
        <div>
            <Row gutter={24} style={{ marginBottom: 24 }} align="middle">
                <Col xs={24} md={14} lg={16}>
                    <Title level={3} style={{ marginBottom: 8 }}>{getGreeting()}, {user.nickname || user.username}</Title>
                    <Paragraph type="secondary" style={{ fontSize: 16, marginBottom: 0 }}>
                        这里是 UniMessage 统一消息中心管理后台，祝您开心每一天！
                    </Paragraph>
                </Col>
                <Col xs={24} md={10} lg={8}>
                    <Card 
                        variant="borderless" 
                        style={{ borderRadius: 12, background: 'linear-gradient(135deg, #1677ff0a 0%, #1677ff00 100%)' }} 
                        styles={{ body: { padding: '16px 20px' } }}
                    >
                        <div style={{ fontWeight: 600, marginBottom: 8, color: '#1f1f1f' }}>系统说明</div>
                        <div style={{ color: '#595959', fontSize: 13, lineHeight: 1.6 }}>
                            UniMessage 是企业级统一消息推送平台，支持多渠道接入、模板管理与消息统计，助您高效触达用户。
                        </div>
                    </Card>
                </Col>
            </Row>

            <Row gutter={[16, 16]}>
                <Col xs={24} sm={12} md={6}>
                    <StatCard 
                        title="应用总数" 
                        value={stats.appCount} 
                        icon={<AppstoreOutlined />}
                        color="#1677ff"
                    />
                </Col>
                <Col xs={24} sm={12} md={6}>
                    <StatCard 
                        title="消息总发送量" 
                        value={stats.msgCount} 
                        icon={<MessageOutlined />}
                        color="#52c41a"
                    />
                </Col>
                <Col xs={24} sm={12} md={6}>
                    <StatCard 
                        title="用户总数" 
                        value={stats.userCount} 
                        icon={<UserOutlined />}
                        color="#722ed1"
                    />
                </Col>
                <Col xs={24} sm={12} md={6}>
                    <StatCard 
                        title="整体成功率" 
                        value={stats.successRate} 
                        suffix="%"
                        icon={<CheckCircleOutlined />}
                        color="#faad14"
                    />
                </Col>
            </Row>

            <Row gutter={[16, 16]} style={{ marginTop: 16 }}>
                <Col xs={24} xl={10}>
                    <Card title="近7天发送趋势" variant="borderless" style={{ borderRadius: 12 }}>
                        <ReactECharts option={lineOption} style={{ height: 250 }} />
                    </Card>
                </Col>
                <Col xs={24} md={12} xl={7}>
                    <Card title="渠道发送分布" variant="borderless" style={{ borderRadius: 12 }}>
                        <div style={{ display: 'flex', height: 250 }}>
                            <div style={{ flex: 1 }}>
                                <ReactECharts 
                                    option={{
                                        ...channelPieOption,
                                        legend: { show: false } // 隐藏默认图例
                                    }} 
                                    style={{ height: '100%' }} 
                                />
                            </div>
                            <div style={{ width: 120, paddingLeft: 16, display: 'flex', flexDirection: 'column', justifyContent: 'center' }}>
                                {(stats.channelDist || []).map((item: any, index: number) => (
                                    <div key={item.name} style={{ display: 'flex', alignItems: 'center', marginBottom: 12 }}>
                                        <div 
                                            style={{ 
                                                width: 8, 
                                                height: 8, 
                                                borderRadius: '50%', 
                                                backgroundColor: modernPalette[index % modernPalette.length],
                                                marginRight: 8 
                                            }} 
                                        />
                                        <ChannelIcon type={item.name} size={14} style={{ marginRight: 6 }} />
                                        <span style={{ fontSize: 12, color: '#4b5563' }}>{item.name}</span>
                                    </div>
                                ))}
                            </div>
                        </div>
                    </Card>
                </Col>
                <Col xs={24} md={12} xl={7}>
                    <Card title="发送状态分布" variant="borderless" style={{ borderRadius: 12 }}>
                        <ReactECharts option={statusPieOption} style={{ height: 250 }} />
                    </Card>
                </Col>
            </Row>
        </div>
    );
};

export default Dashboard;
