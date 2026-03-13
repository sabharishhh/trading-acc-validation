package org.example.tradingaccountvalidation.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class AuditService {
    private final List<Map<String, String>> auditLogs = new CopyOnWriteArrayList<>();
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm:ss");
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final String AUDIT_FILE_PATH = "audit_logs.json";

    @PostConstruct
    public void init() {
        File file = new File(AUDIT_FILE_PATH);
        if (file.exists()) {
            try {
                List<Map<String, String>> savedLogs = objectMapper.readValue(file, new TypeReference<>() {});
                auditLogs.addAll(savedLogs);
            } catch (IOException e) {
                System.err.println("Failed to load audit logs: " + e.getMessage());
            }
        }
    }

    public void logEdit(String fileName, String ruleId, String column, String oldValue, String newValue) {
        Map<String, String> logEntry = new ConcurrentHashMap<>();
        logEntry.put("id", String.valueOf(System.currentTimeMillis()));
        logEntry.put("timestamp", LocalDateTime.now().format(formatter));
        logEntry.put("fileName", fileName);
        logEntry.put("ruleId", ruleId);
        logEntry.put("column", column);
        logEntry.put("oldValue", oldValue != null ? oldValue : "-");
        logEntry.put("newValue", newValue != null ? newValue : "-");

        auditLogs.addFirst(logEntry);

        saveToFile();
    }

    public List<Map<String, String>> getAllLogs() {
        return auditLogs;
    }

    private void saveToFile() {
        try {
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(new File(AUDIT_FILE_PATH), auditLogs);
        } catch (IOException e) {
            System.err.println("Failed to save audit logs to file: " + e.getMessage());
        }
    }
}