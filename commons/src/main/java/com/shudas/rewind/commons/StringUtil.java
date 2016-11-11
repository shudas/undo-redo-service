package com.shudas.rewind.commons;


import javax.annotation.Nullable;
import java.util.regex.Pattern;

public class StringUtil {
    private static final Pattern PATTERN = Pattern.compile("[^a-zA-Z0-9]");

    public static boolean isNullOrWhitespace(@Nullable String s) {
        return s == null || s.trim().isEmpty();
    }

    public static boolean isAlphaNumeric(@Nullable String s) {
        if (s != null) {
            return !PATTERN.matcher(s).find();
        }
        return false;
    }
}
