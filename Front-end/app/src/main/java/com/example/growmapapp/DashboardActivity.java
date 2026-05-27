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
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DashboardActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private TextView userName, userRole, userAvatar;
    private RecyclerView rvHistorico;
    private HistAdapter histAdapter;
    private List<Hist> histData = new ArrayList<>();

    static class Quiz {
        String icon, title, desc;
        Quiz(String i, String t, String d){ icon=i; title=t; desc=d; }
    }

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
        rvHistorico = findViewById(R.id.rvHistorico);

        setupRecyclerView();
        loadUserData();
        setupQuizzes();
        loadQuizHistory();

        findViewById(R.id.btnNavRoadmap).setOnClickListener(v -> startActivity(new Intent(this, RoadmapActivity.class)));
        findViewById(R.id.btnNavSuporte).setOnClickListener(v -> startActivity(new Intent(this, SupportActivity.class)));
        findViewById(R.id.userInfoHeader).setOnClickListener(v -> startActivity(new Intent(this, ProfileActivity.class)));
        findViewById(R.id.btnManagerDashboard).setOnClickListener(v -> startActivity(new Intent(this, ManagerDashboardActivity.class)));

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

    private void setupRecyclerView() {
        rvHistorico.setLayoutManager(new LinearLayoutManager(this));
        histAdapter = new HistAdapter(histData);
        rvHistorico.setAdapter(histAdapter);
    }

    private RecyclerView rvQuizzes;
    private QuizAdapter quizAdapter;

    private void setupQuizzes() {
        rvQuizzes = findViewById(R.id.rvQuizzes);
        if (rvQuizzes == null) return;
        
        rvQuizzes.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        quizAdapter = new QuizAdapter(new ArrayList<>(), quiz -> {
            Intent intent = new Intent(this, GameQuizActivity.class);
            // Pass quiz id or title
            startActivity(intent);
        });
        rvQuizzes.setAdapter(quizAdapter);

        fetchQuizzesFromBackend();
    }

    private void fetchQuizzesFromBackend() {
        com.example.growmapapp.api.CardApiService apiService = com.example.growmapapp.api.ApiClient.getClient().create(com.example.growmapapp.api.CardApiService.class);
        apiService.getCards().enqueue(new retrofit2.Callback<List<com.example.growmapapp.api.CardDto>>() {
            @Override
            public void onResponse(retrofit2.Call<List<com.example.growmapapp.api.CardDto>> call, retrofit2.Response<List<com.example.growmapapp.api.CardDto>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    quizAdapter.setQuizzes(response.body());
                } else {
                    applyMockQuizzes();
                }
            }

            @Override
            public void onFailure(retrofit2.Call<List<com.example.growmapapp.api.CardDto>> call, Throwable t) {
                applyMockQuizzes();
            }
        });
    }

    private void applyMockQuizzes() {
        List<com.example.growmapapp.api.CardDto> mocks = Arrays.asList(
            new com.example.growmapapp.api.CardDto("☕", "Quiz: Fundamentos do Java", "Teste seus conhecimentos em variáveis, loops e sintaxe básica."),
            new com.example.growmapapp.api.CardDto("🐍", "Quiz: Python para Dados", "Desafie-se com perguntas sobre Pandas e Numpy."),
            new com.example.growmapapp.api.CardDto("📊", "Quiz: Funções do Excel", "Você domina PROCV, SOMASES e Tabelas Dinâmicas?"),
            new com.example.growmapapp.api.CardDto("🔄", "Quiz: Colaboração no O365", "Prove seu conhecimento em Teams, SharePoint e OneDrive."),
            new com.example.growmapapp.api.CardDto("🗂️", "Quiz: SQL JOINs", "Teste sua habilidade em combinar tabelas com JOINs.")
        );
        quizAdapter.setQuizzes(mocks);
    }

    private void loadQuizHistory() {
        if (mAuth.getCurrentUser() == null) {
            applyMockHistory();
            return;
        }
        String userId = mAuth.getCurrentUser().getUid();

        // Tenta carregar histórico real se houver uma coleção 'quiz_history' ou similar
        // Por enquanto, usaremos mocks para manter a UI preenchida conforme solicitado
        applyMockHistory();
    }

    private void applyMockHistory() {
        histData.clear();
        histData.add(new Hist("1", "☕", "Fundamentos do Java", "8 / 10", "80%", "26/10/2025", false));
        histData.add(new Hist("2", "📊", "Funções do Excel", "14 / 15", "93%", "25/10/2025", false));
        histData.add(new Hist("3", "🐍", "Python para Dados", "5 / 10", "50%", "24/10/2025", true));
        histData.add(new Hist("4", "🗂️", "SQL JOINs", "9 / 10", "90%", "23/10/2025", false));
        histData.add(new Hist("5", "🔄", "Colaboração no O365", "6 / 10", "60%", "22/10/2025", true));
        histAdapter.notifyDataSetChanged();
    }

    private void loadUserData() {
        if (mAuth.getCurrentUser() == null) {
            applyMockUserData();
            return;
        }
        String uid = mAuth.getCurrentUser().getUid();
        db.collection("user").document(uid).get().addOnSuccessListener(doc -> {
            if (doc.exists()) {
                updateUserUI(doc);
            } else {
                db.collection("users").document(uid).get().addOnSuccessListener(doc2 -> {
                    if (doc2.exists()) updateUserUI(doc2);
                    else applyMockUserData();
                }).addOnFailureListener(e -> applyMockUserData());
            }
        }).addOnFailureListener(e -> applyMockUserData());
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
        if (userName != null) userName.setText("Kauan Davi (Mock)");
        if (userRole != null) userRole.setText("Analista de TI");
        if (userAvatar != null) userAvatar.setText("K");
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadQuizHistory();
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
