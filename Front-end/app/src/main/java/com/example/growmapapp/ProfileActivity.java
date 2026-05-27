package com.example.growmapapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.example.growmapapp.model.User;
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
        
        View btnManager = findViewById(R.id.btnManagerDashboard);
        if (btnManager != null) {
            btnManager.setOnClickListener(v -> startActivity(new Intent(this, ManagerDashboardActivity.class)));
            btnManager.setVisibility(checkIfAdmin() ? View.VISIBLE : View.GONE);
        }

        setupNavbar();
        loadUserData();
        setupChart();
    }

    private void setupNavbar() {
        ImageView btnThemeToggle = findViewById(R.id.btnThemeToggle);

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
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String savedUserId = sharedPreferences.getString("userId", null);
        
        String userId = savedUserId;
        if (userId == null) {
            FirebaseAuth auth = FirebaseAuth.getInstance();
            if (auth.getCurrentUser() != null) {
                userId = auth.getCurrentUser().getUid();
            }
        }

        if (userId != null) {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            String finalUserId = userId;
            db.collection("user").document(userId).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        User user = doc.toObject(User.class);
                        if (user == null) return;

                        TextView tvName = findViewById(R.id.profileName);
                        TextView tvRole = findViewById(R.id.profileRole);
                        TextView tvAvatar = findViewById(R.id.profileAvatar);
                        CheckBox cbIsAdmin = findViewById(R.id.cbIsAdmin);
                        
                        if (user.getFullname() != null && !user.getFullname().isEmpty()) {
                            tvName.setText(user.getFullname());
                            tvAvatar.setText(String.valueOf(user.getFullname().charAt(0)).toUpperCase());
                        }

                        String displayRole = (user.getCargo() != null && !user.getCargo().isEmpty()) ? user.getCargo() : user.getRole();
                        if (displayRole != null) {
                            tvRole.setText(displayRole);
                        }

                        // Set checkbox state and listener
                        if (cbIsAdmin != null) {
                            cbIsAdmin.setOnCheckedChangeListener(null); 
                            cbIsAdmin.setChecked(user.isAdm());
                            
                            // Save to local cache for checkIfAdmin()
                            sharedPreferences.edit().putBoolean("isAdmin", user.isAdm()).apply();

                            cbIsAdmin.setOnCheckedChangeListener((buttonView, isChecked) -> {
                                updateAdminStatus(finalUserId, isChecked);
                            });
                        }

                        ProgressBar pbJava = findViewById(R.id.pbJavaDeveloper);
                        TextView tvJavaPerc = findViewById(R.id.tvJavaPercentage);
                        if (pbJava != null) pbJava.setProgress(user.getPercentage());
                        if (tvJavaPerc != null) {
                            String percText = user.getPercentage() + "%";
                            tvJavaPerc.setText(percText);
                        }
                    }
                });
        }
    }

    private void updateAdminStatus(String userId, boolean isAdmin) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("user").document(userId)
                .update("adm", isAdmin)
                .addOnSuccessListener(aVoid -> {
                    // Update local cache
                    SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
                    sharedPreferences.edit().putBoolean("isAdmin", isAdmin).apply();

                    // Refresh manager button visibility
                    View btnManager = findViewById(R.id.btnManagerDashboard);
                    if (btnManager != null) {
                        btnManager.setVisibility(isAdmin ? View.VISIBLE : View.GONE);
                    }

                    String msg = isAdmin ? "Você agora é ADM!" : "Privilégios de ADM removidos.";
                    Toast.makeText(ProfileActivity.this, msg, Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(ProfileActivity.this, "Erro ao atualizar ADM: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    public boolean checkIfAdmin() {
        // This is a synchronous check based on a local source or logic.
        // Usually, for Firebase, you'd check a cached user object or wait for Firestore.
        // For now, let's assume you might want to call this elsewhere.
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        // This would require storing the admin status in SharedPreferences too for sync access
        return sharedPreferences.getBoolean("isAdmin", false);
    }
}
