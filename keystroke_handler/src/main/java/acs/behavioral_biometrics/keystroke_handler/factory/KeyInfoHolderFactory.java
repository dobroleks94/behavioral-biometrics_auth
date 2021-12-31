package acs.behavioral_biometrics.keystroke_handler.factory;

import acs.behavioral_biometrics.keystroke_handler.enums.SampleType;
import acs.behavioral_biometrics.keystroke_handler.models.KeyProfile;
import acs.behavioral_biometrics.keystroke_handler.models.KeysSample;
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
