/**
 * ═══════════════════════════════════════════════════════════════
 *  Mock API Adapter — for UI-only preview (no backend required)
 * ═══════════════════════════════════════════════════════════════
 *
 *  Activated by running:    npm run dev:mock
 *  (which launches Vite in --mode mock)
 *
 *  This file intercepts every Axios request on the shared client
 *  and returns realistic dummy data with a simulated network delay.
 *  NO production code is changed — run `npm run dev` for real backend.
 */

import type { AxiosInstance } from 'axios';
import MockAdapter from 'axios-mock-adapter';
import type { SeatStatusDto, BookingResponseDto, BookingRequestDto } from '../types';
import shows from '../data/shows';

/** Simulated network delay range (ms) */
const DELAY_MIN = 400;
const DELAY_MAX = 900;

// ── In-memory state so the UI feels "live" ──────────────────────
const bookedSeats = new Map<string, string>(); // "showId-seatId" → bookingRef
const bookings = new Map<string, BookingResponseDto>();

/** Generate seats for a given show */
function generateSeats(showId: number, total: number, seatsPerRow: number): SeatStatusDto[] {
  const rows = 'ABCDEFGHIJKLMNOPQRSTUVWXYZ';
  const seats: SeatStatusDto[] = [];

  for (let i = 0; i < total; i++) {
    const row = rows[Math.floor(i / seatsPerRow)];
    const col = (i % seatsPerRow) + 1;
    const seatId = showId * 1000 + i + 1;
    const seatNumber = `${row}${col}`;
    const key = `${showId}-${seatId}`;

    let status: SeatStatusDto['status'] = 'AVAILABLE';
    let lockedBy: string | null = null;
    let lockExpiry: string | null = null;

    if (bookedSeats.has(key)) {
      status = 'BOOKED';
    } else if (Math.random() < 0.08) {
      // ~8% pre-booked to make the map look realistic
      status = 'BOOKED';
    } else if (Math.random() < 0.04) {
      // ~4% locked by "someone else"
      status = 'LOCKED';
      lockedBy = 'other_user';
      lockExpiry = new Date(Date.now() + 30_000).toISOString();
    }

    seats.push({ seatId, seatNumber, showId, status, lockedBy, lockExpiry });
  }
  return seats;
}

// Pre-seed seat maps per show (lazy, cached)
const seatCache = new Map<number, SeatStatusDto[]>();
function getSeats(showId: number): SeatStatusDto[] {
  if (!seatCache.has(showId)) {
    const show = shows.find((s) => s.id === showId);
    const total = show?.totalSeats ?? 60;
    const perRow = show?.seatsPerRow ?? 12;
    seatCache.set(showId, generateSeats(showId, total, perRow));
  }
  return seatCache.get(showId)!;
}

function randomRef(): string {
  return `BXS-${Math.random().toString(36).substring(2, 10)}`;
}

// ── Register mock routes ─────────────────────────────────────
export function enableMockApi(client: AxiosInstance): void {
  const mock = new MockAdapter(client, {
    delayResponse: Math.floor(Math.random() * (DELAY_MAX - DELAY_MIN) + DELAY_MIN),
  });

  // GET /shows/:showId/seats
  mock.onGet(/\/shows\/(\d+)\/seats$/).reply((cfg) => {
    const match = cfg.url?.match(/\/shows\/(\d+)\/seats$/);
    const showId = Number(match?.[1]);
    if (!showId) return [404, { message: 'Show not found' }];
    return [200, getSeats(showId)];
  });

  // GET /shows/:showId/seats/:seatId
  mock.onGet(/\/shows\/(\d+)\/seats\/(\d+)$/).reply((cfg) => {
    const match = cfg.url?.match(/\/shows\/(\d+)\/seats\/(\d+)$/);
    const showId = Number(match?.[1]);
    const seatId = Number(match?.[2]);
    const seats = getSeats(showId);
    const seat = seats.find((s) => s.seatId === seatId);
    if (!seat) return [404, { message: 'Seat not found' }];
    return [200, seat];
  });

  // POST /bookings
  mock.onPost('/bookings').reply((cfg) => {
    const req: BookingRequestDto = JSON.parse(cfg.data);
    const seats = getSeats(req.showId);
    const seat = seats.find((s) => s.seatId === req.seatId);

    if (!seat || seat.status !== 'AVAILABLE') {
      const res: BookingResponseDto = {
        bookingReference: null,
        seatId: req.seatId,
        seatNumber: seat?.seatNumber ?? '??',
        showId: req.showId,
        userId: req.userId,
        outcome: 'SEAT_UNAVAILABLE',
        seatStatus: seat?.status ?? 'BOOKED',
      };
      return [200, res];
    }

    // Simulate ~90% payment success
    if (Math.random() < 0.1) {
      const res: BookingResponseDto = {
        bookingReference: null,
        seatId: req.seatId,
        seatNumber: seat.seatNumber,
        showId: req.showId,
        userId: req.userId,
        outcome: 'PAYMENT_FAILED',
        seatStatus: 'AVAILABLE',
      };
      return [200, res];
    }

    // Success!
    const ref = randomRef();
    seat.status = 'BOOKED';
    seat.lockedBy = null;
    seat.lockExpiry = null;
    bookedSeats.set(`${req.showId}-${req.seatId}`, ref);

    const res: BookingResponseDto = {
      bookingReference: ref,
      seatId: req.seatId,
      seatNumber: seat.seatNumber,
      showId: req.showId,
      userId: req.userId,
      outcome: 'BOOKED',
      seatStatus: 'BOOKED',
    };
    bookings.set(ref, res);
    return [201, res];
  });

  // GET /bookings/:ref
  mock.onGet(/\/bookings\/(.+)$/).reply((cfg) => {
    const match = cfg.url?.match(/\/bookings\/(.+)$/);
    const ref = decodeURIComponent(match?.[1] ?? '');
    const found = bookings.get(ref);
    if (!found) return [404, { message: `Booking ${ref} not found` }];
    return [200, found];
  });

  // POST /admin/shows/:id/sync  (just acknowledge)
  mock.onPost(/\/admin\/shows\/.+\/sync$/).reply(() => {
    return [200, 'Show synced successfully (mock)'];
  });

  console.info(
    '%c🎬 BookXShow Mock API enabled — no backend required',
    'color: #e50914; font-weight: bold; font-size: 14px;',
  );
}
