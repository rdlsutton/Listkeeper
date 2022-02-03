package com.radlsuttonedmn.listkeeper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Provides access to the SQLite database
 */

class DBHelper extends SQLiteOpenHelper {

    private static DBHelper dbInstance;

    private static final String DATABASE_NAME = "ListKeeper.db";
    private static final String APP_SETTINGS_TABLE_NAME = "AppSettings";
    private static final String LIST_DEFINITIONS_TABLE_NAME = "ListDefinitions";
    private static final String FILTERS_TABLE_NAME = "ListFilters";
    private static final String SORTS_TABLE_NAME = "ListSorts";
    private static final String DATA_FIELD = "F_Data_Field";
    private static final int DATABASE_VERSION = 1;

    static synchronized DBHelper getInstance(Context context) {
        if (dbInstance == null) {
            dbInstance = new DBHelper(context.getApplicationContext());
        }
        return dbInstance;
    }

    // Constructor
    private DBHelper(Context context) {

        // In the call to super the database version needs to be incremented when sending updates
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Override the parent class's onCreate method
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + APP_SETTINGS_TABLE_NAME + " (_id integer primary key," +
                " ActiveFragment text, SelectedList text, RecordId text)");
        db.execSQL("CREATE TABLE " + LIST_DEFINITIONS_TABLE_NAME + " (_id integer primary key," +
                " ListName text, FieldName text, FieldType text, Salt text, InitVector text)");
        db.execSQL("CREATE TABLE " + FILTERS_TABLE_NAME + " (_id integer primary key," +
                " ListName text, FieldName text, FieldValue text)");
        db.execSQL("CREATE TABLE " + SORTS_TABLE_NAME + " (_id integer primary key," +
                " ListName text, FieldName text, Direction text)");
    }

    // When doing actual upgrades this method will need to preserve the stored data
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //db.execSQL("DROP TABLE IF EXISTS " + APP_SETTINGS_TABLE_NAME);
        //db.execSQL("DROP TABLE IF EXISTS " + LIST_DEFINITIONS_TABLE_NAME);
        //db.execSQL("DROP TABLE IF EXISTS " + FILTERS_TABLE_NAME);
        //db.execSQL("DROP TABLE IF EXISTS " + SORTS_TABLE_NAME);
        //onCreate(db);
    }

    // Fetch the stored application state from the database
    void getAppState() {

        SQLiteDatabase db = this.getReadableDatabase();
        AppActivityState appState = new AppActivityState();

        Cursor c = db.query(APP_SETTINGS_TABLE_NAME, null, null, null, null, null, null);
        if (c.getCount() > 0) {
            c.moveToFirst();
            String activeFragment = c.getString(c.getColumnIndex("ActiveFragment"));
            if (activeFragment == null || activeFragment.equals("")) {
                appState.setActiveFragment(FragmentType.NONE);
            } else {
                appState.setActiveFragment(FragmentType.valueOf(activeFragment.toUpperCase()));
            }
            appState.setListName(c.getString(c.getColumnIndex("SelectedList")));
            appState.setRecordId(c.getString(c.getColumnIndex("RecordId")));
        } else {
            appState.setActiveFragment(FragmentType.NONE);
        }
        c.close();
    }

    // Save the current application state in the database
    void saveAppState(AppActivityState appState) {

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("ActiveFragment", appState.getActiveFragment().toString().toLowerCase());
        contentValues.put("SelectedList", appState.getListName());
        contentValues.put("RecordId", appState.getRecordId());
        if (DatabaseUtils.queryNumEntries(db, APP_SETTINGS_TABLE_NAME) > 0) {
            db.update(APP_SETTINGS_TABLE_NAME, contentValues, null, null);
        } else {
            db.insert(APP_SETTINGS_TABLE_NAME, null, contentValues);
        }
    }

    // Determine if the entered list name creates a duplicate table name
    boolean isDuplicateTable(String listName) {

        boolean found = false;

        // SQLite database table names are case insensitive so we need to compare after converting to lower case
        String newTableName = makeTableName(listName).toLowerCase();
        SQLiteDatabase db = this.getReadableDatabase();

        String existTableName;
        Cursor c = db.query(LIST_DEFINITIONS_TABLE_NAME, new String[]{"ListName"}, null, null, "ListName", null, null);
        if (c.getCount() > 0) {
            c.moveToFirst();
            while (!c.isAfterLast()) {
                existTableName = makeTableName(c.getString(c.getColumnIndex("ListName")).toLowerCase());
                if (newTableName.equals(existTableName)) {
                    found = true;
                }
                c.moveToNext();
            }
        }
        c.close();
        return found;
    }

    // Determine if the entered list name is for an encrypted list
    boolean listEncrypted(String listName) {

        String salt, initVector;
        boolean isEncrypted = false;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.query(LIST_DEFINITIONS_TABLE_NAME, null, "ListName = ?",
                    new String[] {listName}, null, null, null);
        if (c.getCount() > 0) {
            c.moveToFirst();
            while (!c.isAfterLast()) {
                salt = c.getString(c.getColumnIndex("Salt"));
                initVector = c.getString(c.getColumnIndex("InitVector"));

                // If there are values for the salt and init vector then this is an encrypted list
                if (salt != null && initVector != null && !salt.equals("") && !initVector.equals("")) {
                    isEncrypted = true;
                }
                c.moveToNext();
            }
        }
        c.close();
        return isEncrypted;
    }

    // Clear out any decrypted data for any encrypted lists
    void clearDecryptedData() {

        List<String> encryptedLists = new ArrayList<>();
        String salt, initVector, definedList;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor c = db.query(LIST_DEFINITIONS_TABLE_NAME, null, null, null, null, null, null);
        if (c.getCount() > 0) {
            c.moveToFirst();
            while (!c.isAfterLast()) {
                salt = c.getString(c.getColumnIndex("Salt"));
                initVector = c.getString(c.getColumnIndex("InitVector"));

                // If there are values for the salt and init vector then this is an encrypted list
                if (salt != null && initVector != null && !salt.equals("") && !initVector.equals("")) {
                    definedList = c.getString(c.getColumnIndex("ListName"));
                    if (!encryptedLists.contains(definedList)) {
                        encryptedLists.add(definedList);
                    }
                }
                c.moveToNext();
            }
        }
        c.close();

        for (String listName : encryptedLists) {
            db.delete(getListTableName(listName), null, null);
        }
    }

    // Fetch a list of the list names from the database
    void getListNames(List<String> listNames, String searchQuery) {

        String listName;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.query(LIST_DEFINITIONS_TABLE_NAME, new String[]{"ListName"}, null, null, "ListName", null, null);
        if (c.getCount() > 0) {
            c.moveToFirst();
            while (!c.isAfterLast()) {
                listName = c.getString(c.getColumnIndex("ListName"));
                if (searchQuery.equals("") || listName.toLowerCase().contains(searchQuery.toLowerCase())) {
                    listNames.add(listName);
                }
                c.moveToNext();
            }
        }
        c.close();
    }

    // Create a new list table name by adding T_<timestamp> to the list name with spaces and periods
    // converted to underscores
    String makeNewTableName(String listName) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_hhmmss", Locale.US);
        return "T_" + dateFormat.format(new Date()) + "_" + makeTableName(listName);
    }

    // Get the current list table name for the specified list name
    String getListTableName(String listName) {

        String tableName = makeTableName(listName);
        SQLiteDatabase db = this.getReadableDatabase();

        boolean found = false;
        List<String> tableNames = new ArrayList<>();
        String foundTable;

        Cursor c = db.query("sqlite_master", new String[] {"name"},
                        "type = ?", new String[] {"table"}, null, null, null);
        if (c.getCount() > 0) {
            c.moveToFirst();
            while (!c.isAfterLast()) {
                foundTable = c.getString(c.getColumnIndex("name"));
                if (foundTable.length() == tableName.length() + 18) {
                    if (foundTable.contains(tableName)) {
                        found = true;
                        tableNames.add(foundTable);
                    }
                }
                c.moveToNext();
            }
        }
        c.close();

        String listTableName = "";
        if (!found) {
            return "T_" + tableName;
        } else {
            for (String foundName : tableNames) {
                if (foundName.compareTo(listTableName) > 0) {
                    listTableName = foundName;
                }
            }
        }
        return listTableName;
    }

    // Create a database table name from the entered list name by prepending T_ and converting spaces and
    // periods to underscores
    private String makeTableName(String listName) {
        return listName.replace(" ", "_").replace(".", "_");
    }

    // Create a database table name for the encrypted table from the entered list name
    private String makeEncryptedTableName(String listName) {
        return "E_" + makeTableName(listName);
    }

    // Create a database table field name to contain the list data
    String makeFieldName(String fieldName) {
        return "F_" + fieldName.replace(" ", "_").replace(".", "_");
    }

    // Fetch the definition of a list from the database
    void getListDefinition(ListDefinition listDefinition, String listName) {

        ListField field;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.query(LIST_DEFINITIONS_TABLE_NAME, null, "ListName = ?",
                new String[]{listName}, null, null, null);

        if (c.getCount() > 0) {
            c.moveToFirst();
            String salt = c.getString(c.getColumnIndex("Salt"));
            String initVector = c.getString(c.getColumnIndex("InitVector"));

            // If there are values for the salt and init vector then this is an encrypted list
            if (salt != null && initVector != null && !salt.equals("") && !initVector.equals("")) {
                listDefinition.setEncrypted(true);
                DataEncrypter dataEncrypter = new DataEncrypter();
                dataEncrypter.setSalt(salt);
                dataEncrypter.setInitVector(initVector);
            } else {
                listDefinition.setEncrypted(false);
            }

            // Fetch the list of fields for this list
            while (!c.isAfterLast()) {
                field = new ListField(c.getString(c.getColumnIndex("FieldName")),
                        FieldType.valueOf(c.getString(c.getColumnIndex("FieldType")).toUpperCase()));
                listDefinition.addField(field);
                c.moveToNext();
            }
        }
        c.close();
    }

    // Remove encryption from a list that was encrypted
    void clearEncryption(String listName) {

        ContentValues contentValues = new ContentValues();
        contentValues.put("Salt", "");
        contentValues.put("InitVector", "");

        SQLiteDatabase db = this.getWritableDatabase();
        db.update(LIST_DEFINITIONS_TABLE_NAME, contentValues, "ListName = ?", new String[]{listName});
    }

    // Add encryption to a list
    void setEncryption(String listName, DataEncrypter dataEncrypter) {

        ContentValues contentValues = new ContentValues();
        contentValues.put("Salt", dataEncrypter.getSalt());
        contentValues.put("InitVector", dataEncrypter.getInitVector());

        SQLiteDatabase db = this.getWritableDatabase();
        db.update(LIST_DEFINITIONS_TABLE_NAME, contentValues, "ListName = ?", new String[]{listName});
    }

    // Create a database table to contain a new list
    private void createListTable(String listName, ListDefinition listDefinition) {

        SQLiteDatabase db = this.getWritableDatabase();
        StringBuilder createStatement = new StringBuilder("CREATE TABLE IF NOT EXISTS ");
        createStatement.append(makeNewTableName(listName));
        createStatement.append(" (_id INTEGER PRIMARY KEY, ");
        for (int i = 0; i < listDefinition.getFieldCount(); i++) {
            if (i > 0) {
                createStatement.append(", ");
            }
            createStatement.append(makeFieldName(listDefinition.getField(i).getFieldName()));
            if (listDefinition.getField(i).getFieldType() == FieldType.NUMBER) {
                createStatement.append(" NUMERIC");
            } else {
                createStatement.append(" TEXT COLLATE NOCASE");
            }
        }
        createStatement.append(")");
        db.execSQL(createStatement.toString());
    }

    // For an encrypted list create a table to contain the encrypted value
    void createEncryptedTable(String listName) {

        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("CREATE TABLE " + makeEncryptedTableName(listName) +
                " (_id INTEGER PRIMARY KEY, " + DATA_FIELD + " TEXT)");
    }

    // Save the definition of a list to the database
    void saveListDefinition(ListDefinition listDefinition, String listName) {

        ListField listField;
        String salt = null, initVector = null;

        // Create the database table to contain the new list
        createListTable(listName, listDefinition);

        // If the new list is to be encrypted, generate the salt and init vector values
        if (listDefinition.isEncrypted()) {
            DataEncrypter dataEncrypter = new DataEncrypter();
            dataEncrypter.createNew();
            salt = dataEncrypter.getSalt();
            initVector = dataEncrypter.getInitVector();
            createEncryptedTable(listName);
        }

        // Process each of the fields in the new list
        for (int i = 0; i < listDefinition.getFieldCount(); i++) {
            listField = listDefinition.getField(i);
            if (!listField.getFieldName().equals("")) {
                writeDefinitionRecord(listName, listDefinition, listField, salt, initVector);
            }
        }
    }

    // Write the definition of a list field to the database
    private void writeDefinitionRecord(String listName, ListDefinition listDefinition, ListField listField, String salt,
                                       String initVector) {

        ContentValues contentValues = new ContentValues();
        contentValues.put("ListName", listName);
        contentValues.put("FieldName", listField.getFieldName());
        contentValues.put("FieldType", listField.getFieldType().toString().toLowerCase());

        // If the new list is to be encrypted, store the salt and init vector values
        if (listDefinition.isEncrypted()) {
            if (salt != null && initVector != null) {
                contentValues.put("Salt", salt);
                contentValues.put("InitVector", initVector);
            }
        }

        SQLiteDatabase db = this.getWritableDatabase();
        db.insert(LIST_DEFINITIONS_TABLE_NAME, null, contentValues);
    }

    // Update the name of the table that stores the list when the list name has been updated
    void renameTable(String oldListName, String newListName) {

        SQLiteDatabase db = this.getWritableDatabase();
        String currentName = getListTableName(oldListName);
        db.execSQL("ALTER TABLE " + currentName + " RENAME TO " +
                    currentName.substring(0, 18) + makeTableName(newListName));
    }

    // Update the name of the encrypted table that stores the encrypted values
    void renameEncryptedTable(String oldListName, String newListName) {

        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("ALTER TABLE " + makeEncryptedTableName(oldListName) + " RENAME TO "
                    + makeEncryptedTableName(newListName));
    }

    // Update the name of a list
    void updateDefinitionListName(String oldListName, String newListName) {

        ContentValues contentValues = new ContentValues();
        contentValues.put("ListName", newListName);

        SQLiteDatabase db = this.getWritableDatabase();
        db.update(LIST_DEFINITIONS_TABLE_NAME, contentValues, "ListName = ?", new String[]{oldListName});
    }

    // Update the list name value for all the filters associated with a list
    void updateFiltersListName(String oldListName, String newListName) {

        ContentValues contentValues = new ContentValues();
        contentValues.put("ListName", newListName);

        SQLiteDatabase db = this.getWritableDatabase();
        db.update(FILTERS_TABLE_NAME, contentValues, "ListName = ?", new String[]{oldListName});
    }

    // Update the list name value for all the sorting criteria associated with a list
    void updateSortsListName(String oldListName, String newListName) {

        ContentValues contentValues = new ContentValues();
        contentValues.put("ListName", newListName);

        SQLiteDatabase db = this.getWritableDatabase();
        db.update(SORTS_TABLE_NAME, contentValues, "ListName = ?", new String[]{oldListName});
    }

    // Update the definition of a list
    void updateListDefinition(ListDefinition listDefinition, String listName) {

        NewListField listField;
        String salt = null, initVector = null;

        SQLiteDatabase db = this.getWritableDatabase();

        // Fetch the salt and init vector values for the existing list definition so they can be included in the
        // new definition
        Cursor c = db.query(LIST_DEFINITIONS_TABLE_NAME, null, "ListName = ?",
                new String[]{listName}, null, null, null);

        if (c.getCount() > 0) {
            c.moveToFirst();
            salt = c.getString(c.getColumnIndex("Salt"));
            initVector = c.getString(c.getColumnIndex("InitVector"));
        }
        c.close();

        // Delete the old list definition
        db.delete(LIST_DEFINITIONS_TABLE_NAME, "ListName = ?", new String[]{listName});

        // Insert the new list definition
        for (int i = 0; i < listDefinition.getNewCount(); i++) {
            listField = listDefinition.getNewField(i);
            if (!listField.getNewFieldName().equals("")) {
                writeNewDefinitionRecord(listName, listField, salt, initVector);
            }
        }
    }

    // Write the definition of a list field to the database
    private void writeNewDefinitionRecord(String listName, NewListField listField, String salt,
                                          String initVector) {

        ContentValues contentValues = new ContentValues();
        contentValues.put("ListName", listName);
        contentValues.put("FieldName", listField.getNewFieldName());
        contentValues.put("FieldType", listField.getNewFieldType().toString().toLowerCase());
        contentValues.put("Salt", salt);
        contentValues.put("InitVector", initVector);

        SQLiteDatabase db = this.getWritableDatabase();
        db.insert(LIST_DEFINITIONS_TABLE_NAME, null, contentValues);
    }

    // Create an update list table
    void createNewListTable(String newTableName, ListDefinition listDefinition) {

        SQLiteDatabase db = this.getWritableDatabase();
        StringBuilder createStatement = new StringBuilder("CREATE TABLE IF NOT EXISTS ");
        createStatement.append(newTableName);
        createStatement.append(" (_id INTEGER PRIMARY KEY, ");
        boolean firstField = true;
        for (int i = 0; i < listDefinition.getNewCount(); i++) {
            if (!listDefinition.getNewField(i).getNewFieldName().equals("")) {
                if (!firstField) {
                    createStatement.append(", ");
                }
                firstField = false;
                createStatement.append(makeFieldName(listDefinition.getNewField(i).getNewFieldName()));
                if (listDefinition.getNewField(i).getNewFieldType() == FieldType.NUMBER) {
                    createStatement.append(" NUMERIC ");
                } else {
                    createStatement.append(" TEXT COLLATE NOCASE");
                }
            }
        }
        createStatement.append(") ");
        db.execSQL(createStatement.toString());
    }

    // Copy the data from the old list table into the new list table
    void populateNewListTable(String oldTableName, String newTableName, ListDefinition listDefinition) {

        SQLiteDatabase db = this.getWritableDatabase();
        NewListField newField;
        StringBuilder insertStatement = new StringBuilder("INSERT INTO ");
        insertStatement.append(newTableName);
        insertStatement.append(" (");
        boolean firstField = true;
        for (int i = 0; i < listDefinition.getNewCount(); i++) {
            newField = listDefinition.getNewField(i);
            if (!newField.getNewFieldName().equals("") && !newField.getOriginalFieldName().equals("")) {
                if (!listDefinition.dataLossField(newField)) {
                    if (!firstField) {
                        insertStatement.append(", ");
                    }
                    firstField = false;
                    insertStatement.append(makeFieldName(newField.getNewFieldName()));
                }
            }
        }
        insertStatement.append(") SELECT ");
        firstField = true;
        for (int i = 0; i < listDefinition.getNewCount(); i++) {
            newField = listDefinition.getNewField(i);
            if (!newField.getNewFieldName().equals("") && !newField.getOriginalFieldName().equals("")) {
                if (!listDefinition.dataLossField(newField)) {
                    if (!firstField) {
                        insertStatement.append(", ");
                    }
                    firstField = false;
                    insertStatement.append(makeFieldName(newField.getOriginalFieldName()));
                }
            }
        }
        insertStatement.append(" FROM ");
        insertStatement.append(oldTableName);
        db.execSQL(insertStatement.toString());
    }

    // Remove the old list table
    void dropOldListTable(String oldTableName) {

        // Delete the list table
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DROP TABLE IF EXISTS " + oldTableName);
    }

    // Remove the data from the encrypted table
    void clearEncryptedTable(String listName) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(makeEncryptedTableName(listName), null, null);
    }

    // Write the encrypted data to the encrypted table
    ResultType writeEncryptedData(String listName, List<DataRecord> dataRecords) {

        SQLiteDatabase db = this.getWritableDatabase();

        // Write the data to the database
        ContentValues contentValues;
        for (DataRecord dataRecord : dataRecords) {
            contentValues = new ContentValues();
            contentValues.put(DATA_FIELD, dataRecord.getEncryptedString());
            try {
                if (db.insert(makeEncryptedTableName(listName), null, contentValues) < 1) {
                    return ResultType.STORE_ERROR;
                }
            } catch (SQLException e) {
                return ResultType.STORE_ERROR;
            }
        }
        return ResultType.VALID;
    }

    // Determine if there is any encrypted data stored for this list
    boolean isEncryptedData(String listName) {

        SQLiteDatabase db = this.getReadableDatabase();
        return (DatabaseUtils.queryNumEntries(db, makeEncryptedTableName(listName)) > 0);
    }

    // Get the encrypted strings from the encrypted table
    void getEncryptedData(String listName, List<String> encryptedData) {

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.query(makeEncryptedTableName(listName), null, null, null, null, null, null);
        if (c.getCount() > 0) {
            c.moveToFirst();
            while(!c.isAfterLast()) {
                encryptedData.add(c.getString(c.getColumnIndex(DATA_FIELD)));
                c.moveToNext();
            }
        }
        c.close();
    }

    // Load a list that has been decrypted into the main table
    void loadDecryptedData(String listName, ListDefinition listDefinition, List<String> decryptedData) {

        List<String> dataValues;
        ContentValues contentValues;
        SQLiteDatabase db = this.getWritableDatabase();

        for (String decryptedValue : decryptedData) {
            dataValues = new ArrayList<>();
            Collections.addAll(dataValues, decryptedValue.split("\\|", -1));

            contentValues = new ContentValues();
            for (int i = 0; i < listDefinition.getFieldCount(); i++) {
                contentValues.put(makeFieldName(listDefinition.getField(i).getFieldName()), dataValues.get(i));
            }
            db.insert(getListTableName(listName), null, contentValues);
        }
    }

    // Get the file paths for the photos in a photo list
    void getPhotoPaths(String listName, ListDefinition listDefinition, List<String> imagePaths) {

        int columnIndex = listDefinition.getFieldCount();
        SQLiteDatabase db = this.getReadableDatabase();
        String imagePath;

        Cursor c = db.query(getListTableName(listName), null, null, null, null, null, null);
        if (c.getCount() > 0) {
            c.moveToFirst();
            while (!c.isAfterLast()) {
                imagePath = c.getString(columnIndex);
                if (!imagePaths.contains(imagePath)) {
                   imagePaths.add(imagePath);
                }
                c.moveToNext();
            }
        }
        c.close();
    }

    // Fetch all of the data values for a list
    ResultType getListDataValues(String listName, List<DataRecord> dataRecords, List<Integer> dataLengths,
                 ListDefinition listDefinition, String whereClause, String[] whereArgs, String orderBy) {

        DataRecord dataRecord;
        ArrayList<String> dataValues;
        String recordID;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.query(getListTableName(listName), null, whereClause, whereArgs, null, null, orderBy);

        // Verify the layout of the list table
        if (c.getColumnCount() != listDefinition.getFieldCount() + 1) {
            return ResultType.DATA_FAIL;
        }
        for (int i = 0; i < listDefinition.getFieldCount(); i++) {
            if (!c.getColumnName(i + 1).equals(makeFieldName(listDefinition.getField(i).getFieldName()))) {
                return ResultType.DATA_FAIL;
            }
        }

        // Retrieve the list table data
        String dataValue;
        if (c.getCount() > 0) {
            c.moveToFirst();
            while (!c.isAfterLast()) {
                dataValues = new ArrayList<>();

                // The first field selected from the table will be the _id value
                recordID = c.getString(0);
                for (int i = 0; i < listDefinition.getFieldCount(); i++) {
                    dataValue = c.getString(i + 1);
                    if (dataValue == null) {
                        dataValues.add("");
                    } else {
                        dataValues.add(dataValue);

                        // If the data value is the longest yet encountered update the data lengths array
                        if (dataValue.length() > dataLengths.get(i)) {
                            dataLengths.set(i, dataValue.length());
                        }
                    }
                }
                dataRecord = new DataRecord(recordID, dataValues);
                dataRecords.add(dataRecord);
                c.moveToNext();
            }
        }
        c.close();
        return ResultType.VALID;
    }

    // Fetch a distinct list of email addresses to use as suggestions
    void getEmailSuggestions(EmailStore emailStore) {

        // Fetch the stored email addresses
        SQLiteDatabase db = this.getReadableDatabase();
        String listName, fieldName;

        String selection = "FieldType = ? and (Salt = ? or Salt is null) and (InitVector = ? or InitVector is null)";
        Cursor c = db.query(LIST_DEFINITIONS_TABLE_NAME, new String[] {"ListName", "FieldName"},
                    selection, new String[] {"email", "", ""},null, null, null);
        if (c.getCount() > 0) {
            c.moveToFirst();
            while (!c.isAfterLast()) {
                listName = c.getString(c.getColumnIndex("ListName"));
                fieldName = c.getString(c.getColumnIndex("FieldName"));
                processField(db, listName, fieldName, emailStore);
                c.moveToNext();
            }
        }
        c.close();
    }

    // Fetch the values from each email field in each list
    private void processField(SQLiteDatabase db, String listName, String fieldName, EmailStore emailStore) {

        String field = makeFieldName(fieldName);
        Cursor c = db.query(getListTableName(listName), new String[] {field},
                field + " <> ?", new String[] {""}, field, null, null);
        if (c.getCount() > 0) {
            c.moveToFirst();
            while (!c.isAfterLast()) {
                emailStore.addEmailAddress(c.getString(c.getColumnIndex(field)));
                c.moveToNext();
            }
        }
        c.close();
    }

    // Fetch a distinct list of values for a field to use as suggestions
    String[] getSuggestions(String listName, String fieldName) {

        SQLiteDatabase db = this.getReadableDatabase();
        List<String> fieldSuggestions = new ArrayList<>();
        String field = makeFieldName(fieldName);

        // Get the text field values from the main table to use as suggestions
        Cursor c = db.query(getListTableName(listName), new String[]{field}, field + " <> ?",
                new String[] {""}, field, null, null);
        if (c.getCount() > 0) {
            c.moveToFirst();
            while (!c.isAfterLast()) {
                fieldSuggestions.add(c.getString(c.getColumnIndex(field)));
                c.moveToNext();
            }
        }
        c.close();
        return fieldSuggestions.toArray(new String[0]);
    }

    // Check for duplicate items
    private boolean isDuplicateItem(String tableName, ListDefinition listDefinition, DataRecord dataRecord) {

        SQLiteDatabase db = this.getReadableDatabase();
        StringBuilder whereClause = new StringBuilder();
        String[] dataValues = new String[listDefinition.getFieldCount()];
        for (int i = 0; i < listDefinition.getFieldCount(); i++) {
            if (i > 0) {
                whereClause.append(" and ");
            }
            whereClause.append(makeFieldName(listDefinition.getField(i).getFieldName()));
            whereClause.append(" = ?");
            dataValues[i] = dataRecord.getDataArray().get(i);
        }
        return (DatabaseUtils.queryNumEntries(db, tableName, whereClause.toString(), dataValues) > 0);
    }

    // Store the data values for a newly entered data record for a list
    ResultType saveListDataValues(String listName, ListDefinition listDefinition, DataRecord dataRecord) {

        SQLiteDatabase db = this.getWritableDatabase();
        String tableName = getListTableName(listName);

        // Check for duplicate data value
        if (isDuplicateItem(tableName, listDefinition, dataRecord)) {
            return ResultType.DUPLICATE_ITEM;
        }

        // Write the data to the database
        ContentValues contentValues = new ContentValues();
        for (int i = 0; i < listDefinition.getFieldCount(); i++) {
            contentValues.put(makeFieldName(listDefinition.getField(i).getFieldName()), dataRecord.getDataArray().get(i));
        }

        try {
            if (db.insert(tableName, null, contentValues) < 1) {
                return ResultType.STORE_ERROR;
            }
        } catch (SQLException e) {
            return ResultType.STORE_ERROR;
        }

        return ResultType.ITEM_SAVED;
    }

    // Fetch the _id value for the most recently inserted record for a list
    int getLastID(String listName) {

        int lastID = 0;

        SQLiteDatabase db = this.getReadableDatabase();

        Cursor c = db.query(getListTableName(listName), new String[] {"_id"}, null, null, null, null, null);
        if (c.getCount() > 0) {
            c.moveToLast();
            lastID = c.getInt(c.getColumnIndex("_id"));
        }
        c.close();
        return lastID;
    }

    // Store an encrypted string in the encrypted table
    ResultType saveEncryptedRecord(String listName, String encryptedString) {

        SQLiteDatabase db = this.getWritableDatabase();

        // Write the data to the database
        ContentValues contentValues = new ContentValues();
        contentValues.put(DATA_FIELD, encryptedString);
        try {
            if (db.insert(makeEncryptedTableName(listName), null, contentValues) < 1) {
                return ResultType.STORE_ERROR;
            }
        } catch (SQLException e) {
            return ResultType.STORE_ERROR;
        }

        return ResultType.ITEM_SAVED;
    }

    // Update a data record for a list
    ResultType updateListItem(String listName, DataRecord dataRecord, ListDefinition listDefinition) {

        Cursor c;
        SQLiteDatabase db = this.getWritableDatabase();
        String tableName = getListTableName(listName);

        // Check for duplicate data value
        StringBuilder whereClause = new StringBuilder();
        String[] dataValues = new String[listDefinition.getFieldCount()];
        for (int i = 0; i < listDefinition.getFieldCount(); i++) {
            if (i > 0) {
                whereClause.append(" and ");
            }
            whereClause.append(makeFieldName(listDefinition.getField(i).getFieldName()));
            whereClause.append(" = ?");
            dataValues[i] = dataRecord.getDataArray().get(i);
        }
        c = db.query(tableName, new String[]{"_id"}, whereClause.toString(), dataValues, null, null, null);
        if (c.getCount() > 0) {
            c.moveToFirst();
            if (c.getString(c.getColumnIndex("_id")).equals(dataRecord.getID())) {

                // No change to the data, the user may be updating the filters
                return ResultType.ITEM_UPDATED;
            } else {

                // Data has been edited to be a duplicate item, return error
                return ResultType.DUPLICATE_ITEM;
            }
        }
        c.close();

        // Update the stored value in the database
        ContentValues contentValues = new ContentValues();
        for (int i = 0; i < listDefinition.getFieldCount(); i++) {
            contentValues.put(makeFieldName(listDefinition.getField(i).getFieldName()),
                    dataRecord.getDataArray().get(i));
        }
        try {
            if (db.update(tableName, contentValues, "_id = ?", new String[]{dataRecord.getID()}) < 1) {
                return ResultType.STORE_ERROR;
            }
        } catch (SQLException e) {
            return ResultType.STORE_ERROR;
        }
        return ResultType.ITEM_UPDATED;
    }

    // Update an encrypted string in the encrypted table
    ResultType updateEncryptedRecord(String listName, String oldEncryptedValue, String newEncryptedValue) {

        SQLiteDatabase db = this.getWritableDatabase();

        // Write the data to the database
        ContentValues contentValues = new ContentValues();
        contentValues.put(DATA_FIELD, newEncryptedValue);
        try {
            if (db.update(makeEncryptedTableName(listName), contentValues,
                                DATA_FIELD + " = ?", new String[] {oldEncryptedValue}) < 1) {
                return ResultType.STORE_ERROR;
            }
        } catch (SQLException e) {
            return ResultType.STORE_ERROR;
        }

        return ResultType.ITEM_SAVED;
    }

    // Delete a data record from a list
    void deleteListItem(String listName, String rowIndex) {

        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(getListTableName(listName), "_id = ?", new String[] {rowIndex});
    }

    // Delete a data record from the encrypted table
    ResultType deleteEncryptedItem(String listName, String encryptedValue) {

        try {
            SQLiteDatabase db = this.getWritableDatabase();
            db.delete(makeEncryptedTableName(listName), DATA_FIELD + " = ?", new String[]{encryptedValue});
        } catch (Exception e) {
            return ResultType.DATA_FAIL;
        }
        return ResultType.VALID;
    }

    // Remove a encrypted table from the database
    void deleteEncryptedTable(String listName) {

        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DROP TABLE IF EXISTS " + makeEncryptedTableName(listName));
    }

    // Remove a list from the database
    void deleteListTable(String listName) {

        // Delete the list table
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DROP TABLE IF EXISTS " + getListTableName(listName));

        // Delete the list definition
        db.delete(LIST_DEFINITIONS_TABLE_NAME, "ListName = ?", new String[] {listName});
    }

    // Fetch all of the filters associated with a list
    void getFilters(String listName, List<DataFilter> filters) {

        DataFilter newFilter;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.query(FILTERS_TABLE_NAME, null, "ListName = ?", new String[] {listName},
                    null, null, null);

        if (c.getCount() > 0) {
            c.moveToFirst();
            while (!c.isAfterLast()) {
                newFilter = new DataFilter(listName, c.getString(c.getColumnIndex("FieldName")),
                        c.getString(c.getColumnIndex("FieldValue")));
                filters.add(newFilter);
                c.moveToNext();
            }
        }
        c.close();
    }

    // Store a list filter in the database
    void saveFilter(DataFilter dataFilter) {

        ContentValues contentValues = new ContentValues();
        contentValues.put("ListName", dataFilter.getListName());
        contentValues.put("FieldName", dataFilter.getFieldName());
        contentValues.put("FieldValue", dataFilter.getFieldValue());

        SQLiteDatabase db = this.getWritableDatabase();
        db.insert(FILTERS_TABLE_NAME, null, contentValues);
    }

    // Delete all the filters associated with a list
    void deleteFilters(String listName) {

        SQLiteDatabase db = this.getWritableDatabase();
        if (DatabaseUtils.queryNumEntries(db, FILTERS_TABLE_NAME, "ListName = ?", new String[] {listName}) > 0) {
            db.delete(FILTERS_TABLE_NAME, "ListName = ?", new String[] {listName});
        }
    }

    // Fetch the sorting criteria associated with a list
    void getSorts(List<DataSort> sorts, String listName) {

        DataSort newSort;

        SQLiteDatabase db = this.getReadableDatabase();

        Cursor c = db.query(SORTS_TABLE_NAME, null, "ListName = ?", new String[] {listName},null, null, null);
        if (c.getCount() > 0) {
            c.moveToFirst();
            while (!c.isAfterLast()) {
                newSort = new DataSort(listName, c.getString(c.getColumnIndex("FieldName")),
                        c.getString(c.getColumnIndex("Direction")));
                sorts.add(newSort);
                c.moveToNext();
            }
        }
        c.close();
    }

    // Store a sorting criteria in the database
    void saveSort(DataSort dataSort) {

        ContentValues contentValues = new ContentValues();
        contentValues.put("ListName", dataSort.getListName());
        contentValues.put("FieldName", dataSort.getFieldName());
        contentValues.put("Direction", dataSort.getDirection());

        SQLiteDatabase db = this.getWritableDatabase();
        db.insert(SORTS_TABLE_NAME, null, contentValues);
    }

    // Delete all the sorting criteria associated with a list
    void deleteSorts(String listName) {

        SQLiteDatabase db = this.getWritableDatabase();
        if (DatabaseUtils.queryNumEntries(db, SORTS_TABLE_NAME, "ListName = ?", new String[] {listName}) > 0) {
            db.delete(SORTS_TABLE_NAME, "ListName = ?", new String[] {listName});
        }
    }
}
