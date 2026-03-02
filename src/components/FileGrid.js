import { motion } from "framer-motion";
import StatusBadge from "./StatusBadge";

export default function FileGrid({ files, loading }) {
  if (loading) return <p>Loading files...</p>;

  return (
    <motion.div initial={{ opacity: 0 }} animate={{ opacity: 1 }}>
      <h2>Rule Files</h2>

      <div className="grid">
        {files.map((file, i) => (
          <motion.div key={i} className="file-card" whileHover={{ y: -5 }}>
            <h3>{file.fileName}</h3>

            <div className="meta">
              <span>{file.ruleCount} Rules</span>
              <span>{file.agendaGroups?.length || 0} Agendas</span>
            </div>

            <StatusBadge
              status={file.status}
              warnings={file.warnings}
            />
          </motion.div>
        ))}
      </div>
    </motion.div>
  );
}