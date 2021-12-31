package acs.behavioral_biometrics.keystroke_handler.utilities;

import acs.behavioral_biometrics.keystroke_handler.models.KeyProfile;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.springframework.stereotype.Component;

@Component
public class KeyHandlingUtils {
    public boolean comparePreviousKeyTo(int keyCode, KeyProfile prevKey) {
        return  (!prevKey.isReleased()) && (prevKey.getKeyCode() == keyCode);
    }

    public KeyProfile updateKey(KeyProfile key, long releaseTime){
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
