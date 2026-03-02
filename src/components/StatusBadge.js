export default function StatusBadge({ status, warnings }) {
  let className = "badge verified";

  if (status === "FAILED") className = "badge failed";
  if (warnings > 0) className = "badge warning";

  return (
    <div className={className}>
      {warnings > 0
        ? `${warnings} Warning(s)`
        : status}
    </div>
  );
}