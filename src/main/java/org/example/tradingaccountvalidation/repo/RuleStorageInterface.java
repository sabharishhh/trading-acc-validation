package org.example.tradingaccountvalidation.repo;

import org.springframework.web.multipart.MultipartFile;

import java.io.File;

public interface RuleStorageInterface {
    void validate(MultipartFile file);
    void save(MultipartFile file);
    File saveTemp(MultipartFile file);
    void moveToRules(File tempFile);
}