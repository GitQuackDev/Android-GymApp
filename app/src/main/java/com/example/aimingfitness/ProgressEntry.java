package com.example.aimingfitness;

import com.google.firebase.Timestamp;
import java.io.Serializable; // Import Serializable

public class ProgressEntry implements Serializable { // Implement Serializable
    private String entryId; // Firestore document ID
    private String userId;
    private Timestamp date;
    private String type; // e.g., "Body Weight", "Workout Log", "Body Measurement"
    private double value;  // For weight, workout weight, measurement value
    private String unit;   // e.g., "kg", "lbs", "cm", "inches"

    // Fields specific to Workout Log
    private String exerciseName; // Also used for Measurement Area in Body Measurement type
    private int sets;
    private int reps;

    // Common field
    private String notes;

    // Constructors
    public ProgressEntry() {
        // Default constructor required for calls to DataSnapshot.getValue(ProgressEntry.class)
    }

    public ProgressEntry(String userId, Timestamp date, String type, double value, String unit, String exerciseName, int sets, int reps, String notes) {
        this.userId = userId;
        this.date = date;
        this.type = type;
        this.value = value;
        this.unit = unit;
        this.exerciseName = exerciseName;
        this.sets = sets;
        this.reps = reps;
        this.notes = notes;
    }

    // Getters and Setters
    public String getEntryId() {
        return entryId;
    }

    public void setEntryId(String entryId) {
        this.entryId = entryId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Timestamp getDate() {
        return date;
    }

    public void setDate(Timestamp date) {
        this.date = date;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getExerciseName() {
        return exerciseName;
    }

    public void setExerciseName(String exerciseName) {
        this.exerciseName = exerciseName;
    }

    public int getSets() {
        return sets;
    }

    public void setSets(int sets) {
        this.sets = sets;
    }

    public int getReps() {
        return reps;
    }

    public void setReps(int reps) {
        this.reps = reps;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
