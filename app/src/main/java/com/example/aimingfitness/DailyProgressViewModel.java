package com.example.aimingfitness;

import android.util.Log;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.google.firebase.auth.FirebaseAuth; // Added import
import com.google.firebase.auth.FirebaseUser; // Added import
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * ViewModel for handling daily progress data operations.
 */
public class DailyProgressViewModel extends ViewModel {
    private static final String TAG = "DailyProgressViewModel";
    
    // Firebase references
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final CollectionReference dailyProgressCollection = db.collection("dailyProgress");
    private final FirebaseAuth mAuth = FirebaseAuth.getInstance(); // Added FirebaseAuth instance
    
    // LiveData
    private final MutableLiveData<List<DailyProgressEntry>> dailyProgressEntries = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>(null);
    private final MutableLiveData<Boolean> operationSuccessful = new MutableLiveData<>(false);
    
    // Public accessors for LiveData
    public LiveData<List<DailyProgressEntry>> getDailyProgressEntries() {
        return dailyProgressEntries;
    }
    
    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }
    
    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }
    
    public LiveData<Boolean> getOperationSuccessful() {
        return operationSuccessful;
    }
    
    // Fetch all progress entries, ordered by date (newest first)
    public void fetchDailyProgressEntries() {
        isLoading.setValue(true);
        errorMessage.setValue(null);
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            errorMessage.setValue("User not authenticated. Cannot fetch daily progress.");
            dailyProgressEntries.setValue(new ArrayList<>());
            isLoading.setValue(false);
            Log.w(TAG, "User not authenticated.");
            return;
        }
        String userId = currentUser.getUid();
        
        dailyProgressCollection
            .whereEqualTo("userId", userId) // Added userId filter
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .addOnCompleteListener(task -> {
                isLoading.setValue(false);
                
                if (task.isSuccessful()) {
                    List<DailyProgressEntry> entries = new ArrayList<>();
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        DailyProgressEntry entry = document.toObject(DailyProgressEntry.class);
                        entry.setId(document.getId());
                        entries.add(entry);
                    }
                    dailyProgressEntries.setValue(entries);
                } else {
                    errorMessage.setValue("Error fetching daily progress entries: " + 
                                        (task.getException() != null ? task.getException().getMessage() : "Unknown error"));
                    Log.e(TAG, "Error fetching daily progress entries", task.getException());
                }
            });
    }
    
    // Save a new progress entry
    public void saveDailyProgressEntry(DailyProgressEntry entry) {
        // Assuming userId is already set on the entry object by the calling code
        isLoading.setValue(true);
        errorMessage.setValue(null);
        operationSuccessful.setValue(false);
        
        dailyProgressCollection
            .add(entry)
            .addOnSuccessListener(documentReference -> {
                isLoading.setValue(false);
                operationSuccessful.setValue(true);
                
                // Refresh the list with latest data
                fetchDailyProgressEntries();
            })
            .addOnFailureListener(e -> {
                isLoading.setValue(false);
                errorMessage.setValue("Failed to save progress: " + e.getMessage());
                Log.e(TAG, "Error saving progress entry", e);
            });
    }
    
    // Update an existing progress entry
    public void updateDailyProgressEntry(DailyProgressEntry entry) {
        // Assuming userId is already set on the entry object by the calling code
        if (entry.getId() == null || entry.getId().isEmpty()) {
            errorMessage.setValue("Cannot update entry: Missing ID");
            return;
        }
        
        isLoading.setValue(true);
        errorMessage.setValue(null);
        operationSuccessful.setValue(false);
        
        dailyProgressCollection
            .document(entry.getId())
            .set(entry)
            .addOnSuccessListener(aVoid -> {
                isLoading.setValue(false);
                operationSuccessful.setValue(true);
                
                // Refresh the list with latest data
                fetchDailyProgressEntries();
            })
            .addOnFailureListener(e -> {
                isLoading.setValue(false);
                errorMessage.setValue("Failed to update progress: " + e.getMessage());
                Log.e(TAG, "Error updating progress entry", e);
            });
    }
    
    // Delete a progress entry
    public void deleteDailyProgressEntry(String entryId) {
        // Deletion might need to be re-evaluated based on security rules,
        // but for now, it operates directly with entryId.
        // If rules restrict deletion to owners, this method might need userId as well,
        // or rules need to allow deletion by ID if the record contains the userId.
        if (entryId == null || entryId.isEmpty()) {
            errorMessage.setValue("Cannot delete entry: Missing ID");
            return;
        }
        
        isLoading.setValue(true);
        errorMessage.setValue(null);
        operationSuccessful.setValue(false);
        
        dailyProgressCollection
            .document(entryId)
            .delete()
            .addOnSuccessListener(aVoid -> {
                isLoading.setValue(false);
                operationSuccessful.setValue(true);
                
                // Refresh the list with latest data
                fetchDailyProgressEntries();
            })
            .addOnFailureListener(e -> {
                isLoading.setValue(false);
                errorMessage.setValue("Failed to delete progress: " + e.getMessage());
                Log.e(TAG, "Error deleting progress entry", e);
            });
    }
    
    // Find entries for a specific date range
    public void fetchEntriesByDateRange(Date startDate, Date endDate) {
        isLoading.setValue(true);
        errorMessage.setValue(null);
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            errorMessage.setValue("User not authenticated. Cannot fetch entries by date range.");
            dailyProgressEntries.setValue(new ArrayList<>());
            isLoading.setValue(false);
            Log.w(TAG, "User not authenticated.");
            return;
        }
        String userId = currentUser.getUid();
        
        dailyProgressCollection
            .whereEqualTo("userId", userId) // Added userId filter
            .whereGreaterThanOrEqualTo("date", startDate)
            .whereLessThanOrEqualTo("date", endDate)
            .orderBy("date", Query.Direction.DESCENDING)
            .get()
            .addOnCompleteListener(task -> {
                isLoading.setValue(false);
                
                if (task.isSuccessful()) {
                    List<DailyProgressEntry> entries = new ArrayList<>();
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        DailyProgressEntry entry = document.toObject(DailyProgressEntry.class);
                        entry.setId(document.getId());
                        entries.add(entry);
                    }
                    dailyProgressEntries.setValue(entries);
                } else {
                    errorMessage.setValue("Error fetching entries by date range: " + 
                                        (task.getException() != null ? task.getException().getMessage() : "Unknown error"));
                    Log.e(TAG, "Error fetching entries by date range", task.getException());
                }
            });
    }
}
