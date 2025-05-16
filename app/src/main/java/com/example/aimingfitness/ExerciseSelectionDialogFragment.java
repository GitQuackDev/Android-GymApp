package com.example.aimingfitness;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

public class ExerciseSelectionDialogFragment extends DialogFragment implements ExerciseSelectionAdapter.OnExerciseSelectedListener {

    private RecyclerView rvExerciseSelection;
    private TextInputEditText etSearchExercise;
    private ChipGroup chipGroupExerciseFilter;
    private TextView tvNoExercisesFound;
    private MaterialButton btnAddCustomExercise, btnConfirmExerciseSelection;
    private ImageButton btnCloseExerciseSelection;
    
    private ExerciseSelectionAdapter exerciseAdapter;
    private List<ExerciseLibrary> exercisesList;
    private List<ExerciseLibrary> selectedExercises = new ArrayList<>();
    
    private OnExerciseSelectedListener listener;

    public interface OnExerciseSelectedListener {
        void onExerciseSelected(ExerciseLibrary exercise);
        void onCustomExerciseRequested();
    }

    public void setOnExerciseSelectedListener(OnExerciseSelectedListener listener) {
        this.listener = listener;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.Theme_AimingFitness_FullScreenDialog);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_exercise_selection, container, false);

        // Initialize views
        rvExerciseSelection = view.findViewById(R.id.rvExerciseSelection);
        etSearchExercise = view.findViewById(R.id.etSearchExercise);
        chipGroupExerciseFilter = view.findViewById(R.id.chipGroupExerciseFilter);
        tvNoExercisesFound = view.findViewById(R.id.tvNoExercisesFound);
        btnAddCustomExercise = view.findViewById(R.id.btnAddCustomExercise);
        btnConfirmExerciseSelection = view.findViewById(R.id.btnConfirmExerciseSelection);
        btnCloseExerciseSelection = view.findViewById(R.id.btnCloseExerciseSelection);

        // Use the sample exercises for the list
        exercisesList = new ArrayList<>(ExerciseLibrary.getSampleExercises());
        exerciseAdapter = new ExerciseSelectionAdapter(requireContext(), exercisesList, this);
        rvExerciseSelection.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvExerciseSelection.setAdapter(exerciseAdapter);

        // Set up search functionality
        etSearchExercise.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                filterExercises();
            }
        });

        // Set up filter chips
        chipGroupExerciseFilter.setOnCheckedStateChangeListener((group, checkedIds) -> {
            filterExercises();
        });

        // Close button
        btnCloseExerciseSelection.setOnClickListener(v -> dismiss());

        // Add custom exercise button
        btnAddCustomExercise.setOnClickListener(v -> {
            if (listener != null) {
                listener.onCustomExerciseRequested();
                dismiss();
            }
        });

        // Confirm selection button visibility (we'll show it if multi-select mode is enabled)
        btnConfirmExerciseSelection.setVisibility(View.GONE);

        // Ensure initial filter state is applied and UI visibility is updated
        filterExercises();

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            dialog.getWindow().setLayout(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.MATCH_PARENT
            );
        }
    }

    private void filterExercises() {
        String searchQuery = etSearchExercise.getText().toString().trim();
        String selectedMuscleGroup = "All";

        // Get the selected muscle group
        int checkedChipId = chipGroupExerciseFilter.getCheckedChipId();
        if (checkedChipId != View.NO_ID) {
            Chip selectedChip = chipGroupExerciseFilter.findViewById(checkedChipId);
            if (selectedChip != null) {
                selectedMuscleGroup = selectedChip.getText().toString();
            }
        }

        // Apply filters
        exerciseAdapter.filter(searchQuery, selectedMuscleGroup);

        // Show/hide no results message
        if (exerciseAdapter.getItemCount() == 0) {
            tvNoExercisesFound.setVisibility(View.VISIBLE);
            rvExerciseSelection.setVisibility(View.GONE);
        } else {
            tvNoExercisesFound.setVisibility(View.GONE);
            rvExerciseSelection.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onExerciseSelected(ExerciseLibrary exercise) {
        if (listener != null) {
            listener.onExerciseSelected(exercise);
            dismiss(); // Dismiss after selection if in single-select mode
        }
    }
}
