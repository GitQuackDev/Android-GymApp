package com.example.aimingfitness;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class AddMealActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;

    private TextInputLayout tilMealName, tilMealDescription, tilMealType;
    private TextInputLayout tilCalories, tilProtein, tilCarbs, tilFats;
    private TextInputLayout tilIngredients, tilRecipe;
    
    private TextInputEditText etMealName, etMealDescription;
    private TextInputEditText etCalories, etProtein, etCarbs, etFats;
    private TextInputEditText etIngredients, etRecipe;
    private AutoCompleteTextView actMealType;
    
    private MaterialButton btnSelectMealImage, btnSaveMeal, btnCancel;
    private ImageView ivMealImagePreview;
    private ProgressBar progressLoading;
    
    private MealViewModel mealViewModel;
    private Uri imageUri;
    private String mealId;
    private boolean isEditMode = false;
    private Meal existingMeal;
    private boolean saveOperationInProgress = false; // Added flag

    private FirebaseStorage storage;
    private StorageReference storageRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_meal);
        
        // Initialize Firebase Storage
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();
        
        // Initialize ViewModel
        mealViewModel = new ViewModelProvider(this).get(MealViewModel.class);
        
        // Initialize UI components
        initializeViews();
        
        // Set toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        
        // Setup meal type dropdown
        String[] mealTypes = {"Breakfast", "Lunch", "Dinner", "Snack", "Dessert", "Other"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_dropdown_item_1line, mealTypes);
        actMealType.setAdapter(adapter);
        
        // Check if in edit mode
        if (getIntent().hasExtra("MEAL_ID") && getIntent().getBooleanExtra("IS_EDIT", false)) {
            isEditMode = true;
            mealId = getIntent().getStringExtra("MEAL_ID");
            getSupportActionBar().setTitle("Edit Meal");
            loadExistingMealDetails();
        } else {
            getSupportActionBar().setTitle("Add Meal");
        }
        
        // Set button click listeners
        btnSelectMealImage.setOnClickListener(v -> openImagePicker());
        
        btnSaveMeal.setOnClickListener(v -> {
            if (validateInput()) {
                saveMeal();
            }
        });
        
        btnCancel.setOnClickListener(v -> finish());

        // Observe changes to the meals list
        mealViewModel.getMealsLiveData().observe(this, meals -> {
            if (isEditMode && meals != null && !meals.isEmpty()) {
                existingMeal = meals.get(0);
                populateFields(existingMeal);
            }
        });

        mealViewModel.getIsLoadingLiveData().observe(this, isLoading -> {
            if (isLoading) {
                if (saveOperationInProgress) {
                    progressLoading.setVisibility(View.VISIBLE);
                }
            } else {
                // Loading has finished
                if (saveOperationInProgress) {
                    progressLoading.setVisibility(View.GONE); // Always hide progress when save op loading finishes
                    String currentError = mealViewModel.getErrorMessageLiveData().getValue();
                    if (currentError == null || currentError.isEmpty()) {
                        // If no error is currently set, assume success for the completed save operation.
                        Toast.makeText(AddMealActivity.this,
                                isEditMode ? "Meal updated successfully" : "Meal added successfully",
                                Toast.LENGTH_SHORT).show();
                        finish();
                    }
                    // If there was an error, the getErrorMessageLiveData observer handles UI, and we don't finish.
                    saveOperationInProgress = false; // Reset flag, operation concluded.
                }
            }
        });

        mealViewModel.getErrorMessageLiveData().observe(this, errorMsg -> {
            if (errorMsg != null && !errorMsg.isEmpty()) {
                // An error message has been posted.
                Toast.makeText(AddMealActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                if (saveOperationInProgress) {
                    // This error is related to the ongoing save operation.
                    btnSaveMeal.setEnabled(true);
                    progressLoading.setVisibility(View.GONE); // Ensure progress is hidden
                    saveOperationInProgress = false; // Reset flag, operation concluded with error.
                }
            }
        });
    }
    
    private void initializeViews() {
        tilMealName = findViewById(R.id.tilMealName);
        tilMealDescription = findViewById(R.id.tilMealDescription);
        tilMealType = findViewById(R.id.tilMealType);
        tilCalories = findViewById(R.id.tilCalories);
        tilProtein = findViewById(R.id.tilProtein);
        tilCarbs = findViewById(R.id.tilCarbs);
        tilFats = findViewById(R.id.tilFats);
        tilIngredients = findViewById(R.id.tilIngredients);
        tilRecipe = findViewById(R.id.tilRecipe);
        
        etMealName = findViewById(R.id.etMealName);
        etMealDescription = findViewById(R.id.etMealDescription);
        actMealType = findViewById(R.id.actMealType);
        etCalories = findViewById(R.id.etCalories);
        etProtein = findViewById(R.id.etProtein);
        etCarbs = findViewById(R.id.etCarbs);
        etFats = findViewById(R.id.etFats);
        etIngredients = findViewById(R.id.etIngredients);
        etRecipe = findViewById(R.id.etRecipe);
        
        btnSelectMealImage = findViewById(R.id.btnSelectMealImage);
        btnSaveMeal = findViewById(R.id.btnSaveMeal);
        btnCancel = findViewById(R.id.btnCancel);
        ivMealImagePreview = findViewById(R.id.ivMealImagePreview);
        progressLoading = findViewById(R.id.progressLoading);
    }
    
    private void loadExistingMealDetails() {
        if (mealId != null && !mealId.isEmpty()) {
            mealViewModel.getMealById(mealId);
        }
    }
    
    private void populateFields(Meal meal) {
        if (meal != null) {
            etMealName.setText(meal.getName());
            etMealDescription.setText(meal.getDescription());
            actMealType.setText(meal.getMealType(), false);
            etCalories.setText(String.valueOf(meal.getCalories()));
            etProtein.setText(String.valueOf(meal.getProtein()));
            etCarbs.setText(String.valueOf(meal.getCarbs()));
            etFats.setText(String.valueOf(meal.getFats()));
            
            // Handle ingredients list
            if (meal.getIngredients() != null && !meal.getIngredients().isEmpty()) {
                StringBuilder ingredientsBuilder = new StringBuilder();
                for (String ingredient : meal.getIngredients()) {
                    ingredientsBuilder.append(ingredient).append("\n");
                }
                etIngredients.setText(ingredientsBuilder.toString().trim());
            }
            
            etRecipe.setText(meal.getRecipe());
            
            // Load image if available
            if (meal.getImageUrl() != null && !meal.getImageUrl().isEmpty()) {
                Glide.with(this)
                    .load(meal.getImageUrl())
                    .placeholder(R.drawable.ic_placeholder_meal)
                    .into(ivMealImagePreview);
            }
        }
    }
    
    private void openImagePicker() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Meal Image"), PICK_IMAGE_REQUEST);
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null) {
            imageUri = data.getData();
            
            // Display selected image
            Glide.with(this)
                .load(imageUri)
                .placeholder(R.drawable.ic_placeholder_meal)
                .into(ivMealImagePreview);
        }
    }
    
    private boolean validateInput() {
        boolean isValid = true;
        
        // Validate meal name
        if (etMealName.getText().toString().trim().isEmpty()) {
            tilMealName.setError("Please enter a meal name");
            isValid = false;
        } else {
            tilMealName.setError(null);
        }
        
        // Validate meal type
        if (actMealType.getText().toString().trim().isEmpty()) {
            tilMealType.setError("Please select a meal type");
            isValid = false;
        } else {
            tilMealType.setError(null);
        }
        
        // Validate calories
        if (etCalories.getText().toString().trim().isEmpty()) {
            tilCalories.setError("Please enter calories");
            isValid = false;
        } else {
            tilCalories.setError(null);
        }
        
        return isValid;
    }
      private void saveMeal() {
        if (!validateInput()) {
            return;
        }

        progressLoading.setVisibility(View.VISIBLE);
        btnSaveMeal.setEnabled(false);
        saveOperationInProgress = true; // Mark that a save operation has started

        // Extract all values and make them final for lambda expressions
        final String name = etMealName.getText().toString().trim();
        final String description = etMealDescription.getText().toString().trim();
        final String mealType = actMealType.getText().toString().trim();
        
        // Add default values for numeric fields to avoid NumberFormatException
        final double calories;
        final double protein;
        final double carbs;
        final double fats;
        
        try {
            calories = !etCalories.getText().toString().trim().isEmpty() ? 
                Double.parseDouble(etCalories.getText().toString().trim()) : 0;
            protein = !etProtein.getText().toString().trim().isEmpty() ? 
                Double.parseDouble(etProtein.getText().toString().trim()) : 0;
            carbs = !etCarbs.getText().toString().trim().isEmpty() ? 
                Double.parseDouble(etCarbs.getText().toString().trim()) : 0;
            fats = !etFats.getText().toString().trim().isEmpty() ? 
                Double.parseDouble(etFats.getText().toString().trim()) : 0;
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Please enter valid numeric values", Toast.LENGTH_LONG).show();
            progressLoading.setVisibility(View.GONE);
            btnSaveMeal.setEnabled(true);
            saveOperationInProgress = false; // Reset flag
            return;
        }
        
        final List<String> ingredients = Arrays.asList(etIngredients.getText().toString().split("\\\\n"));
        final String recipe = etRecipe.getText().toString().trim();
        final String userId = FirebaseAuth.getInstance().getCurrentUser() != null ? 
            FirebaseAuth.getInstance().getCurrentUser().getUid() : null;

        if (userId == null) {
            Toast.makeText(this, "User not logged in.", Toast.LENGTH_SHORT).show();
            progressLoading.setVisibility(View.GONE);
            btnSaveMeal.setEnabled(true);
            saveOperationInProgress = false; // Reset flag
            return;
        }

        // Handle image locally instead of using Firebase Storage
        if (imageUri != null) {
            // Instead of uploading to Firebase, store the URI string
            final String localImageUri = imageUri.toString();
            saveOrUpdateMeal(userId, name, description, localImageUri,
                    calories, protein, carbs, fats, ingredients, recipe, mealType);
        } else if (isEditMode && existingMeal != null && existingMeal.getImageUrl() != null) {
            // Use existing image URL if not changed
            saveOrUpdateMeal(userId, name, description, existingMeal.getImageUrl(),
                    calories, protein, carbs, fats, ingredients, recipe, mealType);
        } else {
            // No image selected
            saveOrUpdateMeal(userId, name, description, null,
                    calories, protein, carbs, fats, ingredients, recipe, mealType);
        }
    }
    
    private void saveOrUpdateMeal(String userId, String name, String description, String imageUrl,
                                 double calories, double protein, double carbs, double fats,
                                 List<String> ingredients, String recipe, String mealType) {

        Meal meal = new Meal(userId, name, description, imageUrl, calories, protein, carbs, fats, ingredients, recipe, mealType);

        if (isEditMode) {
            meal.setMealId(mealId);
            mealViewModel.updateMeal(meal);
        } else {
            mealViewModel.addMeal(meal);
        }
        // Removed the observer from here that was causing issues.
        // Success/error handling and finish() are now managed by observers in onCreate.
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
