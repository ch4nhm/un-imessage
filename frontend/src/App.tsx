import { HashRouter, Routes, Route, Navigate } from 'react-router-dom';
import Login from './pages/Login';
import MainLayout from './layout/MainLayout';
import Dashboard from './pages/Dashboard';
import UserList from './pages/user/UserList';
import AppList from './pages/app/AppList';
import ChannelList from './pages/channel/ChannelList';
import TemplateList from './pages/template/TemplateList';
import BatchList from './pages/message/BatchList';
import DetailList from './pages/message/DetailList';
import RecipientList from './pages/recipient/RecipientList';
import RecipientGroupList from './pages/recipient/RecipientGroupList';
import SystemConfigPage from './pages/system/SystemConfig';
import ShortUrlList from './pages/shorturl/ShortUrlList';

// Protected Route Wrapper
const ProtectedRoute = ({ children }: { children: React.ReactElement }) => {
  const token = localStorage.getItem('uni-message-token');
  if (!token) {
    return <Navigate to="/login" replace />;
  }
  return children;
};

function App() {
  return (
    <HashRouter>
      <Routes>
        <Route path="/login" element={<Login />} />
        
        <Route path="/" element={
            <ProtectedRoute>
                <MainLayout />
            </ProtectedRoute>
        }>
            <Route index element={<Dashboard />} />
            <Route path="users" element={<UserList />} />
            <Route path="apps" element={<AppList />} />
            <Route path="channels" element={<ChannelList />} />
            <Route path="templates" element={<TemplateList />} />
            <Route path="recipients" element={<RecipientList />} />
            <Route path="recipient-groups" element={<RecipientGroupList />} />
            <Route path="logs/batch" element={<BatchList />} />
            <Route path="logs/detail" element={<DetailList />} />
            <Route path="short-urls" element={<ShortUrlList />} />
            <Route path="system/config" element={<SystemConfigPage />} />
        </Route>
      </Routes>
    </HashRouter>
  );
}

export default App;
