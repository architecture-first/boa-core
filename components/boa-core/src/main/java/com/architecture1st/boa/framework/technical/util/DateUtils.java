package com.architecture1st.boa.framework.technical.util;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.Instant;

/**
 * Utility date methods
 */
public class DateUtils {
    private static final int SECOND_MILLIS = 1000;
    private static final int MINUTE_MILLIS = 60 * SECOND_MILLIS;
    private static final int HOUR_MILLIS = 60 * MINUTE_MILLIS;
    private static final int DAY_MILLIS = 24 * HOUR_MILLIS;

    /**
     * Append today's date to a title
     * @param title
     * @return
     */
    public static String appendDaily(String title) {
        String template = "%s:%s";
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("uuuu-MM-dd");
        var date = ZonedDateTime.now(ZoneId.of("GMT"));
        title = String.format(template, title, dtf.format(date));
        return title;
    }

    /**
     * Return a current timestamp
     * @return
     */
    public static String createTimestamp() {
        Instant now = Instant.now();
        return DateTimeFormatter.ISO_INSTANT.format(now);
    }

    /**
     * Convert the ISO compatible date to ta timestamp
     * @param isoString
     * @return
     */
    public static Instant stringToTimestamp(String isoString) {
        return Instant.parse(isoString);
    }

    /**
     * Returns how long ago something has happened
     * @param time
     * @return
     */
    public static String formatTimeAgo(long time) {
        if (time < 1000000000000L) {
            time *= 1000;
        }

        long now = System.currentTimeMillis();
        if (time > now || time <= 0) {
            return null;
        }

        final long diff = now - time;
        if (diff < MINUTE_MILLIS) {
            return "just now";
        } else if (diff < 2 * MINUTE_MILLIS) {
            return "a minute ago";
        } else if (diff < 50 * MINUTE_MILLIS) {
            return diff / MINUTE_MILLIS + " minutes ago";
        } else if (diff < 90 * MINUTE_MILLIS) {
            return "an hour ago";
        } else if (diff < 24 * HOUR_MILLIS) {
            return diff / HOUR_MILLIS + " hours ago";
        } else if (diff < 48 * HOUR_MILLIS) {
            return "yesterday";
        } else {
            return diff / DAY_MILLIS + " days ago";
        }
    }
}
