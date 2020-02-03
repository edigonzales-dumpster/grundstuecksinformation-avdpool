package ch.so.agi.grundstuecksinformation;

import org.apache.camel.support.processor.idempotent.FileIdempotentRepository;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import java.io.File;

import javax.enterprise.context.ApplicationScoped;

import io.quarkus.runtime.annotations.RegisterForReflection;

@ApplicationScoped
@RegisterForReflection
public class AppConfig {
    private static final Logger log = Logger.getLogger(AppConfig.class);
    
    @ConfigProperty(name = "pathToIdempotentFile") 
    String pathToIdempotentFile;

    public FileIdempotentRepository fileConsumerRepo() {
        FileIdempotentRepository fileConsumerRepo = null;
        try {
            fileConsumerRepo = new FileIdempotentRepository();
            fileConsumerRepo.setFileStore(new File(pathToIdempotentFile));
            fileConsumerRepo.setCacheSize(5000);
            fileConsumerRepo.setMaxFileStoreSize(51200000);
        } catch (Exception e) {
            log.error("Caught exception inside Creating fileConsumerRepo ..." + e.getMessage());
        }
        if (fileConsumerRepo == null) {
            log.error("fileConsumerRepo == null");
        }
        return fileConsumerRepo;
    }
}
