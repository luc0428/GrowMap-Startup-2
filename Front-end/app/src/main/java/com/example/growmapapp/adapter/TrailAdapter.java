package com.example.growmapapp.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.growmapapp.R;
import com.example.growmapapp.model.Trail;

import java.util.List;

public class TrailAdapter extends RecyclerView.Adapter<TrailAdapter.TrailViewHolder> {

    private List<Trail> trailList;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Trail trail);
    }

    public TrailAdapter(List<Trail> trailList, OnItemClickListener listener) {
        this.trailList = trailList;
        this.listener = listener;
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
        holder.itemView.setOnClickListener(v -> listener.onItemClick(trail));
    }

    @Override
    public int getItemCount() {
        return trailList.size();
    }

    public static class TrailViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvDescription;

        public TrailViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvItemTitle);
            tvDescription = itemView.findViewById(R.id.tvItemDescription);
        }
    }
}
