package acs.behavioral_biometrics.gui_fx.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Notification {

    SUCCESS("success"), SUCCESS_WRITE("success-write"), SUCCESS_FIS("success-writeFIS"),
    FAIL("fail"), FAIL_FIS("fail-writeFIS"),
    ERROR_PWD("error-password"), ERROR_PHRASE("error-phrase"), ERROR_LOGIN("error-login");

    private final String definition;
}
