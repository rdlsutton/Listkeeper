package com.radlsuttonedmn.listkeeper;

import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;
import com.google.android.material.textfield.TextInputLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.Toolbar;
import android.text.InputFilter;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.NumberPicker;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Fragment that allows creating or editing a new list field
 **/

public class EditFieldFragment extends Fragment {

    private FragmentActivity mActivity;
    private AppActivityState mAppState;
    private AppCompatEditText mEditTextFieldName;
    private String mFieldSorting;
    private FieldType mFieldType;
    private ListDefinition mListDefinition;
    private boolean mIsNewField;
    private int mMaxFieldOrder;
    private String mOriginalFieldName;
    private FieldType mOriginalFieldType;
    private NumberPicker mPickerFieldOrder;
    private View mRootView;

    // Override the parent class's onAttach method
    @Override
    public void onAttach(@NonNull Context context) {

        super.onAttach(context);
        mActivity = (FragmentActivity)context;
    }

    // Override the parent class's onCreate method
    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        mAppState = new AppActivityState();

        // Connect to the main toolbar and set the title to the list name
        setHasOptionsMenu(true);
        Toolbar mainToolbar = mActivity.findViewById(R.id.appToolbar);
        if (mainToolbar != null) {
            mainToolbar.setTitle("");
            mainToolbar.setVisibility(View.VISIBLE);
            TextView textViewTitle = mainToolbar.findViewById(R.id.textViewTitle);
            if (textViewTitle != null) {
                if (mAppState.getPriorFragment() == FragmentType.EDIT_DEFINITION) {
                    textViewTitle.setText(mAppState.getListName());
                } else {
                    textViewTitle.setText(getString(R.string.app_name));
                }
            }
        }
    }

    // Override the parent class's onCreateView method
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mAppState.setActiveFragment(FragmentType.EDIT_FIELD);

        // Fetch the list definition
        mListDefinition = new ListDefinition(mActivity);

        // If the field ID is less than the field count, an existing field is being edited
        if (mListDefinition.isNewList()) {
            mIsNewField = (mListDefinition.getFieldId() >= mListDefinition.getFieldCount());
        } else {
            mIsNewField = (mListDefinition.getFieldId() >= mListDefinition.getNewCount());
        }

        mRootView = inflater.inflate(R.layout.edit_field_layout, container, false);

        // Set up the text input box for the field name
        final TextInputLayout textLayoutFieldName = mRootView.findViewById(R.id.textLayoutFieldName);
        mEditTextFieldName = mRootView.findViewById(R.id.editTextFieldName);
        textLayoutFieldName.setHint(getString(R.string.field_name));

        // Set up the field type radio buttons
        final RadioGroup radioFieldType = mRootView.findViewById(R.id.radioFieldType);
        final RadioButton radioDate = mRootView.findViewById(R.id.radioDate);
        final RadioButton radioEmail = mRootView.findViewById(R.id.radioEmail);
        final RadioButton radioNumber = mRootView.findViewById(R.id.radioNumber);
        final RadioButton radioPhone = mRootView.findViewById(R.id.radioPhone);
        final RadioButton radioPhoto = mRootView.findViewById(R.id.radioPhoto);
        final RadioButton radioText = mRootView.findViewById(R.id.radioText);
        final RadioButton radioTime = mRootView.findViewById(R.id.radioTime);
        final RadioButton radioURL = mRootView.findViewById(R.id.radioURL);
        radioFieldType.clearCheck();

        // Set up the field sorting radio buttons
        final RadioGroup radioSorting = mRootView.findViewById(R.id.radioSorting);
        final RadioButton radioOff = mRootView.findViewById(R.id.radioOff);
        final RadioButton radioAscending = mRootView.findViewById(R.id.radioAscending);
        final RadioButton radioDescending = mRootView.findViewById(R.id.radioDescending);
        radioSorting.clearCheck();

        // Set up the field order picker
        mPickerFieldOrder = mRootView.findViewById(R.id.pickerFieldOrder);
        mPickerFieldOrder.setMinValue(1);

        if (mListDefinition.isNewList()) {
            if (mIsNewField) {
                mMaxFieldOrder = mListDefinition.getFieldCount() + 1;
            } else {
                mMaxFieldOrder = mListDefinition.getFieldCount();
            }
        } else {
            if (mIsNewField) {
                mMaxFieldOrder = mListDefinition.getNewCount() + 1;
            } else {
                mMaxFieldOrder = mListDefinition.getNewCount();
            }
        }
        mPickerFieldOrder.setMaxValue(mMaxFieldOrder);
        mPickerFieldOrder.setWrapSelectorWheel(false);

        if (!mIsNewField) {

            // The UI elements need to be populated
            if (mListDefinition.isNewList()) {
                mEditTextFieldName.setText(mListDefinition.getField(mListDefinition.getFieldId()).getFieldName());
                mOriginalFieldName = mListDefinition.getField(mListDefinition.getFieldId()).getFieldName();
                mOriginalFieldType = mListDefinition.getField(mListDefinition.getFieldId()).getFieldType();
                mFieldType = mListDefinition.getField(mListDefinition.getFieldId()).getFieldType();
                mFieldSorting = mListDefinition.getField(mListDefinition.getFieldId()).getFieldSorting();
            } else {
                mEditTextFieldName.setText(mListDefinition.getNewField(mListDefinition.getFieldId()).getNewFieldName());
                mOriginalFieldName = mListDefinition.getNewField(mListDefinition.getFieldId()).getNewFieldName();
                mOriginalFieldType = mListDefinition.getNewField(mListDefinition.getFieldId()).getNewFieldType();
                mFieldType = mListDefinition.getNewField(mListDefinition.getFieldId()).getNewFieldType();
                mFieldSorting = mListDefinition.getNewField(mListDefinition.getFieldId()).getFieldSorting();
            }

            switch (mFieldType) {
                case DATE:
                    radioDate.setChecked(true);
                    break;
                case EMAIL:
                    radioEmail.setChecked(true);
                    break;
                case NUMBER:
                    radioNumber.setChecked(true);
                    break;
                case PHONE:
                    radioPhone.setChecked(true);
                    break;
                case PHOTO:
                    radioPhoto.setChecked(true);
                    break;
                case TEXT:
                    radioText.setChecked(true);
                    break;
                case TIME:
                    radioTime.setChecked(true);
                    break;
                case URL:
                    radioURL.setChecked(true);
            }

            switch (mFieldSorting) {
                case "Off":
                    radioOff.setChecked(true);
                    break;
                case "Ascending":
                    radioAscending.setChecked(true);
                    break;
                case "Descending":
                    radioDescending.setChecked(true);
            }
        } else {
            mOriginalFieldName = "";
            mOriginalFieldType = FieldType.TEXT;

            // Default the field type radio button group to text and field sorting to off
            mFieldType = FieldType.TEXT;
            radioText.setChecked(true);
            mFieldSorting = "Off";
            radioOff.setChecked(true);
        }
        mPickerFieldOrder.setValue(mListDefinition.getFieldId() + 1);

        // Add an input filter to restrict the field name to letters, numbers, spaces, underscores or blanks
        ArrayList<InputFilter> currentInputFilters = new ArrayList<>(Arrays.asList(mEditTextFieldName.getFilters()));
        currentInputFilters.add(0, new InputFilter() {
             public CharSequence filter(CharSequence source, int start, int end, Spanned span, int spanStart, int spanEnd) {

                 // Only keep characters that are letters, numbers, spaces, underscores or blanks
                 StringBuilder builder = new StringBuilder();
                 for (int i = start; i < end; i++) {
                     char c = source.charAt(i);
                     if (Character.isLetterOrDigit(c) || c == '_' || c == ' ' || c == '.') {
                         builder.append(c);
                     }
                 }

                  // If all characters are valid, return null, otherwise only return the filtered characters
                  boolean allCharactersValid = (builder.length() == end - start);
                  return allCharactersValid ? null : builder.toString();
             }
        });

        // Add the new filter to the existing list of filters so that they do not get overridden
        InputFilter[] newInputFilters = currentInputFilters.toArray(new InputFilter[0]);
        mEditTextFieldName.setFilters(newInputFilters);

        // Add an onCheckedChangeListener to the field type radio button group
        radioFieldType.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {

                if (radioDate.isChecked()) {
                    mFieldType = FieldType.DATE;
                }
                if (radioEmail.isChecked()) {
                    mFieldType = FieldType.EMAIL;
                }
                if (radioNumber.isChecked()) {
                    mFieldType = FieldType.NUMBER;
                }
                if (radioPhone.isChecked()) {
                    mFieldType = FieldType.PHONE;
                }
                if (radioPhoto.isChecked()) {
                    mFieldType = FieldType.PHOTO;
                }
                if (radioText.isChecked()) {
                    mFieldType = FieldType.TEXT;
                }
                if (radioTime.isChecked()) {
                    mFieldType = FieldType.TIME;
                }
                if (radioURL.isChecked()) {
                    mFieldType = FieldType.URL;
                }
            }
        });

        // Add an onCheckedChangeListener to the field sorting radio button group
        radioSorting.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {

                if (radioOff.isChecked()) {
                    mFieldSorting = "Off";
                }
                if (radioAscending.isChecked()) {
                    mFieldSorting = "Ascending";
                }
                if (radioDescending.isChecked()) {
                    mFieldSorting = "Descending";
                }
            }
        });
        return mRootView;
    }

    void updateListField() {

        // If there is no entered field name, return with no action taken
        if (mEditTextFieldName.getText() == null || mEditTextFieldName.getText().toString().trim().equals("")) {
            mAppState.showSnackBar(mActivity, mRootView, getString(R.string.enter_field_name));
            return;
        }

        // If the entered field name creates a duplicate field name, show a warning message
        if (!mOriginalFieldName.equals(mEditTextFieldName.getText().toString().trim())) {
            if (mListDefinition.isDuplicateField(mEditTextFieldName.getText().toString().trim())) {
                mAppState.showSnackBar(mActivity, mRootView, getString(R.string.duplicate_field_name));
                return;
            }
        }

       // Set the sorting criteria for the new list field
        if (mFieldType == FieldType.PHOTO) {
            if (!mFieldSorting.equals("Off")) {
                mAppState.showSnackBar(mActivity, mRootView, getString(R.string.photo_sort));
                mFieldSorting = "Off";
                return;
            }
        }

        // Do not allow the creation of a second photo field
        if (mListDefinition.isPhoto() && mFieldType == FieldType.PHOTO && mOriginalFieldType != FieldType.PHOTO) {
            mAppState.showSnackBar(mActivity, mRootView, getString(R.string.one_photo));
            return;
        }

        // Adjust the field order to insure that any photo field is at the end of the list
        int fieldOrder;
        if (mFieldType == FieldType.PHOTO) {

            // Place the photo field at the end of the list
            fieldOrder = mMaxFieldOrder;
        } else {
            if (mListDefinition.isPhoto() && mOriginalFieldType != FieldType.PHOTO) {

                // If this list started as a photo list, and an existing field is not being changed from photo, then
                // insure that the existing photo field remains at that end of the list
                if (mPickerFieldOrder.getValue() >= mMaxFieldOrder) {
                    fieldOrder = mMaxFieldOrder - 1;
                } else {
                    fieldOrder = mPickerFieldOrder.getValue();
                }
            } else {
                fieldOrder = mPickerFieldOrder.getValue();
            }
        }
        // If this is a new list, add a new list field to the list definition
        if (mListDefinition.isNewList()) {

            ListField updatedListField = new ListField(mEditTextFieldName.getText().toString().trim(), mFieldType);
            updatedListField.setFieldSorting(mFieldSorting);

            // Add the new field to the list definition
            if (mIsNewField) {
                if (fieldOrder - 1 == mListDefinition.getFieldId()) {

                    // This is a new field that has not been moved
                    mListDefinition.addField(updatedListField);
                } else {

                    // This is a new field to be placed at a different position
                    mListDefinition.insertField(fieldOrder - 1, updatedListField);
                }
            } else {

                // This is an update to an already entered field
                mListDefinition.updateField(fieldOrder - 1, updatedListField);
            }
        } else {

            // This is an existing list that is being edited
            NewListField updatedNewListField = new NewListField(mEditTextFieldName.getText().toString().trim(), mFieldType);
            updatedNewListField.setFieldSorting(mFieldSorting);
            if (mIsNewField) {
                updatedNewListField.setOriginalFieldName("");
                updatedNewListField.setOriginalFieldType(FieldType.TEXT);
            } else {
                updatedNewListField.setOriginalFieldName(mListDefinition.getNewField(mListDefinition.getFieldId()).
                        getOriginalFieldName());
                updatedNewListField.setOriginalFieldType(mListDefinition.getNewField(mListDefinition.getFieldId()).
                        getOriginalFieldType());
            }

            // Add the new field to the list definition
            if (mIsNewField) {
                if (fieldOrder - 1 == mListDefinition.getFieldId()) {

                    // This is a new field that has not been moved
                    mListDefinition.addField(updatedNewListField);
                } else {

                    // This is a new field to be placed at a different position
                    mListDefinition.insertField(fieldOrder - 1, updatedNewListField);
                }
            } else {

                // This is an update to an already existing field
                mListDefinition.updateField(fieldOrder - 1, updatedNewListField);
            }
        }

        // Return to the prior fragment
        FragmentType priorFragment = mAppState.getPriorFragment();
        mAppState.setPriorFragment(FragmentType.EDIT_FIELD);
        mAppState.switchFragment(mActivity, priorFragment);
    }
}
