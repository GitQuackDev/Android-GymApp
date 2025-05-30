package com.example.aimingfitness;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;
import android.preference.PreferenceManager;

import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ProfileFragment extends Fragment {

    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int CAMERA_IMAGE_REQUEST = 2;
    private static final String PROFILE_IMAGE_PREF_KEY = "profile_image_uri"; // SharedPreferences key

    private TextView tvProfileWelcome;
    private TextView tvUserEmail;
    private TextView tvTotalWorkouts;
    private TextView tvTotalExercises;
    private TextView tvStreak;
    private Button btnLogoutProfile;
    private ImageView ivProfileImage;
    private ImageView ivEditProfileImage;
    private SwitchCompat switchTheme;
    
    private FirebaseAuth mAuth;
    private FirebaseFirestore db; // Keep for user stats
    private GoogleSignInClient mGoogleSignInClient;
    private Uri imageUri;
    private Uri cameraImageUri;

    private SharedPreferences sharedPreferences;

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance(); // Keep for user stats
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext());

        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id)) // Use your web client ID
                .requestEmail()
                .build();
        if (getActivity() != null) {
            mGoogleSignInClient = GoogleSignIn.getClient(getActivity(), gso);
        }
    }    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // Initialize views
        tvProfileWelcome = view.findViewById(R.id.tvProfileWelcome);
        tvUserEmail = view.findViewById(R.id.tvUserEmail);
        tvTotalWorkouts = view.findViewById(R.id.tvTotalWorkouts);
        tvTotalExercises = view.findViewById(R.id.tvTotalExercises);
        tvStreak = view.findViewById(R.id.tvStreak);
        btnLogoutProfile = view.findViewById(R.id.btnLogoutProfile);
        ivProfileImage = view.findViewById(R.id.ivProfileImage);
        ivEditProfileImage = view.findViewById(R.id.ivEditProfileImage);
        switchTheme = view.findViewById(R.id.switchTheme);

        // Set user information
        updateUserInfo();
        
        // Load profile image
        loadProfileImage();
        
        // Set user stats
        loadUserStats();

        // Setup theme switch with improved UI
        setupThemeSwitch();

        // Setup profile image click
        ivEditProfileImage.setOnClickListener(v -> openImagePicker());

        // Logout button
        btnLogoutProfile.setOnClickListener(v -> {
            mAuth.signOut();
            if (mGoogleSignInClient != null) {
                mGoogleSignInClient.signOut().addOnCompleteListener(getActivity(), task -> {
                    // Optional: Handle Google sign out completion
                    navigateToLogin();
                });
            } else {
                // Fallback if mGoogleSignInClient is null for some reason
                navigateToLogin();
            }
        });

        return view;
    }    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == PICK_IMAGE_REQUEST && data != null && data.getData() != null) {
                // Gallery image selection
                imageUri = data.getData();
                uploadProfileImage();
            } else if (requestCode == CAMERA_IMAGE_REQUEST) {
                // Camera image capture
                if (cameraImageUri != null) {
                    imageUri = cameraImageUri;
                    uploadProfileImage();
                }
            }
        }
    }

    private void updateUserInfo() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String displayName = currentUser.getDisplayName();
            if (displayName != null && !displayName.isEmpty()) {
                tvProfileWelcome.setText(displayName);
            } else {
                tvProfileWelcome.setText("Welcome!");
            }
            
            if (currentUser.getEmail() != null) {
                tvUserEmail.setText(currentUser.getEmail());
            } else {
                tvUserEmail.setText("");
            }
        } else {
            tvProfileWelcome.setText("Welcome!");
            tvUserEmail.setText("");
        }
    }    private void loadProfileImage() {
        // Load profile image URI from SharedPreferences
        String imagePathString = sharedPreferences.getString(PROFILE_IMAGE_PREF_KEY, null);
        FirebaseUser currentUser = mAuth.getCurrentUser(); // Keep for Google photo URL fallback

        if (imagePathString != null) {
            Uri localImageUri = Uri.parse(imagePathString);
            if (getContext() != null && !isDetached()) {
                Glide.with(getContext())
                    .load(localImageUri)
                    .placeholder(R.drawable.ic_person)
                    .error(R.drawable.ic_person) // Fallback if local image is somehow corrupted/deleted
                    .circleCrop()
                    .transition(com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade(300))
                    .into(ivProfileImage);
            }
        } else if (currentUser != null && currentUser.getPhotoUrl() != null) {
            // Fallback to Google photo URL if no local image is set
            if (getContext() != null && !isDetached()) {
                Glide.with(getContext())
                    .load(currentUser.getPhotoUrl())
                    .placeholder(R.drawable.ic_person)
                    .error(R.drawable.ic_person)
                    .circleCrop()
                    .transition(com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade(300))
                    .into(ivProfileImage);
            }
        } else {
            // Load default placeholder if no image is found
            if (getContext() != null && !isDetached()) {
                Glide.with(getContext())
                    .load(R.drawable.ic_person)
                    .circleCrop()
                    .into(ivProfileImage);
            }
        }
    }

    private void loadUserStats() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            // Fetch workout count from Firestore
            db.collection("workoutHistory")
                .whereEqualTo("userId", currentUser.getUid())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (isAdded()) { // Check if fragment is still attached to activity
                        int workoutCount = queryDocumentSnapshots.size();
                        tvTotalWorkouts.setText(String.valueOf(workoutCount));
                        
                        // Calculate total exercises
                        int totalExercises = 0;
                        for (int i = 0; i < queryDocumentSnapshots.size(); i++) {
                            Object totalExercisesObj = queryDocumentSnapshots.getDocuments().get(i).get("totalExercises");
                            if (totalExercisesObj instanceof Number) {
                                totalExercises += ((Number) totalExercisesObj).intValue();
                            }
                        }
                        tvTotalExercises.setText(String.valueOf(totalExercises));
                    }
                });
            
            // For demo purposes, set a default streak
            // In a real app, you would calculate this based on consecutive days with workouts
            tvStreak.setText("3");
        }
    }    private void setupThemeSwitch() {
        // Set the initial state based on the current theme mode
        boolean isDarkMode = sharedPreferences.getBoolean("dark_mode", false);
        switchTheme.setChecked(isDarkMode);
        
        // Add animation to theme change
        switchTheme.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // Save the preference
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("dark_mode", isChecked);
            editor.apply();
            
            // Show transition animation before switching modes
            if (getActivity() != null) {
                android.view.animation.Animation animation = android.view.animation.AnimationUtils.loadAnimation(
                    getContext(), android.R.anim.fade_out);
                animation.setDuration(200);
                
                animation.setAnimationListener(new android.view.animation.Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(android.view.animation.Animation animation) {}
                    
                    @Override
                    public void onAnimationEnd(android.view.animation.Animation animation) {
                        // Apply the theme change after fade out
                        if (isChecked) {
                            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                        } else {
                            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                        }
                    }
                    
                    @Override
                    public void onAnimationRepeat(android.view.animation.Animation animation) {}
                });
                
                // Apply animation to root view
                View rootView = getActivity().getWindow().getDecorView().findViewById(android.R.id.content);
                rootView.startAnimation(animation);
            } else {
                // Fallback if getActivity is null
                if (isChecked) {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                } else {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                }
            }
        });
    }private void openImagePicker() {
        // Create a custom bottom sheet dialog for image selection options
        if (getActivity() != null && !isDetached()) {
            com.google.android.material.bottomsheet.BottomSheetDialog bottomSheetDialog = 
                new com.google.android.material.bottomsheet.BottomSheetDialog(getActivity());
            
            View sheetView = getLayoutInflater().inflate(R.layout.bottom_sheet_image_picker, null);
            bottomSheetDialog.setContentView(sheetView);
            
            // Set up gallery selection
            View btnGallery = sheetView.findViewById(R.id.btnGallery);
            btnGallery.setOnClickListener(v -> {
                Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(galleryIntent, PICK_IMAGE_REQUEST);
                bottomSheetDialog.dismiss();
            });
            
            // Set up camera capture
            View btnCamera = sheetView.findViewById(R.id.btnCamera);
            btnCamera.setOnClickListener(v -> {
                // Check camera permission at runtime for Android 6.0+
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    if (getContext().checkSelfPermission(android.Manifest.permission.CAMERA) != 
                            android.content.pm.PackageManager.PERMISSION_GRANTED) {
                        
                        requestPermissions(new String[]{android.Manifest.permission.CAMERA},
                                CAMERA_IMAGE_REQUEST);
                    } else {
                        openCamera();
                    }
                } else {
                    openCamera();
                }
                bottomSheetDialog.dismiss();
            });
            
            // Set up cancel button
            View btnCancel = sheetView.findViewById(R.id.btnCancel);
            btnCancel.setOnClickListener(v -> bottomSheetDialog.dismiss());
            
            bottomSheetDialog.show();
        }
    }
    
    private void openCamera() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (cameraIntent.resolveActivity(getActivity().getPackageManager()) != null) {
            // Create file for the image
            java.io.File photoFile = null;
            try {
                String timeStamp = new java.text.SimpleDateFormat("yyyyMMdd_HHmmss", java.util.Locale.getDefault()).format(new java.util.Date());
                String imageFileName = "JPEG_" + timeStamp + "_";
                java.io.File storageDir = getActivity().getExternalFilesDir(android.os.Environment.DIRECTORY_PICTURES);
                photoFile = java.io.File.createTempFile(imageFileName, ".jpg", storageDir);
                
                // Save the file path for use in onActivityResult
                cameraImageUri = androidx.core.content.FileProvider.getUriForFile(
                    getContext(),
                    getContext().getPackageName() + ".fileprovider",
                    photoFile);
                
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, cameraImageUri);
                startActivityForResult(cameraIntent, CAMERA_IMAGE_REQUEST);
            } catch (java.io.IOException e) {
                Toast.makeText(getContext(), "Error creating image file", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(getContext(), "No camera app found", Toast.LENGTH_SHORT).show();
        }
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_IMAGE_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else {
                Toast.makeText(getContext(), "Camera permission is required to take pictures", Toast.LENGTH_SHORT).show();
            }
        }
    }
      private void uploadProfileImage() {
        if (imageUri != null && getContext() != null) {
            FirebaseUser currentUser = mAuth.getCurrentUser(); // Needed for unique naming if desired, though not strictly for local storage path
            if (currentUser == null) { // Should not happen if user is on profile screen
                Toast.makeText(getContext(), "User not authenticated.", Toast.LENGTH_SHORT).show();
                return;
            }

            View rootView = getView();
            if (rootView != null) {
                ivEditProfileImage.setEnabled(false);
                ivProfileImage.setColorFilter(android.graphics.Color.parseColor("#80000000"),
                        android.graphics.PorterDuff.Mode.SRC_ATOP);
            }

            try {
                // 1. Create a destination file in internal storage
                String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
                String imageFileName = "PROFILE_" + timeStamp + ".jpg";
                File storageDir = getContext().getFilesDir(); // Internal storage
                File localImageFile = new File(storageDir, imageFileName);

                // 2. Get Bitmap from imageUri (either from gallery or camera)
                android.graphics.Bitmap bitmap = MediaStore.Images.Media.getBitmap(
                        getContext().getContentResolver(), imageUri);

                // 3. Compress and save the bitmap to the local file
                FileOutputStream fos = new FileOutputStream(localImageFile);
                bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 85, fos); // Compress to 85% quality
                fos.flush();
                fos.close();

                // 4. Save the local file URI to SharedPreferences
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString(PROFILE_IMAGE_PREF_KEY, Uri.fromFile(localImageFile).toString());
                editor.apply();

                Toast.makeText(getContext(), "Profile image updated successfully", Toast.LENGTH_SHORT).show();

                // Clear the color filter and re-enable button
                if (rootView != null) {
                    ivProfileImage.clearColorFilter();
                    ivEditProfileImage.setEnabled(true);
                }
                // Refresh the image with a slight delay for better UX
                new android.os.Handler(Looper.getMainLooper()).postDelayed(this::loadProfileImage, 300);

            } catch (IOException e) {
                Toast.makeText(getContext(), "Failed to save image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("ProfileFragment", "Error saving profile image locally", e);
                if (rootView != null) {
                    ivProfileImage.clearColorFilter();
                    ivEditProfileImage.setEnabled(true);
                }
            }
        }
    }

    private void navigateToLogin() {
        if (getContext() != null) {
            Intent intent = new Intent(getContext(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            if (getActivity() != null) {
                getActivity().finish();
            }
        }
    }
}
