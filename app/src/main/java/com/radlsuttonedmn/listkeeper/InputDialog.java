package com.radlsuttonedmn.listkeeper;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import androidx.fragment.app.FragmentActivity;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatEditText;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputLayout;

/**
 * Display an alert dialog defined by the dialog parameters
 */

class InputDialog {

    private final FragmentActivity mActivity;
    private DefinedListsAdapter mDefinedListsAdapter;
    private String mDialogType = "";
    private EditDefinitionFragment mEditDefinitionFragment;
    private AppCompatEditText mEditTextPassword;
    private boolean mEncrypted;
    private final String mMessage;
    private final String mNegativeButtonText;
    private NewDefinitionFragment mNewDefinitionFragment;
    private final String mPositiveButtonText;
    private AppCompatEditText mEditTextSecondPassword;
    private StoredListAdapter mStoredListAdapter;
    private StoredListFragment mStoredListFragment;
    private String[] mSuggestionsList;
    private AutoCompleteTextView mTextViewEmail;
    private final String mTitle;


    // Constructor with the dialog title, message, positive and negative button text as parameters
    @SuppressLint("InflateParams")
    InputDialog(FragmentActivity activity, String title, String message, String positiveButtonText, String negativeButtonText) {

        mActivity = activity;
        mTitle = title;
        mMessage = message;
        mPositiveButtonText = positiveButtonText;
        mNegativeButtonText = negativeButtonText;
    }

    // Provide the calling fragment and dialog type to the input dialog
    void setDialogCaller(EditDefinitionFragment inFragment, String dialogType) {
        mEditDefinitionFragment = inFragment;
        mDialogType = dialogType;
    }

    void setDialogCaller(DefinedListsAdapter inAdapter) {
        mDefinedListsAdapter = inAdapter;
        mDialogType = "delete_list";
    }

    void setDialogCaller(NewDefinitionFragment inFragment) {
        mNewDefinitionFragment = inFragment;
        mDialogType = "set_encrypt";
    }

    void setDialogCaller(StoredListAdapter inAdapter) {
        mStoredListAdapter = inAdapter;
        mDialogType = "delete_item";
    }

    void setDialogCaller(StoredListFragment inFragment, String dialogType) {
        mStoredListFragment = inFragment;
        mDialogType = dialogType;
    }

    void setDialogCaller(StoredListFragment inFragment, String[] suggestionsList, boolean encrypted) {
        mStoredListFragment = inFragment;
        mDialogType = "email";
        mSuggestionsList = suggestionsList;
        mEncrypted = encrypted;
    }

    // Display the input dialog
    @SuppressLint("InflateParams")
    void showDialog() {

        // Create an alert dialog based on the input parameters
        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity, R.style.AlertDialogStyle);
        builder.setTitle(mTitle);
        builder.setMessage(mMessage);

        // A password dialog will ask the user to input a password
        if (mDialogType.equals("password") || mDialogType.equals("new_password")) {
            LayoutInflater inflater = LayoutInflater.from(mActivity);
            View passwordView = inflater.inflate(R.layout.edit_password_layout, null);
            mEditTextPassword = passwordView.findViewById(R.id.editTextPassword);

            // If this is a new list, have the user confirm the password
            if (mDialogType.equals("new_password")) {
                TextView textViewConfirm = passwordView.findViewById(R.id.textViewConfirm);
                textViewConfirm.setVisibility(View.VISIBLE);
                TextInputLayout confirmLayout =
                        passwordView.findViewById(R.id.layoutConfirm);
                confirmLayout.setVisibility(View.VISIBLE);
                mEditTextSecondPassword = passwordView.findViewById(R.id.editTextSecondPassword);
            }
            builder.setView(passwordView);
        }

        // An email dialog will ask the user to input an email address
        if (mDialogType.equals("email")) {

            // Set up the email address auto complete text view
            LayoutInflater inflater = LayoutInflater.from(mActivity);
            View emailView = inflater.inflate(R.layout.email_address_layout, null);
            mTextViewEmail = emailView.findViewById(R.id.textViewEmail);

            // Add an adapter for the AutoCompleteTextView
            ArrayAdapter<String> adapter = new ArrayAdapter<>(mActivity, R.layout.suggestions_text, mSuggestionsList);
            mTextViewEmail.setAdapter(adapter);

            // Set the threshold for the AutoCompleteTextView
            mTextViewEmail.setThreshold(1);

            // If the list is encrypted, warn the user that the data will be emailed without encryption
            if (mEncrypted) {
                TextView textViewEncrypted = emailView.findViewById(R.id.textViewEncrypted);
                textViewEncrypted.setVisibility(View.VISIBLE);
            }
            builder.setView(emailView);
        }

        // Respond to a positive button press
        builder.setPositiveButton(mPositiveButtonText, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (mDialogType) {
                    case "email":
                        if (mTextViewEmail.getText() != null && !mTextViewEmail.toString().equals("")) {
                            mStoredListFragment.sendEmail(mTextViewEmail.getText().toString().replace("|", ""));
                        }
                        break;
                    case "definition_update":
                        mEditDefinitionFragment.dialogYes();
                        break;
                    case "delete_item":
                        mStoredListAdapter.dialogYes();
                        break;
                    case "delete_list":
                        mDefinedListsAdapter.dialogYes();
                        break;
                    case "new_password":
                    case "password":
                        if (mEditTextPassword.getText() != null) {
                            if (mStoredListFragment == null) {
                                mEditDefinitionFragment.setPassword(mEditTextPassword.getText().toString());
                            } else {
                                mStoredListFragment.setPassword(mEditTextPassword.getText().toString());
                            }
                        }
                        break;
                    case "set_encrypt":
                        mNewDefinitionFragment.dialogYes();
                }
                dialog.dismiss();
            }
        });

        // File error and invalid password dialogs to not have cancel buttons
        if (!mDialogType.equals("file_error") && !mDialogType.equals("invalid_password")) {
            builder.setNegativeButton(mNegativeButtonText, new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch (mDialogType) {
                        case "definition_update":
                            mEditDefinitionFragment.dialogNo();
                            break;
                        case "delete_item":
                            mStoredListAdapter.dialogNo();
                            break;
                        case "delete_list":
                            mDefinedListsAdapter.dialogNo();
                            break;
                        case "new_password":
                        case "password":
                            if (mStoredListFragment != null) {
                                mStoredListFragment.dialogNo();
                            }
                    }
                    dialog.dismiss();
                }
            });
        }

        builder.setCancelable(false);
        final AlertDialog dialog = builder.show();

        if (mDialogType.equals("new_password")) {

            // If this is a new list, have the user confirm the password
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);

            // Setup a text watcher to compare the two versions of the password
            mEditTextSecondPassword.addTextChangedListener(new TextWatcher() {
                public void onTextChanged(CharSequence s, int start, int before, int count) { }

                public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

                public void afterTextChanged(Editable s) {

                    // Enable the DONE button when the two versions fo the password match
                    if (mEditTextPassword.getText() != null && mEditTextSecondPassword.getText() != null) {
                        String firstPassword = mEditTextPassword.getText().toString();
                        String secondPassword = mEditTextSecondPassword.getText().toString();
                        if (secondPassword.length() >= firstPassword.length()) {
                            if (firstPassword.equals(secondPassword)) {
                                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                            } else {
                                Toast.makeText(mActivity, mActivity.getString(R.string.password_mismatch), Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                }
            });
        }
    }
}
