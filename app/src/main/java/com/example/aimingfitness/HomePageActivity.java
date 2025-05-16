package com.example.aimingfitness;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;

public class HomePageActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);

        mAuth = FirebaseAuth.getInstance();
        bottomNavigationView = findViewById(R.id.bottom_navigation);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int itemId = item.getItemId();

            if (itemId == R.id.navigation_workouts) {
                selectedFragment = new WorkoutsFragment(); // Changed to load WorkoutsFragment
            } else if (itemId == R.id.navigation_meals) {
                selectedFragment = new MealsFragment();
            } else if (itemId == R.id.navigation_progress) {
                selectedFragment = new ProgressFragment();
            } else if (itemId == R.id.navigation_profile) {
                selectedFragment = new ProfileFragment();
            }

            if (selectedFragment != null) {
                loadFragment(selectedFragment);
            }
            return true;
        });

        if (savedInstanceState == null) {
            // Ensure the correct default fragment is loaded based on the selected item ID
            int defaultItemId = bottomNavigationView.getSelectedItemId();
            if (defaultItemId == R.id.navigation_workouts) {
                loadFragment(new WorkoutsFragment());
            } else if (defaultItemId == R.id.navigation_meals) {
                loadFragment(new MealsFragment());
            } else if (defaultItemId == R.id.navigation_progress) {
                loadFragment(new ProgressFragment());
            } else if (defaultItemId == R.id.navigation_profile) {
                loadFragment(new ProfileFragment());
            } else {
                // Fallback if no item is explicitly selected or if the selection is somehow invalid
                // Load a default fragment, e.g., WorkoutsFragment or the first one in your menu
                loadFragment(new WorkoutsFragment());
            }
        }
    }

    private void loadFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, fragment);
        fragmentTransaction.commit();
    }
}