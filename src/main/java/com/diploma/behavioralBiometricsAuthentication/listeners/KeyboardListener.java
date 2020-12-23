package com.diploma.behavioralBiometricsAuthentication.listeners;

import com.diploma.behavioralBiometricsAuthentication.services.*;
import lombok.AllArgsConstructor;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;
import org.springframework.stereotype.Component;



@Component
@AllArgsConstructor
public class KeyboardListener implements NativeKeyListener {

    private final KeyProfileHandlerService keyProfileHandlerService;
    private final KeyProfileSamplesService kpsService;


    public void nativeKeyPressed(NativeKeyEvent e) {
        keyProfileHandlerService.processPressing(NativeKeyEvent.getKeyText(e.getKeyCode()), e.getKeyCode(), e.getWhen());
    }

    public void nativeKeyReleased(NativeKeyEvent e) {
        kpsService.addTemporary( keyProfileHandlerService.processReleasing(e.getKeyCode(), e.getWhen()) );
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
    }

    public void nativeKeyTyped(NativeKeyEvent e) { }




}
