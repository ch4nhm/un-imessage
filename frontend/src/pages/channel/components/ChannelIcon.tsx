import React from 'react';

// 导入所有渠道图标
import smsIcon from '../../../assets/channel-icons/SMS.png';
import emailIcon from '../../../assets/channel-icons/EMAIL.png';
import wechatOfficialIcon from '../../../assets/channel-icons/WECHAT_OFFICIAL.png';
import wechatWorkIcon from '../../../assets/channel-icons/WECHAT_WORK.png';
import dingtalkIcon from '../../../assets/channel-icons/DINGTALK.png';
import feishuIcon from '../../../assets/channel-icons/FEISHU.png';
import telegramIcon from '../../../assets/channel-icons/TELEGRAM.svg';
import slackIcon from '../../../assets/channel-icons/SLACK.svg';
import tencentSmsIcon from '../../../assets/channel-icons/TENCENT_SMS.png';
import twilioIcon from '../../../assets/channel-icons/TWILIO.svg';
import webhookIcon from '../../../assets/channel-icons/WEBHOOK.png';

interface ChannelIconProps {
  type: string;
  size?: number;
  style?: React.CSSProperties;
}

const CHANNEL_ICONS: Record<string, string> = {
  SMS: smsIcon,
  EMAIL: emailIcon,
  WECHAT_OFFICIAL: wechatOfficialIcon,
  WECHAT_WORK: wechatWorkIcon,
  DINGTALK: dingtalkIcon,
  FEISHU: feishuIcon,
  TELEGRAM: telegramIcon,
  SLACK: slackIcon,
  TENCENT_SMS: tencentSmsIcon,
  TWILIO: twilioIcon,
  WEBHOOK: webhookIcon,
};

const ChannelIcon: React.FC<ChannelIconProps> = ({ type, size = 20, style }) => {
  const iconSrc = CHANNEL_ICONS[type];
  
  if (!iconSrc) {
    return null;
  }

  return (
    <img 
      src={iconSrc} 
      alt={type}
      style={{
        width: size,
        height: size,
        objectFit: 'contain',
        ...style
      }}
    />
  );
};

export default ChannelIcon;