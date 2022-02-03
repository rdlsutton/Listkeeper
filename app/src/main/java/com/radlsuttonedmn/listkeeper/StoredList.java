package com.radlsuttonedmn.listkeeper;

import android.app.Activity;
import android.graphics.Bitmap;
import androidx.fragment.app.FragmentActivity;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Store the data for a defined list, and provide data access methods
 */

class StoredList {

    private final FragmentActivity mActivity;
    private final AppActivityState mAppState;
    private final List<DataRecord> mDataRecords;
    private final DBHelper mListDB;
    private final List<Integer> mMaxDataLengths;

    // Constants for setting the image size
    private final int IMAGE_WIDTH = 72;
    private final int IMAGE_HEIGHT = 72;

    // Constructor with the activity as a parameter
    StoredList(Activity activity) {

        mActivity = (FragmentActivity)activity;
        mAppState = new AppActivityState();
        mListDB = DBHelper.getInstance(mActivity);
        mDataRecords = new ArrayList<>();
        mMaxDataLengths = new ArrayList<>();
    }

    // Getter methods for the stored list parameters
    DataRecord getDataRecord(int index) { return mDataRecords.get(index); }
    int getItemCount() { return mDataRecords.size(); }

    // Get a distinct list of the existing values for the data fields to use as suggestions
    String[] getSuggestions(String fieldName) {
        return mListDB.getSuggestions(mAppState.getListName(), fieldName);
    }

    // Get the length of the longest data value for the specified field
    int getDataLength(int index) {
        return mMaxDataLengths.get(index);
    }

    // Decrypt an encrypted list
    private ResultType decryptList(DataEncrypter dataEncrypter, List<String> encryptedData, List<String> decryptedData) {

        String decryptedValue;
        for (String encryptedValue : encryptedData) {
            decryptedValue = dataEncrypter.decrypt(encryptedValue);
            if (decryptedValue == null || decryptedValue.equals("")) {
                return ResultType.DECRYPT_FAIL;
            }
            decryptedData.add(decryptedValue);
        }
        return ResultType.VALID;
    }

    // Fetch the data from the encrypted table, decrypt it and load into the main table
    ResultType decryptData(ListDefinition listDefinition) {

        List<String> encryptedData = new ArrayList<>();
        List<String> decryptedData = new ArrayList<>();
        mListDB.getEncryptedData(mAppState.getListName(), encryptedData);
        DataEncrypter dataEncrypter = new DataEncrypter();
        ResultType results = dataEncrypter.generateEncryptKey();
        if (results != ResultType.VALID) {
            return results;
        } else {

            // Decrypt the data
            results = decryptList(dataEncrypter, encryptedData, decryptedData);
            if (results != ResultType.VALID) {
                return results;
            } else {
                mListDB.loadDecryptedData(mAppState.getListName(), listDefinition, decryptedData);
            }
        }
        return results;
    }

    // Fetch the email address suggestions
    void getEmailSuggestions() {
        EmailStore emailStore = new EmailStore();
        mListDB.getEmailSuggestions(emailStore);
        mAppState.setEmailStore(emailStore);
    }

    // Set up the bitmap store and load the photographs into it
    void fetchBitmaps(FragmentActivity activity, ListDefinition listDefinition) {

        Bitmap bitmap;

        // If returning from an intent, the bitmap store should already exist
        BitmapStore bitmapStore = mAppState.getBitmapStore();
        if (bitmapStore == null) {
            bitmapStore = new BitmapStore();
            mAppState.setBitmapStore(bitmapStore);
        }

        List<String> imageUriStrings = new ArrayList<>();
        mListDB.getPhotoPaths(mAppState.getListName(), listDefinition, imageUriStrings);

        for (String imageUriString : imageUriStrings) {

            // Add any unique bitmaps to the bitmap hash map
            if (!bitmapStore.contains(imageUriString)) {
                bitmap = PhotoAdjuster.getAdjustedBitmap(activity, imageUriString, IMAGE_WIDTH, IMAGE_HEIGHT);
                if (bitmap != null) {
                    bitmapStore.addBitmap(imageUriString, bitmap);
                }
            }
        }
    }

    // Fetch the data values for a list from the database
    ResultType fetchListData(ListDefinition listDefinition, String whereClause, String[] whereArgs, String orderBy) {

        // Initialize the max data lengths array
        for (int i = 0; i < listDefinition.getFieldCount(); i++) {
            mMaxDataLengths.add(0);
        }

        // Fetch the data from the main table
        return mListDB.getListDataValues(mAppState.getListName(), mDataRecords, mMaxDataLengths, listDefinition,
                whereClause, whereArgs, orderBy);
    }

    // Write the encrypted list to the encrypted table
    ResultType writeEncryptedData() {
        return mListDB.writeEncryptedData(mAppState.getListName(), mDataRecords);
    }

