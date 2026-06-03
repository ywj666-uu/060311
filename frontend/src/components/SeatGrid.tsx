import React from 'react';
import { Tag } from 'antd';
import SeatCell from './SeatCell';
import type { SeatInfo } from '../types';

interface SeatGridProps {
  totalRows: number;
  totalCols: number;
  seats: SeatInfo[];
  hasWindowLeft?: boolean;
  hasWindowRight?: boolean;
  selectedSeat?: SeatInfo | null;
  swapSource?: SeatInfo | null;
  onSeatClick?: (seat: SeatInfo) => void;
}

const SeatGrid: React.FC<SeatGridProps> = ({
  totalRows, totalCols, seats, hasWindowLeft, hasWindowRight,
  selectedSeat, swapSource, onSeatClick
}) => {
  const seatMap = new Map<string, SeatInfo>();
  seats.forEach(s => seatMap.set(`${s.rowNum}-${s.colNum}`, s));

  return (
    <div style={{ padding: 20 }}>
      <div style={{
        textAlign: 'center',
        marginBottom: 16,
        padding: '8px 0',
        background: '#f0f0f0',
        borderRadius: 4,
        fontWeight: 'bold',
        fontSize: 16,
      }}>
        讲台
      </div>

      <div style={{ display: 'flex', gap: 4, marginBottom: 16, justifyContent: 'center', flexWrap: 'wrap' }}>
        <Tag color="green">空闲</Tag>
        <Tag color="blue">已分配</Tag>
        <Tag color="purple">团队</Tag>
        <Tag color="orange">选中</Tag>
        <Tag color="default">不可用</Tag>
      </div>

      <div style={{ display: 'flex', justifyContent: 'center' }}>
        {hasWindowLeft && (
          <div style={{
            writingMode: 'vertical-rl',
            display: 'flex', alignItems: 'center',
            padding: '0 8px', color: '#999', fontSize: 12,
          }}>
            窗户
          </div>
        )}

        <div style={{ display: 'flex', flexDirection: 'column', gap: 6 }}>
          {Array.from({ length: totalRows }, (_, rowIdx) => (
            <div key={rowIdx} style={{ display: 'flex', gap: 6, alignItems: 'center' }}>
              <span style={{ width: 40, fontSize: 12, color: '#999', textAlign: 'right' }}>
                {rowIdx + 1}排
              </span>
              {Array.from({ length: totalCols }, (_, colIdx) => {
                const seat = seatMap.get(`${rowIdx + 1}-${colIdx + 1}`);
                if (!seat) return <div key={colIdx} style={{ width: 52, height: 52 }} />;
                return (
                  <SeatCell
                    key={colIdx}
                    seat={seat}
                    selected={selectedSeat?.seatId === seat.seatId}
                    isSwapSource={swapSource?.seatId === seat.seatId}
                    onClick={onSeatClick}
                  />
                );
              })}
            </div>
          ))}
        </div>

        {hasWindowRight && (
          <div style={{
            writingMode: 'vertical-rl',
            display: 'flex', alignItems: 'center',
            padding: '0 8px', color: '#999', fontSize: 12,
          }}>
            窗户
          </div>
        )}
      </div>

      <div style={{ display: 'flex', justifyContent: 'center', marginTop: 8, gap: 6 }}>
        <span style={{ width: 40 }} />
        {Array.from({ length: totalCols }, (_, i) => (
          <span key={i} style={{ width: 52, textAlign: 'center', fontSize: 11, color: '#999' }}>
            {i + 1}列
          </span>
        ))}
      </div>
    </div>
  );
};

export default SeatGrid;
