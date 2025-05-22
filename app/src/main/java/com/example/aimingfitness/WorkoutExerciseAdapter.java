package com.example.aimingfitness;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class WorkoutExerciseAdapter extends RecyclerView.Adapter<WorkoutExerciseAdapter.ExerciseViewHolder> {    private Context context;
    private List<ExerciseDetail> exercises;
    private int currentExerciseIndex = 0;
    private OnExerciseClickListener listener;
    private boolean summaryMode = false;

    public interface OnExerciseClickListener {
        void onExerciseClick(ExerciseDetail exercise, int position);
    }

    public WorkoutExerciseAdapter(Context context, List<ExerciseDetail> exercises, OnExerciseClickListener listener) {
        this.context = context;
        this.exercises = exercises;
        this.listener = listener;
    }
    
    public WorkoutExerciseAdapter(Context context, List<ExerciseDetail> exercises, boolean summaryMode) {
        this.context = context;
        this.exercises = exercises;
        this.summaryMode = summaryMode;
    }

    @NonNull
    @Override
    public ExerciseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_workout_exercise, parent, false);
        return new ExerciseViewHolder(view);
    }    @Override
    public void onBindViewHolder(@NonNull ExerciseViewHolder holder, int position) {
        ExerciseDetail exercise = exercises.get(position);
        
        holder.tvExerciseName.setText(exercise.getName());
        
        // Format the exercise details text
        StringBuilder details = new StringBuilder();
        details.append(exercise.getSets()).append(" sets");
        
        if (exercise.getReps() != null && !exercise.getReps().isEmpty()) {
            details.append(" • ").append(exercise.getReps()).append(" reps");
        }
        
        if (exercise.getWeight() != null && !exercise.getWeight().isEmpty()) {
            details.append(" • ").append(exercise.getWeight());
        }
        
        holder.tvExerciseDetails.setText(details.toString());
        
        // Handle different display modes
        if (summaryMode) {
            // In summary mode, all exercises are shown as completed
            holder.ivExerciseStatus.setImageResource(R.drawable.ic_check_circle);
            holder.ivExerciseStatus.setVisibility(View.VISIBLE);
            holder.itemView.setBackgroundResource(android.R.color.transparent);
        } else {
            // In workout progress mode
            if (position < currentExerciseIndex) {
                // Completed exercise
                holder.ivExerciseStatus.setImageResource(R.drawable.ic_check_circle);
                holder.ivExerciseStatus.setVisibility(View.VISIBLE);
                holder.itemView.setBackgroundResource(android.R.color.transparent);
            } else if (position == currentExerciseIndex) {
                // Current exercise
                holder.itemView.setBackgroundResource(R.color.selected_exercise_bg);
                holder.ivExerciseStatus.setVisibility(View.INVISIBLE);
            } else {
                // Upcoming exercise
                holder.itemView.setBackgroundResource(android.R.color.transparent);
                holder.ivExerciseStatus.setVisibility(View.INVISIBLE);
            }
            
            // Set click listener only in workout mode
            holder.itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onExerciseClick(exercise, position);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return exercises != null ? exercises.size() : 0;
    }

    public void setCurrentExerciseIndex(int currentExerciseIndex) {
        this.currentExerciseIndex = currentExerciseIndex;
    }

    /**
     * Mark the exercise at the given index as completed by updating the currentExerciseIndex
     * and refreshing the adapter.
     */
    public void markExerciseCompleted(int index) {
        setCurrentExerciseIndex(index + 1); // Move to next exercise
        notifyDataSetChanged();
    }

    static class ExerciseViewHolder extends RecyclerView.ViewHolder {
        TextView tvExerciseName;
        TextView tvExerciseDetails;
        ImageView ivExerciseStatus;

        public ExerciseViewHolder(@NonNull View itemView) {
            super(itemView);
            tvExerciseName = itemView.findViewById(R.id.tvExerciseName);
            tvExerciseDetails = itemView.findViewById(R.id.tvExerciseDetails);
            ivExerciseStatus = itemView.findViewById(R.id.ivExerciseStatus);
        }
    }
}
