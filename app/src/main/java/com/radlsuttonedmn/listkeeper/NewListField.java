package com.radlsuttonedmn.listkeeper;

/**
 * Define a new field to be included in a stored list where the list definition is being edited
 */

class NewListField {

    private final String mNewFieldName;
    private final FieldType mNewFieldType;
    private String mOriginalFieldName;
    private FieldType mOriginalFieldType;
    private String mFieldSorting;

    // Constructor with field names and types
    NewListField(String fieldName, FieldType fieldType) {

        mNewFieldName = fieldName;
        mNewFieldType = fieldType;
        mOriginalFieldName = fieldName;
        mOriginalFieldType = fieldType;
    }

    // Getters and setters for the field names and types
    String getNewFieldName() { return mNewFieldName; }
    FieldType getNewFieldType() { return mNewFieldType; }
    String getOriginalFieldName() { return mOriginalFieldName; }
    FieldType getOriginalFieldType() { return mOriginalFieldType; }
    String getFieldSorting() { return mFieldSorting; }

    void setFieldSorting(String inSort) { mFieldSorting = inSort; }
    void setOriginalFieldName(String inName) { mOriginalFieldName = inName; }
    void setOriginalFieldType(FieldType inType) { mOriginalFieldType = inType; }

    // Returns true when there is a field name change
    boolean nameChange() { return !mOriginalFieldName.equals(mNewFieldName); }

    // Returns true when there is a field type change
    boolean typeChange() { return !(mOriginalFieldType == mNewFieldType); }
}
