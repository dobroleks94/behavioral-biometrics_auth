package com.diploma.behavioralBiometricsAuthentication.listeners;

import com.diploma.behavioralBiometricsAuthentication.entities.associationRule.AssociationRule;
import com.diploma.behavioralBiometricsAuthentication.entities.featureSamples.FeatureSample;
import com.diploma.behavioralBiometricsAuthentication.services.*;
import lombok.AllArgsConstructor;
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
    private final StageCreationService stageCreationService;



    public void nativeKeyPressed(NativeKeyEvent e) {
        keyProfileHandlerService.processPressing(NativeKeyEvent.getKeyText(e.getKeyCode()), e.getKeyCode(), e.getWhen());
    }

    public void nativeKeyReleased(NativeKeyEvent e) {

        List<FeatureSample> featureSamples = featureSampleService.findAll(); // for debug

        if(e.getKeyCode() == NativeKeyEvent.VC_SPACE || e.getKeyCode() == NativeKeyEvent.VC_ENTER){
            kpsService.buildSamples();
            if(e.getKeyCode() == NativeKeyEvent.VC_ENTER){
                FeatureSample sample = featureSampleService.buildFeatureSample();
                featureSampleService.save(sample);
                System.out.println("Sample saved!");
                return;
            }
        }
        //for debug
        if (e.getKeyCode() == NativeKeyEvent.VC_ESCAPE){
            fuzzyFeatureSampleService.setFuzzyMeasures(fuzzyMeasureItemService.computeFuzzyMeasureItems());
            fuzzyFeatureSampleService.deleteAll();
            fuzzyFeatureSampleService.saveAll( featureSampleService.findAll() );

            List<AssociationRule> associationRules = associationRulesService.getAssociationRules(fuzzyFeatureSampleService.findAll());
            //TODO: Saving AssociationRules to database
            return;
        }

        kpsService.addTemporary( keyProfileHandlerService.processReleasing(e.getKeyCode(), e.getWhen()) );

    }

    public void nativeKeyTyped(NativeKeyEvent e) { }




}
