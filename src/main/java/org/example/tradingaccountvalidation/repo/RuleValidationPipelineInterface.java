package org.example.tradingaccountvalidation.repo;

import org.springframework.web.multipart.MultipartFile;

import java.io.File;

public interface RuleValidationPipelineInterface {
    void validate(MultipartFile[] files, File[] tempFiles) throws Exception;
}
