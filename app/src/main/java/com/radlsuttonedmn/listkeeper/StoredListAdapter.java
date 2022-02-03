package com.radlsuttonedmn.listkeeper;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import androidx.fragment.app.FragmentActivity;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter for the list of items in a defined list
 */

public class StoredListAdapter extends
        RecyclerView.Adapter<StoredListAdapter.ViewHolder> {

    private final FragmentActivity mActivity;
    private final AppActivityState mAppState;
    private int mIndex;
    private final StoredList mList;
    private final ListDefinition mListDefinition;

    // Constructor with the sorted list and the list definition as parameters
    StoredListAdapter(FragmentActivity activity, StoredList storedList, ListDefinition listDefinition) {

        mActivity = activity;
        mAppState = new AppActivityState();
        mList = storedList;
        mListDefinition = listDefinition;
    }

    // Method called by the item touch helper to display a dialog to confirm the deletion of a list item
    void onItemDismiss(int position) {

        mIndex = position;
        InputDialog dialog = new InputDialog(mActivity, mActivity.getString(R.string.warning), mActivity.getString(R.string.list_item),
                mActivity.getString(R.string.yes), mActivity.getString(R.string.no));
        dialog.setDialogCaller(this);
        dialog.showDialog();
    }

    // Method called by the confirm item deletion dialog to delete a list item
    void dialogYes() {

        if (mList.deleteItem(mIndex, mListDefinition) == ResultType.VALID) {
            notifyItemRemoved(mIndex);
        }
    }

    // Method called by the confirm item deletion dialog when the item deletion is cancelled
    void dialogNo() {

        notifyItemRemoved(mIndex + 1);
        notifyItemRangeChanged(mIndex, getItemCount());
    }

    // Override the parent class's getItemCount method
    @Override
    public int getItemCount() {
        return mList.getItemCount();
    }

    // Define the view holder as an inner class
    static class ViewHolder extends RecyclerView.ViewHolder {

        final TextView textViewItemMain;
        final TextView textViewItemMore;
        final ImageView imagePhoto;

        ViewHolder(View itemView) {

            super(itemView);
            textViewItemMain = itemView.findViewById(R.id.textViewItemMain);
            textViewItemMore = itemView.findViewById(R.id.textViewItemMore);
            imagePhoto = itemView.findViewById(R.id.imagePhoto);
        }
    }

    // Inflate the layout from XML and return the holder
    @Override
    @NonNull public StoredListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        LayoutInflater inflater = LayoutInflater.from(mActivity);

        // Inflate the custom layout
        View storedListView = inflater.inflate(R.layout.stored_list_item, parent, false);

        // Return a new holder instance
        return new StoredListAdapter.ViewHolder(storedListView);
    }

    // Populate data into the item through the holder
    @Override
    public void onBindViewHolder(@NonNull final StoredListAdapter.ViewHolder viewHolder, int position) {

        // Constants for setting the image size
        final int IMAGE_WIDTH = 72;
        final int IMAGE_HEIGHT = 72;

        try {

            // Get the data model based on position
            final DataRecord dataRecord = mList.getDataRecord(viewHolder.getAdapterPosition());

            // Reformat any date and time values from sortable format to display format
            List<String> dataValues = new ArrayList<>();
            for (int i = 0; i < mListDefinition.getFieldCount(); i++) {
                if (mListDefinition.getField(i).getFieldType() == FieldType.DATE) {
                    dataValues.add(DateValidator.formatForDisplay(dataRecord.getDataArray().get(i)));
                } else {
                    if (mListDefinition.getField(i).getFieldType() == FieldType.TIME) {
                        dataValues.add(TimeValidator.switchFormat(dataRecord.getDataArray().get(i)));
                    } else {
                        dataValues.add(dataRecord.getDataArray().get(i));
                    }
                }
            }

            // Set the maximum number of characters per line
            int maxLineLength;
            if (mListDefinition.isPhoto()) {
                maxLineLength = 18;
            } else {
                maxLineLength = 40;
            }

            // Determine the number of non-photo fields in the current list
            int nonPhotoFieldCount = mListDefinition.getFieldCount();
            if (mListDefinition.isPhoto()) {
                nonPhotoFieldCount--;
            }

            // Process the non-photo fields
            if (nonPhotoFieldCount > 0) {

                // Write the first data field value to item text main
                StringBuilder itemTextMain = new StringBuilder(dataValues.get(0));
                StringBuilder itemTextMore = new StringBuilder();

                int loopStart = 1;
                int lineLength = mList.getDataLength(0);

                if (nonPhotoFieldCount > 1) {

                    // If there is sufficient space available write the second data field value to item text main
                    if (lineLength + mList.getDataLength(1) < maxLineLength) {
                        itemTextMain.append("  \u2022  ");
                        itemTextMain.append(dataValues.get(1) == null ? "" : dataValues.get(1));
                        loopStart = 2;
                    }
                }

                if (nonPhotoFieldCount > loopStart) {

                    // Get the first data field value that was not written to item text main and write it to item text more
                    itemTextMore.append(dataValues.get(loopStart) == null ? "" : dataValues.get(loopStart));
                    lineLength = mList.getDataLength(loopStart++);

                    // Write all the subsequent data field values to item text more
                    for (int i = loopStart; i < nonPhotoFieldCount; i++) {
                        if (lineLength + mList.getDataLength(i) + 5 < maxLineLength) {

                            // There is sufficient room to write the next data field value to the same line
                            itemTextMore.append("  \u2022  ");
                            lineLength += 5;
                            itemTextMore.append(dataValues.get(i) == null ? "" : dataValues.get(i));
                            lineLength += mList.getDataLength(i);
                        } else {

                            // Need to write the next data field value to a new line
                            itemTextMore.append("\n");
                            itemTextMore.append(dataValues.get(i) == null ? "" : dataValues.get(i));
                            lineLength = mList.getDataLength(i);
                        }
                    }
                }

                // Display the first two fields of the data item
                if (itemTextMain.toString().equals("")) {
                    viewHolder.textViewItemMain.setVisibility(View.GONE);
                } else {
                    viewHolder.textViewItemMain.setText(itemTextMain.toString());
                }

                // Display the remaining fields in the data item
                if (itemTextMore.toString().equals("")) {
                    viewHolder.textViewItemMore.setVisibility(View.GONE);
                } else {

                    // Remove any blank lines from the end of the data item
                    String itemMore = itemTextMore.toString();
                    while (itemMore.endsWith("\n")) {
                        itemMore = itemMore.substring(0, itemMore.length() - 1);
                    }
                    viewHolder.textViewItemMore.setText(itemMore);
                }
            }

            // For photo fields, fetch the photo bitmap from the bitmap array
            if (mListDefinition.isPhoto()) {
                viewHolder.imagePhoto.setVisibility(View.VISIBLE);

                // Connect to the bitmap store
                BitmapStore bitmaps = mAppState.getBitmapStore();

                // Fetch the bitmap and display it in the image view
                Bitmap bitmap;
                String imageUriString = dataRecord.getDataArray().get(mListDefinition.getFieldCount() - 1);
                if (imageUriString == null) {
                    bitmap = null;
                } else {
                    if (bitmaps.contains(imageUriString)) {
                        bitmap = bitmaps.getBitmap(imageUriString);
                    } else {
                        bitmap = PhotoAdjuster.getAdjustedBitmap(mActivity, imageUriString, IMAGE_WIDTH, IMAGE_HEIGHT);
                        if (bitmap != null) {
                            bitmaps.addBitmap(imageUriString, bitmap);
                        }
                    }
                }
                if (bitmap == null) {
                    viewHolder.imagePhoto.setImageResource(R.drawable.photo_icon);
                } else {
                    viewHolder.imagePhoto.setImageBitmap(bitmap);

                    // Add an on click listener to the photo image
                    viewHolder.imagePhoto.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            // Create an intent to show the photograph in the photo viewer
                            Intent intent = new Intent();
                            intent.setAction(Intent.ACTION_VIEW);
                            Uri photoUri = Uri.parse(dataRecord.getDataArray().get(mListDefinition.getFieldCount() - 1));
                            intent.setDataAndType(photoUri, "image/*");
                            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            mActivity.startActivity(intent);
                        }
                    });
                }
            }

            // Add an on click listener to the list item to allow editing the item
            viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mAppState.setDataRecord(dataRecord);
                    mAppState.setFilterSettings(null);

                    // Switch to the edit item fragment
                    mAppState.switchFragment(mActivity, FragmentType.EDIT_ITEM);
                }
            });
        } catch (Exception e) {

            // Bail out and switch to the defined lists fragment
            mAppState.switchFragment(mActivity, FragmentType.DEFINED_LISTS);
        }
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
