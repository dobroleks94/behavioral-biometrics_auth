package com.diploma.behavioralBiometricsAuthentication.entities.enums;


/**
 * Measures for fuzzy values, acquired from pre-defined intervals of numeric values
 *
 * Here:
 *  ->   For typing speed, mean dwell time, mean backspace/delete dwell time,
 *                  mean flight time, mean di-/tri-graph key-up/key-down time - the VERY_SLOW, SLOW, MEDIUM, QUICK, FAST, VERY_FAST measures;
 *  ->   For indicators of frequent mistakes and NumPad usage - the VERY_RARE, RARE, MEDIUM, FREQUENT, VERY_FREQUENT;
 */

public enum FuzzyMeasure {
    VERY_LOW    ("Дуже низька",     "ДН"),  LOW         ("Низька",          "Н"),
    LESS_MEDIUM ("Менше середньої", "МС"),  MORE_MEDIUM ("Більше середньої","БС"),
    MEDIUM      ("Середня",         "С"),   HIGH        ("Висока",          "В"),
    VERY_HIGH   ("Дуже висока",     "В");




    String name;
    String shortRepres;

    FuzzyMeasure(String name, String shortRepres) {
        this.name = name;
        this.shortRepres = shortRepres;
    }
}
