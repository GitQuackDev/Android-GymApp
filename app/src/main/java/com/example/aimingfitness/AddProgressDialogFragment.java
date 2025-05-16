package com.example.aimingfitness;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth; // Added import

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

public class AddProgressDialogFragment extends DialogFragment {

    private static final String TAG = "AddProgressDialog";
    private static final String ARG_PROGRESS_ENTRY = "progress_entry"; 

    // Simplified UI elements based on dialog_add_progress_redesigned.xml
    private TextInputLayout tilProgressDate, tilNotes; // Assuming tilNotesNew is R.id.tilNotes
    private TextInputEditText etProgressDate, etNotes; // Assuming etNotesNew is R.id.etNotes
    private MaterialButton btnSaveProgress, btnCancelProgress; // Assuming btnSaveProgressNew and btnCancelProgressNew
    private ProgressBar pbSavingProgress;

    // ViewModel instance
    private ProgressViewModel progressViewModel;
    private ProgressEntry currentProgressEntry; 
    private boolean isEditMode = false;

    private Calendar selectedDateCalendar = Calendar.getInstance();
    private SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
    // Removed: private SimpleDateFormat storedDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    public static AddProgressDialogFragment newInstance() {
        return new AddProgressDialogFragment();
    }

    public static AddProgressDialogFragment newInstance(ProgressEntry progressEntry) {
        AddProgressDialogFragment fragment = new AddProgressDialogFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_PROGRESS_ENTRY, progressEntry);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        progressViewModel = new ViewModelProvider(requireActivity()).get(ProgressViewModel.class);
        if (getArguments() != null) {
            currentProgressEntry = (ProgressEntry) getArguments().getSerializable(ARG_PROGRESS_ENTRY);
            if (currentProgressEntry != null) {
                isEditMode = true;
                selectedDateCalendar.setTime(currentProgressEntry.getDate().toDate());
            }
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the redesigned layout
        View view = inflater.inflate(R.layout.dialog_add_progress_redesigned, container, false);
        initViews(view);
        setupDateEditText();
        setupActionButtons(); // Renamed from setupSaveButton
        observeViewModel();   // Renamed from setupViewModelObservers

        if (isEditMode && currentProgressEntry != null) {
            populateFieldsForEdit();
        } else {
            // Set default date for new entries
            etProgressDate.setText(dateFormat.format(selectedDateCalendar.getTime()));
        }
        return view;
    }

    private void initViews(View view) {
        tilProgressDate = view.findViewById(R.id.tilProgressDate); // Assuming this ID exists in redesigned
        etProgressDate = view.findViewById(R.id.etProgressDate);   // Assuming this ID exists in redesigned
        
        // Using IDs assumed for the redesigned layout based on summary (e.g. R.id.tilNotesNew)
        // If your actual IDs are different (e.g. R.id.tilNotes), please adjust.
        tilNotes = view.findViewById(R.id.tilNotesNew); 
        etNotes = view.findViewById(R.id.etNotesNew);
        
        btnSaveProgress = view.findViewById(R.id.btnSaveProgressNew);
        btnCancelProgress = view.findViewById(R.id.btnCancelProgressNew);
        pbSavingProgress = view.findViewById(R.id.pbSavingProgress); // Assuming this ID exists

        // Hide progress bar initially
        if (pbSavingProgress != null) {
            pbSavingProgress.setVisibility(View.GONE);
        }
    }

    private void populateFieldsForEdit() {
        if (currentProgressEntry != null) {
            etProgressDate.setText(dateFormat.format(currentProgressEntry.getDate().toDate()));
            if (currentProgressEntry.getNotes() != null) {
                etNotes.setText(currentProgressEntry.getNotes());
            }
        }
    }

    private void setupDateEditText() {
        if (etProgressDate == null) return;
        etProgressDate.setText(dateFormat.format(selectedDateCalendar.getTime()));
        etProgressDate.setOnClickListener(v -> showDatePickerDialog());
        if (tilProgressDate != null && tilProgressDate.getEndIconDrawable() != null) { // Check if end icon exists
            tilProgressDate.setEndIconOnClickListener(v -> showDatePickerDialog());
        }
    }

    private void showDatePickerDialog() {
        new DatePickerDialog(requireContext(), (view, year, month, dayOfMonth) -> {
            selectedDateCalendar.set(Calendar.YEAR, year);
            selectedDateCalendar.set(Calendar.MONTH, month);
            selectedDateCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            if (etProgressDate != null) {
                etProgressDate.setText(dateFormat.format(selectedDateCalendar.getTime()));
            }
        }, selectedDateCalendar.get(Calendar.YEAR), selectedDateCalendar.get(Calendar.MONTH), selectedDateCalendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    // Renamed from setupSaveButton to setupActionButtons
    private void setupActionButtons() {
        btnSaveProgress.setOnClickListener(v -> saveProgressEntry());
        btnCancelProgress.setOnClickListener(v -> dismiss());
    }

    // Renamed from setupViewModelObservers to observeViewModel
    private void observeViewModel() {
        progressViewModel.getProgressSaveSuccess().observe(getViewLifecycleOwner(), success -> {
            if (success != null && success) {
                pbSavingProgress.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Progress saved", Toast.LENGTH_SHORT).show();
                // Removed: if (listener != null) listener.onProgressSaved();
                progressViewModel.resetProgressSaveSuccessEvent(); // Reset event
                dismiss();
            }
        });

        progressViewModel.getProgressUpdateSuccess().observe(getViewLifecycleOwner(), success -> {
            if (success != null && success) {
                pbSavingProgress.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Progress updated", Toast.LENGTH_SHORT).show();
                progressViewModel.resetProgressUpdateSuccessEvent(); // Reset event
                dismiss();
            }
        });

        progressViewModel.getError().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                pbSavingProgress.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Error: " + error, Toast.LENGTH_LONG).show();
                progressViewModel.resetErrorEvent(); // Reset event
            }
        });
    }
    
    private void saveProgressEntry() {
        String notes = etNotes.getText() != null ? etNotes.getText().toString().trim() : "";
        Date selectedDate = selectedDateCalendar.getTime();

        // Basic validation (e.g., date must be present)
        if (etProgressDate.getText() == null || TextUtils.isEmpty(etProgressDate.getText().toString())) {
            tilProgressDate.setError("Date is required.");
            return;
        } else {
            tilProgressDate.setError(null); // Clear error
        }

        pbSavingProgress.setVisibility(View.VISIBLE);

        if (isEditMode && currentProgressEntry != null) {
            currentProgressEntry.setDate(new Timestamp(selectedDate));
            currentProgressEntry.setNotes(notes);
            // Type is not changed in edit mode with this simplified dialog
            // userId is already set on currentProgressEntry and shouldn't be changed here
            progressViewModel.updateProgressEntry(currentProgressEntry);
        } else {
            ProgressEntry newEntry = new ProgressEntry();
            newEntry.setDate(new Timestamp(selectedDate));
            newEntry.setNotes(notes);
            newEntry.setType("Undefined"); // Set type to "Undefined" for new entries

            // Set the userId for the new entry
            if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                newEntry.setUserId(FirebaseAuth.getInstance().getCurrentUser().getUid());
            } else {
                Log.e(TAG, "User not authenticated, cannot save progress entry.");
                pbSavingProgress.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Error: You must be signed in to save progress.", Toast.LENGTH_LONG).show();
                return; // Stop execution if user is not signed in
            }

            Log.w(TAG, "Saving new progress entry with type 'Undefined' due to simplified dialog.");
            progressViewModel.saveProgressEntry(newEntry);
        }
    }

    // Removed methods:
    // setupProgressTypeSpinner()
    // setupUnitSpinners()
    // updateDynamicFieldVisibility()
    // And any other methods related to the old complex UI.

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }
}
