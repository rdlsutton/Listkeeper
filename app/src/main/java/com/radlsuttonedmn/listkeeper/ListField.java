package com.radlsuttonedmn.listkeeper;

/**
 * Combine the field name and field type into one entity
 */

class ListField {

    private final String mFieldName;
    private final FieldType mFieldType;
    private String mFieldSorting;

    // Constructor with the field name and field type as parameters
    ListField(String fieldName, FieldType fieldType) {
        mFieldName = fieldName;
        mFieldType = fieldType;
    }

    // Getter and setter methods for the field name and field type
    String getFieldName() { return mFieldName; }
    FieldType getFieldType() { return mFieldType; }
    String getFieldSorting() { return mFieldSorting; }

    void setFieldSorting(String inSort) { mFieldSorting = inSort; }
}
