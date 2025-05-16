package com.example.aimingfitness;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.ArrayList;
import java.util.Collections; // Added for sorting
import java.util.List; // Added for List

// MPAndroidChart imports
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import androidx.core.content.ContextCompat; // Added for colors
import com.github.mikephil.charting.components.XAxis; // Import XAxis

public class ProgressFragment extends Fragment implements 
    ProgressAdapter.OnProgressItemInteractionListener,
    DailyProgressAdapter.OnDailyProgressItemInteractionListener {

    private static final String TAG = "ProgressFragment";
    private RecyclerView rvProgressEntries;
    private RecyclerView rvDailyProgressEntries; // New RecyclerView for daily progress
    private ProgressAdapter progressAdapter;
    private DailyProgressAdapter dailyProgressAdapter; // New adapter for daily progress
    private ProgressViewModel progressViewModel;
    private DailyProgressViewModel dailyProgressViewModel; // Added for MyFitnessPal-style tracking
    private FloatingActionButton fabAddProgress;
    private ProgressBar pbLoadingProgress;
    private LineChart progressChart; // Added LineChart

    public ProgressFragment() {
        // Required empty public constructor
    }    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        progressViewModel = new ViewModelProvider(requireActivity()).get(ProgressViewModel.class);
        
        // Initialize the new DailyProgressViewModel for MyFitnessPal-style tracking
        dailyProgressViewModel = new ViewModelProvider(requireActivity()).get(DailyProgressViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_progress, container, false);
        initViews(view);
        setupRecyclerView();
        setupFab();
        setupChart(); // Call setupChart
        observeViewModel();
        return view;
    }    @Override
    public void onResume() {
        super.onResume();
        // Fetch entries when the fragment becomes visible/resumed, in case of returning from other activities/fragments
        progressViewModel.fetchProgressEntries();
        
        // Also fetch daily progress entries for the MyFitnessPal-style tracking
        dailyProgressViewModel.fetchDailyProgressEntries();
    }    private void initViews(View view) {
        rvProgressEntries = view.findViewById(R.id.rvProgressEntries);
        rvDailyProgressEntries = view.findViewById(R.id.rvDailyProgressEntries); // Initialize new RecyclerView
        fabAddProgress = view.findViewById(R.id.fabAddProgress);
        pbLoadingProgress = view.findViewById(R.id.pbLoadingProgress);
        progressChart = view.findViewById(R.id.progressChart); // Initialize chart
    }

    private void setupRecyclerView() {
        // Setup for origi  nal progress entries
        progressAdapter = new ProgressAdapter(getContext(), new ArrayList<>(), this);
        rvProgressEntries.setLayoutManager(new LinearLayoutManager(getContext()));
        rvProgressEntries.setAdapter(progressAdapter);
        
        // Setup for new daily progress entries
        dailyProgressAdapter = new DailyProgressAdapter(getContext(), new ArrayList<>(), this);
        rvDailyProgressEntries.setLayoutManager(new LinearLayoutManager(getContext()));
        rvDailyProgressEntries.setAdapter(dailyProgressAdapter);
    }private void setupFab() {
        fabAddProgress.setOnClickListener(v -> {
            // Use the new DailyProgressDialogFragment with our redesigned UI
            DailyProgressDialogFragment dialogFragment = DailyProgressDialogFragment.newInstance();
            dialogFragment.show(getParentFragmentManager(), "DailyProgressDialog");
        });
    }

    private void setupChart() {
        progressChart.setNoDataText("No progress data yet. Add some entries!");
        progressChart.getDescription().setEnabled(false); // Disable description text
        progressChart.setTouchEnabled(true);
        progressChart.setDragEnabled(true);
        progressChart.setScaleEnabled(true);
        progressChart.setPinchZoom(true);
        // Add more initial chart setup as needed
    }

    private void updateChartData(List<ProgressEntry> entries) {
        if (entries == null || entries.isEmpty()) {
            progressChart.clear();
            progressChart.invalidate();
            return;
        }

        List<ProgressEntry> weightEntries = new ArrayList<>();
        // Example: Filter for "Body Weight". You can make this dynamic later.
        // Ensure you have R.string.progress_type_weight defined in your strings.xml
        String bodyWeightType = getString(R.string.progress_type_weight); 

        for (ProgressEntry entry : entries) {
            if (entry.getType() != null && entry.getType().equals(bodyWeightType) && entry.getDate() != null) {
                weightEntries.add(entry);
            }
        }

        if (weightEntries.isEmpty()) {
            progressChart.clear();
            progressChart.invalidate();
            return;
        }

        // Sort by date
        Collections.sort(weightEntries, (e1, e2) -> e1.getDate().toDate().compareTo(e2.getDate().toDate()));

        ArrayList<Entry> chartDataEntries = new ArrayList<>();
        for (ProgressEntry pe : weightEntries) {
            // Using timestamp for X-axis, value for Y-axis
            chartDataEntries.add(new Entry(pe.getDate().toDate().getTime(), (float) pe.getValue()));
        }

        LineDataSet lineDataSet = new LineDataSet(chartDataEntries, "Body Weight");
        // Style the dataset (colors, line thickness, circles, etc.)
        lineDataSet.setColor(ContextCompat.getColor(requireContext(), R.color.primary)); // Use R.color.primary
        lineDataSet.setCircleColor(ContextCompat.getColor(requireContext(), R.color.primary)); // Use R.color.primary
        lineDataSet.setLineWidth(2f);
        lineDataSet.setCircleRadius(4f);
        lineDataSet.setDrawCircleHole(false);
        lineDataSet.setValueTextSize(10f);
        lineDataSet.setValueTextColor(ContextCompat.getColor(requireContext(), R.color.text_primary)); // Use R.color.text_primary

        ArrayList<ILineDataSet> dataSets = new ArrayList<>();
        dataSets.add(lineDataSet);

        LineData lineData = new LineData(dataSets);
        progressChart.setData(lineData);

        // Customize X-axis (e.g., date formatter)
        XAxis xAxis = progressChart.getXAxis();
        xAxis.setValueFormatter(new DateAxisValueFormatter()); // Apply the date formatter
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM); // Optional: Set X-axis position
        xAxis.setGranularity(1f); // Optional: Ensure labels are not skipped if too close
        xAxis.setGranularityEnabled(true); // Optional: Enable granularity

        progressChart.animateX(1000); // Add a simple animation
        progressChart.invalidate(); // Refresh the chart
    }
    private void observeViewModel() {
        // Original ProgressViewModel observations
        progressViewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            if (isLoading != null) {
                pbLoadingProgress.setVisibility(isLoading ? View.VISIBLE : View.GONE);
                rvProgressEntries.setVisibility(isLoading ? View.GONE : View.VISIBLE);
                if (isLoading) {
                    progressChart.setVisibility(View.GONE);
                } else {
                    progressChart.setVisibility(View.VISIBLE);
                }
            }
        });

        progressViewModel.getProgressEntries().observe(getViewLifecycleOwner(), entries -> {
            if (entries != null) {
                Log.d(TAG, "Progress entries updated: " + entries.size() + " items");
                progressAdapter.updateProgress(entries);
                updateChartData(entries); // Call method to update chart
            } else {
                Log.d(TAG, "Progress entries is null");
                progressChart.clear(); // Clear chart if no entries
                progressChart.invalidate();
            }
        });
          // Observe the new DailyProgressViewModel
        dailyProgressViewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            // We can use the same loading indicators or add separate ones if needed
            // For now, will use the same for simplicity
            if (isLoading != null && isLoading) {
                pbLoadingProgress.setVisibility(View.VISIBLE);
            }
        });        dailyProgressViewModel.getDailyProgressEntries().observe(getViewLifecycleOwner(), entries -> {
            if (entries != null) {
                Log.d(TAG, "Daily progress entries updated: " + entries.size() + " items");
                dailyProgressAdapter.updateEntries(entries);
                updateNutritionChart(entries);
            }
        });
        
        dailyProgressViewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(getContext(), "Error: " + error, Toast.LENGTH_LONG).show();
                Log.e(TAG, "Daily Progress ViewModel error: " + error);
            }
        });
        
        dailyProgressViewModel.getOperationSuccessful().observe(getViewLifecycleOwner(), success -> {
            if (success != null && success) {
                Log.d(TAG, "Daily progress operation successful, fetching entries.");
                dailyProgressViewModel.fetchDailyProgressEntries(); // Refresh list
            }
        });

        progressViewModel.getError().observe(getViewLifecycleOwner(), error -> {
            if (error != null) {
                Toast.makeText(getContext(), "Error: " + error, Toast.LENGTH_LONG).show();
                Log.e(TAG, "ViewModel error: " + error);
                progressViewModel.resetErrorEvent();
            }
        });

        // Observe save success to refresh list
        progressViewModel.getProgressSaveSuccess().observe(getViewLifecycleOwner(), success -> {
            if (success != null && success) {
                Log.d(TAG, "Progress save success, fetching entries.");
                progressViewModel.fetchProgressEntries(); // Refresh list
                progressViewModel.resetProgressSaveSuccessEvent(); // Reset the event
            }
        });

        // Observe update success to refresh list
        progressViewModel.getProgressUpdateSuccess().observe(getViewLifecycleOwner(), success -> {
            if (success != null && success) {
                Log.d(TAG, "Progress update success, fetching entries.");
                progressViewModel.fetchProgressEntries(); // Refresh list
                progressViewModel.resetProgressUpdateSuccessEvent();
            }
        });

        // Observe delete success to refresh list
        progressViewModel.getProgressDeleteSuccess().observe(getViewLifecycleOwner(), success -> {
            if (success != null && success) {
                Log.d(TAG, "Progress delete success, fetching entries.");
                Toast.makeText(getContext(), "Entry deleted successfully", Toast.LENGTH_SHORT).show();
                progressViewModel.fetchProgressEntries(); // Refresh list
                progressViewModel.resetProgressDeleteSuccessEvent();
            } else if (success != null && !success){
                // Deletion failed, error should be handled by the getError() observer
                progressViewModel.resetProgressDeleteSuccessEvent();
            }
        });
    }

    @Override
    public void onEditClicked(ProgressEntry entry) {
        Log.d(TAG, "Edit clicked for entry: " + entry.getEntryId());
        AddProgressDialogFragment dialogFragment = AddProgressDialogFragment.newInstance(entry);
        dialogFragment.show(getParentFragmentManager(), "EditProgressDialog");
    }    /**
     * Updates the chart with nutrition data from daily progress entries
     * @param entries List of daily progress entries
     */
    private void updateNutritionChart(List<DailyProgressEntry> entries) {
        if (entries == null || entries.isEmpty()) {
            progressChart.clear();
            progressChart.invalidate();
            return;
        }

        // Sort by date
        Collections.sort(entries, (e1, e2) -> e1.getDate().compareTo(e2.getDate()));

        // Create entries for calories data
        ArrayList<Entry> caloriesEntries = new ArrayList<>();
        for (int i = 0; i < entries.size(); i++) {
            DailyProgressEntry entry = entries.get(i);
            if (entry.getCalories() != null) {
                // Using index for X-axis for even spacing, value for Y-axis
                caloriesEntries.add(new Entry(i, entry.getCalories()));
            }
        }

        if (caloriesEntries.isEmpty()) {
            progressChart.clear();
            progressChart.invalidate();
            return;
        }

        LineDataSet caloriesDataSet = new LineDataSet(caloriesEntries, "Calories");
        // Style the dataset
        caloriesDataSet.setColor(ContextCompat.getColor(requireContext(), R.color.primary));
        caloriesDataSet.setCircleColor(ContextCompat.getColor(requireContext(), R.color.primary));
        caloriesDataSet.setLineWidth(2f);
        caloriesDataSet.setCircleRadius(4f);
        caloriesDataSet.setDrawCircleHole(false);
        caloriesDataSet.setValueTextSize(10f);
        caloriesDataSet.setValueTextColor(ContextCompat.getColor(requireContext(), R.color.text_primary));

        // Optional: Add data sets for macros (carbs, protein, fat)
        ArrayList<Entry> carbsEntries = new ArrayList<>();
        ArrayList<Entry> proteinEntries = new ArrayList<>();
        ArrayList<Entry> fatEntries = new ArrayList<>();

        for (int i = 0; i < entries.size(); i++) {
            DailyProgressEntry entry = entries.get(i);
            if (entry.getCarbs() != null) {
                carbsEntries.add(new Entry(i, entry.getCarbs().floatValue()));
            }
            if (entry.getProtein() != null) {
                proteinEntries.add(new Entry(i, entry.getProtein().floatValue()));
            }
            if (entry.getFat() != null) {
                fatEntries.add(new Entry(i, entry.getFat().floatValue()));
            }
        }

        ArrayList<ILineDataSet> dataSets = new ArrayList<>();
        dataSets.add(caloriesDataSet);

        // If we have macros data, add them to the chart
        if (!carbsEntries.isEmpty()) {
            LineDataSet carbsDataSet = new LineDataSet(carbsEntries, "Carbs (g)");
            carbsDataSet.setColor(ContextCompat.getColor(requireContext(), android.R.color.holo_blue_light));
            carbsDataSet.setCircleColor(ContextCompat.getColor(requireContext(), android.R.color.holo_blue_light));
            carbsDataSet.setLineWidth(2f);
            carbsDataSet.setCircleRadius(3f);
            dataSets.add(carbsDataSet);
        }

        if (!proteinEntries.isEmpty()) {
            LineDataSet proteinDataSet = new LineDataSet(proteinEntries, "Protein (g)");
            proteinDataSet.setColor(ContextCompat.getColor(requireContext(), android.R.color.holo_red_light));
            proteinDataSet.setCircleColor(ContextCompat.getColor(requireContext(), android.R.color.holo_red_light));
            proteinDataSet.setLineWidth(2f);
            proteinDataSet.setCircleRadius(3f);
            dataSets.add(proteinDataSet);
        }

        if (!fatEntries.isEmpty()) {
            LineDataSet fatDataSet = new LineDataSet(fatEntries, "Fat (g)");
            fatDataSet.setColor(ContextCompat.getColor(requireContext(), android.R.color.holo_orange_light));
            fatDataSet.setCircleColor(ContextCompat.getColor(requireContext(), android.R.color.holo_orange_light));
            fatDataSet.setLineWidth(2f);
            fatDataSet.setCircleRadius(3f);
            dataSets.add(fatDataSet);
        }

        LineData lineData = new LineData(dataSets);
        progressChart.setData(lineData);

        // Customize X-axis with dates from entries
        XAxis xAxis = progressChart.getXAxis();
        xAxis.setValueFormatter(new com.github.mikephil.charting.formatter.IndexAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                int index = (int) value;
                if (index >= 0 && index < entries.size()) {
                    // Format date as short date string (e.g., "May 5")
                    java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("MMM d", java.util.Locale.getDefault());
                    return sdf.format(entries.get(index).getDate());
                }
                return "";
            }
        });
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setLabelRotationAngle(45f); // Rotate labels for better visibility
        
        progressChart.animateX(1000);
        progressChart.invalidate();
    }

    // Implement DailyProgressAdapter.OnDailyProgressItemInteractionListener
    @Override
    public void onEditClicked(DailyProgressEntry entry) {
        Log.d(TAG, "Edit clicked for daily progress entry: " + entry.getId());
        DailyProgressDialogFragment dialogFragment = DailyProgressDialogFragment.newInstance(entry);
        dialogFragment.show(getParentFragmentManager(), "EditDailyProgressDialog");
    }

    @Override
    public void onDeleteClicked(DailyProgressEntry entry) {
        Log.d(TAG, "Delete clicked for daily progress entry: " + entry.getId());
        new AlertDialog.Builder(requireContext())
            .setTitle("Delete Progress")
            .setMessage("Are you sure you want to delete this progress entry?")
            .setPositiveButton("Delete", (dialog, which) -> {
                dailyProgressViewModel.deleteDailyProgressEntry(entry.getId());
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    @Override
    public void onDeleteClicked(ProgressEntry entry) {
        Log.d(TAG, "Delete clicked for entry: " + entry.getEntryId());
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Progress Entry")
                .setMessage("Are you sure you want to delete this entry?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    if (entry.getEntryId() != null && !entry.getEntryId().isEmpty()) {
                        progressViewModel.deleteProgressEntry(entry.getEntryId());
                    } else {
                        Log.e(TAG, "Entry ID is null or empty, cannot delete.");
                        Toast.makeText(getContext(), "Error: Cannot delete entry without ID.", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
