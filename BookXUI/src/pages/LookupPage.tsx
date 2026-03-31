import { useState } from 'react';
import { motion } from 'framer-motion';
import PageTransition from '../components/common/PageTransition';
import Loader from '../components/common/Loader';
import ErrorDisplay from '../components/common/ErrorDisplay';
import { fetchBooking } from '../api';
import type { BookingResponseDto } from '../types';

export default function LookupPage() {
  const [ref, setRef] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [booking, setBooking] = useState<BookingResponseDto | null>(null);

  const handleSearch = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!ref.trim()) return;
    setLoading(true);
    setError(null);
    setBooking(null);
    try {
      const data = await fetchBooking(ref.trim());
      setBooking(data);
    } catch (err: unknown) {
      setError(err instanceof Error ? err.message : 'Booking not found');
    } finally {
      setLoading(false);
    }
  };

  return (
    <PageTransition>
      <div className="lookup-page container">
        <motion.h1
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
        >
          Find Your Booking
        </motion.h1>

        <motion.form
          className="lookup-form"
          onSubmit={handleSearch}
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ delay: 0.15 }}
        >
          <input
            type="text"
            placeholder="Enter booking reference (e.g. BXS-a1b2c3d4)"
            value={ref}
            onChange={(e) => setRef(e.target.value)}
            required
          />
          <button type="submit" className="btn btn--primary" disabled={loading || !ref.trim()}>
            {loading ? 'Searching…' : 'Search'}
          </button>
        </motion.form>

        {loading && <Loader text="Looking up your booking…" />}
        {error && <ErrorDisplay message={error} />}

        {booking && (
          <motion.div
            className="lookup-result"
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ type: 'spring', stiffness: 300, damping: 25 }}
          >
            <h2>Booking Details</h2>

            <div className="summary-row">
              <span className="summary-row__label">Reference</span>
              <span className="summary-row__value" style={{ fontFamily: 'monospace', color: 'var(--bx-gold)' }}>
                {booking.bookingReference}
              </span>
            </div>

            <div className="summary-row">
              <span className="summary-row__label">Outcome</span>
              <span
                className="summary-row__value"
                style={{ color: booking.outcome === 'BOOKED' ? 'var(--success)' : 'var(--danger)' }}
              >
                {booking.outcome}
              </span>
            </div>

            <div className="summary-row">
              <span className="summary-row__label">Show ID</span>
              <span className="summary-row__value">{booking.showId}</span>
            </div>

            <div className="summary-row">
              <span className="summary-row__label">Seat</span>
              <span className="summary-row__value">{booking.seatNumber} (ID: {booking.seatId})</span>
            </div>

            <div className="summary-row">
              <span className="summary-row__label">User</span>
              <span className="summary-row__value">{booking.userId}</span>
            </div>

            <div className="summary-row">
              <span className="summary-row__label">Seat Status</span>
              <span className="summary-row__value">{booking.seatStatus}</span>
            </div>
          </motion.div>
        )}
      </div>
    </PageTransition>
  );
}
