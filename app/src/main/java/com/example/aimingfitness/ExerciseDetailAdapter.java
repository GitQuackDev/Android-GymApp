package com.example.aimingfitness;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;

import java.util.List;

public class ExerciseDetailAdapter extends RecyclerView.Adapter<ExerciseDetailAdapter.ExerciseDetailViewHolder> {

    private List<ExerciseDetail> exerciseDetails;
    private OnExerciseRemoveListener removeListener;
    private OnExerciseEditListener editListener;
    
    public interface OnExerciseRemoveListener {
        void onExerciseRemoved(int position);
    }
    
    public interface OnExerciseEditListener {
        void onExerciseEdit(int position);
    }
    
    public ExerciseDetailAdapter(android.content.Context context, List<ExerciseDetail> exerciseDetails, 
                               OnExerciseRemoveListener removeListener) {
        this.exerciseDetails = exerciseDetails;
        this.removeListener = removeListener;
    }
    
    public ExerciseDetailAdapter(android.content.Context context, List<ExerciseDetail> exerciseDetails, 
                               OnExerciseRemoveListener removeListener,
                               OnExerciseEditListener editListener) {
        this.exerciseDetails = exerciseDetails;
        this.removeListener = removeListener;
        this.editListener = editListener;
    }

    @NonNull
    @Override
    public ExerciseDetailViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_exercise_detail, parent, false);
        return new ExerciseDetailViewHolder(view);
    }    @Override
    public void onBindViewHolder(@NonNull ExerciseDetailViewHolder holder, int position) {
        ExerciseDetail detail = exerciseDetails.get(position);
        holder.tvExerciseName.setText(detail.getName());
        holder.tvExerciseSets.setText(String.valueOf(detail.getSets()));
        holder.tvExerciseReps.setText(String.valueOf(detail.getReps()));
        
        // Handle weight display
        if (detail.getWeight() != null && !detail.getWeight().isEmpty()) {
            holder.tvExerciseWeight.setText(detail.getWeight());
            holder.tvExerciseWeight.setVisibility(View.VISIBLE);
            holder.tvWeightLabel.setVisibility(View.VISIBLE);
        } else {
            holder.tvExerciseWeight.setVisibility(View.GONE);
            holder.tvWeightLabel.setVisibility(View.GONE);
        }
        
        // Handle rest time display
        if (detail.getRestTimeSeconds() > 0) {
            holder.tvExerciseRest.setText(detail.getRestTimeSeconds() + " sec");
        } else {
            holder.tvExerciseRest.setText("N/A");
        }
        
        // Set notes if available
        if (detail.getNotes() != null && !detail.getNotes().isEmpty()) {
            holder.tvExerciseNotes.setVisibility(View.VISIBLE);
            holder.tvExerciseNotes.setText(detail.getNotes());
        } else {
            holder.tvExerciseNotes.setVisibility(View.GONE);
        }
        
        // Set warm-up chip visibility
        if (detail.isWarmUp()) {
            holder.chipWarmUp.setVisibility(View.VISIBLE);
        } else {
            holder.chipWarmUp.setVisibility(View.GONE);
        }

        // Set click listeners for buttons
        holder.ibRemoveExercise.setOnClickListener(v -> {
            if (removeListener != null) {
                removeListener.onExerciseRemoved(holder.getAdapterPosition());
            }
        });
        
        // Set click listener for the entire item for editing
        holder.itemView.setOnClickListener(v -> {
            if (editListener != null) {
                editListener.onExerciseEdit(holder.getAdapterPosition());
            }
        });
    }

    @Override
    public int getItemCount() {
        return exerciseDetails == null ? 0 : exerciseDetails.size();
    }    static class ExerciseDetailViewHolder extends RecyclerView.ViewHolder {
        TextView tvExerciseName, tvExerciseSets, tvExerciseReps, tvExerciseNotes;
        TextView tvExerciseWeight, tvExerciseRest, tvWeightLabel;
        ImageButton ibRemoveExercise;
        Chip chipWarmUp;

        public ExerciseDetailViewHolder(@NonNull View itemView) {
            super(itemView);
            tvExerciseName = itemView.findViewById(R.id.tvExerciseName);
            tvExerciseSets = itemView.findViewById(R.id.tvExerciseSets);
            tvExerciseReps = itemView.findViewById(R.id.tvExerciseReps);
            tvExerciseNotes = itemView.findViewById(R.id.tvExerciseNotes);
            tvExerciseWeight = itemView.findViewById(R.id.tvExerciseWeight);
            tvExerciseRest = itemView.findViewById(R.id.tvExerciseRest);
            tvWeightLabel = itemView.findViewById(R.id.tvWeightLabel);
            chipWarmUp = itemView.findViewById(R.id.chipWarmUp);
            ibRemoveExercise = itemView.findViewById(R.id.ibRemoveExercise);
        }
    }

    public void updateExerciseDetails(List<ExerciseDetail> newDetails) {
        this.exerciseDetails.clear();
        if (newDetails != null) {
            this.exerciseDetails.addAll(newDetails);
        }
        notifyDataSetChanged();
    }
}
