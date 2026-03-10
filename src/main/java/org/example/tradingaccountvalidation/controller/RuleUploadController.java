package org.example.tradingaccountvalidation.controller;

import org.example.tradingaccountvalidation.repo.RuleValidationPipelineInterface;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.*;
import java.util.Comparator;
import java.util.Objects;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/rules")
public class RuleUploadController {
    private final RuleValidationPipelineInterface validationPipeline;

    @Value("${rules.folder}")
    private String rulesFolder;

    @Value("${rules.temp.folder}")
    private String rulesTempFolder;

    public RuleUploadController(RuleValidationPipelineInterface validationPipeline) {
        this.validationPipeline = validationPipeline;
    }

    @PostMapping("/upload")
    public synchronized ResponseEntity<String> upload(@RequestParam("files") MultipartFile[] files) {
        if (files == null || files.length == 0) {
            return ResponseEntity.badRequest().body("No files provided");
        }

        Path tempDir = Paths.get(rulesTempFolder);

        try {
            if (Files.exists(tempDir)) {
                try (var paths = Files.walk(tempDir)) {
                    paths.sorted(Comparator.reverseOrder())
                            .map(Path::toFile)
                            .forEach(File::delete);
                }
            }

            Files.createDirectories(tempDir);

            for (MultipartFile file : files) {
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

            validationPipeline.validate(files, tempFiles);

            Path rulesDir = Paths.get(rulesFolder);

            if (!Files.exists(rulesDir)) {
                Files.createDirectories(rulesDir);
            }

            for (File file : tempFiles) {
                Files.move(file.toPath(), rulesDir.resolve(file.getName()), StandardCopyOption.REPLACE_EXISTING);
            }

            try (var paths = Files.walk(tempDir)) {
                paths.sorted(Comparator.reverseOrder())
                        .map(Path::toFile)
                        .forEach(File::delete);
            }
            return ResponseEntity.ok(files.length + " rule file(s) validated and deployed successfully");
        } catch (Exception e) {

            String errorMessage = e.getMessage();

            if (e.getCause() != null && e.getCause().getMessage() != null) {
                errorMessage = e.getCause().getMessage();
            }

            try {
                if (Files.exists(tempDir)) {
                    try (var paths = Files.walk(tempDir)) {
                        paths.sorted(Comparator.reverseOrder())
                                .map(Path::toFile)
                                .forEach(File::delete);
                    }
                }
            } catch (Exception ignore) {}

            return ResponseEntity
                    .badRequest()
                    .body("File upload failed:\n" + errorMessage);
        }
    }
}