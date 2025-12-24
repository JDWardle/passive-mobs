package com.jdwardle.passivemobs;

public enum AggressionLevel {
    PASSIVE, NORMAL, PEACEFUL;

    public static AggressionLevel getLevel(String level) throws IllegalStateException {
        return switch (level) {
            case "passive" -> PASSIVE;
            case "normal" -> NORMAL;
            case "peaceful" -> PEACEFUL;
            default -> throw new IllegalStateException("Unexpected value: " + level);
        };
    }

    public String toString() {
        return switch (this) {
            case PASSIVE -> "passive";
            case NORMAL -> "normal";
            case PEACEFUL -> "peaceful";
        };
    }
}
