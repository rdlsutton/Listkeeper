package com.radlsuttonedmn.listkeeper;

/**
 * Validate phone number values
 */

class PhoneValidator {

    // Format the phone number into a standard format
    static String getFormattedPhoneNumber(String phoneNumber) {

        // Format the phone number string
        StringBuilder formattedPhoneNumber = new StringBuilder();
        char c;
        for (int i = phoneNumber.length() - 1; i >= 0; i--) {
            c = phoneNumber.charAt(i);
            if (c >= '0' && c <= '9') {
                formattedPhoneNumber.insert(0, c);
                if (formattedPhoneNumber.length() == 4 || formattedPhoneNumber.length() == 8 ||
                        formattedPhoneNumber.length() == 12 || formattedPhoneNumber.length() == 15 ||
                        formattedPhoneNumber.length() == 18) {
                    formattedPhoneNumber.insert(0, ".");
                }
            }
        }

        // Make sure that the first character in the phone number is not a period
        c = formattedPhoneNumber.charAt(0);
        if (c == '.') {
            formattedPhoneNumber.deleteCharAt(0);
        }

        return formattedPhoneNumber.toString();
    }

    // Returns true when the input string is a valid phone number
    static boolean isValidPhoneNumber(String phoneNumber) {

        // Count the number of numeric digits
        char c;
        int numberCount = 0;
        for (int i = 0; i < phoneNumber.length(); i++) {
            c = phoneNumber.charAt(i);
            if (c >= '0' && c <= '9') {
                numberCount++;
            }
        }

        // Phone number is considered valid if it contains 7 to 20 numeric digits
        return (numberCount >= 7 && numberCount <= 20);
    }
}
