import { useState, useEffect } from "react";
import Sidebar from "./components/Sidebar";
import Overview from "./components/Overview";
import FileGrid from "./components/FileGrid";
import UploadPanel from "./components/UploadPanel";
import RulesTable from "./components/RulesTable";
import BackupPanel from "./components/Backup"; // Import the new Backup component
import "./styles.css";

const API_BASE = "http://localhost:8080";

export default function App() {

  const [activeTab, setActiveTab] = useState("dashboard");
  const [files, setFiles] = useState([]);
  const [stats, setStats] = useState(null);
  const [rules, setRules] = useState([]);

  const [loadingFiles, setLoadingFiles] = useState(false);
  const [loadingStats, setLoadingStats] = useState(false);

  useEffect(() => {
    fetchFiles();
    fetchStats();
    fetchRules();
  }, []);

  useEffect(() => {
    if (activeTab === "rules") {
      fetchRules();
    }
  }, [activeTab]);

  const deleteRule = async (fileName) => {

    if (!window.confirm(`Delete ${fileName}?`)) return;

    try {
      const res = await fetch(`${API_BASE}/rules/${fileName}`, {
        method: "DELETE"
      });

      if (!res.ok) throw new Error(await res.text());

      await fetchFiles();
      await fetchStats();
      await fetchRules();

    } catch (err) {
      alert(err.message);
    }
  };

  const fetchFiles = async () => {
    setLoadingFiles(true);

    try {
      const res = await fetch(`${API_BASE}/rules/files`);
      const data = await res.json();
      setFiles(data);
    } catch (err) {
      console.error("Failed to load files", err);
    } finally {
      setLoadingFiles(false);
    }
  };

  const fetchStats = async () => {
    setLoadingStats(true);

    try {
      const res = await fetch(`${API_BASE}/rules/stats`);
      const data = await res.json();
      setStats(data);
    } catch (err) {
      console.error("Failed to load stats", err);
    } finally {
      setLoadingStats(false);
    }
  };

  const fetchRules = async () => {
    try {
      const res = await fetch(`${API_BASE}/rules/table`);
      const data = await res.json();
      setRules(data);
    } catch (err) {
      console.error("Failed to load rules", err);
    }
  };

  // Helper to completely sync frontend state after a restore or a rule edit
  const handleSystemRefresh = () => {
    fetchFiles();
    fetchStats();
    fetchRules();
  };

  return (
    <div className="app">

      <Sidebar
        activeTab={activeTab}
        setActiveTab={setActiveTab}
      />

      <main className="main">

        {activeTab === "dashboard" && (
          <Overview
            stats={stats}
            loading={loadingStats}
          />
        )}

        {activeTab === "files" && (
          <FileGrid
            files={files}
            loading={loadingFiles}
            deleteRule={deleteRule}
          />
        )}

        {activeTab === "upload" && (
          <UploadPanel
            refreshFiles={fetchFiles}
            refreshStats={fetchStats}
            refreshRules={fetchRules}
          />
        )}

        {activeTab === "rules" && (
          <RulesTable 
            rules={rules} 
            onRuleUpdated={handleSystemRefresh} 
          />
        )}

        {activeTab === "backups" && (
          <BackupPanel 
            onRestoreSuccess={handleSystemRefresh} 
          />
        )}

      </main>

    </div>
  );
}