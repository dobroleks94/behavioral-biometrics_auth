package acs.behavioral_biometrics.keystroke_handler.services;

import acs.behavioral_biometrics.keystroke_handler.factory.KeyInfoHolderFactory;
import acs.behavioral_biometrics.keystroke_handler.models.KeyProfile;
import acs.behavioral_biometrics.keystroke_handler.utilities.KeyHandlingUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
public class KeyProfileHandlerService {

    private final KeyInfoHolderFactory keyInfoHolderFactory;
    private final KeyHandlingUtils keyHandler;
    private KeyProfile currentKey, prevKey;

    @Autowired
    public KeyProfileHandlerService(KeyInfoHolderFactory keyInfoHolderFactory,
                                    KeyHandlingUtils keyHandler){
        this.keyInfoHolderFactory = keyInfoHolderFactory;
        this.keyHandler = keyHandler;
    }

    @PostConstruct
    private void initializeVariables(){
        this.currentKey = keyInfoHolderFactory.createEmptyKeyProfile();
        this.prevKey = keyInfoHolderFactory.createEmptyKeyProfile();
    }


    public void processPressing(String keyValue, int keyCode, long pressTime){
        if(!currentKey.isReleased())     // for case, when new pressed button is pressed before previous one hasn't been released yet (Press-Press case)
            prevKey = currentKey;
        currentKey = keyInfoHolderFactory.createKeyProfile(keyValue, keyCode);
        currentKey.setPressTimestamp(pressTime);

    }

    public KeyProfile processReleasing(int keyCode, long releaseTime){
        if (keyHandler.comparePreviousKeyTo(keyCode, prevKey))
            prevKey = keyHandler.updateKey(currentKey, releaseTime);
        else
            prevKey = keyHandler.updateKey(prevKey, releaseTime);
        return prevKey;
    }
}
