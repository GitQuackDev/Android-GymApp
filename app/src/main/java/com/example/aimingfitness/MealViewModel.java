package com.example.aimingfitness;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

/**
 * ViewModel for handling meal data operations.
 */
public class MealViewModel extends ViewModel {
    private static final String TAG = "MealViewModel";
    
    // Firebase references
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final CollectionReference mealsCollection = db.collection("meals");
    private final FirebaseAuth auth = FirebaseAuth.getInstance();
    
    // LiveData to observe
    private final MutableLiveData<List<Meal>> mealsLiveData = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Boolean> isLoadingLiveData = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessageLiveData = new MutableLiveData<>();
    
    // Getters for LiveData
    public LiveData<List<Meal>> getMealsLiveData() {
        return mealsLiveData;
    }
    
    public LiveData<Boolean> getIsLoadingLiveData() {
        return isLoadingLiveData;
    }
    
    public LiveData<String> getErrorMessageLiveData() {
        return errorMessageLiveData;
    }
    
    /**
     * Load meals for the current user from Firestore
     */
    public void loadMeals() {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            errorMessageLiveData.setValue("You must be logged in to view meals");
            return;
        }
        
        isLoadingLiveData.setValue(true);
        
        String userId = currentUser.getUid();
        mealsCollection.whereEqualTo("userId", userId)
                .get()
                .addOnCompleteListener(task -> {
                    isLoadingLiveData.setValue(false);
                    
                    if (task.isSuccessful()) {
                        List<Meal> loadedMeals = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Meal meal = document.toObject(Meal.class);
                            meal.setMealId(document.getId());
                            loadedMeals.add(meal);
                        }
                        mealsLiveData.setValue(loadedMeals);
                    } else {
                        errorMessageLiveData.setValue("Error loading meals: " + task.getException().getMessage());
                    }
                });
    }
      /**
     * Add a new meal to Firestore
     */
    public void addMeal(Meal meal) {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            errorMessageLiveData.setValue("You must be logged in to add a meal");
            return;
        }
        
        isLoadingLiveData.setValue(true);
        
        // Ensure the meal has the current user's ID
        meal.setUserId(currentUser.getUid());
        
        mealsCollection.add(meal)
                .addOnSuccessListener(documentReference -> {
                    isLoadingLiveData.setValue(false);
                    // Refresh the meals list
                    loadMeals();
                })
                .addOnFailureListener(e -> {
                    isLoadingLiveData.setValue(false);
                    errorMessageLiveData.setValue("Error adding meal: " + e.getMessage());
                });
    }
    
    /**
     * Update an existing meal in Firestore
     */
    public void updateMeal(Meal meal) {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            errorMessageLiveData.setValue("You must be logged in to update a meal");
            return;
        }
        
        isLoadingLiveData.setValue(true);
        
        // Ensure the meal has the current user's ID
        meal.setUserId(currentUser.getUid());
        
        // Get the meal ID
        String mealId = meal.getMealId();
        if (mealId == null || mealId.isEmpty()) {
            isLoadingLiveData.setValue(false);
            errorMessageLiveData.setValue("Invalid meal ID for update");
            return;
        }
        
        mealsCollection.document(mealId)
                .set(meal)
                .addOnSuccessListener(aVoid -> {
                    isLoadingLiveData.setValue(false);
                    // Refresh the meals list
                    loadMeals();
                })
                .addOnFailureListener(e -> {
                    isLoadingLiveData.setValue(false);
                    errorMessageLiveData.setValue("Error updating meal: " + e.getMessage());
                });
    }
    
    /**
     * Delete a meal from Firestore
     */
    public void deleteMeal(String mealId) {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            errorMessageLiveData.setValue("You must be logged in to delete a meal");
            return;
        }
        
        if (mealId == null || mealId.isEmpty()) {
            errorMessageLiveData.setValue("Invalid meal ID for deletion");
            return;
        }
        
        isLoadingLiveData.setValue(true);
        
        mealsCollection.document(mealId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    isLoadingLiveData.setValue(false);
                    // Refresh the meals list
                    loadMeals();
                })
                .addOnFailureListener(e -> {
                    isLoadingLiveData.setValue(false);
                    errorMessageLiveData.setValue("Error deleting meal: " + e.getMessage());
                });
    }
    
    /**
     * Get a single meal by ID
     */
    public void getMealById(String mealId) {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            errorMessageLiveData.setValue("You must be logged in to view meal details");
            return;
        }
        
        if (mealId == null || mealId.isEmpty()) {
            errorMessageLiveData.setValue("Invalid meal ID");
            return;
        }
        
        isLoadingLiveData.setValue(true);
        
        mealsCollection.document(mealId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    isLoadingLiveData.setValue(false);
                    if (documentSnapshot.exists()) {
                        Meal meal = documentSnapshot.toObject(Meal.class);
                        if (meal != null) {
                            meal.setMealId(documentSnapshot.getId());
                            // Create a list with just this meal
                            List<Meal> singleMealList = new ArrayList<>();
                            singleMealList.add(meal);
                            mealsLiveData.setValue(singleMealList);
                        }
                    } else {
                        errorMessageLiveData.setValue("Meal not found");
                    }
                })
                .addOnFailureListener(e -> {
                    isLoadingLiveData.setValue(false);
                    errorMessageLiveData.setValue("Error retrieving meal: " + e.getMessage());
                });
    }
}
