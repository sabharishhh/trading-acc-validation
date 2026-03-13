# Trading Account Validation Engine

![Java](https://img.shields.io/badge/Java-21-orange?style=for-the-badge&logo=java)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.2.1-brightgreen?style=for-the-badge&logo=spring)
![Drools](https://img.shields.io/badge/Drools-8.44-blue?style=for-the-badge)
![React](https://img.shields.io/badge/React-18-61DAFB?style=for-the-badge&logo=react)

## 📌 Project Overview
A dynamic, logic-driven evaluation engine designed to process complex state transitions for accounts. At its core, it utilizes a highly optimized **Drools Decision Table** implementation governed by custom Java parsers.

Unlike standard rule engines that silently fail when conditions are not met, this system features a custom-built **Diagnostic Engine** that parses raw Excel metadata to pinpoint the exact logical mismatch. Coupled with a dedicated React frontend, it provides a seamless interface for researchers and administrators to manage, edit, and safely snapshot evaluation rules in real-time.

## ✨ Key Features

### 🧠 Core Engine & Diagnostics
* **Agenda-Group Routing:** Utilizes Drools' native `AGENDA-GROUP` architecture to isolate execution paths, significantly reducing memory footprint and processing time.
* **Intelligent Diagnostics:** When a payload fails to trigger any rule, the custom `RuleDiagnosisService` maps the payload against the parsed Excel metadata to return granular, human-readable reasons for the failure (e.g., `"Expected N but found Y"`).
* **Dynamic Payload Processing:** Evaluates complex JSON account snapshots against multi-variable constraints without requiring hardcoded Java logic.

### 💻 Web-Based Rule Management (React)
* **Live Excel Editing:** A custom spreadsheet UI that allows users to edit rule conditions directly from the browser. Changes are serialized and written directly back to the physical `.xlsx` files on the server.
* **Automated Snapshot & Restore:** A robust timeline system that automatically backs up the entire ruleset before any file upload, deletion, or UI edit. Includes a 1-click restore mechanism to safely rollback breaking changes.
* **Real-time KPI Dashboard:** Tracks engine status, active agenda groups, total rule counts, and system reload timestamps.

---

## 🏗️ Architecture & Tech Stack

### Backend
* **Java 21 & Spring Boot 3.2.1:** Core application framework and REST API provider.
* **Drools 8.44.0.Final:** Business logic integration platform for executing decision tables.
* **Apache POI:** Custom metadata extraction and live modification of `.xlsx` files.

### Frontend
* **React.js:** Component-driven user interface.
* **Framer Motion:** Smooth, hardware-accelerated UI animations.
* **Lucide React:** Clean, consistent iconography.
* **Pure CSS:** Custom, lightweight styling inspired by spreadsheet interfaces.

---

## 🚀 Getting Started

### Prerequisites
* **JDK 21** or higher
* **Maven 3.8+**
* **Node.js 18+** & **npm**

### Backend Setup (Spring Boot)
1. Navigate to the backend directory:
   ```bash
   cd tradingAccountValidation