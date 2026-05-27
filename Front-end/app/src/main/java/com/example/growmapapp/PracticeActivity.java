package com.example.growmapapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.Arrays;
import java.util.List;

public class PracticeActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private TextView userName, userRole, userAvatar;

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
        setContentView(R.layout.activity_practice);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        userName = findViewById(R.id.userName);
        userRole = findViewById(R.id.userRole);
        userAvatar = findViewById(R.id.userAvatar);

        loadUserData();

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        findViewById(R.id.btnNavDashboard).setOnClickListener(v -> finish());

        Quiz[] quizzes = new Quiz[]{
            new Quiz("☕", "Quiz: Fundamentos do Java", "Teste seus conhecimentos em variáveis, loops e sintaxe básica."),
            new Quiz("🐍", "Quiz: Python para Dados", "Desafie-se com perguntas sobre Pandas e Numpy."),
            new Quiz("📊", "Quiz: Funções do Excel", "Você domina PROCV, SOMASES e Tabelas Dinâmicas?"),
            new Quiz("🔄", "Quiz: Colaboração no O365", "Prove seu conhecimento em Teams, SharePoint e OneDrive."),
            new Quiz("🗂️", "Quiz: SQL JOINs", "Teste sua habilidade em combinar tabelas com JOINs.")
        };
        int[] ids = { R.id.quizJava, R.id.quizPython, R.id.quizExcel, R.id.quizO365, R.id.quizSql };
        for (int i=0; i<ids.length; i++){
            View v = findViewById(ids[i]);
            ((TextView)v.findViewById(R.id.quizIcon)).setText(quizzes[i].icon);
            ((TextView)v.findViewById(R.id.quizTitle)).setText(quizzes[i].title);
            ((TextView)v.findViewById(R.id.quizDesc)).setText(quizzes[i].desc);
        }

        List<Hist> hist = Arrays.asList(
            new Hist("☕","Fundamentos do Java","8 / 10","80%","26/10/2025", false),
            new Hist("📊","Funções do Excel","14 / 15","93%","25/10/2025", false),
            new Hist("🐍","Python para Dados","5 / 10","50%","24/10/2025", true),
            new Hist("🗂️","SQL JOINs","9 / 10","90%","23/10/2025", false),
            new Hist("🔄","Colaboração no O365","6 / 10","60%","22/10/2025", true)
        );

        RecyclerView rv = findViewById(R.id.rvHistorico);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(new HistAdapter(hist));
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

        if (userName != null && name != null) userName.setText(name);
        if (userRole != null && role != null) userRole.setText(role);
        if (userAvatar != null && name != null && !name.isEmpty()) {
            userAvatar.setText(name.substring(0,1).toUpperCase());
        }
    }

    private void applyMockUserData() {
        userName.setText("Kauan Davi (Mock)");
        userRole.setText("Analista de TI");
        userAvatar.setText("K");
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
