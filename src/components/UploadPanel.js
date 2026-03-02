import { useState } from "react";
import { motion } from "framer-motion";
import { Upload } from "lucide-react";

const API_BASE = "http://localhost:8080";

export default function UploadPanel({ refreshFiles, refreshStats }) {
  const [files, setFiles] = useState([]);
  const [message, setMessage] = useState("");
  const [uploading, setUploading] = useState(false);
  const [dragActive, setDragActive] = useState(false);

  const addFiles = (incoming) => {
    setFiles((prev) => {
      const existing = prev.map((f) => f.name);
      const filtered = incoming.filter(
        (f) => f.name.endsWith(".xlsx") && !existing.includes(f.name)
      );
      return [...prev, ...filtered];
    });
  };

  const handleFileChange = (e) => {
    addFiles(Array.from(e.target.files));
    e.target.value = null;
  };

  const handleDrop = (e) => {
    e.preventDefault();
    setDragActive(false);
    addFiles(Array.from(e.dataTransfer.files));
  };

  const removeFile = (index) => {
    setFiles((prev) => prev.filter((_, i) => i !== index));
  };

  const handleUpload = async () => {
    if (!files.length) return;

    setUploading(true);
    setMessage("");

    const formData = new FormData();
    files.forEach((f) => formData.append("files", f));

    try {
      const res = await fetch(`${API_BASE}/rules/upload`, {
        method: "POST",
        body: formData,
      });

      if (!res.ok) throw new Error(await res.text());

      setMessage("Upload successful");
      setFiles([]);
      await refreshFiles();
      await refreshStats();
    } catch {
      setMessage("Upload failed");
    } finally {
      setUploading(false);
    }
  };

  return (
    <motion.div
      initial={{ opacity: 0, y: 20 }}
      animate={{ opacity: 1, y: 0 }}
    >
      <div className="upload-card">
        <h2 className="section-title">Upload Rule Files</h2>

        <label
          className={`drop-zone ${dragActive ? "drag-active" : ""}`}
          onDragOver={(e) => {
            e.preventDefault();
            setDragActive(true);
          }}
          onDragLeave={() => setDragActive(false)}
          onDrop={handleDrop}
        >
          <input
            type="file"
            multiple
            accept=".xlsx"
            onChange={handleFileChange}
          />

          <div className="drop-inner">
            <Upload size={36} />
            <p className="drop-title">Select Excel Files</p>
            <span className="drop-sub">
              or drag and drop (.xlsx only)
            </span>
          </div>
        </label>

        {files.length > 0 && (
          <div className="selected-files">
            {files.map((file, index) => (
              <motion.div
                key={file.name}
                className="file-chip"
                initial={{ scale: 0.9, opacity: 0 }}
                animate={{ scale: 1, opacity: 1 }}
              >
                {file.name}
                <button onClick={() => removeFile(index)}>×</button>
              </motion.div>
            ))}
          </div>
        )}

        <button
          className="primary-btn"
          onClick={handleUpload}
          disabled={uploading}
        >
          {uploading ? "Uploading..." : "Upload Files"}
        </button>

        {message && (
          <div
            className={`upload-message ${
              message.includes("failed") ? "error" : "success"
            }`}
          >
            {message}
          </div>
        )}
      </div>
    </motion.div>
  );
}