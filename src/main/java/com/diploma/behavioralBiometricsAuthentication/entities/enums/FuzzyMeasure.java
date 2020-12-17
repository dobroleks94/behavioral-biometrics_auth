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
    VERY_LOW    ("Дуже низька",     "ДН", "veryLow"),  LOW         ("Низька",          "Н", "low"),
    LESS_MEDIUM ("Менше середньої", "МС", "lessMedium"),  MORE_MEDIUM ("Більше середньої","БС", "moreMedium"),
    MEDIUM      ("Середня",         "С", "medium"),   HIGH        ("Висока",          "В", "high"),
    VERY_HIGH   ("Дуже висока",     "ДВ", "veryHigh");


    private final String shortRepres;
    private final String engRepres;

    FuzzyMeasure(String name, String shortRepres, String engRepres) {
        this.shortRepres = shortRepres;
        this.engRepres = engRepres;
    }

    public String getShortRepres() {
        return shortRepres;
    }
    public String getEngRepres() {
        return engRepres;
    }

    public static FuzzyMeasure getByShortRepres(String shortRepres){
        return Arrays.stream(values())
                .filter(value -> value.getShortRepres().equals(shortRepres))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Such FuzzyMeasure does not exist!"));
    }
}
