package acs.behavioral_biometrics.association_rules.services;

import acs.behavioral_biometrics.app_utils.configuration.YamlPropertySourceFactory;
import acs.behavioral_biometrics.fuzzy_profile_mapper.model.FuzzyFeatureSample;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;

@Service
@Getter
@PropertySource(value = "classpath:association_rules-config.yml", factory = YamlPropertySourceFactory.class)
public class RulesIOManagerService {

//    private static final String TEMP_INPUT_ARFF = "tempInput.arff";
//    private static final String TEMP_INPUT= "tempInput.arff.temp";
//    private static final String TEMP_OUTPUT_ARFF = "tempOutput.arff";
//    private static final String TEMP_OUTPUT = "tempOutput.arff.tmp";
//    private static final String FIS_FILE = "authenticator.fcl";
    @Value("${association-rules.files.temp-input-arff}")
    private String TEMP_INPUT_ARFF;
    @Value("${association-rules.files.temp-output-arff}")
    private String TEMP_OUTPUT_ARFF;
    @Value("${association-rules.files.temp-input}")
    private String TEMP_INPUT;
    @Value("${association-rules.files.temp-output}")
    private String TEMP_OUTPUT;



    public void fillFile(List<FuzzyFeatureSample> fuzzyFeatures) throws IOException {

        Path file = declareAttributes(FuzzyFeatureSample.getMapKeys());

        try {
            Files.write(file, "@DATA\r\n".getBytes(), StandardOpenOption.APPEND);
            for(var profile : fuzzyFeatures)
                Files.write(file, profile.toString().concat("\r\n").getBytes(), StandardOpenOption.APPEND);
        } catch (IOException e) { e.printStackTrace(); }

        System.out.println("Features parameters are detected");
    }

    public void deleteTemporaryFiles(){
        try {
            Files.delete(Paths.get(".", TEMP_INPUT));
            Files.delete(Paths.get(".", TEMP_OUTPUT));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void deleteAllFiles(){
        try {
            Files.delete(Paths.get(".", TEMP_INPUT_ARFF));
            Files.delete(Paths.get(".", TEMP_OUTPUT_ARFF));
            deleteTemporaryFiles();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Path declareAttributes(List<String> mapKeys) throws IOException {
        Path filePath = Paths.get(".", TEMP_INPUT_ARFF);
        if (Files.exists(filePath))
            Files.delete(filePath);
        Files.createFile(filePath);
        mapKeys.forEach(attribute -> {
                    try { Files.write(filePath,
                            String.format("@ATTRIBUTE %s STRING\r\n", attribute).getBytes(),
                            StandardOpenOption.APPEND); }
                    catch (IOException e) { e.printStackTrace(); }
                }
        );
        return filePath;
    }
    public String getTempInputArff() {
        return TEMP_INPUT_ARFF;
    }
    public String getTempInput() {
        return TEMP_INPUT;
    }
    public String getTempOutputArff() {
        return TEMP_OUTPUT_ARFF;
    }
    public String getTempOutput() {
        return TEMP_OUTPUT;
    }
}
