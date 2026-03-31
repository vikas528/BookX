export default function SkeletonSeats() {
  return (
    <div className="seat-grid">
      {Array.from({ length: 40 }).map((_, i) => (
        <div key={i} className="skeleton skeleton--seat" />
      ))}
    </div>
  );
}
