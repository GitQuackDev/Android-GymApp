package com.example.aimingfitness;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class WorkoutsFragment extends Fragment implements WorkoutAdapter.WorkoutActionListener, WorkoutAdapter.WorkoutItemClickListener {

    private static final String TAG = "WorkoutsFragment";

    private RecyclerView rvWorkouts;
    private WorkoutAdapter workoutAdapter;
    private List<Workout> workoutList;
    private FirebaseFirestore db;
    private FloatingActionButton fabAddWorkoutFragment; // Added FAB variable
    private View emptyStateView; // Added for empty state

    public WorkoutsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView called");
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_workouts, container, false);
        Log.d(TAG, "Layout inflated: " + (view != null));

        rvWorkouts = view.findViewById(R.id.rvWorkouts);
        fabAddWorkoutFragment = view.findViewById(R.id.fabAddWorkoutFragment); // Initialize FAB
        emptyStateView = view.findViewById(R.id.emptyStateViewWorkouts); // Initialize empty state view

        Log.d(TAG, "RecyclerView found: " + (rvWorkouts != null));
        Log.d(TAG, "FAB found: " + (fabAddWorkoutFragment != null)); // Log FAB initialization
        Log.d(TAG, "EmptyStateView found: " + (emptyStateView != null));

        rvWorkouts.setLayoutManager(new LinearLayoutManager(getContext()));
        workoutList = new ArrayList<>();
        // Pass 'this' as the WorkoutActionListener to the adapter
        workoutAdapter = new WorkoutAdapter(getContext(), workoutList, this, this);
        rvWorkouts.setAdapter(workoutAdapter);
        Log.d(TAG, "Adapter set on RecyclerView");

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Set OnClickListener for the FAB
        if (fabAddWorkoutFragment != null) {
            fabAddWorkoutFragment.setOnClickListener(v -> {
                Log.d(TAG, "fabAddWorkoutFragment clicked"); // Log FAB click
                Intent intent = new Intent(getActivity(), AddWorkoutActivity.class);
                startActivity(intent);
            });
        } else {
            Log.w(TAG, "fabAddWorkoutFragment is null, cannot set listener.");
        }

        fetchWorkoutsFromFirestore();

        return view;
    }

    private void fetchWorkoutsFromFirestore() {
        Log.d(TAG, "fetchWorkoutsFromFirestore called"); // Added logging
        String currentUserId = null;
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            Log.d(TAG, "Current User ID: " + currentUserId); // Added logging
        } else {
            Log.w(TAG, "User not authenticated. Cannot fetch workouts.");
            workoutList.clear();
            workoutAdapter.updateWorkouts(new ArrayList<>()); // Pass a new list to clear
            updateEmptyStateVisibility();
            Toast.makeText(getContext(), "Please sign in to view workouts.", Toast.LENGTH_LONG).show();
            return;
        }

        final String finalCurrentUserId = currentUserId;
        db.collection("workouts")
                .whereEqualTo("userId", finalCurrentUserId)
                .get()
                .addOnSuccessListener(task -> {
                    Log.d(TAG, "Successfully fetched " + task.size() + " workout documents (with userId filter: " + finalCurrentUserId + ").");
                    for (QueryDocumentSnapshot document : task) {
                        Log.d(TAG, "Document ID: " + document.getId() + ", userId: " + document.getString("userId") + ", Data: " + document.getData());
                    }
                    List<Workout> fetchedWorkouts = new ArrayList<>();
                    for (QueryDocumentSnapshot document : task) {
                        Log.d(TAG, "Raw workout data from Firestore: " + document.getData());
                        Workout workout = document.toObject(Workout.class);
                        if (workout != null) {
                            workout.setId(document.getId());
                            // Ensure exercises list is initialized if it's null from Firestore
                            if (workout.getExercises() == null) {
                                workout.setExercises(new ArrayList<>());
                            }
                            fetchedWorkouts.add(workout);
                            Log.d(TAG, "Parsed workout: " + workout.getName() + ", ID: " + workout.getId() + ", Exercises: " + workout.getExercises().size());
                        } else {
                            Log.w(TAG, "Failed to parse workout document: " + document.getId());
                        }
                    }
                    // Recreate adapter with fresh list to ensure items display
                    rvWorkouts.setAdapter(new WorkoutAdapter(getContext(), fetchedWorkouts, WorkoutsFragment.this, WorkoutsFragment.this));
                    int count = fetchedWorkouts.size();
                    Log.d(TAG, "Total workouts fetched: " + count);
                    // Show/hide empty state based on fetched list
                    if (count == 0) {
                        emptyStateView.setVisibility(View.VISIBLE);
                        rvWorkouts.setVisibility(View.GONE);
                    } else {
                        emptyStateView.setVisibility(View.GONE);
                        rvWorkouts.setVisibility(View.VISIBLE);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error getting documents.", e);
                    Toast.makeText(getContext(), "Error fetching workouts: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    workoutList.clear();
                    workoutAdapter.updateWorkouts(new ArrayList<>());
                    updateEmptyStateVisibility();
                });
    }

    private void updateEmptyStateVisibility() {
        if (emptyStateView != null && rvWorkouts != null) {
            if (workoutList.isEmpty()) {
                Log.d(TAG, "Workout list is empty, showing empty state.");
                emptyStateView.setVisibility(View.VISIBLE);
                rvWorkouts.setVisibility(View.GONE);
            } else {
                Log.d(TAG, "Workout list has items, showing recycler view.");
                emptyStateView.setVisibility(View.GONE);
                rvWorkouts.setVisibility(View.VISIBLE);
            }
        } else {
            Log.w(TAG, "emptyStateViewWorkouts or rvWorkouts not found in fragment_workouts.xml for empty state handling.");
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume called, refreshing workouts.");
        if (db != null) {
            fetchWorkoutsFromFirestore();
        }
    }

    @Override
    public void onWorkoutStart(Workout workout, int position) {
        startWorkoutSession(workout);
    }

    @Override
    public void onWorkoutDelete(Workout workout, int position) {
        new AlertDialog.Builder(getContext())
            .setTitle("Delete Workout")
            .setMessage("Are you sure you want to delete '" + workout.getName() + "'?")
            .setPositiveButton("Delete", (dialog, which) -> {
                deleteWorkoutFromFirestore(workout);
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void deleteWorkoutFromFirestore(Workout workout) {
        if (workout.getId() == null || workout.getId().isEmpty()) {
            Log.e(TAG, "Workout ID is null or empty, cannot delete.");
            Toast.makeText(getContext(), "Error: Workout ID missing.", Toast.LENGTH_SHORT).show();
            return;
        }
        db.collection("workouts").document(workout.getId())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Workout successfully deleted: " + workout.getName());
                    Toast.makeText(getContext(), "Workout '" + workout.getName() + "' deleted.", Toast.LENGTH_SHORT).show();
                    // No need to call fetchWorkoutsFromFirestore() if adapter is updated directly
                    int removedPosition = workoutList.indexOf(workout);
                    if (removedPosition != -1) {
                        workoutList.remove(removedPosition);
                        workoutAdapter.notifyItemRemoved(removedPosition);
                        workoutAdapter.notifyItemRangeChanged(removedPosition, workoutList.size());
                        updateEmptyStateVisibility(); // Update empty state if needed
                    } else {
                        fetchWorkoutsFromFirestore(); // Fallback if item not found in list
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error deleting workout", e);
                    Toast.makeText(getContext(), "Error deleting workout: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public void onWorkoutEdit(Workout workout, int position) {
        Intent intent = new Intent(getActivity(), AddWorkoutActivity.class);
        intent.putExtra("workout", workout);
        intent.putExtra("isEditMode", true);
        startActivity(intent);
    }

    // This method is from WorkoutAdapter.WorkoutItemClickListener
    @Override
    public void onWorkoutItemClick(Workout workout) {
        startWorkoutSession(workout);
    }

    private void startWorkoutSession(Workout workout) {
        if (workout.getExercises() == null || workout.getExercises().isEmpty()) {
            Toast.makeText(getContext(), getString(R.string.no_exercises_found), Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(getActivity(), WorkoutSessionActivity.class);
        intent.putExtra("workout", workout);
        startActivity(intent);
    }
}
