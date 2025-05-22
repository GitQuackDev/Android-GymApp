package com.example.aimingfitness;

import java.util.List;
import java.util.Map;

public class Meal {
    private String mealId;
    private String userId; // Added userId field
    private String name;
    private String description;
    private String imageUrl;
    private double calories;
    private double protein;
    private double carbs;
    private double fats;
    private List<String> ingredients;
    private String recipe; // Instructions or steps
    private String mealType; // e.g., Breakfast, Lunch, Dinner, Snack

    // Required empty public constructor for Firestore deserialization
    public Meal() {}

    public Meal(String userId, String name, String description, String imageUrl, double calories, double protein, double carbs, double fats, List<String> ingredients, String recipe, String mealType) {
        this.userId = userId;
        this.name = name;
        this.description = description;
        this.imageUrl = imageUrl;
        this.calories = calories;
        this.protein = protein;
        this.carbs = carbs;
        this.fats = fats;
        this.ingredients = ingredients;
        this.recipe = recipe;
        this.mealType = mealType;
    }

    // Getters
    public String getMealId() { return mealId; }
    public String getUserId() { return userId; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getImageUrl() { return imageUrl; }
    public double getCalories() { return calories; }
    public double getProtein() { return protein; }
    public double getCarbs() { return carbs; }
    public double getFats() { return fats; }
    public List<String> getIngredients() { return ingredients; }
    public String getRecipe() { return recipe; }
    public String getMealType() { return mealType; }

    // Setters
    public void setMealId(String mealId) { this.mealId = mealId; } // For Firestore document ID
    public void setUserId(String userId) { this.userId = userId; } // Added setter for userId
    public void setName(String name) { this.name = name; }
    public void setDescription(String description) { this.description = description; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public void setCalories(double calories) { this.calories = calories; }
    public void setProtein(double protein) { this.protein = protein; }
    public void setCarbs(double carbs) { this.carbs = carbs; }
    public void setFats(double fats) { this.fats = fats; }
    public void setIngredients(List<String> ingredients) { this.ingredients = ingredients; }
    public void setRecipe(String recipe) { this.recipe = recipe; }
    public void setMealType(String mealType) { this.mealType = mealType; }
}
