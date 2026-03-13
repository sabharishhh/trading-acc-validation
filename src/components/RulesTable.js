import React, { useState } from "react";
import { Save, ChevronDown, ChevronRight, FileSpreadsheet } from "lucide-react";

export default function RulesTable({ rules, loading, onRuleUpdated }) {
  const [edits, setEdits] = useState({});
  const [saving, setSaving] = useState(false);
  const [expandedFiles, setExpandedFiles] = useState({});

  if (loading) {
    return (
      <div className="rules-container">
        <h2 className="section-title">Active Rule Sets</h2>
        {[1, 2, 3].map(i => (
          <div key={i} className="rule-card skeleton-card" style={{ height: '60px', marginBottom: '12px' }}>
            <div className="skeleton-pulse" style={{ width: '100%', height: '100%' }} />
          </div>
        ))}
      </div>
    );
  }

  if (!rules || rules.length === 0) {
    return <div className="empty-state">No rules loaded in the engine.</div>;
  }

  const toggleFile = (fileName) => {
    setExpandedFiles((prev) => ({
      ...prev,
      [fileName]: !prev[fileName],
    }));
  };

  const groupedRules = rules.reduce((acc, rule) => {
    const file = rule.sourceFile || "Unknown File";
    if (!acc[file]) acc[file] = [];
    acc[file].push(rule);
    return acc;
  }, {});

  const handleEdit = (fileName, ruleId, col, newValue) => {
    setEdits((prev) => ({
      ...prev,
      [fileName]: {
        ...prev[fileName],
        [ruleId]: { ...prev[fileName]?.[ruleId], [col]: newValue },
      },
    }));
  };

  const handleSave = async (fileName) => {
    const fileEdits = edits[fileName];
    if (!fileEdits) return;
    setSaving(true);
    try {
      const response = await fetch("http://localhost:8080/api/rules/update", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ fileName, updates: fileEdits }),
      });
      if (!response.ok) throw new Error("Failed to save");
      setEdits((prev) => {
        const newEdits = { ...prev };
        delete newEdits[fileName];
        return newEdits;
      });
      if (onRuleUpdated) onRuleUpdated();
      alert(`Success: ${fileName} updated.`);
    } catch (error) {
      alert("Error saving rules.");
    } finally {
      setSaving(false);
    }
  };

  return (
    <div className="rules-container">
      <h2 className="section-title">Active Rule Sets</h2>
      {Object.entries(groupedRules).map(([fileName, fileRules]) => {
        const isExpanded = expandedFiles[fileName];
        const fileHasEdits = edits[fileName] && Object.keys(edits[fileName]).length > 0;
        
        const conditionColumns = [
          ...new Set(
            fileRules
              .flatMap((rule) => Object.keys(rule.conditions))
              .filter((col) => col !== "statusFrom" && col !== "statusTo")
          ),
        ];

        return (
          <div key={fileName} className={`rule-card ${isExpanded ? "expanded" : ""}`}>
            <div className="rule-card-header" onClick={() => toggleFile(fileName)}>
              <div className="header-left">
                {isExpanded ? <ChevronDown size={20} /> : <ChevronRight size={20} />}
                <FileSpreadsheet size={18} className="file-icon" />
                <span className="filename-text">{fileName}</span>
                <span className="rule-count-badge">{fileRules.length} Rules</span>
              </div>
              
              <div className="header-right" onClick={(e) => e.stopPropagation()}>
                {fileHasEdits && (
                  <button className="save-btn small" onClick={() => handleSave(fileName)} disabled={saving}>
                    <Save size={14} />
                    {saving ? "Saving..." : "Save Changes"}
                  </button>
                )}
              </div>
            </div>

            {isExpanded && (
              <div className="rule-card-content">
                <div className="rules-table-container">
                  <table className="rules-table">
                    <thead>
                      <tr>
                        <th>Rule ID</th>
                        <th>Agenda Group</th>
                        {conditionColumns.map((col) => <th key={col}>{col}</th>)}
                      </tr>
                    </thead>
                    <tbody>
                      {fileRules.map((rule) => (
                        <tr key={rule.ruleId}>
                          <td className="fixed-col">{rule.ruleId}</td>
                          <td>{rule.agendaGroup}</td>
                          {conditionColumns.map((col) => {
                            const originalValue = rule.conditions[col] || "";
                            const currentValue = edits[fileName]?.[rule.ruleId]?.[col] ?? originalValue;

                            return (
                              <td key={col} className="editable-cell">
                                <input 
                                  type="text"
                                  className="cell-input" 
                                  value={currentValue}
                                  placeholder="-"
                                  onChange={(e) => handleEdit(fileName, rule.ruleId, col, e.target.value)}
                                />
                              </td>
                            );
                          })}
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              </div>
            )}
          </div>
        );
      })}
    </div>
  );
}