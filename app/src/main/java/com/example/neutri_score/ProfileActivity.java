package com.example.neutri_score;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.io.InputStream;

public class ProfileActivity extends AppCompatActivity {

    private FrameLayout flAvatarContainer;
    private ImageView ivProfilePhoto;
    private TextView tvAvatarInitials;
    private TextView tvProfileName;
    private TextView tvProfileEmail;
    private TextView btnEditProfile;
    private TextView btnProfileLogout;

    private ActivityResultLauncher<Intent> imagePickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        NeoBrutalistUtils.setupLightStatusBar(this);
        setContentView(R.layout.activity_profile);

        flAvatarContainer = findViewById(R.id.flAvatarContainer);
        ivProfilePhoto = findViewById(R.id.ivProfilePhoto);
        tvAvatarInitials = findViewById(R.id.tvAvatarInitials);
        tvProfileName = findViewById(R.id.tvProfileName);
        tvProfileEmail = findViewById(R.id.tvProfileEmail);
        btnEditProfile = findViewById(R.id.btnEditProfile);
        btnProfileLogout = findViewById(R.id.btnProfileLogout);

        NeoBrutalistUtils.applyTactileFeedback(btnProfileLogout);
        NeoBrutalistUtils.applyTactileFeedback(btnEditProfile);
        NeoBrutalistUtils.applyTactileFeedback(flAvatarContainer);

        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri selectedImageUri = result.getData().getData();
                        if (selectedImageUri != null) {
                            try {

                                getContentResolver().takePersistableUriPermission(
                                        selectedImageUri,
                                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                                );
                            } catch (Exception ignored) {

                            }
                            SessionManager.getInstance(ProfileActivity.this)
                                    .saveProfileImageUri(selectedImageUri.toString());
                            loadProfilePhoto(selectedImageUri.toString());
                            Toast.makeText(ProfileActivity.this, "Profile photo updated", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );

        flAvatarContainer.setOnClickListener(v -> openImagePicker());

        btnEditProfile.setOnClickListener(v -> showEditProfileDialog());

        loadUserData();

        btnProfileLogout.setOnClickListener(v -> {
            SessionManager.getInstance(ProfileActivity.this).logout();
            Toast.makeText(ProfileActivity.this, "Signed out successfully", Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(ProfileActivity.this, SignInActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        BottomNavHelper.setupBottomNav(this, "PROFILE");
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        imagePickerLauncher.launch(intent);
    }

    private void loadUserData() {
        SessionManager sessionManager = SessionManager.getInstance(this);
        String name = sessionManager.getUserName();
        String email = sessionManager.getUserEmail();

        tvProfileName.setText(name);
        tvProfileEmail.setText(email.isEmpty() ? "user@session.local" : email);

        if (name != null && !name.trim().isEmpty()) {
            String[] parts = name.trim().split("\\s+");
            if (parts.length >= 2) {
                tvAvatarInitials.setText(("" + parts[0].charAt(0) + parts[1].charAt(0)).toUpperCase());
            } else {
                tvAvatarInitials.setText(("" + name.charAt(0)).toUpperCase());
            }
        } else {
            tvAvatarInitials.setText("NS");
        }

        String photoUriStr = sessionManager.getProfileImageUri();
        if (photoUriStr != null && !photoUriStr.isEmpty()) {
            loadProfilePhoto(photoUriStr);
        } else {
            ivProfilePhoto.setVisibility(View.GONE);
            tvAvatarInitials.setVisibility(View.VISIBLE);
        }
    }

    private void loadProfilePhoto(String uriStr) {
        try {
            Uri imageUri = Uri.parse(uriStr);
            ivProfilePhoto.setImageURI(imageUri);
            ivProfilePhoto.setVisibility(View.VISIBLE);
            tvAvatarInitials.setVisibility(View.GONE);
        } catch (Exception e) {

            ivProfilePhoto.setVisibility(View.GONE);
            tvAvatarInitials.setVisibility(View.VISIBLE);
        }
    }

    private void showEditProfileDialog() {
        SessionManager sessionManager = SessionManager.getInstance(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_edit_profile, null);

        EditText etEditName = dialogView.findViewById(R.id.etEditName);
        EditText etEditEmail = dialogView.findViewById(R.id.etEditEmail);
        TextView btnCancelEdit = dialogView.findViewById(R.id.btnCancelEdit);
        TextView btnSaveEdit = dialogView.findViewById(R.id.btnSaveEdit);

        etEditName.setText(sessionManager.getUserName());
        etEditEmail.setText(sessionManager.getUserEmail());

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        btnCancelEdit.setOnClickListener(v -> dialog.dismiss());

        btnSaveEdit.setOnClickListener(v -> {
            String newName = etEditName.getText().toString().trim();
            String newEmail = etEditEmail.getText().toString().trim();

            if (newName.isEmpty()) {
                etEditName.setError("Name cannot be empty");
                return;
            }
            if (newEmail.isEmpty()) {
                etEditEmail.setError("Email cannot be empty");
                return;
            }

            sessionManager.updateUserDetails(newName, newEmail);
            loadUserData();
            dialog.dismiss();
            Toast.makeText(ProfileActivity.this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
        });

        dialog.show();
    }
}
