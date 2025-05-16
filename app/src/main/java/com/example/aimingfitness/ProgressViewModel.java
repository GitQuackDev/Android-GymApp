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
import java.util.ArrayList;
import java.util.List;

public class ProgressViewModel extends AndroidViewModel {

    private static final String TAG = "ProgressViewModel";

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    private MutableLiveData<List<ProgressEntry>> progressEntriesLiveData = new MutableLiveData<>();
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

        db.collection("progress").document(userId).collection("entries")
                .whereEqualTo("userId", userId) // Added userId filter
                .orderBy("date", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    isLoadingLiveData.setValue(false);
                    if (task.isSuccessful()) {
                        List<ProgressEntry> entries = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            ProgressEntry entry = document.toObject(ProgressEntry.class);
                            entry.setEntryId(document.getId());
                            entries.add(entry);
                        }
                        progressEntriesLiveData.setValue(entries);
                    } else {
                        Log.w(TAG, "Error getting documents: ", task.getException());
                        errorLiveData.setValue("Error fetching progress: " + task.getException().getMessage());
                        progressEntriesLiveData.setValue(new ArrayList<>()); // Post empty list on error
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
