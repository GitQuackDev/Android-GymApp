package com.example.aimingfitness;

import java.io.Serializable;
import java.util.UUID;

public class ExerciseDetail implements Serializable {
    private String id;
    private String name;
    private int sets;
    private String reps; // e.g., "8-12", "15", "AMRAP"
    private String weight; // e.g., "50 kg", "Bodyweight", "10 lbs"
    private int restTimeSeconds; // Rest time after this exercise's sets
    private String notes;
    private boolean isWarmUp; // Optional: flag if this is a warm-up set/exercise

    public ExerciseDetail() {
        // Default constructor for Firestore
        this.id = UUID.randomUUID().toString(); // Generate ID on creation
    }

    public ExerciseDetail(String name, int sets, String reps, String weight, int restTimeSeconds, String notes, boolean isWarmUp) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.sets = sets;
        this.reps = reps;
        this.weight = weight;
        this.restTimeSeconds = restTimeSeconds;
        this.notes = notes;
        this.isWarmUp = isWarmUp;
    }

    // Getters
    public String getId() { return id; }
    public String getName() { return name; }
    public int getSets() { return sets; }
    public String getReps() { return reps; }
    public String getWeight() { return weight; }
    public int getRestTimeSeconds() { return restTimeSeconds; }
    public String getNotes() { return notes; }
    public boolean isWarmUp() { return isWarmUp; }

    // Setters
    public void setId(String id) { this.id = id; } // Mainly for Firestore
    public void setName(String name) { this.name = name; }
    public void setSets(int sets) { this.sets = sets; }
    public void setReps(String reps) { this.reps = reps; }
    public void setWeight(String weight) { this.weight = weight; }
    public void setRestTimeSeconds(int restTimeSeconds) { this.restTimeSeconds = restTimeSeconds; }
    public void setNotes(String notes) { this.notes = notes; }
    public void setWarmUp(boolean warmUp) { isWarmUp = warmUp; }

    @Override
    public String toString() {
        return "ExerciseDetail{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", sets=" + sets +
                ", reps='" + reps + '\'' +
                ", weight='" + weight + '\'' +
                ", restTimeSeconds=" + restTimeSeconds +
                ", notes='" + notes + '\'' +
                ", isWarmUp=" + isWarmUp +
                '}';
    }
}
