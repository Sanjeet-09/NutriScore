package com.example.neutri_score;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Patterns;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class SignUpActivity extends AppCompatActivity {

    private EditText etFullName;
    private EditText etEmail;
    private EditText etPassword;
    private EditText etConfirmPassword;
    private ImageView ivPasswordToggle;
    private ImageView ivConfirmPasswordToggle;
    private TextView btnSignUp;
    private TextView tvGoToSignIn;

    private boolean isPasswordVisible = false;
    private boolean isConfirmPasswordVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        NeoBrutalistUtils.setupLightStatusBar(this);
        setContentView(R.layout.activity_sign_up);

        // 1. Initialize Views
        initViews();

        // 2. Setup Password Visibility Toggles
        setupPasswordToggles();

        // 3. Setup Button Press Effect
        NeoBrutalistUtils.applyTactileFeedback(btnSignUp);

        // 4. Handle Sign Up Action
        btnSignUp.setOnClickListener(v -> handleSignUp());

        // 5. Navigate back to Sign In
        tvGoToSignIn.setOnClickListener(v -> finish());
    }

    private void initViews() {
        etFullName = findViewById(R.id.etFullName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        ivPasswordToggle = findViewById(R.id.ivPasswordToggle);
        ivConfirmPasswordToggle = findViewById(R.id.ivConfirmPasswordToggle);
        btnSignUp = findViewById(R.id.btnSignUp);
        tvGoToSignIn = findViewById(R.id.tvGoToSignIn);
    }

    private void setupPasswordToggles() {
        // Main password toggle
        ivPasswordToggle.setOnClickListener(v -> {
            if (isPasswordVisible) {
                etPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                ivPasswordToggle.setAlpha(0.6f);
                isPasswordVisible = false;
            } else {
                etPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                ivPasswordToggle.setAlpha(1.0f);
                isPasswordVisible = true;
            }
            etPassword.setSelection(etPassword.getText().length());
        });

        // Confirm password toggle
        ivConfirmPasswordToggle.setOnClickListener(v -> {
            if (isConfirmPasswordVisible) {
                etConfirmPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                ivConfirmPasswordToggle.setAlpha(0.6f);
                isConfirmPasswordVisible = false;
            } else {
                etConfirmPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                ivConfirmPasswordToggle.setAlpha(1.0f);
                isConfirmPasswordVisible = true;
            }
            etConfirmPassword.setSelection(etConfirmPassword.getText().length());
        });
    }

    /**
     * Validates input fields and creates user profile.
     * (AWS Cognito / DynamoDB registration will hook into this method later)
     */
    private void handleSignUp() {
        String fullName = etFullName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        // Validate Full Name
        if (TextUtils.isEmpty(fullName)) {
            etFullName.setError("Full name is required");
            etFullName.requestFocus();
            return;
        }

        if (fullName.length() < 2) {
            etFullName.setError("Please enter a valid name");
            etFullName.requestFocus();
            return;
        }

        // Validate Email
        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Email address is required");
            etEmail.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Please enter a valid email address");
            etEmail.requestFocus();
            return;
        }

        // Validate Password
        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Password is required");
            etPassword.requestFocus();
            return;
        }

        if (password.length() < 6) {
            etPassword.setError("Password must be at least 6 characters");
            etPassword.requestFocus();
            return;
        }

        // Validate Confirm Password
        if (TextUtils.isEmpty(confirmPassword)) {
            etConfirmPassword.setError("Please confirm your password");
            etConfirmPassword.requestFocus();
            return;
        }

        if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError("Passwords do not match");
            etConfirmPassword.requestFocus();
            return;
        }

        // --- REGISTRATION SUCCESS (LOCAL SESSION) ---
        // TODO: Replace with AWS Cognito signUp() API call once AWS backend is ready
        SessionManager.getInstance(this).createLoginSession(fullName, email);

        Toast.makeText(this, "Account created! Welcome, " + fullName, Toast.LENGTH_SHORT).show();

        // Route to MainActivity and clear backstack
        Intent intent = new Intent(SignUpActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}