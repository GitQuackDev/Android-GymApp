package com.example.aimingfitness;

import android.app.Application;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.Timestamp; // Added this import
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ProgressViewModel extends AndroidViewModel {

    private static final String TAG = "ProgressViewModel";

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    private MutableLiveData<List<ProgressEntry>> progressEntriesLiveData = new MutableLiveData<>();
    private MutableLiveData<List<ProgressEntry>> workoutLogEntriesLiveData = new MutableLiveData<>(); // New LiveData for workout logs
    private MutableLiveData<Boolean> isLoadingLiveData = new MutableLiveData<>();
    private MutableLiveData<String> errorLiveData = new MutableLiveData<>();
    private MutableLiveData<Boolean> progressSaveSuccessLiveData = new MutableLiveData<>();
    private MutableLiveData<Boolean> progressUpdateSuccessLiveData = new MutableLiveData<>();
    private MutableLiveData<Boolean> progressDeleteSuccessLiveData = new MutableLiveData<>();


    public ProgressViewModel(@NonNull Application application) {
        super(application);
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
    }

    public LiveData<List<ProgressEntry>> getProgressEntries() {
        return progressEntriesLiveData;
    }

    // New getter for workoutLogEntriesLiveData
    public LiveData<List<ProgressEntry>> getWorkoutLogEntries() {
        return workoutLogEntriesLiveData;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoadingLiveData;
    }

    public LiveData<String> getError() {
        return errorLiveData;
    }

    public LiveData<Boolean> getProgressSaveSuccess() {
        return progressSaveSuccessLiveData;
    }

    public LiveData<Boolean> getProgressUpdateSuccess() {
        return progressUpdateSuccessLiveData;
    }

    public LiveData<Boolean> getProgressDeleteSuccess() {
        return progressDeleteSuccessLiveData;
    }

    public void fetchProgressEntries() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            errorLiveData.setValue("User not logged in.");
            progressEntriesLiveData.setValue(new ArrayList<>()); // Post empty list
            return;
        }
        String userId = currentUser.getUid();
        isLoadingLiveData.setValue(true);

        db.collection("progress").document(userId).collection("entries") // This fetches general progress
                .whereEqualTo("userId", userId) 
                .orderBy("date", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    isLoadingLiveData.setValue(false); // Manages loading for this specific fetch
                    if (task.isSuccessful()) {
                        List<ProgressEntry> entries = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            ProgressEntry entry = document.toObject(ProgressEntry.class);
                            entry.setEntryId(document.getId());
                            entries.add(entry);
                        }
                        progressEntriesLiveData.setValue(entries);
                    } else {
                        Log.w(TAG, "Error getting general progress documents: ", task.getException());
                        errorLiveData.setValue("Error fetching general progress: " + task.getException().getMessage());
                        progressEntriesLiveData.setValue(new ArrayList<>()); 
                    }
                });
    }

    // New method to fetch workout log entries from workoutHistory
    public void fetchWorkoutLogEntries() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            errorLiveData.setValue("User not logged in. Cannot fetch workout logs.");
            workoutLogEntriesLiveData.setValue(new ArrayList<>());
            return;
        }
        String userId = currentUser.getUid();
        isLoadingLiveData.setValue(true);

        db.collection("workoutHistory")
                .whereEqualTo("userId", userId)
                .orderBy("date", Query.Direction.DESCENDING) // Re-enable to order by date
                .get()
                .addOnCompleteListener(task -> {
                    isLoadingLiveData.setValue(false);
                    if (task.isSuccessful()) {
                        List<ProgressEntry> allExerciseEntries = new ArrayList<>();
                        if (task.getResult() != null) {
                            for (QueryDocumentSnapshot sessionDocument : task.getResult()) {
                                Timestamp sessionDate = sessionDocument.getTimestamp("date");
                                if (sessionDate == null) {
                                    Log.w(TAG, "Workout session " + sessionDocument.getId() + " missing date.");
                                    continue; // Skip if no date
                                }

                                // Get the exercises array from the workout
                                List<Map<String, Object>> exercisesInSession = (List<Map<String, Object>>) sessionDocument.get("exercises");
                                String workoutName = sessionDocument.getString("workoutName"); // Updated field name to match the Firestore structure
                                
                                if (workoutName == null || workoutName.isEmpty()) {
                                    workoutName = "Unnamed Workout";
                                }

                                if (exercisesInSession != null && !exercisesInSession.isEmpty()) {
                                    for (Map<String, Object> exerciseData : exercisesInSession) {
                                        ProgressEntry entry = new ProgressEntry();
                                        entry.setEntryId(sessionDocument.getId() + "_" + exerciseData.hashCode()); // Create a unique-ish ID
                                        entry.setUserId(userId);
                                        entry.setType("Workout Log");
                                        entry.setDate(sessionDate);

                                        // Get the name of the exercise
                                        String exerciseName = (String) exerciseData.get("name");
                                        
                                        // Parse weight value (could be empty string or numeric)
                                        double weight = 0.0;
                                        Object weightObj = exerciseData.get("weight");
                                        if (weightObj instanceof Number) {
                                            weight = ((Number) weightObj).doubleValue();
                                        } else if (weightObj instanceof String) {
                                            String weightStr = (String) weightObj;
                                            if (weightStr != null && !weightStr.isEmpty()) {
                                                try {
                                                    weight = Double.parseDouble(weightStr);
                                                } catch (NumberFormatException e) {
                                                    Log.w(TAG, "Could not parse weight string: '" + weightStr + "' for exercise '" 
                                                            + exerciseName + "' in workout '" + workoutName + "'");
                                                }
                                            } else {
                                                // For exercises with empty weight, use sets×reps as a value
                                                // This ensures the chart still shows something even with empty weights
                                                int sets = 0;
                                                Object setsObj = exerciseData.get("sets");
                                                if (setsObj instanceof Number) {
                                                    sets = ((Number) setsObj).intValue();
                                                }
                                                
                                                int reps = 0;
                                                Object repsObj = exerciseData.get("reps");
                                                if (repsObj instanceof Number) {
                                                    reps = ((Number) repsObj).intValue();
                                                } else if (repsObj instanceof String) {
                                                    String repsStr = (String) repsObj;
                                                    if (repsStr != null && !repsStr.isEmpty()) {
                                                        try {
                                                            reps = Integer.parseInt(repsStr);
                                                        } catch (NumberFormatException e) {
                                                            Log.w(TAG, "Could not parse reps string: '" + repsStr + "'");
                                                        }
                                                    }
                                                }
                                                
                                                // Use sets×reps as value if weight is empty
                                                if (sets > 0 && reps > 0) {
                                                    weight = sets * reps; 
                                                }
                                            }
                                        }

                                        // Parse reps
                                        int reps = 0;
                                        Object repsObj = exerciseData.get("reps");
                                        if (repsObj instanceof Number) {
                                            reps = ((Number) repsObj).intValue();
                                        } else if (repsObj instanceof String) {
                                            String repsStr = (String) repsObj;
                                            if (repsStr != null && !repsStr.isEmpty()) {
                                                try {
                                                    reps = Integer.parseInt(repsStr);
                                                } catch (NumberFormatException e) {
                                                    Log.w(TAG, "Could not parse reps string: '" + repsStr + "' for exercise '" 
                                                            + exerciseName + "' in workout '" + workoutName + "'");
                                                }
                                            }
                                        }

                                        // Get sets (typically a number)
                                        int sets = 0;
                                        Object setsObj = exerciseData.get("sets");
                                        if (setsObj instanceof Number) {
                                            sets = ((Number) setsObj).intValue();
                                        }

                                        if (exerciseName != null && !exerciseName.isEmpty()) {
                                            entry.setExerciseName(exerciseName);
                                        } else {
                                            Log.w(TAG, "Exercise in session " + sessionDocument.getId() + " missing exercise name.");
                                            continue; // Skip exercises with no name
                                        }

                                        // Set all collected values to the entry
                                        entry.setValue(weight);
                                        entry.setReps(reps);
                                        entry.setSets(sets);
                                        
                                        // Add notes if available
                                        String notes = (String) exerciseData.get("notes");
                                        if (notes != null) {
                                            entry.setNotes(notes);
                                        }
                                        
                                        // Add workout name as note or as extra context
                                        if (entry.getNotes() == null || entry.getNotes().isEmpty()) {
                                            entry.setNotes("From workout: " + workoutName);
                                        } else {
                                            entry.setNotes(entry.getNotes() + " (From workout: " + workoutName + ")");
                                        }

                                        allExerciseEntries.add(entry);
                                        Log.d(TAG, "Mapped exercise: Name=" + entry.getExerciseName() 
                                                + ", Weight=" + entry.getValue() 
                                                + ", Sets=" + entry.getSets() 
                                                + ", Reps=" + entry.getReps() 
                                                + ", Date=" + entry.getDate().toDate()
                                                + ", From workout=" + workoutName);
                                    }
                                } else {
                                    Log.w(TAG, "Workout session " + sessionDocument.getId() + " has no 'exercises' list or it's empty.");
                                }
                            }
                        }
                        
                        if (allExerciseEntries.isEmpty()) {
                            Log.d(TAG, "No exercises found in any workout sessions.");
                        }
                        
                        // Sort all collected individual exercise entries by date for the chart
                        allExerciseEntries.sort((e1, e2) -> e2.getDate().compareTo(e1.getDate())); // Descending
                        workoutLogEntriesLiveData.setValue(allExerciseEntries);
                        Log.d(TAG, "Fetched and mapped " + allExerciseEntries.size() + " individual exercise entries for chart.");
                        if (!allExerciseEntries.isEmpty()) {
                            Log.d(TAG, "First mapped individual entry for chart: Name=" + allExerciseEntries.get(0).getExerciseName() 
                                    + ", Value=" + allExerciseEntries.get(0).getValue()
                                    + ", Sets=" + allExerciseEntries.get(0).getSets()
                                    + ", Reps=" + allExerciseEntries.get(0).getReps());
                        }
                    } else {
                        Log.w(TAG, "Error getting workout history documents: ", task.getException());
                        errorLiveData.setValue("Error fetching workout logs: " + (task.getException() != null ? task.getException().getMessage() : "Unknown error"));
                        workoutLogEntriesLiveData.setValue(new ArrayList<>());
                    }
                });
    }

    public void saveProgressEntry(ProgressEntry entry) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            errorLiveData.setValue("User not logged in. Cannot save progress.");
            progressSaveSuccessLiveData.setValue(false);
            return;
        }
        String userId = currentUser.getUid();
        // Ensure the entry's userId is set, or matches the current user, if already set.
        // As per summary, AddProgressDialogFragment is expected to set this.
        // If entry.getUserId() is null, or doesn't match, you might want to enforce it here:
        // if (entry.getUserId() == null || !entry.getUserId().equals(userId)) {
        //     Log.w(TAG, "Mismatch or missing userId in ProgressEntry for save. Forcing current userId.");
        //     entry.setUserId(userId);
        // }


        isLoadingLiveData.setValue(true);
        // Path: progress/{userId}/entries/{new_entryId}
        db.collection("progress").document(userId).collection("entries")
                .add(entry)
                .addOnSuccessListener(documentReference -> {
                    isLoadingLiveData.setValue(false);
                    Log.d(TAG, "Progress entry saved with ID: " + documentReference.getId());
                    progressSaveSuccessLiveData.setValue(true);
                    // fetchProgressEntries(); // Data will be refreshed by the fragment observing save success
                })
                .addOnFailureListener(e -> {
                    isLoadingLiveData.setValue(false);
                    Log.w(TAG, "Error adding document", e);
                    errorLiveData.setValue("Failed to save progress: " + e.getMessage());
                    progressSaveSuccessLiveData.setValue(false);
                });
    }

    public void updateProgressEntry(ProgressEntry entry) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            errorLiveData.setValue("User not logged in. Cannot update progress.");
            progressUpdateSuccessLiveData.setValue(false);
            return;
        }
        if (entry.getEntryId() == null || entry.getEntryId().isEmpty()) {
            errorLiveData.setValue("Entry ID is missing. Cannot update progress.");
            progressUpdateSuccessLiveData.setValue(false);
            return;
        }
        String userId = currentUser.getUid();
        // Ensure the entry's userId is set and matches the current user for update.
        // As per summary, the entry object should have userId set.
        // if (entry.getUserId() == null || !entry.getUserId().equals(userId)) {
        //     Log.w(TAG, "Mismatch or missing userId in ProgressEntry for update. Forcing current userId.");
        //     entry.setUserId(userId); // Or handle as an error, depending on policy.
        // }
        isLoadingLiveData.setValue(true);

        // Path: progress/{userId}/entries/{entryId}
        db.collection("progress").document(userId).collection("entries").document(entry.getEntryId())
                .set(entry) // Overwrites the document with the new entry object
                .addOnSuccessListener(aVoid -> {
                    isLoadingLiveData.setValue(false);
                    Log.d(TAG, "Progress entry updated with ID: " + entry.getEntryId());
                    progressUpdateSuccessLiveData.setValue(true);
                })
                .addOnFailureListener(e -> {
                    isLoadingLiveData.setValue(false);
                    Log.w(TAG, "Error updating document", e);
                    errorLiveData.setValue("Failed to update progress: " + e.getMessage());
                    progressUpdateSuccessLiveData.setValue(false);
                });
    }

    public void deleteProgressEntry(String entryId) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            errorLiveData.setValue("User not logged in. Cannot delete progress.");
            progressDeleteSuccessLiveData.setValue(false);
            return;
        }
        if (entryId == null || entryId.isEmpty()) {
            errorLiveData.setValue("Entry ID is missing. Cannot delete progress.");
            progressDeleteSuccessLiveData.setValue(false);
            return;
        }
        String userId = currentUser.getUid();
        isLoadingLiveData.setValue(true);

        // Path: progress/{userId}/entries/{entryId}
        db.collection("progress").document(userId).collection("entries").document(entryId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    isLoadingLiveData.setValue(false);
                    Log.d(TAG, "Progress entry deleted with ID: " + entryId);
                    progressDeleteSuccessLiveData.setValue(true);
                })
                .addOnFailureListener(e -> {
                    isLoadingLiveData.setValue(false);
                    Log.w(TAG, "Error deleting document", e);
                    errorLiveData.setValue("Failed to delete progress: " + e.getMessage());
                    progressDeleteSuccessLiveData.setValue(false);
                });
    }

    // Methods to reset event LiveData
    public void resetProgressSaveSuccessEvent() {
        progressSaveSuccessLiveData.setValue(null);
    }

    public void resetErrorEvent() {
        errorLiveData.setValue(null);
    }

    public void resetProgressUpdateSuccessEvent() {
        progressUpdateSuccessLiveData.setValue(null);
    }

    public void resetProgressDeleteSuccessEvent() {
        progressDeleteSuccessLiveData.setValue(null);
    }
}
