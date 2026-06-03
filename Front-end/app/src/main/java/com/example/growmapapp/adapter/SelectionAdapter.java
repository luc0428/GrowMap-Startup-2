package com.example.growmapapp.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.growmapapp.R;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;

public class SelectionAdapter<T> extends RecyclerView.Adapter<SelectionAdapter.ViewHolder> {

    private List<T> items;
    private Function<T, String> nameGetter;
    private OnItemRemoveListener<T> removeListener;
    private int layoutResId;

    public interface OnItemRemoveListener<T> {
        void onRemove(T item, int position);
    }

    public SelectionAdapter(List<T> items, int layoutResId, Function<T, String> nameGetter, OnItemRemoveListener<T> removeListener) {
        this.items = items;
        this.layoutResId = layoutResId;
        this.nameGetter = nameGetter;
        this.removeListener = removeListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(layoutResId, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        T item = items.get(position);
        holder.tvOrderNumber.setText(String.valueOf(position + 1));
        holder.tvName.setText(nameGetter.apply(item));
        holder.btnRemove.setOnClickListener(v -> removeListener.onRemove(item, holder.getAdapterPosition()));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void onItemMove(int fromPosition, int toPosition) {
        if (fromPosition < toPosition) {
            for (int i = fromPosition; i < toPosition; i++) {
                Collections.swap(items, i, i + 1);
            }
        } else {
            for (int i = fromPosition; i > toPosition; i--) {
                Collections.swap(items, i, i - 1);
            }
        }
        notifyItemMoved(fromPosition, toPosition);
        notifyItemRangeChanged(0, items.size());
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvOrderNumber, tvName;
        ImageView btnRemove, ivDragHandle;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvOrderNumber = itemView.findViewById(R.id.tvOrderNumber);
            tvName = itemView.findViewById(R.id.tvSelectionName);
            btnRemove = itemView.findViewById(R.id.btnRemoveSelection);
            ivDragHandle = itemView.findViewById(R.id.ivDragHandle);
        }
    }
}
