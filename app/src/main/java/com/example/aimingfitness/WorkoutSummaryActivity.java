package com.example.aimingfitness;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

import java.util.ArrayList;
import java.util.List;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class WorkoutSummaryActivity extends AppCompatActivity {
    private TextView tvWorkoutName, tvTotalExercises, tvTotalSets, tvDuration;
    private RecyclerView rvCompletedExercises;
    private WorkoutExerciseAdapter adapter;
    
    private String workoutId;
    private String workoutName;
    private int totalExercises;
    private int totalSets;
    private String duration;
    private List<ExerciseDetail> completedExercises;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workout_summary);
        
        // Setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.workout_complete);
        }
        
        // Initialize views
        tvWorkoutName = findViewById(R.id.tvWorkoutName);
        tvTotalExercises = findViewById(R.id.tvTotalExercises);
        tvTotalSets = findViewById(R.id.tvTotalSets);
        tvDuration = findViewById(R.id.tvDuration);
        rvCompletedExercises = findViewById(R.id.rvCompletedExercises);
        
        MaterialButton btnSaveWorkoutProgress = findViewById(R.id.btnSaveWorkoutProgress);
        MaterialButton btnShareWorkout = findViewById(R.id.btnShareWorkout);
        MaterialButton btnFinishWorkout = findViewById(R.id.btnFinishWorkout);
        
        // Get data from intent
        if (getIntent() != null) {
            workoutId = getIntent().getStringExtra("WORKOUT_ID");
            workoutName = getIntent().getStringExtra("WORKOUT_NAME");
            totalExercises = getIntent().getIntExtra("TOTAL_EXERCISES", 0);
            totalSets = getIntent().getIntExtra("TOTAL_SETS", 0);
            duration = getIntent().getStringExtra("DURATION");
            completedExercises = (List<ExerciseDetail>) getIntent().getSerializableExtra("COMPLETED_EXERCISES");
            
            if (completedExercises == null) {
                completedExercises = new ArrayList<>();
            }
            
            // Verify data integrity and use defaults if needed
            if (totalExercises == 0 && completedExercises != null) {
                totalExercises = completedExercises.size();
            }
            
            if (totalSets == 0 && completedExercises != null) {
                // Calculate total sets if not provided
                for (ExerciseDetail exercise : completedExercises) {
                    totalSets += exercise.getSets();
                }
            }
            
            if (duration == null || duration.isEmpty()) {
                duration = "Unknown";
            }
        }
        
        // Update UI with workout data
        tvWorkoutName.setText(workoutName);
        tvTotalExercises.setText(String.valueOf(totalExercises));
        tvTotalSets.setText(String.valueOf(totalSets));
        tvDuration.setText(duration);
        
        // Setup recycler view
        setupRecyclerView();
        
        // Button click listeners
        btnSaveWorkoutProgress.setOnClickListener(v -> saveWorkoutProgress());
        btnShareWorkout.setOnClickListener(v -> shareWorkoutResults());
        btnFinishWorkout.setOnClickListener(v -> finishWorkout());
    }
    
    private void setupRecyclerView() {
        rvCompletedExercises.setLayoutManager(new LinearLayoutManager(this));
        adapter = new WorkoutExerciseAdapter(this, completedExercises, true);
        rvCompletedExercises.setAdapter(adapter);
    }
    
    private void saveWorkoutProgress() {
        // Get current user
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "You must be logged in to save workout progress", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create workout history object
        Map<String, Object> workoutHistory = new HashMap<>();
        workoutHistory.put("userId", currentUser.getUid());
        workoutHistory.put("workoutId", workoutId);
        workoutHistory.put("workoutName", workoutName);
        workoutHistory.put("totalExercises", totalExercises);
        workoutHistory.put("totalSets", totalSets);
        workoutHistory.put("duration", duration);
        workoutHistory.put("date", new Date());
        
        // Add the exercises list to the workout history document
        List<Map<String, Object>> exercisesList = new ArrayList<>();
        for (ExerciseDetail exercise : completedExercises) {
            Map<String, Object> exerciseMap = new HashMap<>();
            exerciseMap.put("id", exercise.getId());
            exerciseMap.put("name", exercise.getName());
            exerciseMap.put("sets", exercise.getSets());
            exerciseMap.put("reps", exercise.getReps());
            exerciseMap.put("weight", exercise.getWeight());
            exerciseMap.put("notes", exercise.getNotes());
            exerciseMap.put("restTimeSeconds", exercise.getRestTimeSeconds());
            exerciseMap.put("warmUp", exercise.isWarmUp());
            
            exercisesList.add(exerciseMap);
        }
        workoutHistory.put("exercises", exercisesList);
        
        // Get Firestore instance
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        
        // Save to Firestore
        db.collection("workoutHistory")
            .add(workoutHistory)
            .addOnSuccessListener(documentReference -> {
                Toast.makeText(this, "Workout progress saved successfully!", Toast.LENGTH_SHORT).show();
                
                // Save each exercise as a ProgressEntry to integrate with the progress tracking system
                saveWorkoutToProgressSystem(currentUser.getUid());
                
                // Disable button after saving
                findViewById(R.id.btnSaveWorkoutProgress).setEnabled(false);
                ((MaterialButton)findViewById(R.id.btnSaveWorkoutProgress)).setText("Progress Saved");
            })
            .addOnFailureListener(e -> {
                Toast.makeText(this, "Error saving workout progress: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
    }
    
    /**
     * Saves each exercise from the workout to the progress system for tracking
     */
    private void saveWorkoutToProgressSystem(String userId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Date currentDate = new Date();
        
        for (ExerciseDetail exercise : completedExercises) {
            // Skip if exercise doesn't have weight data
            if (exercise.getWeight() == null || exercise.getWeight().isEmpty() || 
                exercise.getWeight().equalsIgnoreCase("bodyweight")) {
                continue;
            }
            
            // Try to parse weight to a numeric value
            double weightValue;
            try {
                String weightStr = exercise.getWeight().replaceAll("[^\\d.]", "");
                weightValue = Double.parseDouble(weightStr);
            } catch (NumberFormatException e) {
                // Skip if weight is not parseable
                continue;
            }
            
            // Create a ProgressEntry for this exercise
            ProgressEntry entry = new ProgressEntry();
            entry.setUserId(userId);
            entry.setDate(new Timestamp(currentDate));
            entry.setType("Workout Log");
            entry.setValue(weightValue);
            entry.setUnit(exercise.getWeight().contains("kg") ? "kg" : "lbs");
            entry.setExerciseName(exercise.getName());
            entry.setSets(exercise.getSets());
            
            // Try to parse reps
            try {
                entry.setReps(Integer.parseInt(exercise.getReps()));
            } catch (NumberFormatException e) {
                entry.setReps(0); // Default if reps is not a simple number
            }
            
            entry.setNotes("From workout: " + workoutName);
            
            // Save to Firestore progress collection
            db.collection("progress")
                .add(entry)
                .addOnFailureListener(e -> {
                    // Silent failure - we've already shown success for the overall workout
                });
        }
    }
    
    private void shareWorkoutResults() {
        // Create a formatted string with workout details
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, MMM d, yyyy", Locale.getDefault());
        String currentDate = dateFormat.format(new Date());
        
        StringBuilder workoutSummary = new StringBuilder();
        workoutSummary.append("I just crushed ").append(workoutName).append("! 💪\n\n");
        workoutSummary.append("Date: ").append(currentDate).append("\n");
        workoutSummary.append("Duration: ").append(duration).append("\n");
        workoutSummary.append("Exercises: ").append(totalExercises).append("\n");
        workoutSummary.append("Total Sets: ").append(totalSets).append("\n\n");
        
        workoutSummary.append("Workout details:\n");
        for (int i = 0; i < completedExercises.size(); i++) {
            ExerciseDetail exercise = completedExercises.get(i);
            workoutSummary.append(i + 1).append(". ")
                    .append(exercise.getName()).append(": ")
                    .append(exercise.getSets()).append(" x ")
                    .append(exercise.getReps());
            
            if (exercise.getWeight() != null && !exercise.getWeight().isEmpty()) {
                workoutSummary.append(" @ ").append(exercise.getWeight());
            }
            
            workoutSummary.append("\n");
        }
        
        workoutSummary.append("\nTracked with AimingFitness app 🏋️‍♀️");
        
        // Create share intent
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "My " + workoutName + " Workout");
        shareIntent.putExtra(Intent.EXTRA_TEXT, workoutSummary.toString());
        startActivity(Intent.createChooser(shareIntent, "Share workout via"));
    }
    
    private void finishWorkout() {
        // Return to home page/workouts fragment
        Intent intent = new Intent(this, HomePageActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }
    
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    @Override
    public void onBackPressed() {
        // Go back to home page instead of the workout session
        finishWorkout();
    }
}
