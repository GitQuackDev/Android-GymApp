package com.example.aimingfitness;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import androidx.cardview.widget.CardView;

// MPAndroidChart imports
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import androidx.core.content.ContextCompat;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

public class ProgressFragment extends Fragment implements 
    ProgressAdapter.OnProgressItemInteractionListener, // Listener for general progress entries
    DailyProgressAdapter.OnDailyProgressItemInteractionListener { // Listener for daily summary entries

    private static final String TAG = "ProgressFragment";
    private RecyclerView rvProgressEntries;
    private RecyclerView rvDailyProgressEntries;
    private ProgressAdapter progressAdapter;
    private DailyProgressAdapter dailyProgressAdapter;
    private ProgressViewModel progressViewModel;
    private DailyProgressViewModel dailyProgressViewModel;
    private FloatingActionButton fabAddProgress;
    private ProgressBar pbLoadingProgress;
    private BarChart exerciseBarChart;
    private Spinner spinnerExerciseFilter;
    private CardView cardExerciseChart;
    private TextView tvNoExerciseData;
    private String selectedExercise = "All Exercises"; // Default exercise filter

    public ProgressFragment() {
        // Required empty public constructor
    }
    
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        progressViewModel = new ViewModelProvider(requireActivity()).get(ProgressViewModel.class);
        dailyProgressViewModel = new ViewModelProvider(requireActivity()).get(DailyProgressViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_progress, container, false);
        initViews(view);
        setupRecyclerView();
        setupFab();
        setupCharts();
        setupExerciseFilter();
        observeViewModel();
        return view;
    }
    
    @Override
    public void onResume() {
        super.onResume();
        progressViewModel.fetchProgressEntries();
        progressViewModel.fetchWorkoutLogEntries(); // Fetch workout logs
        dailyProgressViewModel.fetchDailyProgressEntries();
    }
    private void initViews(View view) {
        rvProgressEntries = view.findViewById(R.id.rvProgressEntries);
        rvDailyProgressEntries = view.findViewById(R.id.rvDailyProgressEntries);
        fabAddProgress = view.findViewById(R.id.fabAddProgress);
        pbLoadingProgress = view.findViewById(R.id.pbLoadingProgress);
        
        exerciseBarChart = view.findViewById(R.id.exerciseBarChart);
        spinnerExerciseFilter = view.findViewById(R.id.spinnerExerciseFilter);
        
        cardExerciseChart = view.findViewById(R.id.cardExerciseChart);
        
        tvNoExerciseData = view.findViewById(R.id.tvNoExerciseData);
    }

    private void setupRecyclerView() {
        progressAdapter = new ProgressAdapter(getContext(), new ArrayList<>(), this);
        rvProgressEntries.setLayoutManager(new LinearLayoutManager(getContext()));
        rvProgressEntries.setAdapter(progressAdapter);
        
        dailyProgressAdapter = new DailyProgressAdapter(getContext(), new ArrayList<>(), this);
        rvDailyProgressEntries.setLayoutManager(new LinearLayoutManager(getContext()));
        rvDailyProgressEntries.setAdapter(dailyProgressAdapter);
    }
      private void setupFab() {
        fabAddProgress.setOnClickListener(v -> {
            DailyProgressDialogFragment dialogFragment = DailyProgressDialogFragment.newInstance();
            dialogFragment.show(getParentFragmentManager(), "DailyProgressDialog");
        });
    }
    
    private void setupExerciseFilter() {
        // Will be populated with exercises when data is loaded
    }
    
    private void updateExerciseFilterSpinner(List<ProgressEntry> entries) {
        // Extract unique exercise names
        Set<String> exercises = new HashSet<>();
        exercises.add("All Exercises");
        
        for (ProgressEntry entry : entries) {
            if (entry.getType() != null && entry.getType().equals("Workout Log") && entry.getExerciseName() != null) {
                exercises.add(entry.getExerciseName());
            }
        }
        
        // Create adapter for spinner
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(
                requireContext(), android.R.layout.simple_spinner_item, new ArrayList<>(exercises));
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerExerciseFilter.setAdapter(spinnerAdapter);
        
        // Select "All Exercises" by default
        int position = spinnerAdapter.getPosition("All Exercises");
        spinnerExerciseFilter.setSelection(position);
        
        // Set listener
        spinnerExerciseFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedExercise = (String) parent.getItemAtPosition(position);
                List<ProgressEntry> workoutEntries = progressViewModel.getWorkoutLogEntries().getValue();
                if (workoutEntries != null) {
                    Log.d(TAG, "Exercise filter changed to: " + selectedExercise);
                    updateExerciseBarChart(workoutEntries);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });
    }
    
    private void setupCharts() {
        // Setup Exercise Bar Chart
        exerciseBarChart.setNoDataText("No exercise data yet. Complete some workouts!");
        exerciseBarChart.getDescription().setEnabled(false);
        exerciseBarChart.setTouchEnabled(true);
        exerciseBarChart.setDragEnabled(true);
        exerciseBarChart.setScaleEnabled(true);
        exerciseBarChart.setPinchZoom(false);
        exerciseBarChart.setDrawValueAboveBar(true);
        exerciseBarChart.setDrawGridBackground(false);
        exerciseBarChart.setFitBars(true);
        
        XAxis xAxisExercise = exerciseBarChart.getXAxis();
        xAxisExercise.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxisExercise.setDrawGridLines(false);
        xAxisExercise.setGranularity(1f);
        xAxisExercise.setLabelRotationAngle(45);
        
        YAxis leftAxis = exerciseBarChart.getAxisLeft();
        leftAxis.setLabelCount(8, false);
        leftAxis.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART);
        leftAxis.setSpaceTop(15f);
        
        exerciseBarChart.getAxisRight().setEnabled(false);
    }
      // Chart visibility method removed as we're only using the weight chart
      // Exercise filter methods removed as we're simplifying to just show the weight chart

    private void updateExerciseBarChart(List<ProgressEntry> entries) {
        if (entries == null || entries.isEmpty()) {
            exerciseBarChart.clear();
            exerciseBarChart.invalidate();
            tvNoExerciseData.setVisibility(View.VISIBLE);
            exerciseBarChart.setVisibility(View.GONE);
            Log.d(TAG, "No exercise entries to display in chart");
            return;
        }

        List<ProgressEntry> workoutEntries = new ArrayList<>();
        for (ProgressEntry entry : entries) {
            if (entry.getType() != null && entry.getType().equals("Workout Log") && entry.getExerciseName() != null) {
                // If filtering is active, only include the selected exercise
                if (selectedExercise.equals("All Exercises") || selectedExercise.equals(entry.getExerciseName())) {
                    workoutEntries.add(entry);
                    Log.d(TAG, "Adding to chart: Exercise=" + entry.getExerciseName() 
                        + ", Value=" + entry.getValue() + ", Sets=" + entry.getSets() 
                        + ", Reps=" + entry.getReps() + ", Date=" + entry.getDate().toDate());
                }
            }
        }

        if (workoutEntries.isEmpty()) {
            exerciseBarChart.clear();
            exerciseBarChart.invalidate();
            tvNoExerciseData.setVisibility(View.VISIBLE);
            exerciseBarChart.setVisibility(View.GONE);
            Log.d(TAG, "No matching workout entries for filter: " + selectedExercise);
            return;
        }

        tvNoExerciseData.setVisibility(View.GONE);
        exerciseBarChart.setVisibility(View.VISIBLE);
        
        // Sort by date
        Collections.sort(workoutEntries, (e1, e2) -> e1.getDate().toDate().compareTo(e2.getDate().toDate()));

        // Group entries by exercise name, then map each exercise to its average weight
        Map<String, List<ProgressEntry>> exerciseEntries = new HashMap<>();
        
        for (ProgressEntry entry : workoutEntries) {
            String exerciseName = entry.getExerciseName();
            if (!exerciseEntries.containsKey(exerciseName)) {
                exerciseEntries.put(exerciseName, new ArrayList<>());
            }
            exerciseEntries.get(exerciseName).add(entry);
        }
        
        ArrayList<BarEntry> barEntries = new ArrayList<>();
        ArrayList<String> xLabels = new ArrayList<>();
        int index = 0;
        
        for (Map.Entry<String, List<ProgressEntry>> mapEntry : exerciseEntries.entrySet()) {
            List<ProgressEntry> exerciseList = mapEntry.getValue();
            // Calculate average weight for this exercise
            double totalWeight = 0;
            for (ProgressEntry entry : exerciseList) {
                totalWeight += entry.getValue();
            }
            float avgWeight = (float) (totalWeight / exerciseList.size());
            
            barEntries.add(new BarEntry(index, avgWeight));
            xLabels.add(mapEntry.getKey());
            index++;
            
            Log.d(TAG, "Chart data: Exercise=" + mapEntry.getKey() + ", AvgWeight=" + avgWeight 
                    + ", TotalEntries=" + exerciseList.size());
        }

        BarDataSet barDataSet = new BarDataSet(barEntries, "Exercise Weight");
        
        // Set colors for each bar
        int[] colors = new int[barEntries.size()];
        for (int i = 0; i < barEntries.size(); i++) {
            colors[i] = ColorTemplate.MATERIAL_COLORS[i % ColorTemplate.MATERIAL_COLORS.length];
        }
        barDataSet.setColors(colors);
        
        barDataSet.setDrawValues(true);
        barDataSet.setValueTextSize(10f);
        barDataSet.setValueTextColor(ContextCompat.getColor(requireContext(), R.color.black));

        BarData barData = new BarData(barDataSet);
        barData.setBarWidth(0.9f);
        
        exerciseBarChart.setData(barData);
        
        // Customize X-axis with exercise names
        XAxis xAxis = exerciseBarChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(xLabels));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setLabelCount(xLabels.size());
        xAxis.setLabelRotationAngle(45f);

        exerciseBarChart.animateY(1000);
        exerciseBarChart.invalidate();
    }

    private void observeViewModel() {
        pbLoadingProgress.setVisibility(View.VISIBLE);

        progressViewModel.getProgressEntries().observe(getViewLifecycleOwner(), entries -> {
            if (entries != null) {
                Log.d(TAG, "Observed " + entries.size() + " general progress entries.");
                // Filter out workout logs if they are mixed in, though they shouldn't be with separate fetching
                List<ProgressEntry> generalProgressEntries = new ArrayList<>();
                for (ProgressEntry entry : entries) {
                    if (!"Workout Log".equals(entry.getType())) {
                        generalProgressEntries.add(entry);
                    }
                }
                progressAdapter.updateEntries(generalProgressEntries);
            } else {
                Log.d(TAG, "Observed null general progress entries.");
            }
        });

        // New observer for workout log entries
        progressViewModel.getWorkoutLogEntries().observe(getViewLifecycleOwner(), workoutLogEntries -> {
            if (workoutLogEntries != null) {
                Log.d(TAG, "Observed " + workoutLogEntries.size() + " workout log entries for chart.");
                updateExerciseBarChart(workoutLogEntries);
                updateExerciseFilterSpinner(workoutLogEntries); // Update spinner with exercises from workout logs
            } else {
                Log.d(TAG, "Observed null workout log entries.");
                updateExerciseBarChart(new ArrayList<>()); // Clear chart if null
                updateExerciseFilterSpinner(new ArrayList<>()); // Clear spinner if null
            }
            // Consider moving pbLoadingProgress.setVisibility(View.GONE) here if this is the last data to load
        });

        dailyProgressViewModel.getDailyProgressEntries().observe(getViewLifecycleOwner(), entries -> {
            if (entries != null) {
                Log.d(TAG, "Observed " + entries.size() + " daily progress entries.");
                dailyProgressAdapter.updateEntries(entries);
            } else {
                Log.d(TAG, "Observed null daily progress entries.");
            }
             pbLoadingProgress.setVisibility(View.GONE); 
        });

        progressViewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            if (isLoading != null && isLoading) {
                pbLoadingProgress.setVisibility(View.VISIBLE);
            } else {
                // Loading state might be complex with multiple fetches.
                // Consider a more robust way to manage global loading state if needed.
                // For now, individual fetches manage their parts, and dailyProgressViewModel handles the final GONE.
            }
        });
        
        dailyProgressViewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            if (isLoading != null && isLoading) {
                pbLoadingProgress.setVisibility(View.VISIBLE);
            } else {
                pbLoadingProgress.setVisibility(View.GONE);
            }
        });

        progressViewModel.getError().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(getContext(), "Error: " + error, Toast.LENGTH_LONG).show();
                Log.e(TAG, "ProgressViewModel Error: " + error);
            }
        });
        
        // Corrected method name: getErrorMessage instead of getError
        dailyProgressViewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(getContext(), "Error: " + error, Toast.LENGTH_LONG).show();
                Log.e(TAG, "DailyProgressViewModel Error: " + error);
            }
        });

        progressViewModel.getProgressDeleteSuccess().observe(getViewLifecycleOwner(), success -> {
            if (success != null && success) {
                Toast.makeText(getContext(), "Progress entry deleted.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Implementation for ProgressAdapter.OnProgressItemInteractionListener
    @Override
    public void onEditClicked(ProgressEntry entry) { // This is for ProgressAdapter
        // Renaming to avoid conflict and make it clear this is for general ProgressEntry
        // Actual rename: onEditProgressClicked. For now, keeping as is to match interface.
        Toast.makeText(getContext(), "Edit clicked for: " + entry.getType(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDeleteClicked(ProgressEntry entry) { // This is for ProgressAdapter
        // Renaming to avoid conflict. Actual rename: onDeleteProgressClicked.
        new AlertDialog.Builder(requireContext())
            .setTitle("Delete Entry")
            .setMessage("Are you sure you want to delete this progress entry?")
            .setPositiveButton("Delete", (dialog, which) -> {
                if (entry.getEntryId() != null && !entry.getEntryId().isEmpty()) {
                    progressViewModel.deleteProgressEntry(entry.getEntryId());
                } else {
                    Toast.makeText(getContext(), "Error: Entry ID is missing.", Toast.LENGTH_SHORT).show();
                }
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    // Implementation for DailyProgressAdapter.OnDailyProgressItemInteractionListener
    // These methods correctly override DailyProgressAdapter.OnDailyProgressItemInteractionListener
    @Override
    public void onEditClicked(DailyProgressEntry entry) { // This is for DailyProgressAdapter
        DailyProgressDialogFragment dialogFragment = DailyProgressDialogFragment.newInstance(entry);
        dialogFragment.show(getParentFragmentManager(), "DailyProgressDialogEdit");
    }

    @Override
    public void onDeleteClicked(DailyProgressEntry entry) { // This is for DailyProgressAdapter
        new AlertDialog.Builder(requireContext())
            .setTitle("Delete Daily Entry")
            .setMessage("Are you sure you want to delete this daily entry?")
            .setPositiveButton("Delete", (dialog, which) -> {
                if (entry.getId() != null && !entry.getId().isEmpty()) {
                    dailyProgressViewModel.deleteDailyProgressEntry(entry.getId());
                } else {
                    Toast.makeText(getContext(), "Error: Daily Entry ID is missing.", Toast.LENGTH_SHORT).show();
                }
            })
            .setNegativeButton("Cancel", null)
            .show();
    }
}
