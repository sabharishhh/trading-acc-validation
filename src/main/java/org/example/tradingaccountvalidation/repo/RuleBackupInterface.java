package org.example.tradingaccountvalidation.repo;

import org.example.tradingaccountvalidation.model.BackupSnapshot;

import java.util.List;

public interface RuleBackupInterface {
    void createBackup(String actionTrigger);
    List<BackupSnapshot> listBackups();
    void restoreBackup(String folderName);
    void deleteBackup(String folderName);
}
