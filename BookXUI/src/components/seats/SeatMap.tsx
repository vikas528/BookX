import { motion } from 'framer-motion';
import type { SeatStatusDto } from '../../types';

interface Props {
  seats: SeatStatusDto[];
  selectedSeatId: number | null;
  onSelect: (seat: SeatStatusDto) => void;
}

export default function SeatMap({ seats, selectedSeatId, onSelect }: Props) {
  return (
    <>
      <div className="screen-indicator">
        <div className="screen-indicator__line" />
        <span className="screen-indicator__label">Screen this way</span>
      </div>

      <div className="seat-grid">
        {seats.map((seat, i) => {
          const isSelected = seat.seatId === selectedSeatId;
          const isClickable = seat.status === 'AVAILABLE' || isSelected;

          let className = 'seat';
          if (isSelected) {
            className += ' seat--selected';
          } else {
            className += ` seat--${seat.status.toLowerCase()}`;
          }

          return (
            <motion.button
              key={seat.seatId}
              className={className}
              onClick={() => isClickable && onSelect(seat)}
              disabled={!isClickable}
              title={`${seat.seatNumber} — ${isSelected ? 'Selected' : seat.status}`}
              initial={{ opacity: 0, scale: 0.5 }}
              animate={{ opacity: 1, scale: 1 }}
              transition={{
                delay: i * 0.015,
                type: 'spring',
                stiffness: 400,
                damping: 20,
              }}
              whileHover={isClickable ? { scale: 1.2 } : undefined}
              whileTap={isClickable ? { scale: 0.9 } : undefined}
            >
              {seat.seatNumber}
            </motion.button>
          );
        })}
      </div>

      <SeatLegend />
    </>
  );
}

function SeatLegend() {
  const items = [
    { color: 'var(--seat-available)', label: 'Available' },
    { color: 'var(--seat-selected)', label: 'Selected' },
    { color: 'var(--seat-locked)', label: 'Locked' },
    { color: 'var(--seat-booked)', label: 'Booked' },
  ];

  return (
    <div className="seat-legend">
      {items.map((item) => (
        <div key={item.label} className="seat-legend__item">
          <div className="seat-legend__dot" style={{ backgroundColor: item.color }} />
          <span>{item.label}</span>
        </div>
      ))}
    </div>
  );
}
