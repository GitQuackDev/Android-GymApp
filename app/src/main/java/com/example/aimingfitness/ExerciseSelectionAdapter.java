package com.example.aimingfitness;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class ExerciseSelectionAdapter extends RecyclerView.Adapter<ExerciseSelectionAdapter.ExerciseViewHolder> {

    private List<ExerciseLibrary> exercisesList;
    private List<ExerciseLibrary> filteredExercisesList;
    private Context context;
    private OnExerciseSelectedListener listener;

    public interface OnExerciseSelectedListener {
        // When the whole exercise item is clicked (for viewing details)
        void onExerciseSelected(ExerciseLibrary exercise);
        
        // When just the add button is clicked (for quick adding)
        default void onAddButtonClicked(ExerciseLibrary exercise) {
            // Default implementation just calls onExerciseSelected
            onExerciseSelected(exercise);
        }
    }

    public ExerciseSelectionAdapter(Context context, List<ExerciseLibrary> exercisesList, OnExerciseSelectedListener listener) {
        this.context = context;
        this.exercisesList = exercisesList;
        this.filteredExercisesList = new ArrayList<>(exercisesList);
        this.listener = listener;
    }

    @NonNull
    @Override
    public ExerciseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_exercise_selection, parent, false);
        return new ExerciseViewHolder(view);
    }

    /**
     * Updates the adapter with a new list of exercises (for filtering)
     */
    public void updateExercises(List<ExerciseLibrary> newExercises) {
        this.filteredExercisesList.clear();
        this.filteredExercisesList.addAll(newExercises);
        notifyDataSetChanged();
    }

    @Override
    public void onBindViewHolder(@NonNull ExerciseViewHolder holder, int position) {
        ExerciseLibrary exercise = filteredExercisesList.get(position);
        
        holder.tvExerciseName.setText(exercise.getName());
        holder.tvExerciseMuscles.setText(exercise.getFormattedMuscles());
        holder.tvExerciseEquipment.setText(exercise.getFormattedEquipment());
        
        // Set a default image for now
        // In a real app, you'd use Glide or Picasso to load images from URLs or resources
        holder.ivExerciseImage.setImageResource(android.R.drawable.ic_menu_gallery);
        
        // Set click listener on the whole item
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onExerciseSelected(exercise);
            }
        });
        
        // Add button click listener
        if (holder.btnAddExercise != null) {
            holder.btnAddExercise.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onAddButtonClicked(exercise);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return filteredExercisesList.size();
    }

    public void filter(String query, String muscleGroup) {
        filteredExercisesList.clear();
        
        if (query.isEmpty() && (muscleGroup.isEmpty() || muscleGroup.equalsIgnoreCase("All"))) {
            filteredExercisesList.addAll(exercisesList);
        } else {
            String lowerCaseQuery = query.toLowerCase();
            for (ExerciseLibrary exercise : exercisesList) {
                boolean matchesQuery = query.isEmpty() || 
                    exercise.getName().toLowerCase().contains(lowerCaseQuery);
                
                boolean matchesMuscleGroup = muscleGroup.isEmpty() || 
                    muscleGroup.equalsIgnoreCase("All") || 
                    exercise.getPrimaryMuscleGroup().equalsIgnoreCase(muscleGroup);
                
                if (matchesQuery && matchesMuscleGroup) {
                    filteredExercisesList.add(exercise);
                }
            }
        }
        
        notifyDataSetChanged();
    }

    static class ExerciseViewHolder extends RecyclerView.ViewHolder {
        ImageView ivExerciseImage;
        TextView tvExerciseName, tvExerciseMuscles, tvExerciseEquipment;
        com.google.android.material.button.MaterialButton btnAddExercise;

        ExerciseViewHolder(@NonNull View itemView) {
            super(itemView);
            ivExerciseImage = itemView.findViewById(R.id.ivExerciseImage);
            tvExerciseName = itemView.findViewById(R.id.tvExerciseName);
            tvExerciseMuscles = itemView.findViewById(R.id.tvExerciseMuscles);
            tvExerciseEquipment = itemView.findViewById(R.id.tvExerciseEquipment);
            btnAddExercise = itemView.findViewById(R.id.btnAddExercise);
        }
    }
}
