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
        setupReports();
    }

    private void setupNavbar() {
        ImageView btnThemeToggle = findViewById(R.id.btnThemeToggle);
        View userAvatarView = findViewById(R.id.userAvatar);

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
                startActivity(new Intent(this, UserListActivity.class));
            });
        }
    }

    private void setupCharts() {
        LineChartView chart = findViewById(R.id.teamChart);
        if (chart != null) {
            // Tenta buscar as pontuações gerais reais do Firebase (Simulação com coleção "teste")
            db.collection("stats").document("performance").get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists() && doc.contains("scores")) {
                        java.util.List<Double> doubleScores = (java.util.List<Double>) doc.get("scores");
                        java.util.List<Float> data = new java.util.ArrayList<>();
                        for (Double d : doubleScores) { data.add(d.floatValue()); }
                        java.util.List<String> labels = (java.util.List<String>) doc.get("labels");
                        if (labels == null) labels = java.util.Arrays.asList("Jan", "Fev", "Mar", "Abr", "Mai", "Jun", "Jul");
                        chart.setData(data, labels);
                    } else {
                        // Dados Mockados se não houver backend configurado para testes
                        chart.setData(java.util.Arrays.asList(40f, 55f, 60f, 65f, 75f, 80f, 85f), 
                                      java.util.Arrays.asList("Jan", "Fev", "Mar", "Abr", "Mai", "Jun", "Jul"));
                    }
                })
                .addOnFailureListener(e -> {
                    chart.setData(java.util.Arrays.asList(40f, 55f, 60f, 65f, 75f, 80f, 85f), 
                                  java.util.Arrays.asList("Jan", "Fev", "Mar", "Abr", "Mai", "Jun", "Jul"));
                });
        }
    }

    private void setupReports() {
        androidx.recyclerview.widget.RecyclerView rvReports = findViewById(R.id.rvRecentReports);
        if (rvReports != null) {
            rvReports.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(this));
            
            // Simulação de busca dos últimos quizzes concluídos no Firebase
            db.collection("quiz_results").orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING).limit(5).get()
                .addOnSuccessListener(querySnapshots -> {
                    java.util.List<Report> reports = new java.util.ArrayList<>();
                    if (!querySnapshots.isEmpty()) {
                        for (DocumentSnapshot doc : querySnapshots) {
                            reports.add(new Report(doc.getString("userName"), doc.getString("quizName"), doc.getLong("score")));
                        }
                    } else {
                        // Mock fallback
                        reports.add(new Report("Kauan Davi", "Fundamentos Java", 90L));
                        reports.add(new Report("Ana Silva", "Android UI", 85L));
                        reports.add(new Report("Carlos Souza", "Botpress IA", 100L));
                    }
                    rvReports.setAdapter(new ReportAdapter(reports));
                })
                .addOnFailureListener(e -> {
                    java.util.List<Report> reports = new java.util.ArrayList<>();
                    reports.add(new Report("Kauan Davi (Mock)", "Fundamentos Java", 90L));
                    rvReports.setAdapter(new ReportAdapter(reports));
                });
        }
    }

    static class Report {
        String userName, quizName;
        Long score;
        Report(String u, String q, Long s) { userName = u; quizName = q; score = s; }
    }

    static class ReportAdapter extends androidx.recyclerview.widget.RecyclerView.Adapter<ReportAdapter.VH> {
        java.util.List<Report> data;
        ReportAdapter(java.util.List<Report> data) { this.data = data; }

        @androidx.annotation.NonNull
        @Override
        public VH onCreateViewHolder(@androidx.annotation.NonNull android.view.ViewGroup parent, int viewType) {
            android.view.View v = android.view.LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_2, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(@androidx.annotation.NonNull VH holder, int position) {
            Report r = data.get(position);
            holder.text1.setText(r.userName + " finalizou " + r.quizName);
            holder.text1.setTextColor(android.graphics.Color.WHITE);
            holder.text1.setTextSize(14f);
            holder.text2.setText("Pontuação: " + (r.score != null ? r.score : 0) + "%");
            holder.text2.setTextColor(android.graphics.Color.LTGRAY);
        }

        @Override
        public int getItemCount() { return data.size(); }

        static class VH extends androidx.recyclerview.widget.RecyclerView.ViewHolder {
            TextView text1, text2;
            VH(android.view.View v) {
                super(v);
                text1 = v.findViewById(android.R.id.text1);
                text2 = v.findViewById(android.R.id.text2);
            }
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
