package com.example.aimingfitness;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide; // Import Glide
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import java.util.ArrayList;
import java.util.List;

public class WorkoutAdapter extends RecyclerView.Adapter<WorkoutAdapter.WorkoutViewHolder> {

    private List<Workout> workoutList;
    private Context context;
    private WorkoutActionListener actionListener;
    private WorkoutItemClickListener itemClickListener; // Added for item clicks

    // Interface for handling actions like edit, delete, start
    public interface WorkoutActionListener {
        void onWorkoutEdit(Workout workout, int position);
        void onWorkoutDelete(Workout workout, int position);
        void onWorkoutStart(Workout workout, int position); // Kept for potential specific start button
    }

    // Interface for handling item clicks
    public interface WorkoutItemClickListener {
        void onWorkoutItemClick(Workout workout);
    }

    public WorkoutAdapter(Context context, List<Workout> workoutList, WorkoutActionListener actionListener, WorkoutItemClickListener itemClickListener) {
        this.context = context;
        // Create a separate copy to avoid modifying the source list
        this.workoutList = workoutList != null ? new ArrayList<>(workoutList) : new ArrayList<>();
        this.actionListener = actionListener;
        this.itemClickListener = itemClickListener;
    }

    @NonNull
    @Override
    public WorkoutViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_item_workout, parent, false);
        return new WorkoutViewHolder(view);
    }    @Override
    public void onBindViewHolder(@NonNull WorkoutViewHolder holder, int position) {
        Workout workout = workoutList.get(position);
        if (workout == null) {
            android.util.Log.e("WorkoutAdapter", "Workout at position " + position + " is null.");
            // Optionally, set placeholder text or hide views
            holder.tvWorkoutName.setText("Invalid Workout");
            holder.ivWorkoutImage.setImageResource(R.drawable.default_workout_image); // A default placeholder
            holder.chipGroupTags.removeAllViews(); // Clear any previous chips
            holder.tvDifficultyBadge.setVisibility(View.GONE);
            holder.tvDurationBadge.setVisibility(View.GONE);
            holder.tvExercisesCount.setVisibility(View.GONE);
            holder.tvLastPerformed.setVisibility(View.GONE);
            return;
        }
        android.util.Log.d("WorkoutAdapter", "Binding workout: " + workout.getName() + " at position " + position);

        // Set workout name
        holder.tvWorkoutName.setText(workout.getName());

        // Set image using Glide
        if (workout.getImageUrl() != null && !workout.getImageUrl().isEmpty()) {
            Glide.with(context)
                 .load(Uri.parse(workout.getImageUrl()))
                 .placeholder(R.drawable.ic_placeholder_workout)
                 .error(R.drawable.ic_placeholder_workout)
                 .centerCrop()
                 .into(holder.ivWorkoutImage);
        } else {
            holder.ivWorkoutImage.setImageResource(R.drawable.ic_placeholder_workout);
        }

        // Set difficulty badge
        if (workout.getDifficulty() != null && !workout.getDifficulty().isEmpty()) {
            holder.tvDifficultyBadge.setText(workout.getDifficulty().toUpperCase());
            holder.tvDifficultyBadge.setVisibility(View.VISIBLE);
        } else {
            holder.tvDifficultyBadge.setVisibility(View.GONE);
        }

        // Set duration badge
        if (workout.getDuration() != null && !workout.getDuration().isEmpty()) {
            holder.tvDurationBadge.setText(workout.getDuration().toUpperCase() + " MINS");
            holder.tvDurationBadge.setVisibility(View.VISIBLE);
        } else {
            holder.tvDurationBadge.setVisibility(View.GONE);
        }

        // Clear any existing chips before adding new ones
        holder.chipGroupTags.removeAllViews();        // Clear any existing chips before adding new ones
        holder.chipGroupTags.removeAllViews();

        // Add muscle group chips
        List<String> muscleGroups = new ArrayList<>();
        if (workout.getMuscleGroups() != null && !workout.getMuscleGroups().isEmpty()) {
            String[] groups = workout.getMuscleGroups().split(",\\s*");
            for (String group : groups) {
                if (group != null && !group.trim().isEmpty()) {
                    muscleGroups.add(group.trim());
                }
            }
        }

        // Add muscle group chips
        for (String muscleGroup : muscleGroups) {
            Chip chip = new Chip(context, null, R.style.Widget_AimingFitness_Chip_Tag);
            chip.setText(muscleGroup);
            chip.setCheckable(false);
            chip.setClickable(false);
            holder.chipGroupTags.addView(chip);
        }

        // Set visibility of chip group based on whether there are muscle groups
        holder.chipGroupTags.setVisibility(muscleGroups.isEmpty() ? View.GONE : View.VISIBLE);

        // Set exercise count
        if (workout.getExercises() != null) {
            int exerciseCount = workout.getExercises().size();
            holder.tvExercisesCount.setText(exerciseCount + (exerciseCount == 1 ? " exercise" : " exercises"));
            holder.tvExercisesCount.setVisibility(View.VISIBLE);
            
            // In a real app, this would come from workout history data
            holder.tvLastPerformed.setText("Last: 3 days ago");
            holder.tvLastPerformed.setVisibility(View.VISIBLE);
        } else {
            holder.tvExercisesCount.setText("No exercises");
            holder.tvLastPerformed.setVisibility(View.GONE);
        }        holder.itemView.setOnClickListener(v -> {
            if (itemClickListener != null) {
                itemClickListener.onWorkoutItemClick(workout);
            }
        });
        
        // Set click listener for start button
        holder.btnStartWorkout.setOnClickListener(v -> {
            if (actionListener != null) {
                actionListener.onWorkoutStart(workout, position);
            }
        });

        holder.btnMoreOptions.setOnClickListener(v -> {
            // Implement popup menu logic here
            showPopupMenu(holder.btnMoreOptions, workout, position);
        });
    }

    private void showPopupMenu(View view, Workout workout, int position) {
        PopupMenu popup = new PopupMenu(context, view);
        popup.getMenuInflater().inflate(R.menu.workout_item_menu, popup.getMenu());
        popup.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.action_edit_workout) {
                if (actionListener != null) {
                    actionListener.onWorkoutEdit(workout, position);
                }
                return true;
            } else if (itemId == R.id.action_delete_workout) {
                if (actionListener != null) {
                    actionListener.onWorkoutDelete(workout, position);
                }
                return true;
            } else {
                return false;
            }
        });
        popup.show();
    }

    @Override
    public int getItemCount() {
        android.util.Log.d("WorkoutAdapter", "getItemCount: " + workoutList.size());
        return workoutList.size();
    }

    public void updateWorkouts(List<Workout> newWorkouts) {
        android.util.Log.d("WorkoutAdapter", "updateWorkouts called with newWorkouts size: " + (newWorkouts == null ? "null" : newWorkouts.size()));
        this.workoutList.clear();
        android.util.Log.d("WorkoutAdapter", "Internal list after clear: " + workoutList.size());
        if (newWorkouts != null) {
            this.workoutList.addAll(newWorkouts);
            android.util.Log.d("WorkoutAdapter", "Internal list after addAll: " + workoutList.size());
        }
        notifyDataSetChanged(); // Consider DiffUtil for better performance
    }    static class WorkoutViewHolder extends RecyclerView.ViewHolder {
        ImageView ivWorkoutImage;
        TextView tvWorkoutName;
        TextView tvExercisesCount;
        TextView tvLastPerformed;
        TextView tvDifficultyBadge;
        TextView tvDurationBadge;
        ChipGroup chipGroupTags;
        ImageButton btnStartWorkout;
        ImageButton btnMoreOptions; // Added

        public WorkoutViewHolder(@NonNull View itemView) {
            super(itemView);
            ivWorkoutImage = itemView.findViewById(R.id.workout_image_view);
            tvWorkoutName = itemView.findViewById(R.id.workout_name_text_view);
            chipGroupTags = itemView.findViewById(R.id.workout_tags_chip_group);
            tvExercisesCount = itemView.findViewById(R.id.workout_exercises_count);
            tvLastPerformed = itemView.findViewById(R.id.workout_last_performed);
            tvDifficultyBadge = itemView.findViewById(R.id.workout_difficulty_badge);
            tvDurationBadge = itemView.findViewById(R.id.workout_duration_badge);
            btnStartWorkout = itemView.findViewById(R.id.btn_start_workout);
            btnMoreOptions = itemView.findViewById(R.id.btn_more_options); // Added
        }
    }
}
