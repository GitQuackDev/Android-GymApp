package com.example.aimingfitness.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.aimingfitness.ExerciseLibrary;
import com.example.aimingfitness.repository.ExerciseRepository;

import java.util.ArrayList;
import java.util.List;

public class ExerciseLibraryViewModel extends ViewModel {

    private final ExerciseRepository exerciseRepository;
    private final MutableLiveData<List<ExerciseLibrary>> filteredExercises = new MutableLiveData<>(new ArrayList<>());
    private List<ExerciseLibrary> allExercises = new ArrayList<>();
    
    public ExerciseLibraryViewModel() {
        exerciseRepository = new ExerciseRepository();
        
        // Initialize repository and load data
        exerciseRepository.initializeDatabaseIfEmpty();
        loadExercises();
        
        // Observe repository data
        exerciseRepository.getExercisesLiveData().observeForever(exercises -> {
            allExercises = exercises;
            filteredExercises.setValue(exercises);
        });
    }
    
    /**
     * Reload exercises from repository
     */
    public void loadExercises() {
        exerciseRepository.fetchAllExercises();
    }
    
    /**
     * Add a new exercise
     */
    public void addExercise(ExerciseLibrary exercise) {
        exerciseRepository.addExercise(exercise);
    }
    
    /**
     * Update an existing exercise
     */
    public void updateExercise(ExerciseLibrary exercise) {
        exerciseRepository.updateExercise(exercise);
    }
    
    /**
     * Delete an exercise
     */
    public void deleteExercise(String exerciseId) {
        exerciseRepository.deleteExercise(exerciseId);
    }
    
    /**
     * Filter exercises by search query and muscle group
     */
    public void filterExercises(String searchQuery, String muscleGroup) {
        List<ExerciseLibrary> filtered = new ArrayList<>();
        
        for (ExerciseLibrary exercise : allExercises) {
            // Search query filter
            boolean matchesQuery = searchQuery.isEmpty() || 
                    exercise.getName().toLowerCase().contains(searchQuery.toLowerCase());
            
            // Muscle group filter
            boolean matchesMuscleGroup = muscleGroup.equals("All") || 
                    exercise.getPrimaryMuscleGroup().equals(muscleGroup);
            
            if (matchesQuery && matchesMuscleGroup) {
                filtered.add(exercise);
            }
        }
        
        filteredExercises.setValue(filtered);
    }
    
    /**
     * Get all exercises as LiveData for observation
     */
    public LiveData<List<ExerciseLibrary>> getExercises() {
        return filteredExercises;
    }
    
    /**
     * Get loading state as LiveData for observation
     */
    public LiveData<Boolean> getIsLoading() {
        return exerciseRepository.getIsLoading();
    }
    
    /**
     * Get error messages as LiveData for observation
     */
    public LiveData<String> getErrorMessage() {
        return exerciseRepository.getErrorMessage();
    }
}
