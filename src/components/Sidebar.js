import { LayoutDashboard, FileSpreadsheet, Upload } from "lucide-react";

export default function Sidebar({ activeTab, setActiveTab }) {
  return (
    <div className="sidebar">
      <h2 className="logo">Rule Console</h2>

      <NavItem
        label="Dashboard"
        icon={<LayoutDashboard size={18} />}
        active={activeTab === "dashboard"}
        onClick={() => setActiveTab("dashboard")}
      />

      <NavItem
        label="Rule Files"
        icon={<FileSpreadsheet size={18} />}
        active={activeTab === "files"}
        onClick={() => setActiveTab("files")}
      />

      <NavItem
        label="Upload"
        icon={<Upload size={18} />}
        active={activeTab === "upload"}
        onClick={() => setActiveTab("upload")}
      />
    </div>
  );
}

function NavItem({ label, icon, active, onClick }) {
  return (
    <div
      className={`nav-item ${active ? "active" : ""}`}
      onClick={onClick}
    >
      {icon}
      {label}
    </div>
  );
}