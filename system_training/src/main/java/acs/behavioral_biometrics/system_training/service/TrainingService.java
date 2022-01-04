package acs.behavioral_biometrics.system_training.service;

import acs.behavioral_biometrics.access_control.AccessControlService;
import acs.behavioral_biometrics.app_utils.service.UserService;
import acs.behavioral_biometrics.association_rules.models.AssociationRule;
import acs.behavioral_biometrics.association_rules.services.AssociationRulesService;
import acs.behavioral_biometrics.fuzzy_inference_system.services.FuzzyInferenceService;
import acs.behavioral_biometrics.fuzzy_inference_system.services.FuzzyMeasureItemService;
import acs.behavioral_biometrics.fuzzy_inference_system.services.FuzzyIOManagerService;
import acs.behavioral_biometrics.fuzzy_profile_mapper.model.FuzzyFeatureSample;
import acs.behavioral_biometrics.fuzzy_profile_mapper.services.FuzzyFeatureSampleService;
import acs.behavioral_biometrics.info_utils.ArbitraryPhraseExtractor;
import acs.behavioral_biometrics.keystroke_handler.listener.KeyboardListener;
import acs.behavioral_biometrics.keystroke_handler.services.KeyProfileSamplesService;
import acs.behavioral_biometrics.user_keystroke_profile.model.FeatureSample;
import acs.behavioral_biometrics.user_keystroke_profile.service.FeatureSampleService;
import lombok.Data;
import net.sourceforge.jFuzzyLogic.FIS;
import org.jnativehook.GlobalScreen;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
@Data
public class TrainingService {

    @Value("${fcl.fis.deffuzification.term.step}")
    private int termStep;
    private boolean activeListener;

    private final KeyboardListener listener;
    private final KeyProfileSamplesService kpsService;

    private final AccessControlService accessControlService;
    private final UserService userService;

    private final FuzzyInferenceService fuzzyInferenceService;
    private final FuzzyFeatureSampleService ffsService;
    private final FuzzyMeasureItemService fuzzyMeasureItemService;
    private final FuzzyIOManagerService fuzzyIoManagerService;

    private final FeatureSampleService fsService;

    private final AssociationRulesService arService;

    private final ArbitraryPhraseExtractor phraseExtractor;

    /*public TrainingService(KeyboardListener listener,
                           KeyProfileSamplesService kpsService,
                           AccessControlService accessControlService,
                           UserService userService,
                           FuzzyInferenceService fuzzyInferenceService,
                           FuzzyFeatureSampleService ffsService,
                           FuzzyMeasureItemService fuzzyMeasureItemService,
                           IOManagerService ioManagerService,
                           FeatureSampleService fsService,
                           AssociationRulesService arService,
                           ArbitraryPhraseExtractor phraseExtractor) {
        this.listener = listener;
        this.kpsService = kpsService;
        this.accessControlService = accessControlService;
        this.userService = userService;
        this.fuzzyInferenceService = fuzzyInferenceService;
        this.ffsService = ffsService;
        this.fuzzyMeasureItemService = fuzzyMeasureItemService;
        this.ioManagerService = ioManagerService;
        this.fsService = fsService;
        this.arService = arService;
        this.phraseExtractor = phraseExtractor;
    }*/

    public void generateInferringFIS(List<AssociationRule> associationRules) throws IOException {
         FIS fis = fuzzyInferenceService.createNewFIS(termStep, associationRules);
         fuzzyIoManagerService.writeFIS(fis);
    }

    public List<AssociationRule> obtainNewKeystrokeAssociationRules(long userId) {
        ffsService.accumulateFuzzyMeasures(fuzzyMeasureItemService.computeFuzzyMeasureItems());
        clearSamplesAndRules(userId);

        List<FuzzyFeatureSample> fuzzyFeatures = ffsService.saveAll( fsService.findAll() );
        return arService.saveAll(
                arService.assignOwner(
                        userService.findById(userId),
                        arService.getAssociationRules(fuzzyFeatures))
        );
    }

    private void clearSamplesAndRules(long userId) {
        ffsService.deleteAllByUserId(userId);
        arService.deleteUserRules(userId);
    }

    public void writeSample(long userId) {
        kpsService.buildSamples();

        FeatureSample featureSample = fsService.buildFeatureSample();
        featureSample.setUserId(userId);
        fsService.save(featureSample);

        deactivateListener();
        updateUser(false);
    }

    public void updateUser(boolean isProtected) {
        accessControlService.getAuthenticatedUser().setProtectionEnabled(isProtected);
        userService.saveUser(accessControlService.getAuthenticatedUser());
    }

    public boolean activateListener(){
        if(!this.activeListener) {
            GlobalScreen.addNativeKeyListener(listener);
            this.activeListener = true;
            return true;
        }
        return false;
    }

    public boolean deactivateListener(){
        if(this.activeListener) {
            GlobalScreen.removeNativeKeyListener(listener);
            this.activeListener = false;
            return true;
        }
        return false;
    }

    public String extractArbitraryPhrase(String text) throws IOException {
        String phrase = phraseExtractor.getRandomPhrase();
        if (!phrase.equals(text))
            return phrase;
        return extractArbitraryPhrase(phrase);
    }
    public void clearKeystrokeData(){
        kpsService.clearAllContainers();
    }
    public void processKeystrokeData(){
        kpsService.buildSamples();
    }
}
