package com.example.aimingfitness;

import java.util.List;
import java.io.Serializable; // For passing between activities/fragments if needed
import com.example.aimingfitness.ExerciseDetail; // Added import

public class Workout implements Serializable {
    private String id;
    private String userId; // Added userId field
    private String name;
    private String description;
    private String type; // e.g., Strength, Cardio, Flexibility
    private String duration; // e.g., "30 minutes", "1 hour" - consider changing to int (minutes)
    private String difficulty; // e.g., Beginner, Intermediate, Advanced
    private String muscleGroups; // Comma-separated or a List<String>
    private List<ExerciseDetail> exercises; // Changed from String to List<ExerciseDetail>
    // Add a field for an image if you plan to display images for workouts
    // private String imageUrl;

    // Constructors
    public Workout() {
        // Default constructor required for calls to DataSnapshot.getValue(Workout.class) if using Firebase
    }

    // Updated constructor to accept List<ExerciseDetail>
    public Workout(String id, String userId, String name, String description, String type, String duration, String difficulty, String muscleGroups, List<ExerciseDetail> exercises) {
        this.id = id;
        this.userId = userId; // Initialize userId
        this.name = name;
        this.description = description;
        this.type = type;
        this.duration = duration;
        this.difficulty = difficulty;
        this.muscleGroups = muscleGroups;
        this.exercises = exercises;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() { // Added getter for userId
        return userId;
    }

    public void setUserId(String userId) { // Added setter for userId
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    public String getMuscleGroups() {
        return muscleGroups;
    }

    public void setMuscleGroups(String muscleGroups) {
        this.muscleGroups = muscleGroups;
    }

    // Updated getter for exercises
    public List<ExerciseDetail> getExercises() {
        return exercises;
    }

    // Updated setter for exercises
    public void setExercises(List<ExerciseDetail> exercises) {
        this.exercises = exercises;
    }

    // toString() might be useful for debugging
    @Override
    public String toString() {
        return "Workout{" +
                "id='" + id + '\'' +
                ", userId='" + userId + '\'' +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", type='" + type + '\'' +
                ", duration='" + duration + '\'' +
                ", difficulty='" + difficulty + '\'' +
                ", muscleGroups='" + muscleGroups + '\'' +
                ", exercises=" + (exercises != null ? exercises.toString() : "null") +
                '}';
    }
}
