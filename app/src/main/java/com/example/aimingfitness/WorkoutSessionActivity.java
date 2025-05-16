package com.example.aimingfitness;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class WorkoutSessionActivity extends AppCompatActivity implements WorkoutExerciseAdapter.OnExerciseClickListener {

    private static final String TAG = "WorkoutSessionActivity";
    private static final int DEFAULT_REST_TIME = 60; // Default rest time in seconds if none specified

    private Workout workout;
    private List<ExerciseDetail> exerciseDetails = new ArrayList<>();
    
    // UI Components
    private CollapsingToolbarLayout collapsingToolbar;
    private TextView tvTotalExercises;
    private TextView tvEstimatedTime;
    private TextView tvDifficulty;
    private TextView tvCurrentExerciseName;
    private TextView tvCurrentExerciseDescription;
    private TextView tvCurrentSetProgress;
    private TextView tvCurrentReps;
    private TextView tvCurrentWeight;
    private LinearLayout llWeight;
    private TextView tvTimer;
    private TextView tvTimerLabel;
    private MaterialButton btnSkipTimer;
    private MaterialButton btnPauseTimer;
    private MaterialCardView cardTimer;
    private ExtendedFloatingActionButton fabNextExercise;
    private RecyclerView rvExerciseList;
    private WorkoutExerciseAdapter exerciseAdapter;
      // State variables
    private int currentExerciseIndex = 0;
    private int currentSetNumber = 1;
    private boolean isRestPhase = false;
    private CountDownTimer restTimer;
    private boolean isTimerPaused = false;
    private long timeRemaining = 0;
    
    // Workout tracking
    private long workoutStartTime;
    private int totalCompletedSets = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workout_session);

        // Get the workout from intent
        if (getIntent() != null) {
            workout = (Workout) getIntent().getSerializableExtra("workout");
        }
        
        if (workout == null) {
            Toast.makeText(this, "Error loading workout details", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        // Initialize UI components
        initializeUI();
        
        // Set up toolbar and action bar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        
        // Set workout info
        collapsingToolbar.setTitle(workout.getName());
          // Set workout stats
        exerciseDetails = workout.getExercises();
        if (exerciseDetails == null) {
            exerciseDetails = new ArrayList<>();
        }
        
        // Start tracking workout time
        workoutStartTime = System.currentTimeMillis();
        
        int totalExercises = exerciseDetails.size();
        tvTotalExercises.setText(String.valueOf(totalExercises));
        tvEstimatedTime.setText(workout.getDuration());
        tvDifficulty.setText(workout.getDifficulty());
        
        // Set up exercise adapter
        exerciseAdapter = new WorkoutExerciseAdapter(this, exerciseDetails, this);
        rvExerciseList.setAdapter(exerciseAdapter);
        
        // Start with the first exercise if available
        if (!exerciseDetails.isEmpty()) {
            updateCurrentExerciseDisplay();
        } else {
            // Handle case where there are no exercises
            showNoExercisesMessage();
        }

        // Set up FAB click listener
        fabNextExercise.setOnClickListener(v -> {
            if (isRestPhase) {
                // If we're in rest phase, cancel it and move to the next exercise or set
                if (restTimer != null) {
                    restTimer.cancel();
                }
                completeRestPeriod();
            } else {
                // If we're not in rest phase, complete the current set
                completeCurrentSet();
            }
        });

        // Skip timer button
        btnSkipTimer.setOnClickListener(v -> {
            if (restTimer != null) {
                restTimer.cancel();
            }
            completeRestPeriod();
        });

        // Pause/resume timer button
        btnPauseTimer.setOnClickListener(v -> {
            if (isTimerPaused) {
                // Resume timer
                startRestTimer(timeRemaining);
                btnPauseTimer.setText(R.string.pause);
                isTimerPaused = false;
            } else {
                // Pause timer
                if (restTimer != null) {
                    restTimer.cancel();
                }
                btnPauseTimer.setText(R.string.resume);
                isTimerPaused = true;
            }
        });
    }

    private void initializeUI() {
        collapsingToolbar = findViewById(R.id.collapsingToolbar);
        tvTotalExercises = findViewById(R.id.tvTotalExercises);
        tvEstimatedTime = findViewById(R.id.tvEstimatedTime);
        tvDifficulty = findViewById(R.id.tvDifficulty);
        tvCurrentExerciseName = findViewById(R.id.tvCurrentExerciseName);
        tvCurrentExerciseDescription = findViewById(R.id.tvCurrentExerciseDescription);
        tvCurrentSetProgress = findViewById(R.id.tvCurrentSetProgress);
        tvCurrentReps = findViewById(R.id.tvCurrentReps);
        tvCurrentWeight = findViewById(R.id.tvCurrentWeight);
        llWeight = findViewById(R.id.llWeight);
        tvTimer = findViewById(R.id.tvTimer);
        tvTimerLabel = findViewById(R.id.tvTimerLabel);
        btnSkipTimer = findViewById(R.id.btnSkipTimer);
        btnPauseTimer = findViewById(R.id.btnPauseTimer);
        cardTimer = findViewById(R.id.cardTimer);
        fabNextExercise = findViewById(R.id.fabNextExercise);
        rvExerciseList = findViewById(R.id.rvExerciseList);
        
        rvExerciseList.setLayoutManager(new LinearLayoutManager(this));
        
        // Initially hide the timer card until needed
        cardTimer.setVisibility(View.GONE);
    }

    private void updateCurrentExerciseDisplay() {
        if (exerciseDetails.isEmpty() || currentExerciseIndex >= exerciseDetails.size()) {
            return;
        }
        
        ExerciseDetail exercise = exerciseDetails.get(currentExerciseIndex);
        tvCurrentExerciseName.setText(exercise.getName());
        
        // Set description/notes if available, otherwise hide it
        if (exercise.getNotes() != null && !exercise.getNotes().isEmpty()) {
            tvCurrentExerciseDescription.setText(exercise.getNotes());
            tvCurrentExerciseDescription.setVisibility(View.VISIBLE);
        } else {
            tvCurrentExerciseDescription.setVisibility(View.GONE);
        }
        
        // Set current set progress
        String setProgress = String.format(Locale.getDefault(), "%d / %d", currentSetNumber, exercise.getSets());
        tvCurrentSetProgress.setText(setProgress);
        
        // Set reps
        tvCurrentReps.setText(exercise.getReps());
        
        // Set weight if available
        if (exercise.getWeight() != null && !exercise.getWeight().isEmpty()) {
            tvCurrentWeight.setText(exercise.getWeight());
            llWeight.setVisibility(View.VISIBLE);
        } else {
            llWeight.setVisibility(View.GONE);
        }
        
        // Update adapter to highlight current exercise
        exerciseAdapter.setCurrentExerciseIndex(currentExerciseIndex);
        exerciseAdapter.notifyDataSetChanged();
        
        // Update FAB text
        if (isRestPhase) {
            fabNextExercise.setText(R.string.skip);
            fabNextExercise.setIconResource(R.drawable.ic_check);
        } else {
            fabNextExercise.setText(R.string.complete_set);
            fabNextExercise.setIconResource(R.drawable.ic_check);
        }
    }

    private void showNoExercisesMessage() {
        // Hide exercise-specific UI
        tvCurrentExerciseName.setText(R.string.no_exercises_found);
        tvCurrentExerciseDescription.setVisibility(View.GONE);
        tvCurrentSetProgress.setText("0 / 0");
        tvCurrentReps.setText("-");
        llWeight.setVisibility(View.GONE);
        fabNextExercise.setVisibility(View.GONE);
    }    private void completeCurrentSet() {
        ExerciseDetail currentExercise = exerciseDetails.get(currentExerciseIndex);
        
        // Track completed sets
        totalCompletedSets++;
        
        if (currentSetNumber < currentExercise.getSets()) {
            // If there are more sets for this exercise, start rest period
            currentSetNumber++;
            startRestPeriod(currentExercise.getRestTimeSeconds());
        } else {
            // If this was the last set, move to the next exercise
            currentSetNumber = 1;
            currentExerciseIndex++;
            
            // Check if there are more exercises
            if (currentExerciseIndex < exerciseDetails.size()) {
                // Start rest period before next exercise
                ExerciseDetail nextExercise = exerciseDetails.get(currentExerciseIndex);
                startRestPeriod(nextExercise.isWarmUp() ? 30 : 60); // Less rest if it's a warmup
            } else {
                // Workout completed
                showWorkoutCompleteDialog();
                return;
            }
        }
        
        updateCurrentExerciseDisplay();
    }

    private void startRestPeriod(int seconds) {
        isRestPhase = true;
        if (seconds <= 0) {
            seconds = DEFAULT_REST_TIME;
        }
        
        // Show the timer card
        cardTimer.setVisibility(View.VISIBLE);
        startRestTimer(seconds * 1000L); // Convert to milliseconds
        
        // Update FAB
        fabNextExercise.setText(R.string.skip);
        
        // Update adapter
        exerciseAdapter.notifyDataSetChanged();
    }

    private void startRestTimer(long milliseconds) {
        if (restTimer != null) {
            restTimer.cancel();
        }
        
        restTimer = new CountDownTimer(milliseconds, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeRemaining = millisUntilFinished;
                updateTimerDisplay(millisUntilFinished);
            }
            
            @Override
            public void onFinish() {
                completeRestPeriod();
            }
        }.start();
    }

    private void updateTimerDisplay(long millisUntilFinished) {
        int seconds = (int) (millisUntilFinished / 1000);
        int minutes = seconds / 60;
        seconds = seconds % 60;
        
        tvTimer.setText(String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds));
    }

    private void completeRestPeriod() {
        isRestPhase = false;
        cardTimer.setVisibility(View.GONE);
        
        // Reset pause state
        isTimerPaused = false;
        btnPauseTimer.setText(R.string.pause);
        
        // Update FAB
        fabNextExercise.setText(R.string.complete_set);
        
        updateCurrentExerciseDisplay();
    }    private void showWorkoutCompleteDialog() {
        // Calculate workout duration
        long workoutEndTime = System.currentTimeMillis();
        long workoutDurationMs = workoutEndTime - workoutStartTime;
        String formattedDuration = formatDuration(workoutDurationMs);
        
        // Calculate total reps (if available as numbers)
        int totalReps = 0;
        for (ExerciseDetail exercise : exerciseDetails) {
            try {
                // Try to parse reps if it's a numeric value
                int reps = Integer.parseInt(exercise.getReps());
                totalReps += reps * exercise.getSets();
            } catch (NumberFormatException e) {
                // Skip if reps is not a simple number (e.g., "8-12" or "AMRAP")
            }
        }
        
        String message = getString(R.string.workout_complete_message) + 
                "\n\nYou completed:\n" +
                "• " + exerciseDetails.size() + " exercises\n" +
                "• " + totalCompletedSets + " sets\n" +
                "• Duration: " + formattedDuration;
                
        if (totalReps > 0) {
            message += "\n• Approximately " + totalReps + " reps";
        }
        
        new AlertDialog.Builder(this)
                .setTitle(R.string.workout_complete)
                .setMessage(message)
                .setCancelable(false)
                .setPositiveButton(R.string.view_summary, (dialog, which) -> {
                    // Launch workout summary activity
                    Intent intent = new Intent(this, WorkoutSummaryActivity.class);
                    intent.putExtra("WORKOUT_ID", workout.getId());
                    intent.putExtra("WORKOUT_NAME", workout.getName());
                    intent.putExtra("TOTAL_EXERCISES", exerciseDetails.size());
                    intent.putExtra("TOTAL_SETS", totalCompletedSets);
                    intent.putExtra("DURATION", formattedDuration);
                    intent.putExtra("COMPLETED_EXERCISES", new ArrayList<>(exerciseDetails));
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton(R.string.return_to_workouts, (dialog, which) -> {
                    finish();
                })
                .show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        // Ask for confirmation if workout is in progress
        if (!exerciseDetails.isEmpty() && currentExerciseIndex < exerciseDetails.size()) {
            new AlertDialog.Builder(this)
                    .setTitle("Quit Workout?")
                    .setMessage("Are you sure you want to quit this workout? Your progress will not be saved.")
                    .setPositiveButton("Quit", (dialog, which) -> finish())
                    .setNegativeButton("Continue Workout", null)
                    .show();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (restTimer != null) {
            restTimer.cancel();
        }
    }    @Override
    public void onExerciseClick(ExerciseDetail exercise, int position) {
        // If user clicks on an exercise in the list, jump to that exercise
        if (position != currentExerciseIndex) {
            new AlertDialog.Builder(this)
                    .setTitle("Jump to Exercise")
                    .setMessage("Do you want to jump to " + exercise.getName() + "?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        currentExerciseIndex = position;
                        currentSetNumber = 1;
                        if (restTimer != null) {
                            restTimer.cancel();
                        }
                        isRestPhase = false;
                        cardTimer.setVisibility(View.GONE);
                        updateCurrentExerciseDisplay();
                    })
                    .setNegativeButton("No", null)
                    .show();
        }
    }
    
    /**
     * Formats milliseconds to a human-readable duration (mm:ss or hh:mm:ss)
     * @param durationMs Duration in milliseconds
     * @return Formatted duration string
     */
    private String formatDuration(long durationMs) {
        long seconds = durationMs / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        
        minutes %= 60;
        seconds %= 60;
        
        if (hours > 0) {
            return String.format(Locale.getDefault(), "%d:%02d:%02d", hours, minutes, seconds);
        } else {
            return String.format(Locale.getDefault(), "%d:%02d", minutes, seconds);
        }
    }
}
