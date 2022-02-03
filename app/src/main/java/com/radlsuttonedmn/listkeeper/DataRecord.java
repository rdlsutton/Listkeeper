package com.radlsuttonedmn.listkeeper;

import java.util.ArrayList;
import java.util.List;

/**
 * Stores a list item's data as a string, or an encrypted string and as a data array.
 * Combines the item data with its record ID
 */

class DataRecord {

    private final List<String> mDataArray;
    private String mEncryptedString;
    private String mID;

    // Constructor with field count parameter
    DataRecord(int count) {

        mDataArray = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            mDataArray.add("");
        }
    }

    // Constructor with parameters for record ID and a data array
    DataRecord(String ID, List<String> inRecord) {
        mID = ID;
        mDataArray = new ArrayList<>();
        mDataArray.addAll(inRecord);
    }

    // Constructor with a data array parameter
    DataRecord(List<String> inRecord) {
        mDataArray = new ArrayList<>();
        mDataArray.addAll(inRecord);
    }

    // Getter and setter methods for the data record parameters
    List<String> getDataArray() { return mDataArray; }
    String getEncryptedString() { return mEncryptedString; }
    int getFieldCount() { return mDataArray.size(); }
    String getID() { return mID; }

    void setDataValue(int index, String inValue) { mDataArray.set(index, inValue); }
    void setEncryptedString(String inString) { mEncryptedString = inString; }
    void setID(String inID) { mID = inID; }

    // Combine the data values into a pipe delimited string
    String getDataString() {

        // Create a data string from the data array
        StringBuilder dataString = new StringBuilder();

        // Add each field to the data string
        for (int i = 0; i < mDataArray.size(); i++) {
            if (i > 0) {
                dataString.append("|");
            }
            dataString.append(mDataArray.get(i));
        }
        return dataString.toString();
    }
}
