package com.example.growmapapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import androidx.core.content.ContextCompat;
import android.util.TypedValue;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.PopupMenu;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

public class SupportActivity extends AppCompatActivity {

    private TextView userAvatar, userName, userRole;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_support);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        userAvatar = findViewById(R.id.userAvatar);
        userName = findViewById(R.id.userName);
        userRole = findViewById(R.id.userRole);

        loadUserData();
        setupNavbar();
        setupFAQ();
        setupChat();
    }

    private void loadUserData() {
        if (mAuth.getCurrentUser() != null) {
            String uid = mAuth.getCurrentUser().getUid();
            db.collection("user").document(uid).get().addOnSuccessListener(doc -> {
                if (doc.exists()) {
                    String name = doc.getString("fullname");
                    String role = doc.getString("role");
                    
                    if (name != null && !name.isEmpty()) {
                        if (userAvatar != null) userAvatar.setText(String.valueOf(name.charAt(0)).toUpperCase());
                        if (userName != null) userName.setText(name);
                    }
                    
                    if (role != null && !role.isEmpty()) {
                        if (userRole != null) userRole.setText(role);
                        
                        boolean isManager = role.equalsIgnoreCase("Gestor") || role.equalsIgnoreCase("Gerente") || role.equalsIgnoreCase("Admin");
                        View btnManager = findViewById(R.id.btnManagerDashboard);
                        if (btnManager != null) {
                            btnManager.setVisibility(isManager ? View.VISIBLE : View.GONE);
                        }
                    }
                }
            });
        }
    }

    private void setupNavbar() {
        View btnThemeToggle = findViewById(R.id.btnThemeToggle);
        View btnNavHome = findViewById(R.id.btnNavHome);
        View btnNavRoadmap = findViewById(R.id.btnNavRoadmap);
        View btnManagerDashboard = findViewById(R.id.btnManagerDashboard);
        
        if (userAvatar != null) {
            userAvatar.setOnClickListener(v -> startActivity(new Intent(this, ProfileActivity.class)));
        }

        if (btnManagerDashboard != null) {
            btnManagerDashboard.setOnClickListener(v -> startActivity(new Intent(this, ManagerDashboardActivity.class)));
        }
        
        updateThemeIcon((ImageView) btnThemeToggle);

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
    }

    private void setupFAQ() {
        LinearLayout faqContainer = findViewById(R.id.faqContainer);
        String[][] faqs = {
            {"Como altero minha senha?", "Você pode alterar sua senha na tela de Gerenciamento de Usuário clicando no seu perfil."},
            {"Onde vejo meus cursos?", "Todos os seus cursos estão listados na aba 'Cursos' do menu superior."},
            {"Como entrar em contato?", "Você pode usar este chat ou enviar um e-mail para suporte@growmap.com."}
        };

        for (String[] faq : faqs) {
            View itemView = LayoutInflater.from(this).inflate(android.R.layout.simple_list_item_2, faqContainer, false);
            TextView text1 = itemView.findViewById(android.R.id.text1);
            TextView text2 = itemView.findViewById(android.R.id.text2);

            text1.setText(faq[0]);
            text1.setTextColor(getResources().getColor(R.color.text_primary));
            text1.setTextSize(16);
            
            text2.setText(faq[1]);
            text2.setTextColor(getResources().getColor(R.color.text_secondary));
            text2.setVisibility(View.GONE);
            text2.setPadding(0, 8, 0, 16);

            itemView.setOnClickListener(v -> {
                if (text2.getVisibility() == View.GONE) {
                    text2.setVisibility(View.VISIBLE);
                } else {
                    text2.setVisibility(View.GONE);
                }
            });

            faqContainer.addView(itemView);
        }
    }

    private void setupChat() {
        android.webkit.WebView webView = findViewById(R.id.botpressWebView);
        if (webView != null) {
            android.webkit.WebSettings webSettings = webView.getSettings();
            webSettings.setJavaScriptEnabled(true);
            webSettings.setDomStorageEnabled(true);
            
            // Aqui você deve colocar a URL fornecida pelo Botpress quando publicar seu bot
            webView.loadUrl("https://mediafiles.botpress.cloud/YOUR_BOT_ID/webchat/bot.html");
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
