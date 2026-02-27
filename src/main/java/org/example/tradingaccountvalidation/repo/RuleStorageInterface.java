package org.example.tradingaccountvalidation.repo;

import org.springframework.web.multipart.MultipartFile;

public interface RuleStorageInterface {

    void validate(MultipartFile file);

    void save(MultipartFile file);
}