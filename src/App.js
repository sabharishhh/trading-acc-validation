import { useState, useEffect } from "react";
import Sidebar from "./components/Sidebar";
import Overview from "./components/Overview";
import FileGrid from "./components/FileGrid";
import UploadPanel from "./components/UploadPanel";
import "./styles.css";

const API_BASE = "http://localhost:8080";

export default function App() {
  const [activeTab, setActiveTab] = useState("dashboard");
  const [files, setFiles] = useState([]);
  const [stats, setStats] = useState(null);
  const [loadingFiles, setLoadingFiles] = useState(false);
  const [loadingStats, setLoadingStats] = useState(false);

  useEffect(() => {
    fetchFiles();
    fetchStats();
  }, []);

  const fetchFiles = async () => {
    setLoadingFiles(true);
    try {
      const res = await fetch(`${API_BASE}/rules/files`);
      const data = await res.json();
      setFiles(data);
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
    } finally {
      setLoadingStats(false);
    }
  };

  return (
    <div className="app">
      <Sidebar activeTab={activeTab} setActiveTab={setActiveTab} />

      <main className="main">
        {activeTab === "dashboard" && (
          <Overview stats={stats} loading={loadingStats} />
        )}

        {activeTab === "files" && (
          <FileGrid files={files} loading={loadingFiles} />
        )}

        {activeTab === "upload" && (
          <UploadPanel refreshFiles={fetchFiles} refreshStats={fetchStats} />
        )}
      </main>
    </div>
  );
}