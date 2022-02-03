package com.radlsuttonedmn.listkeeper;

import java.util.ArrayList;
import java.util.List;

class EmailStore {

    private final List<String> mEmailAddresses;

    // Constructor that initializes the email store
    EmailStore() {
        mEmailAddresses = new ArrayList<>();
    }

    // Add a new email address to the email address array
    void addEmailAddress(String emailAddress) {
        if (!mEmailAddresses.contains(emailAddress)) {
            mEmailAddresses.add(emailAddress);
        }
    }

    // Fetch the email address suggestions
    List<String> getEmailAddresses() { return mEmailAddresses; }
}
