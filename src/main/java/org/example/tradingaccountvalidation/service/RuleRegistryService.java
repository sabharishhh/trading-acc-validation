package org.example.tradingaccountvalidation.service;

import org.example.tradingaccountvalidation.model.FileInfo;
import org.example.tradingaccountvalidation.model.RuleMeta;
import org.example.tradingaccountvalidation.repo.RuleRegistryInterface;
import org.springframework.stereotype.Service;
import java.io.File;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class RuleRegistryService implements RuleRegistryInterface {
    private final Map<String, FileInfo> fileInfoMap = new HashMap<>();

    private int totalRules = 0;
    private int totalFiles = 0;
    private Set<String> agendaGroups = new HashSet<>();
    private String lastReloadTime = "";
    private boolean lastBuildSuccess = false;

    @Override
    public synchronized void refresh(String rulesFolderPath, List<RuleMeta> allRules, boolean buildStatus) {
        fileInfoMap.clear();

        this.lastBuildSuccess = buildStatus;
        this.totalRules = allRules.size();

        this.agendaGroups = allRules.stream()
                .map(RuleMeta::getAgendaGroup)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        File folder = new File(rulesFolderPath);
        File[] files = folder.listFiles((dir, name) -> name.endsWith(".xlsx"));

        if (files != null) {
            totalFiles = files.length;

            for (File file : files) {
                String fileName = file.getName();

                List<RuleMeta> rulesForFile = allRules.stream()
                        .filter(r -> fileName.equals(r.getSourceFile()))
                        .toList();

                FileInfo info = new FileInfo();
                info.setFileName(fileName);
                info.setRuleCount(rulesForFile.size());

                info.setAgendaGroups(
                        rulesForFile.stream()
                                .map(RuleMeta::getAgendaGroup)
                                .filter(Objects::nonNull)
                                .distinct()
                                .toList()
                );

                info.setWarnings(0);

                info.setStatus(buildStatus ? "VERIFIED" : "FAILED");
                info.setLastModified(LocalDateTime.now().toString());

                fileInfoMap.put(fileName, info);
            }
        } else {
            totalFiles = 0;
        }
        lastReloadTime = LocalDateTime.now().toString();
    }

    @Override
    public List<FileInfo> getFiles() {
        return new ArrayList<>(fileInfoMap.values());
    }

    @Override
    public Map<String, Object> getStats() {
        Map<String, Object> stats = new HashMap<>();

        stats.put("totalFiles", totalFiles);
        stats.put("totalRules", totalRules);
        stats.put("agendaGroups", agendaGroups.size());
        stats.put("lastReloadTime", lastReloadTime);
        stats.put("lastBuildStatus", lastBuildSuccess ? "SUCCESS" : "FAILED");

        return stats;
    }
}