package com.radlsuttonedmn.listkeeper;

import android.annotation.SuppressLint;
import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Provide the adapter for the list of defined lists
 */

public class DefinedListsAdapter extends
            RecyclerView.Adapter<DefinedListsAdapter.ViewHolder> {

    private final FragmentActivity mActivity;
    private final DefinedLists mDefinedLists;
    private int mIndex;

    // Constructor with defined lists class as a parameter
    DefinedListsAdapter(FragmentActivity activity, DefinedLists definedLists) {

        mActivity = activity;
        mDefinedLists = definedLists;
    }

    // Method called by the item touch helper that displays a confirmation dialog for deleting a defined list
    void onItemDismiss(int position) {

        mIndex = position;
        String message = mActivity.getString(R.string.remove_list) + " " + mDefinedLists.getListName(mIndex) +
                " " + mActivity.getString(R.string.delete_items);
        InputDialog dialog = new InputDialog(mActivity, mActivity.getString(R.string.warning), message,
                mActivity.getString(R.string.yes), mActivity.getString(R.string.no));
        dialog.setDialogCaller(this);
        dialog.showDialog();
    }

    // Method called by the input dialog to delete a defined list
    void dialogYes() {

        // Delete the selected defined list
        String listName = mDefinedLists.getListName(mIndex);
        mDefinedLists.deleteList(mIndex);
        notifyItemRemoved(mIndex);

        // Delete any sorts associated with the selected defined list
        ListSorts sorts = new ListSorts(mActivity, listName);
        if (sorts.isAnySort()) {
            sorts.removeAllSorts();
        }

        // Delete any filters associated with the selected defined list
        ListFilters filters = new ListFilters(mActivity, listName);
        if (filters.isAnyFilter()) {
            filters.removeAllFilters();
        }
    }

    // Method called by the input dialog when deleting a defined list is cancelled
    void dialogNo() {

        notifyItemRemoved(mIndex + 1);
        notifyItemRangeChanged(mIndex, getItemCount());
    }

    // Override the parent class's getItemCount method
    @Override
    public int getItemCount() {
            return mDefinedLists.getListCount();
        }

    // Define the view holder as an inner class
    static class ViewHolder extends RecyclerView.ViewHolder {

        final TextView textViewListName;

        ViewHolder(View itemView) {

            super(itemView);
            textViewListName = itemView.findViewById(R.id.textViewItemText);
        }
    }

    // Inflate the layout from XML and return the holder
    @Override
    @NonNull
    public DefinedListsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        LayoutInflater inflater = LayoutInflater.from(mActivity);

        // Inflate the custom layout
        View definedListsView = inflater.inflate(R.layout.single_text_view, parent, false);

        // Return a new holder instance
        return new ViewHolder(definedListsView);
    }

    // Populate data into the item through the holder
    @Override
    @SuppressLint("InflateParams")
    public void onBindViewHolder(@NonNull final DefinedListsAdapter.ViewHolder viewHolder, int position) {

        // Get the data model based on position
        final String listName = mDefinedLists.getListName(viewHolder.getAdapterPosition());

        // Set item views based on the views and data model
        viewHolder.textViewListName.setText(listName);

        // Set an on click listener on each defined list item
        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                AppActivityState appState = new AppActivityState();

                // Go to the Stored List fragment for the selected list
                appState.setListName(listName);
                appState.setDataRecord(null);
                appState.setSearchQuery("");
                appState.switchFragment(mActivity, FragmentType.STORED_LIST);
            }
        });
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }
}
