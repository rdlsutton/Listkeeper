package com.radlsuttonedmn.listkeeper;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import com.google.android.material.textfield.TextInputLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.core.content.ContextCompat;
import androidx.appcompat.widget.Toolbar;
import android.text.InputFilter;
import android.text.InputType;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Enter a new item into a stored list
 */

public class ItemFragment extends Fragment {

    private FragmentActivity mActivity;
    private AppActivityState mAppState;
    private DataRecord mDataRecord;
    private ListFilters mFilters;
    private StoredList mList;
    private ListDefinition mListDefinition;
    private View mRootView;

    // Lists for the UI fields so that the entered values can be read when saving the edited item
    private final List<AutoCompleteTextView> mAutoCompleteTextViewFields = new ArrayList<>();
    private final List<OnTouchEditText> mOnTouchEditTextFields = new ArrayList<>();
    private final List<androidx.appcompat.widget.SwitchCompat> mFilterSwitches = new ArrayList<>();
    private String mImageUriString = "";

    // Constants for setting the image size
    private final int IMAGE_WIDTH = 72;
    private final int IMAGE_HEIGHT = 72;

    // Constant for the photo selection intent
    private final int NEW_PHOTO = 1;

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
                textViewTitle.setText(mAppState.getListName());
            }
        }
    }

    // Override the parent class's onCreateView method
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mList = new StoredList(mActivity);

        // Fetch the list definition
        mListDefinition = new ListDefinition(mActivity);
        mListDefinition.getDefinition();
        mFilters = new ListFilters(mActivity, mAppState.getListName());

        mRootView = inflater.inflate(R.layout.item_layout, container, false);

        // Create a new instance of DataRecord so that the original record is not updated until after the updates have been validated
        if (createNewDataRecord(mListDefinition) != ResultType.VALID) {
            mAppState.showSnackBar(mActivity, mRootView, getString(R.string.encrypt_fail));
        }

        // Add the main linear layout to the activity layout
        LinearLayout itemFieldsLayout = mRootView.findViewById(R.id.layoutItemFields);

        // Add a view in the main linear layout for each list field
        ListField listField;
        LinearLayout itemLayout;

        createHeaderRow(itemFieldsLayout);

        for (int i = 0; i < mListDefinition.getFieldCount(); i++) {

            // Get each field from the list definition
            listField = mListDefinition.getField(i);

            // Set up a horizontal linear layout for each list field
            itemLayout = new LinearLayout(mActivity);
            LinearLayout.LayoutParams itemLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            itemLayoutParams.setMargins(getPixels(5), getPixels(4), getPixels(5),
                        getPixels(4));
            itemLayout.setLayoutParams(itemLayoutParams);
            itemLayout.setOrientation(LinearLayout.HORIZONTAL);

            // Set up the text view to display the field name
            createFieldNameTextView(itemLayout, listField);

            // Set up an input field specific to each type of field
            switch (listField.getFieldType()) {
                case DATE:
                    createDateInputField(i, itemLayout);
                    break;
                case EMAIL:
                    createEmailInputField(i, itemLayout);
                    break;
                case NUMBER:
                    createNumberInputField(i, itemLayout);
                    break;
                case PHONE:
                    createPhoneInputField(i, itemLayout);
                    break;
                case PHOTO:
                    createPhotoInputField(i, itemLayout);
                    break;
                case TEXT:
                    createTextInputField(i, listField.getFieldName(), itemLayout);
                    break;
                case TIME:
                    createTimeInputField(i, itemLayout);
                    break;
                case URL:
                    createURLInputField(i, itemLayout);
            }

            // Add a phone icon for phone fields when an item is being edited
            if (mAppState.getPhoneAvailable()) {
                if (mAppState.getActiveFragment() == FragmentType.EDIT_ITEM &&
                                            listField.getFieldType() == FieldType.PHONE) {
                    addPhoneIcon(i, itemLayout);
                }
            }

            // Add a browser icon for URL fields when an item is being edited
            if (mAppState.getActiveFragment() == FragmentType.EDIT_ITEM && listField.getFieldType() == FieldType.URL) {
                addBrowserIcon(i, itemLayout);
            }

            // Add a filter switch to the item layout for non-photo fields
            if (listField.getFieldType() != FieldType.PHOTO) {
                addFilterSwitch(listField, mListDefinition, itemLayout, mFilters);
            }

            // Add each new field to the main scrollable layout
            itemFieldsLayout.addView(itemLayout);

            // Add a separator line after each field view
            addSeparator(itemFieldsLayout);
        }
        return mRootView;
    }

    // Create a new data record
    private ResultType createNewDataRecord(ListDefinition listDefinition) {

        ResultType results = ResultType.VALID;
        mDataRecord = new DataRecord(listDefinition.getFieldCount());
        if (mAppState.getDataRecord() != null) {

            // For an existing item, copy the data from the data record saved in AppActivityState
            mDataRecord.setID(mAppState.getDataRecord().getID());
            for (int i = 0; i < listDefinition.getFieldCount(); i++) {
                mDataRecord.setDataValue(i, mAppState.getDataRecord().getDataArray().get(i));
            }
            if  (listDefinition.isEncrypted()) {
                results = mList.encryptValue(mDataRecord);
            }
        }
        return results;
    }

    // Convert DP to pixels
    private int getPixels(int dimension) {
        return Math.round(dimension * getResources().getDisplayMetrics().density);
    }

    // Add a text view to create a header row
    private void createHeaderRow(LinearLayout itemLayout) {

        TextView textViewHeader = new TextView(mActivity);
        LinearLayout.LayoutParams textViewParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        textViewHeader.setLayoutParams(textViewParams);
        textViewHeader.setPadding(getPixels(10), 0, getPixels(10), 0);
        textViewHeader.setGravity(Gravity.END);
        textViewHeader.setTextSize(16);
        textViewHeader.setTextColor(ContextCompat.getColor(mActivity, R.color.colorBlack));
        textViewHeader.setBackgroundColor(ContextCompat.getColor(mActivity, R.color.colorPrimary));
        textViewHeader.setText(getString(R.string.filter));
        itemLayout.addView(textViewHeader);
    }

    private void createFieldNameTextView(LinearLayout itemLayout, ListField listField) {

        TextView textViewFieldName = new TextView(mActivity);
        LinearLayout.LayoutParams textViewParams = new LinearLayout.LayoutParams(getPixels(100),
                LinearLayout.LayoutParams.WRAP_CONTENT);
        textViewParams.setMargins(getPixels(5), getPixels(5), 0, getPixels(5));
        textViewParams.gravity = Gravity.CENTER;
        textViewFieldName.setLayoutParams(textViewParams);
        textViewFieldName.setTextSize(16);
        textViewFieldName.setTextColor(ContextCompat.getColor(mActivity, R.color.colorBlack));

        // Populate the text view with the field name
        textViewFieldName.setText(listField.getFieldName());
        itemLayout.addView(textViewFieldName);
    }

    // Create an AutoCompleteTextView for the field value
    private void createAutoCompleteTextView(AutoCompleteTextView textViewFieldValue, LinearLayout itemLayout) {

        // Create an AutoCompleteTextView to capture the edit input value
        LinearLayout.LayoutParams textViewParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        textViewFieldValue.setLayoutParams(textViewParams);
        textViewFieldValue.setBackground(ContextCompat.getDrawable(mActivity, R.drawable.edit_text_underline_white));
        textViewFieldValue.setInputType(InputType.TYPE_CLASS_TEXT);
        textViewFieldValue.setTextSize(16);
        textViewFieldValue.setTextColor(ContextCompat.getColor(mActivity, R.color.colorBlack));
        textViewFieldValue.setFilters(new InputFilter[] { new InputFilter.LengthFilter(80)});
        textViewFieldValue.setImeOptions(EditorInfo.IME_ACTION_DONE | EditorInfo.IME_FLAG_NO_EXTRACT_UI);

        // Create a TextInputLayout to contain the AutoCompleteTextView
        TextInputLayout textInputLayout = new TextInputLayout(mActivity);
        LinearLayout.LayoutParams textLayoutParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT);
        textLayoutParams.weight = 1.0f;
        textLayoutParams.setMargins(getPixels(4), getPixels(4), getPixels(4),
                getPixels(4));
        textInputLayout.setLayoutParams(textLayoutParams);

        // Add the text view to the text input layout and add the text input layout to the item field layout
        textInputLayout.addView(textViewFieldValue, textViewParams);
        itemLayout.addView(textInputLayout);

        // Add the new AutoCompleteTextView field to the fields list
        mAutoCompleteTextViewFields.add(textViewFieldValue);
    }

    // Create an OnTouchEditText for the field value
    private void createOnTouchEditText(OnTouchEditText editTextFieldValue, LinearLayout itemLayout) {

        // Create an OnTouchEditText to capture the edited input value
        LinearLayout.LayoutParams editTextParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
        editTextFieldValue.setLayoutParams(editTextParams);
        editTextFieldValue.setBackground(ContextCompat.getDrawable(mActivity, R.drawable.edit_text_underline_white));
        editTextFieldValue.setInputType(InputType.TYPE_CLASS_TEXT);
        editTextFieldValue.setTextSize(16);
        editTextFieldValue.setTextColor(ContextCompat.getColor(mActivity, R.color.colorBlack));
        editTextFieldValue.setFilters(new InputFilter[] { new InputFilter.LengthFilter(80)});
        editTextFieldValue.setImeOptions(EditorInfo.IME_ACTION_DONE | EditorInfo.IME_FLAG_NO_EXTRACT_UI);

        // Create a TextInputLayout to contain the OnTouchEditText
        TextInputLayout textInputLayout = new TextInputLayout(mActivity);
        LinearLayout.LayoutParams textLayoutParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT);
        textLayoutParams.weight = 1.0f;
        textLayoutParams.setMargins(getPixels(4), getPixels(4), getPixels(4),
                getPixels(4));
        textInputLayout.setLayoutParams(textLayoutParams);

        // Add the edit text to the text input layout and add the text input layout to the item field layout
        textInputLayout.addView(editTextFieldValue, editTextParams);
        itemLayout.addView(textInputLayout);

        // Add the new OnTouchEditText field to the fields list
        mOnTouchEditTextFields.add(editTextFieldValue);
    }

    // Create an ImageView for the photo field
    private void createImageView(ImageView imageView, LinearLayout itemLayout) {

        // Create a linear layout to contain the photo
        LinearLayout imageLayout = new LinearLayout(mActivity);
        LinearLayout.LayoutParams imageLayoutParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT);
        imageLayoutParams.weight = 1.0f;
        imageLayoutParams.gravity = Gravity.CENTER;
        imageLayout.setLayoutParams(imageLayoutParams);
        imageLayout.setOrientation(LinearLayout.HORIZONTAL);
        imageLayout.setBackgroundColor(ContextCompat.getColor(mActivity, R.color.colorWhite));

        // Create an image view for the photo
        imageView.setLayoutParams(new LinearLayout.LayoutParams(getPixels(IMAGE_WIDTH), getPixels(IMAGE_HEIGHT)));
        // maybe imageView.setScaleType(ImageView.ScaleType.FIT_XY);
        imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        imageView.setAdjustViewBounds(true);
        imageView.setContentDescription(getString(R.string.photo));

        // Add the image view to the image layout and add the image layout to the item field layout
        imageLayout.addView(imageView);
        itemLayout.addView(imageLayout);
    }

    // Create a phone icon for the phone fields
    private void addPhoneIcon(final int index, LinearLayout itemLayout) {

        // Create an image view for the phone icon
        ImageView imageViewPhone = new ImageView(mActivity);
        LinearLayout.LayoutParams imagePhoneParams = new LinearLayout.LayoutParams(getPixels(24), getPixels(24));
        imagePhoneParams.gravity = Gravity.CENTER;
        imageViewPhone.setLayoutParams(imagePhoneParams);
        imageViewPhone.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        imageViewPhone.setAdjustViewBounds(true);
        imageViewPhone.setContentDescription(getString(R.string.phone));
        imageViewPhone.setImageResource(R.drawable.phone_icon);

        // Add an on click listener to the phone icon
        imageViewPhone.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                // Send intent to switch to the phone app
                try {
                    Intent dialIntent = new Intent(Intent.ACTION_DIAL);
                    dialIntent.setData(Uri.fromParts("tel", mDataRecord.getDataArray().get(index), null));
                    mActivity.startActivity(dialIntent);
                } catch (ActivityNotFoundException e) {
                    mAppState.showSnackBar(mActivity, mRootView, getString(R.string.no_phone));
                }
            }
        });

        // Add the image view to the item field layout
        itemLayout.addView(imageViewPhone);
    }

    // Create a browser icon for the URL fields
    private void addBrowserIcon(final int index, LinearLayout itemLayout) {

        // Create an image view for the browser icon
        ImageView imageViewBrowser = new ImageView(mActivity);
        LinearLayout.LayoutParams imageBrowserParams = new LinearLayout.LayoutParams(getPixels(24), getPixels(24));
        imageBrowserParams.gravity = Gravity.CENTER;
        imageViewBrowser.setLayoutParams(imageBrowserParams);
        imageViewBrowser.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        imageViewBrowser.setAdjustViewBounds(true);
        imageViewBrowser.setContentDescription(getString(R.string.web));
        imageViewBrowser.setImageResource(R.drawable.web_icon);

        // Add an on click listener to the browser icon
        imageViewBrowser.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                // Send intent to switch to the web app
                try {
                    String url = mDataRecord.getDataArray().get(index);

                    // Ensure that the url is properly formatted
                    if (!url.startsWith("https://") && !url.startsWith("http://")) {
                        url = "http://" + url;
                    }
                    Intent webIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    mActivity.startActivity(webIntent);
                } catch (ActivityNotFoundException e) {
                    mAppState.showSnackBar(mActivity, mRootView, getString(R.string.no_web));
                }
            }
        });

        // Add the image view to the item field layout
        itemLayout.addView(imageViewBrowser);
    }

    // Create a switch widget to control list filtering
    private void addFilterSwitch(ListField listField, ListDefinition listDefinition, LinearLayout itemLayout, ListFilters filters) {

        ContextThemeWrapper newContext = new ContextThemeWrapper(mActivity, R.style.SwitchStyle);
        androidx.appcompat.widget.SwitchCompat filterSwitch = new androidx.appcompat.widget.SwitchCompat(newContext);
        LinearLayout.LayoutParams switchParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
        switchParams.setMargins(getPixels(4), getPixels(4), 0, getPixels(4));
        switchParams.gravity = Gravity.CENTER;
        filterSwitch.setLayoutParams(switchParams);
        filterSwitch.setSwitchMinWidth(getPixels(48));
        filterSwitch.setTextOn("");
        filterSwitch.setTextOff("");
        itemLayout.addView(filterSwitch);

        // Add the new filter switch to the switches list
        mFilterSwitches.add(filterSwitch);

        // Set the filter switches to the correct states
        if (mAppState.getFilterSettings() == null) {
            if (mAppState.getActiveFragment() == FragmentType.EDIT_ITEM) {

                // Use the list filters from the database to set the switches
                if (filters.isFilter(listField.getFieldName())) {
                    filterSwitch.setChecked(true);
                }
            }
        } else {

            // The filter switch settings were saved before the photo intent, use them to set the filter switches
            for (int i = 0; i < listDefinition.getFieldCount(); i++) {
                if (listDefinition.getField(i).getFieldName().equals(listField.getFieldName())) {
                    filterSwitch.setChecked(mAppState.getFilterSettings().get(i));
                }
            }
        }
    }

    // Initialize the text displayed in the textViewFieldValue
    private void initializeTextView(int index, String fieldName, AutoCompleteTextView textViewFieldValue) {

        // Add an adapter for the AutoCompleteTextView
        String[] suggestionsList = mList.getSuggestions(fieldName);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(mActivity, R.layout.suggestions_text, suggestionsList);
        textViewFieldValue.setAdapter(adapter);

        // Set the threshold for the AutoCompleteTextView
        textViewFieldValue.setThreshold(1);

        // Initialize the text displayed in the AutoCompleteTextView
        textViewFieldValue.setText(mDataRecord.getDataArray().get(index));
    }

    // Create an input field for entering date values
    private void createDateInputField(int index, LinearLayout itemLayout) {

        // Create an OnTouchEditText for the date field
        OnTouchEditText editTextFieldValue = new OnTouchEditText(mActivity);
        createOnTouchEditText(editTextFieldValue, itemLayout);
        editTextFieldValue.setText(DateValidator.formatForDisplay(mDataRecord.getDataArray().get(index)));

        // Add a date listener to the date field
        DateListener dateListener = new DateListener(editTextFieldValue, mActivity);
    }

    // Create an input field for entering email address values
    private void createEmailInputField(int index, LinearLayout itemLayout) {

        // Create an OnTouchEditText for the email field
        OnTouchEditText editTextFieldValue = new OnTouchEditText(mActivity);
        createOnTouchEditText(editTextFieldValue, itemLayout);
        editTextFieldValue.setText(mDataRecord.getDataArray().get(index));
    }

    // Create an input field for entering number values
    private void createNumberInputField(int index, LinearLayout itemLayout) {

        // Create an OnTouchEditText for the number field
        OnTouchEditText editTextFieldValue = new OnTouchEditText(mActivity);
        createOnTouchEditText(editTextFieldValue, itemLayout);
        editTextFieldValue.setText(mDataRecord.getDataArray().get(index));

        // Set up the number field input type
        editTextFieldValue.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED |
                        InputType.TYPE_NUMBER_FLAG_DECIMAL);
        editTextFieldValue.setText(mDataRecord.getDataArray().get(index));
    }

    // Create an input field for entering phone number values
    private void createPhoneInputField(int index, LinearLayout itemLayout) {

        // Create an OnTouchEditText for the phone field
        OnTouchEditText editTextFieldValue = new OnTouchEditText(mActivity);
        createOnTouchEditText(editTextFieldValue, itemLayout);
        editTextFieldValue.setText(mDataRecord.getDataArray().get(index));
    }

    // Initialize the image displayed in the imageViewPhoto
    private void initializeImageView(int index, ImageView imageViewPhoto) {

        mImageUriString = mDataRecord.getDataArray().get(index);

        // Rotate and scale the bitmap (if the image path is blank, a null bitmap will be returned)
        Bitmap bitmap = PhotoAdjuster.getAdjustedBitmap(mActivity, mImageUriString, IMAGE_WIDTH, IMAGE_HEIGHT);

        if (bitmap == null) {

            // If no image path is found use the photo icon
            imageViewPhoto.setImageResource(R.drawable.photo_icon);
        } else {
            imageViewPhoto.setImageBitmap(bitmap);
        }
    }

    // Create an input field for adding photographs to the list
    private void createPhotoInputField(int index, LinearLayout itemLayout) {

        // Create a photo layout for displaying the selected photo
        ImageView imageViewPhoto = new ImageView(mActivity);
        createImageView(imageViewPhoto, itemLayout);
        initializeImageView(index, imageViewPhoto);

        // Set up the photo icon and add on on click listener
        imageViewPhoto.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                // Send an intent to the image selection app
                //Intent photoIntent = new Intent(Intent.ACTION_PICK,
                        //android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                Intent photoIntent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                photoIntent.setType("image/*");

                // Save the data record to the AppActivityState so the edit texts can be restored after the intent
                // activity
                populateDataRecord();
                mAppState.setDataRecord(mDataRecord);

                // Save the filter button states to AppActivityState so they can be recovered when returning
                // from the photo intent
                List<Boolean> filterSettings = new ArrayList<>();
                for (androidx.appcompat.widget.SwitchCompat filterSwitch : mFilterSwitches) {
                    filterSettings.add(filterSwitch.isChecked());
                }
                mAppState.setFilterSettings(filterSettings);

                // Set the active fragment to indicate to onResume that the app is returning from a photo intent
                if (mAppState.getActiveFragment() == FragmentType.EDIT_ITEM) {
                    mAppState.setActiveFragment(FragmentType.EDIT_ITEM_RETURN);
                } else {
                    mAppState.setActiveFragment(FragmentType.NEW_ITEM_RETURN);
                }

                // The returned data will be handled in onActivityResult
                mActivity.startActivityForResult(Intent.createChooser(photoIntent, getString(R.string.select_photo)), NEW_PHOTO);
            }
        });
    }

    // Create an input field for entering text values
    private void createTextInputField(int index, String fieldName, LinearLayout itemLayout) {

        // Create an AutoCompleteTextView for the text field
        AutoCompleteTextView textViewFieldValue = new AutoCompleteTextView(mActivity);
        createAutoCompleteTextView(textViewFieldValue, itemLayout);
        initializeTextView(index, fieldName, textViewFieldValue);
    }

    // Create an input field for entering time values
    private void createTimeInputField(int index, LinearLayout itemLayout) {

        // Create an OnTouchEditText for the time field
        OnTouchEditText editTextFieldValue = new OnTouchEditText(mActivity);
        createOnTouchEditText(editTextFieldValue, itemLayout);
        editTextFieldValue.setText(TimeValidator.switchFormat(mDataRecord.getDataArray().get(index)));

        // Add a time listener to the time field
        TimeListener timeListener = new TimeListener(editTextFieldValue, mActivity);
    }

    // Create an input field for entering URL values
    private void createURLInputField(int index, LinearLayout itemLayout) {

        // Create an OnTouchEditText for the URL field
        OnTouchEditText editTextFieldValue = new OnTouchEditText(mActivity);
        createOnTouchEditText(editTextFieldValue, itemLayout);
        editTextFieldValue.setText(mDataRecord.getDataArray().get(index));
    }

    // Validate and store the entered data values
    void processDataValues() {

        // Populate the data record with the values entered into the UI fields
        populateDataRecord();

        ResultType results;

        // Update the item for an edited item or save the item for a new item
        if (mAppState.getActiveFragment() == FragmentType.EDIT_ITEM) {
            results = mList.updateListItem(mDataRecord, mListDefinition);
        } else {
            results = mList.saveDataValues(mDataRecord, mListDefinition);
        }
        switch (results) {
            case DUPLICATE_ITEM:
                mAppState.showSnackBar(mActivity, mRootView, getString(R.string.duplicate_item));
                return;
            case ITEM_SAVED:

                // If the list is encrypted, save a record in the encrypted table
                if (mListDefinition.isEncrypted()) {
                    results = mList.writeEncryptedRecord(mDataRecord);
                    if (results == ResultType.ENCRYPT_FAIL) {
                        mAppState.showSnackBar(mActivity, mRootView, getString(R.string.encrypt_fail));
                    }
                }

                // Store the bitmap for a photo list
                if (mListDefinition.isPhoto()) {

                    // If there is a new unique bitmap add it to the bitmap hash map
                    BitmapStore bitmaps = mAppState.getBitmapStore();
                    if (!bitmaps.contains(mImageUriString)) {
                        Bitmap bitmap = PhotoAdjuster.getAdjustedBitmap(mActivity, mImageUriString, IMAGE_WIDTH, IMAGE_HEIGHT);
                        if (bitmap != null) {
                            bitmaps.addBitmap(mImageUriString, bitmap);
                        }
                    }
                }

                // Update the list filter settings
                updateFilters();

                // For a new item, save the new item to app activity state to insure list scrolling is set properly
                mDataRecord.setID(String.valueOf(mList.getLastID(mAppState.getListName())));
                mAppState.setDataRecord(mDataRecord);
                mAppState.showSnackBar(mActivity, mRootView, getString(R.string.item_saved));

                // Return to the stored list fragment
                mAppState.switchFragment(mActivity, FragmentType.STORED_LIST);
                break;
            case ITEM_UPDATED:

                // If the list is encrypted, update the record in the encrypted table
                if (mListDefinition.isEncrypted()) {
                    results = mList.updateEncryptedRecord(mDataRecord);
                    if (results == ResultType.ENCRYPT_FAIL) {
                        mAppState.showSnackBar(mActivity, mRootView, getString(R.string.encrypt_fail));
                    }
                }

                // Store the bitmap for a photo list
                if (mListDefinition.isPhoto()) {

                    // If there is a new unique bitmap add it to the bitmap hash map
                    BitmapStore bitmaps = mAppState.getBitmapStore();
                    if (!bitmaps.contains(mImageUriString)) {
                        Bitmap bitmap = PhotoAdjuster.getAdjustedBitmap(mActivity, mImageUriString, IMAGE_WIDTH, IMAGE_HEIGHT);
                        if (bitmap != null) {
                            bitmaps.addBitmap(mImageUriString, bitmap);
                        }
                    }
                }

                // Update the list filter settings
                updateFilters();

                // Save the data record to app activity state to insure that list scrolling is set properly
                mAppState.setDataRecord(mDataRecord);
                mAppState.showSnackBar(mActivity, mRootView, getString(R.string.item_updated));

                // Return to the stored list fragment
                mAppState.switchFragment(mActivity, FragmentType.STORED_LIST);
                break;
            case INVALID_NUMBER:
                mAppState.showSnackBar(mActivity, mRootView, getString(R.string.invalid_number));
                return;
            case INVALID_PHONE:
                mAppState.showSnackBar(mActivity, mRootView, getString(R.string.invalid_phone));
                return;
            case ENCRYPT_FAIL:
                mAppState.showSnackBar(mActivity, mRootView, getString(R.string.encrypt_fail));
                return;
            case NO_DATA:
                mAppState.showSnackBar(mActivity, mRootView, getString(R.string.no_data));
                return;
            default:
                mAppState.showSnackBar(mActivity, mRootView, getString(R.string.store_error));
        }
    }

    // Populate the data record with the values entered into the UI fields
    private void populateDataRecord() {

        int autoCompleteTextViewCount = 0;
        int onTouchEditTextCount = 0;
        String dataValue;

        // Populate each of the fields in the data record
        for (int i = 0; i < mListDefinition.getFieldCount(); i++) {

            // Format and add each field to the data record
            switch (mListDefinition.getField(i).getFieldType()) {
                case DATE:
                    dataValue = mOnTouchEditTextFields.get(onTouchEditTextCount++).getText().toString().
                            trim().replace("|", "");
                    mDataRecord.setDataValue(i, DateValidator.formatForSort(dataValue));
                    break;
                case PHOTO:
                    if (mImageUriString == null) {
                        mDataRecord.setDataValue(i, "");
                    } else {
                        mDataRecord.setDataValue(i, mImageUriString.trim().replace("|", ""));
                    }
                    break;
                case TEXT:
                    mDataRecord.setDataValue(i, mAutoCompleteTextViewFields.get(autoCompleteTextViewCount++).getText().toString().
                        trim().replace("|", ""));
                    break;
                case TIME:
                    dataValue = mOnTouchEditTextFields.get(onTouchEditTextCount++).getText().toString().
                            trim().replace("|", "");
                    mDataRecord.setDataValue(i, TimeValidator.switchFormat(dataValue));
                    break;
                default:
                    mDataRecord.setDataValue(i, mOnTouchEditTextFields.get(onTouchEditTextCount++).getText().toString().
                                trim().replace("|", ""));
            }
        }
    }

    // Update the list filters
    private void updateFilters() {

        ListField listField;
        List<Boolean> existingFilters = new ArrayList<>();

        for (int i = 0; i < mListDefinition.getFieldCount(); i++) {
            existingFilters.add(mFilters.isFilter(mListDefinition.getField(i).getFieldName()));
        }

        if (mAppState.getActiveFragment() == FragmentType.EDIT_ITEM) {
            mFilters.removeAllFilters();
        }

        for (int i = 0; i < mListDefinition.getFieldCount(); i++) {
            listField = mListDefinition.getField(i);

            // Do not do photo fields because there is no filter switch for that field
            if (listField.getFieldType() != FieldType.PHOTO) {
                if (mFilterSwitches.get(i).isChecked()) {
                    if (mAppState.getActiveFragment() == FragmentType.EDIT_ITEM && existingFilters.get(i)) {

                        // Restore the original filter using the original data value
                        mFilters.addFilter(listField.getFieldName(), mAppState.getDataRecord().getDataArray().get(i));
                    } else {
                        mFilters.addFilter(listField.getFieldName(), mDataRecord.getDataArray().get(i));
                    }
                }
            }
        }
    }

    // Add a separator line to the field view
    private void addSeparator(LinearLayout itemFieldsLayout) {

        View separatorView = new View(mActivity);
        LinearLayout.LayoutParams separatorParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                    getPixels(1));
        separatorView.setLayoutParams(separatorParams);
        separatorView.setBackgroundColor(ContextCompat.getColor(mActivity, R.color.colorPrimaryDark));
        itemFieldsLayout.addView(separatorView);
    }
}
