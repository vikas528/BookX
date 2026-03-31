/* ─── Types matching the BookXShow backend DTOs ─── */

export type SeatStatus = 'AVAILABLE' | 'LOCKED' | 'BOOKED';
export type BookingOutcome = 'BOOKED' | 'SEAT_UNAVAILABLE' | 'PAYMENT_FAILED' | 'LOCK_EXPIRED';
export type BookingStatus = 'CONFIRMED' | 'CANCELLED';

export interface SeatStatusDto {
  seatId: number;
  seatNumber: string;
  showId: number;
  status: SeatStatus;
  lockedBy: string | null;
  lockExpiry: string | null;
}

export interface BookingRequestDto {
  showId: number;
  seatId: number;
  userId: string;
  amount: number;
}

export interface BookingResponseDto {
  bookingReference: string | null;
  seatId: number;
  seatNumber: string;
  showId: number;
  userId: string;
  outcome: BookingOutcome;
  seatStatus: SeatStatus;
}

export interface ErrorResponseDto {
  timestamp: string;
  status: number;
  error: string;
  message: string;
  path: string;
}

export interface ShowDto {
  id: number;
  externalShowId: string;
  name: string;
  venue: string;
  showDateTime: string;
  totalSeats: number;
  seatsPerRow: number;
  basePrice: number;
  genre: string;
  language: string;
  duration: string;
  rating: number;
  posterUrl: string;
}

/* ─── UI-only models ─── */

export interface Show {
  id: number;
  externalShowId: string;
  name: string;
  venue: string;
  showDateTime: string;
  totalSeats: number;
  seatsPerRow: number;
  genre: string;
  language: string;
  duration: string;
  rating: number;
  posterUrl: string;
  price: number;
}
