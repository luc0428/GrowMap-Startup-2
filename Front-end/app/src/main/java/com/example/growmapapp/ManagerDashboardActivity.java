package com.example.growmapapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class ManagerDashboardActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private TextView userName, userRole, userAvatar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manager_dashboard);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        userName = findViewById(R.id.userName);
        userRole = findViewById(R.id.userRole);
        userAvatar = findViewById(R.id.userAvatar);

        setupNavbar();
        loadUserData();
        setupDashboardCards();
        setupCharts();
    }

    private void setupNavbar() {
        ImageView btnThemeToggle = findViewById(R.id.btnThemeToggle);
        View userAvatarView = findViewById(R.id.userAvatar);
        TextView btnDashboard = findViewById(R.id.btnNavDashboardText);
        TextView btnSuporteText = findViewById(R.id.btnNavSuporteText);

        // Opções da Bottom Navbar
        View navHome = findViewById(R.id.btnNavHome);
        View navRoadmap = findViewById(R.id.btnNavRoadmap);
        View navSuporte = findViewById(R.id.btnNavSuporte);

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

        if (btnSuporteText != null) {
            btnSuporteText.setOnClickListener(v -> startActivity(new Intent(this, SupportActivity.class)));
        }

        if (navHome != null) {
            navHome.setOnClickListener(v -> {
                Intent intent = new Intent(this, DashboardActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
            });
        }

        if (navRoadmap != null) {
            navRoadmap.setOnClickListener(v -> startActivity(new Intent(this, RoadmapActivity.class)));
        }

        if (navSuporte != null) {
            navSuporte.setOnClickListener(v -> startActivity(new Intent(this, SupportActivity.class)));
        }

        if (userAvatarView != null) {
            userAvatarView.setOnClickListener(v -> startActivity(new Intent(this, ProfileActivity.class)));
        }
    }

    private void loadUserData() {
        if (mAuth.getCurrentUser() == null) return;

        String userId = mAuth.getCurrentUser().getUid();
        db.collection("user").document(userId).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                        DocumentSnapshot doc = task.getResult();
                        String name = doc.getString("fullname");
                        String role = doc.getString("role");

                        if (userName != null && name != null) {
                            userName.setText(name);
                            if (userAvatar != null) {
                                userAvatar.setText(String.valueOf(name.charAt(0)).toUpperCase());
                            }
                        }
                        if (userRole != null && role != null) userRole.setText(role);
                    }
                });
    }

    private void setupDashboardCards() {
        View cardUsers = findViewById(R.id.cardManageUsers);
        if (cardUsers != null) {
            cardUsers.setOnClickListener(v -> {
                startActivity(new Intent(this, GerenciarUser.class));
            });
        }
    }

    private void setupCharts() {
        LineChartView chart = findViewById(R.id.teamChart);
        if (chart != null) {
            java.util.List<Float> data = java.util.Arrays.asList(40f, 55f, 60f, 65f, 75f, 80f, 85f);
            java.util.List<String> labels = java.util.Arrays.asList("Jan", "Fev", "Mar", "Abr", "Mai", "Jun", "Jul");
            chart.setData(data, labels);
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
