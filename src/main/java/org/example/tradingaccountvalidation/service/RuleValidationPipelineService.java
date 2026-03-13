package org.example.tradingaccountvalidation.service;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.example.tradingaccountvalidation.model.RuleMeta;
import org.example.tradingaccountvalidation.repo.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@Data
@AllArgsConstructor
public class RuleValidationPipelineService implements RuleValidationPipelineInterface {
    private final RuleStorageInterface storage;
    private final RuleEngineInterface engine;
    private final RuleValidationInterface validator;
    private final RuleMetadataLoaderInterface metadataLoader;

    @Override
    public void validate(MultipartFile[] files, File[] tempFiles) throws Exception {
        for (MultipartFile file : files) {
            storage.validate(file);
        }

        engine.validateRuleFiles(tempFiles);
        metadataLoader.reload();

        List<RuleMeta> existingRules = metadataLoader.getAllRules();
        List<RuleMeta> newRules = metadataLoader.loadFromFiles(tempFiles);

        List<RuleMeta> combined = new ArrayList<>();
        combined.addAll(existingRules);
        combined.addAll(newRules);

        Set<String> uploadingFiles = new HashSet<>();

        for (File file : tempFiles) {
            uploadingFiles.add(file.getName());
        }

        validator.validateDuplicateRuleTables(combined, uploadingFiles);
    }
}
