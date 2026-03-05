package org.example.tradingaccountvalidation.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.kie.api.definition.rule.All;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FileInfo {

    private String fileName;
    private int ruleCount;
    private List<String> agendaGroups;
    private int warnings;
    private String status;
    private String lastModified;

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public int getRuleCount() { return ruleCount; }
    public void setRuleCount(int ruleCount) { this.ruleCount = ruleCount; }

    public List<String> getAgendaGroups() { return agendaGroups; }
    public void setAgendaGroups(List<String> agendaGroups) { this.agendaGroups = agendaGroups; }

    public int getWarnings() { return warnings; }
    public void setWarnings(int warnings) { this.warnings = warnings; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getLastModified() { return lastModified; }
    public void setLastModified(String lastModified) { this.lastModified = lastModified; }
}