package com.example.growmapapp.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.example.growmapapp.R;
import com.example.growmapapp.model.Activity;
import com.example.growmapapp.model.ActivityTrail;
import java.util.List;

public class LearningPathAdapter extends RecyclerView.Adapter<LearningPathAdapter.ViewHolder> {

    private static final int VIEW_TYPE_LEFT = 0;
    private static final int VIEW_TYPE_RIGHT = 1;

    public static class TrailStep {
        public Activity activity;
        public ActivityTrail pivot;
        public TrailStep(Activity activity, ActivityTrail pivot) {
            this.activity = activity;
            this.pivot = pivot;
        }
    }

    private List<TrailStep> steps;
    private OnActivityClickListener listener;

    public interface OnActivityClickListener {
        void onActivityClick(Activity activity);
    }

    public LearningPathAdapter(List<TrailStep> steps, OnActivityClickListener listener) {
        this.steps = steps;
        this.listener = listener;
    }

    @Override
    public int getItemViewType(int position) {
        return (position % 2 == 0) ? VIEW_TYPE_LEFT : VIEW_TYPE_RIGHT;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int layoutRes = (viewType == VIEW_TYPE_LEFT) ? R.layout.item_trail_left : R.layout.item_trail_right;
        View view = LayoutInflater.from(parent.getContext()).inflate(layoutRes, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TrailStep step = steps.get(position);
        Activity activity = step.activity;
        ActivityTrail pivot = step.pivot;

        holder.tvTitle.setText(activity.getTitle());
        holder.tvNodeIcon.setText(String.valueOf(position + 1));
        
        if (pivot != null && pivot.isCompleted()) {
            holder.progressBar.setProgress(100);
            holder.tvStatus.setText("Concluído");
            holder.tvStatus.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.accent_cyan));
        } else {
            holder.progressBar.setProgress(0);
            holder.tvStatus.setText("Pendente");
            holder.tvStatus.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.text_secondary));
        }

        holder.stepCard.setOnClickListener(v -> listener.onActivityClick(activity));
    }

    @Override
    public int getItemCount() {
        return steps.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        View stepCard;
        TextView tvTitle, tvStatus, tvNodeIcon;
        ProgressBar progressBar;

        public ViewHolder(@NonNull View v) {
            super(v);
            stepCard = v.findViewById(R.id.stepCard);
            tvTitle = v.findViewById(R.id.stepTitle);
            tvStatus = v.findViewById(R.id.stepStatus);
            tvNodeIcon = v.findViewById(R.id.stepNodeIcon);
            progressBar = v.findViewById(R.id.stepProgress);
        }
    }
}
