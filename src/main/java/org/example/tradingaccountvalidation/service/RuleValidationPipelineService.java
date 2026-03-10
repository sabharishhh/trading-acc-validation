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

        /* 1 file validation */
        for (MultipartFile file : files) {
            storage.validate(file);
        }

        /* 2 drools compilation validation */
        engine.validateRuleFiles(tempFiles);

        /* 3 load existing metadata */
        metadataLoader.reload();
        List<RuleMeta> existingRules = metadataLoader.getAllRules();

        /* 4 load metadata from uploaded files */
        List<RuleMeta> newRules = metadataLoader.loadFromFiles(tempFiles);

        /* 5 combine rules */
        List<RuleMeta> combined = new ArrayList<>();
        combined.addAll(existingRules);
        combined.addAll(newRules);

        /* 6 detect uploaded filenames */
        Set<String> uploadingFiles = new HashSet<>();

        for (File file : tempFiles) {
            uploadingFiles.add(file.getName());
        }

        /* 7 validate duplicates */
        validator.validateDuplicateRuleTables(combined, uploadingFiles);
    }
}
