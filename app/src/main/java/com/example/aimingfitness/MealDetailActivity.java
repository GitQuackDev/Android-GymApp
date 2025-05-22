package com.example.aimingfitness;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;

public class MealDetailActivity extends AppCompatActivity {

    private MealViewModel mealViewModel;
    private String mealId;
    private Meal currentMeal;
    
    // UI components
    private CollapsingToolbarLayout collapsingToolbar;
    private ImageView ivMealImage;
    private TextView tvMealDescription;
    private TextView tvCalories, tvProtein, tvCarbs, tvFats;
    private LinearLayout llIngredients;
    private TextView tvRecipe;
    private FloatingActionButton fabEditMeal;
    private ProgressBar progressLoading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meal_detail);
        
        // Initialize UI components
        initViews();
        
        // Set up toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        
        // Get meal ID from intent
        if (getIntent() != null && getIntent().hasExtra("MEAL_ID")) {
            mealId = getIntent().getStringExtra("MEAL_ID");
        } else {
            Toast.makeText(this, "Meal not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        // Initialize ViewModel
        mealViewModel = new ViewModelProvider(this).get(MealViewModel.class);
        
        // Set up observers
        setupObservers();
        
        // Set up FAB click listener
        fabEditMeal.setOnClickListener(v -> {
            if (currentMeal != null) {
                Intent intent = new Intent(MealDetailActivity.this, AddMealActivity.class);
                intent.putExtra("MEAL_ID", mealId);
                intent.putExtra("IS_EDIT", true);
                startActivity(intent);
            }
        });
        
        // Load meal data
        loadMealDetails();
    }
    
    private void initViews() {
        collapsingToolbar = findViewById(R.id.collapsingToolbar);
        ivMealImage = findViewById(R.id.ivMealImage);
        tvMealDescription = findViewById(R.id.tvMealDescription);
        tvCalories = findViewById(R.id.tvCalories);
        tvProtein = findViewById(R.id.tvProtein);
        tvCarbs = findViewById(R.id.tvCarbs);
        tvFats = findViewById(R.id.tvFats);
        llIngredients = findViewById(R.id.llIngredients);
        tvRecipe = findViewById(R.id.tvRecipe);
        fabEditMeal = findViewById(R.id.fabEditMeal);
        progressLoading = findViewById(R.id.progressLoading);
    }
    
    private void setupObservers() {
        // Observe meals list changes
        mealViewModel.getMealsLiveData().observe(this, meals -> {
            if (meals != null && !meals.isEmpty()) {
                // Since we're querying by ID, we should only get one meal
                currentMeal = meals.get(0);
                displayMealDetails(currentMeal);
            }
        });
        
        // Observe loading state
        mealViewModel.getIsLoadingLiveData().observe(this, isLoading -> {
            progressLoading.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        });
        
        // Observe error messages
        mealViewModel.getErrorMessageLiveData().observe(this, errorMsg -> {
            if (errorMsg != null && !errorMsg.isEmpty()) {
                Toast.makeText(MealDetailActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                if (errorMsg.contains("not found")) {
                    finish();
                }
            }
        });
    }
    
    private void loadMealDetails() {
        if (mealId != null && !mealId.isEmpty()) {
            mealViewModel.getMealById(mealId);
        } else {
            Toast.makeText(this, "Invalid meal ID", Toast.LENGTH_SHORT).show();
            finish();
        }
    }
    
    private void displayMealDetails(Meal meal) {
        currentMeal = meal; // Keep a reference to the current meal

        collapsingToolbar.setTitle(meal.getName());

        if (meal.getImageUrl() != null && !meal.getImageUrl().isEmpty()) {
            Glide.with(this)
                    .load(meal.getImageUrl())
                    .placeholder(R.drawable.ic_placeholder_meal) // Add a placeholder drawable
                    .error(R.drawable.ic_placeholder_meal) // Add an error drawable
                    .into(ivMealImage);
        } else {
            ivMealImage.setImageResource(R.drawable.ic_placeholder_meal); // Default placeholder
        }

        tvMealDescription.setText(meal.getDescription());
        tvCalories.setText(String.format("%.0f Cal", meal.getCalories()));
        tvProtein.setText(String.format("P: %.0fg", meal.getProtein()));
        tvCarbs.setText(String.format("C: %.0fg", meal.getCarbs()));
        tvFats.setText(String.format("F: %.0fg", meal.getFats()));

        // Display ingredients
        llIngredients.removeAllViews(); // Clear previous ingredients
        if (meal.getIngredients() != null && !meal.getIngredients().isEmpty()) {
            for (String ingredient : meal.getIngredients()) {
                TextView tvIngredient = new TextView(this);
                tvIngredient.setText("• " + ingredient);
                tvIngredient.setTextAppearance(com.google.android.material.R.style.TextAppearance_MaterialComponents_Body2);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
                // Corrected to use margin_small
                params.setMargins(0, 0, 0, getResources().getDimensionPixelSize(R.dimen.margin_small)); // Add bottom margin
                tvIngredient.setLayoutParams(params);
                llIngredients.addView(tvIngredient);
            }
        } else {
            TextView tvNoIngredients = new TextView(this);
            tvNoIngredients.setText("No ingredients listed.");
            tvNoIngredients.setTextAppearance(this, com.google.android.material.R.style.TextAppearance_MaterialComponents_Body2);
            llIngredients.addView(tvNoIngredients);
        }

        tvRecipe.setText(meal.getRecipe() != null && !meal.getRecipe().isEmpty() ? meal.getRecipe() : "No recipe provided.");

        progressLoading.setVisibility(View.GONE);
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        // Reload meal details in case they were updated
        loadMealDetails();
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
