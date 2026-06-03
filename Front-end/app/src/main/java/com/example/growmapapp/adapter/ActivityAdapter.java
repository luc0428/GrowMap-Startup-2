package com.example.growmapapp.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.growmapapp.R;
import com.example.growmapapp.model.Activity;

import java.util.List;

public class ActivityAdapter extends RecyclerView.Adapter<ActivityAdapter.ActivityViewHolder> {

    private List<Activity> activityList;
    private OnItemClickListener navigateListener;
    private OnItemClickListener editListener;
    private OnDeleteClickListener deleteListener;

    public interface OnItemClickListener {
        void onItemClick(Activity activity);
    }

    public interface OnDeleteClickListener {
        void onDeleteClick(Activity activity);
    }

    public ActivityAdapter(List<Activity> activityList, OnItemClickListener navigateListener, OnItemClickListener editListener, OnDeleteClickListener deleteListener) {
        this.activityList = activityList;
        this.navigateListener = navigateListener;
        this.editListener = editListener;
        this.deleteListener = deleteListener;
    }

    @NonNull
    @Override
    public ActivityViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_generic_crud, parent, false);
        return new ActivityViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ActivityViewHolder holder, int position) {
        Activity activity = activityList.get(position);
        holder.tvTitle.setText(activity.getTitle());
        holder.tvDescription.setText(activity.getDescription());
        
        holder.btnEdit.setOnClickListener(v -> editListener.onItemClick(activity));
        holder.btnDelete.setOnClickListener(v -> deleteListener.onDeleteClick(activity));
        holder.itemView.setOnClickListener(v -> navigateListener.onItemClick(activity));
    }

    @Override
    public int getItemCount() {
        return activityList.size();
    }

    public static class ActivityViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvDescription;
        ImageView btnEdit, btnDelete;

        public ActivityViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvItemTitle);
            tvDescription = itemView.findViewById(R.id.tvItemDescription);
            btnEdit = itemView.findViewById(R.id.btnEditItem);
            btnDelete = itemView.findViewById(R.id.btnDeleteItem);
        }
    }
}
