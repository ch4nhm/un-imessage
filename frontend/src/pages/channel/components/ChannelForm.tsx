import React from 'react';
import {Form, Input, InputNumber, Switch, Space, Typography} from 'antd';
import ChannelIcon from '../components/ChannelIcon';

const { Text } = Typography;

interface ChannelFormProps {
    type: string;
}

const ChannelForm: React.FC<ChannelFormProps> = ({type}) => {
    const getChannelTitle = (channelType: string) => {
        const typeMap: Record<string, string> = {
            'SMS': '阿里云短信',
            'EMAIL': '邮件',
            'WECHAT_OFFICIAL': '微信服务号',
            'WECHAT_WORK': '企业微信',
            'DINGTALK': '钉钉',
            'FEISHU': '飞书',
            'TELEGRAM': 'Telegram',
            'SLACK': 'Slack',
            'TENCENT_SMS': '腾讯云短信',
            'TWILIO': 'Twilio',
            'WEBHOOK': 'Webhook'
        };
        return typeMap[channelType] || channelType;
    };

    const renderFields = () => {
        switch (type) {
            case 'SMS':
                return (
                    <>
                        <Form.Item name="accessKeyId" label="AccessKeyId" rules={[{required: true}]}>
                            <Input/>
                        </Form.Item>
                        <Form.Item name="accessKeySecret" label="AccessKeySecret" rules={[{required: true}]}>
                            <Input.Password/>
                        </Form.Item>
                        <Form.Item name="signName" label="签名名称" rules={[{required: true}]}>
                            <Input/>
                        </Form.Item>
                    </>
                );
            case 'EMAIL':
                return (
                    <>
                        <Form.Item name="host" label="SMTP主机" rules={[{required: true}]}>
                            <Input placeholder="smtp.example.com"/>
                        </Form.Item>
                        <Form.Item name="port" label="端口" rules={[{required: true}]}>
                            <InputNumber style={{width: '100%'}}/>
                        </Form.Item>
                        <Form.Item name="username" label="用户名" rules={[{required: true}]}>
                            <Input/>
                        </Form.Item>
                        <Form.Item name="password" label="密码/授权码" rules={[{required: true}]}>
                            <Input.Password/>
                        </Form.Item>
                        <Form.Item name="ssl" label="启用SSL" valuePropName="checked">
                            <Switch/>
                        </Form.Item>
                    </>
                );
            case 'WECHAT_OFFICIAL':
                return (
                    <>
                        <Form.Item name="appId" label="AppID" rules={[{required: true}]}>
                            <Input/>
                        </Form.Item>
                        <Form.Item name="secret" label="AppSecret" rules={[{required: true}]}>
                            <Input.Password/>
                        </Form.Item>
                        <Form.Item name="redirectUrl" label="跳转链接" tooltip="点击模板消息后跳转的URL">
                            <Input placeholder="https://example.com/detail"/>
                        </Form.Item>
                    </>
                );
            case 'WECHAT_WORK':
                return (
                    <>
                        <Form.Item name="corpId" label="企业ID (CorpId)" rules={[{required: true}]}>
                            <Input/>
                        </Form.Item>
                        <Form.Item name="corpSecret" label="应用密钥 (Secret)" rules={[{required: true}]}>
                            <Input.Password/>
                        </Form.Item>
                        <Form.Item name="agentId" label="应用AgentId" rules={[{required: true}]}>
                            <InputNumber style={{width: '100%'}}/>
                        </Form.Item>
                    </>
                );
            case 'DINGTALK':
                return (
                    <>
                        <Form.Item name="webhook" label="Webhook地址" rules={[{required: true}]}>
                            <Input/>
                        </Form.Item>
                        <Form.Item name="secret" label="加签密钥 (可选)">
                            <Input.Password/>
                        </Form.Item>
                    </>
                );
            case 'FEISHU':
                return (
                    <>
                        <Form.Item name="webhook" label="Webhook地址">
                            <Input placeholder="群机器人Webhook"/>
                        </Form.Item>
                        <Form.Item label="或者 (应用消息模式)" style={{marginBottom: 0}}>
                            <Form.Item name="appId" label="AppID">
                                <Input/>
                            </Form.Item>
                            <Form.Item name="appSecret" label="AppSecret">
                                <Input.Password/>
                            </Form.Item>
                        </Form.Item>
                    </>
                );
            case 'TELEGRAM':
                return (
                    <>
                        <Form.Item name="botToken" label="Bot Token" rules={[{required: true}]}>
                            <Input.Password placeholder="123456:ABC-DEF1234ghIkl-zyx57W2v1u123ew11"/>
                        </Form.Item>
                    </>
                );
            case 'SLACK':
                return (
                    <>
                        <Form.Item name="webhookUrl" label="Webhook URL" rules={[{required: true}]}>
                            <Input placeholder="https://hooks.slack.com/services/..."/>
                        </Form.Item>
                    </>
                );
            case 'TENCENT_SMS':
                return (
                    <>
                        <Form.Item name="secretId" label="SecretId" rules={[{required: true}]}>
                            <Input/>
                        </Form.Item>
                        <Form.Item name="secretKey" label="SecretKey" rules={[{required: true}]}>
                            <Input.Password/>
                        </Form.Item>
                        <Form.Item name="sdkAppId" label="SdkAppId" rules={[{required: true}]}>
                            <Input/>
                        </Form.Item>
                        <Form.Item name="signName" label="签名名称" rules={[{required: true}]}>
                            <Input/>
                        </Form.Item>
                        <Form.Item name="region" label="区域 (Region)">
                            <Input placeholder="ap-guangzhou (默认)"/>
                        </Form.Item>
                    </>
                );
            case 'TWILIO':
                return (
                    <>
                        <Form.Item name="accountSid" label="Account SID" rules={[{required: true}]}>
                            <Input/>
                        </Form.Item>
                        <Form.Item name="authToken" label="Auth Token" rules={[{required: true}]}>
                            <Input.Password/>
                        </Form.Item>
                        <Form.Item name="fromPhone" label="发送号码 (From)" rules={[{required: true}]}>
                            <Input placeholder="+15017122661"/>
                        </Form.Item>
                    </>
                );
            case 'WEBHOOK':
                return (
                    <>
                        <Form.Item name="url" label="URL" rules={[{required: true}]}>
                            <Input placeholder="https://your-api.com/callback"/>
                        </Form.Item>
                        <Form.Item name="method" label="Method" initialValue="POST">
                            <Input placeholder="POST"/>
                        </Form.Item>
                        {/* Headers暂不提供复杂UI配置，可让用户直接输入JSON或简单Key-Value */}
                    </>
                );
            default:
                return <div style={{color: 'gray'}}>请选择渠道类型</div>;
        }
    };

    return (
        <>
            {type && (
                <div style={{ marginBottom: 16, padding: '12px 16px', background: '#f5f5f5', borderRadius: 8 }}>
                    <Space align="center">
                        <ChannelIcon type={type} size={20} />
                        <Text strong style={{ fontSize: 16 }}>{getChannelTitle(type)} 配置</Text>
                    </Space>
                </div>
            )}
            {renderFields()}
        </>
    );
};

export default ChannelForm;
