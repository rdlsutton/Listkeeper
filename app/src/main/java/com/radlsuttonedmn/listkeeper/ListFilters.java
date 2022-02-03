package com.radlsuttonedmn.listkeeper;

import android.app.Activity;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * List the filters associated with a defined list
 */

class ListFilters {

    private List<DataFilter> mFilters;
    private final DBHelper mListDB;
    private final String mListName;
    private List<DataFilter> mNewFilters;
    private List<DataFilter> mRemoveFilters;

    // Constructor with the list name as a parameter
    ListFilters(Activity activity, String listName) {

        mListName = listName;
        mListDB = DBHelper.getInstance(activity);
        mFilters = new ArrayList<>();
        mListDB.getFilters(listName, mFilters);
        mNewFilters = new ArrayList<>();
        mRemoveFilters = new ArrayList<>();
    }

    // Returns true if there are any filters associated with the current list
    boolean isAnyFilter() {
        return (mFilters.size() > 0);
    }

    // Update the field name for a filter when a list definition update occurs
    void renameFilter(String oldFieldName, String newFieldName) {

        DataFilter newFilter;

        for (DataFilter dataFilter : mFilters) {
            if (dataFilter.getFieldName().equals(oldFieldName)) {
                mRemoveFilters.add(dataFilter);
                newFilter = new DataFilter(mListName, newFieldName, dataFilter.getFieldValue());
                mNewFilters.add(newFilter);
            }
        }
    }

    // Associate a new filter with the current list
    void addFilter(String fieldName, String fieldValue) {
        DataFilter dataFilter = new DataFilter(mListName, fieldName, fieldValue);
        mListDB.saveFilter(dataFilter);
    }

    // Remove a filter from the current list
    void removeFilter(String fieldName) {

        for (DataFilter dataFilter : mFilters) {
            if (dataFilter.getFieldName().equals(fieldName)) {
                mRemoveFilters.add(dataFilter);
            }
        }
    }

    // Remove all of the filters from the current list
    void removeAllFilters() {
        mListDB.deleteFilters(mListName);
        mFilters = new ArrayList<>();
    }

    // Update the filters associated with the current list
    void commitFilters() {

        // Remove the filters to be deleted from the filters list
        for (DataFilter removeFilter : mRemoveFilters) {
            Iterator<DataFilter> iterator = mFilters.iterator();
            while (iterator.hasNext()) {
                DataFilter dataFilter = iterator.next();
                if (dataFilter.getFieldName().equals(removeFilter.getFieldName())) {
                    iterator.remove();
                }
            }
        }

        // Add the new filters to the filters list
        mFilters.addAll(mNewFilters);

        // Commit the list of filters to the database
        mListDB.deleteFilters(mListName);
        for (DataFilter dataFilter : mFilters) {
            mListDB.saveFilter(dataFilter);
        }

        // Reset the lists of new and deleted filters
        mNewFilters = new ArrayList<>();
        mRemoveFilters = new ArrayList<>();
    }

    // Returns true if there are any filters for the specified field in the current list
    boolean isFilter(String fieldName) {

        for (DataFilter dataFilter : mFilters) {
            if (dataFilter.getFieldName().equals(fieldName)) {
                return true;
            }
        }
        return false;
    }

    // Return a where clause that can be used in the database select query
    String getWhereClause() {

        if (!isAnyFilter()) { return null; }
        StringBuilder whereClause = new StringBuilder();
        boolean firstTime = true;

        for (DataFilter dataFilter : mFilters) {
            if (!firstTime) { whereClause.append(" and "); }
            firstTime = false;
            whereClause.append(mListDB.makeFieldName(dataFilter.getFieldName()));
            whereClause.append("=?");
        }
        return whereClause.toString();
    }

    // Return the where args for the select query where clause
    String[] getWhereArgs() {

        if (!isAnyFilter()) { return null; }
        List<String> fieldValues = new ArrayList<>();

        for (DataFilter dataFilter : mFilters) {
            fieldValues.add(dataFilter.getFieldValue());
        }
        return fieldValues.toArray(new String[0]);
    }
}
