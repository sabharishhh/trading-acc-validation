import React from "react";

export default function Loading() {
  return (
    <div className="loading-container">
      <div className="shimmer-wrapper">
        <h2 className="section-title">Initialising Dashboard...</h2>
        <div className="kpi-grid">
          {[1, 2, 3, 4].map((i) => (
            <div key={i} className="skeleton-card">
              <div className="skeleton-title"></div>
              <div className="skeleton-value"></div>
            </div>
          ))}
        </div>
        <div className="skeleton-table"></div>
      </div>
    </div>
  );
}