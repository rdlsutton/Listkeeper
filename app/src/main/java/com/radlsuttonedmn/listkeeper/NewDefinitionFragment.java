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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.ItemTouchHelper;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

/**
 * Define a new stored list
 */

public class NewDefinitionFragment extends Fragment {

    private FragmentActivity mActivity;
    private AppActivityState mAppState;
    private AppCompatEditText mEditTextListName;
    private ListDefinition mListDefinition;
    private View mRootView;
    private TextInputLayout mTextLayoutListName;


    // Method called by the encryption confirmation dialog to update the encryption setting for a list
    void dialogYes() {

        if (mListDefinition.isEncrypted()) {
            mListDefinition.setEncrypted(false);
            mAppState.showSnackBar(mActivity, mRootView, getString(R.string.encrypt_off));
        } else {
            mListDefinition.setEncrypted(true);
            mAppState.showSnackBar(mActivity, mRootView, getString(R.string.encrypt_on));
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
                textViewTitle.setText(getString(R.string.app_name));
            }
        }
    }

    // Override the parent class's onCreateView method
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mAppState.setActiveFragment(FragmentType.NEW_DEFINITION);

        // Start a new list definition
        mListDefinition = new ListDefinition(mActivity);
        if (mAppState.getPriorFragment() != FragmentType.EDIT_FIELD) {
            mListDefinition.startNewDefinition();
        }

        mRootView = inflater.inflate(R.layout.definition_layout, container, false);

        // Set up the text input box for the list name
        mTextLayoutListName = mRootView.findViewById(R.id.textLayoutListName);
        mEditTextListName = mRootView.findViewById(R.id.editTextListName);
        mTextLayoutListName.setHint(getString(R.string.new_list_name));
        mEditTextListName.setText(mListDefinition.getNewListName());

        // Add a recyclerView in the activity layout
        RecyclerView recyclerViewFields = mRootView.findViewById(R.id.recyclerViewFields);
        DividerItemDecoration itemDecoration = new
                DividerItemDecoration(mActivity, DividerItemDecoration.VERTICAL);
        if (ContextCompat.getDrawable(mActivity, R.drawable.list_divider) != null) {
            itemDecoration.setDrawable(Objects.requireNonNull(ContextCompat.getDrawable(mActivity, R.drawable.list_divider)));
            recyclerViewFields.addItemDecoration(itemDecoration);
        }

        // Create a new fields list adapter
        DefinitionAdapter adapter = new DefinitionAdapter(mActivity, mListDefinition);
        recyclerViewFields.setAdapter(adapter);

        // Attach a swipe listener to enable swipe to delete a field
        ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback(adapter);
        ItemTouchHelper touchHelper = new ItemTouchHelper(callback);
        touchHelper.attachToRecyclerView(recyclerViewFields);

        // Set a layout manager to position the items
        recyclerViewFields.setLayoutManager(new LinearLayoutManager(mActivity));

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
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {

                mListDefinition.setNewListName(charSequence.toString());
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });

        // Add an on click listener to the add field button
        Button buttonAddField = mRootView.findViewById(R.id.buttonAddField);
        buttonAddField.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                // Setting the field ID to the field count will indicate that this is a new field
                mListDefinition.setFieldId(mListDefinition.getFieldCount());

                // Switch to the edit field fragment
                mAppState.setPriorFragment(FragmentType.NEW_DEFINITION);
                mAppState.switchFragment(mActivity, FragmentType.EDIT_FIELD);
            }
        });
        return mRootView;
    }

    // Validate and save the new list
    void saveNewList() {

        // Verify that a list name was entered
        if (mEditTextListName.getText() == null || mEditTextListName.getText().toString().trim().equals("")) {
            mTextLayoutListName.setError(getString(R.string.new_list_name));
            return;
        }
        mTextLayoutListName.setError(null);

        // Verify that at least one field has been defined
        if (mListDefinition.getFieldCount() == 0) {
            mAppState.showSnackBar(mActivity, mRootView, getString(R.string.no_fields));
            return;
        }

        // Do not allow photographs in an encrypted list
        if (mListDefinition.isEncrypted() && mListDefinition.isPhoto()) {
            mAppState.showSnackBar(mActivity, mRootView, getString(R.string.no_encrypt_photo));
            return;
        }

        // Validate and create the new list
        String newListName = mEditTextListName.getText().toString().trim();
        if (mListDefinition.saveDefinition(newListName)
                            == ResultType.DUPLICATE_TABLE) {
            mAppState.showSnackBar(mActivity, mRootView, getString(R.string.duplicate_list_name));
            return;
        }

        // Return to the stored list fragment
        mAppState.setListName(newListName);
        mAppState.switchFragment(mActivity, FragmentType.STORED_LIST);
    }

    // Update the encryption setting for the new list
    void setEncryption() {

        String message;

        // Set up encryption confirmation dialog
        if (mListDefinition.isEncrypted()) {
            message = getString(R.string.set_encrypt_off);
        } else {

            // Do not allow photographs in an encrypted list
            if (mListDefinition.isPhoto()) {
                mAppState.showSnackBar(mActivity, mRootView, getString(R.string.no_encrypt_photo));
                return;
            }
            message = getString(R.string.set_encrypt_on);
        }

        // Go to the encryption confirmation dialog
        InputDialog dialog = new InputDialog(mActivity, getString(R.string.confirm),
                message, getString(R.string.yes), getString(R.string.no));
        dialog.setDialogCaller(this);
        dialog.showDialog();
    }
}
