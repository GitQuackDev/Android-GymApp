package com.example.aimingfitness;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView; // Changed from ImageButton
import android.widget.PopupMenu; // Added for PopupMenu
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class ProgressAdapter extends RecyclerView.Adapter<ProgressAdapter.ProgressViewHolder> {

    private Context context;
    private List<ProgressEntry> progressList;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
    private OnProgressItemInteractionListener listener;

    // Interface for handling item interactions
    public interface OnProgressItemInteractionListener {
        void onEditClicked(ProgressEntry entry);
        void onDeleteClicked(ProgressEntry entry);
    }

    public ProgressAdapter(Context context, List<ProgressEntry> progressList, OnProgressItemInteractionListener listener) {
        this.context = context;
        this.progressList = progressList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ProgressViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_progress_entry, parent, false);
        return new ProgressViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProgressViewHolder holder, int position) {
        ProgressEntry entry = progressList.get(position);

        holder.tvProgressItemType.setText(entry.getType());
        holder.tvProgressItemDate.setText(dateFormat.format(entry.getDate().toDate()));

        String valueText = "";
        if (entry.getType() == null) { // Basic null check for type
            valueText = "N/A";
        } else if (entry.getType().equals(context.getString(R.string.progress_type_weight))) {
            valueText = String.format(Locale.getDefault(), "%.1f %s", entry.getValue(), entry.getUnit());
        } else if (entry.getType().equals(context.getString(R.string.progress_type_workout))) {
            valueText = String.format(Locale.getDefault(), "%s: %.1f %s, %d sets, %d reps",
                    entry.getExerciseName(), entry.getValue(), entry.getUnit(), entry.getSets(), entry.getReps());
        } else if (entry.getType().equals(context.getString(R.string.progress_type_measurement))) {
            // Assuming getExerciseName() stores the measurement area for this type
            valueText = String.format(Locale.getDefault(), "%s: %.1f %s",
                    entry.getExerciseName(), entry.getValue(), entry.getUnit());
        } else {
            valueText = "Details not available"; // Fallback for unknown types
        }
        holder.tvProgressItemValue.setText(valueText);

        if (entry.getNotes() != null && !entry.getNotes().isEmpty()) {
            holder.tvProgressItemNotes.setText(entry.getNotes()); // Removed "Notes: " prefix as it's clear from context
            holder.tvProgressItemNotes.setVisibility(View.VISIBLE);
        } else {
            holder.tvProgressItemNotes.setVisibility(View.GONE);
        }

        holder.ivProgressItemMenu.setOnClickListener(v -> {
            if (listener != null) {
                PopupMenu popup = new PopupMenu(context, holder.ivProgressItemMenu);
                popup.inflate(R.menu.menu_progress_item); // You'll need to create this menu resource
                popup.setOnMenuItemClickListener(item -> {
                    int itemId = item.getItemId();
                    if (itemId == R.id.action_edit_progress) {
                        listener.onEditClicked(entry);
                        return true;
                    } else if (itemId == R.id.action_delete_progress) {
                        listener.onDeleteClicked(entry);
                        return true;
                    }
                    return false;
                });
                popup.show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return progressList.size();
    }

    public void updateProgress(List<ProgressEntry> newProgressList) {
        this.progressList.clear();
        this.progressList.addAll(newProgressList);
        notifyDataSetChanged(); // Consider using DiffUtil for better performance
    }

    static class ProgressViewHolder extends RecyclerView.ViewHolder {
        TextView tvProgressItemDate, tvProgressItemType, tvProgressItemValue, tvProgressItemNotes;
        ImageView ivProgressItemMenu; // Changed from ImageButtons

        public ProgressViewHolder(@NonNull View itemView) {
            super(itemView);
            tvProgressItemDate = itemView.findViewById(R.id.tvProgressItemDate);
            tvProgressItemType = itemView.findViewById(R.id.tvProgressItemType);
            tvProgressItemValue = itemView.findViewById(R.id.tvProgressItemValue);
            tvProgressItemNotes = itemView.findViewById(R.id.tvProgressItemNotes);
            ivProgressItemMenu = itemView.findViewById(R.id.ivProgressItemMenu);
        }
    }
}
