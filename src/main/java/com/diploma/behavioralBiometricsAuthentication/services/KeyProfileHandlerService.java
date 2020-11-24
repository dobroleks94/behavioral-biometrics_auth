package com.diploma.behavioralBiometricsAuthentication.services;

import com.diploma.behavioralBiometricsAuthentication.entities.keysAnalysisEntities.KeyProfile;
import com.diploma.behavioralBiometricsAuthentication.factories.KeyInfoHolderFactory;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
public class KeyProfileHandlerService {

    private final KeyInfoHolderFactory keyInfoHolderFactory;
    private Utility keyHandler;
    private KeyProfile currentKey, prevKey;


    public KeyProfileHandlerService(KeyInfoHolderFactory keyInfoHolderFactory){
        this.keyInfoHolderFactory = keyInfoHolderFactory;
    }

    @PostConstruct
    private void initializeVariables(){

        this.keyHandler = new Utility();

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

        if(keyHandler.comparePreviousKeyTo(keyCode))
            prevKey = keyHandler.updateKey(prevKey, releaseTime);
        else
            prevKey = keyHandler.updateKey(currentKey, releaseTime);

        return prevKey;
    }



    private class Utility {

        private boolean comparePreviousKeyTo(int keyCode) {
            return  (!prevKey.isReleased()) && (prevKey.getKeyCode() == keyCode);
        }

        private KeyProfile updateKey(KeyProfile key, long releaseTime){
            key.setReleaseTimestamp(releaseTime);
            key.updateHoldTime();
            return checkForSpecialKey(key);
        }

        private KeyProfile checkForSpecialKey(KeyProfile key) {
            if (key.getKeyCode() == NativeKeyEvent.VC_BACKSPACE)
                key.setBackspaceKey(true);
            else if (key.getKeyCode() == NativeKeyEvent.VC_DELETE)
                key.setDeleteKey(true);
            else if (key.getKeyVal().contains("NumPad"))
                key.setNumpadKey(true);

            return key;
        }
    }
}
