package com.example.aimingfitness;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

/**
 * Adapter for displaying daily progress entries in a RecyclerView.
 * Adapts the DailyProgressEntry model to the item_daily_progress layout.
 */
public class DailyProgressAdapter extends RecyclerView.Adapter<DailyProgressAdapter.DailyProgressViewHolder> {

    private final Context context;
    private final List<DailyProgressEntry> progressEntries;
    private final SimpleDateFormat dateFormat;
    private final OnDailyProgressItemInteractionListener listener;
    private final NumberFormat numberFormat;

    /**
     * Interface for handling item interactions
     */
    public interface OnDailyProgressItemInteractionListener {
        void onEditClicked(DailyProgressEntry entry);
        void onDeleteClicked(DailyProgressEntry entry);
    }

    public DailyProgressAdapter(Context context, List<DailyProgressEntry> progressEntries, OnDailyProgressItemInteractionListener listener) {
        this.context = context;
        this.progressEntries = progressEntries;
        this.dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        this.listener = listener;
        this.numberFormat = NumberFormat.getNumberInstance(Locale.getDefault());
    }

    @NonNull
    @Override
    public DailyProgressViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_daily_progress, parent, false);
        return new DailyProgressViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DailyProgressViewHolder holder, int position) {
        DailyProgressEntry entry = progressEntries.get(position);

        // Set date
        if (entry.getDate() != null) {
            holder.tvDailyProgressDate.setText(dateFormat.format(entry.getDate()));
        } else {
            holder.tvDailyProgressDate.setText(R.string.no_date);
        }

        // Set nutrition data
        if (entry.getCalories() != null) {
            holder.tvCalories.setText(String.format(Locale.getDefault(), "%s %s", 
                numberFormat.format(entry.getCalories()), context.getString(R.string.cal_unit)));
        } else {
            holder.tvCalories.setText(R.string.no_calories_recorded);
        }

        // Format macros text
        StringBuilder macrosText = new StringBuilder();
        if (entry.getCarbs() != null) {
            macrosText.append("C: ").append(entry.getCarbs()).append("g");
        } else {
            macrosText.append("C: -");
        }

        if (entry.getProtein() != null) {
            macrosText.append(" | P: ").append(entry.getProtein()).append("g");
        } else {
            macrosText.append(" | P: -");
        }

        if (entry.getFat() != null) {
            macrosText.append(" | F: ").append(entry.getFat()).append("g");
        } else {
            macrosText.append(" | F: -");
        }
        holder.tvMacros.setText(macrosText.toString());

        // Set steps data
        if (entry.getSteps() != null) {
            holder.tvStepsCount.setText(String.format(Locale.getDefault(), "%s %s", 
                numberFormat.format(entry.getSteps()), context.getString(R.string.steps)));
            
            if (entry.getStepsGoal() != null && entry.getStepsGoal() > 0) {
                holder.tvStepsGoal.setText(String.format(Locale.getDefault(), "%s: %s", 
                    context.getString(R.string.goal_label), numberFormat.format(entry.getStepsGoal())));
                
                // Calculate and set progress
                int progress = (int) (((float) entry.getSteps() / entry.getStepsGoal()) * 100);
                if (progress > 100) progress = 100; // Cap at 100%
                holder.pbSteps.setProgress(progress);
            } else {
                holder.tvStepsGoal.setText(R.string.no_goal_set);
                holder.pbSteps.setProgress(0);
            }
        } else {
            holder.tvStepsCount.setText(R.string.no_steps_recorded);
            holder.tvStepsGoal.setText("");
            holder.pbSteps.setProgress(0);
        }

        // Handle exercise section visibility
        boolean hasExerciseData = (entry.getExerciseName() != null && !entry.getExerciseName().isEmpty()) || 
                                  (entry.getExerciseDuration() != null && !entry.getExerciseDuration().isEmpty()) || 
                                  (entry.getExerciseCaloriesBurned() != null);
        
        if (hasExerciseData) {
            holder.layoutExercise.setVisibility(View.VISIBLE);
            
            // Set exercise data
            holder.tvExerciseName.setText(entry.getExerciseName() != null ? entry.getExerciseName() : "");
            holder.tvExerciseDuration.setText(entry.getExerciseDuration() != null ? entry.getExerciseDuration() : "");
            
            if (entry.getExerciseCaloriesBurned() != null) {
                holder.tvExerciseCalories.setText(String.format(Locale.getDefault(), "%s %s", 
                    numberFormat.format(entry.getExerciseCaloriesBurned()), context.getString(R.string.cal_unit)));
            } else {
                holder.tvExerciseCalories.setText("");
            }
        } else {
            holder.layoutExercise.setVisibility(View.GONE);
        }

        // Handle notes section visibility
        if (entry.getNotes() != null && !entry.getNotes().trim().isEmpty()) {
            holder.layoutNotes.setVisibility(View.VISIBLE);
            holder.tvNotes.setText(entry.getNotes());
        } else {
            holder.layoutNotes.setVisibility(View.GONE);
        }

        // Set up popup menu for edit/delete options
        holder.ivDailyProgressMenu.setOnClickListener(v -> {
            if (listener != null) {
                PopupMenu popup = new PopupMenu(context, holder.ivDailyProgressMenu);
                popup.inflate(R.menu.menu_progress_item); // Reuse the same menu resource
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
        return progressEntries.size();
    }

    /**
     * Update the adapter data with new entries
     * @param newEntries The new list of daily progress entries
     */
    public void updateEntries(List<DailyProgressEntry> newEntries) {
        this.progressEntries.clear();
        this.progressEntries.addAll(newEntries);
        notifyDataSetChanged(); // Could be optimized with DiffUtil
    }

    /**
     * ViewHolder class for daily progress items
     */
    static class DailyProgressViewHolder extends RecyclerView.ViewHolder {
        TextView tvDailyProgressDate;
        TextView tvCalories;
        TextView tvMacros;
        TextView tvStepsCount;
        TextView tvStepsGoal;
        ProgressBar pbSteps;
        TextView tvExerciseName;
        TextView tvExerciseDuration;
        TextView tvExerciseCalories;
        TextView tvNotes;
        ImageView ivDailyProgressMenu;
        LinearLayout layoutExercise;
        LinearLayout layoutNotes;

        public DailyProgressViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDailyProgressDate = itemView.findViewById(R.id.tvDailyProgressDate);
            tvCalories = itemView.findViewById(R.id.tvCalories);
            tvMacros = itemView.findViewById(R.id.tvMacros);
            tvStepsCount = itemView.findViewById(R.id.tvStepsCount);
            tvStepsGoal = itemView.findViewById(R.id.tvStepsGoal);
            pbSteps = itemView.findViewById(R.id.pbSteps);
            tvExerciseName = itemView.findViewById(R.id.tvExerciseName);
            tvExerciseDuration = itemView.findViewById(R.id.tvExerciseDuration);
            tvExerciseCalories = itemView.findViewById(R.id.tvExerciseCalories);
            tvNotes = itemView.findViewById(R.id.tvNotes);
            ivDailyProgressMenu = itemView.findViewById(R.id.ivDailyProgressMenu);
            layoutExercise = itemView.findViewById(R.id.layoutExercise);
            layoutNotes = itemView.findViewById(R.id.layoutNotes);
        }
    }
}
