package com.example.growmapapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.growmapapp.api.CardDto;

import java.util.List;

public class QuizAdapter extends RecyclerView.Adapter<QuizAdapter.QuizViewHolder> {

    private List<CardDto> quizList;
    private OnQuizClickListener listener;

    public interface OnQuizClickListener {
        void onQuizClick(CardDto quiz);
    }

    public QuizAdapter(List<CardDto> quizList, OnQuizClickListener listener) {
        this.quizList = quizList;
        this.listener = listener;
    }

    public void setQuizzes(List<CardDto> quizzes) {
        this.quizList = quizzes;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public QuizViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_quiz_card, parent, false);
        return new QuizViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull QuizViewHolder holder, int position) {
        CardDto quiz = quizList.get(position);
        holder.icon.setText(quiz.getIcon() != null ? quiz.getIcon() : "📝");
        holder.title.setText(quiz.getTitle() != null ? quiz.getTitle() : "Quiz");
        holder.desc.setText(quiz.getDescription() != null ? quiz.getDescription() : "Descrição não disponível");

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onQuizClick(quiz);
            }
        });
    }

    @Override
    public int getItemCount() {
        return quizList != null ? quizList.size() : 0;
    }

    static class QuizViewHolder extends RecyclerView.ViewHolder {
        TextView icon, title, desc;

        QuizViewHolder(@NonNull View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.quizIcon);
            title = itemView.findViewById(R.id.quizTitle);
            desc = itemView.findViewById(R.id.quizDesc);
        }
    }
}
