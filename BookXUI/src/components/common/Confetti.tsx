import { useMemo } from 'react';

const COLORS = ['#e50914', '#ff6b6b', '#ffb800', '#2ecc71', '#42a5f5', '#ab47bc', '#ff7043'];

interface Piece {
  id: number;
  left: string;
  color: string;
  delay: string;
  size: number;
  shape: 'square' | 'circle';
}

export default function Confetti() {
  const pieces: Piece[] = useMemo(
    () =>
      Array.from({ length: 60 }).map((_, i) => ({
        id: i,
        left: `${Math.random() * 100}%`,
        color: COLORS[Math.floor(Math.random() * COLORS.length)],
        delay: `${Math.random() * 2}s`,
        size: 6 + Math.random() * 8,
        shape: Math.random() > 0.5 ? 'square' : 'circle',
      })),
    [],
  );

  return (
    <div className="confetti-container">
      {pieces.map((p) => (
        <div
          key={p.id}
          className="confetti-piece"
          style={{
            left: p.left,
            width: p.size,
            height: p.size,
            backgroundColor: p.color,
            borderRadius: p.shape === 'circle' ? '50%' : '2px',
            animationDelay: p.delay,
          }}
        />
      ))}
    </div>
  );
}
