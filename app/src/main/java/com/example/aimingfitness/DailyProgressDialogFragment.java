package com.example.aimingfitness;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth; // Added import
import com.google.firebase.auth.FirebaseUser; // Added import
import com.google.firebase.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

public class DailyProgressDialogFragment extends DialogFragment {

    private static final String TAG = "DailyProgressDialog";
    private static final String ARG_PROGRESS_ENTRY = "daily_progress_entry";

    // Header section
    private TextInputLayout tilProgressDate;
    private TextInputEditText etProgressDate;
    
    // Meals section
    private TextInputLayout tilCalories;
    private TextInputEditText etCalories;
    
    // Macros section
    private TextInputLayout tilCarbs, tilProtein, tilFat;
    private TextInputEditText etCarbs, etProtein, etFat;
    
    // Steps section
    private TextInputLayout tilSteps, tilStepsGoal;
    private TextInputEditText etSteps, etStepsGoal;
    
    // Exercise section
    private TextInputLayout tilExerciseDuration, tilExerciseCalories, tilExerciseName;
    private TextInputEditText etExerciseDuration, etExerciseCalories, etExerciseName;
    
    // Notes section
    private TextInputLayout tilNotes;
    private TextInputEditText etNotes;
    
    // Buttons
    private MaterialButton btnSaveProgress, btnCancelProgress;

    // ViewModel instance
    private DailyProgressViewModel dailyProgressViewModel;
    private DailyProgressEntry currentEntry;
    private boolean isEditMode = false;
    private FirebaseAuth mAuth; // Added FirebaseAuth instance

