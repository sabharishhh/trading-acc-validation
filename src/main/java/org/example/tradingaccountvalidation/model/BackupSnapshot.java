package org.example.tradingaccountvalidation.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BackupSnapshot {
    private String folderName;
    private String timestamp;
    private String action;
    private int fileCount;
}