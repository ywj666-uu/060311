import { BrowserRouter, Routes, Route } from 'react-router-dom';
import { ConfigProvider } from 'antd';
import zhCN from 'antd/locale/zh_CN';
import HomePage from './pages/HomePage';
import RegisterPage from './pages/RegisterPage';
import SeatMapPage from './pages/SeatMapPage';
import AdminPage from './pages/AdminPage';

function App() {
  return (
    <ConfigProvider locale={zhCN}>
      <BrowserRouter>
        <Routes>
          <Route path="/" element={<HomePage />} />
          <Route path="/activity/:id/register" element={<RegisterPage />} />
          <Route path="/activity/:id/seats" element={<SeatMapPage />} />
          <Route path="/admin/activity/:id" element={<AdminPage />} />
        </Routes>
      </BrowserRouter>
    </ConfigProvider>
  );
}

export default App;
