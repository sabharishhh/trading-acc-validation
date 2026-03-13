package org.example.tradingaccountvalidation.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RuleMeta {
    private String ruleId;
    private String ruleTableName;
    private String agendaGroup;
    private String sourceFile;
    private final List<ConditionMeta> conditions = new ArrayList<>();
}