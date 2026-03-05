package org.example.tradingaccountvalidation.controller;

import org.example.tradingaccountvalidation.model.RuleMeta;
import org.springframework.beans.factory.annotation.Value;
import org.example.tradingaccountvalidation.model.FileInfo;
import org.example.tradingaccountvalidation.repo.RuleEngineInterface;
import org.example.tradingaccountvalidation.repo.RuleMetadataLoaderInterface;
import org.example.tradingaccountvalidation.service.RuleRegistryService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/rules")
@CrossOrigin
public class RuleDashboardController {

    private final RuleRegistryService registry;
    private final RuleEngineInterface engine;
    private final RuleMetadataLoaderInterface metadataLoader;

    @Value("${rules.folder}")
    private String rulesFolder;

    public RuleDashboardController(RuleEngineInterface engine, RuleMetadataLoaderInterface metadataLoader, RuleRegistryService registry) {
        this.registry = registry;
        this.engine = engine;
        this.metadataLoader = metadataLoader;
    }

    @GetMapping("/files")
    public List<FileInfo> getFiles() throws Exception {
        engine.reloadRules();
        metadataLoader.reload();
        registry.refresh(rulesFolder,
                metadataLoader.getAllRules(),
                true);

        return registry.getFiles();
    }

    @GetMapping("/stats")
    public Map<String, Object> getStats() {
        return registry.getStats();
    }

    @GetMapping("/rules")
    public List<RuleMeta> getAllRules() {
        return metadataLoader.getAllRules();
    }

    @GetMapping("/active")
    public List<RuleMeta> getActiveRules() {
        return metadataLoader.getAllRules();
    }
}