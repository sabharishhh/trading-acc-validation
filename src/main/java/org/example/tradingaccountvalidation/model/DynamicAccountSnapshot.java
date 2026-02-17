package org.example.tradingaccountvalidation.model;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.annotation.JsonValue;
import org.example.tradingaccountvalidation.controller.TradingAccountValidationController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DynamicAccountSnapshot {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private ObjectNode root;

    private static final Logger log = LoggerFactory.getLogger(DynamicAccountSnapshot.class);

    public DynamicAccountSnapshot(JsonNode node) {

        if (node == null || !node.isObject()) {
            throw new IllegalArgumentException("Root JSON must be an object");
        }

        if (node.has("raw") && node.get("raw").isObject()) {
            node = node.get("raw");
        }

        this.root = (ObjectNode) node;
    }

    public DynamicAccountSnapshot(String jsonPayload) {
        try {
            JsonNode parsed = MAPPER.readTree(jsonPayload);
            initialize(parsed);
        } catch (Exception e) {
            throw new RuntimeException("Invalid JSON payload", e);
        }
    }

    private void initialize(JsonNode parsed) {
        if (parsed == null || !parsed.isObject()) {
            throw new IllegalArgumentException("Root JSON must be an object");
        }

        if (parsed.has("raw") && parsed.get("raw").isObject()) {
            parsed = parsed.get("raw");
        }

        this.root = (ObjectNode) parsed;
    }


    public Object get(String path) {
        JsonNode node = navigate(path);

        if (node == null || node.isMissingNode() || node.isNull()) {
            return null;
        }

        if (node.isTextual()) return node.asText();
        if (node.isInt()) return node.asInt();
        if (node.isLong()) return node.asLong();
        if (node.isDouble() || node.isFloat()) return node.asDouble();
        if (node.isBoolean()) return node.asBoolean();

        return node;
    }


    public String getString(String path) {
        Object val = get(path);
        return val != null ? val.toString() : null;
    }

    public Double getDouble(String path) {
        Object val = get(path);
        return (val instanceof Number) ? ((Number) val).doubleValue() : null;
    }

    public Long getLong(String path) {
        Object val = get(path);
        return (val instanceof Number) ? ((Number) val).longValue() : null;
    }

    public Integer getInt(String path) {
        Object val = get(path);
        return (val instanceof Number) ? ((Number) val).intValue() : null;
    }

    public Boolean getBoolean(String path) {
        Object val = get(path);
        return (val instanceof Boolean) ? (Boolean) val : null;
    }


    public void set(String path, Object value) {

        String[] parts = clean(path).split("/");
        ObjectNode current = root;

        for (int i = 0; i < parts.length - 1; i++) {

            JsonNode next = current.get(parts[i]);

            if (next == null || !next.isObject()) {
                next = MAPPER.createObjectNode();
                current.set(parts[i], next);
            }

            current = (ObjectNode) next;
        }

        String last = parts[parts.length - 1];

        if (value instanceof String)
            current.put(last, (String) value);
        else if (value instanceof Integer)
            current.put(last, (Integer) value);
        else if (value instanceof Long)
            current.put(last, (Long) value);
        else if (value instanceof Double)
            current.put(last, (Double) value);
        else if (value instanceof Boolean)
            current.put(last, (Boolean) value);
        else
            current.set(last, MAPPER.valueToTree(value));
    }

    private JsonNode navigate(String path) {

        String[] parts = clean(path).split("/");
        JsonNode current = root;
        for (String part : parts) {
            if (part.isEmpty()) continue;
            current = current.path(part);
        }

        return current;
    }

    private String clean(String path) {
        if (path == null) return "";
        return path.startsWith("/") ? path.substring(1) : path;
    }

    public boolean exists(String path) {
        JsonNode node = navigate(path);
        return node != null && !node.isMissingNode() && !node.isNull();
    }

    @JsonValue
    public JsonNode getRoot() {
        return root;
    }

    @Override
    public String toString() {
        return root.toPrettyString();
    }

    public void printStatus(String status) {
        log.info("Account Status: {}", status);
    }
}