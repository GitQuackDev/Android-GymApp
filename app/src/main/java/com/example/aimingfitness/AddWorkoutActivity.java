package com.example.aimingfitness;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.chip.Chip;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.chip.ChipGroup;
import com.example.aimingfitness.viewmodel.ExerciseLibraryViewModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class AddWorkoutActivity extends AppCompatActivity implements
        ExerciseDetailAdapter.OnExerciseRemoveListener,
        ExerciseSelectionDialogFragment.OnExerciseSelectedListener {

    private static final String TAG = "AddWorkoutActivity";
    private static final int PICK_IMAGE_REQUEST = 1;

    private TextInputLayout tilWorkoutName, tilWorkoutDescription, tilWorkoutType, tilWorkoutDuration, tilWorkoutDifficulty;
    private TextInputEditText etWorkoutName, etWorkoutDescription, etWorkoutDuration, etCustomMuscleGroup;
    private AutoCompleteTextView actWorkoutType, actWorkoutDifficulty;
    private MaterialButton btnSaveWorkout, btnCancelWorkout, btnSelectWorkoutImage;
    private ImageView ivWorkoutImagePreview;
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
    private boolean isEditMode = false;
    private Workout existingWorkout = null;
    private Uri selectedImageUri = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_add_workout);

        db = FirebaseFirestore.getInstance();
        exerciseDetailsList = new ArrayList<>();

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
        ivWorkoutImagePreview = findViewById(R.id.ivWorkoutImagePreview);
        btnSelectWorkoutImage = findViewById(R.id.btnSelectWorkoutImage);

        btnSelectWorkoutImage.setOnClickListener(v -> openImageChooser());

        exerciseDetailAdapter = new ExerciseDetailAdapter(this, exerciseDetailsList, this);
        rvExerciseDetails.setLayoutManager(new LinearLayoutManager(this));
        rvExerciseDetails.setAdapter(exerciseDetailAdapter);

        if (getIntent().hasExtra("isEditMode") && getIntent().getBooleanExtra("isEditMode", false)) {
            isEditMode = true;
            if (getIntent().hasExtra("workout")) {
                existingWorkout = (Workout) getIntent().getSerializableExtra("workout");
                if (existingWorkout != null) {
                    Log.d(TAG, "Edit mode: Populating fields for workout ID: " + existingWorkout.getId());
                    populateFieldsForEdit();
                    btnSaveWorkout.setText(getString(R.string.update_workout_button_text));
                } else {
                    Log.e(TAG, "Edit mode: Workout object is null. Cannot populate fields.");
                    Toast.makeText(this, "Error: Could not load workout details for editing.", Toast.LENGTH_LONG).show();
                    finish();
                    return;
                }
            } else {
                Log.e(TAG, "Edit mode: Intent is missing 'workout' extra. Cannot populate fields.");
                Toast.makeText(this, "Error: Workout data not provided for editing.", Toast.LENGTH_LONG).show();
                finish();
                return;
            }
        } else {
            Log.d(TAG, "Add mode: Initializing for new workout.");
             setupDefaultMuscleGroupChips();
        }

        ArrayAdapter<CharSequence> workoutTypeAdapter = ArrayAdapter.createFromResource(this,
                R.array.workout_types, android.R.layout.simple_dropdown_item_1line);
        actWorkoutType.setAdapter(workoutTypeAdapter);

        ArrayAdapter<CharSequence> difficultyAdapter = ArrayAdapter.createFromResource(this,
                R.array.workout_difficulty_levels, android.R.layout.simple_dropdown_item_1line);
        actWorkoutDifficulty.setAdapter(difficultyAdapter);

        etCustomMuscleGroup.setOnEditorActionListener((v, actionId, event) -> {
            String customGroup = etCustomMuscleGroup.getText().toString().trim();
            if (!customGroup.isEmpty()) {
                addMuscleGroupChip(customGroup, true);
                etCustomMuscleGroup.setText("");
            }
            return false;
        });

        exerciseLibraryViewModel = new ViewModelProvider(this).get(ExerciseLibraryViewModel.class);
        
        exerciseLibraryAdapter = new ExerciseSelectionAdapter(this, new ArrayList<>(), new ExerciseSelectionAdapter.OnExerciseSelectedListener() {
            @Override
            public void onExerciseSelected(ExerciseLibrary exercise) {
                showExerciseDetailsDialog(exercise);
            }
            
            @Override
            public void onAddButtonClicked(ExerciseLibrary exercise) {
                addExerciseToWorkout(exercise, 3, "10", "0kg", "", 0);
            }
        });
        rvExerciseLibrary.setLayoutManager(new LinearLayoutManager(this));
        rvExerciseLibrary.setAdapter(exerciseLibraryAdapter);
        
        FloatingActionButton fabAddCustomExercise = findViewById(R.id.fabAddCustomExercise);
        fabAddCustomExercise.setOnClickListener(v -> {
            showAddCustomExerciseDialog();
        });
        
        exerciseLibraryViewModel.getExercises().observe(this, exercises -> {
            exerciseLibraryAdapter.updateExercises(exercises);
            
            if (exercises == null || exercises.isEmpty()) {
                tvNoExercisesFound.setVisibility(View.VISIBLE);
                rvExerciseLibrary.setVisibility(View.GONE);
            } else {
                tvNoExercisesFound.setVisibility(View.GONE);
                rvExerciseLibrary.setVisibility(View.VISIBLE);
            }
        });
        
        exerciseLibraryViewModel.getIsLoading().observe(this, isLoading -> {
        });
        
        exerciseLibraryViewModel.getErrorMessage().observe(this, errorMsg -> {
            if (errorMsg != null && !errorMsg.isEmpty()) {
                Toast.makeText(this, errorMsg, Toast.LENGTH_SHORT).show();
            }
        });
        
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
        
        chipGroupExerciseFilter.setOnCheckedStateChangeListener((group, checkedIds) -> {
            filterExercises();
        });
        
        fabAddExercise.setOnClickListener(v -> showExerciseSelectionDialog());
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
        // Show dialog to input sets, reps, weight, notes, rest time
        if (exercise != null) {
            // Example: addExerciseToWorkout(exercise, 3, "10-12", "20kg", "Focus on form", 60);
            // For now, let's open the detailed dialog
            showExerciseDetailsDialog(exercise); // This was the previous call, ensure it's correct.
                                                 // The error is in a different showExerciseDetailsDialog overload or a direct call.
                                                 // Let's find the specific call at line 161.
                                                 // The error is: addExerciseToWorkout(exercise, 3, 10, "", 0);
                                                 // Corrected call based on signature: ExerciseLibrary, int, String, String, String, int
                                                 // addExerciseToWorkout(exercise, 3, "10", "0kg", "", 0); // Assuming default weight and no notes for this specific path if it exists
                                                 // However, the summary indicates showExerciseDetailsDialog(ExerciseLibrary exercise) was updated.
                                                 // The error is likely in the *other* showExerciseDetailsDialog(String exerciseName) if it calls addExerciseToWorkout directly with wrong params.
                                                 // Let's assume the error is in a direct call or the String overload of showExerciseDetailsDialog.
                                                 // The provided error points to line 161. I need to see that line.
                                                 // Based on the error: addExerciseToWorkout(exercise, 3, 10, "", 0);
                                                 // Required: ExerciseLibrary,int,String,String,String,int
                                                 // Found:    ExerciseLibrary,int,int,String,int
                                                 // This call is missing one String argument (likely notes) and reps is int instead of String.
                                                 // The call should be: addExerciseToWorkout(exercise, 3, "10", "some_weight_string", "some_notes_string", 0);
                                                 // Or, if it's a placeholder call: addExerciseToWorkout(exercise, 3, "10", "", "", 0);
        }
    }

    @Override
    public void onCustomExerciseRequested() {
        showExerciseDetailsDialog("");
    }

    private void populateFieldsForEdit() {
        if (existingWorkout == null) return;

        etWorkoutName.setText(existingWorkout.getName());
        etWorkoutDescription.setText(existingWorkout.getDescription());
        actWorkoutType.setText(existingWorkout.getType(), false);
        etWorkoutDuration.setText(existingWorkout.getDuration());
        actWorkoutDifficulty.setText(existingWorkout.getDifficulty(), false);

        chipGroupMuscleGroups.removeAllViews();
        setupDefaultMuscleGroupChips(); 
        if (existingWorkout.getMuscleGroups() != null && !existingWorkout.getMuscleGroups().isEmpty()) {
            String[] muscleGroupsArray = existingWorkout.getMuscleGroups().split(",\\s*");
            for (int i = 0; i < chipGroupMuscleGroups.getChildCount(); i++) {
                Chip chip = (Chip) chipGroupMuscleGroups.getChildAt(i);
                for (String groupName : muscleGroupsArray) {
                    if (chip.getText().toString().equalsIgnoreCase(groupName.trim())) {
                        chip.setChecked(true);
                        break;
                    }
                }
            }
            for (String groupName : muscleGroupsArray) {
                boolean found = false;
                for (int i = 0; i < chipGroupMuscleGroups.getChildCount(); i++) {
                    Chip chip = (Chip) chipGroupMuscleGroups.getChildAt(i);
                    if (chip.getText().toString().equalsIgnoreCase(groupName.trim())) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    addMuscleGroupChip(groupName.trim(), true);
                }
            }
        }

        if (existingWorkout.getExercises() != null) {
            exerciseDetailsList.clear();
            exerciseDetailsList.addAll(existingWorkout.getExercises());
            exerciseDetailAdapter.notifyDataSetChanged();
            updateExerciseListVisibility();
        }

        if (existingWorkout.getImageUrl() != null && !existingWorkout.getImageUrl().isEmpty()) {
            selectedImageUri = Uri.parse(existingWorkout.getImageUrl());
            Glide.with(this)
                 .load(selectedImageUri)
                 .placeholder(R.drawable.ic_placeholder_workout)
                 .error(R.drawable.ic_placeholder_workout)
                 .into(ivWorkoutImagePreview);
        } else {
            ivWorkoutImagePreview.setImageResource(R.drawable.ic_placeholder_workout);
        }
    }

    private void setupDefaultMuscleGroupChips() {
        String[] defaultGroups = {"Chest", "Back", "Legs", "Shoulders", "Arms", "Abs", "Glutes", "Full Body"};
        for (String group : defaultGroups) {
            addMuscleGroupChip(group, false);
        }
    }

    private void addMuscleGroupChip(String group, boolean isChecked) {
        Chip chip = new Chip(this);
        chip.setText(group);
        chip.setCheckable(true);
        chip.setChecked(isChecked);
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
        TextInputEditText etReps = dialogView.findViewById(R.id.etReps);
        TextInputEditText etExerciseWeight = dialogView.findViewById(R.id.etExerciseWeight);
        TextInputEditText etRestTime = dialogView.findViewById(R.id.etRestTime);
        TextInputEditText etExerciseNotes = dialogView.findViewById(R.id.etExerciseNotes);
        MaterialCheckBox cbWarmUpSet = dialogView.findViewById(R.id.cbWarmUpSet);
        MaterialButton btnSaveExerciseDetail = dialogView.findViewById(R.id.btnSaveExerciseDetail);
        MaterialButton btnCancelExerciseDetail = dialogView.findViewById(R.id.btnCancelExerciseDetail);

        if (!TextUtils.isEmpty(exerciseName)) {
            etExerciseName.setText(exerciseName);
        }

        androidx.appcompat.app.AlertDialog dialog = builder.create();

        btnSaveExerciseDetail.setOnClickListener(v -> {
            String name = etExerciseName.getText().toString().trim();
            String setsStr = etExerciseSets.getText().toString().trim();
            String repsStr = etReps.getText().toString().trim();
            String weightStr = etExerciseWeight.getText().toString().trim();
            String restTimeStr = etRestTime.getText() != null ? etRestTime.getText().toString().trim() : "";
            int restTimeSeconds = 0;
            if (!TextUtils.isEmpty(restTimeStr)) {
                try {
                    restTimeSeconds = Integer.parseInt(restTimeStr);
                    if (restTimeSeconds < 0) restTimeSeconds = 0;
                } catch (NumberFormatException e) {
                    restTimeSeconds = 0;
                }
            }
            String notes = etExerciseNotes.getText().toString().trim();
            boolean isWarmUp = cbWarmUpSet.isChecked();

            if (TextUtils.isEmpty(name)) {
                etExerciseName.setError("Exercise name is required");
                return;
            }
            if (TextUtils.isEmpty(setsStr)) {
                etExerciseSets.setError("Sets are required");
                return;
            }
            if (TextUtils.isEmpty(repsStr)) {
                etReps.setError("Reps are required");
                return;
            }

            int sets = 0;
            try {
                sets = Integer.parseInt(setsStr);
            } catch (NumberFormatException e) {
                etExerciseSets.setError("Invalid number for sets");
                return;
            }

            if (!TextUtils.isEmpty(restTimeStr)) {
                try {
                    restTimeSeconds = Integer.parseInt(restTimeStr);
                    if (restTimeSeconds < 0) {
                        etRestTime.setError("Rest time cannot be negative");
                        return;
                    }
                } catch (NumberFormatException e) {
                    etRestTime.setError("Invalid number for rest time");
                    return;
                }
            }

            ExerciseDetail newExercise = new ExerciseDetail(
                    name,
                    sets,
                    repsStr,
                    weightStr,
                    restTimeSeconds,
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
      private void showExerciseDetailsDialog(ExerciseLibrary exercise) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_exercise_details, null);
        builder.setView(dialogView);

        TextView tvExerciseTitle = dialogView.findViewById(R.id.tvExerciseTitle);
        ImageView ivExerciseIconDialog = dialogView.findViewById(R.id.ivExerciseIconDialog);
        TextInputEditText etSets = dialogView.findViewById(R.id.etSets);
        TextInputEditText etReps = dialogView.findViewById(R.id.etReps);
        TextInputEditText etWeight = dialogView.findViewById(R.id.etWeight); // Added
        TextInputEditText etNotes = dialogView.findViewById(R.id.etNotes);   // Added
        MaterialButton btnRest30s = dialogView.findViewById(R.id.btnRest30s);
        MaterialButton btnRest60s = dialogView.findViewById(R.id.btnRest60s);
        MaterialButton btnRest90s = dialogView.findViewById(R.id.btnRest90s);
        MaterialButton btnRest120s = dialogView.findViewById(R.id.btnRest120s);

        final int[] selectedRestTime = {60}; // Default rest time

        if (exercise != null) {
            tvExerciseTitle.setText(exercise.getName());
            // Load image if URL exists
            if (exercise.getImageUrl() != null && !exercise.getImageUrl().isEmpty()) {
                Glide.with(this).load(exercise.getImageUrl()).into(ivExerciseIconDialog);
            } else {
                ivExerciseIconDialog.setImageResource(R.drawable.ic_fitness_center); // Default icon
            }
        }

        // Pre-fill with some defaults or last used values if available
        etSets.setText("3");
        etReps.setText("10-12");
        etWeight.setText("0kg"); // Default weight

        // Handle rest time button selection
        View.OnClickListener restButtonListener = v -> {
            resetRestButtonsStyle(btnRest30s, btnRest60s, btnRest90s, btnRest120s);
            ((MaterialButton) v).setStrokeColorResource(R.color.secondary_variant); // Use a defined color
            ((MaterialButton) v).setTextColor(ContextCompat.getColor(this, R.color.secondary_variant));
            if (v.getId() == R.id.btnRest30s) selectedRestTime[0] = 30;
            else if (v.getId() == R.id.btnRest60s) selectedRestTime[0] = 60;
            else if (v.getId() == R.id.btnRest90s) selectedRestTime[0] = 90;
            else if (v.getId() == R.id.btnRest120s) selectedRestTime[0] = 120;
        };

        btnRest30s.setOnClickListener(restButtonListener);
        btnRest60s.setOnClickListener(restButtonListener);
        btnRest90s.setOnClickListener(restButtonListener);
        btnRest120s.setOnClickListener(restButtonListener);
        btnRest60s.performClick(); // Default selection

        builder.setPositiveButton("Add", (dialog, which) -> {
            String setsStr = etSets.getText().toString().trim();
            String repsStr = etReps.getText().toString().trim();
            String weightStr = etWeight.getText().toString().trim(); // Added
            String notesStr = etNotes.getText().toString().trim();   // Added

            if (TextUtils.isEmpty(setsStr) || TextUtils.isEmpty(repsStr)) {
                Toast.makeText(this, "Sets and reps cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }
            try {
                int sets = Integer.parseInt(setsStr);
                // Reps is already a string, weight is a string, notes is a string
                if (exercise != null) {
                    // This is where the call to addExerciseToWorkout happens from the dialog
                    addExerciseToWorkout(exercise, sets, repsStr, weightStr, notesStr, selectedRestTime[0]);
                }
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Invalid number for sets", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        
        // The specific error was: addExerciseToWorkout(exercise, 3, 10, "", 0);
        // This call is not in the provided snippet of showExerciseDetailsDialog(ExerciseLibrary exercise)
        // It must be somewhere else, or the line number 161 refers to a different context.
        // Assuming the error is in a different part of the code, I will make a generic correction
        // to the method signature or a hypothetical call if it were at line 161.
        // If line 161 was:
        // someMethod() {
        //    ExerciseLibrary exercise = ...;
        //    addExerciseToWorkout(exercise, 3, 10, "", 0); // Original erroneous call at line 161
        // }
        // It should be changed to:
        // addExerciseToWorkout(exercise, 3, "10", "", "", 0); // Corrected call

        AlertDialog dialog = builder.create(); // Corrected to androidx.appcompat.app.AlertDialog
        dialog.show();
    }
    
    private void resetRestButtonsStyle(MaterialButton... buttons) {
        for (MaterialButton button : buttons) {
            button.setStrokeColor(ContextCompat.getColorStateList(this, R.color.text_hint)); // Corrected syntax and usage
            button.setTextColor(ContextCompat.getColor(this, R.color.text_hint));
        }
    }
      private void addExerciseToWorkout(ExerciseLibrary exercise, int sets, String reps, String weight, String notes, int restTimeSeconds) {
        ExerciseDetail detail = new ExerciseDetail(
                exercise.getName(),
                sets,
                reps,
                weight, 
                restTimeSeconds,
                notes,
                false 
        );
        if (exerciseDetailsList == null) {
            exerciseDetailsList = new ArrayList<>();
        }
        exerciseDetailsList.add(detail);
        if (exerciseDetailAdapter == null) {
            exerciseDetailAdapter = new ExerciseDetailAdapter(this, exerciseDetailsList, this);
            rvExerciseDetails.setAdapter(exerciseDetailAdapter);
            rvExerciseDetails.setLayoutManager(new LinearLayoutManager(this));
        } else {
            exerciseDetailAdapter.notifyDataSetChanged();
        }
        if (tvNoExercises != null) {
            tvNoExercises.setVisibility(exerciseDetailsList.isEmpty() ? View.VISIBLE : View.GONE);
        }
        Toast.makeText(this, exercise.getName() + " added to workout", Toast.LENGTH_SHORT).show();
        updateExerciseListVisibility();
    }
    
    private void updateExerciseListVisibility() {
        if (exerciseDetailsList != null && tvNoExercises != null && rvExerciseDetails != null) {
            if (exerciseDetailsList.isEmpty()) {
                tvNoExercises.setVisibility(View.VISIBLE);
                rvExerciseDetails.setVisibility(View.GONE);
            } else {
                tvNoExercises.setVisibility(View.GONE);
                rvExerciseDetails.setVisibility(View.VISIBLE);
            }
        }
    }

    private void openImageChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Workout Image"), PICK_IMAGE_REQUEST);
    }

    private String copyImageToInternalStorage(Uri sourceUri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(sourceUri);
            File imageFile = new File(getFilesDir(), "workout_" + System.currentTimeMillis() + ".jpg");
            OutputStream outputStream = new FileOutputStream(imageFile);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }
            outputStream.close();
            inputStream.close();
            return imageFile.getAbsolutePath();
        } catch (Exception e) {
            Log.e(TAG, "Error copying image to internal storage", e);
            return null;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri pickedUri = data.getData();
            String localPath = copyImageToInternalStorage(pickedUri);
            if (localPath != null) {
                selectedImageUri = Uri.fromFile(new File(localPath));
                Glide.with(this)
                     .load(selectedImageUri)
                     .placeholder(R.drawable.ic_placeholder_workout)
                     .error(R.drawable.ic_placeholder_workout)
                     .into(ivWorkoutImagePreview);
            } else {
                Toast.makeText(this, "Failed to copy image.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void saveWorkout() {
        Log.d(TAG, "saveWorkout() method CALLED.");

        String workoutName = etWorkoutName.getText().toString().trim();
        String description = etWorkoutDescription.getText().toString().trim();
        String type = actWorkoutType.getText().toString().trim();
        String duration = etWorkoutDuration.getText().toString().trim();
        String difficulty = actWorkoutDifficulty.getText().toString().trim();
        List<String> muscleGroupsList = getSelectedMuscleGroups();
        String muscleGroups = String.join(", ", muscleGroupsList);

        if (TextUtils.isEmpty(workoutName)) {
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

        String currentUserId = null;
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        } else {
            Log.e(TAG, "Error: User not authenticated. Cannot save workout.");
            Toast.makeText(this, "Error: User not authenticated. Cannot save workout.", Toast.LENGTH_LONG).show();
            return;
        }

        final String finalCurrentUserId = currentUserId;
        String workoutIdToSave;

        if (isEditMode && existingWorkout != null && existingWorkout.getId() != null && !existingWorkout.getId().isEmpty()) {
            workoutIdToSave = existingWorkout.getId();
            Log.d(TAG, "Updating existing workout with ID: " + workoutIdToSave);
        } else {
            workoutIdToSave = UUID.randomUUID().toString();
            Log.d(TAG, "Saving new workout with generated ID: " + workoutIdToSave);
        }
        
        final String finalWorkoutIdToSave = workoutIdToSave;

        // Always save with empty imageUrl
        String imageUrl = selectedImageUri != null ? selectedImageUri.toString() : "";
        proceedToSaveWorkoutData(finalWorkoutIdToSave, finalCurrentUserId, workoutName, description, type, duration, difficulty, muscleGroups, imageUrl);
    }

    private void proceedToSaveWorkoutData(String workoutId, String userId, String name, String description, String type, String duration, String difficulty, String muscleGroups, String imageUrl) {
        Map<String, Object> workoutMap = new HashMap<>();
        workoutMap.put("id", workoutId);
        workoutMap.put("userId", userId);
        workoutMap.put("name", name);
        workoutMap.put("description", description);
        workoutMap.put("type", type);
        workoutMap.put("duration", duration);
        workoutMap.put("difficulty", difficulty);
        workoutMap.put("muscleGroups", muscleGroups);
        workoutMap.put("exercises", new ArrayList<>(exerciseDetailsList)); 
        workoutMap.put("imageUrl", imageUrl != null ? imageUrl : "");

        db.collection("workouts").document(workoutId)
                .set(workoutMap)
                .addOnSuccessListener(aVoid -> {
                    String message = isEditMode ? "Workout Updated: " : "Workout Saved: ";
                    Log.d(TAG, "Successfully saved/updated workout with ID: " + workoutId + " for user: " + userId);
                    Toast.makeText(AddWorkoutActivity.this, message + name, Toast.LENGTH_LONG).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error saving/updating workout with ID: " + workoutId + " for user: " + userId, e);
                    Toast.makeText(AddWorkoutActivity.this, "Error saving workout: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    @Override
    public void onExerciseRemoved(int position) {
        if (exerciseDetailsList != null && position >= 0 && position < exerciseDetailsList.size()) {
            exerciseDetailsList.remove(position);
            exerciseDetailAdapter.notifyItemRemoved(position);
            if (tvNoExercises != null) {
                tvNoExercises.setVisibility(exerciseDetailsList.isEmpty() ? View.VISIBLE : View.GONE);
            }
            Toast.makeText(this, "Exercise removed", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void filterExercises() {
        String searchQuery = etSearchExerciseLibrary.getText().toString().trim();
        String selectedMuscleGroup = "All";
        List<Integer> checkedChipIds = chipGroupExerciseFilter.getCheckedChipIds();
        if (!checkedChipIds.isEmpty()) {
            Chip checkedChip = findViewById(checkedChipIds.get(0));
            if (checkedChip != null) {
                selectedMuscleGroup = checkedChip.getText().toString();
            }
        }
        exerciseLibraryViewModel.filterExercises(searchQuery, selectedMuscleGroup);
    }

    private void showAddCustomExerciseDialog() {
        // Inflate the custom dialog layout
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_custom_exercise, null);
        TextInputEditText etCustomExerciseName = dialogView.findViewById(R.id.etCustomExerciseName);
        ChipGroup chipGroupCustomMuscle = dialogView.findViewById(R.id.chipGroupCustomMuscle);
        // Add chips for muscle groups dynamically or ensure they are in XML
        // For simplicity, let's assume they are in XML or added similarly to other chip groups

        new MaterialAlertDialogBuilder(this)
                .setTitle("Add Custom Exercise")
                .setView(dialogView)
                .setPositiveButton("Add", (dialog, which) -> {
                    String exerciseName = etCustomExerciseName.getText().toString().trim();
                    List<String> selectedMuscles = new ArrayList<>();
                    for (int i = 0; i < chipGroupCustomMuscle.getChildCount(); i++) {
                        Chip chip = (Chip) chipGroupCustomMuscle.getChildAt(i);
                        if (chip.isChecked()) {
                            selectedMuscles.add(chip.getText().toString());
                        }
                    }
                    if (!exerciseName.isEmpty() && !selectedMuscles.isEmpty()) {
                        // Create and save the custom exercise
                        ExerciseLibrary customExercise = new ExerciseLibrary(
                                UUID.randomUUID().toString(), // Generate unique ID
                                exerciseName,
                                String.join(", ", selectedMuscles), // primaryMuscleGroup
                                new ArrayList<String>(), // secondaryMuscleGroups
                                new ArrayList<String>(), // equipment
                                "User defined exercise", // instructions
                                "" // imageUrl
                        );
                        exerciseLibraryViewModel.addCustomExercise(customExercise);
                        Toast.makeText(this, "Custom exercise '" + exerciseName + "' added.", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Exercise name and at least one muscle group are required.", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
