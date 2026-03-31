import client from './client';
import type { SeatStatusDto } from '../types';

/** GET /shows/{showId}/seats — all seats for a show */
export const fetchSeats = async (showId: number): Promise<SeatStatusDto[]> => {
  const { data } = await client.get<SeatStatusDto[]>(`/shows/${showId}/seats`);
  return data;
};

/** GET /shows/{showId}/seats/{seatId} — single seat */
export const fetchSeat = async (showId: number, seatId: number): Promise<SeatStatusDto> => {
  const { data } = await client.get<SeatStatusDto>(`/shows/${showId}/seats/${seatId}`);
  return data;
};
