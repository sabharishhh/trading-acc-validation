package org.example.tradingaccountvalidation.config;

import org.kie.api.KieServices;
import org.kie.api.builder.*;
import org.kie.api.io.ResourceType;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.kie.internal.io.ResourceFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.io.IOException;

@Configuration
public class DroolsConfig {

    private static final Logger log = LoggerFactory.getLogger(DroolsConfig.class);

    private final KieServices kieServices = KieServices.Factory.get();

    @Value("${rules.folder}")
    private String rulesFolderPath;

    private volatile KieContainer kieContainer;

    @PostConstruct
    public void init() throws IOException {
        reload();
    }

    public synchronized void reload() throws IOException {
        KieFileSystem kieFileSystem = kieServices.newKieFileSystem();

        File folder = new File(rulesFolderPath);
        File[] files = folder.listFiles((dir, name) -> name.endsWith(".xlsx"));

        if (files != null) {
            for (File file : files) {
                kieFileSystem.write(
                        ResourceFactory.newFileResource(file).setResourceType(ResourceType.DTABLE)
                );
            }
        }

        log.info("External rule resources loaded successfully.");

        getKieRepository();

        KieBuilder kb = kieServices.newKieBuilder(kieFileSystem);

        kb.buildAll();

        if (kb.getResults().hasMessages(
                org.kie.api.builder.Message.Level.ERROR)) {
            throw new RuntimeException("Build Errors:\n" + kb.getResults()
            );
        }

        KieModule kieModule = kb.getKieModule();

        this.kieContainer = kieServices.newKieContainer(kieModule.getReleaseId());
    }

    @Bean
    public KieContainer getKieContainer() {
        return this.kieContainer;
    }

    @Bean
    public KieSession getKieSession() {
        return this.kieContainer.newKieSession();
    }

    private void getKieRepository() {
        final KieRepository kieRepository = kieServices.getRepository();

        kieRepository.addKieModule(new KieModule() {
            public ReleaseId getReleaseId() {
                return kieRepository.getDefaultReleaseId();
            }
        });
    }
}