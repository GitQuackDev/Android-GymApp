package com.example.aimingfitness;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log; // Added for logging
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.List;

public class WorkoutActivity extends AppCompatActivity implements WorkoutAdapter.WorkoutActionListener, WorkoutAdapter.WorkoutItemClickListener {

    private static final String TAG = "WorkoutActivity"; // Added for logging

    private RecyclerView recyclerViewWorkouts;
    private WorkoutAdapter workoutAdapter;
    private List<Workout> workoutList;
    private FloatingActionButton fabAddWorkout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workout); // Make sure this layout file exists

        recyclerViewWorkouts = findViewById(R.id.recyclerViewWorkouts);
        fabAddWorkout = findViewById(R.id.fabAddWorkout);

        workoutList = new ArrayList<>();
        // TODO: Initialize with actual data from Firebase or local DB in onResume or a ViewModel

        workoutAdapter = new WorkoutAdapter(this, workoutList, this, this);
        recyclerViewWorkouts.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewWorkouts.setAdapter(workoutAdapter);

        fabAddWorkout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(WorkoutActivity.this, AddWorkoutActivity.class);
                startActivity(intent); // Added this line
            }
        });

        loadWorkouts(); // Initial load
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadWorkouts(); // Ensure UI is always updated when returning to this activity
    }

    private void loadWorkouts() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                : null;

        if (currentUserId == null) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "User not authenticated, cannot load workouts."); // Added logging
            return;
        }
        Log.d(TAG, "Loading workouts for userId: " + currentUserId); // Added logging

        db.collection("workouts")
                .whereEqualTo("userId", currentUserId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    Log.d(TAG, "Successfully fetched " + queryDocumentSnapshots.size() + " workout documents."); // Added logging
                    workoutList.clear();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        Log.d(TAG, "Raw workout data from Firestore: " + doc.getData()); // Added logging for raw data
                        Workout workout = doc.toObject(Workout.class);
                        if (workout != null) {
                            Log.d(TAG, "Parsed workout: " + workout.getName() + ", ID: " + workout.getId()); // Added logging for parsed object
                            workoutList.add(workout);
                        } else {
                            Log.w(TAG, "Failed to parse workout document: " + doc.getId()); // Added logging for parsing failure
                        }
                    }
                    Log.d(TAG, "Total workouts in list: " + workoutList.size()); // Added logging
                    workoutAdapter.updateWorkouts(workoutList);
                    // Show/hide empty state
                    View emptyStateView = findViewById(R.id.emptyStateViewWorkouts);
                    if (workoutList.isEmpty()) {
                        Log.d(TAG, "Workout list is empty, showing empty state."); // Added logging
                        emptyStateView.setVisibility(View.VISIBLE);
                        recyclerViewWorkouts.setVisibility(View.GONE);
                    } else {
                        Log.d(TAG, "Workout list has items, showing recycler view."); // Added logging
                        emptyStateView.setVisibility(View.GONE);
                        recyclerViewWorkouts.setVisibility(View.VISIBLE);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to load workouts", e); // Enhanced logging
                    Toast.makeText(this, "Failed to load workouts: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    // Handle item click from WorkoutItemClickListener
    @Override
    public void onWorkoutItemClick(Workout workout) {
        // Handle workout item click (e.g., open details)
        Toast.makeText(this, "Clicked: " + workout.getName(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onWorkoutEdit(Workout workout, int position) {
        // Handle workout edit action
        Toast.makeText(this, "Edit: " + workout.getName(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onWorkoutDelete(Workout workout, int position) {
        // Handle workout delete action
        Toast.makeText(this, "Delete: " + workout.getName(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onWorkoutStart(Workout workout, int position) {
        // Handle workout start action
        Toast.makeText(this, "Start: " + workout.getName(), Toast.LENGTH_SHORT).show();
    }

    // TODO: Handle onActivityResult if you use startActivityForResult to get data back from AddWorkoutActivity
    // @Override
    // protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
    //     super.onActivityResult(requestCode, resultCode, data);
    //     if (requestCode == ADD_WORKOUT_REQUEST && resultCode == RESULT_OK) {
    //         // A new workout might have been added, reload the list
    //         loadWorkouts();
    //     }
    // }
}
