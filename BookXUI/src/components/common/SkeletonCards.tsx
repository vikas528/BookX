interface Props {
  count?: number;
}

export default function SkeletonCards({ count = 6 }: Props) {
  return (
    <div className="shows-grid">
      {Array.from({ length: count }).map((_, i) => (
        <div key={i} className="skeleton skeleton--card" />
      ))}
    </div>
  );
}
