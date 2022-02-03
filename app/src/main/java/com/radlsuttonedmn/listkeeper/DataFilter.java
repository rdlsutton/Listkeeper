package com.radlsuttonedmn.listkeeper;

/**
 * Class to define a filter used to determine which list items are displayed
 */

class DataFilter {

    private final String mListName;
    private final String mFieldName;
    private final String mFieldValue;

    // Constructor
    DataFilter(String listName, String fieldName, String fieldValue) {

        mListName = listName;
        mFieldName = fieldName;
        mFieldValue = fieldValue;
    }

    // Getter methods for the filter parameters
    String getListName() { return mListName; }
    String getFieldName() { return mFieldName; }
    String getFieldValue() { return mFieldValue; }
}