    private Calendar selectedDateCalendar = Calendar.getInstance();
    private SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());

    public static DailyProgressDialogFragment newInstance() {
        return new DailyProgressDialogFragment();
    }

    public static DailyProgressDialogFragment newInstance(DailyProgressEntry entry) {
        DailyProgressDialogFragment fragment = new DailyProgressDialogFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_PROGRESS_ENTRY, entry);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dailyProgressViewModel = new ViewModelProvider(requireActivity()).get(DailyProgressViewModel.class);
        mAuth = FirebaseAuth.getInstance(); // Initialize FirebaseAuth
        
        if (getArguments() != null && getArguments().containsKey(ARG_PROGRESS_ENTRY)) {
            currentEntry = (DailyProgressEntry) getArguments().getSerializable(ARG_PROGRESS_ENTRY);
            isEditMode = (currentEntry != null);
            Log.d(TAG, "onCreate: isEditMode = " + isEditMode);
            if (currentEntry != null) {
                Log.d(TAG, "onCreate: currentEntry ID = " + currentEntry.getId() + ", Date = " + (currentEntry.getDate() != null ? dateFormat.format(currentEntry.getDate()) : "null") + ", Calories = " + currentEntry.getCalories());
            } else {
                Log.d(TAG, "onCreate: currentEntry is null");
            }
        } else {
            Log.d(TAG, "onCreate: No arguments or no ARG_PROGRESS_ENTRY found.");
            isEditMode = false; // Ensure isEditMode is false if no args
        }
        
        // Use a larger dialog style
        setStyle(DialogFragment.STYLE_NORMAL, R.style.FullScreenDialog);
    }

    @Nullable    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Use the MyFitnessPal style layout
        View view = inflater.inflate(R.layout.dialog_add_progress_redesigned, container, false);
        initViews(view);
        setupDateEditText();
        setupActionButtons();
        observeViewModel();
        
        // Populate with existing data if in edit mode
        if (isEditMode && currentEntry != null) {
            if (getDialog() != null && getDialog().getWindow() != null) {
                getDialog().setTitle("Edit Progress");
            }
            populateFieldsForEdit();
        }
        
        return view;
    }
    
    private void initViews(View view) {
        // Initialize Header section
        tilProgressDate = view.findViewById(R.id.tilProgressDate);
        etProgressDate = view.findViewById(R.id.etProgressDate);
        
        // Initialize Meals section
        tilCalories = view.findViewById(R.id.tilCalories);
        etCalories = view.findViewById(R.id.etCalories);
        
        // Initialize Macros section
        tilCarbs = view.findViewById(R.id.tilCarbs);
        tilProtein = view.findViewById(R.id.tilProtein);
        tilFat = view.findViewById(R.id.tilFat);
        etCarbs = view.findViewById(R.id.etCarbs);
        etProtein = view.findViewById(R.id.etProtein);
        etFat = view.findViewById(R.id.etFat);
        
        // Initialize Steps section
        tilSteps = view.findViewById(R.id.tilSteps);
        tilStepsGoal = view.findViewById(R.id.tilStepsGoal);
        etSteps = view.findViewById(R.id.etSteps);
        etStepsGoal = view.findViewById(R.id.etStepsGoal);
        
        // Initialize Exercise section
        tilExerciseDuration = view.findViewById(R.id.tilExerciseDuration);
        tilExerciseCalories = view.findViewById(R.id.tilExerciseCalories);
        tilExerciseName = view.findViewById(R.id.tilExerciseNameNew);
        etExerciseDuration = view.findViewById(R.id.etExerciseDuration);
        etExerciseCalories = view.findViewById(R.id.etExerciseCalories);
        etExerciseName = view.findViewById(R.id.etExerciseNameNew);
        
        // Initialize Notes section
        tilNotes = view.findViewById(R.id.tilNotesNew);
        etNotes = view.findViewById(R.id.etNotesNew);
        
        // Initialize Buttons
        btnSaveProgress = view.findViewById(R.id.btnSaveProgressNew);
        btnCancelProgress = view.findViewById(R.id.btnCancelProgressNew);
    }
    
    private void setupDateEditText() {
        // Set current date as default
        etProgressDate.setText(dateFormat.format(selectedDateCalendar.getTime()));
        
        // Show date picker on click
        etProgressDate.setOnClickListener(v -> {
            DatePickerDialog datePickerDialog = new DatePickerDialog(
                requireContext(),
                (view, year, month, dayOfMonth) -> {
                    selectedDateCalendar.set(Calendar.YEAR, year);
                    selectedDateCalendar.set(Calendar.MONTH, month);
                    selectedDateCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    etProgressDate.setText(dateFormat.format(selectedDateCalendar.getTime()));
                },
                selectedDateCalendar.get(Calendar.YEAR),
                selectedDateCalendar.get(Calendar.MONTH),
                selectedDateCalendar.get(Calendar.DAY_OF_MONTH)
            );
            datePickerDialog.show();
        });
    }
    
    private void setupActionButtons() {
        btnSaveProgress.setOnClickListener(v -> {
            if (validateInputs()) {
                saveProgressEntry();
            }
        });
        
        btnCancelProgress.setOnClickListener(v -> dismiss());
    }
    
    private void observeViewModel() {
        dailyProgressViewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            // Toggle loading state (you might add a progress indicator)
            btnSaveProgress.setEnabled(!isLoading);
        });
        
        dailyProgressViewModel.getErrorMessage().observe(getViewLifecycleOwner(), errorMsg -> {
            if (errorMsg != null && !errorMsg.isEmpty()) {
                Toast.makeText(requireContext(), errorMsg, Toast.LENGTH_LONG).show();
            }
        });
        
        dailyProgressViewModel.getOperationSuccessful().observe(getViewLifecycleOwner(), isSuccessful -> {
            if (isSuccessful) {
                dismiss();
            }
        });
    }
    
    private boolean validateInputs() {
        boolean isValid = true;
        
        // Validate date (required)
        if (TextUtils.isEmpty(etProgressDate.getText())) {
            tilProgressDate.setError("Date is required");
            isValid = false;
        } else {
            tilProgressDate.setError(null);
        }
        
        // Calories (optional, but if provided must be valid)
        if (!TextUtils.isEmpty(etCalories.getText())) {
            try {
                Integer.parseInt(etCalories.getText().toString());
                tilCalories.setError(null);
            } catch (NumberFormatException e) {
                tilCalories.setError("Must be a valid number");
                isValid = false;
            }
        }
        
        // Validate macros (optional, but if provided must be valid)
        if (!TextUtils.isEmpty(etCarbs.getText())) {
            try {
                Double.parseDouble(etCarbs.getText().toString());
                tilCarbs.setError(null);
            } catch (NumberFormatException e) {
                tilCarbs.setError("Must be a valid number");
                isValid = false;
            }
        }
        
        if (!TextUtils.isEmpty(etProtein.getText())) {
            try {
                Double.parseDouble(etProtein.getText().toString());
                tilProtein.setError(null);
            } catch (NumberFormatException e) {
                tilProtein.setError("Must be a valid number");
                isValid = false;
            }
        }
        
        if (!TextUtils.isEmpty(etFat.getText())) {
            try {
                Double.parseDouble(etFat.getText().toString());
                tilFat.setError(null);
            } catch (NumberFormatException e) {
                tilFat.setError("Must be a valid number");
                isValid = false;
            }
        }
        
        // Validate steps (optional, but if provided must be valid)
        if (!TextUtils.isEmpty(etSteps.getText())) {
            try {
                Integer.parseInt(etSteps.getText().toString());
                tilSteps.setError(null);
            } catch (NumberFormatException e) {
                tilSteps.setError("Must be a valid number");
                isValid = false;
            }
        }
        
        if (!TextUtils.isEmpty(etStepsGoal.getText())) {
            try {
                Integer.parseInt(etStepsGoal.getText().toString());
                tilStepsGoal.setError(null);
            } catch (NumberFormatException e) {
                tilStepsGoal.setError("Must be a valid number");
                isValid = false;
            }
        }
        
        // Validate exercise calories (optional, but if provided must be valid)
        if (!TextUtils.isEmpty(etExerciseCalories.getText())) {
            try {
                Integer.parseInt(etExerciseCalories.getText().toString());
                tilExerciseCalories.setError(null);
            } catch (NumberFormatException e) {
                tilExerciseCalories.setError("Must be a valid number");
                isValid = false;
            }
        }
        
        return isValid;
    }
    
    private void saveProgressEntry() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(getContext(), "You must be logged in to save progress.", Toast.LENGTH_SHORT).show();
            Log.w(TAG, "User not authenticated. Cannot save progress entry.");
            return;
        }
        String userId = currentUser.getUid();

        DailyProgressEntry entry = new DailyProgressEntry();
        entry.setUserId(userId); // Set userId
        
        // Set ID if editing
        if (isEditMode && currentEntry != null && currentEntry.getId() != null) {
            entry.setId(currentEntry.getId());
            // Ensure userId from currentEntry is preserved if it was already set, though ideally it should match currentUser.getUid()
            if (currentEntry.getUserId() != null && !currentEntry.getUserId().equals(userId)) {
                Log.w(TAG, "Warning: Editing entry with a different userId than current user. Overriding with current user's ID.");
                // Policy: override with current user's ID
            }
        }
        
        // Set date
        entry.setDate(selectedDateCalendar.getTime());
        
        // Set meals data
        if (!TextUtils.isEmpty(etCalories.getText())) {
            entry.setCalories(Integer.parseInt(etCalories.getText().toString()));
        }
        
        // Set macros data
        if (!TextUtils.isEmpty(etCarbs.getText())) {
            entry.setCarbs(Double.parseDouble(etCarbs.getText().toString()));
        }
        
        if (!TextUtils.isEmpty(etProtein.getText())) {
            entry.setProtein(Double.parseDouble(etProtein.getText().toString()));
        }
        
        if (!TextUtils.isEmpty(etFat.getText())) {
            entry.setFat(Double.parseDouble(etFat.getText().toString()));
        }
        
        // Set steps data
        if (!TextUtils.isEmpty(etSteps.getText())) {
            entry.setSteps(Integer.parseInt(etSteps.getText().toString()));
        }
        
        if (!TextUtils.isEmpty(etStepsGoal.getText())) {
            entry.setStepsGoal(Integer.parseInt(etStepsGoal.getText().toString()));
        }
        
        // Set exercise data
        if (!TextUtils.isEmpty(etExerciseDuration.getText())) {
            entry.setExerciseDuration(etExerciseDuration.getText().toString());
        }
        
        if (!TextUtils.isEmpty(etExerciseCalories.getText())) {
            entry.setExerciseCaloriesBurned(Integer.parseInt(etExerciseCalories.getText().toString()));
        }
        
        if (!TextUtils.isEmpty(etExerciseName.getText())) {
            entry.setExerciseName(etExerciseName.getText().toString());
        }
        
        // Set notes
        if (!TextUtils.isEmpty(etNotes.getText())) {
            entry.setNotes(etNotes.getText().toString());
        }
        
        // Save or update
        if (isEditMode) {
            Log.d(TAG, "saveProgressEntry: Attempting to update entry with ID = " + entry.getId() + ", Date = " + dateFormat.format(entry.getDate()) + ", Calories = " + entry.getCalories());
            dailyProgressViewModel.updateDailyProgressEntry(entry);
        } else {
            Log.d(TAG, "saveProgressEntry: Attempting to save new entry with Date = " + dateFormat.format(entry.getDate()) + ", Calories = " + entry.getCalories());
            dailyProgressViewModel.saveDailyProgressEntry(entry);
        }
    }
    
    private void populateFieldsForEdit() {
        if (currentEntry == null) {
            Log.d(TAG, "populateFieldsForEdit: currentEntry is null, cannot populate.");
            return;
        }
        Log.d(TAG, "populateFieldsForEdit: Populating fields for entry ID = " + currentEntry.getId() + ", Date = " + (currentEntry.getDate() != null ? dateFormat.format(currentEntry.getDate()) : "null") + ", Calories = " + currentEntry.getCalories());
        
        // Set date
        if (currentEntry.getDate() != null) {
            selectedDateCalendar.setTime(currentEntry.getDate());
            etProgressDate.setText(dateFormat.format(currentEntry.getDate()));
            Log.d(TAG, "populateFieldsForEdit: Set date to " + etProgressDate.getText().toString());
        } else {
            Log.d(TAG, "populateFieldsForEdit: currentEntry.getDate() is null");
            etProgressDate.setText(dateFormat.format(Calendar.getInstance().getTime())); // Default to current date if null
        }
        
        // Set meals data
        if (currentEntry.getCalories() != null) {
            etCalories.setText(String.valueOf(currentEntry.getCalories()));
            Log.d(TAG, "populateFieldsForEdit: Set calories to " + etCalories.getText().toString());
        } else {
            etCalories.setText(""); // Clear field if null
            Log.d(TAG, "populateFieldsForEdit: currentEntry.getCalories() is null");
        }
        
        // Set macros data
        if (currentEntry.getCarbs() != null) {
            etCarbs.setText(String.valueOf(currentEntry.getCarbs()));
            Log.d(TAG, "populateFieldsForEdit: Set carbs to " + etCarbs.getText().toString());
        } else {
            etCarbs.setText(""); // Clear field if null
            Log.d(TAG, "populateFieldsForEdit: currentEntry.getCarbs() is null");
        }
        
        if (currentEntry.getProtein() != null) {
            etProtein.setText(String.valueOf(currentEntry.getProtein()));
            Log.d(TAG, "populateFieldsForEdit: Set protein to " + etProtein.getText().toString());
        } else {
            etProtein.setText(""); // Clear field if null
            Log.d(TAG, "populateFieldsForEdit: currentEntry.getProtein() is null");
        }
        
        if (currentEntry.getFat() != null) {
            etFat.setText(String.valueOf(currentEntry.getFat()));
            Log.d(TAG, "populateFieldsForEdit: Set fat to " + etFat.getText().toString());
        } else {
            etFat.setText(""); // Clear field if null
            Log.d(TAG, "populateFieldsForEdit: currentEntry.getFat() is null");
        }
        
        // Set steps data
        if (currentEntry.getSteps() != null) {
            etSteps.setText(String.valueOf(currentEntry.getSteps()));
            Log.d(TAG, "populateFieldsForEdit: Set steps to " + etSteps.getText().toString());
        } else {
            etSteps.setText(""); // Clear field if null
            Log.d(TAG, "populateFieldsForEdit: currentEntry.getSteps() is null");
        }
        
        if (currentEntry.getStepsGoal() != null) {
            etStepsGoal.setText(String.valueOf(currentEntry.getStepsGoal()));
            Log.d(TAG, "populateFieldsForEdit: Set stepsGoal to " + etStepsGoal.getText().toString());
        } else {
            etStepsGoal.setText(""); // Clear field if null
            Log.d(TAG, "populateFieldsForEdit: currentEntry.getStepsGoal() is null");
        }
        
        // Set exercise data
        if (currentEntry.getExerciseDuration() != null) {
            etExerciseDuration.setText(currentEntry.getExerciseDuration());
            Log.d(TAG, "populateFieldsForEdit: Set exerciseDuration to " + etExerciseDuration.getText().toString());
        } else {
            etExerciseDuration.setText(""); // Clear field if null
            Log.d(TAG, "populateFieldsForEdit: currentEntry.getExerciseDuration() is null");
        }
        
        if (currentEntry.getExerciseCaloriesBurned() != null) {
            etExerciseCalories.setText(String.valueOf(currentEntry.getExerciseCaloriesBurned()));
            Log.d(TAG, "populateFieldsForEdit: Set exerciseCaloriesBurned to " + etExerciseCalories.getText().toString());
        } else {
            etExerciseCalories.setText(""); // Clear field if null
            Log.d(TAG, "populateFieldsForEdit: currentEntry.getExerciseCaloriesBurned() is null");
        }
        
        if (currentEntry.getExerciseName() != null) {
            etExerciseName.setText(currentEntry.getExerciseName());
            Log.d(TAG, "populateFieldsForEdit: Set exerciseName to " + etExerciseName.getText().toString());
        } else {
            etExerciseName.setText(""); // Clear field if null
            Log.d(TAG, "populateFieldsForEdit: currentEntry.getExerciseName() is null");
        }
        
        // Set notes
        if (currentEntry.getNotes() != null) {
            etNotes.setText(currentEntry.getNotes());
            Log.d(TAG, "populateFieldsForEdit: Set notes to " + etNotes.getText().toString());
        } else {
            etNotes.setText(""); // Clear field if null
            Log.d(TAG, "populateFieldsForEdit: currentEntry.getNotes() is null");
        }
        Log.d(TAG, "populateFieldsForEdit: Finished populating fields.");
    }
}
