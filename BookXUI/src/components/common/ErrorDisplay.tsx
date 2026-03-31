interface Props {
  message: string;
  onRetry?: () => void;
}

export default function ErrorDisplay({ message, onRetry }: Props) {
  return (
    <div className="error-display">
      <div className="error-display__icon">⚠️</div>
      <p className="error-display__message">{message}</p>
      {onRetry && (
        <button className="btn btn--secondary" onClick={onRetry}>
          Try Again
        </button>
      )}
    </div>
  );
}
