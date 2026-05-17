package com.example.novatelaupx;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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

    private void setupToolbar() {
        ImageView btnThemeToggle = findViewById(R.id.btnThemeToggle);

        if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES) {
            btnThemeToggle.setImageResource(android.R.drawable.ic_menu_day); // Icon de Sol
        } else {
            btnThemeToggle.setImageResource(android.R.drawable.ic_menu_recent_history); // Icon de Lua
        }

        btnThemeToggle.setOnClickListener(v -> {
            int currentMode = AppCompatDelegate.getDefaultNightMode();
            if (currentMode == AppCompatDelegate.MODE_NIGHT_YES) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            }
            recreate();
        });
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

        findViewById(R.id.btnAddCollaborator).setOnClickListener(v -> {
            Toast.makeText(this, "Adicionar novo colaborador", Toast.LENGTH_SHORT).show();
        });
    }

    private void setupRecyclerView() {
        RecyclerView rvUsers = findViewById(R.id.rvUsers);
        rvUsers.setLayoutManager(new LinearLayoutManager(this));
        List<User> userList = new ArrayList<>();
        userList.add(new User("Kauan Davi", "Analista de TI", "kauan.davi@email.com", 75, "2 anos"));
        userList.add(new User("Larissa Lima", "Desenvolvedora Frontend", "larissa.lima@email.com", 92, "1 ano e 3 meses"));
        userList.add(new User("Rafael Souza", "Analista de BI", "rafael.souza@email.com", 88, "6 meses"));
        userList.add(new User("Gabriel Alves", "Designer UI/UX", "gabriel.alves@email.com", 65, "2 anos e 10 meses"));
        userList.add(new User("Lucas Martins", "Suporte Técnico", "lucas.martins@email.com", 80, "1 ano e 1 mes"));

        adapter = new UserAdapter(userList);
        rvUsers.setAdapter(adapter);
    }
}