import React, { useState, useEffect, useMemo } from 'react';
import { Layout, Menu, Button, theme, Dropdown, Breadcrumb } from 'antd';
import Avatar, { genConfig } from 'react-nice-avatar';
import {
  MenuFoldOutlined,
  MenuUnfoldOutlined,
  AppstoreOutlined,
  MessageOutlined,
  SettingOutlined,
  LogoutOutlined,
  UserOutlined,
  HistoryOutlined,
  DownOutlined,
  TeamOutlined,
  IdcardOutlined,
  UsergroupAddOutlined,
  UnorderedListOutlined,
  FileTextOutlined,
  ApiOutlined,
  DesktopOutlined,
  LinkOutlined,
} from '@ant-design/icons';
import { Outlet, useNavigate, useLocation } from 'react-router-dom';
import { logout, getUserInfo } from '../api/auth';
import { getSystemConfig } from '../api/systemConfig';
import type { User } from '../api/types';
import defaultLogo from '../assets/logo.svg';

const { Header, Sider, Content } = Layout;

const MainLayout: React.FC = () => {
  const [collapsed, setCollapsed] = useState(false);
  const [user, setUser] = useState<User | null>(null);
  const [sysConfig, setSysConfig] = useState<{ systemName: string; logo: string }>({
    systemName: 'UniMessage',
    logo: defaultLogo,
  });
  const {
    token: { colorBgContainer, borderRadiusLG },
  } = theme.useToken();
  const navigate = useNavigate();
  const location = useLocation();

  useEffect(() => {
    fetchUserInfo();
    fetchConfig();
  }, []);

  const fetchConfig = async () => {
    try {
      const config = await getSystemConfig();
      if (config) {
        setSysConfig({
          systemName: config.systemName || 'UniMessage',
          logo: config.logo || defaultLogo,
        });
        // If icon is present, we might want to update favicon or use it elsewhere. 
        // For now, let's just focus on Sider display.
        if (config.icon) {
            // Optional: Update favicon dynamically
            const link = document.querySelector("link[rel*='icon']") as HTMLLinkElement || document.createElement('link');
            link.type = 'image/x-icon';
            link.rel = 'shortcut icon';
            link.href = config.icon;
            document.getElementsByTagName('head')[0].appendChild(link);
        }
        if (config.systemName) {
            document.title = config.systemName;
        }
      }
    } catch (error) {
      console.error(error);
    }
  };

  const fetchUserInfo = async () => {
    try {
      const userData = await getUserInfo();
      setUser(userData);
      localStorage.setItem('user', JSON.stringify(userData));
    } catch (error) {
      console.error(error);
    }
  };

  const handleLogout = async () => {
      try {
          await logout();
      } catch(e) {
          // ignore
      }
      localStorage.removeItem('uni-message-token');
      localStorage.removeItem('user');
      navigate('/login');
  }

  const userMenu = {
    items: [
      {
        key: 'logout',
        icon: <LogoutOutlined />,
        label: '退出登录',
        onClick: handleLogout,
      },
    ],
  };

  const menuItems = [
    {
      key: '/',
      icon: <DesktopOutlined />,
      label: '工作台',
    },
    {
      key: '/apps',
      icon: <AppstoreOutlined />,
      label: '应用接入',
    },
    {
      key: '/channels',
      icon: <ApiOutlined />,
      label: '推送渠道',
    },
    {
      key: '/templates',
      icon: <MessageOutlined />,
      label: '消息模板',
    },
    {
      key: 'recipients',
      icon: <TeamOutlined />,
      label: '通讯录',
      children: [
        {
          key: '/recipients',
          icon: <IdcardOutlined />,
          label: '联系人',
        },
        {
          key: '/recipient-groups',
          icon: <UsergroupAddOutlined />,
          label: '群组管理',
        }
      ]
    },
    {
      key: 'logs',
      icon: <HistoryOutlined />,
      label: '发送日志',
      children: [
        {
          key: '/logs/batch',
          icon: <UnorderedListOutlined />,
          label: '批次查询',
        },
        {
          key: '/logs/detail',
          icon: <FileTextOutlined />,
          label: '明细查询',
        }
      ]
    },
    {
      key: '/short-urls',
      icon: <LinkOutlined />,
      label: '短链管理',
    },
    {
      key: 'settings',
      icon: <SettingOutlined />,
      label: '系统设置',
      children: [
        {
          key: '/system/config',
          icon: <SettingOutlined />,
          label: '系统配置',
        },
        {
          key: '/users',
          icon: <UserOutlined />,
          label: '系统用户',
        }
      ]
    },
  ];

  const breadcrumbItems = useMemo(() => {
    const items: { title: string }[] = [];
    
    const findPath = (menuList: any[], parents: any[] = []) => {
      for (const item of menuList) {
        if (item.key === location.pathname) {
          parents.forEach(p => items.push({ title: p.label }));
          items.push({ title: item.label });
          return true;
        }
        if (item.children) {
          if (findPath(item.children, [...parents, item])) {
            return true;
          }
        }
      }
      return false;
    };

    findPath(menuItems);
    return items;
  }, [location.pathname]);

  const avatarConfig = useMemo(() => {
    return genConfig(user?.nickname || user?.username || 'Admin');
  }, [user]);

  return (
    <Layout style={{ minHeight: '100vh' }}>
      <Sider 
        trigger={null} 
        collapsible 
        collapsed={collapsed}
        theme="light"
        width={220}
        style={{
          boxShadow: '2px 0 8px 0 rgba(29,35,41,0.05)',
          zIndex: 10,
          borderRight: '1px solid #f0f0f0'
        }}
      >
        <div style={{ 
          height: 64, 
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          background: '#fff',
          borderBottom: '1px solid #f0f0f0'
        }}>
            <div style={{
              fontSize: collapsed ? '18px' : '20px',
              fontWeight: 'bold',
              whiteSpace: 'nowrap',
              overflow: 'hidden',
              transition: 'all 0.3s',
              display: 'flex',
              alignItems: 'center',
              gap: 8,
              cursor: 'pointer'
            }} onClick={() => navigate('/')}>
              <div style={{
                 width: 32,
                 height: 32,
                 display: 'flex',
                 alignItems: 'center',
                 justifyContent: 'center'
              }}>
                <img src={sysConfig.logo} alt="logo" style={{ width: '100%', height: '100%', objectFit: 'contain' }} />
              </div>
              {!collapsed && <span style={{ color: '#1890ff', fontSize: '18px' }}>{sysConfig.systemName}</span>}
            </div>
        </div>
        <Menu
          theme="light"
          mode="inline"
          selectedKeys={[location.pathname]}
          defaultOpenKeys={['logs']}
          onClick={({ key }) => navigate(key)}
          style={{ borderRight: 0, marginTop: 8, padding: '0 8px' }}
          items={menuItems}
        />
      </Sider>
      <Layout>
        <Header style={{ 
          padding: '0 24px 0 0', 
          background: 'rgba(255, 255, 255, 0.8)', 
          backdropFilter: 'blur(8px)',
          display: 'flex', 
          justifyContent: 'space-between', 
          alignItems: 'center',
          borderBottom: '1px solid #f0f0f0',
          position: 'sticky',
          top: 0,
          zIndex: 9,
          height: 64
        }}>
          <div style={{ display: 'flex', alignItems: 'center' }}>
            <Button
              type="text"
              icon={collapsed ? <MenuUnfoldOutlined /> : <MenuFoldOutlined />}
              onClick={() => setCollapsed(!collapsed)}
              style={{
                fontSize: '16px',
                width: 64,
                height: 64,
              }}
            />
            <Breadcrumb items={breadcrumbItems} />
          </div>
          
          <Dropdown menu={userMenu}>
            <div 
              style={{ 
                display: 'flex', 
                alignItems: 'center', 
                padding: '0 16px',
                height: '64px', 
                cursor: 'pointer',
                transition: 'all 0.3s',
              }}
              onMouseEnter={(e) => e.currentTarget.style.backgroundColor = 'rgba(0,0,0,0.025)'}
              onMouseLeave={(e) => e.currentTarget.style.backgroundColor = 'transparent'}
            >
              <Avatar style={{ width: 36, height: 36 }} {...avatarConfig} />
              <span style={{ 
                marginLeft: 12, 
                marginRight: 4,
                fontSize: '14px', 
                fontWeight: 500,
                color: 'rgba(0, 0, 0, 0.88)' 
              }}>
                {user?.nickname || user?.username || 'Admin'}
              </span>
              <DownOutlined style={{ fontSize: '12px', color: 'rgba(0, 0, 0, 0.45)' }} />
            </div>
          </Dropdown>
        </Header>
        <Content
          style={{
            margin: '24px 16px',
            padding: 24,
            minHeight: 280,
            background: colorBgContainer,
            borderRadius: borderRadiusLG,
            boxShadow: '0 1px 2px rgba(0,0,0,0.05)'
          }}
        >
          <Outlet />
        </Content>
      </Layout>
    </Layout>
  );
};

export default MainLayout;
