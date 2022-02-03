package com.radlsuttonedmn.listkeeper;

/**
 * Define a sorting criteria used to control the order in which list items are displayed
 */

class DataSort {

    private final String mListName;
    private final String mFieldName;
    private final String mDirection;

    // Constructor with the sorting criteria as parameters
    DataSort(String listName, String fieldName, String direction) {

        mListName = listName;
        mFieldName = fieldName;
        mDirection = direction;
    }

    // Getter and setter methods for the sorting criteria
    String getListName() { return mListName; }
    String getFieldName() { return mFieldName; }
    String getDirection() { return mDirection; }
}
