package org.example.tradingaccountvalidation.controller;

import org.example.tradingaccountvalidation.repo.RuleEngineInterface;
import org.example.tradingaccountvalidation.repo.RuleMetadataLoaderInterface;
import org.example.tradingaccountvalidation.repo.RuleStorageInterface;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.*;
import java.util.Comparator;
import java.util.Objects;

@RestController
@RequestMapping("/rules")
public class RuleUploadController {
    private final RuleStorageInterface storage;
    private final RuleEngineInterface engine;
    private final RuleMetadataLoaderInterface metadataLoader;

    @Value("${rules.folder}")
    private String rulesFolder;

    @Value("${rules.temp.folder")
    private String rulesTempFolder;

    public RuleUploadController(RuleStorageInterface storage, RuleEngineInterface engine, RuleMetadataLoaderInterface metadataLoader) {
        this.storage = storage;
        this.engine = engine;
        this.metadataLoader = metadataLoader;
    }

    @PostMapping("/upload")
    public ResponseEntity<String> upload(@RequestParam("files") MultipartFile[] files) {
        if (files == null || files.length == 0) {
            return ResponseEntity.badRequest().body("No files provided");
        }

        Path tempDir = Paths.get(rulesTempFolder);

        try {
            if (Files.exists(tempDir)) {
                Files.walk(tempDir)
                        .sorted(Comparator.reverseOrder())
                        .map(Path::toFile)
                        .forEach(File::delete);
            }

            Files.createDirectories(tempDir);

            for (MultipartFile file : files) {
                storage.validate(file);

                String fileName = Objects.requireNonNull(file.getOriginalFilename(), "Filename cannot be null");
                Path tempTarget = tempDir.resolve(fileName);

                Files.copy(
                        file.getInputStream(),
                        tempTarget,
                        StandardCopyOption.REPLACE_EXISTING
                );
            }

            File[] tempFiles = tempDir.toFile().listFiles((d, n) -> n.endsWith(".xlsx"));

            if (tempFiles == null || tempFiles.length == 0) {
                return ResponseEntity.badRequest().body("No valid .xlsx files found");
            }

            engine.validateRuleFiles(tempFiles);

            Path rulesDir = Paths.get(rulesFolder);

            if (!Files.exists(rulesDir)) {
                Files.createDirectories(rulesDir);
            }

            for (File file : tempFiles) {
                Files.move(file.toPath(), rulesDir.resolve(file.getName()), StandardCopyOption.REPLACE_EXISTING);
            }

            engine.reloadRules();
            metadataLoader.reload();

            Files.walk(tempDir)
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);

            return ResponseEntity.ok(files.length + " file(s) uploaded safely and rules reloaded"
            );
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Upload failed: " + e.getMessage());
        }
    }
}