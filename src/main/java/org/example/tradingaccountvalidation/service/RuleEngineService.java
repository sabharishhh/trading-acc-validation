package org.example.tradingaccountvalidation.service;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import org.example.tradingaccountvalidation.repo.RuleEngineInterface;
import org.kie.api.KieServices;
import org.kie.api.builder.*;
import org.kie.api.io.ResourceType;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.internal.io.ResourceFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class RuleEngineService implements RuleEngineInterface {
    @Value("${rules.folder}")
    private String rulesFolder;

    private final AtomicReference<KieContainer> containerRef = new AtomicReference<>();

    @Getter
    private String lastBuildStatus = "UNKNOWN";

    @Getter
    private String lastReloadTime = "";

    @PostConstruct
    public void init() {
        reloadRules();
    }

    @Override
    public synchronized void reloadRules() {
        File folder = new File(rulesFolder);
        File[] files = folder.listFiles((dir, name) -> name.endsWith(".xlsx"));

        buildAndReplace(files);
    }

    @Override
    public void validateRuleFiles(File[] files) {
        buildOnly(files);
    }

    private void buildOnly(File[] files) {
        KieServices ks = KieServices.Factory.get();
        KieFileSystem kfs = ks.newKieFileSystem();

        if (files != null) {
            for (File file : files) {
                kfs.write(ResourceFactory.newFileResource(file).setResourceType(ResourceType.DTABLE));
            }
        }

        KieBuilder builder = ks.newKieBuilder(kfs).buildAll();
        checkForErrors(builder.getResults());
    }

    private void buildAndReplace(File[] files) {
        KieServices ks = KieServices.Factory.get();
        KieFileSystem kfs = ks.newKieFileSystem();

        if (files != null) {
            for (File file : files) {
                kfs.write(ResourceFactory.newFileResource(file).setResourceType(ResourceType.DTABLE));
            }
        }

        KieBuilder builder = ks.newKieBuilder(kfs).buildAll();
        Results results = builder.getResults();

        if (results.hasMessages(Message.Level.ERROR)) {
            lastBuildStatus = "FAILED";
            throw new RuntimeException(formatErrors(results));
        }

        KieContainer newContainer = ks.newKieContainer(ks.getRepository().getDefaultReleaseId());
        containerRef.set(newContainer);

        lastBuildStatus = "SUCCESS";
        lastReloadTime = LocalDateTime.now().toString();
    }

    private void checkForErrors(Results results) {
        if (results.hasMessages(Message.Level.ERROR)) {
            throw new RuntimeException(formatErrors(results));
        }
    }

    private String formatErrors(Results results) {
        StringBuilder errorMessage = new StringBuilder();
        String currentFile = "";

        for (Message msg : results.getMessages(Message.Level.ERROR)) {
            String file = "Unknown file";

            if (msg.getPath() != null) {
                Path p = Paths.get(msg.getPath());
                file = p.getFileName().toString();
            }

            if (!file.equals(currentFile)) {
                errorMessage.append("\n").append(file).append("\n");
                currentFile = file;
            }

            errorMessage
                    .append("Line ")
                    .append(msg.getLine())
                    .append(" : ")
                    .append(msg.getText())
                    .append("\n");
        }
        return errorMessage.toString();
    }

    @Override
    public KieSession newSession() {
        return containerRef.get().newKieSession();
    }
}