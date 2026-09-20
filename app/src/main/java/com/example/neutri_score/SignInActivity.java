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

public class SignInActivity extends AppCompatActivity {

    private EditText etEmail;
    private EditText etPassword;
    private ImageView ivPasswordToggle;
    private TextView btnSignIn;
    private TextView tvGoToSignUp;
    private TextView tvForgotPassword;

    private boolean isPasswordVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        NeoBrutalistUtils.setupLightStatusBar(this);
        setContentView(R.layout.activity_sign_in);

        initViews();

        setupPasswordToggle();

        NeoBrutalistUtils.applyTactileFeedback(btnSignIn);

        btnSignIn.setOnClickListener(v -> handleSignIn());

        tvGoToSignUp.setOnClickListener(v -> {
            Intent intent = new Intent(SignInActivity.this, SignUpActivity.class);
            startActivity(intent);
        });

        tvForgotPassword.setOnClickListener(v -> handleForgotPassword());
    }

    private void initViews() {
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        ivPasswordToggle = findViewById(R.id.ivPasswordToggle);
        btnSignIn = findViewById(R.id.btnSignIn);
        tvGoToSignUp = findViewById(R.id.tvGoToSignUp);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);
    }

    private void setupPasswordToggle() {
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
    }

    private void handleSignIn() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

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

        String derivedName = email.contains("@") ? email.substring(0, email.indexOf('@')) : "User";
        SessionManager.getInstance(this).createLoginSession(derivedName, email);

        Toast.makeText(this, "Welcome back, " + derivedName + "!", Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(SignInActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void handleForgotPassword() {
        String email = etEmail.getText().toString().trim();
        if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Enter your email here to receive a reset link");
            etEmail.requestFocus();
            Toast.makeText(this, "Please provide your email address first", Toast.LENGTH_SHORT).show();
        } else {

            Toast.makeText(this, "Password reset instructions will be sent to " + email + " once AWS backend is active.", Toast.LENGTH_LONG).show();
        }
    }
}
