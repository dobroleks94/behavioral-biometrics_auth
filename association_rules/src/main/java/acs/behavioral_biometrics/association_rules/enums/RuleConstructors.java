package acs.behavioral_biometrics.association_rules.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

@AllArgsConstructor
@Getter
public enum RuleConstructors {
    SPACE(" "),
    CONDITION("IF"),
    IS("IS"),
    CONSEQUENCE("THEN"),
    AND("AND"),
    OR("OR");

    private final String pattern;
    public static String construct(RuleConstructors ... constructors){
        return Arrays.stream(constructors)
                .map(RuleConstructors::getPattern)
                .reduce(String::concat)
                .orElseThrow(RuntimeException::new);
    }
}
