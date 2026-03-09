package org.example.tradingaccountvalidation.service;

import org.example.tradingaccountvalidation.repo.RuleStorageInterface;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.nio.file.*;
import java.util.Objects;

@Service
public class RuleStorageService implements RuleStorageInterface {
    @Value("${rules.folder}")
    private String rulesFolder;

    private static final Logger log = LoggerFactory.getLogger(RuleStorageService.class);

    private static final long MAX_SIZE = 5 * 1024 * 1024;

    @Override
    public void validate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            log.info("File is empty");
            throw new RuntimeException("File is empty");
        }

        if (file.getSize() > MAX_SIZE) {
            log.info("File > 5 MB");
            throw new RuntimeException("File exceeds 5 MB");
        }

        String name = file.getOriginalFilename();

        if (name == null || !name.toLowerCase().endsWith(".xlsx")) {
            log.info("Invalid file type, .xlsx files are only allowed");
            throw new RuntimeException("Only .xlsx files allowed");
        }
    }

    @Override
    public void save(MultipartFile file) {
        try {
            Path folder = Paths.get(rulesFolder);

            if (!Files.exists(folder)) {
                Files.createDirectories(folder);
            }

            Path target = folder.resolve(Objects.requireNonNull(file.getOriginalFilename()));
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException("File save failed", e);
        }
    }
}