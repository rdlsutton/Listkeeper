package com.radlsuttonedmn.listkeeper;

import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.view.inputmethod.InputMethodManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Display the help text associated with the active fragment
 */

public class HelpFragment extends Fragment {

    private FragmentActivity mActivity;
    private AppActivityState mAppState;

    // Fetch the help text for the previous fragment from the string resources file
    private void getHelpText(List<HelpTextItem> helpText) {

        switch (mAppState.getHelpPriorFragment()) {
            case DEFINED_LISTS:
                helpText.add(new HelpTextItem(getString(R.string.tap_on), R.drawable.search_icon, getString(R.string.defined_lists_help_1)));
                helpText.add(new HelpTextItem("", 0, getString(R.string.defined_lists_help_2)));
                helpText.add(new HelpTextItem("", 0, getString(R.string.defined_lists_help_3)));
                helpText.add(new HelpTextItem("", 0, getString(R.string.defined_lists_help_4)));
                break;
            case EDIT_FIELD:
                helpText.add(new HelpTextItem("", 0, getString(R.string.edit_field_help_1)));
                helpText.add(new HelpTextItem("", 0, getString(R.string.edit_field_help_2)));
                helpText.add(new HelpTextItem("", 0, getString(R.string.edit_field_help_3)));
                helpText.add(new HelpTextItem("", 0, getString(R.string.edit_field_help_4)));
                helpText.add(new HelpTextItem("", 0, getString(R.string.edit_field_help_5)));
                helpText.add(new HelpTextItem("", 0, getString(R.string.edit_field_help_6)));
                helpText.add(new HelpTextItem("", 0, getString(R.string.edit_field_help_7)));
                helpText.add(new HelpTextItem("", 0, getString(R.string.edit_field_help_8)));
                helpText.add(new HelpTextItem("", 0, getString(R.string.edit_field_help_9)));
                helpText.add(new HelpTextItem("", 0, getString(R.string.edit_field_help_10)));
                helpText.add(new HelpTextItem("", 0, getString(R.string.edit_field_help_11)));
                helpText.add(new HelpTextItem("", 0, getString(R.string.edit_field_help_12)));
                helpText.add(new HelpTextItem("", 0, getString(R.string.edit_field_help_13)));
                helpText.add(new HelpTextItem("", 0, getString(R.string.edit_field_help_14)));
                helpText.add(new HelpTextItem("", 0, getString(R.string.edit_field_help_15)));
                helpText.add(new HelpTextItem("", 0, getString(R.string.edit_field_help_16)));
                helpText.add(new HelpTextItem("", 0, getString(R.string.edit_field_help_17)));
                helpText.add(new HelpTextItem("", 0, getString(R.string.edit_field_help_18)));
                helpText.add(new HelpTextItem("", 0, getString(R.string.edit_field_help_19)));
                if (mAppState.getPriorFragment() == FragmentType.EDIT_DEFINITION) {
                    helpText.add(new HelpTextItem("", 0, getString(R.string.edit_field_help_20)));
                }
                break;
            case EDIT_DEFINITION:
            case NEW_DEFINITION:
                helpText.add(new HelpTextItem(getString(R.string.tap_on), R.drawable.lock_icon,
                        getString(R.string.new_definition_help_1)));
                helpText.add(new HelpTextItem("", 0, getString(R.string.new_definition_help_2)));
                helpText.add(new HelpTextItem(getString(R.string.tap_on), R.drawable.save_icon,
                        getString(R.string.new_definition_help_3)));
                helpText.add(new HelpTextItem("", 0, getString(R.string.new_definition_help_4)));
                helpText.add(new HelpTextItem("", 0, getString(R.string.new_definition_help_5)));
                helpText.add(new HelpTextItem("", 0, getString(R.string.new_definition_help_6)));
                break;
            case EDIT_ITEM:
                helpText.add(new HelpTextItem(getString(R.string.tap_on), R.drawable.save_icon,
                        getString(R.string.edit_item_help_1)));
                helpText.add(new HelpTextItem("", 0, getString(R.string.edit_item_help_2)));
                helpText.add(new HelpTextItem("", 0, getString(R.string.edit_item_help_3)));
                helpText.add(new HelpTextItem("", 0, getString(R.string.edit_item_help_4)));
                helpText.add(new HelpTextItem("", 0, getString(R.string.edit_item_help_5)));
                helpText.add(new HelpTextItem(getString(R.string.tap_on), R.drawable.photo_icon_white,
                        getString(R.string.edit_item_help_6)));
                helpText.add(new HelpTextItem("", 0, getString(R.string.edit_item_help_7)));
                helpText.add(new HelpTextItem(getString(R.string.tap_on), R.drawable.phone_icon_white,
                        getString(R.string.edit_item_help_8)));
                helpText.add(new HelpTextItem("", 0, getString(R.string.edit_item_help_9)));
                helpText.add(new HelpTextItem(getString(R.string.tap_on), R.drawable.web_icon_white,
                        getString(R.string.edit_item_help_10)));
                break;
            case NEW_ITEM:
                helpText.add(new HelpTextItem(getString(R.string.tap_on), R.drawable.save_icon,
                        getString(R.string.new_item_help_1)));
                helpText.add(new HelpTextItem("", 0, getString(R.string.new_item_help_2)));
                helpText.add(new HelpTextItem("", 0, getString(R.string.new_item_help_3)));
                helpText.add(new HelpTextItem(getString(R.string.tap_on), R.drawable.photo_icon_white,
                        getString(R.string.new_item_help_4)));
                helpText.add(new HelpTextItem("", 0, getString(R.string.new_item_help_5)));
                break;
            case STORED_LIST:
                helpText.add(new HelpTextItem(getString(R.string.tap_on), R.drawable.search_icon,
                        getString(R.string.stored_list_help_1)));
                helpText.add(new HelpTextItem("", 0, getString(R.string.stored_list_help_2)));
                helpText.add(new HelpTextItem("", 0, getString(R.string.stored_list_help_3)));
                helpText.add(new HelpTextItem("", 0, getString(R.string.stored_list_help_4)));
                helpText.add(new HelpTextItem("", 0, getString(R.string.stored_list_help_5)));
                helpText.add(new HelpTextItem("", 0, getString(R.string.stored_list_help_6)));
                helpText.add(new HelpTextItem("", 0, getString(R.string.stored_list_help_7)));
                helpText.add(new HelpTextItem("", 0, getString(R.string.stored_list_help_8)));
                helpText.add(new HelpTextItem("", 0, getString(R.string.stored_list_help_9)));
                helpText.add(new HelpTextItem(getString(R.string.tap_on), R.drawable.import_file_icon_white,
                        getString(R.string.stored_list_help_10)));
                helpText.add(new HelpTextItem(getString(R.string.tap_on), R.drawable.email_icon_white,
                        getString(R.string.stored_list_help_11)));
                helpText.add(new HelpTextItem("", 0, getString(R.string.stored_list_help_12)));
                helpText.add(new HelpTextItem("", 0, getString(R.string.stored_list_help_13)));
                helpText.add(new HelpTextItem("", 0, getString(R.string.stored_list_help_14)));
                helpText.add(new HelpTextItem("", 0, getString(R.string.stored_list_help_15)));
                helpText.add(new HelpTextItem("", 0, getString(R.string.stored_list_help_16)));
                helpText.add(new HelpTextItem("", 0, getString(R.string.stored_list_help_17)));
                helpText.add(new HelpTextItem("", 0, getString(R.string.stored_list_help_18)));
                helpText.add(new HelpTextItem("", 0, getString(R.string.stored_list_help_19)));
                helpText.add(new HelpTextItem("", 0, getString(R.string.stored_list_help_20)));
                helpText.add(new HelpTextItem("", 0, getString(R.string.stored_list_help_21)));
                helpText.add(new HelpTextItem("", 0, getString(R.string.stored_list_help_22)));
                helpText.add(new HelpTextItem("", 0, getString(R.string.stored_list_help_23)));
                helpText.add(new HelpTextItem("", 0, getString(R.string.stored_list_help_24)));
                helpText.add(new HelpTextItem("", 0, getString(R.string.stored_list_help_25)));
                helpText.add(new HelpTextItem("", 0, getString(R.string.stored_list_help_26)));
                helpText.add(new HelpTextItem("", 0, getString(R.string.stored_list_help_27)));
                helpText.add(new HelpTextItem("", 0, getString(R.string.stored_list_help_28)));
                helpText.add(new HelpTextItem("", 0, getString(R.string.stored_list_help_29)));
                helpText.add(new HelpTextItem("", 0, getString(R.string.stored_list_help_30)));
                helpText.add(new HelpTextItem("", 0, getString(R.string.stored_list_help_31)));
                helpText.add(new HelpTextItem("", 0, getString(R.string.stored_list_help_32)));
                helpText.add(new HelpTextItem("", 0, getString(R.string.stored_list_help_33)));
                helpText.add(new HelpTextItem("", 0, getString(R.string.stored_list_help_34)));
                helpText.add(new HelpTextItem("", 0, getString(R.string.stored_list_help_35)));
                helpText.add(new HelpTextItem("", 0, getString(R.string.stored_list_help_36)));
                helpText.add(new HelpTextItem("", 0, getString(R.string.stored_list_help_37)));
                helpText.add(new HelpTextItem("", 0, getString(R.string.stored_list_help_38)));
                helpText.add(new HelpTextItem("", 0, getString(R.string.stored_list_help_39)));
                helpText.add(new HelpTextItem("", 0, getString(R.string.stored_list_help_40)));
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

        // Update the current app state
        mAppState = new AppActivityState();
        mAppState.setActiveFragment(FragmentType.HELP);

        // Connect to the main toolbar and set the title to the list name
        setHasOptionsMenu(true);
        Toolbar mainToolbar = mActivity.findViewById(R.id.appToolbar);
        if (mainToolbar != null) {
            mainToolbar.setTitle("");
            mainToolbar.setVisibility(View.VISIBLE);
            TextView textViewTitle = mainToolbar.findViewById(R.id.textViewTitle);
            if (textViewTitle != null) {
                if (mAppState.getHelpPriorFragment() == FragmentType.DEFINED_LISTS ||
                        mAppState.getHelpPriorFragment() == FragmentType.NEW_DEFINITION) {
                    textViewTitle.setText(getString(R.string.app_name));
                } else {
                    textViewTitle.setText(mAppState.getListName());
                }
            }
        }
    }

   // Override the parent class's onCreateView method
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Retrieve the help text from the strings resource
        List<HelpTextItem> helpText = new ArrayList<>();
        getHelpText(helpText);

        View rootView = inflater.inflate(R.layout.help_layout, container, false);

        // Suppress appearance of the soft keyboard
        InputMethodManager imm = (InputMethodManager)mActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(rootView.getWindowToken(), 0);
        }

        // Add a recyclerView in the activity layout
        RecyclerView recyclerViewHelp = rootView.findViewById(R.id.recyclerViewHelp);

        // Create an adapter and pass in the help text
        HelpAdapter adapter = new HelpAdapter(mActivity, helpText);

        // Attach the adapter to the recyclerView to populate the items
        recyclerViewHelp.setAdapter(adapter);

        // Set a layout manager to position the items
        recyclerViewHelp.setLayoutManager(new LinearLayoutManager(mActivity));
        return rootView;
    }
}
