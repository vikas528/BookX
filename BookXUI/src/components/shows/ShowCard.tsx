import { useNavigate } from 'react-router-dom';
import { motion } from 'framer-motion';
import type { Show } from '../../types';

interface Props {
  show: Show;
  index: number;
}

export default function ShowCard({ show, index }: Props) {
  const navigate = useNavigate();

  const dateStr = new Date(show.showDateTime).toLocaleDateString('en-IN', {
    weekday: 'short',
    day: 'numeric',
    month: 'short',
    hour: '2-digit',
    minute: '2-digit',
  });

  return (
    <motion.div
      className="show-card"
      onClick={() => navigate(`/show/${show.id}`, { state: { show } })}
      initial={{ opacity: 0, y: 30 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ delay: index * 0.08, duration: 0.45, ease: [0.16, 1, 0.3, 1] }}
      whileHover={{ scale: 1.03 }}
      whileTap={{ scale: 0.98 }}
    >
      <img
        className="show-card__poster"
        src={show.posterUrl}
        alt={show.name}
        loading="lazy"
      />
      <div className="show-card__body">
        <h3 className="show-card__title">{show.name}</h3>
        <div className="show-card__meta">
          <span className="show-card__tag">{show.genre}</span>
          <span>{show.language}</span>
          <span>•</span>
          <span>{show.duration}</span>
        </div>
        <p className="show-card__meta">{show.venue}</p>
        <p className="show-card__meta">{dateStr}</p>
        <div className="show-card__footer">
          <span className="show-card__price">₹{show.price}</span>
          <span className="show-card__rating">
            ⭐ {show.rating}
          </span>
        </div>
      </div>
    </motion.div>
  );
}
