package com.diploma.behavioralBiometricsAuthentication.listeners;

import com.diploma.behavioralBiometricsAuthentication.entities.User;
import com.diploma.behavioralBiometricsAuthentication.entities.associationRule.AssociationRule;
import com.diploma.behavioralBiometricsAuthentication.entities.featureSamples.FeatureSample;
import com.diploma.behavioralBiometricsAuthentication.entities.featureSamples.FuzzyFeatureSample;
import com.diploma.behavioralBiometricsAuthentication.entities.fuzzification.FuzzyMeasureItem;
import com.diploma.behavioralBiometricsAuthentication.entities.logger.SystemLogger;
import com.diploma.behavioralBiometricsAuthentication.services.*;
import lombok.AllArgsConstructor;
import net.sourceforge.jFuzzyLogic.FIS;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;


@Component
@AllArgsConstructor
public class KeyboardListener implements NativeKeyListener {

    private final KeyProfileHandlerService keyProfileHandlerService;
    private final KeyProfileSamplesService kpsService;
    private final FeatureSampleService featureSampleService;
    private final FuzzyFeatureSampleService fuzzyFeatureSampleService;
    private final FuzzyMeasureItemService fuzzyMeasureItemService;
    private final AssociationRulesService associationRulesService;
    private final SystemLogger systemLogger;
    private final UserService userService;
    private final FuzzyInferenceService fuzzyInferenceService;
    private final IOManagerService ioManagerService;



    public void nativeKeyPressed(NativeKeyEvent e) {
        keyProfileHandlerService.processPressing(NativeKeyEvent.getKeyText(e.getKeyCode()), e.getKeyCode(), e.getWhen());
    }

    public void nativeKeyReleased(NativeKeyEvent e) {

        /*if(e.getKeyCode() == NativeKeyEvent.VC_SPACE || e.getKeyCode() == NativeKeyEvent.VC_ENTER){
            kpsService.buildSamples();
            if(e.getKeyCode() == NativeKeyEvent.VC_ENTER){
//                FeatureSample sample = FeatureSample sample = featureSampleService.buildFeatureSample();
//                featureSampleService.save(sample);
//                systemLogger.log(SystemLogger.SAMPLE_SAVE_SUCCESS_RESULT);
//                String result = fuzzyInferenceService.authentication(sample);
//                System.out.println(result);
                return;
            }
        }
        //for debug
        if (e.getKeyCode() == NativeKeyEvent.VC_ESCAPE){
            User user = userService.findById(1L);

            fuzzyFeatureSampleService.setFuzzyMeasures(fuzzyMeasureItemService.computeFuzzyMeasureItems());
            fuzzyFeatureSampleService.deleteAllByUserId(user.getId());
            associationRulesService.deleteAll();

            List<FuzzyFeatureSample> fuzzyFeatures = fuzzyFeatureSampleService.saveAll( featureSampleService.findAll() );
            List<AssociationRule> associationRules = associationRulesService.saveAll(
                    associationRulesService.assignOwner(userService.findById(1L),
                            associationRulesService.getAssociationRules(fuzzyFeatures))
            );
            List<FuzzyMeasureItem> measureItems = fuzzyMeasureItemService.getAllFuzzyMeasureItems();
            systemLogger.log(SystemLogger.ASSOCIATION_RULES_SAVE_SUCCESS_RESULT);
            FIS fis = fuzzyInferenceService.createNewFIS(10, associationRules);
            try { ioManagerService.writeFIS(fis); }
            catch (IOException ioException) { ioException.printStackTrace(); }


            return;
        }*/
        kpsService.addTemporary( keyProfileHandlerService.processReleasing(e.getKeyCode(), e.getWhen()) );

    }

    public void nativeKeyTyped(NativeKeyEvent e) { }




}
