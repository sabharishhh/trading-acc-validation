package org.example.tradingaccountvalidation.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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

}