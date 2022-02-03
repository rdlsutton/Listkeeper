package com.radlsuttonedmn.listkeeper;

import android.app.Activity;

import java.util.ArrayList;
import java.util.List;

/**
 * Define the fields that compose the definition of a stored list
 */

class ListDefinition {

    private final Activity mActivity;
    private final AppActivityState mAppState;
    private static List<String> mDataLossFields;
    private static int mFieldId;
    private static boolean mIsEmail;
    private static boolean mIsEncrypted;
    private static boolean mIsPhoto;
    private final DBHelper mListDB;
    private static List<ListField> mListFields;
    private static List<NewListField> mNewFields;
    private static boolean mNewList;
    private static String mNewListName;

    // Constructor with the activity as a parameter
    ListDefinition(Activity activity) {
        mActivity = activity;
        mAppState = new AppActivityState();
        mListDB = DBHelper.getInstance(mActivity);
    }

    // Getter and setter methods for the list definition parameters
    int getFieldId() { return mFieldId; }
    ListField getField(int index) {
        return mListFields.get(index);
    }
    NewListField getNewField(int index) { return mNewFields.get(index); }
    boolean isEmail() { return mIsEmail; }
    boolean isEncrypted() { return mIsEncrypted; }
    boolean isPhoto() { return mIsPhoto; }
    boolean isNewList() { return mNewList; }
    String getNewListName() { return mNewListName; }

    void setEncrypted(boolean inFlag) { mIsEncrypted = inFlag; }
    void setFieldId(int inId) { mFieldId = inId; }
    void setNewListName(String inName) { mNewListName = inName; }

    // Fetch the stored list's definition from the database
    void getDefinition() {

        mListFields = new ArrayList<>();

        // Set new list flag to true so that the email and photo field flags will be set based on mListFields
        mNewList = true;
        mListDB.getListDefinition(this, mAppState.getListName());
    }

    // Start a new list definition for a new list
    void startNewDefinition() {

        mListFields = new ArrayList<>();
        mNewList = true;
        mIsEmail = false;
        mIsEncrypted = false;
        mIsPhoto = false;
        mNewListName = "";
    }

    // Return the number of fields in the stored list
    int getFieldCount() {
        return mListFields.size();
    }

    // For list definition updates, return the updated number of fields in the stored list
    int getNewCount() { return mNewFields.size(); }

    // Returns true if a new field for a stored list is a duplicate of an existing field
    boolean isDuplicateField(String newFieldName) {

        // In SQLite field names are case insensitive so the comparison needs to be done in all lower case
        newFieldName = newFieldName.replace(" ", "_").replace(".", "_").toLowerCase();
        String existFieldName;
        if (mNewList) {
            for (ListField listField : mListFields) {
                existFieldName = listField.getFieldName().
                                        replace(" ", "_").replace(".", "_").toLowerCase();
                if (existFieldName.equals(newFieldName)) {
                    return true;
                }
            }
        } else {
            for (NewListField listField : mNewFields) {
                existFieldName = listField.getNewFieldName().
                        replace(" ", "_").replace(".", "_").toLowerCase();
                if (existFieldName.equals(newFieldName)) {
                    return true;
                }
            }
        }
        return false;
    }

    // Set the flag variables that indicate if the list has email or photo fields
    private void setEmailAndPhotoFlags() {

        mIsEmail = false;
        mIsPhoto = false;
        if (mNewList) {
            for (ListField listField : mListFields) {
                if (listField.getFieldType() == FieldType.EMAIL) {
                    mIsEmail = true;
                }
                if (listField.getFieldType() == FieldType.PHOTO) {
                    mIsPhoto = true;
                }
            }
        } else {
            for (NewListField listField : mNewFields) {
                if (listField.getNewFieldType() == FieldType.EMAIL) {
                    mIsEmail = true;
                }
                if (listField.getNewFieldType() == FieldType.PHOTO) {
                    mIsPhoto = true;
                }
            }
        }
    }

    // Add an additional field to the new definition of a stored list
    void addField(ListField newField) {
        mListFields.add(newField);
        setEmailAndPhotoFlags();
    }

    // Add an additional field to the edit definition of a stored list
    void addField(NewListField newField) {
        mNewFields.add(newField);
        setEmailAndPhotoFlags();
    }

    // Insert a new field into a location in the new list definition
    void insertField(int index, ListField newField) {
        mListFields.add(index, newField);
        setEmailAndPhotoFlags();
    }

    // Insert a new field into a location in the edit list definition
    void insertField(int index, NewListField newField) {
        mNewFields.add(index, newField);
        setEmailAndPhotoFlags();
    }

    // Update an existing field in a new list definition with a new field definition
    void updateField(int index, ListField newField) {
        mListFields.remove(mFieldId);
        mListFields.add(index, newField);
        setEmailAndPhotoFlags();
    }

    // Update an existing field in an edit list definition with a new field definition
    void updateField(int index, NewListField newField) {
        mNewFields.remove(mFieldId);
        mNewFields.add(index, newField);
        setEmailAndPhotoFlags();
    }

