import React, { useState, useEffect } from "react";
import { ClipboardList, ArrowRight, FileSignature } from "lucide-react";

export default function AuditPanel() {
  const [logs, setLogs] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchLogs();
  }, []);

  const fetchLogs = async () => {
    try {
      const res = await fetch("http://localhost:8080/api/rules/audit");
      const data = await res.json();
      setLogs(data);
    } catch (err) {
      console.error("Failed to fetch audit logs", err);
    } finally {
      setLoading(false);
    }
  };

  if (loading) {
    return <div className="loading-state">Loading audit trail...</div>;
  }

  return (
    <div className="panel-container">
      <div className="panel-header">
        <div className="panel-title-group">
          <h2 className="panel-title">Rule Audit Service</h2>
        </div>
        <div className="status-badge badge-info">
          <ClipboardList size={14} style={{ marginRight: "6px" }} />
          {logs.length} Records
        </div>
      </div>

      {logs.length === 0 ? (
        <div className="empty-state">
          <FileSignature size={48} className="empty-icon" />
          <h3>No Edits Recorded</h3>
          <p>Any changes made to the rules via the Active Rules console will appear here.</p>
        </div>
      ) : (
        <div className="modern-table-wrapper">
          <table className="modern-table">
            <thead>
              <tr>
                <th>Timestamp</th>
                <th>Target File</th>
                <th>Rule ID</th>
                <th>Modified Column</th>
                <th>Change Log</th>
              </tr>
            </thead>
            <tbody>
              {logs.map((log) => (
                <tr key={log.id}>
                  <td className="text-muted" style={{ fontSize: "13px" }}>{log.timestamp}</td>
                  <td className="fw-500 text-dark">{log.fileName}</td>
                  <td><span className="agenda-tag">{log.ruleId}</span></td>
                  <td className="text-muted">{log.column}</td>
                  <td>
                    <div style={{ display: "flex", alignItems: "center", gap: "8px" }}>
                      <span className="status-badge badge-danger">{log.oldValue}</span>
                      <ArrowRight size={14} className="text-muted" />
                      <span className="status-badge badge-success">{log.newValue}</span>
                    </div>
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