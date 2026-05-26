package com.example.growmapapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Arrays;
import java.util.List;

public class DashboardActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private TextView userName, userRole, userAvatar, txtOverallScore;
    private RecyclerView rvFinished, rvInProgress;
    private LineChartView lineChart;

    static class Quiz {
        String icon, title, desc;
        Quiz(String i, String t, String d){ icon=i; title=t; desc=d; }
    }
    static class Hist {
        String icon, title, acertos, percent, data;
        boolean low;
        Hist(String i, String t, String a, String p, String d, boolean low){
            icon=i; title=t; acertos=a; percent=p; data=d; this.low=low;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        userName = findViewById(R.id.userName);
        userRole = findViewById(R.id.userRole);
        userAvatar = findViewById(R.id.userAvatar);
        txtOverallScore = findViewById(R.id.txtOverallScore);
        rvFinished = findViewById(R.id.rvFinished);
        rvInProgress = findViewById(R.id.rvInProgress);
        lineChart = findViewById(R.id.lineChart);
        TextView btnHistorico = findViewById(R.id.btnHistorico);

        TextView btnCursos = findViewById(R.id.btnCursos);
        TextView btnDashboard = findViewById(R.id.btnDashboard);
        TextView btnSuporte = findViewById(R.id.btnSuporte);
        View userInfoHeader = findViewById(R.id.userInfoHeader);
        ImageView btnThemeToggle = findViewById(R.id.btnThemeToggle);

        loadUserData();
        updateThemeIcon(btnThemeToggle);

        if (btnHistorico != null) {
            btnHistorico.setOnClickListener(v -> {
                startActivity(new Intent(this, GameQuizActivity.class));
            });
        }

        // Alternar Tema
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

        // Navegação
        if (btnCursos != null) {
            btnCursos.setOnClickListener(v -> {
                startActivity(new Intent(this, RoadmapActivity.class));
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

        if (btnDashboard != null) {
            btnDashboard.setOnClickListener(v -> {
                Toast.makeText(this, "Você já está na Dashboard", Toast.LENGTH_SHORT).show();
            });
        }

        if (btnSuporte != null) {
            btnSuporte.setOnClickListener(v -> {
                startActivity(new Intent(this, SupportActivity.class));
            });
        }

        setupRecyclerViews();
        setupChart();
    }



    private void setupChart() {
        if (lineChart != null) {
            List<Float> data = Arrays.asList(30f, 45f, 40f, 70f, 65f, 90f, 85f);
            List<String> labels = Arrays.asList("Seg", "Ter", "Qua", "Qui", "Sex", "Sáb", "Dom");
            lineChart.setData(data, labels);
        }
    }

    private void setupRecyclerViews() {
        if (rvFinished != null) {
            rvFinished.setLayoutManager(new LinearLayoutManager(this));
            List<Hist> finishedData = Arrays.asList(
                    new Hist("☕", "Java Fundamentos", "10 / 10", "100%", "20/10/2023", false),
                    new Hist("🌐", "Web Design", "8 / 10", "80%", "15/10/2023", false)
            );
            rvFinished.setAdapter(new HistAdapter(finishedData));
        }

        if (rvInProgress != null) {
            rvInProgress.setLayoutManager(new LinearLayoutManager(this));
            List<Hist> progressData = Arrays.asList(
                    new Hist("🐍", "Python para Dados", "7 / 10", "75%", "Em progresso", true),
                    new Hist("🎨", "UI/UX Advanced", "2 / 10", "20%", "Em progresso", true)
            );
            rvInProgress.setAdapter(new HistAdapter(progressData));
        }
    }

    private void loadUserData() {
        if (mAuth.getCurrentUser() == null) {
            setUnknownUser();
            return;
        }

        String userId = mAuth.getCurrentUser().getUid();
        db.collection("user").document(userId).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                        DocumentSnapshot doc = task.getResult();
                        String name = doc.getString("fullname");
                        String role = doc.getString("role");

                        if (userName != null) {
                            if (name != null && !name.isEmpty()) {
                                userName.setText(name);
                                if (userAvatar != null) {
                                    userAvatar.setText(String.valueOf(name.charAt(0)).toUpperCase());
                                }
                            } else {
                                userName.setText("Unknown");
                                if (userAvatar != null) userAvatar.setText("?");
                            }
                        }

                        if (userRole != null) {
                            if (role != null && !role.isEmpty()) {
                                userRole.setText(role);
                            } else {
                                userRole.setText("Bugado");
                            }
                        }
                    } else {
                        setUnknownUser();
                    }
                });
    }

    private void setUnknownUser() {
        if (userName != null) userName.setText("Unknown");
        if (userRole != null) userRole.setText("Bugado");
        if (userAvatar != null) userAvatar.setText("?");
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

    static class HistAdapter extends RecyclerView.Adapter<HistAdapter.VH> {
        List<Hist> data;
        HistAdapter(List<Hist> d){ data=d; }
        @NonNull @Override public VH onCreateViewHolder(@NonNull ViewGroup p, int v){
            return new VH(LayoutInflater.from(p.getContext()).inflate(R.layout.item_historico, p, false));
        }
        @Override public void onBindViewHolder(@NonNull VH h, int i){
            Hist x = data.get(i);
            h.icon.setText(x.icon);
            h.title.setText(x.title);
            h.acertos.setText(x.acertos);
            h.percent.setText(x.percent);
            h.data.setText(x.data);
            if (x.low){
                h.percent.setBackgroundResource(R.drawable.bg_badge_orange);
                h.percent.setTextColor(0xFFF59E0B);
            } else {
                h.percent.setBackgroundResource(R.drawable.bg_badge_green);
                h.percent.setTextColor(0xFF10B981);
            }
        }
        @Override public int getItemCount(){ return data.size(); }
        static class VH extends RecyclerView.ViewHolder {
            TextView icon,title,acertos,percent,data;
            VH(View v){
                super(v);
                icon=v.findViewById(R.id.hIcon);
                title=v.findViewById(R.id.hTitle);
                acertos=v.findViewById(R.id.hAcertos);
                percent=v.findViewById(R.id.hPercent);
                data=v.findViewById(R.id.hData);
            }
        }
    }
}
