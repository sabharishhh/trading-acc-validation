package org.example.tradingaccountvalidation.service;

import jakarta.annotation.PostConstruct;
import org.example.tradingaccountvalidation.repo.RuleEngineInterface;
import org.example.tradingaccountvalidation.repo.RuleMetadataLoaderInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.file.*;
import static java.nio.file.StandardWatchEventKinds.*;

@Component
public class RuleFolderWatcherService {
    private static final Logger log = LoggerFactory.getLogger(RuleFolderWatcherService.class);

    @Value("${rules.folder}")
    private String rulesFolder;

    private final RuleEngineInterface engine;
    private final RuleMetadataLoaderInterface metadataLoader;
    private final RuleRegistryService registry;

    public RuleFolderWatcherService(RuleEngineInterface engine, RuleMetadataLoaderInterface metadataLoader, RuleRegistryService registry) {
        this.engine = engine;
        this.metadataLoader = metadataLoader;
        this.registry = registry;
    }

    @PostConstruct
    public void startWatcher() {
        try {
            log.info("Loading rules at application startup...");

            engine.reloadRules();
            metadataLoader.reload();
            registry.refresh(
                    rulesFolder,
                    metadataLoader.getAllRules(),
                    true
            );
            log.info("Initial rule load completed.");
        } catch (Exception e) {
            log.error("Initial rule load failed", e);
        }

        Thread watcherThread = new Thread(this::watch);
        watcherThread.setDaemon(true);
        watcherThread.start();

        log.info("Rule folder watcher started for directory: {}", rulesFolder);
    }

    private void watch() {
        try {
            WatchService watchService = FileSystems.getDefault().newWatchService();

            Path path = Paths.get(rulesFolder);
            path.register(
                    watchService,
                    ENTRY_CREATE,
                    ENTRY_MODIFY,
                    ENTRY_DELETE
            );

            while (true) {
                WatchKey key = watchService.take();
                boolean reloadNeeded = false;

                for (WatchEvent<?> event : key.pollEvents()) {
                    WatchEvent.Kind<?> kind = event.kind();
                    Path file = (Path) event.context();

                    if (!file.toString().endsWith(".xlsx")) {
                        continue;
                    }

                    if (kind == ENTRY_CREATE) {
                        log.info("Rule file added: {}", file);
                    } else if (kind == ENTRY_MODIFY) {
                        log.info("Rule file modified: {}", file);
                    } else if (kind == ENTRY_DELETE) {
                        log.info("Rule file deleted: {}", file);
                    }

                    reloadNeeded = true;
                }

                if (reloadNeeded) {
                    log.info("Rule change detected. Reloading rule engine...");

                    engine.reloadRules();
                    metadataLoader.reload();
                    registry.refresh(
                            rulesFolder,
                            metadataLoader.getAllRules(),
                            true
                    );
                    log.info("Rulebase reload completed successfully.");
                }
                key.reset();
            }
        } catch (Exception e) {
            log.error("Rule folder watcher failed", e);
            throw new RuntimeException("Rule watcher failed", e);
        }
    }
}