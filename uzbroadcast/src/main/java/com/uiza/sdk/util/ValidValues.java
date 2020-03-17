package com.uiza.sdk.util;

public class ValidValues {
    private ValidValues() {
    }

    public static void check(int value, int min, int max) {
        if (value > max || value < min)
            throw new IllegalArgumentException(String.format("You must set value in [%d, %d]", min, max));
//        else pass
    }
}
