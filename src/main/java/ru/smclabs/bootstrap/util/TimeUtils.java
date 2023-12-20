package ru.smclabs.bootstrap.util;

import java.text.SimpleDateFormat;
import java.util.concurrent.TimeUnit;

public class TimeUtils {

    private static final SimpleDateFormat DATE_TIME_FORMAT = new SimpleDateFormat("dd.MM.yyyy' 'HH:mm");
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd.MM.yyyy");

    public static String toHumanTime(long timestamp) {
        return toHumanTime(timestamp, false);
    }

    public static String toHumanTime(long timestamp, boolean shortTypes) {
        long days = TimeUnit.MILLISECONDS.toDays(timestamp);
        long hours = TimeUnit.MILLISECONDS.toHours(timestamp) - TimeUnit.DAYS.toHours(days);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(timestamp) - TimeUnit.DAYS.toMinutes(days)
                - TimeUnit.HOURS.toMinutes(hours);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(timestamp) - TimeUnit.DAYS.toSeconds(days)
                - TimeUnit.HOURS.toSeconds(hours) - TimeUnit.MINUTES.toSeconds(minutes);

        StringBuilder builder = new StringBuilder();

        if (days > 0) {
            builder.append(days).append(StringUtils.dependName(days,
                    shortTypes ? "д." : " день",
                    shortTypes ? "д." : " дня",
                    shortTypes ? "д." : " дней"));
        }

        if (hours > 0) {
            if (builder.length() > 0) builder.append(" ");
            builder.append(hours).append(StringUtils.dependName(hours,
                    shortTypes ? "ч." : " час",
                    shortTypes ? "ч." : " часа",
                    shortTypes ? "ч." : " часов"));
        }

        if (minutes > 0 && days == 0) {
            if (builder.length() > 0) builder.append(" ");
            builder.append(minutes).append(StringUtils.dependName(minutes,
                    shortTypes ? "м." : " минута",
                    shortTypes ? "м." : " минуты",
                    shortTypes ? "м." : " минут"));
        }

        if (seconds > 0 && days == 0 && hours == 0) {
            if (builder.length() > 0) builder.append(" ");
            builder.append(seconds).append(StringUtils.dependName(seconds,
                    shortTypes ? "с." : " секунда",
                    shortTypes ? "с." : " секунды",
                    shortTypes ? "с." : " секунд"));
        }

        if (days == 0 && hours == 0 && minutes == 0 && seconds == 0) {
            builder.append("0").append(shortTypes ? "с" : " секунд");
        }

        return builder.toString();
    }

    public static String toHumanTimeOrDate(Long timestamp) {
        long timeAfterRealise = System.currentTimeMillis() - timestamp;
        return timeAfterRealise > 1000L * 60 * 60 * 24 * 30
                ? toDateTime(timestamp)
                : toHumanTime(timeAfterRealise) + " назад";
    }

    public static String toDateTime(long timestamp) {
        return DATE_TIME_FORMAT.format(timestamp);
    }

    public static String toDate(long timestamp) {
        return DATE_FORMAT.format(timestamp);
    }

}
