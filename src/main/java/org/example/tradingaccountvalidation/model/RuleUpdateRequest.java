package org.example.tradingaccountvalidation.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RuleUpdateRequest {
    private String fileName;
    private Map<String, Map<String, String>> updates;
}