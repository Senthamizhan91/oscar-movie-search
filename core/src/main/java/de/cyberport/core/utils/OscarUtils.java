package de.cyberport.core.utils;

import org.apache.commons.lang3.StringUtils;

public class OscarUtils {

    public static boolean compareInteger(int actual, int expected) {
        return actual == expected ? true : false;
    }

    public static boolean compareString(String actual, String expected) {
        if (StringUtils.isNotBlank(actual) && StringUtils.isNotBlank(expected)) {
            return StringUtils.equalsIgnoreCase(actual, expected) ? true : false;
        }
        return false;
    }

    public static boolean compareMinValue(int actual, int minimum) {
        return actual >= minimum ? true : false;
    }

    public static boolean compareMaxValue(int actual, int maximum) {
        return actual <= maximum ? true : false;
    }
}
