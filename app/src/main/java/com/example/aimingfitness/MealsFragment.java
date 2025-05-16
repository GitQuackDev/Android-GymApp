package com.example.aimingfitness;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class MealsFragment extends Fragment {

    private static final String TAG = "MealsFragment";

    private RecyclerView rvMeals;
    private MealAdapter mealAdapter;
    private List<Meal> mealList;
    private FirebaseFirestore db;

    public MealsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_meals, container, false);

        rvMeals = view.findViewById(R.id.rvMeals);
        rvMeals.setLayoutManager(new LinearLayoutManager(getContext()));

        mealList = new ArrayList<>();
        mealAdapter = new MealAdapter(getContext(), mealList);
        rvMeals.setAdapter(mealAdapter);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        fetchMealsFromFirestore();

        return view;
    }

    private void fetchMealsFromFirestore() {
        String currentUserId = null;
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        } else {
            Log.w(TAG, "User not authenticated. Cannot fetch meals.");
            mealList.clear();
            mealAdapter.notifyDataSetChanged();
            // Optionally, show a message to the user
            Toast.makeText(getContext(), "Please sign in to view meals.", Toast.LENGTH_LONG).show();
            return;
        }

        db.collection("meals")
                .whereEqualTo("userId", currentUserId) // Filter by userId
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        mealList.clear(); 
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Meal meal = document.toObject(Meal.class);
                            meal.setMealId(document.getId()); 
                            mealList.add(meal);
                            Log.d(TAG, document.getId() + " => " + document.getData());
                        }
                        mealAdapter.notifyDataSetChanged(); 
                    } else {
                        Log.w(TAG, "Error getting documents.", task.getException());
                        // Handle the error, e.g., show a toast message
                        Toast.makeText(getContext(), "Error fetching meals: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }
}
