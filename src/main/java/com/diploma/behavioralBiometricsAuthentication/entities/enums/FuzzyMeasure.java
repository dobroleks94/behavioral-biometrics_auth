package com.diploma.behavioralBiometricsAuthentication.entities.enums;


import java.util.Arrays;

/**
 * Measures for fuzzy values, acquired from pre-defined intervals of numeric values
 *
 * Here:
 *  ->   For typing speed, mean dwell time, frequency of mistakes and NumPad usage,
 *              mean backspace/delete dwell time, mean flight time,
 *              mean di-/tri-graph key-up/key-down time - the VERY_LOW, LOW, LESS_MEDIUM, MEDIUM, MORE_MEDIUM, HIGH, VERY_HIGH measures;
 */

public enum FuzzyMeasure {
    VERY_LOW    ("Дуже низька",     "ДН"),  LOW         ("Низька",          "Н"),
    LESS_MEDIUM ("Менше середньої", "МС"),  MORE_MEDIUM ("Більше середньої","БС"),
    MEDIUM      ("Середня",         "С"),   HIGH        ("Висока",          "В"),
    VERY_HIGH   ("Дуже висока",     "ДВ");




    String name;
    String shortRepres;

    FuzzyMeasure(String name, String shortRepres) {
        this.name = name;
        this.shortRepres = shortRepres;
    }

    public String getShortRepres() {
        return shortRepres;
    }

    public static FuzzyMeasure getByShortRepres(String shortRepres){
        return Arrays.stream(values())
                .filter(value -> value.getShortRepres().equals(shortRepres))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Such FuzzyMeasure does not exist!"));
    }
}