    // Extract data records that meet the search criteria
    void applySearch(String searchCriteria, ListDefinition listDefinition) {

        searchCriteria = searchCriteria.toLowerCase();
        if (searchCriteria.contains("*")) {
            if (searchCriteria.length() - searchCriteria.replace("*", "").length() > 1) {
                String searchFor = searchCriteria.substring(1, searchCriteria.length() - 2);
                if (searchFor.contains("*")) {
                    searchIndex(searchCriteria, listDefinition);
                } else {
                    searchContains(searchFor, listDefinition);
                }
            } else {
                if (searchCriteria.endsWith("*")) {
                    searchWith(searchCriteria.replace("*", ""), listDefinition, "starts");
                } else {
                    if (searchCriteria.startsWith("*")) {
                        searchWith(searchCriteria.replace("*", ""), listDefinition, "ends");
                    } else {
                        searchIndex(searchCriteria, listDefinition);
                    }
                }
            }
        } else {
            searchContains(searchCriteria, listDefinition);
        }
    }

    // Search the data records for values that contain the searched for string
    private void searchContains(String searchFor, ListDefinition listDefinition) {

        Iterator<DataRecord> iterator = mDataRecords.iterator();
        while (iterator.hasNext()) {
            DataRecord dataRecord = iterator.next();
            StringBuilder searchIn = new StringBuilder();
            for (int i = 0; i < listDefinition.getFieldCount(); i++) {
                if (listDefinition.getField(i).getFieldType() == FieldType.DATE) {
                    searchIn.append(DateValidator.formatForDisplay(dataRecord.getDataArray().get(i)));
                } else {
                    if (listDefinition.getField(i).getFieldType() == FieldType.TIME) {
                        searchIn.append(TimeValidator.switchFormat(dataRecord.getDataArray().get(i)).toLowerCase());
                    } else {
                        if (listDefinition.getField(i).getFieldType() != FieldType.PHOTO) {
                            searchIn.append(dataRecord.getDataArray().get(i).toLowerCase());
                        }
                    }
                }
            }
            if (!searchIn.toString().contains(searchFor)) {
                iterator.remove();
            }
        }
    }

    // Search the data records for values that start with the searched for string
    private void searchWith(String searchFor, ListDefinition listDefinition, String mode) {

        Iterator<DataRecord> iterator = mDataRecords.iterator();
        boolean isFound;
        String dataValue;

        while (iterator.hasNext()) {
            DataRecord dataRecord = iterator.next();
            isFound = false;
            for (int i = 0; i < listDefinition.getFieldCount(); i++) {
                if (listDefinition.getField(i).getFieldType() == FieldType.DATE) {
                    dataValue = DateValidator.formatForDisplay(dataRecord.getDataArray().get(i));
                } else {
                    if (listDefinition.getField(i).getFieldType() == FieldType.TIME) {
                        dataValue = TimeValidator.switchFormat(dataRecord.getDataArray().get(i)).toLowerCase();
                    } else {
                        if (listDefinition.getField(i).getFieldType() != FieldType.PHOTO) {
                            dataValue = dataRecord.getDataArray().get(i).toLowerCase();
                        } else {
                            dataValue = "";
                        }
                    }
                }
                if (mode.equals("starts") && dataValue.startsWith(searchFor)) {
                    isFound = true;
                }
                if (mode.equals("ends") && dataValue.endsWith(searchFor)) {
                    isFound = true;
                }
            }
            if (!isFound) {
                iterator.remove();
            }
        }
    }

    // Search the data records for values that contain the tokens defined by the wildcard characters in the search criteria
    private void searchIndex(String searchFor, ListDefinition listDefinition) {

        String[] searchTokens  = searchFor.split("\\*");
        String remainingString;
        boolean foundPossible;
        boolean wasFound;
        List<String> dataValues;

        Iterator<DataRecord> iterator = mDataRecords.iterator();
        while (iterator.hasNext()) {
            DataRecord dataRecord = iterator.next();
            dataValues = new ArrayList<>();
            for (int i = 0; i < listDefinition.getFieldCount(); i++) {
                if (listDefinition.getField(i).getFieldType() == FieldType.DATE) {
                    dataValues.add(DateValidator.formatForDisplay(dataRecord.getDataArray().get(i)));
                } else {
                    if (listDefinition.getField(i).getFieldType() == FieldType.TIME) {
                        dataValues.add(TimeValidator.switchFormat(dataRecord.getDataArray().get(i)).toLowerCase());
                    } else {
                        if (listDefinition.getField(i).getFieldType() != FieldType.PHOTO) {
                            dataValues.add(dataRecord.getDataArray().get(i).toLowerCase());
                        } else {
                            dataValues.add("");
                        }
                    }
                }
            }
            wasFound = false;
            for (String dataValue : dataValues) {
                remainingString = dataValue;
                foundPossible = true;
                for (String tokenString : searchTokens) {
                    if (remainingString.contains(tokenString)) {
                        remainingString = remainingString.substring(remainingString.indexOf(tokenString)
                                + tokenString.length());
                    } else {
                        foundPossible = false;
                    }
                }
                if (foundPossible) {
                    wasFound = true;
                }
            }
            if (!wasFound) {
                iterator.remove();
            }
        }
    }

