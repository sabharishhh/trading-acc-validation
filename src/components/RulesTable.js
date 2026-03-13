import React, { useState } from "react";
import { Save } from "lucide-react";

export default function RulesTable({ rules, onRuleUpdated }) {
  const [edits, setEdits] = useState({});
  const [saving, setSaving] = useState(false);

  if (!rules || rules.length === 0) {
    return <div>No rules loaded</div>;
  }

  // Group rules by file
  const groupedRules = rules.reduce((acc, rule) => {
    const file = rule.sourceFile || "Unknown File";
    if (!acc[file]) acc[file] = [];
    acc[file].push(rule);
    return acc;
  }, {});

  // Handle cell edits
  const handleEdit = (fileName, ruleId, col, newValue) => {
    setEdits((prev) => ({
      ...prev,
      [fileName]: {
        ...prev[fileName],
        [ruleId]: {
          ...(prev[fileName]?.[ruleId] || {}),
          [col]: newValue,
        },
      },
    }));
  };

  // Send edits to Spring Boot backend
  const handleSave = async (fileName) => {
    const fileEdits = edits[fileName];
    if (!fileEdits) return;

    setSaving(true);
    try {
      const response = await fetch("http://localhost:8080/api/rules/update", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          fileName: fileName,
          updates: fileEdits,
        }),
      });

      if (!response.ok) throw new Error("Failed to save changes");

      // Clear local edits for this file on success
      setEdits((prev) => {
        const newEdits = { ...prev };
        delete newEdits[fileName];
        return newEdits;
      });

      // Optional: trigger a refresh of the rules from the parent component
      if (onRuleUpdated) onRuleUpdated();
      alert("Rules updated and engine reloaded successfully!");
    } catch (error) {
      console.error(error);
      alert("Error saving rules.");
    } finally {
      setSaving(false);
    }
  };

  return (
    <div>
      {Object.entries(groupedRules).map(([fileName, fileRules]) => {
        const conditionColumns = [
          ...new Set(
            fileRules
              .flatMap((rule) => Object.keys(rule.conditions))
              .filter((col) => col !== "statusFrom" && col !== "statusTo")
          ),
        ];

        const fileHasEdits = edits[fileName] && Object.keys(edits[fileName]).length > 0;

        return (
          <div key={fileName} style={{ marginBottom: "40px" }}>
            <div className="table-header-action">
              <h3 style={{ margin: 0 }}>{fileName}</h3>
              {fileHasEdits && (
                <button
                  className="save-btn"
                  onClick={() => handleSave(fileName)}
                  disabled={saving}
                >
                  <Save size={16} />
                  {saving ? "Saving..." : "Save Changes"}
                </button>
              )}
            </div>

            <div className="rules-table-container">
              <table className="rules-table">
                <thead>
                  <tr>
                    <th>Rule</th>
                    <th>Agenda</th>
                    {conditionColumns.map((col) => (
                      <th key={col}>{col}</th>
                    ))}
                  </tr>
                </thead>
                <tbody>
                  {fileRules.map((rule) => (
                    <tr key={rule.ruleId}>
                      <td className="fixed-col">{rule.ruleId}</td>
                      <td className="fixed-col">{rule.agendaGroup}</td>

                      {conditionColumns.map((col) => {
                        const originalValue = rule.conditions[col] || "-";
                        const isEditable = originalValue === "Y" || originalValue === "N";
                        
                        // Get current value (either edited or original)
                        const currentValue =
                          edits[fileName]?.[rule.ruleId]?.[col] !== undefined
                            ? edits[fileName][rule.ruleId][col]
                            : originalValue;

                        return (
                          <td key={col} className={isEditable ? "editable-cell" : ""}>
                            {isEditable ? (
                              <select
                                className="cell-dropdown"
                                value={currentValue}
                                onChange={(e) =>
                                  handleEdit(fileName, rule.ruleId, col, e.target.value)
                                }
                              >
                                <option value="Y">Y</option>
                                <option value="N">N</option>
                              </select>
                            ) : (
                              currentValue
                            )}
                          </td>
                        );
                      })}
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        );
      })}
    </div>
  );
}