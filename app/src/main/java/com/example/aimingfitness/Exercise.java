package com.example.aimingfitness;

public class Exercise {
    private String name;
    private String sets;
    private String reps;
    private String restTime; // e.g., "60s"
    private String instructions;
    private String videoUrl; // Optional: for a demonstration video

    // Required empty public constructor for Firestore deserialization
    public Exercise() {}

    public Exercise(String name, String sets, String reps, String restTime, String instructions, String videoUrl) {
        this.name = name;
        this.sets = sets;
        this.reps = reps;
        this.restTime = restTime;
        this.instructions = instructions;
        this.videoUrl = videoUrl;
    }

    // Getters
    public String getName() { return name; }
    public String getSets() { return sets; }
    public String getReps() { return reps; }
    public String getRestTime() { return restTime; }
    public String getInstructions() { return instructions; }
    public String getVideoUrl() { return videoUrl; }

    // Setters
    public void setName(String name) { this.name = name; }
    public void setSets(String sets) { this.sets = sets; }
    public void setReps(String reps) { this.reps = reps; }
    public void setRestTime(String restTime) { this.restTime = restTime; }
    public void setInstructions(String instructions) { this.instructions = instructions; }
    public void setVideoUrl(String videoUrl) { this.videoUrl = videoUrl; }
}
