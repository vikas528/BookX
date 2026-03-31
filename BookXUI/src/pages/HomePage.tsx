import { useState, useMemo } from 'react';
import { motion } from 'framer-motion';
import PageTransition from '../components/common/PageTransition';
import ShowList from '../components/shows/ShowList';
import showsCatalogue from '../data/shows';

export default function HomePage() {
  const [search, setSearch] = useState('');

  const filtered = useMemo(() => {
    if (!search.trim()) return showsCatalogue;
    const q = search.toLowerCase();
    return showsCatalogue.filter(
      (s) =>
        s.name.toLowerCase().includes(q) ||
        s.venue.toLowerCase().includes(q) ||
        s.genre.toLowerCase().includes(q) ||
        s.language.toLowerCase().includes(q),
    );
  }, [search]);

  return (
    <PageTransition>
      {/* Hero */}
      <section className="hero">
        <motion.span
          className="hero__badge"
          initial={{ opacity: 0, y: -10 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ delay: 0.15 }}
        >
          🎬 Live Seat Booking
        </motion.span>

        <motion.h1
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ delay: 0.25, duration: 0.6 }}
        >
          Your Seat Awaits
        </motion.h1>

        <motion.p
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ delay: 0.4, duration: 0.6 }}
        >
          Pick a show, choose your seat, and we'll handle the rest.
          Real-time availability. Instant confirmation.
        </motion.p>

        {/* Search bar */}
        <motion.div
          style={{ marginTop: '2rem', width: '100%', maxWidth: 480 }}
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ delay: 0.55, duration: 0.5 }}
        >
          <input
            type="text"
            placeholder="Search shows, venues, genres…"
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            style={{
              width: '100%',
              padding: '14px 24px',
              background: 'var(--bg-card)',
              border: '1px solid rgba(255,255,255,0.08)',
              borderRadius: 'var(--radius-xl)',
              color: 'var(--text-primary)',
              fontSize: '1rem',
              outline: 'none',
            }}
          />
        </motion.div>
      </section>

      {/* Shows */}
      <section className="shows-section container">
        <h2 className="shows-section__title">
          {search ? `Results for "${search}"` : 'Now Showing'}
        </h2>

        {filtered.length > 0 ? (
          <ShowList shows={filtered} />
        ) : (
          <p style={{ color: 'var(--text-secondary)', textAlign: 'center', padding: '3rem 0' }}>
            No shows match your search. Try a different keyword.
          </p>
        )}
      </section>
    </PageTransition>
  );
}
