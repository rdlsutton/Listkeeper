package com.radlsuttonedmn.listkeeper;

import java.util.Locale;

/**
 * Validate date values brought in during file imports
 */

class DateValidator {

   private static String mFormattedDate = "";

   // Return the date value formatted into a standard format (mm/dd/yyyy)
    static String getFormattedDate() { return mFormattedDate; }

    // Format a date string into a format that can be sorted by a database order by clause
    static String formatForSort(String inDate) {

        if (inDate.equals("")) { return inDate; }
        String[] dateTokens = inDate.split("/");
        return dateTokens[2] + "/" + dateTokens[0] + "/" + dateTokens[1];
    }

    // Format a date string from the database for display on the screen
    static String formatForDisplay(String inDate) {

        if (inDate.equals("")) { return inDate; }
        String[] dateTokens = inDate.split("/");
        return dateTokens[1] + "/" + dateTokens[2] + "/" + dateTokens[0];
    }

    // Validate the entered date value
    static boolean isValidDate(String date) {

        // Verify that the input date is the correct length
        if (date.trim().length() < 6 || date.trim().length() > 10) {
            return false;
        }

        // Verify that the input date has valid separator characters
        if (!date.contains("-") && !date.contains("/")) {
            return false;
        }

        String validDate = date.replace("-", "/");
        String[] dateTokens = validDate.split("/");

        // Verify that the input date contains a month, day and year
        if (dateTokens.length != 3) {
            return false;
        }

        // Verify that the input date elements are numeric
        char c;
        String mm;
        String dd;
        String yyyy;
        int dateNumber;

        for (String token : dateTokens) {
            for (int i = 0; i < token.length(); i++) {
                c = token.charAt(i);
                if (c < '0' || c > '9') {
                    return false;
                }
            }
        }

        // Do not allow mm/yyyy/dd format
        dateNumber = Integer.parseInt(dateTokens[1]);
        if (dateNumber >= 100) {
            return false;
        }

        dateNumber = Integer.parseInt(dateTokens[0]);
        if (dateNumber >= 100) {

            // Input date format is yyyy/MM/dd
            mm = String.format(Locale.US, "%02d", Integer.parseInt(dateTokens[1]));
            dd = String.format(Locale.US, "%02d", Integer.parseInt(dateTokens[2]));
            yyyy = String.format(Locale.US, "%04d", Integer.parseInt(dateTokens[0]));
            mFormattedDate = mm + "/" + dd + "/" + yyyy;
            return true;
        }

        dateNumber = Integer.parseInt(dateTokens[2]);
        if (dateNumber >= 100) {

            // Input date format is MM/dd/yyyy
            mm = String.format(Locale.US, "%02d", Integer.parseInt(dateTokens[0]));
            dd = String.format(Locale.US, "%02d", Integer.parseInt(dateTokens[1]));
            yyyy = String.format(Locale.US, "%04d", Integer.parseInt(dateTokens[2]));
            mFormattedDate = mm + "/" + dd + "/" + yyyy;
            return true;
        }

        // If we are here the input date format is MM/dd/yy
        mm = String.format(Locale.US, "%02d", Integer.parseInt(dateTokens[0]));
        dd = String.format(Locale.US, "%02d", Integer.parseInt(dateTokens[1]));
        yyyy = String.format(Locale.US, "%04d", Integer.parseInt(dateTokens[2]) + 2000);
        mFormattedDate = mm + "/" + dd + "/" + yyyy;
        return true;
    }
}
