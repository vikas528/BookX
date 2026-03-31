import { useLocation, useNavigate, Navigate } from 'react-router-dom';
import { motion } from 'framer-motion';
import PageTransition from '../components/common/PageTransition';
import Confetti from '../components/common/Confetti';
import type { BookingResponseDto, Show } from '../types';

export default function ConfirmationPage() {
  const location = useLocation();
  const navigate = useNavigate();
  const state = location.state as { booking?: BookingResponseDto; show?: Show } | null;

  if (!state?.booking) {
    return <Navigate to="/" replace />;
  }

  const { booking, show } = state;
  const isSuccess = booking.outcome === 'BOOKED';

  return (
    <PageTransition>
      {isSuccess && <Confetti />}

      <div className="confirmation container">
        <motion.div
          className={`confirmation__icon ${isSuccess ? 'confirmation__icon--success' : 'confirmation__icon--fail'}`}
          initial={{ scale: 0 }}
          animate={{ scale: 1 }}
          transition={{ type: 'spring', stiffness: 260, damping: 15, delay: 0.2 }}
        >
          {isSuccess ? '✓' : '✗'}
        </motion.div>

        <motion.h1
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ delay: 0.35 }}
        >
          {isSuccess ? 'Booking Confirmed!' : bookingOutcomeLabel(booking.outcome)}
        </motion.h1>

        <motion.p
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ delay: 0.45 }}
        >
          {isSuccess
            ? `Your seat ${booking.seatNumber} for "${show?.name ?? 'the show'}" has been booked successfully.`
            : outcomeMessage(booking.outcome)}
        </motion.p>

        {booking.bookingReference && (
          <motion.div
            className="confirmation__ref"
            initial={{ opacity: 0, scale: 0.9 }}
            animate={{ opacity: 1, scale: 1 }}
            transition={{ delay: 0.6, type: 'spring' }}
          >
            {booking.bookingReference}
          </motion.div>
        )}

        <motion.div
          style={{ display: 'flex', gap: '1rem', marginTop: '2.5rem' }}
          initial={{ opacity: 0 }}
          animate={{ opacity: 1 }}
          transition={{ delay: 0.8 }}
        >
          {isSuccess && (
            <button className="btn btn--secondary" onClick={() => navigate('/lookup')}>
              View Booking
            </button>
          )}
          <button className="btn btn--primary" onClick={() => navigate('/')}>
            Back to Shows
          </button>
        </motion.div>
      </div>
    </PageTransition>
  );
}

function bookingOutcomeLabel(outcome: string): string {
  switch (outcome) {
    case 'SEAT_UNAVAILABLE':
      return 'Seat Unavailable';
    case 'PAYMENT_FAILED':
      return 'Payment Failed';
    case 'LOCK_EXPIRED':
      return 'Lock Expired';
    default:
      return 'Booking Failed';
  }
}

function outcomeMessage(outcome: string): string {
  switch (outcome) {
    case 'SEAT_UNAVAILABLE':
      return 'This seat was grabbed by someone else. Please go back and pick another seat.';
    case 'PAYMENT_FAILED':
      return 'Your payment could not be processed. The seat has been released. Please try again.';
    case 'LOCK_EXPIRED':
      return 'The seat lock expired before payment completed. Please try again quickly.';
    default:
      return 'Something went wrong. Please try again.';
  }
}
