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

public class RoadmapNodeAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ACTIVITY = 1;
    private static final int TYPE_FOOTER = 2;

    private final List<RoadmapItem> items;
    private final OnActivityClickListener listener;

    public interface OnActivityClickListener {
        void onActivityClick(ActivityNode node);
    }

    public static class RoadmapItem {
        public int type;
        public ActivityNode activityNode;
        public String trailTitle;
        public String trailDesc;

        public RoadmapItem(int type, String title, String desc) {
            this.type = type;
            this.trailTitle = title;
            this.trailDesc = desc;
        }

        public RoadmapItem(ActivityNode node) {
            this.type = TYPE_ACTIVITY;
            this.activityNode = node;
        }
    }

    public static class ActivityNode {
        public Activity activity;
        public String pivotId;
        public int status; // 0: Done, 1: Current, 2: Locked
        public boolean sideLeft;

        public ActivityNode(Activity activity, String pivotId, int status, boolean sideLeft) {
            this.activity = activity;
            this.pivotId = pivotId;
            this.status = status;
            this.sideLeft = sideLeft;
        }
    }

    public RoadmapNodeAdapter(List<RoadmapItem> items, OnActivityClickListener listener) {
        this.items = items;
        this.listener = listener;
    }

    @Override
    public int getItemViewType(int position) {
        return items.get(position).type;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == TYPE_HEADER) {
            return new HeaderViewHolder(inflater.inflate(R.layout.item_roadmap_trail_header, parent, false));
        } else if (viewType == TYPE_FOOTER) {
            return new FooterViewHolder(inflater.inflate(R.layout.item_roadmap_trail_footer, parent, false));
        } else {
            return new ActivityViewHolder(inflater.inflate(R.layout.item_roadmap_dynamic_row, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        RoadmapItem item = items.get(position);

        if (holder instanceof HeaderViewHolder) {
            HeaderViewHolder h = (HeaderViewHolder) holder;
            h.tvTitle.setText("Trilha: " + item.trailTitle);
            h.tvDesc.setText(item.trailDesc);
        } else if (holder instanceof FooterViewHolder) {
            FooterViewHolder f = (FooterViewHolder) holder;
            f.tvTitle.setText("Fim da trilha: " + item.trailTitle);
        } else if (holder instanceof ActivityViewHolder) {
            ActivityViewHolder a = (ActivityViewHolder) holder;
            ActivityNode node = item.activityNode;

            a.tvTitleLeft.setText(node.activity.getTitle());
            a.tvDescLeft.setText(node.activity.getDescription());
            a.tvTitleRight.setText(node.activity.getTitle());
            a.tvDescRight.setText(node.activity.getDescription());

            if (node.sideLeft) {
                a.cardLeft.setVisibility(View.VISIBLE);
                a.cardRight.setVisibility(View.INVISIBLE);
            } else {
                a.cardLeft.setVisibility(View.INVISIBLE);
                a.cardRight.setVisibility(View.VISIBLE);
            }

            switch (node.status) {
                case 0: // Done
                    a.nodeGlow.setBackgroundResource(R.drawable.bg_node_glow_cyan);
                    a.nodeBg.setBackgroundResource(R.drawable.bg_node_start);
                    a.iconNode.setImageResource(R.drawable.ic_check_rounded);
                    break;
                case 1: // Current
                    a.nodeGlow.setBackgroundResource(R.drawable.bg_node_glow_purple);
                    a.nodeBg.setBackgroundResource(R.drawable.bg_node_inprogress);
                    a.iconNode.setImageResource(R.drawable.ic_hourglass);
                    break;
                case 2: // Locked
                    a.nodeGlow.setBackgroundResource(R.drawable.bg_node_glow_yellow);
                    a.nodeBg.setBackgroundResource(R.drawable.bg_node_locked);
                    a.iconNode.setImageResource(R.drawable.ic_lock);
                    break;
            }

            a.itemView.setOnClickListener(v -> listener.onActivityClick(node));
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ActivityViewHolder extends RecyclerView.ViewHolder {
        View cardLeft, cardRight, nodeGlow, nodeBg;
        TextView tvTitleLeft, tvDescLeft, tvTitleRight, tvDescRight;
        ImageView iconNode;

        ActivityViewHolder(View v) {
            super(v);
            cardLeft = v.findViewById(R.id.cardContainerLeft);
            cardRight = v.findViewById(R.id.cardContainerRight);
            nodeGlow = v.findViewById(R.id.nodeGlow);
            nodeBg = v.findViewById(R.id.nodeBg);
            iconNode = v.findViewById(R.id.iconNode);
            tvTitleLeft = cardLeft.findViewById(R.id.tvNodeTitle);
            tvDescLeft = cardLeft.findViewById(R.id.tvNodeDesc);
            tvTitleRight = cardRight.findViewById(R.id.tvNodeTitle);
            tvDescRight = cardRight.findViewById(R.id.tvNodeDesc);
        }
    }

    static class HeaderViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvDesc;
        HeaderViewHolder(View v) {
            super(v);
            tvTitle = v.findViewById(R.id.tvTrailHeaderName);
            tvDesc = v.findViewById(R.id.tvTrailHeaderDesc);
        }
    }

    static class FooterViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle;
        FooterViewHolder(View v) {
            super(v);
            tvTitle = v.findViewById(R.id.tvTrailFooterName);
        }
    }
}
