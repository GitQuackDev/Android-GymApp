package com.example.aimingfitness;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.aimingfitness.viewmodel.ExerciseLibraryViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import android.util.Log;

public class AddWorkoutActivity extends AppCompatActivity implements 
        ExerciseDetailAdapter.OnExerciseRemoveListener,
        ExerciseSelectionDialogFragment.OnExerciseSelectedListener {

    private static final String TAG = "AddWorkoutActivity"; // Logging TAG

    private TextInputLayout tilWorkoutName, tilWorkoutDescription, tilWorkoutType, tilWorkoutDuration, tilWorkoutDifficulty;
    private TextInputEditText etWorkoutName, etWorkoutDescription, etWorkoutDuration, etCustomMuscleGroup;
    private AutoCompleteTextView actWorkoutType, actWorkoutDifficulty;
    private MaterialButton btnSaveWorkout, btnCancelWorkout;
    private RecyclerView rvExerciseDetails, rvExerciseLibrary;
    private ExerciseDetailAdapter exerciseDetailAdapter;
    private ExerciseSelectionAdapter exerciseLibraryAdapter;
    private List<ExerciseDetail> exerciseDetailsList;
    private FirebaseFirestore db;
    private ChipGroup chipGroupMuscleGroups;
    private FloatingActionButton fabAddExercise;
    private TextView tvNoExercises;
    private TextInputEditText etSearchExerciseLibrary;
    private ChipGroup chipGroupExerciseFilter;
    private TextView tvNoExercisesFound;
    private List<ExerciseLibrary> allExercises;
    private ExerciseLibraryViewModel exerciseLibraryViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_add_workout);

        db = FirebaseFirestore.getInstance();

        // Initialize views
        tilWorkoutName = findViewById(R.id.tilWorkoutName);
        etWorkoutName = findViewById(R.id.etWorkoutName);
        tilWorkoutDescription = findViewById(R.id.tilWorkoutDescription);
        etWorkoutDescription = findViewById(R.id.etWorkoutDescription);
        tilWorkoutType = findViewById(R.id.tilWorkoutType);
        actWorkoutType = findViewById(R.id.actWorkoutType);
        tilWorkoutDuration = findViewById(R.id.tilWorkoutDuration);
        etWorkoutDuration = findViewById(R.id.etWorkoutDuration);
        tilWorkoutDifficulty = findViewById(R.id.tilWorkoutDifficulty);
        actWorkoutDifficulty = findViewById(R.id.actWorkoutDifficulty);
        chipGroupMuscleGroups = findViewById(R.id.chipGroupMuscleGroups);
        etCustomMuscleGroup = findViewById(R.id.etCustomMuscleGroup);
        fabAddExercise = findViewById(R.id.fabAddExercise);
        rvExerciseDetails = findViewById(R.id.rvExerciseDetails);
        rvExerciseLibrary = findViewById(R.id.rvExerciseLibrary);
        tvNoExercises = findViewById(R.id.tvNoExercises);
        btnSaveWorkout = findViewById(R.id.btnSaveWorkout);
        btnCancelWorkout = findViewById(R.id.btnCancelWorkout);
        etSearchExerciseLibrary = findViewById(R.id.etSearchExerciseLibrary);
        chipGroupExerciseFilter = findViewById(R.id.chipGroupExerciseFilter);
        tvNoExercisesFound = findViewById(R.id.tvNoExercisesFound);

        // Setup ArrayAdapter for AutoCompleteTextViews using string arrays from resources
        ArrayAdapter<CharSequence> workoutTypeAdapter = ArrayAdapter.createFromResource(this,
                R.array.workout_types, android.R.layout.simple_dropdown_item_1line);
        actWorkoutType.setAdapter(workoutTypeAdapter);

        ArrayAdapter<CharSequence> difficultyAdapter = ArrayAdapter.createFromResource(this,
                R.array.workout_difficulty_levels, android.R.layout.simple_dropdown_item_1line);
        actWorkoutDifficulty.setAdapter(difficultyAdapter);

        // Setup default muscle group chips
        setupDefaultMuscleGroupChips();

        // Add custom muscle group chip when user enters text and presses done
        etCustomMuscleGroup.setOnEditorActionListener((v, actionId, event) -> {
            String customGroup = etCustomMuscleGroup.getText().toString().trim();
            if (!customGroup.isEmpty()) {
                addMuscleGroupChip(customGroup);
                etCustomMuscleGroup.setText("");
            }
            return false;
        });

        // Initialize RecyclerView and Adapter for workout exercises
        exerciseDetailsList = new ArrayList<>();
        exerciseDetailAdapter = new ExerciseDetailAdapter(this, exerciseDetailsList, this);
        rvExerciseDetails.setLayoutManager(new LinearLayoutManager(this));
        rvExerciseDetails.setAdapter(exerciseDetailAdapter);

        // Initialize ViewModel
        exerciseLibraryViewModel = new ViewModelProvider(this).get(ExerciseLibraryViewModel.class);
        
        // Initialize exercise adapter with an empty list (will be populated from ViewModel)
        exerciseLibraryAdapter = new ExerciseSelectionAdapter(this, new ArrayList<>(), new ExerciseSelectionAdapter.OnExerciseSelectedListener() {
            @Override
            public void onExerciseSelected(ExerciseLibrary exercise) {
                // Show the exercise details dialog for configuring reps/sets
                showExerciseDetailsDialog(exercise);
            }
            
            @Override
            public void onAddButtonClicked(ExerciseLibrary exercise) {
                // Quick add with default values
                addExerciseToWorkout(exercise, 3, 10, "");
            }
        });
        rvExerciseLibrary.setLayoutManager(new LinearLayoutManager(this));
        rvExerciseLibrary.setAdapter(exerciseLibraryAdapter);
        
        // Setup FAB for adding custom exercises
        FloatingActionButton fabAddCustomExercise = findViewById(R.id.fabAddCustomExercise);
        fabAddCustomExercise.setOnClickListener(v -> {
            showAddCustomExerciseDialog();
        });
        
        // Observe LiveData from ViewModel
        exerciseLibraryViewModel.getExercises().observe(this, exercises -> {
            // Update adapter with new data
            exerciseLibraryAdapter.updateExercises(exercises);
            
            // Show/hide no results message
            if (exercises == null || exercises.isEmpty()) {
                tvNoExercisesFound.setVisibility(View.VISIBLE);
                rvExerciseLibrary.setVisibility(View.GONE);
            } else {
                tvNoExercisesFound.setVisibility(View.GONE);
                rvExerciseLibrary.setVisibility(View.VISIBLE);
            }
        });
        
        // Show loading indicator when loading
        exerciseLibraryViewModel.getIsLoading().observe(this, isLoading -> {
            // You could show a loading progress indicator here
        });
        
        // Show error messages
        exerciseLibraryViewModel.getErrorMessage().observe(this, errorMsg -> {
            if (errorMsg != null && !errorMsg.isEmpty()) {
                Toast.makeText(this, errorMsg, Toast.LENGTH_SHORT).show();
            }
        });
        
        // Setup search functionality
        etSearchExerciseLibrary.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                filterExercises();
            }
        });
        
        // Setup filter chip functionality
        chipGroupExerciseFilter.setOnCheckedStateChangeListener((group, checkedIds) -> {
            filterExercises();
        });
        
        btnSaveWorkout.setOnClickListener(v -> saveWorkout());
        btnCancelWorkout.setOnClickListener(v -> finish());
        
        updateExerciseListVisibility();
    }

    private void showExerciseSelectionDialog() {
        ExerciseSelectionDialogFragment dialogFragment = new ExerciseSelectionDialogFragment();
        dialogFragment.setOnExerciseSelectedListener(this);
        dialogFragment.show(getSupportFragmentManager(), "ExerciseSelection");
    }

    @Override
    public void onExerciseSelected(ExerciseLibrary exercise) {
        // When an exercise is selected from the library, show the details dialog with pre-filled name
        showExerciseDetailsDialog(exercise.getName());
    }

    @Override
    public void onCustomExerciseRequested() {
        // When user wants to add a custom exercise, show an empty details dialog
        showExerciseDetailsDialog("");
    }

    private void setupDefaultMuscleGroupChips() {
        String[] defaultGroups = {"Chest", "Back", "Legs", "Shoulders", "Arms", "Abs", "Glutes", "Full Body"};
        for (String group : defaultGroups) {
            addMuscleGroupChip(group);
        }
    }

    private void addMuscleGroupChip(String group) {
        Chip chip = new Chip(this);
        chip.setText(group);
        chip.setCheckable(true);
        chip.setChecked(false);
        chipGroupMuscleGroups.addView(chip);
    }

    private List<String> getSelectedMuscleGroups() {
        List<String> selected = new ArrayList<>();
        for (int i = 0; i < chipGroupMuscleGroups.getChildCount(); i++) {
            Chip chip = (Chip) chipGroupMuscleGroups.getChildAt(i);
            if (chip.isChecked()) {
                selected.add(chip.getText().toString());
            }
        }
        return selected;
    }

    private void showExerciseDetailsDialog(String exerciseName) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_exercise_detail, null);
        builder.setView(dialogView);

        TextInputEditText etExerciseName = dialogView.findViewById(R.id.etExerciseName);
        TextInputEditText etExerciseSets = dialogView.findViewById(R.id.etExerciseSets);
        TextInputEditText etExerciseReps = dialogView.findViewById(R.id.etExerciseReps);
        TextInputEditText etExerciseWeight = dialogView.findViewById(R.id.etExerciseWeight);
        TextInputEditText etExerciseRestTime = dialogView.findViewById(R.id.etExerciseRestTime);
        TextInputEditText etExerciseNotes = dialogView.findViewById(R.id.etExerciseNotes);
        MaterialCheckBox cbWarmUpSet = dialogView.findViewById(R.id.cbWarmUpSet);
        MaterialButton btnSaveExerciseDetail = dialogView.findViewById(R.id.btnSaveExerciseDetail);
        MaterialButton btnCancelExerciseDetail = dialogView.findViewById(R.id.btnCancelExerciseDetail);

        // Pre-fill the exercise name if provided from library
        if (!TextUtils.isEmpty(exerciseName)) {
            etExerciseName.setText(exerciseName);
        }

        androidx.appcompat.app.AlertDialog dialog = builder.create();

        btnSaveExerciseDetail.setOnClickListener(v -> {
            String name = etExerciseName.getText().toString().trim();
            String setsStr = etExerciseSets.getText().toString().trim();
            String repsStr = etExerciseReps.getText().toString().trim();
            String weightStr = etExerciseWeight.getText().toString().trim();
            String restTimeStr = etExerciseRestTime.getText().toString().trim();
            String notes = etExerciseNotes.getText().toString().trim();
            boolean isWarmUp = cbWarmUpSet.isChecked();

            // Validation
            if (TextUtils.isEmpty(name)) {
                etExerciseName.setError("Exercise name is required");
                return;
            }
            if (TextUtils.isEmpty(setsStr)) {
                etExerciseSets.setError("Sets are required");
                return;
            }
            if (TextUtils.isEmpty(repsStr)) {
                etExerciseReps.setError("Reps are required");
                return;
            }

            int sets = 0;
            try {
                sets = Integer.parseInt(setsStr);
            } catch (NumberFormatException e) {
                etExerciseSets.setError("Invalid number for sets");
                return;
            }

            int restTime = 0;
            if (!TextUtils.isEmpty(restTimeStr)) {
                try {
                    restTime = Integer.parseInt(restTimeStr);
                } catch (NumberFormatException e) {
                    etExerciseRestTime.setError("Invalid number for rest time");
                    return;
                }
            }

            ExerciseDetail newExercise = new ExerciseDetail(
                    name,
                    sets,
                    repsStr,
                    weightStr,
                    restTime,
                    notes,
                    isWarmUp
            );

            exerciseDetailsList.add(newExercise);
            exerciseDetailAdapter.notifyItemInserted(exerciseDetailsList.size() - 1);
            updateExerciseListVisibility();
            Toast.makeText(AddWorkoutActivity.this, "Exercise Added: " + name, Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });

        btnCancelExerciseDetail.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }
    
    /**
     * Shows a dialog to configure exercise details (sets, reps, etc.)
     */
    private void showExerciseDetailsDialog(ExerciseLibrary exercise) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_exercise_details, null);
        
        // Get references to views
        TextInputEditText etSets = dialogView.findViewById(R.id.etSets);
        TextInputEditText etReps = dialogView.findViewById(R.id.etReps);
        TextInputEditText etNotes = dialogView.findViewById(R.id.etNotes);
        
        // Set default values
        etSets.setText("3");
        etReps.setText("10");
        
        // Create the dialog
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this)
                .setView(dialogView)
                .setTitle("Configure " + exercise.getName())
                .setPositiveButton("Add", (dialog, which) -> {
                    // Get input values
                    int sets = Integer.parseInt(etSets.getText().toString());
                    int reps = Integer.parseInt(etReps.getText().toString());
                    String notes = etNotes.getText().toString().trim();
                    
                    // Add to workout
                    addExerciseToWorkout(exercise, sets, reps, notes);
                })
                .setNegativeButton("Cancel", null);
        
        builder.create().show();
    }
    
    /**
     * Adds an exercise to the workout with specified details
     */
    private void addExerciseToWorkout(ExerciseLibrary exercise, int sets, int reps, String notes) {
        // Create a new ExerciseDetail object
        ExerciseDetail detail = new ExerciseDetail(
                exercise.getName(), // name
                sets,               // sets
                String.valueOf(reps), // reps as String
                "",                 // weight (empty for now)
                0,                  // restTimeSeconds (default 0)
                notes,              // notes
                false               // isWarmUp
        );
        
        // Add to the list
        if (exerciseDetailsList == null) {
            exerciseDetailsList = new ArrayList<>();
        }
        exerciseDetailsList.add(detail);
        
        // Update adapter
        if (exerciseDetailAdapter == null) {
            exerciseDetailAdapter = new ExerciseDetailAdapter(this, exerciseDetailsList, this);
            rvExerciseDetails.setAdapter(exerciseDetailAdapter);
            rvExerciseDetails.setLayoutManager(new LinearLayoutManager(this));
        } else {
            exerciseDetailAdapter.notifyDataSetChanged();
        }
        
        // Show/hide no exercises message
        if (tvNoExercises != null) {
            tvNoExercises.setVisibility(exerciseDetailsList.isEmpty() ? View.VISIBLE : View.GONE);
        }
        
        // Show toast confirmation
        Toast.makeText(this, exercise.getName() + " added to workout", Toast.LENGTH_SHORT).show();

        // Ensure the exercise list is visible
        updateExerciseListVisibility();
    }
    
    private void updateExerciseListVisibility() {
        if (exerciseDetailsList.isEmpty()) {
            tvNoExercises.setVisibility(View.VISIBLE);
            rvExerciseDetails.setVisibility(View.GONE);
        } else {
            tvNoExercises.setVisibility(View.GONE);
            rvExerciseDetails.setVisibility(View.VISIBLE);
        }
    }

    private void saveWorkout() {
        String name = etWorkoutName.getText().toString().trim();
        String description = etWorkoutDescription.getText().toString().trim();
        String type = actWorkoutType.getText().toString().trim();
        String duration = etWorkoutDuration.getText().toString().trim();
        String difficulty = actWorkoutDifficulty.getText().toString().trim();
        List<String> muscleGroupsList = getSelectedMuscleGroups();
        String muscleGroups = String.join(", ", muscleGroupsList);

        // Basic Validation
        if (TextUtils.isEmpty(name)) {
            tilWorkoutName.setError(getString(R.string.workout_name_hint) + " is required");
            return;
        }
        tilWorkoutName.setError(null);

        if (TextUtils.isEmpty(type)) {
            tilWorkoutType.setError(getString(R.string.workout_type_hint) + " is required");
            return;
        }
        tilWorkoutType.setError(null);

        if (TextUtils.isEmpty(duration)) {
            tilWorkoutDuration.setError(getString(R.string.workout_duration_hint) + " is required");
            return;
        }
        tilWorkoutDuration.setError(null);

        if (TextUtils.isEmpty(difficulty)) {
            tilWorkoutDifficulty.setError(getString(R.string.workout_difficulty_hint) + " is required");
            return;
        }
        tilWorkoutDifficulty.setError(null);

        if (exerciseDetailsList.isEmpty()) {
            Toast.makeText(this, "Please add at least one exercise to the plan.", Toast.LENGTH_LONG).show();
            return;
        }

        // Create Workout Object
        String workoutId = UUID.randomUUID().toString();
        String currentUserId = null;
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            Log.d(TAG, "Current User ID for saving: " + currentUserId); // Log User ID
        } else {
            Log.e(TAG, "Error: User not authenticated. Cannot save workout.");
            Toast.makeText(this, "Error: User not authenticated. Cannot save workout.", Toast.LENGTH_LONG).show();
            return;
        }

        final String finalCurrentUserId = currentUserId; // Create effectively final variable
        final String finalWorkoutId = workoutId; // Create effectively final variable

        Log.d(TAG, "Generated Workout ID: " + finalWorkoutId); // Log Workout ID
        Workout newWorkout = new Workout(finalWorkoutId, finalCurrentUserId, name, description, type, duration, difficulty, muscleGroups, new ArrayList<>(exerciseDetailsList));
        Log.d(TAG, "Workout object to save: " + newWorkout.toString()); // Log Workout object

        // Save workout using a plain Map to ensure all fields are written
        Map<String, Object> workoutMap = new HashMap<>();
        workoutMap.put("id", finalWorkoutId);
        workoutMap.put("userId", finalCurrentUserId);
        workoutMap.put("name", name);
        workoutMap.put("description", description);
        workoutMap.put("type", type);
        workoutMap.put("duration", duration);
        workoutMap.put("difficulty", difficulty);
        workoutMap.put("muscleGroups", muscleGroups);
        workoutMap.put("exercises", new ArrayList<>(exerciseDetailsList));
        db.collection("workouts").document(finalWorkoutId)
                .set(workoutMap)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Successfully saved workout with ID: " + finalWorkoutId + " for user: " + finalCurrentUserId);
                    Toast.makeText(AddWorkoutActivity.this, "Workout Saved: " + newWorkout.getName(), Toast.LENGTH_LONG).show();
                    finish(); // Close activity on success
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error saving workout with ID: " + finalWorkoutId + " for user: " + finalCurrentUserId, e);
                    Toast.makeText(AddWorkoutActivity.this, "Error saving workout: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    @Override
    public void onExerciseRemoved(int position) {
        if (exerciseDetailsList != null && position >= 0 && position < exerciseDetailsList.size()) {
            // Remove the exercise from the list
            exerciseDetailsList.remove(position);
            
            // Update the adapter
            exerciseDetailAdapter.notifyItemRemoved(position);
            
            // Show/hide no exercises message
            if (tvNoExercises != null) {
                tvNoExercises.setVisibility(exerciseDetailsList.isEmpty() ? View.VISIBLE : View.GONE);
            }
            
            // Show toast confirmation
            Toast.makeText(this, "Exercise removed", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void filterExercises() {
        String searchQuery = etSearchExerciseLibrary.getText().toString().trim();
        String selectedMuscleGroup = "All";
        
        // Get the selected muscle group
        int checkedChipId = chipGroupExerciseFilter.getCheckedChipId();
        if (checkedChipId != View.NO_ID) {
            Chip selectedChip = chipGroupExerciseFilter.findViewById(checkedChipId);
            if (selectedChip != null) {
                selectedMuscleGroup = selectedChip.getText().toString();
            }
        }
        
        // Apply filters through ViewModel
        exerciseLibraryViewModel.filterExercises(searchQuery, selectedMuscleGroup);
    }

    /**
     * Shows dialog for adding a custom exercise to the database
     */
    private void showAddCustomExerciseDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_custom_exercise, null);
        
        // Get references to UI components
        TextInputEditText etCustomExerciseName = dialogView.findViewById(R.id.etCustomExerciseName);
        AutoCompleteTextView actCustomPrimaryMuscle = dialogView.findViewById(R.id.actCustomPrimaryMuscle);
        TextInputEditText etCustomSecondaryMuscles = dialogView.findViewById(R.id.etCustomSecondaryMuscles);
        TextInputEditText etCustomEquipment = dialogView.findViewById(R.id.etCustomEquipment);
        TextInputEditText etCustomInstructions = dialogView.findViewById(R.id.etCustomInstructions);
        MaterialButton btnCancelCustomExercise = dialogView.findViewById(R.id.btnCancelCustomExercise);
        MaterialButton btnSaveCustomExercise = dialogView.findViewById(R.id.btnSaveCustomExercise);
        
        // Set up the muscle group dropdown
        String[] muscleGroups = {"Chest", "Back", "Legs", "Shoulders", "Arms", "Abs", "Cardio"};
        ArrayAdapter<String> muscleGroupAdapter = new ArrayAdapter<>(
                this, R.layout.dropdown_item, muscleGroups);
        actCustomPrimaryMuscle.setAdapter(muscleGroupAdapter);
        
        // Create the dialog
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this)
                .setView(dialogView);
        
        androidx.appcompat.app.AlertDialog dialog = builder.create();
        
        // Set button click listeners
        btnCancelCustomExercise.setOnClickListener(v -> {
            dialog.dismiss();
        });
        
        btnSaveCustomExercise.setOnClickListener(v -> {
            // Validate input fields
            String exerciseName = etCustomExerciseName.getText().toString().trim();
            String primaryMuscleGroup = actCustomPrimaryMuscle.getText().toString().trim();
            
            if (TextUtils.isEmpty(exerciseName)) {
                Toast.makeText(this, "Please enter exercise name", Toast.LENGTH_SHORT).show();
                return;
            }
            
            if (TextUtils.isEmpty(primaryMuscleGroup)) {
                Toast.makeText(this, "Please select a primary muscle group", Toast.LENGTH_SHORT).show();
                return;
            }
            
            // Process secondary muscles (comma separated)
            String secondaryMusclesStr = etCustomSecondaryMuscles.getText().toString().trim();
            List<String> secondaryMusclesList = new ArrayList<>();
            if (!TextUtils.isEmpty(secondaryMusclesStr)) {
                String[] secondaryMuscles = secondaryMusclesStr.split(",");
                for (String muscle : secondaryMuscles) {
                    secondaryMusclesList.add(muscle.trim());
                }
            }
            
            // Process equipment (comma separated)
            String equipmentStr = etCustomEquipment.getText().toString().trim();
            List<String> equipmentList = new ArrayList<>();
            if (!TextUtils.isEmpty(equipmentStr)) {
                String[] equipment = equipmentStr.split(",");
                for (String item : equipment) {
                    equipmentList.add(item.trim());
                }
            }
            
            // Get instructions
            String instructions = etCustomInstructions.getText().toString().trim();
            
            // Create new exercise object
            ExerciseLibrary newExercise = new ExerciseLibrary(
                    UUID.randomUUID().toString(),  // Generate unique ID
                    exerciseName,
                    primaryMuscleGroup,
                    secondaryMusclesList,
                    equipmentList,
                    instructions,
                    ""  // No image URL for custom exercises
            );
            
            // Save to database via ViewModel
            exerciseLibraryViewModel.addExercise(newExercise);
            
            Toast.makeText(this, "Exercise added successfully", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });
        
        // Show the dialog
        dialog.show();
    }
}
