package com.diploma.behavioralBiometricsAuthentication.services;

import com.diploma.behavioralBiometricsAuthentication.entities.featureSamples.FuzzyFeatureSample;
import net.sourceforge.jFuzzyLogic.FIS;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;

@Service
public class IOManagerService {

    private static final String TEMP_INPUT_ARFF = "tempInput.arff";
    private static final String TEMP_INPUT= "tempInput.arff.temp";
    private static final String TEMP_OUTPUT_ARFF = "tempOutput.arff";
    private static final String TEMP_OUTPUT = "tempOutput.arff.tmp";
    private static final String FIS_FILE = "authenticator.fcl";

    private Utility utils;

    @PostConstruct
    private void initializeVariables(){
        this.utils = new Utility();
    }

    public void fillFile(List<FuzzyFeatureSample> fuzzyFeatures) throws IOException {

        Path file = utils.declareAttributes(FuzzyFeatureSample.getMapKeys());

        try {
            Files.write(file, "@DATA\r\n".getBytes(), StandardOpenOption.APPEND);
            for(var profile : fuzzyFeatures)
                Files.write(file, profile.toString().concat("\r\n").getBytes(), StandardOpenOption.APPEND);
        } catch (IOException e) { e.printStackTrace(); }

        System.out.println("Features parameters are detected");
    }
    public void writeFIS(FIS fis) throws IOException {
        Path filePath = Paths.get(".", FIS_FILE);
        Files.createFile(filePath);

        Files.write(filePath, fis.toString().getBytes());
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


    public static String getTempInputArff() {
        return TEMP_INPUT_ARFF;
    }
    public static String getTempInput() {
        return TEMP_INPUT;
    }
    public static String getTempOutputArff() {
        return TEMP_OUTPUT_ARFF;
    }
    public static String getTempOutput() {
        return TEMP_OUTPUT;
    }


    private class Utility{

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
    }
}
