package com.example.growmapapp;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.growmapapp.adapter.UserAdapter;
import com.example.growmapapp.model.User;

import java.util.ArrayList;
import java.util.List;

public class GerenciarUser extends AppCompatActivity {

    private UserAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.gerenciar_user);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        setupToolbar();
        setupRecyclerView();
        setupSearch();
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

    private void setupToolbar() {
        View btnBack = findViewById(R.id.btnBack);
        ImageView btnThemeToggle = findViewById(R.id.btnThemeToggle);
        View btnDashboard = findViewById(R.id.btnDashboard);
        View btnCursos = findViewById(R.id.btnCursos);
        View btnSuporte = findViewById(R.id.btnSuporte);

        updateThemeIcon(btnThemeToggle);

        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

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
                android.content.Intent intent = new android.content.Intent(this, DashboardActivity.class);
                intent.setFlags(android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP | android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
            });
        }

        if (btnCursos != null) {
            btnCursos.setOnClickListener(v -> {
                startActivity(new android.content.Intent(this, RoadmapActivity.class));
            });
        }

        if (btnSuporte != null) {
            btnSuporte.setOnClickListener(v -> {
                startActivity(new android.content.Intent(this, SupportActivity.class));
            });
        }
    }

    private void setupSearch() {
        EditText etSearch = findViewById(R.id.etSearch);

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (adapter != null) {
                    adapter.filter(s.toString());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        findViewById(R.id.btnAddCollaborator).setOnClickListener(v ->
                Toast.makeText(this, "Adicionar novo colaborador", Toast.LENGTH_SHORT).show()
        );
    }

    private void setupRecyclerView() {
        RecyclerView rvUsers = findViewById(R.id.rvUsers);
        rvUsers.setLayoutManager(new LinearLayoutManager(this));

        List<User> userList = new ArrayList<>();

        userList.add(new User("Kauan Davi", "Analista de TI", "kauan.davi@email.com", 75, "2 anos"));
        userList.add(new User("Larissa Lima", "Desenvolvedora Frontend", "larissa.lima@email.com", 92, "1 ano e 3 meses"));
        userList.add(new User("Rafael Souza", "Analista de BI", "rafael.souza@email.com", 88, "6 meses"));
        userList.add(new User("Gabriel Alves", "Designer UI/UX", "gabriel.alves@email.com", 65, "2 anos e 10 meses"));
        userList.add(new User("Lucas Martins", "Suporte Técnico", "lucas.martins@email.com", 80, "1 ano e 1 mês"));

        adapter = new UserAdapter(userList);
        rvUsers.setAdapter(adapter);
    }
}