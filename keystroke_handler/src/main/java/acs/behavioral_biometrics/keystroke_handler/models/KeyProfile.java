package acs.behavioral_biometrics.keystroke_handler.models;

import lombok.Getter;
import lombok.Setter;

/**
 * Describes the single pressed key.
 * Contains information about pressed key value, code, release/press timestamps, time of key hold;
 * Differentiates whether pressed key is key for edit or belongs to NumPad keys;
 * Describes key's state: whether it pressed or released.
 */
@Getter
@Setter
public class KeyProfile{

    //Key's measures
    private String keyVal;
    private int keyCode;
    private long pressTimestamp, releaseTimestamp, holdTime;

    //Feature of special key
    private boolean backspaceKey, deleteKey, numpadKey,
    //Key's state
                    pressed, released;


    public KeyProfile(){
        this.pressed = false;
        this.released = true;
        this.backspaceKey = false;
        this.deleteKey = false;
        this.numpadKey = false;
    }
    public KeyProfile(String keyVal, int keyCode) {
        this();
        this.keyVal = keyVal;
        this.keyCode = keyCode;
    }

    public void setPressTimestamp(long pressTimestamp) {
        setPressed(true);
        setReleased(false);
        this.pressTimestamp = pressTimestamp;
    }

    public void setReleaseTimestamp(long releaseTimestamp) {
        setReleased(true);
        setPressed(false);
        this.releaseTimestamp = releaseTimestamp;
    }

    public void updateHoldTime(){
        this.holdTime = releaseTimestamp - pressTimestamp;
    }

    @Override
    public String toString() {
        return "[ Key: " + keyVal + "]\n[Hold time = " + holdTime + "; Pressed-"+pressed+"; Released-" + released+"]\n---------------------------";
    }
}