    // Remove a field from a stored list's definition
    void deleteField(int index) {
        if (mNewList) {
            mListFields.remove(index);
        } else {
            if (!mNewFields.get(index).getOriginalFieldName().equals("")) {
                mDataLossFields.add(mNewFields.get(index).getOriginalFieldName());
            }
            mNewFields.remove(index);
        }
        setEmailAndPhotoFlags();
    }

    // Store the definition of a list in the database
    ResultType saveDefinition(String newListName) {

        // Validate that the entered list name does not create a duplicate table name
        if (mListDB.isDuplicateTable(newListName)) {
            return ResultType.DUPLICATE_TABLE;
        }

        // Save the new list definition to the database
        mListDB.saveListDefinition(this, newListName);

        // Disable sorting on any photo fields
        if (mIsPhoto) {
            for (ListField listField : mListFields) {
                if (listField.getFieldType() == FieldType.PHOTO) {
                    listField.setFieldSorting("Off");
                }
            }
        }

        // Save the selected sorting criteria to the database
        ListSorts listSorts = new ListSorts(mActivity, newListName);
        listSorts.removeAllSorts();
        for (ListField listField : mListFields) {
            if (!listField.getFieldSorting().equals("Off")) {
                listSorts.addSort(listField.getFieldName(), listField.getFieldSorting());
            }
        }
        listSorts.commitSorts();

        return ResultType.VALID;
    }

    // Initialize the new definition for an existing stored list where the definition is being updated
    void createEditDefinition() {

        NewListField newField;
        ListSorts listSorts = new ListSorts(mActivity, mAppState.getListName());

        mNewList = false;
        mNewListName = mAppState.getListName();
        mDataLossFields = new ArrayList<>();

        // Create an array that will list the fields in the updated definition
        mNewFields = new ArrayList<>();
        for (ListField listField : mListFields) {
            newField = new NewListField(listField.getFieldName(), listField.getFieldType());
            if (listSorts.isSort(listField.getFieldName())) {
                newField.setFieldSorting(listSorts.getSortDirection(listField.getFieldName()));
            } else {
                newField.setFieldSorting("Off");
            }
            mNewFields.add(newField);
        }
    }

    // Returns true if the update of a list's definition results in the loss of data for any fields
    boolean isDataLoss() { return (mDataLossFields.size() > 0); }

    // Validate the proposed updates to a stored list's definition
    ResultType validateUpdate(String newListName, boolean switchEncryption) {

        // If the list name is changed, do the duplicate table check
        if (!newListName.equals(mAppState.getListName())) {
            if (mListDB.isDuplicateTable(newListName)) {
                return ResultType.DUPLICATE_TABLE;
            }
        }

        // If there are no fields left, cancel the update
        if (mNewFields.size() < 1) {
            return ResultType.NO_FIELDS;
        }

        // Do not allow photographs in an encrypted list
        if (mIsEncrypted && !switchEncryption || !mIsEncrypted && switchEncryption) {
            if (mIsPhoto) {
                return ResultType.ENCRYPT_PHOTO;
            }
        }

        // Identify the field type changes that will cause data losses
        for (NewListField listField : mNewFields) {
            if (listField.typeChange() && !listField.getOriginalFieldName().equals("")) {
                switch (listField.getOriginalFieldType()) {
                    case PHOTO:
                    case TEXT:
                        mDataLossFields.add(listField.getOriginalFieldName());
                        break;
                    case DATE:
                    case EMAIL:
                    case NUMBER:
                    case PHONE:
                    case TIME:
                    case URL:
                        if (listField.getNewFieldType() != FieldType.TEXT) {
                            mDataLossFields.add(listField.getOriginalFieldName());
                        }
                }
            }
        }

        // Return field change if there are any data loss fields
        if (isDataLoss()) {
            return ResultType.FIELD_CHANGE;
        }

        int index = 0;
        for (NewListField listField : mNewFields) {

            // Return field change if there are any field name or type changes
            if (listField.nameChange() || listField.typeChange()) {
                return ResultType.FIELD_CHANGE;
            }

            // Return field change if there are any field position changes
            if (index != getPriorIndex(listField)) {
                return ResultType.FIELD_CHANGE;
            }
            index++;
        }

        return ResultType.NO_CHANGE;
    }

    // Returns a string that lists the fields that will lose data in the list definition update
    String getDataLossList() {

        StringBuilder message = new StringBuilder();
        boolean firstTime = true;
        for (String lossField : mDataLossFields) {
            if (!firstTime) {
                message.append(", ");
            }
            message.append(lossField);
            firstTime = false;
        }
        return message.toString();
    }

