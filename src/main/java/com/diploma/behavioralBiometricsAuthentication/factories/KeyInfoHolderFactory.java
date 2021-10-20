package com.diploma.behavioralBiometricsAuthentication.factories;

import com.diploma.behavioralBiometricsAuthentication.entities.keysAnalysisEntities.KeyProfile;
import com.diploma.behavioralBiometricsAuthentication.entities.keysAnalysisEntities.KeysSample;
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

    public KeysSample createSample(String name, SampleType type,
                                   double meanDwell, long flightTime,
                                   long keyDownTime, long keyUpTime){
        return new KeysSample(name, type, meanDwell, flightTime, keyUpTime, keyDownTime);
    }

}
