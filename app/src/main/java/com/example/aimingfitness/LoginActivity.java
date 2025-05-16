package com.example.aimingfitness;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import com.example.aimingfitness.utils.ThemeHelper;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
// Firebase imports
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class LoginActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 9001;

    private TextInputLayout tilEmail;
    private TextInputLayout tilPassword;
    private TextInputEditText etEmail;
    private TextInputEditText etPassword;
    private MaterialButton btnLogin;
    private MaterialButton btnForgotPassword;
    private MaterialButton btnRegister;
    private MaterialButton btnGoogleLogin;
    
    private GoogleSignInClient googleSignInClient;
    private FirebaseAuth mAuth; // Declare FirebaseAuth instance

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Apply the theme before setting the content view
        ThemeHelper.applyTheme(this);
        
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Initialize views
        initViews();

        // Set up click listeners
        setupClickListeners();

        // Configure Google Sign-In
        configureGoogleSignIn();
    }
    
    private void initViews() {
        tilEmail = findViewById(R.id.tilEmail);
        tilPassword = findViewById(R.id.tilPassword);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnForgotPassword = findViewById(R.id.btnForgotPassword);
        btnRegister = findViewById(R.id.btnRegister);
        btnGoogleLogin = findViewById(R.id.btnGoogleLogin);
    }
    
    private void setupClickListeners() {
        // Login button click
        btnLogin.setOnClickListener(v -> handleLogin());
        
        // Register button click - navigate to register screen
        btnRegister.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });

        // Forgot password button click - navigate to forgot password screen
        btnForgotPassword.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, ForgotPasswordActivity.class);
            startActivity(intent);
        });

        // Google login button click
        btnGoogleLogin.setOnClickListener(v -> signInWithGoogle());
    }
    
    private void configureGoogleSignIn() {
        // Configure sign-in to request the user's ID, email address, and basic profile
        // Also request idToken for Firebase authentication
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id)) // Crucial for Firebase
                .requestEmail()
                .build();

        // Build a GoogleSignInClient with the options specified by gso
        googleSignInClient = GoogleSignIn.getClient(this, gso);
    }
    
    private void handleLogin() {
        // Reset errors
        tilEmail.setError(null);
        tilPassword.setError(null);

        // Get input values
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        // Validate input
        boolean isValid = true;
        
        if (TextUtils.isEmpty(email)) {
            tilEmail.setError("Email is required");
            isValid = false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.setError(getString(R.string.invalid_email));
            isValid = false;
        }
        
        if (TextUtils.isEmpty(password)) {
            tilPassword.setError("Password is required");
            isValid = false;
        }
        
        if (isValid) {
            // Firebase email/password auth
            mAuth.signInWithEmailAndPassword(email, password)
               .addOnCompleteListener(this, task -> {
                   if (task.isSuccessful()) {
                       FirebaseUser user = mAuth.getCurrentUser();
                       Toast.makeText(LoginActivity.this, "Login Successful.", Toast.LENGTH_SHORT).show();
                       navigateToHomePage();
                   } else {
                       Toast.makeText(LoginActivity.this, "Authentication failed: " + task.getException().getMessage(), 
                               Toast.LENGTH_LONG).show();
                   }
               });
        }
    }
    
    private void signInWithGoogle() {
        // First, sign out from any existing Google session to force account chooser
        if (googleSignInClient != null) {
            googleSignInClient.signOut().addOnCompleteListener(this, task -> {
                // After sign out, proceed with sign-in intent
                Intent signInIntent = googleSignInClient.getSignInIntent();
                startActivityForResult(signInIntent, RC_SIGN_IN);
            });
        } else {
            // Fallback if googleSignInClient is null, though it should be initialized in onCreate
            Intent signInIntent = googleSignInClient.getSignInIntent(); // This would cause a NullPointerException if it were null, but it's for logical flow.
            startActivityForResult(signInIntent, RC_SIGN_IN);
        }
    }
    
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach a listener
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleGoogleSignInResult(task);
        }
    }
    
    private void handleGoogleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            if (account != null) {
                // Google Sign In was successful, authenticate with Firebase
                firebaseAuthWithGoogle(account);
            }
        } catch (ApiException e) {
            // Google Sign In failed
            Toast.makeText(this, "Google sign in failed: " + e.getStatusCode() + " " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        FirebaseUser user = mAuth.getCurrentUser();
                        Toast.makeText(LoginActivity.this, "Firebase Auth Success: " + (user != null ? user.getDisplayName() : "Unknown user"), Toast.LENGTH_SHORT).show();
                        navigateToHomePage();
                    } else {
                        // If sign in fails, display a message to the user.
                        Toast.makeText(LoginActivity.this, "Firebase Authentication Failed: " + task.getException().getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void navigateToHomePage() {
        Intent intent = new Intent(LoginActivity.this, HomePageActivity.class);
        startActivity(intent);
        finish(); // Call finish to remove LoginActivity from the back stack
    }
    
    // Check for existing Google Sign In account on app start
    @Override
    protected void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        // FirebaseUser currentUser = mAuth.getCurrentUser(); // Removed this block
        // if (currentUser != null) {
        //     // User is already signed in, navigate to main app screen
        //     Toast.makeText(this, "Already signed in as " + currentUser.getDisplayName(), Toast.LENGTH_SHORT).show();
        //     navigateToHomePage();
        // }
        // No need to check GoogleSignIn.getLastSignedInAccount(this) if primarily using Firebase Auth state
    }
}