    // Validate and format input data values
    private ResultType validateFormatData(DataRecord dataRecord, ListDefinition listDefinition) {

        String inData;
        boolean nonBlankData = false;

        for (int i = 0; i < listDefinition.getFieldCount(); i++) {

            // Verify that this is not an all blank record
            inData = dataRecord.getDataArray().get(i).trim();
            if (inData.equals("")) {
                dataRecord.setDataValue(i, "");
            } else {
                nonBlankData = true;
                switch (listDefinition.getField(i).getFieldType()) {
                    case DATE:
                    case EMAIL:
                    case PHOTO:
                    case TEXT:
                    case TIME:
                    case URL:
                        dataRecord.setDataValue(i, inData);
                        break;
                    case NUMBER:

                        // Remove leading zeros from number values, leaving zero values as zero
                        dataRecord.setDataValue(i, inData.replaceFirst("^0+(?!$)", ""));
                        break;
                    case PHONE:
                        if (PhoneValidator.isValidPhoneNumber(inData)) {
                            inData = PhoneValidator.getFormattedPhoneNumber(inData);
                            dataRecord.setDataValue(i, inData);
                        } else {
                            return ResultType.INVALID_PHONE;
                        }
                }
            }
        }

        if (!nonBlankData) {
            return ResultType.NO_DATA;
        }
        return ResultType.VALID;
    }

    // Encrypt a data record for an encrypted list
    ResultType encryptValue(DataRecord dataRecord) {

        DataEncrypter dataEncrypter = new DataEncrypter();
        String encryptedValue = dataEncrypter.encrypt(dataRecord.getDataString());
        if (encryptedValue == null || encryptedValue.equals("")) {
            return ResultType.ENCRYPT_FAIL;
        }
        dataRecord.setEncryptedString(encryptedValue);
        return ResultType.VALID;
    }

    // Store a data record in the database
    ResultType saveDataValues(DataRecord dataRecord, ListDefinition listDefinition) {

        EmailStore emailStore = mAppState.getEmailStore();
        ResultType results = validateFormatData(dataRecord, listDefinition);
        if (results != ResultType.VALID) {
            return results;
        }

        // Save the data record to the database
        results = mListDB.saveListDataValues(mAppState.getListName(), listDefinition, dataRecord);
        if (results != ResultType.ITEM_SAVED) {
            return results;
        }

        // Store any email addresses in the data record in the email addresses store
        if (listDefinition.isEmail() && !listDefinition.isEncrypted()) {
            for (int i = 0; i < listDefinition.getFieldCount(); i++) {
                if (listDefinition.getField(i).getFieldType() == FieldType.EMAIL) {
                    emailStore.addEmailAddress(dataRecord.getDataArray().get(i));
                }
            }
        }

        return results;
    }

    // Save an encrypted record in the encrypted table
    ResultType writeEncryptedRecord(DataRecord dataRecord) {

        ResultType results = encryptValue(dataRecord);
        if (results == ResultType.VALID) {
            results = mListDB.saveEncryptedRecord(mAppState.getListName(), dataRecord.getEncryptedString());
        }
        return results;
    }

    // Validate the imported date, number and time values
    private ResultType validateImportedValues(List<String> itemRecord, ListDefinition listDefinition) {

        String inData;
        for (int i = 0; i < itemRecord.size(); i++) {

            // Remove the | character because that is the data separator character
            inData = itemRecord.get(i).trim().replace("|", "");
            if (inData.equals("")) {
                itemRecord.set(i, "");
            } else {
                switch (listDefinition.getField(i).getFieldType()) {
                    case EMAIL:
                    case PHONE:
                    case PHOTO:
                    case TEXT:
                    case URL:
                        itemRecord.set(i, inData);
                        break;
                    case DATE:
                        if (DateValidator.isValidDate(inData)) {
                            inData = DateValidator.getFormattedDate();
                            itemRecord.set(i, DateValidator.formatForSort(inData));
                        } else {
                            return ResultType.INVALID_DATE;
                        }
                        break;
                    case NUMBER:
                        try {
                            Float.parseFloat(inData);
                        } catch (NumberFormatException e) {
                            return ResultType.INVALID_NUMBER;
                        }
                        itemRecord.set(i, inData);
                        break;
                    case TIME:
                        if (TimeValidator.isValidTime(inData)) {
                            inData = TimeValidator.getFormattedTime();
                            itemRecord.set(i, TimeValidator.switchFormat(inData));
                        } else {
                            return ResultType.INVALID_TIME;
                        }
                }
            }
        }
        return ResultType.VALID;
    }

