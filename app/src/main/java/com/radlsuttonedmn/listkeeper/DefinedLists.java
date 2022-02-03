package com.radlsuttonedmn.listkeeper;

import android.app.Activity;
import androidx.fragment.app.FragmentActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * Manage the list of defined lists
 */

class DefinedLists {

    private final FragmentActivity mActivity;
    private final List<String> mDefinedLists;
    private final DBHelper mListDB;

    // Constructor with a search criteria parameter
    DefinedLists(Activity activity, String searchQuery) {

        mActivity = (FragmentActivity)activity;
        mListDB = DBHelper.getInstance(mActivity);
        mDefinedLists = new ArrayList<>();
        mListDB.getListNames(mDefinedLists, searchQuery);
    }

    // Getter methods for defined lists parameters
    int getListCount() { return mDefinedLists.size(); }
    String getListName(int index) { return mDefinedLists.get(index); }

    // Delete a defined list
    void deleteList(int index) {

        // Fetch the list definition to determine if this is an encrypted list
        AppActivityState appState = new AppActivityState();
        appState.setListName(mDefinedLists.get(index));
        ListDefinition listDefinition = new ListDefinition(mActivity);
        listDefinition.getDefinition();

        // If this is an encrypted list delete the encrypted table
        if (listDefinition.isEncrypted()) {
            mListDB.deleteEncryptedTable(mDefinedLists.get(index));
        }
        mListDB.deleteListTable(mDefinedLists.get(index));
        mDefinedLists.remove(index);
    }
}
