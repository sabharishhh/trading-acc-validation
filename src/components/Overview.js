import { motion } from "framer-motion";

export default function Overview({ stats, loading }) {
  if (loading || !stats) return <p>Loading stats...</p>;

  return (
    <motion.div initial={{ opacity: 0 }} animate={{ opacity: 1 }}>
      <h2>System Overview</h2>

      <div className="kpi-grid">
        <KPI title="Total Files" value={stats.totalFiles} />
        <KPI title="Total Rules" value={stats.totalRules} />
        <KPI title="Agenda Groups" value={stats.agendaGroups} />
        <KPI title="Last Reload" value={formatTime(stats.lastReloadTime)} />
      </div>
    </motion.div>
  );
}

function formatTime(timestamp) {
  if (!timestamp) return "-";

  const date = new Date(timestamp);

  return date.toLocaleString("en-IN", {
    year: "numeric",
    month: "short",
    day: "numeric",
    hour: "2-digit",
    minute: "2-digit",
    second: "2-digit",
  });
}

function KPI({ title, value }) {
  return (
    <motion.div className="kpi-card" whileHover={{ scale: 1.03 }}>
      <div className="kpi-title">{title}</div>
      <div className="kpi-value">{value}</div>
    </motion.div>
  );
}