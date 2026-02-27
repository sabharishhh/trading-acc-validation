package org.example.tradingaccountvalidation.service;

import org.example.tradingaccountvalidation.repo.RuleStorageInterface;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.Objects;

@Service
public class RuleStorageService implements RuleStorageInterface {
    @Value("${rules.folder}")
    private String rulesFolder;

    private static final long MAX_SIZE = 5 * 1024 * 1024;

    @Override
    public void validate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            log.info()
            throw new RuntimeException("File is empty");
        }

        if (file.getSize() > MAX_SIZE) {
            throw new RuntimeException("File exceeds 5 MB");
        }

        String name = file.getOriginalFilename();

        if (name == null || !name.toLowerCase().endsWith(".xlsx")) {
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