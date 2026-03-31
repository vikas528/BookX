import type { Show } from '../types';

/**
 * ══════════════════════════════════════════════════════════════
 *  Mock show catalogue
 * ══════════════════════════════════════════════════════════════
 *
 *  The BookXShow backend does NOT expose a "list all shows" API.
 *  Shows are synced one-by-one from the Admin Service via
 *  POST /admin/shows/{externalShowId}/sync.
 *
 *  This file provides a **static catalogue** so the UI can
 *  render a polished home page immediately. Each entry's `id`
 *  field must match the show ID in your local database after
 *  syncing.
 *
 *  ➜  Edit / extend this list as you sync more shows.
 */

const shows: Show[] = [
  {
    id: 1,
    externalShowId: 'SHOW-001',
    name: 'Interstellar — IMAX Experience',
    venue: 'PVR Orion Mall, Bangalore',
    showDateTime: '2026-04-05T18:30:00',
    totalSeats: 120,
    seatsPerRow: 12,
    genre: 'Sci-Fi',
    language: 'English',
    duration: '2h 49m',
    rating: 4.8,
    posterUrl: 'https://images.unsplash.com/photo-1534796636912-3b95b3ab5986?w=400&h=600&fit=crop',
    price: 350,
  },
  {
    id: 2,
    externalShowId: 'SHOW-002',
    name: 'Pushpa 2 — The Rule',
    venue: 'INOX GVK One, Hyderabad',
    showDateTime: '2026-04-06T20:00:00',
    totalSeats: 200,
    seatsPerRow: 20,
    genre: 'Action',
    language: 'Telugu',
    duration: '3h 10m',
    rating: 4.5,
    posterUrl: 'https://images.unsplash.com/photo-1440404653325-ab127d49abc1?w=400&h=600&fit=crop',
    price: 300,
  },
  {
    id: 3,
    externalShowId: 'SHOW-003',
    name: 'Dune: Part Three',
    venue: 'Cinépolis Seasons Mall, Pune',
    showDateTime: '2026-04-07T15:00:00',
    totalSeats: 150,
    seatsPerRow: 15,
    genre: 'Sci-Fi',
    language: 'English',
    duration: '2h 35m',
    rating: 4.7,
    posterUrl: 'https://images.unsplash.com/photo-1536440136628-849c177e76a1?w=400&h=600&fit=crop',
    price: 400,
  },
  {
    id: 4,
    externalShowId: 'SHOW-004',
    name: 'Stree 3',
    venue: 'PVR Phoenix, Mumbai',
    showDateTime: '2026-04-08T21:00:00',
    totalSeats: 180,
    seatsPerRow: 18,
    genre: 'Horror / Comedy',
    language: 'Hindi',
    duration: '2h 20m',
    rating: 4.3,
    posterUrl: 'https://images.unsplash.com/photo-1485846234645-a62644f84728?w=400&h=600&fit=crop',
    price: 280,
  },
  {
    id: 5,
    externalShowId: 'SHOW-005',
    name: 'The Batman II',
    venue: 'INOX R-City, Mumbai',
    showDateTime: '2026-04-10T19:30:00',
    totalSeats: 160,
    seatsPerRow: 16,
    genre: 'Action / Thriller',
    language: 'English',
    duration: '2h 55m',
    rating: 4.6,
    posterUrl: 'https://images.unsplash.com/photo-1509347528160-9a9e33742cdb?w=400&h=600&fit=crop',
    price: 380,
  },
  {
    id: 6,
    externalShowId: 'SHOW-006',
    name: 'KGF Chapter 3',
    venue: 'PVR Nexus Mall, Bangalore',
    showDateTime: '2026-04-12T17:00:00',
    totalSeats: 220,
    seatsPerRow: 22,
    genre: 'Action / Drama',
    language: 'Kannada',
    duration: '3h 05m',
    rating: 4.9,
    posterUrl: 'https://images.unsplash.com/photo-1478720568477-152d9b164e26?w=400&h=600&fit=crop',
    price: 320,
  },
];

export default shows;
