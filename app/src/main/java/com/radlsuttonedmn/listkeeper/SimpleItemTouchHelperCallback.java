package com.radlsuttonedmn.listkeeper;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.ItemTouchHelper;

/**
 * Support the implementation of swiping functions on the app's fragments
 */

class SimpleItemTouchHelperCallback extends ItemTouchHelper.Callback {

    private final String mActionType;
    private DefinedListsAdapter mDefinedListsAdapter = null;
    private DefinitionAdapter mDefinitionAdapter = null;
    private StoredListAdapter mStoredListAdapter = null;

    // Constructor for the defined lists adapter
    SimpleItemTouchHelperCallback(DefinedListsAdapter adapter) {
        mDefinedListsAdapter = adapter;
        mActionType = "delete_list";
    }

    // Constructor for the edit definition adapter
    SimpleItemTouchHelperCallback(DefinitionAdapter adapter) {
        mDefinitionAdapter = adapter;
        mActionType = "definition";
    }

    // Constructor for the stored list adapter
    SimpleItemTouchHelperCallback(StoredListAdapter adapter) {
        mStoredListAdapter = adapter;
        mActionType = "delete_item";
    }

    // Disable long press drag
    @Override
    public boolean isLongPressDragEnabled() { return false; }

    // Enable swiping right and left
    @Override
    public boolean isItemViewSwipeEnabled() {
        return true;
    }

    // Override the parent class's getMovementFlags method
    @Override
    public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
        int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
        int swipeFlags = ItemTouchHelper.START | ItemTouchHelper.END;
        return makeMovementFlags(dragFlags, swipeFlags);
    }

    // Override the parent class's onMove method
    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder,
                                @NonNull RecyclerView.ViewHolder target) { return true; }

    // Execute the calling adapter's onItemDismiss method when a swipe occurs
    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
        switch (mActionType) {
            case "delete_item":
                mStoredListAdapter.onItemDismiss(viewHolder.getAdapterPosition());
                break;
            case "delete_list":
                mDefinedListsAdapter.onItemDismiss(viewHolder.getAdapterPosition());
                break;
            case "definition":
                mDefinitionAdapter.onItemDismiss(viewHolder.getAdapterPosition());
                break;
        }
    }
}
