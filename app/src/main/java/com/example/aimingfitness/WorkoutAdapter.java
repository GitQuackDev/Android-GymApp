package com.example.aimingfitness;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
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
    }

    @Override
    public void onBindViewHolder(@NonNull WorkoutViewHolder holder, int position) {
        Workout workout = workoutList.get(position);
        if (workout == null) {
            android.util.Log.e("WorkoutAdapter", "Workout at position " + position + " is null.");
            // Optionally, set placeholder text or hide views
            holder.tvWorkoutName.setText("Invalid Workout");
            holder.ivWorkoutImage.setImageResource(R.drawable.default_workout_image); // A default placeholder
            holder.chipGroupTags.removeAllViews(); // Clear any previous chips
            return;
        }
        android.util.Log.d("WorkoutAdapter", "Binding workout: " + workout.getName() + " at position " + position);

        holder.tvWorkoutName.setText(workout.getName());

        // TODO: Load image using Glide or Picasso if workout.getImageUrl() is available
        // For now, using a placeholder or a default image if getImageUrl is not yet implemented
        // if (workout.getImageUrl() != null && !workout.getImageUrl().isEmpty()) {
        //     Glide.with(context).load(workout.getImageUrl()).placeholder(R.drawable.default_workout_image).into(holder.ivWorkoutImage);
        // } else {
        holder.ivWorkoutImage.setImageResource(R.drawable.default_workout_image); // Use a default image
        // }

        // Clear any existing chips before adding new ones
        holder.chipGroupTags.removeAllViews();

        // Add tags as Chips programmatically
        List<String> tags = new ArrayList<>();
        if (workout.getDifficulty() != null && !workout.getDifficulty().isEmpty()) {
            tags.add(workout.getDifficulty());
        }
        if (workout.getDuration() != null && !workout.getDuration().isEmpty()) {
            tags.add(workout.getDuration());
        }
        if (workout.getMuscleGroups() != null && !workout.getMuscleGroups().isEmpty()) {
            // Assuming muscleGroups is a comma-separated string or similar
            // For simplicity, let's assume it's a single string tag for now, or split if needed
            tags.add(workout.getMuscleGroups());
        }

        for (String tagText : tags) {
            if (tagText != null && !tagText.trim().isEmpty()) {
                Chip chip = new Chip(context);
                chip.setText(tagText);
                // chip.setChipBackgroundColorResource(R.color.chip_background_color); // Optional: style your chips
                // chip.setTextColor(ContextCompat.getColor(context, R.color.chip_text_color));
                holder.chipGroupTags.addView(chip);
            }
        }
        if (tags.isEmpty()){
             android.util.Log.d("WorkoutAdapter", "No tags for workout: " + workout.getName());
        }

        holder.itemView.setOnClickListener(v -> {
            if (itemClickListener != null) {
                itemClickListener.onWorkoutItemClick(workout);
            }
        });
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
    }

    static class WorkoutViewHolder extends RecyclerView.ViewHolder {
        ImageView ivWorkoutImage;
        TextView tvWorkoutName;
        // TextView tvWorkoutDescription; // Removed as per new layout
        // Chip chipDuration; // Replaced by ChipGroup
        // Chip chipDifficulty; // Replaced by ChipGroup
        // Chip chipMuscles; // Replaced by ChipGroup
        ChipGroup chipGroupTags;

        public WorkoutViewHolder(@NonNull View itemView) {
            super(itemView);
            ivWorkoutImage = itemView.findViewById(R.id.workout_image_view);
            tvWorkoutName = itemView.findViewById(R.id.workout_name_text_view);
            chipGroupTags = itemView.findViewById(R.id.workout_tags_chip_group);

            // Ensure all IDs match list_item_workout.xml
            // tvWorkoutDescription = itemView.findViewById(R.id.tvWorkoutDescription);
            // chipDuration = itemView.findViewById(R.id.chipDuration);
            // chipDifficulty = itemView.findViewById(R.id.chipDifficulty);
            // chipMuscles = itemView.findViewById(R.id.chipTargetMuscles);
        }
    }
}
