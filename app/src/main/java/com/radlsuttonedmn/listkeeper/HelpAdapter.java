package com.radlsuttonedmn.listkeeper;

import android.annotation.SuppressLint;
import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

/**
 * Adapter for the help text items associated with a fragment
 */

public class HelpAdapter extends RecyclerView.Adapter<HelpAdapter.ViewHolder> {

    private final FragmentActivity mActivity;
    private final List<HelpTextItem> mHelpText;

    // Constructor with the help text item as a parameter
    HelpAdapter(FragmentActivity activity, List<HelpTextItem> helpText) {

        mActivity = activity;
        mHelpText = helpText;
    }

    // Override the parent class's getItemCount method
    @Override
    public int getItemCount() {
        return mHelpText.size();
    }

    // Define the view holder as an inner class
    static class ViewHolder extends RecyclerView.ViewHolder {

        final TextView textViewHelpOne;
        final ImageView imageViewIcon;
        final TextView textViewHelpTwo;

        ViewHolder(View itemView) {

            super(itemView);
            textViewHelpOne = itemView.findViewById(R.id.textViewHelpOne);
            imageViewIcon = itemView.findViewById(R.id.imageViewIcon);
            textViewHelpTwo = itemView.findViewById(R.id.textViewHelpTwo);
        }
    }

    // Inflate the layout from XML and return the holder
    @Override
    @NonNull
    public HelpAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        LayoutInflater inflater = LayoutInflater.from(mActivity);

        // Inflate the custom layout
        View helpView = inflater.inflate(R.layout.help_item_layout, parent, false);

        // Return a new holder instance
        return new ViewHolder(helpView);
    }

    // Populate data into the item through the holder
    @Override
    @SuppressLint("InflateParams")
    public void onBindViewHolder(@NonNull final HelpAdapter.ViewHolder viewHolder, int position) {

        // Get the data model based on position
        String helpItemOne = mHelpText.get(viewHolder.getAdapterPosition()).getHelpTextOne();
        String helpItemTwo = mHelpText.get(viewHolder.getAdapterPosition()).getHelpTextTwo();

        // Set item views based on the views and data model
        if (!helpItemOne.equals("")) {
            viewHolder.textViewHelpOne.setVisibility(View.VISIBLE);
            viewHolder.imageViewIcon.setVisibility(View.VISIBLE);
            viewHolder.textViewHelpOne.setText(helpItemOne);
            viewHolder.imageViewIcon.setImageResource(mHelpText.get(viewHolder.getAdapterPosition()).getHelpResourceID());
        } else {
            viewHolder.textViewHelpOne.setVisibility(View.GONE);
            viewHolder.imageViewIcon.setVisibility(View.GONE);
        }
        viewHolder.textViewHelpTwo.setText(helpItemTwo);
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
