import { useState } from 'react';
import { motion } from 'framer-motion';
import type { SeatStatusDto, Show } from '../../types';

interface Props {
  show: Show;
  seat: SeatStatusDto;
  loading: boolean;
  onSubmit: (userId: string) => void;
}

export default function BookingForm({ show, seat, loading, onSubmit }: Props) {
  const [userId, setUserId] = useState('');

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (userId.trim()) {
      onSubmit(userId.trim());
    }
  };

  return (
    <motion.div
      className="booking-panel"
      initial={{ opacity: 0, x: 40 }}
      animate={{ opacity: 1, x: 0 }}
      transition={{ duration: 0.4, ease: [0.16, 1, 0.3, 1] }}
    >
      <h3 className="booking-panel__title">Booking Summary</h3>

      <div className="summary-row">
        <span className="summary-row__label">Show</span>
        <span className="summary-row__value">{show.name}</span>
      </div>

      <div className="summary-row">
        <span className="summary-row__label">Venue</span>
        <span className="summary-row__value">{show.venue}</span>
      </div>

      <div className="summary-row">
        <span className="summary-row__label">Seat</span>
        <span className="summary-row__value">{seat.seatNumber}</span>
      </div>

      <div className="summary-row summary-row--total">
        <span className="summary-row__label">Total</span>
        <span className="summary-row__value">₹{show.price.toFixed(2)}</span>
      </div>

      <form onSubmit={handleSubmit} style={{ marginTop: '1.5rem' }}>
        <div className="form-group">
          <label htmlFor="userId">Your User ID</label>
          <input
            id="userId"
            type="text"
            placeholder="e.g. john_doe"
            value={userId}
            onChange={(e) => setUserId(e.target.value)}
            required
            autoComplete="off"
          />
        </div>

        <motion.button
          type="submit"
          className="btn btn--primary btn--block btn--lg"
          disabled={loading || !userId.trim()}
          whileHover={{ scale: 1.02 }}
          whileTap={{ scale: 0.97 }}
        >
          {loading ? (
            <>
              <span className="spinner" style={{ width: 20, height: 20, borderWidth: 3 }} />
              Processing…
            </>
          ) : (
            <>🎬 Book Now &mdash; ₹{show.price}</>
          )}
        </motion.button>
      </form>
    </motion.div>
  );
}
