package com.example.growmapapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

public class RoadmapActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_roadmap);
        setupNavbar();
    }

    private void setupNavbar() {
        View btnBack = findViewById(R.id.btnBack);
        ImageView btnThemeToggle = findViewById(R.id.btnThemeToggle);
        View btnDashboard = findViewById(R.id.btnDashboard);
        View btnCursos = findViewById(R.id.btnCursos);
        View btnSuporte = findViewById(R.id.btnSuporte);
        View userInfoHeader = findViewById(R.id.userInfoHeader);
        View userAvatar = findViewById(R.id.userAvatar);

        updateThemeIcon(btnThemeToggle);

        if (btnBack != null) btnBack.setOnClickListener(v -> finish());

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

        if (btnCursos != null) {
            btnCursos.setOnClickListener(v -> {
                // Já está em cursos
            });
        }

        if (btnSuporte != null) {
            btnSuporte.setOnClickListener(v -> {
                startActivity(new Intent(this, SupportActivity.class));
            });
        }

        if (userInfoHeader != null) {
            userInfoHeader.setOnClickListener(v -> {
                startActivity(new Intent(this, GerenciarUser.class));
            });
        }

        if (userAvatar != null) {
            userAvatar.setOnClickListener(v -> {
                startActivity(new Intent(this, GerenciarUser.class));
            });
        }

        setupRoadmapTabs();
        setupRoadmapSteps();
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

    private void setupRoadmapTabs() {
        LinearLayout tabsContainer = findViewById(R.id.roadmapTabsContainer);
        if (tabsContainer != null) {
            for (int i = 0; i < tabsContainer.getChildCount(); i++) {
                View tab = tabsContainer.getChildAt(i);
                if (tab instanceof TextView) {
                    final String tabName = ((TextView) tab).getText().toString();
                    tab.setOnClickListener(v -> {
                        Toast.makeText(this, "Filtrando por: " + tabName, Toast.LENGTH_SHORT).show();
                    });
                }
            }
        }
    }

    private void setupRoadmapSteps() {
        View step = findViewById(R.id.tvStepTitle);
        if (step != null) {
            View parent = (View) step.getParent().getParent();
            parent.setOnClickListener(v -> {
                Toast.makeText(this, "Abrindo aula: " + ((TextView)step).getText(), Toast.LENGTH_SHORT).show();
            });
        }
    }
}
