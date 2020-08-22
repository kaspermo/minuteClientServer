package com.server.minute;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;

/**
 * Data is a simple data structure, which holds a string object of a hashed date and time.
 * The date and time can be updated to current date and time via setTime().
 *
 * @author Kasper Møller Nielsen
 * @version 1.0
 * @since 2020-21-08
 */

public class Data {

    private static String timeHash;

    /**
     * Returns the timeHash of this Data object
     *
     * @return The string stored in the field timeHash
     */

    public static String getTime() {
        return timeHash;
    }

    /**
     * The method updates timeHash with the current time, hashed in as a String.
     * <p>
     * Local current date and time is found and nanoseconds are omitted from the time.
     * date and time is hashed and timeHash is set as the dateHash and timeHash divided by a space.
     */

    public static void setTime() {
        LocalDateTime ldt = LocalDateTime.now();
        LocalDate ld = ldt.toLocalDate();
        LocalTime lt = ldt.toLocalTime().truncatedTo(ChronoUnit.SECONDS);
        timeHash = ld.hashCode() + " " + lt.hashCode();
    }
}
