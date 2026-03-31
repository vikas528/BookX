import { NavLink } from 'react-router-dom';
import { motion } from 'framer-motion';

export default function Navbar() {
  return (
    <motion.nav
      className="navbar"
      initial={{ y: -60, opacity: 0 }}
      animate={{ y: 0, opacity: 1 }}
      transition={{ duration: 0.5, ease: [0.16, 1, 0.3, 1] }}
    >
      <NavLink to="/" className="navbar__logo">
        <svg width="32" height="32" viewBox="0 0 100 100">
          <defs>
            <linearGradient id="g" x1="0%" y1="0%" x2="100%" y2="100%">
              <stop offset="0%" stopColor="#e50914" />
              <stop offset="100%" stopColor="#ff6b6b" />
            </linearGradient>
          </defs>
          <rect x="10" y="15" width="80" height="55" rx="8" fill="url(#g)" />
          <text
            x="50"
            y="50"
            textAnchor="middle"
            fontFamily="Arial Black"
            fontSize="18"
            fill="white"
            fontWeight="bold"
          >
            BX
          </text>
        </svg>
        Book<span>X</span>Show
      </NavLink>

      <ul className="navbar__links">
        <li>
          <NavLink to="/" end>
            Shows
          </NavLink>
        </li>
        <li>
          <NavLink to="/lookup">My Booking</NavLink>
        </li>
      </ul>
    </motion.nav>
  );
}
