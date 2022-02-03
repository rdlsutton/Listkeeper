package com.radlsuttonedmn.listkeeper;

import android.content.Context;
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
import android.widget.TextView;

import java.util.Objects;

/**
 * Fragment that displays a list of the lists that have been defined
 */

public class DefinedListsFragment extends Fragment {

    private FragmentActivity mActivity;
    private AppActivityState mAppState;

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

        // Connect to the main toolbar and reset the toolbar title
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
        mAppState.setActiveFragment(FragmentType.DEFINED_LISTS);

        // Initialize the list of defined lists
        DefinedLists definedLists = new DefinedLists(mActivity, mAppState.getSearchQuery());

        // When entering the defined lists fragment, clear out any existing decrypted data and
        // clear any existing password
        DBHelper listDB = DBHelper.getInstance(mActivity);
        listDB.clearDecryptedData();
        mAppState.setPassword("");

        // Clear out any existing bitmap store
        mAppState.setBitmapStore(null);

        View rootView = inflater.inflate(R.layout.defined_lists_layout, container, false);

        // Suppress the appearance of the soft keyboard
        InputMethodManager imm = (InputMethodManager)mActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(rootView.getWindowToken(), 0);
        }

        // Add a recyclerView in the activity layout
        RecyclerView recyclerViewDefinedLists = rootView.findViewById(R.id.recyclerViewDefinedLists);
        DividerItemDecoration itemDecoration = new
                DividerItemDecoration(mActivity, DividerItemDecoration.VERTICAL);
        if (ContextCompat.getDrawable(mActivity, R.drawable.list_divider) != null) {
            itemDecoration.setDrawable(Objects.requireNonNull(ContextCompat.getDrawable(mActivity, R.drawable.list_divider)));
            recyclerViewDefinedLists.addItemDecoration(itemDecoration);
        }

        // Create an adapter and pass in the defined lists data
        DefinedListsAdapter adapter = new DefinedListsAdapter(mActivity, definedLists);

        // Attach the adapter to the recyclerView to populate the items
        recyclerViewDefinedLists.setAdapter(adapter);

        // Attach a swipe listener to enable swipe to delete a list
        ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback(adapter);
        ItemTouchHelper touchHelper = new ItemTouchHelper(callback);
        touchHelper.attachToRecyclerView(recyclerViewDefinedLists);

        // Set a layout manager to position the items
        recyclerViewDefinedLists.setLayoutManager(new LinearLayoutManager(mActivity));

        // Set an on click listener on the add list button
        Button buttonAddList = rootView.findViewById(R.id.buttonAddList);
        buttonAddList.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mAppState.setSearchQuery("");

                // Switch to the list definition fragment
                mAppState.setPriorFragment(FragmentType.DEFINED_LISTS);
                mAppState.switchFragment(mActivity, FragmentType.NEW_DEFINITION);
            }
        });

       return rootView;
    }
}
