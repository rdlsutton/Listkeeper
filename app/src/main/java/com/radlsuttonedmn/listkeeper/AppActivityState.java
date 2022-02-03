package com.radlsuttonedmn.listkeeper;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.snackbar.SnackbarContentLayout;

import java.util.List;

/**
 * Stores the current application state and facilitates fragment switching
 */

class AppActivityState {

    private static FragmentType mActiveFragment = FragmentType.NONE;
    private static BitmapStore mBitmapStore = null;
    private static DataRecord mDataRecord = null;
    private static EmailStore mEmailStore = null;
    private static String mFileURI = null;
    private static List<Boolean> mFilterSettings = null;
    private static FragmentType mHelpPriorFragment;
    private static String mListName = "none";
    private static String mPassword = "";
    private static boolean mPhoneAvailable = true;
    private static FragmentType mPriorFragment = FragmentType.NONE;
    private static String mRecordId = "0";
    private static String mSearchQuery = "";

    // Getters and setters for the app activity parameters
    FragmentType getActiveFragment() {
        return mActiveFragment;
    }
    BitmapStore getBitmapStore() { return mBitmapStore; }
    DataRecord getDataRecord() { return mDataRecord; }
    EmailStore getEmailStore() { return mEmailStore; }
    String getFileURI() { return mFileURI; }
    List<Boolean> getFilterSettings() { return mFilterSettings; }
    FragmentType getHelpPriorFragment() { return mHelpPriorFragment; }
    String getListName() { return mListName; }
    String getPassword() { return mPassword; }
    boolean getPhoneAvailable() { return mPhoneAvailable; }
    FragmentType getPriorFragment() { return mPriorFragment; }
    String getRecordId() { return mRecordId; }
    String getSearchQuery() { return mSearchQuery; }

    void setActiveFragment(FragmentType inFragment) { mActiveFragment = inFragment; }
    void setBitmapStore(BitmapStore inStore) { mBitmapStore = inStore; }
    void setDataRecord(DataRecord inRecord) { mDataRecord = inRecord; }
    void setEmailStore(EmailStore inStore) { mEmailStore = inStore; }
    void setFileURI(String inURI) { mFileURI = inURI; }
    void setFilterSettings(List<Boolean> inSettings) { mFilterSettings = inSettings; }
    void setHelpPriorFragment(FragmentType inFragment) { mHelpPriorFragment = inFragment; }
    void setListName(String inListName) { mListName = inListName; }
    void setPassword(String inPassword) { mPassword = inPassword; }
    void setPhoneAvailable(boolean inAvailable) { mPhoneAvailable = inAvailable; }
    void setPriorFragment(FragmentType inFragment) { mPriorFragment = inFragment; }
    void setRecordId(String inId) { mRecordId = inId; }
    void setSearchQuery(String inQuery) { mSearchQuery = inQuery; }

    // Method to allow caller to swap fragments
    void switchFragment(FragmentActivity activity, FragmentType nextFragment) {

        // Create the requested fragment
        Fragment newFragment = null;
        String fragmentTag = "";
        switch (nextFragment) {
            case DEFINED_LISTS:
                newFragment = new DefinedListsFragment();
                fragmentTag = "DefinedLists";
                break;
            case EDIT_DEFINITION:
                newFragment = new EditDefinitionFragment();
                fragmentTag = "EditDefinition";
                break;
            case EDIT_ITEM:
                mActiveFragment = FragmentType.EDIT_ITEM;
                newFragment = new ItemFragment();
                fragmentTag = "Item";
                break;
            case EDIT_FIELD:
                newFragment = new EditFieldFragment();
                fragmentTag = "EditField";
                break;
            case HELP:
                newFragment = new HelpFragment();
                fragmentTag = "HelpFragment";
                break;
            case NEW_DEFINITION:
                newFragment = new NewDefinitionFragment();
                fragmentTag = "NewDefinition";
                break;
            case NEW_ITEM:
                mActiveFragment = FragmentType.NEW_ITEM;
                newFragment = new ItemFragment();
                fragmentTag = "Item";
                break;
            case STORED_LIST:
                newFragment = new StoredListFragment();
                fragmentTag = "StoredList";
        }
        if (newFragment == null) {
            newFragment = new DefinedListsFragment();
            fragmentTag = "DefinedLists";
        }
        try {
            final FragmentTransaction transaction = activity.getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.listFragmentHolder, newFragment, fragmentTag);
            transaction.commit();
        } catch (IllegalStateException e) {
            //showToast(activity, e.getMessage());
            Toast.makeText(activity, activity.getString(R.string.frag_fail), Toast.LENGTH_SHORT).show();
        }
    }

    // Show a snack bar with updated colors for the background and text
    void showSnackBar(FragmentActivity activity, View rootView, String message) {

        Snackbar snack = Snackbar.make(rootView, message, Snackbar.LENGTH_LONG);
        View view = snack.getView();
        int backgroundColor = ResourcesCompat.getColor(view.getResources(), R.color.colorPrimaryDark, null);
        view.setBackgroundColor(backgroundColor);
        TextView snackMessage = view.findViewById(com.google.android.material.R.id.snackbar_text);
        snackMessage.setTextColor(ContextCompat.getColor(activity, R.color.colorWhite));
        ((SnackbarContentLayout) snackMessage.getParent()).setBackgroundColor(backgroundColor);
        snackMessage.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        snack.show();
    }
}
