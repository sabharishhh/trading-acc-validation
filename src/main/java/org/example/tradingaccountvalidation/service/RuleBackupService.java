package org.example.tradingaccountvalidation.service;

import lombok.RequiredArgsConstructor;
import org.example.tradingaccountvalidation.model.BackupSnapshot;
import org.example.tradingaccountvalidation.repo.RuleBackupInterface;
import org.example.tradingaccountvalidation.repo.RuleEngineInterface;
import org.example.tradingaccountvalidation.repo.RuleMetadataLoaderInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class RuleBackupService implements RuleBackupInterface {

    private static final Logger log = LoggerFactory.getLogger(RuleBackupService.class);

    @Value("${rules.folder}")
    private String rulesFolder;

    // Default to a folder named "rules_backup" next to your project
    @Value("${backup.folder:./rules_backup}")
    private String backupFolder;

    private final RuleEngineInterface ruleEngine;
    private final RuleMetadataLoaderInterface metadataLoader;

    @Override
    public void createBackup(String actionTrigger) {
        try {
            File srcDir = new File(rulesFolder);
            if (!srcDir.exists() || !srcDir.isDirectory()) return;

            File[] files = srcDir.listFiles((dir, name) -> name.endsWith(".xlsx") && !name.startsWith("~$"));
            if (files == null || files.length == 0) return;

            // Format: 20260313_094530_UI_UPDATE
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String backupFolderName = timestamp + "_" + actionTrigger;

            Path backupPath = Paths.get(backupFolder, backupFolderName);
            Files.createDirectories(backupPath);

            for (File file : files) {
                Path target = backupPath.resolve(file.getName());
                Files.copy(file.toPath(), target, StandardCopyOption.REPLACE_EXISTING);
            }

            log.info("Created rule backup snapshot: {}", backupFolderName);

        } catch (Exception e) {
            log.error("Failed to create backup: {}", e.getMessage(), e);
        }
    }

    @Override
    public List<BackupSnapshot> listBackups() {
        List<BackupSnapshot> snapshots = new ArrayList<>();
        File backupDir = new File(backupFolder);

        if (!backupDir.exists() || !backupDir.isDirectory()) return snapshots;

        File[] folders = backupDir.listFiles(File::isDirectory);
        if (folders == null) return snapshots;

        // Sort newest first
        Arrays.sort(folders, Comparator.comparing(File::getName).reversed());

        for (File folder : folders) {
            String name = folder.getName();
            String[] parts = name.split("_", 3); // Expected: [yyyyMMdd, HHmmss, Action]

            String timestamp = parts.length >= 2 ? parts[0] + " " + parts[1].replaceAll("..(?!$)", "$0:") : name;
            String action = parts.length >= 3 ? parts[2] : "MANUAL_BACKUP";

            int fileCount = 0;
            File[] backedUpFiles = folder.listFiles((d, n) -> n.endsWith(".xlsx"));
            if (backedUpFiles != null) fileCount = backedUpFiles.length;

            snapshots.add(new BackupSnapshot(name, timestamp, action, fileCount));
        }

        return snapshots;
    }

    @Override
    public void restoreBackup(String folderName) {
        try {
            Path targetBackupPath = Paths.get(backupFolder, folderName);
            if (!Files.exists(targetBackupPath)) {
                throw new RuntimeException("Backup snapshot not found: " + folderName);
            }

            // 1. Clear current active rules
            File activeDir = new File(rulesFolder);
            File[] activeFiles = activeDir.listFiles((dir, name) -> name.endsWith(".xlsx"));
            if (activeFiles != null) {
                for (File f : activeFiles) f.delete();
            }

            // 2. Copy backup files to active rules folder
            try (Stream<Path> stream = Files.list(targetBackupPath)) {
                stream.filter(path -> path.toString().endsWith(".xlsx"))
                        .forEach(src -> {
                            try {
                                Path dest = Paths.get(rulesFolder, src.getFileName().toString());
                                Files.copy(src, dest, StandardCopyOption.REPLACE_EXISTING);
                            } catch (Exception e) {
                                throw new RuntimeException("Failed to copy file during restore", e);
                            }
                        });
            }

            // 3. Trigger full system reload
            ruleEngine.reloadRules();
            metadataLoader.reload();

            log.info("Successfully restored rules from snapshot: {}", folderName);

        } catch (Exception e) {
            log.error("Restore failed: {}", e.getMessage(), e);
            throw new RuntimeException("Restore failed", e);
        }
    }

    @Override
    public void deleteBackup(String folderName) {
        try {
            Path targetBackupPath = Paths.get(backupFolder, folderName);
            if (!Files.exists(targetBackupPath)) {
                throw new RuntimeException("Backup snapshot not found: " + folderName);
            }

            // Recursively delete the directory and its contents
            try (Stream<Path> stream = Files.walk(targetBackupPath)) {
                stream.sorted(Comparator.reverseOrder())
                        .map(Path::toFile)
                        .forEach(File::delete);
            }

            log.info("Successfully deleted backup snapshot: {}", folderName);

        } catch (Exception e) {
            log.error("Failed to delete backup: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to delete backup", e);
        }
    }
}