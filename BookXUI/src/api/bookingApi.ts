import client from './client';
import type { BookingRequestDto, BookingResponseDto } from '../types';

/** POST /bookings — create a new booking */
export const createBooking = async (req: BookingRequestDto): Promise<BookingResponseDto> => {
  const { data } = await client.post<BookingResponseDto>('/bookings', req);
  return data;
};

/** GET /bookings/{bookingReference} — lookup booking */
export const fetchBooking = async (ref: string): Promise<BookingResponseDto> => {
  const { data } = await client.get<BookingResponseDto>(`/bookings/${encodeURIComponent(ref)}`);
  return data;
};
