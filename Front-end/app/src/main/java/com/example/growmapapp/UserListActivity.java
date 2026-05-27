package com.example.growmapapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class UserListActivity extends AppCompatActivity {

    private RecyclerView rvUsers;
    private UserAdapter adapter;
    private List<User> userList = new ArrayList<>();
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_list);

        db = FirebaseFirestore.getInstance();
        rvUsers = findViewById(R.id.rvUsers);
        rvUsers.setLayoutManager(new LinearLayoutManager(this));

        adapter = new UserAdapter(userList, user -> {
            Intent intent = new Intent(this, GerenciarUser.class);
            intent.putExtra("userId", user.id);
            startActivity(intent);
        });
        rvUsers.setAdapter(adapter);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        loadUsers();
    }

    private void loadUsers() {
        db.collection("user").get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                userList.clear();
                for (QueryDocumentSnapshot doc : task.getResult()) {
                    String id = doc.getId();
                    String name = doc.getString("fullname");
                    String role = doc.getString("role");
                    if (role == null) role = doc.getString("cargo");
                    
                    userList.add(new User(id, name != null ? name : "Sem Nome", role != null ? role : "Sem Cargo"));
                }
                adapter.notifyDataSetChanged();
            } else {
                Toast.makeText(this, "Erro ao carregar usuários.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    static class User {
        String id, name, role;
        User(String id, String name, String role) {
            this.id = id; this.name = name; this.role = role;
        }
    }

    static class UserAdapter extends RecyclerView.Adapter<UserAdapter.VH> {
        List<User> list;
        OnUserClickListener listener;

        interface OnUserClickListener { void onClick(User user); }

        UserAdapter(List<User> list, OnUserClickListener listener) {
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
            User u = list.get(position);
            holder.tvName.setText(u.name);
            holder.tvRole.setText(u.role);
            if (!u.name.isEmpty()) holder.tvAvatar.setText(String.valueOf(u.name.charAt(0)).toUpperCase());
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
