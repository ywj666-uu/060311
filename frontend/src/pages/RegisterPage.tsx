import React, { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { Card, Typography, Button, message, Table } from 'antd';
import { ArrowLeftOutlined } from '@ant-design/icons';
import RegistrationForm from '../components/RegistrationForm';
import { activityApi, registrationApi } from '../api';
import type { Activity, Registration } from '../types';

const { Title } = Typography;

const RegisterPage: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const [activity, setActivity] = useState<Activity | null>(null);
  const [registrations, setRegistrations] = useState<Registration[]>([]);

  const activityId = Number(id);

  const loadData = () => {
    activityApi.get(activityId).then(setActivity);
    registrationApi.list(activityId).then(setRegistrations);
  };

  useEffect(() => { loadData(); }, [activityId]);

  const columns = [
    { title: '报名ID', dataIndex: 'id', key: 'id' },
    { title: '偏好', dataIndex: 'preferredArea', key: 'preferredArea',
      render: (v: string) => ({ FRONT: '前排', BACK: '后排', WINDOW: '靠窗', ANY: '不限' }[v] || v)
    },
    { title: '状态', dataIndex: 'status', key: 'status',
      render: (v: string) => ({ PENDING: '待分配', ALLOCATED: '已分配', CANCELLED: '已取消' }[v] || v)
    },
    { title: '时间', dataIndex: 'registrationTime', key: 'time',
      render: (v: string) => v ? new Date(v).toLocaleString('zh-CN') : '-'
    },
  ];

  return (
    <div style={{ maxWidth: 800, margin: '0 auto', padding: 24 }}>
      <Button icon={<ArrowLeftOutlined />} onClick={() => navigate('/')} style={{ marginBottom: 16 }}>
        返回
      </Button>

      <Title level={3}>{activity?.title || '加载中...'} - 报名</Title>

      {activity?.status === 'OPEN' ? (
        <Card title="填写报名信息" style={{ marginBottom: 24 }}>
          <RegistrationForm activityId={activityId} onSuccess={loadData} />
        </Card>
      ) : (
        <Card style={{ marginBottom: 24 }}>
          <p>当前活动不在报名期间</p>
        </Card>
      )}

      <Card title={`已报名列表 (${registrations.filter(r => r.status !== 'CANCELLED').length}人)`}>
        <Table
          dataSource={registrations.filter(r => r.status !== 'CANCELLED')}
          columns={columns}
          rowKey="id"
          size="small"
          pagination={false}
        />
      </Card>
    </div>
  );
};

export default RegisterPage;
