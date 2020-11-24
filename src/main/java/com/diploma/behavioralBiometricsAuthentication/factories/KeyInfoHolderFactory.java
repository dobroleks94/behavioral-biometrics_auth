package com.diploma.behavioralBiometricsAuthentication.factories;

import com.diploma.behavioralBiometricsAuthentication.entities.keysAnalysisEntities.KeyProfile;
import com.diploma.behavioralBiometricsAuthentication.entities.keysAnalysisEntities.Sample;
import com.diploma.behavioralBiometricsAuthentication.entities.enums.SampleType;
import org.springframework.stereotype.Component;

@Component
public class KeyInfoHolderFactory {

    public KeyProfile createEmptyKeyProfile(){
        return new KeyProfile();
    }

    public KeyProfile createKeyProfile(String keyVal, int keyCode){
        return new KeyProfile(keyVal, keyCode);
    }

    public Sample createSample(String name, SampleType type,
                                      double meanDwell, long flightTime,
                                      long keyDownTime, long keyUpTime){
        return new Sample(name, type, meanDwell, flightTime, keyUpTime, keyDownTime);
    }

}
