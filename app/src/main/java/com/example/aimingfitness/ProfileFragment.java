package com.example.aimingfitness;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ProfileFragment extends Fragment {

    private TextView tvProfileWelcome;
    private Button btnLogoutProfile;
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();

        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id)) // Use your web client ID
                .requestEmail()
                .build();
        if (getActivity() != null) {
            mGoogleSignInClient = GoogleSignIn.getClient(getActivity(), gso);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        tvProfileWelcome = view.findViewById(R.id.tvProfileWelcome);
        btnLogoutProfile = view.findViewById(R.id.btnLogoutProfile);
        // mAuth = FirebaseAuth.getInstance(); // Moved to onCreate

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String displayName = currentUser.getDisplayName();
            if (displayName != null && !displayName.isEmpty()) {
                tvProfileWelcome.setText("Welcome, " + displayName + "!");
            } else if (currentUser.getEmail() != null) {
                tvProfileWelcome.setText("Welcome, " + currentUser.getEmail() + "!");
            } else {
                tvProfileWelcome.setText("Welcome!");
            }
        } else {
            tvProfileWelcome.setText("Welcome!");
        }

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