    // Add a record imported from a file to the stored list
    ResultType addImportedRecord(List<String> itemRecord, ListDefinition listDefinition) {

        // Validate the number of fields in the input record
        if (itemRecord.size() != listDefinition.getFieldCount()) {
            return ResultType.STORE_ERROR;
        }

        // Validate and format the imported data values
        ResultType results = validateImportedValues(itemRecord, listDefinition);
        if (results != ResultType.VALID) {
            return results;
        }

        // Save each record to the database
        DataRecord dataRecord = new DataRecord(itemRecord);
        results = saveDataValues(dataRecord, listDefinition);
        if (results == ResultType.ITEM_SAVED) {
            if (listDefinition.isPhoto()) {

                // Connect to the bitmap store
                BitmapStore bitmaps = mAppState.getBitmapStore();

                // Add a new bitmap to the bitmap hash map
                String imageUriString = dataRecord.getDataArray().get(listDefinition.getFieldCount() - 1);
                if (!imageUriString.equals("")) {
                    if (!bitmaps.contains(imageUriString)) {
                        Bitmap bitmap = PhotoAdjuster.getAdjustedBitmap(mActivity, imageUriString,
                            IMAGE_WIDTH, IMAGE_HEIGHT);
                        if (bitmap != null) {
                            bitmaps.addBitmap(imageUriString, bitmap);
                        }
                    }
                }
            }

            // Add the new item to the encrypted table if this is an encrypted list
            if (listDefinition.isEncrypted()) {
                results = writeEncryptedRecord(dataRecord);
            }
        }
        return results;
    }

    // Fetch the _id value for the most recently inserted data record
    int getLastID(String listName) {
        return mListDB.getLastID(listName);
    }

    // Update an encrypted record in the encrypted table
    ResultType updateEncryptedRecord(DataRecord dataRecord) {

        String oldEncryptedValue = dataRecord.getEncryptedString();
        ResultType results = encryptValue(dataRecord);
        if (results == ResultType.VALID) {
            String newEncryptedValue = dataRecord.getEncryptedString();
            results = mListDB.updateEncryptedRecord(mAppState.getListName(), oldEncryptedValue, newEncryptedValue);
        }
        return results;
    }

    // Update a data record in the database
    ResultType updateListItem(DataRecord dataRecord, ListDefinition listDefinition) {

        EmailStore emailStore = mAppState.getEmailStore();
        ResultType results = validateFormatData(dataRecord, listDefinition);
        if (results != ResultType.VALID) {
            return results;
        }

        // Store any email addresses in the data record in the email address store
        if (listDefinition.isEmail() && !listDefinition.isEncrypted()) {
            for (int i = 0; i < listDefinition.getFieldCount(); i++) {
                if (listDefinition.getField(i).getFieldType() == FieldType.EMAIL) {
                    emailStore.addEmailAddress(dataRecord.getDataArray().get(i));
                }
            }
        }

        // Update the item in the database
        return mListDB.updateListItem(mAppState.getListName(), dataRecord, listDefinition);
    }

    // Remove a data record from the database
    ResultType deleteItem(int index, ListDefinition listDefinition) {

        ResultType results = ResultType.VALID;
        mListDB.deleteListItem(mAppState.getListName(), mDataRecords.get(index).getID());
        if (listDefinition.isEncrypted()) {
            results = encryptValue(mDataRecords.get(index));
            if (results == ResultType.VALID) {
                results = mListDB.deleteEncryptedItem(mAppState.getListName(),
                        mDataRecords.get(index).getEncryptedString());
            }
        }

        if (results == ResultType.VALID) {
            mDataRecords.remove(index);
        }
        return results;
    }

    // Encrypt the data in an encrypted list
    ResultType encryptList() {

        String encryptedValue;

        DataEncrypter dataEncrypter = new DataEncrypter();
        for (DataRecord dataRecord : mDataRecords) {
            encryptedValue = dataEncrypter.encrypt(dataRecord.getDataString());
            if (encryptedValue == null || encryptedValue.equals("")) {
                return ResultType.ENCRYPT_FAIL;
            }
            dataRecord.setEncryptedString(encryptedValue);
        }
        return ResultType.VALID;
    }

    // Compose the body of an email for a list that is being emailed
    String composeEmail() {

        StringBuilder emailBody = new StringBuilder();
        for (DataRecord dataRecord : mDataRecords) {
            emailBody.append(dataRecord.getDataString().replace("|", ","));
            emailBody.append("\n");
        }
        return emailBody.toString();
    }
}
