import React from 'react';
import { Tooltip } from 'antd';
import type { SeatInfo } from '../types';

interface SeatCellProps {
  seat: SeatInfo;
  selected?: boolean;
  isSwapSource?: boolean;
  onClick?: (seat: SeatInfo) => void;
}

const SeatCell: React.FC<SeatCellProps> = ({ seat, selected, isSwapSource, onClick }) => {
  const getColor = () => {
    if (isSwapSource) return '#faad14';
    if (selected) return '#ff7a45';
    if (seat.status === 'ALLOCATED') {
      if (seat.teamId) return '#722ed1';
      if (!seat.preferenceMatched && seat.preferredArea && seat.preferredArea !== 'ANY') return '#cf1322';
      return '#1890ff';
    }
    if (seat.status === 'UNAVAILABLE') return '#d9d9d9';
    return '#52c41a';
  };

  const getLabel = () => {
    if (seat.status === 'ALLOCATED' && seat.studentName) {
      return seat.studentName.length > 2 ? seat.studentName.substring(0, 2) : seat.studentName;
    }
    return '';
  };

  const tooltipContent = () => {
    const parts = [`${seat.rowNum}排${seat.colNum}列`];
    parts.push(`区域: ${translateArea(seat.areaTag)}`);
    if (seat.studentName) parts.push(`学生: ${seat.studentName}`);
    if (seat.studentId) parts.push(`学号: ${seat.studentId}`);
    if (seat.teamName) parts.push(`团队: ${seat.teamName}`);
    if (seat.status === 'ALLOCATED') {
      parts.push(`偏好: ${translateArea(seat.preferredArea)}`);
      parts.push(`匹配: ${seat.preferenceMatched ? '是' : '否'}`);
    }
    return parts.join('\n');
  };

  return (
    <Tooltip title={<pre style={{ margin: 0, fontSize: 12 }}>{tooltipContent()}</pre>}>
      <div
        onClick={() => onClick?.(seat)}
        style={{
          width: 52,
          height: 52,
          backgroundColor: getColor(),
          borderRadius: 6,
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          cursor: onClick ? 'pointer' : 'default',
          color: '#fff',
          fontSize: 11,
          fontWeight: 500,
          border: selected ? '3px solid #ff4d4f' : '2px solid rgba(255,255,255,0.3)',
          transition: 'all 0.2s',
          userSelect: 'none',
        }}
      >
        {getLabel()}
      </div>
    </Tooltip>
  );
};

function translateArea(tag: string | null): string {
  if (!tag) return '不限';
  const map: Record<string, string> = {
    FRONT: '前排', BACK: '后排', MIDDLE: '中间',
    WINDOW: '靠窗', WINDOW_LEFT: '靠窗(左)', WINDOW_RIGHT: '靠窗(右)',
    ANY: '不限',
  };
  return map[tag] || tag;
}

export default SeatCell;
