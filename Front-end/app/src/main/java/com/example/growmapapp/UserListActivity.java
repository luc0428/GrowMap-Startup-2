package com.example.growmapapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.growmapapp.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class UserListActivity extends AppCompatActivity {

    private RecyclerView rvUsers;
    private UserListAdapter adapter;
    private List<UserListItem> userList = new ArrayList<>();
    private List<UserListItem> userListFull = new ArrayList<>();
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private EditText etSearch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gerenciar_user);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        
        rvUsers = findViewById(R.id.rvUsers);
        etSearch = findViewById(R.id.etSearch);
        rvUsers.setLayoutManager(new LinearLayoutManager(this));

        adapter = new UserListAdapter(userList, user -> {
            Intent intent = new Intent(this, GerenciarUser.class);
            intent.putExtra("userId", user.id);
            startActivity(intent);
        });
        rvUsers.setAdapter(adapter);

        setupNavbar();
        setupSearch();
        loadUsers();
    }

    private void setupSearch() {
        if (etSearch != null) {
            etSearch.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                    filter(s.toString());
                }
                @Override public void afterTextChanged(Editable s) {}
            });
        }
    }

    private void filter(String query) {
        userList.clear();
        if (query.isEmpty()) {
            userList.addAll(userListFull);
        } else {
            String lowerCaseQuery = query.toLowerCase();
            for (UserListItem item : userListFull) {
                if (item.name.toLowerCase().contains(lowerCaseQuery) || 
                    item.role.toLowerCase().contains(lowerCaseQuery)) {
                    userList.add(item);
                }
            }
        }
        adapter.notifyDataSetChanged();
    }

    private void setupNavbar() {
        View btnBack = findViewById(R.id.btnBackHeader);
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());

        ImageView btnThemeToggle = findViewById(R.id.btnThemeToggle);
        if (btnThemeToggle != null) {
            updateThemeIcon(btnThemeToggle);
            btnThemeToggle.setOnClickListener(v -> {
                int mode = AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES ? 
                           AppCompatDelegate.MODE_NIGHT_NO : AppCompatDelegate.MODE_NIGHT_YES;
                AppCompatDelegate.setDefaultNightMode(mode);
                recreate();
            });
        }

        View navHome = findViewById(R.id.btnNavDashboard);
        if (navHome != null) navHome.setOnClickListener(v -> {
            startActivity(new Intent(this, DashboardActivity.class));
            finish();
        });

        View navRoadmap = findViewById(R.id.btnNavRoadmap);
        if (navRoadmap != null) navRoadmap.setOnClickListener(v -> startActivity(new Intent(this, RoadmapActivity.class)));

        View navSuporte = findViewById(R.id.btnNavSuporte);
        if (navSuporte != null) navSuporte.setOnClickListener(v -> startActivity(new Intent(this, SupportActivity.class)));

        View btnAdd = findViewById(R.id.btnAddCollaborator);
        if (btnAdd != null) btnAdd.setOnClickListener(v -> {
            startActivity(new Intent(this, RegisterActivity.class));
        });

        View btnManager = findViewById(R.id.btnManagerDashboard);
        if (btnManager != null) {
            btnManager.setOnClickListener(v -> {
                Intent intent = new Intent(this, ManagerDashboardActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
            });
            btnManager.setVisibility(checkIfAdmin() ? View.VISIBLE : View.GONE);
        }
    }

    private boolean checkIfAdmin() {
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        return sharedPreferences.getBoolean("isAdmin", false);
    }

    private void updateThemeIcon(ImageView btn) {
        if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES) {
            btn.setImageResource(R.drawable.ic_sun);
        } else {
            btn.setImageResource(R.drawable.ic_moon);
        }
    }

    private void loadUsers() {
        db.collection("user").get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                userListFull.clear();
                for (QueryDocumentSnapshot doc : task.getResult()) {
                    String id = doc.getId();
                    String name = doc.getString("fullname");
                    String cargo = doc.getString("cargo");
                    String role = doc.getString("role");
                    
                    String displayRole = (cargo != null && !cargo.isEmpty()) ? cargo : (role != null ? role : "Sem Cargo");
                    
                    userListFull.add(new UserListItem(id, name != null ? name : "Sem Nome", displayRole));
                }
                userList.clear();
                userList.addAll(userListFull);
                adapter.notifyDataSetChanged();
            } else {
                Toast.makeText(this, "Erro ao carregar usuários.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    static class UserListItem {
        String id, name, role;
        UserListItem(String id, String name, String role) {
            this.id = id; this.name = name; this.role = role;
        }
    }

    static class UserListAdapter extends RecyclerView.Adapter<UserListAdapter.VH> {
        List<UserListItem> list;
        OnUserClickListener listener;

        interface OnUserClickListener { void onClick(UserListItem user); }

        UserListAdapter(List<UserListItem> list, OnUserClickListener listener) {
            this.list = list; this.listener = listener;
        }

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user, parent, false);
            return new VH(view);
        }

        @Override
        public void onBindViewHolder(@NonNull VH holder, int position) {
            UserListItem u = list.get(position);
            holder.tvName.setText(u.name);
            holder.tvRole.setText(u.role);
            if (u.name != null && !u.name.isEmpty()) {
                holder.tvAvatar.setText(String.valueOf(u.name.charAt(0)).toUpperCase());
            }
            holder.itemView.setOnClickListener(v -> listener.onClick(u));
        }

        @Override
        public int getItemCount() { return list.size(); }

        static class VH extends RecyclerView.ViewHolder {
            TextView tvName, tvRole, tvAvatar;
            VH(View v) {
                super(v);
                tvName = v.findViewById(R.id.tvName);
                tvRole = v.findViewById(R.id.tvRole);
                tvAvatar = v.findViewById(R.id.tvUserAvatar);
            }
        }
    }
}
