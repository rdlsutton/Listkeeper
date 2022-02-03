package com.radlsuttonedmn.listkeeper;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Adapter for the list of fields included in a list definition
 */

public class DefinitionAdapter extends
        RecyclerView.Adapter<DefinitionAdapter.ViewHolder> {

    private final FragmentActivity mActivity;
    private final ListDefinition mListDefinition;
    private final AppActivityState mAppState;

    // Constructor with the list definition and sorting criteria as parameters
    DefinitionAdapter(FragmentActivity activity, ListDefinition listDefinition) {

        mActivity = activity;
        mListDefinition = listDefinition;
        mAppState = new AppActivityState();
    }

    // Method called by the item touch helper to delete a field from the list definition
    void onItemDismiss(int position) {

        mListDefinition.deleteField(position);
        notifyItemRemoved(position);
    }

    // Override the parent class's getItemCount method
    @Override
    public int getItemCount() {
        if (mAppState.getActiveFragment() == FragmentType.EDIT_DEFINITION) {
            return mListDefinition.getNewCount();
        } else {
            return mListDefinition.getFieldCount();
        }
    }

    // Define the view holder as an inner class
    static class ViewHolder extends RecyclerView.ViewHolder {

        final TextView textViewFieldName;
        final TextView textViewFieldType;
        final TextView textViewFieldSorting;

        ViewHolder(View itemView) {

            super(itemView);
            textViewFieldName = itemView.findViewById(R.id.textViewFieldName);
            textViewFieldType = itemView.findViewById(R.id.textViewFieldType);
            textViewFieldSorting = itemView.findViewById(R.id.textViewFieldSorting);
        }
    }

    // Inflate the layout from XML and return the holder
    @Override
    @NonNull
    public DefinitionAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        LayoutInflater inflater = LayoutInflater.from(mActivity);

        // Inflate the custom layout
        View fieldsListView = inflater.inflate(R.layout.definition_field, parent, false);

        // Return a new holder instance
        return new ViewHolder(fieldsListView);
    }

    // Populate data into the item through the holder
    @Override
    public void onBindViewHolder(@NonNull final DefinitionAdapter.ViewHolder viewHolder, int position) {

        // Get the data model based on position
        String fieldName;
        FieldType fieldType;
        String fieldSorting;

        if (mAppState.getActiveFragment() == FragmentType.EDIT_DEFINITION) {
            NewListField newListField = mListDefinition.getNewField(viewHolder.getAdapterPosition());
            fieldName = newListField.getNewFieldName();
            fieldType = newListField.getNewFieldType();
            fieldSorting = newListField.getFieldSorting();
        } else {
            ListField listField = mListDefinition.getField(viewHolder.getAdapterPosition());
            fieldName = listField.getFieldName();
            fieldType = listField.getFieldType();
            fieldSorting = listField.getFieldSorting();
        }

        // Set up the list field text views
        viewHolder.textViewFieldName.setText(fieldName);
        switch (fieldType) {
            case DATE:
                viewHolder.textViewFieldType.setText(mActivity.getString(R.string.type_date));
                break;
            case EMAIL:
                viewHolder.textViewFieldType.setText(mActivity.getString(R.string.type_email));
                break;
            case NUMBER:
                viewHolder.textViewFieldType.setText(mActivity.getString(R.string.type_number));
                break;
            case PHONE:
                viewHolder.textViewFieldType.setText(mActivity.getString(R.string.type_phone));
                break;
            case PHOTO:
                viewHolder.textViewFieldType.setText(mActivity.getString(R.string.type_photo));
                break;
            case TEXT:
                viewHolder.textViewFieldType.setText(mActivity.getString(R.string.type_text));
                break;
            case TIME:
                viewHolder.textViewFieldType.setText(mActivity.getString(R.string.type_time));
                break;
            case URL:
                viewHolder.textViewFieldType.setText(mActivity.getString(R.string.type_url));
        }
        String sortText;
        if (fieldSorting == null || fieldSorting.equals("Off")) {
            sortText = mActivity.getString(R.string.sorting) + " Off";
        } else {
            sortText = mActivity.getString(R.string.sort) + " " + fieldSorting;
        }
        viewHolder.textViewFieldSorting.setText(sortText);

        // Set an on click listener on the adapter item to allow editing the list field
        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                mListDefinition.setFieldId(viewHolder.getAdapterPosition());

                // Switch to the edit field fragment
                mAppState.setPriorFragment(mAppState.getActiveFragment());
                mAppState.switchFragment(mActivity, FragmentType.EDIT_FIELD);
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
