package com.example.growmapapp.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.growmapapp.R;
import com.example.growmapapp.model.User;

import java.util.ArrayList;
import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    private List<User> userList;
    private List<User> userListFull;

    public UserAdapter(List<User> userList) {
        this.userList = userList;
        this.userListFull = new ArrayList<>(userList);
    }

    public void filter(String text) {
        userList = new ArrayList<>();
        if (text.isEmpty()) {
            userList.addAll(userListFull);
        } else {
            text = text.toLowerCase();
            for (User item : userListFull) {
                if (item.getFullname().toLowerCase().contains(text) || 
                    item.getGmail().toLowerCase().contains(text) ||
                    (item.getCargo() != null && item.getCargo().toLowerCase().contains(text))) {
                    userList.add(item);
                }
            }
        }
        notifyDataSetChanged();
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
        holder.tvName.setText(user.getFullname());
        
        // Prioritizes 'cargo', falls back to 'role'
        String displayRole = (user.getCargo() != null && !user.getCargo().isEmpty()) ? user.getCargo() : user.getRole();
        holder.tvRole.setText(displayRole);

        holder.tvEmail.setText(user.getGmail());
        holder.progressBar.setProgress(user.getPercentage());
        holder.tvPercentage.setText(user.getPercentage() + "%");
        holder.tvTempoValue.setText(user.getTime());
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvRole, tvEmail, tvPercentage, tvTempoValue;
        ProgressBar progressBar;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvRole = itemView.findViewById(R.id.tvRole);
            tvEmail = itemView.findViewById(R.id.tvEmail);
            progressBar = itemView.findViewById(R.id.progressBar);
            tvPercentage = itemView.findViewById(R.id.tvPercentage);
            tvTempoValue = itemView.findViewById(R.id.tvTempoValue);
        }
    }
}
