package com.example.aimingfitness;

import android.content.Intent;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

public class MealsFragment extends Fragment implements MealAdapter.OnMealItemClickListener {

    private static final String TAG = "MealsFragment";

    private RecyclerView rvMeals;
    private MealAdapter mealAdapter;
    private List<Meal> mealList;
    private MealViewModel mealViewModel;
    private FloatingActionButton fabAddMeal;
    private ProgressBar progressLoading;
    private TextView tvNoMeals;

    public MealsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_meals, container, false);

        // Initialize views
        rvMeals = view.findViewById(R.id.rvMeals);
        fabAddMeal = view.findViewById(R.id.fabAddMeal);
        progressLoading = view.findViewById(R.id.progressLoading);
        tvNoMeals = view.findViewById(R.id.tvNoMeals);

        // Set up RecyclerView
        rvMeals.setLayoutManager(new LinearLayoutManager(getContext()));
        mealList = new ArrayList<>();
        mealAdapter = new MealAdapter(getContext(), mealList, this);
        rvMeals.setAdapter(mealAdapter);

        // Initialize ViewModel
        mealViewModel = new ViewModelProvider(this).get(MealViewModel.class);
        
        // Observe changes to the meals list
        mealViewModel.getMealsLiveData().observe(getViewLifecycleOwner(), meals -> {
            mealList.clear();
            if (meals != null && !meals.isEmpty()) {
                mealList.addAll(meals);
                tvNoMeals.setVisibility(View.GONE);
                rvMeals.setVisibility(View.VISIBLE);
            } else {
                tvNoMeals.setVisibility(View.VISIBLE);
                rvMeals.setVisibility(View.GONE);
            }
            mealAdapter.notifyDataSetChanged();
        });

        // Observe loading state
        mealViewModel.getIsLoadingLiveData().observe(getViewLifecycleOwner(), isLoading -> {
            progressLoading.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        });

        // Observe error messages
        mealViewModel.getErrorMessageLiveData().observe(getViewLifecycleOwner(), errorMsg -> {
            if (errorMsg != null && !errorMsg.isEmpty()) {
                Toast.makeText(getContext(), errorMsg, Toast.LENGTH_LONG).show();
            }
        });

        // Set up FAB click listener
        fabAddMeal.setOnClickListener(v -> {
            if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                // Launch AddMealActivity
                Intent intent = new Intent(getActivity(), AddMealActivity.class);
                startActivity(intent);
            } else {
                Toast.makeText(getContext(), "Please sign in to add meals", Toast.LENGTH_LONG).show();
            }
        });

        // Load meals from Firestore
        loadMeals();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Refresh meals list when returning to this fragment
        loadMeals();
    }

    private void loadMeals() {
        if (mealViewModel != null) {
            mealViewModel.loadMeals();
        }
    }

    @Override
    public void onMealClick(Meal meal, int position) {
        // Open meal details activity/dialog
        Intent intent = new Intent(getActivity(), MealDetailActivity.class);
        intent.putExtra("MEAL_ID", meal.getMealId());
        startActivity(intent);
    }

    @Override
    public void onEditClick(Meal meal, int position) {
        // Open edit meal activity
        Intent intent = new Intent(getActivity(), AddMealActivity.class);
        intent.putExtra("MEAL_ID", meal.getMealId());
        intent.putExtra("IS_EDIT", true);
        startActivity(intent);
    }

    @Override
    public void onDeleteClick(Meal meal, int position) {
        // Show confirmation dialog before deleting
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Delete Meal")
                .setMessage("Are you sure you want to delete this meal?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    mealViewModel.deleteMeal(meal.getMealId());
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
