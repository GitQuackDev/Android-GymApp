package com.example.aimingfitness;

import android.content.Intent;
import android.graphics.drawable.Drawable; 
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.util.Log; 
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable; 
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource; 
import com.bumptech.glide.load.engine.GlideException; 
import com.bumptech.glide.request.RequestListener; 
import com.bumptech.glide.request.target.Target; 
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
    private static final int DEFAULT_REST_TIME_SECONDS = 60; 

    private Workout workout;
    private List<ExerciseDetail> exerciseDetails = new ArrayList<>();
    
    
    private CollapsingToolbarLayout collapsingToolbar;
    private ImageView ivWorkoutHeader; 
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
      
    private int currentExerciseIndex = 0;
    private int currentSetNumber = 1;
    private boolean isRestPhase = false;
    private CountDownTimer restTimer;
    private boolean isTimerPaused = false;
    private long timeRemaining = 0;
    
    
    private long workoutStartTime;
    private int totalCompletedSets = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workout_session);

        
        if (getIntent() != null) {
            workout = (Workout) getIntent().getSerializableExtra("workout");
        }
        
        if (workout == null) {
            Toast.makeText(this, "Error loading workout details", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        
        initializeUI();
        
        
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        
        
        collapsingToolbar.setTitle(workout.getName());

        
        Log.d(TAG, "WorkoutSessionActivity: imageUrl=" + workout.getImageUrl());
        if (workout.getImageUrl() != null && !workout.getImageUrl().isEmpty()) {
            try {
                Uri imageUri = Uri.parse(workout.getImageUrl());
                Glide.with(this)
                     .load(imageUri)
                     .placeholder(R.drawable.ic_placeholder_workout)
                     .error(R.drawable.ic_placeholder_workout)
                     .centerCrop()
                     .into(ivWorkoutHeader);
            } catch (Exception e) {
                Log.e(TAG, "WorkoutSessionActivity: Failed to load image URI: " + workout.getImageUrl(), e);
                Toast.makeText(this, "Workout image not found. Please re-select the image.", Toast.LENGTH_LONG).show();
                ivWorkoutHeader.setImageResource(R.drawable.ic_placeholder_workout);
            }
        } else {
            Toast.makeText(this, "No workout image set. Please edit the workout and select an image.", Toast.LENGTH_LONG).show();
            ivWorkoutHeader.setImageResource(R.drawable.ic_placeholder_workout);
        }

          
        exerciseDetails = workout.getExercises();
        if (exerciseDetails == null) {
            exerciseDetails = new ArrayList<>();
        }
        
        
        workoutStartTime = System.currentTimeMillis();
        
        int totalExercises = exerciseDetails.size();
        tvTotalExercises.setText(String.valueOf(totalExercises));
        tvEstimatedTime.setText(workout.getDuration());
        tvDifficulty.setText(workout.getDifficulty());
        
        
        exerciseAdapter = new WorkoutExerciseAdapter(this, exerciseDetails, this);
        rvExerciseList.setAdapter(exerciseAdapter);
        
        
        if (!exerciseDetails.isEmpty()) {
            updateCurrentExerciseDisplay();
        } else {
            
            showNoExercisesMessage();
        }

        
        fabNextExercise.setOnClickListener(v -> {
            if (isRestPhase) {
                
                if (restTimer != null) {
                    restTimer.cancel();
                }
                completeRestPeriod();
            } else {
                
                completeCurrentSet();
            }
        });

        
        btnSkipTimer.setOnClickListener(v -> {
            if (restTimer != null) {
                restTimer.cancel();
            }
            completeRestPeriod();
        });

        
        btnPauseTimer.setOnClickListener(v -> {
            if (isTimerPaused) {
                
                startRestTimer(timeRemaining);
                btnPauseTimer.setText(R.string.pause);
                isTimerPaused = false;
            } else {
                
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
        ivWorkoutHeader = findViewById(R.id.ivWorkoutHeader); 
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
        
        
        cardTimer.setVisibility(View.GONE);
    }

    private void updateCurrentExerciseDisplay() {
        if (exerciseDetails.isEmpty() || currentExerciseIndex >= exerciseDetails.size()) {
            return;
        }
        
        ExerciseDetail exercise = exerciseDetails.get(currentExerciseIndex);
        tvCurrentExerciseName.setText(exercise.getName());
        
        
        if (exercise.getNotes() != null && !exercise.getNotes().isEmpty()) {
            tvCurrentExerciseDescription.setText(exercise.getNotes());
            tvCurrentExerciseDescription.setVisibility(View.VISIBLE);
        } else {
            tvCurrentExerciseDescription.setVisibility(View.GONE);
        }
        
        
        String setProgress = String.format(Locale.getDefault(), "%d / %d", currentSetNumber, exercise.getSets());
        tvCurrentSetProgress.setText(setProgress);
        
        
        tvCurrentReps.setText(exercise.getReps());
        
        
        if (exercise.getWeight() != null && !exercise.getWeight().isEmpty()) {
            tvCurrentWeight.setText(exercise.getWeight());
            llWeight.setVisibility(View.VISIBLE);
        } else {
            llWeight.setVisibility(View.GONE);
        }
        
        
        exerciseAdapter.setCurrentExerciseIndex(currentExerciseIndex);
        exerciseAdapter.notifyDataSetChanged();
        
        
        if (isRestPhase) {
            fabNextExercise.setText(R.string.skip);
            fabNextExercise.setIconResource(R.drawable.ic_check);
        } else {
            fabNextExercise.setText(R.string.complete_set);
            fabNextExercise.setIconResource(R.drawable.ic_check);
        }
    }

    private void showNoExercisesMessage() {
        
        tvCurrentExerciseName.setText(R.string.no_exercises_found);
        tvCurrentExerciseDescription.setVisibility(View.GONE);
        tvCurrentSetProgress.setText("0 / 0");
        tvCurrentReps.setText("-");
        llWeight.setVisibility(View.GONE);
        fabNextExercise.setVisibility(View.GONE);
    }    private void completeCurrentSet() {
        totalCompletedSets++;
        currentSetNumber++;
        ExerciseDetail currentExercise = exerciseDetails.get(currentExerciseIndex);

        if (currentSetNumber > currentExercise.getSets()) {
            
            
            exerciseAdapter.markExerciseCompleted(currentExerciseIndex);
            
            currentExerciseIndex++; 
            currentSetNumber = 1; 

            if (currentExerciseIndex < exerciseDetails.size()) {
                
                ExerciseDetail nextExercise = exerciseDetails.get(currentExerciseIndex);
                int restTimeForNext = nextExercise.getRestTimeSeconds() > 0 ? nextExercise.getRestTimeSeconds() : DEFAULT_REST_TIME_SECONDS;
                startRestTimer(restTimeForNext * 1000L);
                updateCurrentExerciseDisplay(); 
            } else {
                
                finishWorkout();
            }
        } else {
            
            int restTimeForCurrent = currentExercise.getRestTimeSeconds() > 0 ? currentExercise.getRestTimeSeconds() : DEFAULT_REST_TIME_SECONDS;
            startRestTimer(restTimeForCurrent * 1000L);
            updateCurrentExerciseDisplay(); 
        }
    }

    private void startRestPeriod(int seconds) {
        isRestPhase = true;
        if (seconds <= 0) {
            seconds = DEFAULT_REST_TIME_SECONDS;
        }
        
        
        cardTimer.setVisibility(View.VISIBLE);
        startRestTimer(seconds * 1000L); 
        
        
        fabNextExercise.setText(R.string.skip);
        
        
        exerciseAdapter.notifyDataSetChanged();
    }

    private void startRestTimer(long millisInFuture) {
        cardTimer.setVisibility(View.VISIBLE);
        tvTimerLabel.setText("REST");
        fabNextExercise.setText(R.string.skip_rest);
        isRestPhase = true;
        isTimerPaused = false;
        btnPauseTimer.setText(R.string.pause);
        btnPauseTimer.setVisibility(View.VISIBLE);
        btnSkipTimer.setVisibility(View.VISIBLE);

        restTimer = new CountDownTimer(millisInFuture, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeRemaining = millisUntilFinished;
                tvTimer.setText(String.format(Locale.getDefault(), "%02d:%02d",
                        (millisUntilFinished / 1000) / 60, (millisUntilFinished / 1000) % 60));
            }

            @Override
            public void onFinish() {
                completeRestPeriod();
            }
        }.start();
    }

    private void completeRestPeriod() {
        isRestPhase = false;
        cardTimer.setVisibility(View.GONE);
        fabNextExercise.setText(R.string.next_set_exercise);
        btnPauseTimer.setVisibility(View.GONE);
        btnSkipTimer.setVisibility(View.GONE);

        
        if (currentExerciseIndex >= exerciseDetails.size()) {
            finishWorkout();
            return;
        }
        updateCurrentExerciseDisplay(); 
    }    private void showWorkoutCompleteDialog() {
        
        long workoutEndTime = System.currentTimeMillis();
        long workoutDurationMs = workoutEndTime - workoutStartTime;
        String formattedDuration = formatDuration(workoutDurationMs);
        
        
        int totalReps = 0;
        for (ExerciseDetail exercise : exerciseDetails) {
            try {
                
                int reps = Integer.parseInt(exercise.getReps());
                totalReps += reps * exercise.getSets();
            } catch (NumberFormatException e) {
                
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

    /**
     * Called when the workout is finished. Navigates to the summary screen.
     */    private void finishWorkout() {
        
        long workoutEndTime = System.currentTimeMillis();
        long workoutDurationMs = workoutEndTime - workoutStartTime;
        String formattedDuration = formatDuration(workoutDurationMs);
        
        showWorkoutCompleteDialog();
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
