package com.example.growmapapp;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class GameQuizActivity extends AppCompatActivity {

    TextView txtQuestion, txtProgress, txtResult;
    ProgressBar progressBar;

    RadioGroup radioGroup;
    RadioButton option1, option2, option3, option4;

    Button btnNext, btnExit;

    ArrayList<Question> questions = new ArrayList<>();

    int currentQuestion = 0;
    int score = 0;

    private String roadmapId, stepId;
    private com.google.firebase.firestore.FirebaseFirestore db;
    private com.google.firebase.auth.FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_quiz);

        db = com.google.firebase.firestore.FirebaseFirestore.getInstance();
        mAuth = com.google.firebase.auth.FirebaseAuth.getInstance();

        roadmapId = getIntent().getStringExtra("roadmapId");
        stepId = getIntent().getStringExtra("stepId");

        txtQuestion = findViewById(R.id.txtQuestion);
        txtProgress = findViewById(R.id.txtProgress);
        txtResult = findViewById(R.id.txtResult);

        progressBar = findViewById(R.id.progressBar);

        radioGroup = findViewById(R.id.radioGroup);

        option1 = findViewById(R.id.option1);
        option2 = findViewById(R.id.option2);
        option3 = findViewById(R.id.option3);
        option4 = findViewById(R.id.option4);

        btnNext = findViewById(R.id.btnNext);
        btnExit = findViewById(R.id.btnExit);

        loadQuestions();
        showQuestion();

        btnNext.setOnClickListener(v -> {

            int selectedId = radioGroup.getCheckedRadioButtonId();

            if(selectedId == -1){
                Toast.makeText(this, "Selecione uma opção", Toast.LENGTH_SHORT).show();
                return;
            }

            RadioButton selected = findViewById(selectedId);

            int answerIndex = radioGroup.indexOfChild(selected);

            if(answerIndex == questions.get(currentQuestion).getCorrectAnswer()){
                score++;
            }

            currentQuestion++;

            if(currentQuestion < questions.size()){
                showQuestion();
            } else {
                showFinalResult();
            }
        });
    }

    private void showQuestion(){

        radioGroup.clearCheck();

        Question q = questions.get(currentQuestion);

        txtQuestion.setText(q.getQuestion());

        option1.setText(q.getOptions()[0]);
        option2.setText(q.getOptions()[1]);
        option3.setText(q.getOptions()[2]);
        option4.setText(q.getOptions()[3]);

        txtProgress.setText("Pergunta " + (currentQuestion + 1) + " de 5");

        progressBar.setProgress((currentQuestion + 1) * 20);
    }

    private void showFinalResult(){

        txtQuestion.setText("Quiz Finalizado ☕");

        radioGroup.setVisibility(RadioGroup.GONE);

        btnNext.setVisibility(Button.GONE);
        btnExit.setVisibility(Button.VISIBLE);
        
        // Se o usuário acertou mais de 60%, marcamos como concluído
        if (score >= 3 && mAuth.getCurrentUser() != null && roadmapId != null && stepId != null) {
            updateStepStatus();
        }

        btnExit.setOnClickListener(v -> finish());

        txtResult.setText("Você acertou " + score + " de " + questions.size() + " perguntas!");

        if(score == 5){
            txtResult.setTextColor(Color.CYAN);
            txtResult.append("\n\nMestre Java 🔥");
        }
        else if(score >= 3){
            txtResult.setTextColor(Color.GREEN);
            txtResult.append("\n\nQuase especialista 😎");
        }
        else{
            txtResult.setTextColor(Color.RED);
            txtResult.append("\n\nContinue estudando 📚");
        }
    }

    private void updateStepStatus() {
        String userId = mAuth.getCurrentUser().getUid();
        db.collection("users").document(userId)
                .collection("roadmaps").document(roadmapId)
                .collection("steps").document(stepId)
                .update("status", "completed")
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Progresso salvo!", Toast.LENGTH_SHORT).show();
                    unlockNextStep(userId);
                });
    }

    private void unlockNextStep(String userId) {
        // Busca a próxima tarefa baseada na ordem
        db.collection("users").document(userId)
                .collection("roadmaps").document(roadmapId)
                .collection("steps").document(stepId).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists() && doc.contains("order")) {
                        long currentOrder = doc.getLong("order");
                        db.collection("users").document(userId)
                                .collection("roadmaps").document(roadmapId)
                                .collection("steps")
                                .whereEqualTo("order", currentOrder + 1)
                                .limit(1)
                                .get()
                                .addOnSuccessListener(query -> {
                                    if (!query.isEmpty()) {
                                        query.getDocuments().get(0).getReference().update("status", "in_progress");
                                    }
                                });
                    }
                });
    }

    private void loadQuestions(){

        questions.add(new Question(
                "O que é o 'Bytecode' no contexto do Java?",
                new String[]{"Código fonte original", "Código compilado para a JVM", "Um tipo de dado primitivo", "Um erro de compilação"},
                1
        ));

        questions.add(new Question(
                "Qual o objetivo da palavra-chave 'static'?",
                new String[]{"Tornar a variável constante", "Permitir acesso sem instanciar", "Ocultar o método", "Otimizar o uso de RAM"},
                1
        ));

        questions.add(new Question(
                "Qual destas não é uma característica do POO?",
                new String[]{"Encapsulamento", "Polimorfismo", "Compilação Dinâmica", "Abstração"},
                2
        ));

        questions.add(new Question(
                "O que acontece se um erro não for tratado com try-catch?",
                new String[]{"O app fecha (Crash)", "O erro é ignorado", "O Java autocorrige", "O compilador avisa"},
                0
        ));

        questions.add(new Question(
                "Para que serve a 'Garbage Collection'?",
                new String[]{"Limpar o código fonte", "Liberar memória não usada", "Excluir arquivos temporários", "Gerenciar banco de dados"},
                1
        ));
    }
}
