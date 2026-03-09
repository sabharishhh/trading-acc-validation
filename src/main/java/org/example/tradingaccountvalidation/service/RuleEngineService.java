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
                kfs.write(ResourceFactory.newFileResource(file)
                        .setResourceType(ResourceType.DTABLE));
            }
        }

        KieBuilder builder = ks.newKieBuilder(kfs).buildAll();

        if (builder.getResults().hasMessages(Message.Level.ERROR)) {
            throw new RuntimeException(
                    "Rule validation failed: " + builder.getResults().getMessages()
            );
        }
    }

    private void buildAndReplace(File[] files) {
        KieServices ks = KieServices.Factory.get();
        KieFileSystem kfs = ks.newKieFileSystem();

        if (files != null) {
            for (File file : files) {
                kfs.write(ResourceFactory.newFileResource(file)
                        .setResourceType(ResourceType.DTABLE));
            }
        }

        KieBuilder builder = ks.newKieBuilder(kfs).buildAll();

        if (builder.getResults().hasMessages(Message.Level.ERROR)) {
            lastBuildStatus = "FAILED";
            throw new RuntimeException(
                    "Rule build failed: " + builder.getResults().getMessages()
            );
        }

        KieContainer newContainer =
                ks.newKieContainer(ks.getRepository().getDefaultReleaseId());

        containerRef.set(newContainer);

        lastBuildStatus = "SUCCESS";
        lastReloadTime = LocalDateTime.now().toString();
    }

    @Override
    public KieSession newSession() {
        return containerRef.get().newKieSession();
    }
}