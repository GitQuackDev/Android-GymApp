package com.example.aimingfitness.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.aimingfitness.ExerciseLibrary;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Repository class for handling exercise data operations between the app and Firestore
 */
public class ExerciseRepository {
    
    private static final String COLLECTION_EXERCISES = "exercises";
    private final FirebaseFirestore db;
    
    private final MutableLiveData<List<ExerciseLibrary>> exercisesLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    
    public ExerciseRepository() {
        db = FirebaseFirestore.getInstance();
    }
    
    /**
     * Fetch all exercises from Firestore database
     */
    public void fetchAllExercises() {
        isLoading.setValue(true);
        
        db.collection(COLLECTION_EXERCISES)
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                List<ExerciseLibrary> exercises = new ArrayList<>();
                
                for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                    ExerciseLibrary exercise = document.toObject(ExerciseLibrary.class);
                    exercises.add(exercise);
                }
                
                exercisesLiveData.setValue(exercises);
                isLoading.setValue(false);
            })
            .addOnFailureListener(e -> {
                errorMessage.setValue("Failed to fetch exercises: " + e.getMessage());
                isLoading.setValue(false);
                // Fallback to sample data on error
                exercisesLiveData.setValue(ExerciseLibrary.getSampleExercises());
            });
    }
    
    /**
     * Add a new exercise to the database
     */
    public void addExercise(ExerciseLibrary exercise) {
        isLoading.setValue(true);
        
        db.collection(COLLECTION_EXERCISES)
            .document(exercise.getId())
            .set(exercise)
            .addOnSuccessListener(aVoid -> {
                isLoading.setValue(false);
                // Refresh the exercise list
                fetchAllExercises();
            })
            .addOnFailureListener(e -> {
                errorMessage.setValue("Failed to add exercise: " + e.getMessage());
                isLoading.setValue(false);
            });
    }
    
    /**
     * Update an existing exercise
     */
    public void updateExercise(ExerciseLibrary exercise) {
        isLoading.setValue(true);
        
        db.collection(COLLECTION_EXERCISES)
            .document(exercise.getId())
            .set(exercise)
            .addOnSuccessListener(aVoid -> {
                isLoading.setValue(false);
                // Refresh the exercise list
                fetchAllExercises();
            })
            .addOnFailureListener(e -> {
                errorMessage.setValue("Failed to update exercise: " + e.getMessage());
                isLoading.setValue(false);
            });
    }
    
    /**
     * Delete an exercise from the database
     */
    public void deleteExercise(String exerciseId) {
        isLoading.setValue(true);
        
        db.collection(COLLECTION_EXERCISES)
            .document(exerciseId)
            .delete()
            .addOnSuccessListener(aVoid -> {
                isLoading.setValue(false);
                // Refresh the exercise list
                fetchAllExercises();
            })
            .addOnFailureListener(e -> {
                errorMessage.setValue("Failed to delete exercise: " + e.getMessage());
                isLoading.setValue(false);
            });
    }
    
    /**
     * Initialize the database with sample exercises if it's empty
     */
    public void initializeDatabaseIfEmpty() {
        db.collection(COLLECTION_EXERCISES)
            .limit(1)
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                if (queryDocumentSnapshots.isEmpty()) {
                    // Database is empty, populate with sample data
                    List<ExerciseLibrary> sampleExercises = ExerciseLibrary.getSampleExercises();
                    
                    for (ExerciseLibrary exercise : sampleExercises) {
                        addExercise(exercise);
                    }
                }
            });
    }
    
    /**
     * Return LiveData of exercises for observation
     */
    public LiveData<List<ExerciseLibrary>> getExercisesLiveData() {
        return exercisesLiveData;
    }
    
    /**
     * Return LiveData of loading state for observation
     */
    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }
    
    /**
     * Return LiveData of error messages for observation
     */
    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }
}
