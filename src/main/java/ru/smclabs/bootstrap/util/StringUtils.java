package ru.smclabs.bootstrap.util;

import java.math.BigDecimal;

public class StringUtils {

    public static String dependName(double n, String form1, String form2, String form3) {
        n = Math.abs(n) % 100;
        double n1 = n % 10;
        if (n > 10 && n < 20) return form3;
        if (n1 > 1 && n1 < 5) return form2;
        if (n1 == 1) return form1;
        return form3;
    }

    public static String dependName(long n, String form1, String form2, String form3) {
        n = Math.abs(n) % 100;
        long n1 = n % 10;
        if (n > 10 && n < 20) return form3;
        if (n1 > 1 && n1 < 5) return form2;
        if (n1 == 1) return form1;
        return form3;
    }

    public static String dependName(int n, String form1, String form2, String form3) {
        n = Math.abs(n) % 100;
        int n1 = n % 10;
        if (n > 10 && n < 20) return form3;
        if (n1 > 1 && n1 < 5) return form2;
        if (n1 == 1) return form1;
        return form3;
    }

    public static String dependName(BigDecimal turnover, String form1, String form2, String form3) {
        return dependName(turnover.doubleValue(), form1, form2, form3);
    }

    public static String[] splitByLength(String string, int length) {
        return string.replaceAll("\\s+", " ")
                .replaceAll(String.format(" *(.{1,%d})(?=$| ) *", length), "$1\n")
                .split("\n");
    }
}
