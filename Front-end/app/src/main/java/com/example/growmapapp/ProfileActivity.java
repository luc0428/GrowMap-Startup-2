package com.example.growmapapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Arrays;
import java.util.List;

public class ProfileActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        
        setupNavbar();
        loadUserData();
        setupChart();
    }

    private void setupNavbar() {
        ImageView btnThemeToggle = findViewById(R.id.btnThemeToggle);
        TextView btnDashboard = findViewById(R.id.btnNavDashboardText);

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

    private void setupChart() {
        LineChartView chart = findViewById(R.id.profileLineChart);
        if (chart != null) {
            List<Float> data = Arrays.asList(20f, 60f, 45f, 80f, 50f, 95f, 70f);
            List<String> labels = Arrays.asList("S", "T", "Q", "Q", "S", "S", "D");
            chart.setData(data, labels);
        }
    }

    private void loadUserData() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        if (auth.getCurrentUser() != null) {
            db.collection("user").document(auth.getCurrentUser().getUid()).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        String name = doc.getString("fullname");
                        String role = doc.getString("role");
                        Long percentage = doc.getLong("percentage");
                        
                        TextView tvName = findViewById(R.id.profileName);
                        TextView tvRole = findViewById(R.id.profileRole);
                        TextView tvAvatar = findViewById(R.id.profileAvatar);
                        
                        if (name != null) {
                            tvName.setText(name);
                            tvAvatar.setText(String.valueOf(name.charAt(0)).toUpperCase());
                        }
                        if (role != null) tvRole.setText(role);

                        if (percentage != null) {
                            ProgressBar pbJava = findViewById(R.id.pbJavaDeveloper);
                            TextView tvJavaPerc = findViewById(R.id.tvJavaPercentage);
                            if (pbJava != null) pbJava.setProgress(percentage.intValue());
                            if (tvJavaPerc != null) {
                                String percText = percentage + "%";
                                tvJavaPerc.setText(percText);
                            }
                        }
                    }
                });
        }
    }
}
