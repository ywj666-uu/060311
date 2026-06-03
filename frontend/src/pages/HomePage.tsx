import React, { useEffect, useState } from 'react';
import { Card, List, Tag, Button, Typography, Space } from 'antd';
import { useNavigate } from 'react-router-dom';
import { CalendarOutlined, EnvironmentOutlined } from '@ant-design/icons';
import { activityApi } from '../api';
import type { Activity } from '../types';

const { Title } = Typography;

const HomePage: React.FC = () => {
  const [activities, setActivities] = useState<Activity[]>([]);
  const [loading, setLoading] = useState(true);
  const navigate = useNavigate();

  useEffect(() => {
    activityApi.list().then(setActivities).finally(() => setLoading(false));
  }, []);

  const statusColor = (status: string) => {
    switch (status) {
      case 'OPEN': return 'green';
      case 'CLOSED': return 'red';
      case 'FINISHED': return 'default';
      default: return 'blue';
    }
  };

  const statusText = (status: string) => {
    switch (status) {
      case 'OPEN': return '报名中';
      case 'CLOSED': return '已截止';
      case 'FINISHED': return '已结束';
      default: return status;
    }
  };

  return (
    <div style={{ maxWidth: 900, margin: '0 auto', padding: 24 }}>
      <Title level={2} style={{ textAlign: 'center', marginBottom: 32 }}>
        校园活动座位智能分配系统
      </Title>

      <List
        loading={loading}
        grid={{ gutter: 16, xs: 1, sm: 1, md: 2, lg: 2 }}
        dataSource={activities}
        renderItem={(item) => (
          <List.Item>
            <Card
              title={item.title}
              extra={<Tag color={statusColor(item.status)}>{statusText(item.status)}</Tag>}
              actions={[
                <Button type="link" onClick={() => navigate(`/activity/${item.id}/register`)}>
                  报名
                </Button>,
                <Button type="link" onClick={() => navigate(`/activity/${item.id}/seats`)}>
                  座位图
                </Button>,
                <Button type="link" onClick={() => navigate(`/admin/activity/${item.id}`)}>
                  管理
                </Button>,
              ]}
            >
              <p>{item.description}</p>
              <Space direction="vertical" size={4}>
                <span><CalendarOutlined /> {item.startTime ? new Date(item.startTime).toLocaleString('zh-CN') : '待定'}</span>
                <span><EnvironmentOutlined /> 场地ID: {item.venueId}</span>
              </Space>
            </Card>
          </List.Item>
        )}
      />
    </div>
  );
};

export default HomePage;
