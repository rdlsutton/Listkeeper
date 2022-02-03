package com.radlsuttonedmn.listkeeper;

import android.Manifest;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

/**
 * Main activity for the ListKeeper app
 */

public class MainActivity extends AppCompatActivity {

    // Override the parent class's onCreate method
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        try {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.layout_main);

            // Set up permission to read from external storage
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) ==
                    PackageManager.PERMISSION_DENIED) {

                // Ask for permission
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
            }

            // Check for permission
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                    PackageManager.PERMISSION_DENIED) {

                // Ask for permission
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            }

            // Add a tool bar to use as an action bar at the top of the screen
            androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.appToolbar);
            if (toolbar != null) {
                setSupportActionBar(toolbar);
                if (getSupportActionBar() != null) {
                    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                    getSupportActionBar().setDisplayShowHomeEnabled(true);
                }
            }

            // Connect to the app state
            AppActivityState appState = new AppActivityState();

            // If the app was completely closed, default to defined lists fragment because stored list fragment will not reload
            appState.setActiveFragment(FragmentType.DEFINED_LISTS);

            // Set up the list database
            DBHelper listDB = DBHelper.getInstance(this);

            // Save the current app settings to the database
            listDB.saveAppState(appState);
            listDB.close();
        } catch (Exception e) {
            Toast.makeText(this, getString(R.string.create_fail), Toast.LENGTH_SHORT).show();
            finishAffinity();
            System.exit(0);
        }
    }

    // Override the parent class's onResume method
    @Override
    protected void onResume() {

        // Connect to the app state
        AppActivityState appState = new AppActivityState();

        try {
            super.onResume();

            // Set up the list database
            DBHelper listDB = DBHelper.getInstance(this);

            // Fetch the app settings from the database
            listDB.getAppState();

            // Determine if this device has a phone and save the results in AppActivityState
            appState.setPhoneAvailable(this.getPackageManager().hasSystemFeature(PackageManager.FEATURE_TELEPHONY));

            // Attach the correct active fragment to the layout
            switch (appState.getActiveFragment()) {
                case EDIT_DEFINITION:
                case EDIT_ITEM:
                case EDIT_FIELD:
                case NEW_ITEM:
                case STORED_LIST:
                    if (listDB.listEncrypted(appState.getListName())) {
                        appState.switchFragment(this, FragmentType.DEFINED_LISTS);
                    } else {
                        appState.switchFragment(this, FragmentType.STORED_LIST);
                    }
                    break;
                case EDIT_ITEM_RETURN:
                    appState.switchFragment(this, FragmentType.EDIT_ITEM);
                    break;
                case DEFINED_LISTS:
                case NONE:
                case NEW_DEFINITION:
                    appState.switchFragment(this, FragmentType.DEFINED_LISTS);
                    break;
                case NEW_ITEM_RETURN:
                    appState.switchFragment(this, FragmentType.NEW_ITEM);
                    break;
                case STORED_LIST_RETURN:
                    appState.switchFragment(this, FragmentType.STORED_LIST);
                    break;
                default:
                    appState.switchFragment(this, FragmentType.DEFINED_LISTS);
            }
        } catch (Exception e) {
            Toast.makeText(this, getString(R.string.resume_fail), Toast.LENGTH_SHORT).show();
            finishAffinity();
            System.exit(0);
        }
    }

    // Override the parent class's onPause method
    @Override
    protected void onPause() {
        super.onPause();

        // Connect to the list database
        DBHelper listDB = DBHelper.getInstance(this);

        // Connect to the app state
        AppActivityState appState = new AppActivityState();

        // Clear any decrypted data for all encrypted lists
        listDB.clearDecryptedData();
        appState.setPassword("");

        // If the help fragment is active set active fragment to the correct return fragment
        if (appState.getActiveFragment() == FragmentType.HELP) {
            switch (appState.getHelpPriorFragment()) {
                case NONE:
                case DEFINED_LISTS:
                case NEW_DEFINITION:
                    appState.setActiveFragment(FragmentType.DEFINED_LISTS);
                    break;
                case EDIT_DEFINITION:
                case EDIT_ITEM:
                case EDIT_FIELD:
                case NEW_ITEM:
                case STORED_LIST:
                    appState.setActiveFragment(FragmentType.STORED_LIST);
            }
        }

        // Save the current app settings to the database
        listDB.saveAppState(appState);
        listDB.close();
    }

    // Override the parent class's onCreateOptionsMenu method
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        final FragmentActivity activity = this;

        // Connect to the app state
        final AppActivityState appState = new AppActivityState();

        // Inflate the menu, this adds items to the action bar if it is present
        this.getMenuInflater().inflate(R.menu.toolbar_menu, menu);

        // For each fragment, turn off the menu options that do not apply to the fragment
        MenuItem encrypt = menu.findItem(R.id.encrypt);
        MenuItem help = menu.findItem(R.id.help);
        MenuItem save = menu.findItem(R.id.save);
        MenuItem search = menu.findItem(R.id.search);
        switch (appState.getActiveFragment()) {
            case DEFINED_LISTS:
            case STORED_LIST:
            case STORED_LIST_RETURN:
                encrypt.setVisible(false);
                save.setVisible(false);
                break;
            case EDIT_DEFINITION:
            case NEW_DEFINITION:
                search.setVisible(false);
                break;
            case EDIT_FIELD:
            case EDIT_ITEM:
            case NEW_ITEM:
                encrypt.setVisible(false);
                search.setVisible(false);
                break;
            case HELP:
                encrypt.setVisible(false);
                help.setVisible(false);
                save.setVisible(false);
                search.setVisible(false);
                break;
        }

        // Initialize the search manager
        SearchManager manager = (SearchManager)getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView)menu.findItem(R.id.search).getActionView();
        if (manager != null) {
            searchView.setSearchableInfo(manager.getSearchableInfo(getComponentName()));

            // Add an on query text listener to the search manager
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

                @Override
                public boolean onQueryTextSubmit(String searchQuery) {
                    if (!searchQuery.trim().equals("")) {
                        appState.setSearchQuery(searchQuery.trim());

                        // Reset the current fragment
                        appState.switchFragment(activity, appState.getActiveFragment());
                    }
                    return false;
                }

                @Override
                public boolean onQueryTextChange(String searchQuery) {

                    // Allow user to reset the list by removing the search query
                    if (searchQuery.trim().equals("")) {
                        appState.setSearchQuery("");

                        // Reset the current fragment
                        appState.switchFragment(activity, appState.getActiveFragment());
                    }
                    return false;
                }
            });
        }
        return true;
    }

    // Override the parent class's onOptionsItemSelected method
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        // Connect to the app state
        final AppActivityState appState = new AppActivityState();

        if (item.getItemId() == R.id.encrypt) {
            if (appState.getActiveFragment() == FragmentType.NEW_DEFINITION) {
                NewDefinitionFragment fragment = (NewDefinitionFragment)
                        getSupportFragmentManager()
                                .findFragmentByTag("NewDefinition");
                if (fragment != null) {
                    fragment.setEncryption();
                }
            }
            if (appState.getActiveFragment() == FragmentType.EDIT_DEFINITION) {
                EditDefinitionFragment fragment = (EditDefinitionFragment)
                        getSupportFragmentManager()
                                .findFragmentByTag("EditDefinition");
                if (fragment != null) {
                    fragment.setEncryption();
                }
            }
        }
        if (item.getItemId() == R.id.help) {

            // Save the current fragment in order to return to it when the help fragment is closed
            appState.setHelpPriorFragment(appState.getActiveFragment());
            appState.switchFragment(this, FragmentType.HELP);
        }
        if (item.getItemId() == R.id.save) {
            switch (appState.getActiveFragment()) {
                case EDIT_DEFINITION:
                    EditDefinitionFragment editDefinitionFragment = (EditDefinitionFragment)
                            getSupportFragmentManager().findFragmentByTag("EditDefinition");
                    if (editDefinitionFragment != null) {
                        editDefinitionFragment.processUpdate();
                    }
                    break;
                case EDIT_FIELD:
                    EditFieldFragment editFieldFragment = (EditFieldFragment)
                            getSupportFragmentManager().findFragmentByTag("EditField");
                    if (editFieldFragment != null) {
                        editFieldFragment.updateListField();
                    }
                    break;
                case EDIT_ITEM:
                case NEW_ITEM:
                    ItemFragment itemFragment = (ItemFragment)
                            getSupportFragmentManager().findFragmentByTag("Item");
                    if (itemFragment != null) {
                        itemFragment.processDataValues();
                    }
                    break;
                case NEW_DEFINITION:
                    NewDefinitionFragment newDefinitionFragment = (NewDefinitionFragment)
                            getSupportFragmentManager().findFragmentByTag("NewDefinition");
                    if (newDefinitionFragment != null) {
                        newDefinitionFragment.saveNewList();
                    }
                    break;
            }
        }
        if (item.getItemId() == R.id.search) {
            return super.onOptionsItemSelected(item);
        }
        if (item.getItemId() != R.id.encrypt && item.getItemId() != R.id.help && item.getItemId() != R.id.save
                && item.getItemId() != R.id.search) {

            // Back arrow was pressed, return to the correct fragment
            processBackPressed();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        processBackPressed();
    }

    // Return to the proper fragment when the back arrow is pressed
    private void processBackPressed() {

        // Connect to the app state
        final AppActivityState appState = new AppActivityState();

        appState.setSearchQuery("");
        switch (appState.getActiveFragment()) {
            case DEFINED_LISTS:
                finishAffinity();
                System.exit(0);
                break;
            case EDIT_DEFINITION:
            case EDIT_ITEM:
            case NEW_ITEM:
                appState.switchFragment(this, FragmentType.STORED_LIST);
                break;
            case EDIT_FIELD:
                FragmentType priorFragment = appState.getPriorFragment();
                appState.setPriorFragment(FragmentType.EDIT_FIELD);
                appState.switchFragment(this, priorFragment);
                break;
            case HELP:
                appState.switchFragment(this, appState.getHelpPriorFragment());
                break;
            default:
                appState.switchFragment(this, FragmentType.DEFINED_LISTS);
        }
    }

    // Process the results from the intent
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        // Connect to the app state
        final AppActivityState appState = new AppActivityState();

        // Constant for photo selection intent
        final int NEW_PHOTO = 1;
        final int EDIT_PHOTO = 2;
        final int OPEN_CSV = 3;

        // Verify that the intent returned valid results
        if (resultCode != RESULT_OK) {
            super.onActivityResult(requestCode, resultCode, data);
            return;
        }

        // Verify that the activity is returning from photo or file selection
        if (requestCode != NEW_PHOTO && requestCode != EDIT_PHOTO &&
                requestCode != OPEN_CSV) {
            super.onActivityResult(requestCode, resultCode, data);
            return;
        }

        if (requestCode == OPEN_CSV) {

            // Clear out any existing file URI
            appState.setFileURI("");

            try {
                if (data != null && data.getData() != null) {

                    // The returned file URI
                    Uri pickedUri = data.getData();
                    if (pickedUri != null) {
                        MimeTypeMap mime = MimeTypeMap.getSingleton();
                        String fileType = mime.getExtensionFromMimeType(getContentResolver().getType(pickedUri));
                        if (fileType != null) {
                            if (fileType.trim().toLowerCase().equals("csv")) {
                                appState.setFileURI(pickedUri.toString());
                            } else {
                                Toast.makeText(this, getString(R.string.file_type), Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                }
            } catch (Exception e) {
                Toast.makeText(this, getString(R.string.file_read_error), Toast.LENGTH_SHORT).show();
            }
        } else {

            // Initialize the image path in the data record to blank
            appState.getDataRecord().getDataArray().set(appState.getDataRecord().getFieldCount() - 1, "");

            try {
                if (data != null && data.getData() != null) {

                    // The returned picture URI
                    Uri pickedUri = data.getData();
                    if (pickedUri != null) {

                        // Make the URI persistable so that the photos can still be read after app is
                        // close and restarted
                        getContentResolver().takePersistableUriPermission(pickedUri,
                                Intent.FLAG_GRANT_READ_URI_PERMISSION);

                        // Save the URI as a string in the AppActivityState data record
                        appState.getDataRecord().getDataArray().set(appState.getDataRecord().getFieldCount() - 1,
                                pickedUri.toString());
                    }
                }
            } catch (Exception e) {
                appState.getDataRecord().getDataArray().set(appState.getDataRecord().getFieldCount() - 1, "");
            }
        }

        // Call the superclass method
        super.onActivityResult(requestCode, resultCode, data);
    }
}
