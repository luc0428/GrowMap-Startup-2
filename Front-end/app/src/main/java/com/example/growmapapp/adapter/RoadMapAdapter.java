package com.example.growmapapp.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.growmapapp.R;
import com.example.growmapapp.model.RoadMap;

import java.util.List;

public class RoadMapAdapter extends RecyclerView.Adapter<RoadMapAdapter.RoadMapViewHolder> {

    private List<RoadMap> roadMapList;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(RoadMap roadMap);
    }

    public RoadMapAdapter(List<RoadMap> roadMapList, OnItemClickListener listener) {
        this.roadMapList = roadMapList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public RoadMapViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_generic_crud, parent, false);
        return new RoadMapViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RoadMapViewHolder holder, int position) {
        RoadMap roadMap = roadMapList.get(position);
        holder.tvTitle.setText(roadMap.getTitle());
        holder.tvDescription.setText(roadMap.getDescription());
        holder.itemView.setOnClickListener(v -> listener.onItemClick(roadMap));
    }

    @Override
    public int getItemCount() {
        return roadMapList.size();
    }

    public static class RoadMapViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvDescription;

        public RoadMapViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvItemTitle);
            tvDescription = itemView.findViewById(R.id.tvItemDescription);
        }
    }
}
