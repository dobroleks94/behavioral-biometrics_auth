package acs.behavioral_biometrics.keystroke_handler.services;

import acs.behavioral_biometrics.keystroke_handler.enums.SampleType;
import acs.behavioral_biometrics.keystroke_handler.models.KeyProfile;
import acs.behavioral_biometrics.keystroke_handler.models.KeysSample;
import acs.behavioral_biometrics.keystroke_handler.utilities.KeySamplesUtils;
import lombok.Getter;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

@Service
public class KeyProfileSamplesService {

    private final KeySamplesUtils utils;

    @Getter
    private List<KeysSample> samplesCollector;
    @Getter
    private List<KeyProfile> keyProfilesCollector, temporaryCollector;


    public KeyProfileSamplesService(KeySamplesUtils utils) {
        this.utils = utils;
    }

    @PostConstruct
    private void initializeVariables() {
        this.samplesCollector = new ArrayList<>();
        this.keyProfilesCollector = new ArrayList<>();
        this.temporaryCollector = new ArrayList<>();
    }


    public void addTemporary(KeyProfile key) {
        temporaryCollector.add(key);
    }

    public void buildSamples() {
        if (utils.gather(SampleType.DiGraph, temporaryCollector, samplesCollector) && utils.gather(SampleType.TriGraph, temporaryCollector, samplesCollector)) {
            keyProfilesCollector.addAll(temporaryCollector);
            temporaryCollector.clear();
        }
    }

    public void clearAllContainers(){
        getTemporaryCollector().clear();
        getKeyProfilesCollector().clear();
        getSamplesCollector().clear();
    }
}
