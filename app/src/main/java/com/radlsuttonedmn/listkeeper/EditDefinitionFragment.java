package com.radlsuttonedmn.listkeeper;

import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;
import com.google.android.material.textfield.TextInputLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.core.content.ContextCompat;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Fragment that updates the definition of an existing list
 */

public class EditDefinitionFragment extends Fragment {

    private FragmentActivity mActivity;
    private AppActivityState mAppState;
    private AppCompatEditText mEditTextListName;
    private boolean mFieldChange;
    private ListDefinition mListDefinition;
    private String mNewListName;
    private LinearLayout mProgressBarLayout;
    private ResultType mResults;
    private RecyclerView mRecyclerViewFields;
    private View mRootView;
    private boolean mSwitchEncryption;
    private TextInputLayout mTextLayoutListName;

    // Method called by the update definition dialog that fetches a password for an encrypted list or runs the
    // update definition background task for an unencrypted list
    void dialogYes() {

        // If encryption is being added, need to get a password
        if (mSwitchEncryption) {
            if (mListDefinition.isEncrypted()) {
                prepareDataLoad();
                dataLoad();
            } else {
                InputDialog dialog = new InputDialog(mActivity, "", getString(R.string.encrypt_password),
                        getString(R.string.done), getString(R.string.cancel));
                dialog.setDialogCaller(this, "new_password");
                dialog.showDialog();
            }
        } else {
            prepareDataLoad();
            dataLoad();
        }
    }

    // Method called by the update definition dialog when the definition update is cancelled
    void dialogNo() {

        // Return to the stored list fragment
        mAppState.switchFragment(mActivity, FragmentType.STORED_LIST);
    }

    // Method called by the enter password dialog that validates the password and executes a background task to
    // decrypt the list
    void setPassword(String password) {

        mAppState.setPassword(password);
        DataEncrypter dataEncrypter = new DataEncrypter();
        if (dataEncrypter.validPassword()) {
            prepareDataLoad();
            dataLoad();
        } else {

            // Display invalid password dialog
            InputDialog dialog = new InputDialog(mActivity, getString(R.string.invalid_password),
                        getString(R.string.password_requirements), getString(R.string.ok), getString(R.string.cancel));
            dialog.setDialogCaller(this, "invalid_password");
            dialog.showDialog();
        }
    }

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

        mAppState.setActiveFragment(FragmentType.EDIT_DEFINITION);
        mSwitchEncryption = false;

        // Start a new list definition
        mListDefinition = new ListDefinition(mActivity);
        if (mAppState.getPriorFragment() != FragmentType.EDIT_FIELD) {
            mListDefinition.getDefinition();
            mListDefinition.createEditDefinition();
        }

        mRootView = inflater.inflate(R.layout.definition_layout, container, false);

        // Initialize the progress bar
        mProgressBarLayout = mRootView.findViewById(R.id.progressBarLayout);

        // Set up the text input box for the list name
        mTextLayoutListName = mRootView.findViewById(R.id.textLayoutListName);
        mEditTextListName = mRootView.findViewById(R.id.editTextListName);
        mTextLayoutListName.setHint(getString(R.string.edit_list_name));
        mEditTextListName.setText(mListDefinition.getNewListName());

        // Add a recyclerView in the activity layout
        mRecyclerViewFields = mRootView.findViewById(R.id.recyclerViewFields);
        DividerItemDecoration itemDecoration = new
                DividerItemDecoration(mActivity, DividerItemDecoration.VERTICAL);
        if (ContextCompat.getDrawable(mActivity, R.drawable.list_divider) != null) {
            itemDecoration.setDrawable(Objects.requireNonNull(ContextCompat.getDrawable(mActivity, R.drawable.list_divider)));
            mRecyclerViewFields.addItemDecoration(itemDecoration);
        }

        // Create a new fields list adapter
        DefinitionAdapter adapter = new DefinitionAdapter(mActivity, mListDefinition);
        mRecyclerViewFields.setAdapter(adapter);

        // Attach a swipe listener to enable swipe to delete a field
        ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback(adapter);
        ItemTouchHelper touchHelper = new ItemTouchHelper(callback);
        touchHelper.attachToRecyclerView(mRecyclerViewFields);

        // Set a layout manager to position the items
        mRecyclerViewFields.setLayoutManager(new LinearLayoutManager(mActivity));

        // Add input filter to restrict the list name to letters, numbers, spaces, underscores or blanks
        ArrayList<InputFilter> currentInputFilters = new ArrayList<>(Arrays.asList(mEditTextListName.getFilters()));
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
        mEditTextListName.setFilters(newInputFilters);

