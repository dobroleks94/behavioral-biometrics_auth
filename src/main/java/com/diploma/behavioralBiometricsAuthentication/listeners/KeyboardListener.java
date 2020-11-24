package com.diploma.behavioralBiometricsAuthentication.listeners;

import com.diploma.behavioralBiometricsAuthentication.entities.featureSamples.FeatureSample;
import com.diploma.behavioralBiometricsAuthentication.services.*;
import lombok.AllArgsConstructor;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;
import org.springframework.stereotype.Component;

import java.util.List;


@Component
@AllArgsConstructor
public class KeyboardListener implements NativeKeyListener {

    private final KeyProfileHandlerService keyProfileHandlerService;
    private final KeyProfileSamplesService kpsService;
    private final FeatureSampleService featureSampleService;
    private final FuzzyFeatureSampleService fuzzyFeatureSampleService;
    private final FuzzyMeasureItemService fuzzyMeasureItemService;



    public void nativeKeyPressed(NativeKeyEvent e) {
        keyProfileHandlerService.processPressing(NativeKeyEvent.getKeyText(e.getKeyCode()), e.getKeyCode(), e.getWhen());
    }

    public void nativeKeyReleased(NativeKeyEvent e) {

        List<FeatureSample> featureSamples = featureSampleService.findAll(); // for debug


        if(e.getKeyCode() == NativeKeyEvent.VC_SPACE || e.getKeyCode() == NativeKeyEvent.VC_ENTER){
            kpsService.buildSamples();
            if(e.getKeyCode() == NativeKeyEvent.VC_ENTER){
                FeatureSample sample = kpsService.buildFeatureSample();
                featureSampleService.save(sample);

                fuzzyFeatureSampleService.setFuzzyMeasures(
                        !fuzzyMeasureItemService.getAllFuzzyMeasureItems().isEmpty()
                                ? fuzzyMeasureItemService.getAllFuzzyMeasureItems()
                                : fuzzyMeasureItemService.computeFuzzyMeasureItems()
                );
                fuzzyFeatureSampleService.saveAll( featureSampleService.findAll() );
                System.out.println("Sample saved!");
                //System.exit(0);
            }
        }
        //for debug
        if (e.getKeyCode() == NativeKeyEvent.VC_ESCAPE){
            Double[] time = featureSampleService.getTimeCostsRange();
            Double[] freq = featureSampleService.getFrequencyRange();
            Double[] speed = featureSampleService.getTypingSpeedRange();
            System.exit(0);
        }

        kpsService.addTemporary( keyProfileHandlerService.processReleasing(e.getKeyCode(), e.getWhen()) );

    }

    public void nativeKeyTyped(NativeKeyEvent e) { }




}
