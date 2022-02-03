package com.radlsuttonedmn.listkeeper;

/**
 * A single help text item including two text fields and an icon
 */

class HelpTextItem {

    private final String mHelpTextOne;
    private final int mResourceID;
    private final String mHelpTextTwo;

    // Constructor with the help text and an icon as parameters
    HelpTextItem(String textOne, int resourceID, String textTwo) {

        mHelpTextOne = textOne;
        mResourceID = resourceID;
        mHelpTextTwo = textTwo;
    }

    // Getter methods for the help text components
    String getHelpTextOne() { return mHelpTextOne; }
    int getHelpResourceID() { return mResourceID; }
    String getHelpTextTwo() { return mHelpTextTwo; }
}
