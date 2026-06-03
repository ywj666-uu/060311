import React, { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { Typography, Button, Spin } from 'antd';
import { ArrowLeftOutlined } from '@ant-design/icons';
import SeatGrid from '../components/SeatGrid';
import { seatApi } from '../api';
import type { SeatMapResponse } from '../types';

const { Title } = Typography;

const SeatMapPage: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const [seatMap, setSeatMap] = useState<SeatMapResponse | null>(null);
  const [loading, setLoading] = useState(true);

  const activityId = Number(id);

  useEffect(() => {
    seatApi.getSeatMap(activityId)
      .then(setSeatMap)
      .finally(() => setLoading(false));
  }, [activityId]);

  if (loading) return <div style={{ textAlign: 'center', padding: 100 }}><Spin size="large" /></div>;
  if (!seatMap) return <div>加载失败</div>;

  const allocated = seatMap.seats.filter(s => s.status === 'ALLOCATED').length;
  const total = seatMap.seats.filter(s => s.status !== 'UNAVAILABLE').length;

  return (
    <div style={{ maxWidth: 900, margin: '0 auto', padding: 24 }}>
      <Button icon={<ArrowLeftOutlined />} onClick={() => navigate('/')} style={{ marginBottom: 16 }}>
        返回
      </Button>

      <Title level={3}>{seatMap.activityTitle} - 座位图</Title>
      <p style={{ color: '#666', marginBottom: 16 }}>
        场地: {seatMap.venueName} | 已分配: {allocated}/{total} 座
      </p>

      <div style={{ background: '#fafafa', borderRadius: 8, padding: 16 }}>
        <SeatGrid
          totalRows={seatMap.totalRows}
          totalCols={seatMap.totalCols}
          seats={seatMap.seats}
          hasWindowLeft={seatMap.hasWindowLeft}
          hasWindowRight={seatMap.hasWindowRight}
        />
      </div>
    </div>
  );
};

export default SeatMapPage;
