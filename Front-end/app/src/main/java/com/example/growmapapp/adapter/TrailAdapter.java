package com.example.growmapapp.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.growmapapp.R;
import com.example.growmapapp.model.Trail;

import java.util.List;

public class TrailAdapter extends RecyclerView.Adapter<TrailAdapter.TrailViewHolder> {

    private List<Trail> trailList;
    private OnItemClickListener navigateListener;
    private OnItemClickListener editListener;
    private OnDeleteClickListener deleteListener;

    public interface OnItemClickListener {
        void onItemClick(Trail trail);
    }

    public interface OnDeleteClickListener {
        void onDeleteClick(Trail trail);
    }

    public TrailAdapter(List<Trail> trailList, OnItemClickListener navigateListener, OnItemClickListener editListener, OnDeleteClickListener deleteListener) {
        this.trailList = trailList;
        this.navigateListener = navigateListener;
        this.editListener = editListener;
        this.deleteListener = deleteListener;
    }

    @NonNull
    @Override
    public TrailViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_generic_crud, parent, false);
        return new TrailViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TrailViewHolder holder, int position) {
        Trail trail = trailList.get(position);
        holder.tvTitle.setText(trail.getTitle());
        holder.tvDescription.setText(trail.getDescription());
        
        holder.btnEdit.setOnClickListener(v -> editListener.onItemClick(trail));
        holder.btnDelete.setOnClickListener(v -> deleteListener.onDeleteClick(trail));
        holder.itemView.setOnClickListener(v -> navigateListener.onItemClick(trail));
    }

    @Override
    public int getItemCount() {
        return trailList.size();
    }

    public static class TrailViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvDescription;
        ImageView btnEdit, btnDelete;

        public TrailViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvItemTitle);
            tvDescription = itemView.findViewById(R.id.tvItemDescription);
            btnEdit = itemView.findViewById(R.id.btnEditItem);
            btnDelete = itemView.findViewById(R.id.btnDeleteItem);
        }
    }
}
