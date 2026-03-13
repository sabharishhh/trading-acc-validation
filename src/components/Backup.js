import React, { useState, useEffect } from "react";
import { History, RotateCcw, ShieldCheck, DatabaseBackup, Trash2 } from "lucide-react";

export default function BackupPanel({ onRestoreSuccess }) {
  const [backups, setBackups] = useState([]);
  const [loading, setLoading] = useState(true);
  const [restoring, setRestoring] = useState(null);

  const fetchBackups = async () => {
    setLoading(true);
    try {
      const res = await fetch("http://localhost:8080/api/backups");
      const data = await res.json();
      setBackups(data);
    } catch (error) {
      console.error("Failed to fetch backups:", error);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchBackups();
  }, []);

  const handleRestore = async (folderName) => {
    if (!window.confirm(`Are you sure you want to restore from ${folderName}? Current rules will be overwritten.`)) {
      return;
    }

    setRestoring(folderName);
    try {
      const res = await fetch(`http://localhost:8080/api/backups/restore/${folderName}`, {
        method: "POST"
      });

      if (!res.ok) throw new Error("Restore failed");

      alert("System restored successfully! The engine has been reloaded.");
      if (onRestoreSuccess) onRestoreSuccess();
      fetchBackups(); 
    } catch (error) {
      console.error(error);
      alert("Error restoring backup.");
    } finally {
      setRestoring(null);
    }
  };

  const handleDelete = async (folderName) => {
    if (!window.confirm(`Delete snapshot ${folderName}? This cannot be undone.`)) {
      return;
    }

    try {
      const res = await fetch(`http://localhost:8080/api/backups/delete/${folderName}`, {
        method: "DELETE"
      });

      if (!res.ok) throw new Error("Failed to delete backup");
      
      // Refresh the timeline
      fetchBackups();
    } catch (error) {
      console.error(error);
      alert("Error deleting backup.");
    }
  };

  const handleManualBackup = async () => {
    try {
      await fetch("http://localhost:8080/api/backups/create?action=MANUAL_SNAPSHOT", { method: "POST" });
      fetchBackups();
    } catch (err) {
      console.error(err);
    }
  };

  const getBadgeClass = (action) => {
    if (action.includes("DELETE")) return "status-badge badge-danger";
    if (action.includes("UPLOAD")) return "status-badge badge-success";
    if (action.includes("UPDATE")) return "status-badge badge-warning";
    return "status-badge badge-info"; 
  };

  if (loading) return <div className="loading-state">Loading timeline...</div>;

  return (
    <div className="panel-container">
      <div className="panel-header">
        <div className="panel-title-group">
          <h2 className="panel-title">System Timeline</h2>
          <p className="panel-subtitle">Manage and instantly restore previous versions of your rule engine.</p>
        </div>
        <button className="primary-btn" onClick={handleManualBackup}>
          <DatabaseBackup size={16} />
          Create Snapshot
        </button>
      </div>

      {backups.length === 0 ? (
        <div className="empty-state">
          <ShieldCheck size={48} className="empty-icon" />
          <h3>No Backups Yet</h3>
          <p>Your timeline is clear. Make a change to a rule or upload a file to trigger an auto-backup.</p>
        </div>
      ) : (
        <div className="modern-table-wrapper">
          <table className="modern-table">
            <thead>
              <tr>
                <th>Snapshot ID (Time)</th>
                <th>Trigger Event</th>
                <th>Rule Files</th>
                <th style={{ textAlign: "right" }}>Action</th>
              </tr>
            </thead>
            <tbody>
              {backups.map((bkp) => (
                <tr key={bkp.folderName}>
                  <td className="fw-500 text-dark">{bkp.timestamp}</td>
                  <td>
                    <span className={getBadgeClass(bkp.action)}>
                      {bkp.action.replace(/_/g, " ")}
                    </span>
                  </td>
                  <td className="text-muted">{bkp.fileCount} active files</td>
                  
                  {/* Updated Action Column with Flexbox */}
                  <td style={{ display: "flex", justifyContent: "flex-end", gap: "12px", alignItems: "center" }}>
                    <button
                      className="danger-btn-outline"
                      onClick={() => handleRestore(bkp.folderName)}
                      disabled={restoring !== null}
                    >
                      <RotateCcw size={14} className={restoring === bkp.folderName ? "spin-icon" : ""} />
                      {restoring === bkp.folderName ? "Restoring..." : "Restore"}
                    </button>
                    
                    <button 
                      className="icon-only-btn text-muted" 
                      onClick={() => handleDelete(bkp.folderName)}
                      title="Delete Snapshot"
                    >
                      <Trash2 size={18} />
                    </button>
                  </td>

                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}