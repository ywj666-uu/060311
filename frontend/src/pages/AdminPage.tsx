import React, { useEffect, useState, useCallback } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { Typography, Button, Card, Space, message, Spin, Alert } from 'antd';
import { ArrowLeftOutlined, DownloadOutlined, ThunderboltOutlined, ReloadOutlined } from '@ant-design/icons';
import SeatGrid from '../components/SeatGrid';
import { seatApi, registrationApi } from '../api';
import type { SeatMapResponse, SeatInfo } from '../types';

const { Title } = Typography;

const AdminPage: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const [seatMap, setSeatMap] = useState<SeatMapResponse | null>(null);
  const [loading, setLoading] = useState(true);
  const [allocating, setAllocating] = useState(false);
  const [swapSource, setSwapSource] = useState<SeatInfo | null>(null);
  const [adjustMode, setAdjustMode] = useState(false);

  const activityId = Number(id);

  /** 强制从后端拉取最新座位图（后端已同步更新DB和Redis） */
  const forceRefreshSeatMap = useCallback(async () => {
    try {
      const fresh = await seatApi.getSeatMap(activityId);
      setSeatMap(fresh);
    } catch {
      message.error('刷新座位图失败');
    }
  }, [activityId]);

  const loadSeatMap = useCallback(() => {
    setLoading(true);
    seatApi.getSeatMap(activityId)
      .then(setSeatMap)
      .finally(() => setLoading(false));
  }, [activityId]);

  useEffect(() => { loadSeatMap(); }, [loadSeatMap]);

  const handleAllocate = async () => {
    setAllocating(true);
    try {
      const result = await seatApi.allocate(activityId);
      message.success(`${result.message}，共分配 ${result.allocatedCount} 个座位`);
      // 分配后强制从后端重新拉取
      await forceRefreshSeatMap();
    } catch (err: any) {
      message.error(err.response?.data?.message || '分配失败');
    } finally {
      setAllocating(false);
    }
  };

  const handleExport = async () => {
    try {
      const blob = await seatApi.exportExcel(activityId);
      const url = window.URL.createObjectURL(new Blob([blob]));
      const link = document.createElement('a');
      link.href = url;
      link.setAttribute('download', `座位表.xlsx`);
      document.body.appendChild(link);
      link.click();
      link.remove();
      window.URL.revokeObjectURL(url);
      message.success('导出成功');
    } catch {
      message.error('导出失败');
    }
  };

  const handleSeatClick = async (seat: SeatInfo) => {
    if (!adjustMode) return;

    if (!swapSource) {
      if (seat.status !== 'ALLOCATED') {
        message.info('请先点击一个已分配的座位作为源');
        return;
      }
      setSwapSource(seat);
      message.info(`已选中 ${seat.rowNum}排${seat.colNum}列 (${seat.studentName})，请点击目标座位`);
    } else {
      if (seat.seatId === swapSource.seatId) {
        setSwapSource(null);
        message.info('已取消选择');
        return;
      }

      try {
        if (seat.status === 'ALLOCATED') {
          const regId1 = await findRegIdBySeatId(swapSource.seatId);
          const regId2 = await findRegIdBySeatId(seat.seatId);
          if (!regId1 || !regId2) {
            message.error('找不到对应报名记录');
            return;
          }
          await seatApi.swap(regId1, regId2);
        } else {
          const regId = await findRegIdBySeatId(swapSource.seatId);
          if (!regId) {
            message.error('找不到对应报名记录');
            return;
          }
          await seatApi.adjust(regId, seat.seatId);
        }
        message.success('座位调整成功，正在刷新...');
        // 调整完成后强制从后端重新拉取（确保DB+Redis同步后的最新状态）
        await forceRefreshSeatMap();
      } catch (err: any) {
        message.error(err.response?.data?.message || '调整失败');
      } finally {
        setSwapSource(null);
      }
    }
  };

  const findRegIdBySeatId = async (seatId: number): Promise<number | null> => {
    const regs = await registrationApi.list(activityId);
    const reg = regs.find(r => r.allocatedSeatId === seatId && r.status === 'ALLOCATED');
    return reg ? reg.id : null;
  };

  if (loading) return <div style={{ textAlign: 'center', padding: 100 }}><Spin size="large" /></div>;
  if (!seatMap) return <div>加载失败</div>;

  const allocated = seatMap.seats.filter(s => s.status === 'ALLOCATED').length;
  const total = seatMap.seats.filter(s => s.status !== 'UNAVAILABLE').length;
  const matched = seatMap.seats.filter(s => s.status === 'ALLOCATED' && s.preferenceMatched).length;

  return (
    <div style={{ maxWidth: 1000, margin: '0 auto', padding: 24 }}>
      <Button icon={<ArrowLeftOutlined />} onClick={() => navigate('/')} style={{ marginBottom: 16 }}>
        返回
      </Button>

      <Title level={3}>管理面板 - {seatMap.activityTitle}</Title>
      <p style={{ color: '#666', marginBottom: 16 }}>
        {seatMap.venueName} | 已分配: {allocated}/{total} 座 | 偏好命中: {matched}/{allocated}
      </p>

      <Card style={{ marginBottom: 16 }}>
        <Space wrap>
          <Button
            type="primary"
            icon={<ThunderboltOutlined />}
            loading={allocating}
            onClick={handleAllocate}
          >
            自动分配座位
          </Button>
          <Button
            type={adjustMode ? 'primary' : 'default'}
            danger={adjustMode}
            onClick={() => { setAdjustMode(!adjustMode); setSwapSource(null); }}
          >
            {adjustMode ? '退出调整模式' : '手动调整座位'}
          </Button>
          <Button icon={<ReloadOutlined />} onClick={forceRefreshSeatMap}>
            刷新
          </Button>
          <Button icon={<DownloadOutlined />} onClick={handleExport}>
            导出 Excel
          </Button>
        </Space>
      </Card>

      {adjustMode && (
        <Alert
          message="调整模式已开启"
          description={swapSource
            ? `已选中源: ${swapSource.rowNum}排${swapSource.colNum}列 (${swapSource.studentName})。点击另一个已分配座位进行交换，或点击空位进行移动。再次点击源座位取消。`
            : "点击一个已分配的座位选为源，再点击目标座位完成移动或交换。每次操作后自动同步后端并刷新视图。"}
          type="info"
          showIcon
          style={{ marginBottom: 16 }}
        />
      )}

      <Card>
        <SeatGrid
          totalRows={seatMap.totalRows}
          totalCols={seatMap.totalCols}
          seats={seatMap.seats}
          hasWindowLeft={seatMap.hasWindowLeft}
          hasWindowRight={seatMap.hasWindowRight}
          swapSource={swapSource}
          onSeatClick={adjustMode ? handleSeatClick : undefined}
        />
      </Card>
    </div>
  );
};

export default AdminPage;
