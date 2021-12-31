package acs.behavioral_biometrics.keystroke_handler.models;

import acs.behavioral_biometrics.keystroke_handler.enums.SampleType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * Combination of several keys (here is combination of 2 and 3 keys)
 * Contains information about
 *      ** relation between release/press timestamps of several keys
 *      ** time between pressing two keys
 *      ** mean hold time of comprised keys
 */
@Getter
@Setter
@AllArgsConstructor
public class KeysSample {

    private String name;
    private SampleType type;

    private double meanDwell;
    private long flightTime, keyUpTime, keyDownTime;
}
