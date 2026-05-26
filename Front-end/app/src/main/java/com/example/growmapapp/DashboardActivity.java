package com.example.growmapapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
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
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class DashboardActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private TextView userName, userRole, userAvatar, txtOverallScore;
    private RecyclerView rvFinished, rvInProgress;
    private HistAdapter finishedAdapter, progressAdapter;
    private List<Hist> fullFinishedData = new ArrayList<>(), fullProgressData = new ArrayList<>();

    static class Hist {
        String id, icon, title, acertos, percent, data;
        boolean low;
        Hist(String id, String i, String t, String a, String p, String d, boolean low){
            this.id = id; icon=i; title=t; acertos=a; percent=p; data=d; this.low=low;
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

        setupRecyclerViews();
        loadUserData();
        loadRealRoadmaps();

        findViewById(R.id.btnNavRoadmap).setOnClickListener(v -> startActivity(new Intent(this, RoadmapActivity.class)));
        findViewById(R.id.btnNavSuporte).setOnClickListener(v -> startActivity(new Intent(this, SupportActivity.class)));
        findViewById(R.id.userInfoHeader).setOnClickListener(v -> startActivity(new Intent(this, ProfileActivity.class)));
        
        ImageView btnThemeToggle = findViewById(R.id.btnThemeToggle);
        if (btnThemeToggle != null) {
            btnThemeToggle.setOnClickListener(v -> {
                int mode = AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES ? 
                           AppCompatDelegate.MODE_NIGHT_NO : AppCompatDelegate.MODE_NIGHT_YES;
                AppCompatDelegate.setDefaultNightMode(mode);
                recreate();
            });
        }
    }

    private void setupRecyclerViews() {
        rvFinished.setLayoutManager(new LinearLayoutManager(this));
        rvInProgress.setLayoutManager(new LinearLayoutManager(this));
        finishedAdapter = new HistAdapter(fullFinishedData);
        progressAdapter = new HistAdapter(fullProgressData);
        rvFinished.setAdapter(finishedAdapter);
        rvInProgress.setAdapter(progressAdapter);
    }

    private void loadRealRoadmaps() {
        if (mAuth.getCurrentUser() == null) {
            applyMockRoadmaps();
            return;
        }
        String userId = mAuth.getCurrentUser().getUid();

        db.collection("users").document(userId).collection("roadmaps")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (querySnapshot.isEmpty()) {
                        applyMockRoadmaps();
                    } else {
                        fullProgressData.clear();
                        fullFinishedData.clear();
                        for (DocumentSnapshot doc : querySnapshot) {
                            String name = doc.getString("name");
                            String icon = doc.getString("icon") != null ? doc.getString("icon") : "📍";
                            fullProgressData.add(new Hist(doc.getId(), icon, name, "Ver trilha", "Andamento", "Ativo", true));
                        }
                        progressAdapter.updateData(new ArrayList<>(fullProgressData));
                        finishedAdapter.updateData(new ArrayList<>(fullFinishedData));
                    }
                })
                .addOnFailureListener(e -> applyMockRoadmaps());
    }

    private void applyMockRoadmaps() {
        fullProgressData.clear();
        fullFinishedData.clear();
        
        // Dados Mockados de exemplo
        fullProgressData.add(new Hist("mock1", "☕", "Java Fundamentos (Mock)", "7/10", "70%", "Ativo", true));
        fullProgressData.add(new Hist("mock2", "🚀", "Android Especialista (Mock)", "2/15", "13%", "Em breve", true));
        
        fullFinishedData.add(new Hist("mock3", "✅", "Lógica de Programação", "10/10", "100%", "Concluído", false));
        
        progressAdapter.updateData(new ArrayList<>(fullProgressData));
        finishedAdapter.updateData(new ArrayList<>(fullFinishedData));
    }

    private void loadUserData() {
        if (mAuth.getCurrentUser() == null) {
            applyMockUserData();
            return;
        }
        String uid = mAuth.getCurrentUser().getUid();
        
        // Tenta buscar na coleção 'user' (singular) conforme snapshot
        db.collection("user").document(uid).get()
            .addOnSuccessListener(doc -> {
                if (doc.exists()) {
                    updateUserUI(doc);
                } else {
                    // Tenta 'users' (plural) se não achar em 'user'
                    db.collection("users").document(uid).get().addOnSuccessListener(doc2 -> {
                        if (doc2.exists()) {
                            updateUserUI(doc2);
                        } else {
                            applyMockUserData();
                        }
                    }).addOnFailureListener(e -> applyMockUserData());
                }
            })
            .addOnFailureListener(e -> applyMockUserData());
    }

    private void updateUserUI(DocumentSnapshot doc) {
        String name = doc.getString("fullname");
        if (name == null) name = doc.getString("name");
        
        String role = doc.getString("role");
        if (role == null) role = doc.getString("cargo");
        if (role == null) role = doc.getString("position");
        
        if (userName != null) userName.setText(name != null ? name : "Usuário");
        if (userRole != null) userRole.setText(role != null ? role : "Explorador");
        if (userAvatar != null && name != null && !name.isEmpty()) {
            userAvatar.setText(name.substring(0,1).toUpperCase());
        }
    }

    private void applyMockUserData() {
        if (userName != null) userName.setText("Usuário Teste");
        if (userRole != null) userRole.setText("Desenvolvedor Mock");
        if (userAvatar != null) userAvatar.setText("U");
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadRealRoadmaps();
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
            h.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(v.getContext(), RoadmapActivity.class);
                intent.putExtra("roadmapId", x.id); // Passa o ID para abrir a trilha certa
                v.getContext().startActivity(intent);
            });
        }
        @Override public int getItemCount(){ return data.size(); }
        public void updateData(List<Hist> newData) {
            this.data.clear();
            this.data.addAll(newData);
            notifyDataSetChanged();
        }
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