        // Add a text change listener to update the list name in the new definition
        mEditTextListName.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence sequence, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence sequence, int start, int before, int count) {

                mListDefinition.setNewListName(sequence.toString());
            }

            @Override
            public void afterTextChanged(Editable sequence) { }
        });

        // Add an on click listener to the add field button
        Button buttonAddField = mRootView.findViewById(R.id.buttonAddField);
        buttonAddField.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                // Setting the field ID to the field count will indicate that this is a new field
                mListDefinition.setFieldId(mListDefinition.getNewCount());

                // Switch to the edit field fragment
                mAppState.setPriorFragment(FragmentType.EDIT_DEFINITION);
                mAppState.switchFragment(mActivity, FragmentType.EDIT_FIELD);
            }
        });
        return mRootView;
    }

    // Set up an input dialog to confirm the list definition update
    private void setUpDialog(boolean fieldChange, boolean dataLoss) {

        String message = getString(R.string.want_to);
        if (mSwitchEncryption) {
            if (mListDefinition.isEncrypted()) {
                message = message + "\n" + getString(R.string.turn_encrypt_off);
            } else {
                message = message + "\n" + getString(R.string.turn_encrypt_on);
                if (mListDefinition.isPhoto()) {
                    message = message + "\n" + getString(R.string.no_encrypt_photo);
                }
            }
        }
        if (fieldChange) {
            if (message.length() > 18) {
                message = message + ",";
            }
            message = message + "\n" + getString(R.string.list_change);
        }
        if (!mAppState.getListName().equals(mNewListName)) {
            if (message.length() > 18) {
                message = message + ",";
            }
            message = message + "\n" + getString(R.string.name_change);
        }
        message = message + "\n" + getString(R.string.this_list);
        if (dataLoss) {
            message = message + "\n" + getString(R.string.lose_data);
            message = message + " " + mListDefinition.getDataLossList() + ".";
        }

        InputDialog dialog = new InputDialog(mActivity, getString(R.string.warning), message,
                getString(R.string.yes), getString(R.string.no));
        dialog.setDialogCaller(this, "definition_update");
        dialog.showDialog();
    }

    // Validate the list definition update and initiate an async task to perform the updates
    void processUpdate() {

        mFieldChange = false;
        if (mEditTextListName.getText() == null || mEditTextListName.getText().toString().equals("")) {
            mTextLayoutListName.setError(getString(R.string.new_list_name));
        } else {
            mTextLayoutListName.setError(null);
            mNewListName = mEditTextListName.getText().toString();

            // Check the validity of the proposed list definition update
            mResults = mListDefinition.validateUpdate(mNewListName, mSwitchEncryption);

            // Cancel update if there is a duplicate table name
            if (mResults == ResultType.DUPLICATE_TABLE) {
                mAppState.showSnackBar(mActivity, mRootView, getString(R.string.duplicate_list_name));
                return;
            }

            // Cancel update if there are no fields in the new definition
            if (mResults == ResultType.NO_FIELDS) {
                mAppState.showSnackBar(mActivity, mRootView, getString(R.string.no_fields));
                return;
            }

            // Do not allow photographs in an encrypted list
            if (mResults == ResultType.ENCRYPT_PHOTO) {
                mAppState.showSnackBar(mActivity, mRootView, getString(R.string.no_encrypt_photo));
                return;
            }

            if (mResults == ResultType.FIELD_CHANGE) {
                mFieldChange = true;
            }

            if ((mFieldChange) || !mNewListName.equals(mAppState.getListName()) || (mSwitchEncryption)) {
                setUpDialog((mResults == ResultType.FIELD_CHANGE), mListDefinition.isDataLoss());
            } else {
                prepareDataLoad();
                dataLoad();
            }
        }
    }

    // Update the encryption setting for the list
    void setEncryption() {
        mSwitchEncryption = true;
        processUpdate();
    }

    // Before running the background task, turn on the progress bar display
    void prepareDataLoad() {

        mRecyclerViewFields.setVisibility(View.GONE);
        mProgressBarLayout.setVisibility(View.VISIBLE);
    }

    // Set up the main and background threads to load the data in the background
    void dataLoad() {

        // Create an executor that executes tasks in the main thread.
        final Executor mainExecutor = ContextCompat.getMainExecutor(mActivity);

        // Create an executor that executes tasks in the background thread.
        ScheduledExecutorService backgroundExecutor = Executors.newSingleThreadScheduledExecutor();

        // Execute a task in the background thread.
        backgroundExecutor.execute(new Runnable() {
            @Override
            public void run() {
                // Load the data from the database
                loadData();

                // Update the UI on the main thread
                mainExecutor.execute(new Runnable() {
                    @Override
                    public void run() {
                        loadRecyclerView();
                    }
                });
            }
        });
    }

    // Run a background task to perform the list definition updates
    void loadData() {

        mResults = ResultType.VALID;

        // Update the list definition
        if ((mFieldChange) || !mNewListName.equals(mAppState.getListName()) || (mSwitchEncryption)) {
            mResults = mListDefinition.updateDefinition(mNewListName, mFieldChange, mSwitchEncryption);
        }

        // Update the list sorting criteria for the new list definition
        if (mResults == ResultType.VALID) {
            mListDefinition.updateListSorts(mNewListName);
        }

        // Reset the app state data record to reset list scrolling
        mAppState.setDataRecord(null);
    }

    // After running the background task, turn off the progress bar display, report the list definition update results
    // and return to the previous fragment
    void loadRecyclerView() {

        if (isAdded()) {
            switch (mResults) {
                case DECRYPT_FAIL:
                    mAppState.showSnackBar(mActivity, mRootView, getString(R.string.decrypt_fail));
                    return;
                case DATA_FAIL:
                    mAppState.showSnackBar(mActivity, mRootView, getString(R.string.data_fail));
                    return;
                case STORE_ERROR:
                    mAppState.showSnackBar(mActivity, mRootView, getString(R.string.store_error));
                    return;
                default:
                    mAppState.showSnackBar(mActivity, mRootView, getString(R.string.update_complete));
            }
        }

        // Return to the stored list fragment
        mAppState.switchFragment(mActivity, FragmentType.STORED_LIST);
    }
}
