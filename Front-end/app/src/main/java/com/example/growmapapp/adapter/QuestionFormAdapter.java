package com.example.growmapapp.adapter;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import com.example.growmapapp.Question;
import com.example.growmapapp.R;
import java.util.Arrays;
import java.util.List;

public class QuestionFormAdapter extends RecyclerView.Adapter<QuestionFormAdapter.ViewHolder> {

    private List<Question> questions;

    public QuestionFormAdapter(List<Question> questions) {
        this.questions = questions;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_question_form, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Question q = questions.get(position);
        
        holder.etQuestion.setText(q.getQuestion());
        List<String> options = q.getOptions();
        if (options != null && options.size() >= 4) {
            holder.etOp1.setText(options.get(0));
            holder.etOp2.setText(options.get(1));
            holder.etOp3.setText(options.get(2));
            holder.etOp4.setText(options.get(3));
        }

        // Set correct radio button based on saved state
        int correct = q.getCorrectAnswer();
        if (correct == 0) holder.rbOp1.setChecked(true);
        else if (correct == 1) holder.rbOp2.setChecked(true);
        else if (correct == 2) holder.rbOp3.setChecked(true);
        else if (correct == 3) holder.rbOp4.setChecked(true);

        holder.btnRemove.setOnClickListener(v -> {
            int pos = holder.getAdapterPosition();
            if (pos != RecyclerView.NO_POSITION) {
                questions.remove(pos);
                notifyItemRemoved(pos);
            }
        });

        TextWatcher watcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateQuestionState(holder);
            }
            @Override
            public void afterTextChanged(Editable s) {}
        };

        holder.etQuestion.addTextChangedListener(watcher);
        holder.etOp1.addTextChangedListener(watcher);
        holder.etOp2.addTextChangedListener(watcher);
        holder.etOp3.addTextChangedListener(watcher);
        holder.etOp4.addTextChangedListener(watcher);

        holder.rgCorrect.setOnCheckedChangeListener((group, checkedId) -> {
            updateQuestionState(holder);
        });
    }

    private void updateQuestionState(ViewHolder holder) {
        int pos = holder.getAdapterPosition();
        if (pos == RecyclerView.NO_POSITION) return;

        int correctIndex = 0;
        int checkedId = holder.rgCorrect.getCheckedRadioButtonId();
        if (checkedId == R.id.rbOption1) correctIndex = 0;
        else if (checkedId == R.id.rbOption2) correctIndex = 1;
        else if (checkedId == R.id.rbOption3) correctIndex = 2;
        else if (checkedId == R.id.rbOption4) correctIndex = 3;

        Question currentQ = questions.get(pos);
        currentQ.setQuestion(holder.etQuestion.getText().toString());
        currentQ.setOptions(Arrays.asList(
                holder.etOp1.getText().toString(),
                holder.etOp2.getText().toString(),
                holder.etOp3.getText().toString(),
                holder.etOp4.getText().toString()
        ));
        currentQ.setCorrectAnswer(correctIndex);
    }

    @Override
    public int getItemCount() {
        return questions.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        EditText etQuestion, etOp1, etOp2, etOp3, etOp4;
        TextView btnRemove;
        RadioGroup rgCorrect;
        RadioButton rbOp1, rbOp2, rbOp3, rbOp4;

        public ViewHolder(@NonNull View v) {
            super(v);
            etQuestion = v.findViewById(R.id.etQuestionText);
            etOp1 = v.findViewById(R.id.etOption1);
            etOp2 = v.findViewById(R.id.etOption2);
            etOp3 = v.findViewById(R.id.etOption3);
            etOp4 = v.findViewById(R.id.etOption4);
            btnRemove = v.findViewById(R.id.btnRemoveQuestion);
            rgCorrect = v.findViewById(R.id.rgCorrectAnswer);
            rbOp1 = v.findViewById(R.id.rbOption1);
            rbOp2 = v.findViewById(R.id.rbOption2);
            rbOp3 = v.findViewById(R.id.rbOption3);
            rbOp4 = v.findViewById(R.id.rbOption4);
        }
    }
}
