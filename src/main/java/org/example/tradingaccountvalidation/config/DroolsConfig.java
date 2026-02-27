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

    private KieFileSystem getKieFileSystem() throws IOException {
        KieFileSystem kieFileSystem = kieServices.newKieFileSystem();

        File folder = new File(rulesFolderPath);
        File[] files = folder.listFiles((dir, name) -> name.endsWith(".xlsx"));

        if (files != null) {
            for (File file : files) {
                kieFileSystem.write(
                        ResourceFactory.newFileResource(file)
                                .setResourceType(ResourceType.DTABLE)
                );
            }
        }

        log.info("External rule resources loaded successfully.");

        return kieFileSystem;
    }

//    private KieFileSystem getKieFileSystem() throws IOException {
//        KieFileSystem kieFileSystem = kieServices.newKieFileSystem();
//        kieFileSystem.write(ResourceFactory.newClassPathResource("rules/rules.drl").setResourceType(ResourceType.DRL));
//        log.info("Class path resource loaded successfully.");
//        return kieFileSystem;
//    }

    @Bean
    public KieContainer getKieContainer() throws IOException {
        log.info("Container created");
        getKieRepository();
        KieBuilder kb = kieServices.newKieBuilder(getKieFileSystem());
        kb.buildAll();

        if (kb.getResults().hasMessages(org.kie.api.builder.Message.Level.ERROR)) {
            throw new RuntimeException("Build Errors:\n" + kb.getResults().toString());
        }

        KieModule kieModule = kb.getKieModule();
        return kieServices.newKieContainer(kieModule.getReleaseId());

    }

    private void getKieRepository() {
        final KieRepository kieRepository = kieServices.getRepository();

        kieRepository.addKieModule(new KieModule() {
            public ReleaseId getReleaseId() {
                return kieRepository.getDefaultReleaseId();
            }
        });
    }

    @Bean
    public KieSession getKieSession() throws IOException {
        log.debug("Session created");
        return getKieContainer().newKieSession();
    }
}