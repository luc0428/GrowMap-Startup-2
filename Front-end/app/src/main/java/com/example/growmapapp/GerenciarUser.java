package com.example.growmapapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class GerenciarUser extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gerenciar_user);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        setupNavbar();
        setupActions();
        loadUserInfo();
    }

    private void loadUserInfo() {
        if (mAuth.getCurrentUser() == null) return;

        String userId = mAuth.getCurrentUser().getUid();
        db.collection("user").document(userId).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                        String name = task.getResult().getString("fullname");
                        String role = task.getResult().getString("role");
                        String email = task.getResult().getString("email");

                        TextView userName = findViewById(R.id.userName);
                        TextView userRole = findViewById(R.id.userRole);
                        TextView userAvatar = findViewById(R.id.userAvatar);
                        EditText etName = findViewById(R.id.etName);
                        EditText etRole = findViewById(R.id.etRole);
                        EditText etEmail = findViewById(R.id.etEmail);

                        if (userName != null && name != null) userName.setText(name);
                        if (userRole != null && role != null) userRole.setText(role);
                        if (userAvatar != null && name != null && !name.isEmpty()) {
                            userAvatar.setText(String.valueOf(name.charAt(0)).toUpperCase());
                        }

                        if (etName != null && name != null) etName.setText(name);
                        if (etRole != null && role != null) etRole.setText(role);
                        if (etEmail != null && email != null) etEmail.setText(email);
                    }
                });
    }

    private void setupActions() {
        View btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        View btnUpdate = findViewById(R.id.btnUpdate);
        if (btnUpdate != null) {
            btnUpdate.setOnClickListener(v -> saveUserData());
        }
    }

    private void saveUserData() {
        if (mAuth.getCurrentUser() == null) return;

        String userId = mAuth.getCurrentUser().getUid();
        EditText etName = findViewById(R.id.etName);
        EditText etRole = findViewById(R.id.etRole);
        EditText etEmail = findViewById(R.id.etEmail);

        String name = etName.getText().toString().trim();
        String role = etRole.getText().toString().trim();
        String email = etEmail.getText().toString().trim();

        if (name.isEmpty() || role.isEmpty() || email.isEmpty()) {
            Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show();
            return;
        }

        java.util.Map<String, Object> updates = new java.util.HashMap<>();
        updates.put("fullname", name);
        updates.put("role", role);
        updates.put("email", email);

        db.collection("user").document(userId).update(updates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Dados atualizados com sucesso!", Toast.LENGTH_SHORT).show();
                    loadUserInfo(); // Refresh UI
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Erro ao atualizar: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void setupNavbar() {
        ImageView btnThemeToggle = findViewById(R.id.btnThemeToggle);
        View userAvatar = findViewById(R.id.userAvatar);
        TextView btnDashboard = findViewById(R.id.btnNavDashboardText);
        TextView btnSuporte = findViewById(R.id.btnNavSuporteText);

        // Bottom Navbar
        View btnNavHome = findViewById(R.id.btnNavHome);
        View btnNavRoadmap = findViewById(R.id.btnNavRoadmap);
        View btnNavGestao = findViewById(R.id.btnNavGestao);
        View btnNavSuporte = findViewById(R.id.btnNavSuporte);

        updateThemeIcon(btnThemeToggle);

        if (btnThemeToggle != null) {
            btnThemeToggle.setOnClickListener(v -> {
                int mode = AppCompatDelegate.getDefaultNightMode();
                if (mode == AppCompatDelegate.MODE_NIGHT_YES) {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                } else {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                }
                recreate();
            });
        }

        if (btnDashboard != null) {
            btnDashboard.setOnClickListener(v -> {
                Intent intent = new Intent(this, DashboardActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
            });
        }

        if (btnSuporte != null) {
            btnSuporte.setOnClickListener(v -> {
                startActivity(new Intent(this, SupportActivity.class));
            });
        }

        if (userAvatar != null) {
            userAvatar.setOnClickListener(v -> startActivity(new Intent(this, ProfileActivity.class)));
        }

        // Bottom Navbar Listeners
        if (btnNavHome != null) {
            btnNavHome.setOnClickListener(v -> {
                Intent intent = new Intent(this, DashboardActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
            });
        }

        if (btnNavRoadmap != null) {
            btnNavRoadmap.setOnClickListener(v -> {
                startActivity(new Intent(this, RoadmapActivity.class));
            });
        }

        if (btnNavGestao != null) {
            btnNavGestao.setOnClickListener(v -> {
                Toast.makeText(this, "Você já está na Gestão", Toast.LENGTH_SHORT).show();
            });
        }

        if (btnNavSuporte != null) {
            btnNavSuporte.setOnClickListener(v -> {
                startActivity(new Intent(this, SupportActivity.class));
            });
        }
    }

    private void updateThemeIcon(ImageView btn) {
        if (btn == null) return;
        int mode = AppCompatDelegate.getDefaultNightMode();
        if (mode == AppCompatDelegate.MODE_NIGHT_YES) {
            btn.setImageResource(R.drawable.ic_sun);
        } else {
            btn.setImageResource(R.drawable.ic_moon);
        }
    }
}
