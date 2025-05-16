package com.example.aimingfitness;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ExerciseLibrary {
    private String id;
    private String name;
    private String primaryMuscleGroup;
    private List<String> secondaryMuscleGroups;
    private List<String> equipment;
    private String instructions;
    private String imageUrl;

    public ExerciseLibrary() {
        // Required empty constructor for Firebase
    }

    public ExerciseLibrary(String id, String name, String primaryMuscleGroup, List<String> secondaryMuscleGroups, 
                   List<String> equipment, String instructions, String imageUrl) {
        this.id = id;
        this.name = name;
        this.primaryMuscleGroup = primaryMuscleGroup;
        this.secondaryMuscleGroups = secondaryMuscleGroups;
        this.equipment = equipment;
        this.instructions = instructions;
        this.imageUrl = imageUrl;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPrimaryMuscleGroup() {
        return primaryMuscleGroup;
    }

    public void setPrimaryMuscleGroup(String primaryMuscleGroup) {
        this.primaryMuscleGroup = primaryMuscleGroup;
    }

    public List<String> getSecondaryMuscleGroups() {
        return secondaryMuscleGroups;
    }

    public void setSecondaryMuscleGroups(List<String> secondaryMuscleGroups) {
        this.secondaryMuscleGroups = secondaryMuscleGroups;
    }

    public List<String> getEquipment() {
        return equipment;
    }

    public void setEquipment(List<String> equipment) {
        this.equipment = equipment;
    }

    public String getInstructions() {
        return instructions;
    }

    public void setInstructions(String instructions) {
        this.instructions = instructions;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getFormattedMuscles() {
        StringBuilder sb = new StringBuilder();
        sb.append("Target: ").append(primaryMuscleGroup);
        
        if (secondaryMuscleGroups != null && !secondaryMuscleGroups.isEmpty()) {
            sb.append(" • Secondary: ");
            sb.append(String.join(", ", secondaryMuscleGroups));
        }
        
        return sb.toString();
    }

    public String getFormattedEquipment() {
        if (equipment == null || equipment.isEmpty()) {
            return "Equipment: None/Bodyweight";
        }
        return "Equipment: " + String.join(", ", equipment);
    }

    /**
     * Creates a sample list of exercises for demonstration purposes
     */
    public static List<ExerciseLibrary> getSampleExercises() {
        List<ExerciseLibrary> exercises = new ArrayList<>();
        
        // Chest exercises
        exercises.add(new ExerciseLibrary(
            "bench_press", 
            "Bench Press", 
            "Chest", 
            Arrays.asList("Triceps", "Shoulders"), 
            Arrays.asList("Barbell", "Bench"), 
            "Lie on a bench, grip the bar with hands slightly wider than shoulder width, lower the bar to your chest, and push it back up.",
            "bench_press_image"
        ));
        
        exercises.add(new ExerciseLibrary(
            "incline_bench_press", 
            "Incline Bench Press", 
            "Chest", 
            Arrays.asList("Triceps", "Shoulders"), 
            Arrays.asList("Barbell", "Incline Bench"), 
            "Similar to the flat bench press but performed on an incline bench to target the upper chest more.",
            "incline_press_image"
        ));
        
        exercises.add(new ExerciseLibrary(
            "chest_dips", 
            "Chest Dips", 
            "Chest", 
            Arrays.asList("Triceps", "Shoulders"), 
            Arrays.asList("Dip Bars"), 
            "Support your weight on parallel bars, lean forward, lower your body by bending your arms, then push back up.",
            "chest_dips_image"
        ));
        
        exercises.add(new ExerciseLibrary(
            "push_ups", 
            "Push-Ups", 
            "Chest", 
            Arrays.asList("Triceps", "Shoulders"), 
            Arrays.asList(), 
            "Start in a plank position, lower your body by bending your elbows, then push back up.",
            "push_ups_image"
        ));
        
        // Back exercises
        exercises.add(new ExerciseLibrary(
            "pull_ups", 
            "Pull-Ups", 
            "Back", 
            Arrays.asList("Biceps"), 
            Arrays.asList("Pull-Up Bar"), 
            "Hang from a bar with an overhand grip, pull yourself up until your chin is over the bar, then lower yourself.",
            "pull_ups_image"
        ));
        
        exercises.add(new ExerciseLibrary(
            "barbell_rows", 
            "Barbell Rows", 
            "Back", 
            Arrays.asList("Biceps", "Shoulders"), 
            Arrays.asList("Barbell"), 
            "Bend at your hips, keep your back straight, pull a barbell to your lower chest, then lower it.",
            "barbell_rows_image"
        ));
        
        exercises.add(new ExerciseLibrary(
            "lat_pulldowns", 
            "Lat Pulldowns", 
            "Back", 
            Arrays.asList("Biceps"), 
            Arrays.asList("Cable Machine", "Lat Bar"), 
            "Sit at a lat pulldown machine, grip the bar with hands wider than shoulder width, pull the bar down to your chest, then let it rise slowly.",
            "lat_pulldowns_image"
        ));
        
        // Legs exercises
        exercises.add(new ExerciseLibrary(
            "squats", 
            "Squats", 
            "Legs", 
            Arrays.asList("Glutes", "Lower Back"), 
            Arrays.asList("Barbell", "Squat Rack"), 
            "Stand with a barbell on your upper back, bend your knees and hips to lower your body, then stand back up.",
            "squats_image"
        ));
        
        exercises.add(new ExerciseLibrary(
            "deadlifts", 
            "Deadlifts", 
            "Legs", 
            Arrays.asList("Lower Back", "Glutes"), 
            Arrays.asList("Barbell"), 
            "Stand with feet shoulder-width apart, bend at the hips and knees to grab a barbell, then stand up by extending your hips and knees.",
            "deadlifts_image"
        ));
        
        exercises.add(new ExerciseLibrary(
            "leg_press", 
            "Leg Press", 
            "Legs", 
            Arrays.asList("Glutes"), 
            Arrays.asList("Leg Press Machine"), 
            "Sit in a leg press machine, push the platform away by extending your knees, then lower it back slowly.",
            "leg_press_image"
        ));
        
        exercises.add(new ExerciseLibrary(
            "lunges", 
            "Lunges", 
            "Legs", 
            Arrays.asList("Glutes"), 
            Arrays.asList("Dumbbells"), 
            "Step forward with one leg, lower your body until both knees are bent at 90-degree angles, then push back to the starting position.",
            "lunges_image"
        ));
        
        // Shoulders exercises
        exercises.add(new ExerciseLibrary(
            "overhead_press", 
            "Overhead Press", 
            "Shoulders", 
            Arrays.asList("Triceps"), 
            Arrays.asList("Barbell"), 
            "Stand with a barbell at shoulder height, press it overhead until your arms are fully extended, then lower it back.",
            "overhead_press_image"
        ));
        
        exercises.add(new ExerciseLibrary(
            "lateral_raises", 
            "Lateral Raises", 
            "Shoulders", 
            Arrays.asList(), 
            Arrays.asList("Dumbbells"), 
            "Stand with dumbbells at your sides, raise them out to the sides until they're at shoulder level, then lower them back.",
            "lateral_raises_image"
        ));
        
        // Arms exercises
        exercises.add(new ExerciseLibrary(
            "bicep_curls", 
            "Bicep Curls", 
            "Arms", 
            Arrays.asList(), 
            Arrays.asList("Dumbbells"), 
            "Stand with dumbbells at your sides, palms facing forward, curl the weights up toward your shoulders, then lower them back.",
            "bicep_curls_image"
        ));
        
        exercises.add(new ExerciseLibrary(
            "tricep_extensions", 
            "Tricep Extensions", 
            "Arms", 
            Arrays.asList(), 
            Arrays.asList("Dumbbell"), 
            "Hold a dumbbell with both hands above your head, lower it behind your head by bending your elbows, then extend your arms.",
            "tricep_extensions_image"
        ));
        
        // Abs exercises
        exercises.add(new ExerciseLibrary(
            "crunches", 
            "Crunches", 
            "Abs", 
            Arrays.asList(), 
            Arrays.asList(), 
            "Lie on your back with knees bent, feet flat on the floor, hands behind your head, lift your shoulders toward your knees, then lower back down.",
            "crunches_image"
        ));
        
        exercises.add(new ExerciseLibrary(
            "planks", 
            "Planks", 
            "Abs", 
            Arrays.asList("Shoulders"), 
            Arrays.asList(), 
            "Get in a push-up position but rest on your forearms, hold your body in a straight line from head to heels.",
            "planks_image"
        ));
        
        // Cardio exercises
        exercises.add(new ExerciseLibrary(
            "running", 
            "Running", 
            "Cardio", 
            Arrays.asList("Legs"), 
            Arrays.asList("Treadmill"), 
            "Run at a steady pace for a set duration or distance.",
            "running_image"
        ));
        
        exercises.add(new ExerciseLibrary(
            "cycling", 
            "Cycling", 
            "Cardio", 
            Arrays.asList("Legs"), 
            Arrays.asList("Stationary Bike"), 
            "Pedal on a stationary bike at various resistance levels and speeds.",
            "cycling_image"
        ));
        
        return exercises;
    }
}
