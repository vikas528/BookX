import ShowCard from './ShowCard';
import type { Show } from '../../types';

interface Props {
  shows: Show[];
}

export default function ShowList({ shows }: Props) {
  return (
    <div className="shows-grid">
      {shows.map((show, i) => (
        <ShowCard key={show.id} show={show} index={i} />
      ))}
    </div>
  );
}
