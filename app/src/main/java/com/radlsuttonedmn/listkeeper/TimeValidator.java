package com.radlsuttonedmn.listkeeper;

import java.util.Locale;

/**
 * Validate the time values brought in during file imports
 */

class TimeValidator {

    private static String mFormattedTime = "";

    // Return the time value formatted for display
    static String getFormattedTime() { return mFormattedTime; }

    // Switch a time format between database format and display format
    static String switchFormat(String inTime) {

        if (inTime.equals("")) { return inTime; }
        String[] timeTokens = inTime.split(" ");
        return timeTokens[1] + " " + timeTokens[0];
    }

    // Return true if the input value is a valid time value
    static boolean isValidTime(String time) {

        boolean AMIndicator = false;
        boolean PMIndicator = false;

        // Verify that the input time is the correct length
        if (time.length() < 3 || time.length() > 8) {
            return false;
        }

        // Verify that the input time contains valid separator characters
        if (!time.contains(":")) {
            return false;
        }

        // Check for AM or PM
        String hourMin;

        if (time.trim().endsWith("AM")) {
            AMIndicator = true;
            hourMin = time.replace("AM", "").trim();
        } else {
            if (time.trim().endsWith("PM")) {
                PMIndicator = true;
                hourMin = time.replace("PM", "").trim();
            } else {
                hourMin = time.trim();
            }
        }

        String[] timeTokens = hourMin.split(":");

        // Verify that the input time contains hours and minutes
        if (timeTokens.length != 2) {
            return false;
        }

        // Verify that the time elements are numeric
        char c;
        for (String token : timeTokens) {
            for (int i = 0; i < token.length(); i++) {
                c = token.charAt(i);
                if (c < '0' || c > '9') {
                    return false;
                }
            }
        }

        // Verify that the minutes value is within the valid range
        int minutes = Integer.parseInt(timeTokens[1]);
        if (minutes > 59) {
            return false;
        }

        // Verify that the hours value is within the valid range
        int hours = Integer.parseInt(timeTokens[0]);
        if (AMIndicator || PMIndicator) {
            if (hours < 1 || hours > 12) {
                return false;
            }
        } else {
            if (hours < 1 || hours > 24) {
                return false;
            }
        }

        // Time value is valid, format the time value string
        final String mFormattedTime = String.format(Locale.US, "%02d", hours) + ":" +
                String.format(Locale.US, "%02d", minutes) + " AM";
        if (AMIndicator) {
            TimeValidator.mFormattedTime = mFormattedTime;
            return true;
        }
        if (PMIndicator) {
            TimeValidator.mFormattedTime = String.format(Locale.US, "%02d", hours) + ":" + String.format(Locale.US, "%02d", minutes)
                    + " PM";
            return true;
        }
        if (hours > 12) {
            hours -= 12;
            TimeValidator.mFormattedTime = String.format(Locale.US, "%02d", hours) + ":" + String.format(Locale.US, "%02d", minutes)
                    + " PM";
        } else {
            TimeValidator.mFormattedTime = mFormattedTime;
        }
        return true;
    }
}
