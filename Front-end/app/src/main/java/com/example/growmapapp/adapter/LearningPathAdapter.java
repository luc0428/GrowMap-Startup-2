package com.example.growmapapp.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.growmapapp.R;
import com.example.growmapapp.model.Activity;
import java.util.List;

public class LearningPathAdapter extends RecyclerView.Adapter<LearningPathAdapter.ViewHolder> {

    private List<Activity> activities;
    private OnActivityClickListener listener;

    public interface OnActivityClickListener {
        void onActivityClick(Activity activity);
    }

    public LearningPathAdapter(List<Activity> activities, OnActivityClickListener listener) {
        this.activities = activities;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_learning_step, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Activity activity = activities.get(position);
        holder.tvTitle.setText(activity.getTitle());

        holder.progressBar.setProgress(0); 
        holder.tvStatus.setText("Disponível");

        float translationX = (position % 2 == 0) ? -50f : 50f;
        holder.itemView.setTranslationX(translationX);

        holder.btnIcon.setOnClickListener(v -> listener.onActivityClick(activity));
    }

    @Override
    public int getItemCount() {
        return activities.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView btnIcon, tvTitle, tvStatus;
        ProgressBar progressBar;

        public ViewHolder(@NonNull View v) {
            super(v);
            btnIcon = v.findViewById(R.id.stepIcon);
            tvTitle = v.findViewById(R.id.stepTitle);
            tvStatus = v.findViewById(R.id.stepStatus);
            progressBar = v.findViewById(R.id.stepProgress);
        }
    }
}
