interface Props {
  text?: string;
}

export default function Loader({ text = 'Loading…' }: Props) {
  return (
    <div className="loader-overlay">
      <div className="spinner" />
      <p className="loader-text">{text}</p>
    </div>
  );
}
