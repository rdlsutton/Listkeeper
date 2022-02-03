package com.radlsuttonedmn.listkeeper;

import android.app.Activity;

import java.util.ArrayList;
import java.util.List;

/**
 *  List the sorting criteria associated with a defined list
 */

class ListSorts {

    private final DBHelper mListDB;
    private final String mListName;
    private List<DataSort> mSorts;

    // Constructor with the list name as a parameter
    ListSorts(Activity activity, String listName) {

        mListName = listName;
        mListDB = DBHelper.getInstance(activity);
        mSorts = new ArrayList<>();
        mListDB.getSorts(mSorts, listName);
    }

    // Returns true if there are any sorting criteria for the current list
    boolean isAnySort() {
        return (mSorts.size() > 0);
    }

    // Add additional sorting criteria for the current list
    void addSort(String fieldName, String sortDirection) {
        DataSort dataSort = new DataSort(mListName, fieldName, sortDirection);
        mSorts.add(dataSort);
    }

    // Remove all sorting criteria for the current list
    void removeAllSorts() {
        mSorts = new ArrayList<>();
        mListDB.deleteSorts(mListName);
    }

    // Store the sorting criteria in the database
    void commitSorts() {
        mListDB.deleteSorts(mListName);
        for (DataSort dataSort : mSorts) {
            mListDB.saveSort(dataSort);
        }
    }

    // Returns true if the specified field is included in the sorting criteria
    boolean isSort(String fieldName) {

        {for (DataSort dataSort : mSorts)
            if (dataSort.getFieldName().equals(fieldName)) {
                return true;
            }
        }
        return false;
    }

    // Return the current sorting direction for the specified field
    String getSortDirection(String fieldName) {

        String direction = "";
        for (DataSort dataSort : mSorts) {
            if (dataSort.getFieldName().equals(fieldName)) {
                direction = dataSort.getDirection();
            }
        }
        return direction;
    }

    // Return an order by clause that can be used in the database select query
    String getOrderBy() {

        if (!isAnySort()) { return null; }
        StringBuilder orderBy = new StringBuilder();
        boolean firstTime = true;

        for (DataSort dataSort : mSorts) {
            if (!firstTime) { orderBy.append(", "); }
            firstTime = false;
            orderBy.append(mListDB.makeFieldName(dataSort.getFieldName()));
            orderBy.append(" ");
            if (dataSort.getDirection().equals("Ascending")) {
                orderBy.append("ASC");
            } else {
                orderBy.append("DESC");
            }
        }
        return orderBy.toString();
    }
}
