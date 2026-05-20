package com.example.growmapapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    private final List<User> userListFull;
    private List<User> userList;

    public UserAdapter(List<User> userList) {
        this.userList = userList;
        this.userListFull = new ArrayList<>(userList);
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = userList.get(position);
        holder.tvName.setText(user.getName());
        holder.tvRole.setText(user.getRole());
        holder.tvEmail.setText(user.getEmail());
        holder.progressBar.setProgress(user.getScore());
        holder.tvPercentage.setText(holder.itemView.getContext().getString(R.string.score_format, user.getScore()));
        holder.tvTempoValue.setText(user.getTenure());

        holder.btnVerPerfil.setOnClickListener(v -> {
            Toast.makeText(v.getContext(), "Perfil de " + user.getName(), Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public void filter(String text) {
        userList = new ArrayList<>();
        if (text.isEmpty()) {
            userList.addAll(userListFull);
        } else {
            text = text.toLowerCase();
            for (User item : userListFull) {
                if (item.getName().toLowerCase().contains(text) ||
                        item.getRole().toLowerCase().contains(text) ||
                        item.getEmail().toLowerCase().contains(text)) {
                    userList.add(item);
                }
            }
        }
        notifyDataSetChanged();
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvRole, tvEmail, tvPercentage, tvTempoValue, btnVerPerfil;
        ProgressBar progressBar;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvRole = itemView.findViewById(R.id.tvRole);
            tvEmail = itemView.findViewById(R.id.tvEmail);
            tvPercentage = itemView.findViewById(R.id.tvPercentage);
            tvTempoValue = itemView.findViewById(R.id.tvTempoValue);
            btnVerPerfil = itemView.findViewById(R.id.btnVerPerfil);
            progressBar = itemView.findViewById(R.id.progressBar);
        }
    }
}