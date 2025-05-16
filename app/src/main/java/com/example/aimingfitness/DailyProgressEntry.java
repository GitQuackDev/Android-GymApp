package com.example.aimingfitness;

import com.google.firebase.Timestamp;
import java.io.Serializable;
import java.util.Date;

/**
 * Model class for daily progress tracking, inspired by MyFitnessPal style.
 */
public class DailyProgressEntry implements Serializable {
    private String id;
    private String userId; // Added userId field
    private Date date;
    private Timestamp timestamp;
    
    // Meals data
    private Integer calories;
    
    // Macros data
    private Double carbs;
    private Double protein;
    private Double fat;
    
    // Steps data
    private Integer steps;
    private Integer stepsGoal;
    
    // Exercise data
    private String exerciseDuration;
    private Integer exerciseCaloriesBurned;
    private String exerciseName;
    
    // Additional notes
    private String notes;
    
    // Default constructor for Firebase
    public DailyProgressEntry() {
        timestamp = new Timestamp(new Date());
        date = timestamp.toDate();
    }
    
    // Constructor with required fields
    public DailyProgressEntry(String userId, Date date, Integer calories, Double carbs, Double protein, Double fat) { // Added userId
        this.userId = userId; // Added userId
        this.date = date;
        this.timestamp = new Timestamp(date);
        this.calories = calories;
        this.carbs = carbs;
        this.protein = protein;
        this.fat = fat;
    }
    
    // Full constructor
    public DailyProgressEntry(String userId, Date date, Integer calories, Double carbs, Double protein, Double fat, // Added userId
                              Integer steps, Integer stepsGoal, String exerciseDuration,
                              Integer exerciseCaloriesBurned, String exerciseName, String notes) {
        this.userId = userId; // Added userId
        this.date = date;
        this.timestamp = new Timestamp(date);
        this.calories = calories;
        this.carbs = carbs;
        this.protein = protein;
        this.fat = fat;
        this.steps = steps;
        this.stepsGoal = stepsGoal;
        this.exerciseDuration = exerciseDuration;
        this.exerciseCaloriesBurned = exerciseCaloriesBurned;
        this.exerciseName = exerciseName;
        this.notes = notes;
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
    
    public Date getDate() {
        return date;
    }
    
    public void setDate(Date date) {
        this.date = date;
        this.timestamp = new Timestamp(date);
    }
    
    public Timestamp getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
        this.date = timestamp.toDate();
    }
    
    public Integer getCalories() {
        return calories;
    }
    
    public void setCalories(Integer calories) {
        this.calories = calories;
    }
    
    public Double getCarbs() {
        return carbs;
    }
    
    public void setCarbs(Double carbs) {
        this.carbs = carbs;
    }
    
    public Double getProtein() {
        return protein;
    }
    
    public void setProtein(Double protein) {
        this.protein = protein;
    }
    
    public Double getFat() {
        return fat;
    }
    
    public void setFat(Double fat) {
        this.fat = fat;
    }
    
    public Integer getSteps() {
        return steps;
    }
    
    public void setSteps(Integer steps) {
        this.steps = steps;
    }
    
    public Integer getStepsGoal() {
        return stepsGoal;
    }
    
    public void setStepsGoal(Integer stepsGoal) {
        this.stepsGoal = stepsGoal;
    }
    
    public String getExerciseDuration() {
        return exerciseDuration;
    }
    
    public void setExerciseDuration(String exerciseDuration) {
        this.exerciseDuration = exerciseDuration;
    }
    
    public Integer getExerciseCaloriesBurned() {
        return exerciseCaloriesBurned;
    }
    
    public void setExerciseCaloriesBurned(Integer exerciseCaloriesBurned) {
        this.exerciseCaloriesBurned = exerciseCaloriesBurned;
    }
    
    public String getExerciseName() {
        return exerciseName;
    }
    
    public void setExerciseName(String exerciseName) {
        this.exerciseName = exerciseName;
    }
    
    public String getNotes() {
        return notes;
    }
    
    public void setNotes(String notes) {
        this.notes = notes;
    }
}