    // Update the definition for a stored list
    ResultType updateDefinition(String newListName, boolean fieldChange, boolean switchEncryption) {

        ResultType results = ResultType.VALID;

        StoredList storedList = new StoredList(mActivity);

        // If the name of the list is being updated, call the methods needed to update the list name
        if (!newListName.equals(mAppState.getListName())) {
            mListDB.renameTable(mAppState.getListName(), newListName);
            if (mIsEncrypted) {
                mListDB.renameEncryptedTable(mAppState.getListName(), newListName);
            }
            mListDB.updateDefinitionListName(mAppState.getListName(), newListName);
            mListDB.updateFiltersListName(mAppState.getListName(), newListName);
            mListDB.updateSortsListName(mAppState.getListName(), newListName);
            mAppState.setListName(newListName);
        }

        // If the order, type or name of any of the fields in a list is being updated, call the methods needed to
        // complete the definition update
        if (fieldChange) {

            // Reconfigure the list table, update and reload the data
            String oldTableName = mListDB.getListTableName(newListName);
            String newTableName = mListDB.makeNewTableName(newListName);
            mListDB.createNewListTable(newTableName, this);
            mListDB.populateNewListTable(oldTableName, newTableName, this);
            mListDB.dropOldListTable(oldTableName);
            mListDB.updateListDefinition(this, newListName);
        }

        // If the list is encrypted and the encryption is being removed
        if (mIsEncrypted && switchEncryption) {

            // Remove the encrypted table and clear the encryption setting
            mListDB.deleteEncryptedTable(mAppState.getListName());
            mListDB.clearEncryption(mAppState.getListName());
        }

        // If the list is not encrypted and encryption is being added
        if (!mIsEncrypted && switchEncryption) {

            // Refresh the list definition to the new configuration
            getDefinition();

            results = storedList.fetchListData(this, null, null, null);
            if (results == ResultType.VALID) {

                // Encrypt the data
                DataEncrypter dataEncrypter = new DataEncrypter();
                dataEncrypter.createNew();
                results = dataEncrypter.generateEncryptKey();
                if (results == ResultType.VALID) {
                    results = storedList.encryptList();
                }
                if (results == ResultType.VALID) {
                    mListDB.createEncryptedTable(mAppState.getListName());
                    results = storedList.writeEncryptedData();
                    mListDB.setEncryption(mAppState.getListName(), dataEncrypter);
                }
            }
        }

        // If the list is encrypted and encryption is being retained
        if (mIsEncrypted && !switchEncryption) {

            // If there was a field change the encrypted data will need to be refreshed
            if (fieldChange) {

                // Clear the encrypt table
                mListDB.clearEncryptedTable(mAppState.getListName());

                // Refresh the list definition to the new configuration
                getDefinition();
                results = storedList.fetchListData(this, null, null, null);
                if (results == ResultType.VALID) {
                    // Encrypt the data
                    results = storedList.encryptList();
                    if (results == ResultType.VALID) {
                        results = storedList.writeEncryptedData();
                    }
                }
            }
        }

        // Update the list filters to conform to the new list definition
        updateListFilters();

        return results;
    }

    // Return true if the update of a list field's data type causes a loss of data
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    boolean dataLossField(NewListField newField) {

        for (String lossField : mDataLossFields) {
            if (lossField.equals(newField.getOriginalFieldName())) {
                return true;
            }
        }
        return false;
    }

    // Returns the original location of a list field in the list's field order
    private int getPriorIndex(NewListField newField) {

        int priorIndex = 0;
        for (ListField existField : mListFields) {
            if (existField.getFieldName().equals(newField.getOriginalFieldName())) {
                return priorIndex;
            }
            priorIndex++;
        }
        return priorIndex;
    }

    // Update the list's sorting criteria in response to a list definition update
    void updateListSorts(String newListName) {

        // Disable sorting on any photo fields
        if (mIsPhoto) {
            for (NewListField listField : mNewFields) {
                if (listField.getNewFieldType() == FieldType.PHOTO) {
                    listField.setFieldSorting("Off");
                }
            }
        }

        // Save the selected sorting criteria to the database
        ListSorts listSorts = new ListSorts(mActivity, newListName);
        listSorts.removeAllSorts();
        for (NewListField listField : mNewFields) {
            if (!listField.getFieldSorting().equals("Off")) {
                listSorts.addSort(listField.getNewFieldName(), listField.getFieldSorting());
            }
        }
        listSorts.commitSorts();
    }

    // Update the associated filters for a list in response to a list definition update
    private void updateListFilters() {

        ListFilters filters = new ListFilters(mActivity, mAppState.getListName());
        if (filters.isAnyFilter()) {

            // Delete the filters for any fields on the data loss list
            for (String lossField : mDataLossFields) {
                if (filters.isFilter(lossField)) {
                    filters.removeFilter(lossField);
                }

            }

            // Update any filter where the field has been renamed
            for (NewListField newField : mNewFields) {
                if (newField.nameChange()) {
                    if (filters.isFilter(newField.getOriginalFieldName())) {
                        filters.renameFilter(newField.getOriginalFieldName(), newField.getNewFieldName());
                    }
                }
            }
            filters.commitFilters();
        }
    }
}
