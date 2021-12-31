package acs.behavioral_biometrics.gui_fx.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Messages {

    RECORD_ADDED("Record has been successfully added!"),
    BIOMETRICS_ACTIVATED("Biometrics security successfully activated"),
    SUCCESS_AUTH("Authorization successfully done!"),

    FAIL_AUTH("Authorization failed :("),
    FAIL("Oops... There is unexpected error :("),

    ERROR_PWD("Wrong password!"),
    ERROR_LOGIN("Wrong username!"),
    ERROR_PHRASE("Check the text is valid!");

    private final String message;
}
