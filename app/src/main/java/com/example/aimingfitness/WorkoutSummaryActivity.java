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
        
        // Get Firestore instance
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        
        // Save to Firestore
        db.collection("workoutHistory")
            .add(workoutHistory)
            .addOnSuccessListener(documentReference -> {
                Toast.makeText(this, "Workout progress saved successfully!", Toast.LENGTH_SHORT).show();
                
                // Disable button after saving
                findViewById(R.id.btnSaveWorkoutProgress).setEnabled(false);
                ((MaterialButton)findViewById(R.id.btnSaveWorkoutProgress)).setText("Progress Saved");
            })
            .addOnFailureListener(e -> {
                Toast.makeText(this, "Error saving workout progress: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
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
