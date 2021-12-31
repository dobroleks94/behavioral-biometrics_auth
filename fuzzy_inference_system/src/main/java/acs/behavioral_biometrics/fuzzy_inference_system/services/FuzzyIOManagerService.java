package acs.behavioral_biometrics.fuzzy_inference_system.services;

import acs.behavioral_biometrics.app_utils.configuration.YamlPropertySourceFactory;
import net.sourceforge.jFuzzyLogic.FIS;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
@PropertySource(value = "classpath:fis-config.yml", factory = YamlPropertySourceFactory.class)
public class FuzzyIOManagerService {

    @Value("${fcl.fis.file-name}")
    private String FIS_FILE;

    public void writeFIS(FIS fis) throws IOException {
        Path filePath = Paths.get(".", FIS_FILE);
        if(Files.exists(filePath))
            Files.delete(filePath);
        Files.createFile(filePath);
        Files.write(filePath, fis.toString().getBytes());
    }

    public FIS loadFIS() {
        return FIS.load(FIS_FILE, true);
    }
}
