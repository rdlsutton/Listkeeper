package com.radlsuttonedmn.listkeeper;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.view.inputmethod.InputMethodManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Display the items that are included in a defined list
 */

public class StoredListFragment extends Fragment {

    private FragmentActivity mActivity;
    private StoredListAdapter mAdapter;
    private Button mButtonAddItem;
    private AppActivityState mAppState;
    private LinearLayout mButtonBar;
    private ListFilters mFilters;
    private StoredListFragment mFragment;
    private String mImportResults;
    private StoredList mList;
    private ListDefinition mListDefinition;
    private ScrollView mProgressBarLayout;
    private RecyclerView mRecyclerViewStoredList;
    private ResultType mResults;
    private View mRootView;
    private ListSorts mSorts;
    private TextView mTextViewFieldsMain;
    private TextView mTextViewFieldsMore;

    private static final int OPEN_CSV = 3;


    // Method called by the email address dialog to execute the email intent
    void sendEmail(String emailAddress) {

        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:"));
        intent.putExtra(Intent.EXTRA_EMAIL, new String[] {emailAddress});
        intent.putExtra(Intent.EXTRA_SUBJECT, "Here Are My Lists: " + mAppState.getListName());
        intent.putExtra(Intent.EXTRA_TEXT, mList.composeEmail());
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            mActivity.startActivity(Intent.createChooser(intent, "Send mail ..."));
        } catch (ActivityNotFoundException ex) {
            mAppState.showSnackBar(mActivity, mRootView, getString(R.string.no_email));
        }
    }

    // Method called by the enter password dialog that validates the password and executes an async task
    // to decrypt the list
    void setPassword(String password) {

        mAppState.setPassword(password);
        DataEncrypter dataEncrypter = new DataEncrypter();
        if (dataEncrypter.validPassword()) {

            // Execute a background task to decrypt the list and fetch the data items
            prepareDataLoad();
            dataLoad("encrypted");
        } else {

            // Display the invalid password dialog
            InputDialog dialog = new InputDialog(mActivity, getString(R.string.invalid_password),
                        getString(R.string.password_requirements), getString(R.string.ok), getString(R.string.cancel));
            dialog.setDialogCaller(this, "invalid_password");
            dialog.showDialog();

            // Return to the defined lists fragment
            mAppState.switchFragment(mActivity, FragmentType.DEFINED_LISTS);
        }
    }

    // Method called when the enter password dialog is cancelled
    void dialogNo() {

        // Return to the defined lists fragment
        mAppState.switchFragment(mActivity, FragmentType.DEFINED_LISTS);
    }

    // Override the parent class's onAttach method
    @Override
    public void onAttach(@NonNull Context context) {

        super.onAttach(context);
        mActivity = (FragmentActivity)context;
        mFragment = this;
    }

    // Override the parent class's onCreate method
    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        mAppState = new AppActivityState();

        // Connect to the main toolbar and set the title to the list name
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

        try {
            // Fetch the stored list from the app activity state
            mList = new StoredList(mActivity);
            mFilters = new ListFilters(mActivity, mAppState.getListName());
            mSorts = new ListSorts(mActivity, mAppState.getListName());

            // Fetch the list definition
            mListDefinition = new ListDefinition(mActivity);
            mListDefinition.getDefinition();
        } catch (Exception e) {
            mAppState.showSnackBar(mActivity, mRootView, getString(R.string.data_fail));

            // Bail out and switch to the defined lists fragment
            mAppState.switchFragment(mActivity, FragmentType.DEFINED_LISTS);
        }

        mRootView = inflater.inflate(R.layout.stored_list_layout, container, false);

        // Initialize the progress bar
        mButtonBar = mRootView.findViewById(R.id.buttonBar);
        mProgressBarLayout = mRootView.findViewById(R.id.progressBarLayout);

        // Add a recyclerView in the activity layout
        mRecyclerViewStoredList = mRootView.findViewById(R.id.recyclerViewStoredList);

        // Suppress appearance of the soft keyboard
        InputMethodManager imm = (InputMethodManager) mActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(mRootView.getWindowToken(), 0);
        }

        // Display the field names from the list definition
        mTextViewFieldsMain = mRootView.findViewById(R.id.textViewFieldsMain);
        mTextViewFieldsMore = mRootView.findViewById(R.id.textViewFieldsMore);

        // Add an item decoration to the recycler view
        DividerItemDecoration itemDecoration = new
                DividerItemDecoration(mActivity, DividerItemDecoration.VERTICAL);
        if (ContextCompat.getDrawable(mActivity, R.drawable.list_divider) != null) {
            itemDecoration.setDrawable(Objects.requireNonNull(ContextCompat.getDrawable(mActivity, R.drawable.list_divider)));
            mRecyclerViewStoredList.addItemDecoration(itemDecoration);
        }

        // Set a layout manager to position the items
        mRecyclerViewStoredList.setLayoutManager(new LinearLayoutManager(mActivity));

        // Create an adapter passing in the stored list data
        mAdapter = new StoredListAdapter(mActivity, mList, mListDefinition);

        // Attach the adapter to the recyclerView to populate the items
        mRecyclerViewStoredList.setAdapter(mAdapter);

        // Attach a swipe listener to enable swipe to delete an item
        ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback(mAdapter);
        ItemTouchHelper touchHelper = new ItemTouchHelper(callback);
        touchHelper.attachToRecyclerView(mRecyclerViewStoredList);

        // Add an on click listener to the column headers to enable editing the list definition
        mTextViewFieldsMain.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                // Switch to the edit definition fragment
                mAppState.setPriorFragment(FragmentType.STORED_LIST);
                mAppState.switchFragment(mActivity, FragmentType.EDIT_DEFINITION);
            }
        });

        mTextViewFieldsMore.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                // Switch to the edit definition fragment
                mAppState.setPriorFragment(FragmentType.STORED_LIST);
                mAppState.switchFragment(mActivity, FragmentType.EDIT_DEFINITION);
            }
        });

        // Add an on click listener to the add item button
        mButtonAddItem = mRootView.findViewById(R.id.buttonAddItem);
        mButtonAddItem.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                // Switch to the new item fragment
                mAppState.setDataRecord(null);
                mAppState.setFilterSettings(null);
                mAppState.switchFragment(mActivity, FragmentType.NEW_ITEM);
            }
        });

        // Add an on checked change listener to the filter switch
        androidx.appcompat.widget.SwitchCompat switchAllFilter = mRootView.findViewById(R.id.switchAllFilter);
        if (mFilters.isAnyFilter()) {
            switchAllFilter.setChecked(true);
            switchAllFilter.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                    // Remove all filters
                    mFilters.removeAllFilters();
                    mAppState.showSnackBar(mActivity, mRootView, getString(R.string.filters_removed));

                    // Reset the stored list fragment
                    mAppState.switchFragment(mActivity, FragmentType.STORED_LIST);
                }
            });
        } else {
            switchAllFilter.setVisibility(View.GONE);
        }

        // Add an on checked change listener to the sorting switch
        androidx.appcompat.widget.SwitchCompat switchAllSort = mRootView.findViewById(R.id.switchAllSort);
        if (mSorts.isAnySort()) {
            switchAllSort.setChecked(true);
            switchAllSort.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                    // Remove all sorts
                    mSorts.removeAllSorts();
                    mAppState.showSnackBar(mActivity, mRootView, getString(R.string.sorts_removed));

                    // Reset the stored list fragment
                    mAppState.switchFragment(mActivity, FragmentType.STORED_LIST);
                }
            });
        } else {
            switchAllSort.setVisibility(View.GONE);
        }

        // Add an on click listener to the import icon
        ImageButton buttonImport = mRootView.findViewById(R.id.buttonImport);
        buttonImport.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                // Initialize the app state for the return from the intent
                mAppState.setActiveFragment(FragmentType.STORED_LIST_RETURN);
                mAppState.setFileURI("");

                // Open a file selector dialog using the ACTION_OPEN_DOCUMENT intent
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("*/*");

                mActivity.startActivityForResult(intent, OPEN_CSV);
            }
        });

        // Add an on click listener to the email icon
        ImageButton buttonEmail = mRootView.findViewById(R.id.buttonEmail);
        buttonEmail.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                // Create a list of suggested email addresses
                EmailStore emailStore = mAppState.getEmailStore();
                List<String> suggestionsList = emailStore.getEmailAddresses();
                String[] suggestions = suggestionsList.toArray(new String[0]);

                // Display the email address dialog
                InputDialog dialog = new InputDialog(mActivity, "", getString(R.string.email_address),
                        getString(R.string.done), getString(R.string.cancel));
                dialog.setDialogCaller(mFragment, suggestions, mListDefinition.isEncrypted());
                dialog.showDialog();
            }
        });

        return mRootView;
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        DBHelper listDB = DBHelper.getInstance(mActivity);

        // If this is an encrypted list, display the enter password dialog
        if (mListDefinition.isEncrypted()) {
            if (mAppState.getPassword().equals("")) {
                InputDialog dialog = new InputDialog(mActivity, "", getString(R.string.encrypt_password),
                        getString(R.string.done), getString(R.string.cancel));
                if (listDB.isEncryptedData(mAppState.getListName())) {
                    dialog.setDialogCaller(this, "password");
                } else {
                    dialog.setDialogCaller(this, "new_password");
                }
                dialog.showDialog();
            } else {

                // Execute the background task to fetch the stored list's data items
                prepareDataLoad();
                dataLoad("decrypted");
            }
        } else {

            // Execute the background task to fetch the stored list's data items
            prepareDataLoad();
            dataLoad("unencrypted");
        }
    }

    // Prior to starting the background task, turn on the progress bar display
    void prepareDataLoad() {

        mRecyclerViewStoredList.setVisibility(View.GONE);
        mButtonAddItem.setVisibility(View.GONE);
        mButtonBar.setVisibility(View.GONE);
        mProgressBarLayout.setVisibility(View.VISIBLE);
    }

    // Set up the main and background threads to load the data in the background
    void dataLoad(final String loadType) {

        // Create an executor that executes tasks in the main thread.
        final Executor mainExecutor = ContextCompat.getMainExecutor(mActivity);

        // Create an executor that executes tasks in the background thread.
        ScheduledExecutorService backgroundExecutor = Executors.newSingleThreadScheduledExecutor();

        // Execute a task in the background thread.
        backgroundExecutor.execute(new Runnable() {
            @Override
            public void run() {
                // Load the data from the database
                loadData(loadType);

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

    // After completing the background task, turn off the progress bar and turn on the recycler view
    void loadRecyclerView() {

        // Report any file import results
        if (!mImportResults.equals("")) {
            if (isAdded()) {
                if (mImportResults.equals(getString(R.string.import_complete))) {
                    mAppState.showSnackBar(mActivity, mRootView, mImportResults);
                } else {
                    InputDialog dialog = new InputDialog(mActivity, mActivity.getString(R.string.import_results),
                            mImportResults, mActivity.getString(R.string.ok), mActivity.getString(R.string.cancel));
                    dialog.setDialogCaller(this, "file_error");
                    dialog.showDialog();
                }
            }
        }

        // Handle cases of decryption failure
        if (mResults == ResultType.DECRYPT_FAIL) {
            if (isAdded()) {
                mAppState.showSnackBar(mActivity, mRootView, getString(R.string.decrypt_fail));
            }

            // Return to the defined lists fragment to prevent repeated asking for a password
            mAppState.switchFragment(mActivity, FragmentType.DEFINED_LISTS);
            return;
        }

        // Handle any database errors that occurred
        if (mResults != ResultType.VALID) {
            if (isAdded()) {
                mAppState.showSnackBar(mActivity, mRootView, getString(R.string.data_fail));
            }
            mAppState.switchFragment(mActivity, FragmentType.DEFINED_LISTS);
        }

        if (isAdded()) {

            // Set the maximum number of characters per line
            int maxLineLength;
            if (mListDefinition.isPhoto()) {
                maxLineLength = 18;
            } else {
                maxLineLength = 40;
            }

            // Populate the text views for the header row
            StringBuilder fieldsMain = new StringBuilder(mListDefinition.getField(0).getFieldName());
            StringBuilder fieldsMore = new StringBuilder();

            int loopStart = 1;
            int lineLength = mList.getDataLength(0);

            if (mListDefinition.getFieldCount() > 1) {

                // If there is sufficient space available write the second field name to fields main
                if (lineLength + mList.getDataLength(1) < maxLineLength) {
                    fieldsMain.append("  \u2022  ");
                    fieldsMain.append(mListDefinition.getField(1).getFieldName());
                    loopStart = 2;
                }
            }

            if (mListDefinition.getFieldCount() > loopStart) {

                // Get the first field name that was not written to fields main and write it to fields more
                fieldsMore.append(mListDefinition.getField(loopStart).getFieldName());
                lineLength = mList.getDataLength(loopStart++);

                // Write all the subsequent field names to fields more
                for (int i = loopStart; i < mListDefinition.getFieldCount(); i++) {
                    if (lineLength + mList.getDataLength(i) + 5 < maxLineLength) {

                        // There is sufficient room to write the next field name to the same line
                        fieldsMore.append("  \u2022  ");
                        lineLength += 5;
                        fieldsMore.append(mListDefinition.getField(i).getFieldName());
                        lineLength += mList.getDataLength(i);
                    } else {

                        // Need to write the next field name to a new line
                        fieldsMore.append("\n");
                        fieldsMore.append(mListDefinition.getField(i).getFieldName());
                        lineLength = mList.getDataLength(i);
                    }
                }
            }

            mTextViewFieldsMain.setText(fieldsMain.toString());
            mTextViewFieldsMore.setText(fieldsMore.toString());
            if (fieldsMore.toString().equals("")) {
                mTextViewFieldsMore.setVisibility(View.GONE);
            }

            // Notify the adapter that the data is ready
            mAdapter.notifyDataSetChanged();
            mRecyclerViewStoredList.setVisibility(View.VISIBLE);
            mButtonAddItem.setVisibility(View.VISIBLE);
            mButtonBar.setVisibility(View.VISIBLE);
            mProgressBarLayout.setVisibility(View.GONE);

            // If a data record has been saved in the app state, use it to position the recyclerView
            if (mAppState.getDataRecord() != null) {
                DataRecord dataRecord;
                for (int i = 0; i < mList.getItemCount(); i++) {
                    dataRecord = mList.getDataRecord(i);
                    if (dataRecord.getID().equals(mAppState.getDataRecord().getID())) {
                        mRecyclerViewStoredList.scrollToPosition(i);
                        break;
                    }
                }
            }

            // Display the number of items in a toast
            mAppState.showSnackBar(mActivity, mRootView, mList.getItemCount() + " " + getString(R.string.items));
        } else {
            mAppState.switchFragment(mActivity, FragmentType.DEFINED_LISTS);
        }
    }

    // In the background task, retrieve the data from the main table
    void loadData(String loadType) {

        // Initialize import results so post execute will know if there is anything to report
        mImportResults = "";

        // If returning from file selector intent, import the selected file
        if (mAppState.getActiveFragment() == FragmentType.STORED_LIST_RETURN) {
            if (mAppState.getFileURI() != null) {
                if (!mAppState.getFileURI().equals("")) {
                    FilesList filesList = new FilesList(mActivity);
                    mImportResults = filesList.importFile(mAppState.getFileURI(), mList, mListDefinition);
                }
            }
        }

        // Reset the app state active fragment
        mAppState.setActiveFragment(FragmentType.STORED_LIST);

        // Pull the list data from the database
        try {
            mResults = ResultType.VALID;
            if (loadType.equals("encrypted")) {
            //if (params[0].equals("encrypted")) {
                mResults = mList.decryptData(mListDefinition);
            }
            if (mResults == ResultType.VALID) {

                // Retrieve the email addresses to use as suggestions for emailing this list
                mList.getEmailSuggestions();

                // Fetch the list data from the main table
                mResults = mList.fetchListData(mListDefinition, mFilters.getWhereClause(),
                        mFilters.getWhereArgs(), mSorts.getOrderBy());
                if (mResults == ResultType.VALID) {
                    if (!mAppState.getSearchQuery().equals("")) {
                        mList.applySearch(mAppState.getSearchQuery(), mListDefinition);
                    }

                    // If the list contains photographs make sure that the bitmap store is initialized
                    if (mListDefinition.isPhoto()) {
                        mList.fetchBitmaps(mActivity, mListDefinition);
                    }
                }
            }
        } catch (Exception e) {
            mResults = ResultType.DATA_FAIL;
        }
    }
}
