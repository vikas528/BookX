import { useState, useCallback } from 'react';
import { useParams, useLocation, useNavigate } from 'react-router-dom';
import PageTransition from '../components/common/PageTransition';
import SeatMap from '../components/seats/SeatMap';
import BookingForm from '../components/booking/BookingForm';
import Loader from '../components/common/Loader';
import ErrorDisplay from '../components/common/ErrorDisplay';
import Toast from '../components/common/Toast';
import SkeletonSeats from '../components/common/SkeletonSeats';
import { useApi } from '../hooks/useApi';
import { fetchSeats, createBooking } from '../api';
import type { SeatStatusDto, Show } from '../types';
import showsCatalogue from '../data/shows';

export default function SeatPage() {
  const { showId } = useParams<{ showId: string }>();
  const location = useLocation();
  const navigate = useNavigate();
  const numericShowId = Number(showId);

  // Get show info from navigation state or fallback to catalogue
  const show: Show | undefined =
    (location.state as { show?: Show })?.show ??
    showsCatalogue.find((s) => s.id === numericShowId);

  const [selectedSeat, setSelectedSeat] = useState<SeatStatusDto | null>(null);
  const [booking, setBooking] = useState(false);
  const [toast, setToast] = useState<{ message: string; type: 'success' | 'error' } | null>(null);

  const { data: seats, loading, error, refetch } = useApi(
    () => fetchSeats(numericShowId),
    [numericShowId],
  );

  const handleSeatSelect = useCallback(
    (seat: SeatStatusDto) => {
      setSelectedSeat((prev) => (prev?.seatId === seat.seatId ? null : seat));
    },
    [],
  );

  const handleBook = useCallback(
    async (userId: string) => {
      if (!selectedSeat || !show) return;
      setBooking(true);
      try {
        const res = await createBooking({
          showId: numericShowId,
          seatId: selectedSeat.seatId,
          userId,
          amount: show.price,
        });
        navigate('/confirmation', { state: { booking: res, show } });
      } catch (err: unknown) {
        const message = err instanceof Error ? err.message : 'Booking failed';
        setToast({ message, type: 'error' });
      } finally {
        setBooking(false);
      }
    },
    [selectedSeat, show, numericShowId, navigate],
  );

  if (!show) {
    return (
      <PageTransition>
        <div className="container" style={{ paddingTop: '4rem' }}>
          <ErrorDisplay message={`Show #${showId} not found in catalogue. Sync it from the admin service first.`} />
        </div>
      </PageTransition>
    );
  }

  return (
    <PageTransition>
      <div className="container seat-page">
        <div className="seat-page__header">
          <h1>{show.name}</h1>
          <p>
            {show.venue} &bull; {show.language} &bull; {show.duration}
          </p>
        </div>

        <div className="two-col">
          {/* Left: Seat map */}
          <div>
            {loading && <SkeletonSeats />}
            {error && <ErrorDisplay message={error} onRetry={refetch} />}
            {seats && (
              <SeatMap
                seats={seats}
                selectedSeatId={selectedSeat?.seatId ?? null}
                onSelect={handleSeatSelect}
              />
            )}
          </div>

          {/* Right: Booking panel */}
          <div>
            {selectedSeat ? (
              <BookingForm
                show={show}
                seat={selectedSeat}
                loading={booking}
                onSubmit={handleBook}
              />
            ) : (
              <div className="booking-panel" style={{ textAlign: 'center', color: 'var(--text-secondary)' }}>
                <p>👆 Select an available seat to continue</p>
              </div>
            )}
          </div>
        </div>
      </div>

      {toast && (
        <Toast message={toast.message} type={toast.type} onClose={() => setToast(null)} />
      )}
    </PageTransition>
  );
}
