package com.example.growmapapp;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class GameQuizActivity extends AppCompatActivity {

    TextView txtQuestion, txtProgress, txtResult;
    ProgressBar progressBar;

    RadioGroup radioGroup;
    RadioButton option1, option2, option3, option4;

    Button btnNext, btnExit;

    ArrayList<Question> questions = new ArrayList<>();

    int currentQuestion = 0;
    int score = 0;

    private String activityId, roadmapId, stepId;
    private com.google.firebase.firestore.FirebaseFirestore db;
    private com.google.firebase.auth.FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_quiz);

        db = com.google.firebase.firestore.FirebaseFirestore.getInstance();
        mAuth = com.google.firebase.auth.FirebaseAuth.getInstance();

        activityId = getIntent().getStringExtra("activityId");
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

        if (activityId != null) {
            loadQuestionsFromFirestore();
        } else {
            loadQuestions();
            showQuestion();
        }

        btnNext.setOnClickListener(v -> {
            int selectedId = radioGroup.getCheckedRadioButtonId();

            if(selectedId == -1){
                Toast.makeText(this, "Selecione uma opção", Toast.LENGTH_SHORT).show();
                return;
            }

            RadioButton selected = findViewById(selectedId);
            int answerIndex = -1;
            if (selectedId == R.id.option1) answerIndex = 0;
            else if (selectedId == R.id.option2) answerIndex = 1;
            else if (selectedId == R.id.option3) answerIndex = 2;
            else if (selectedId == R.id.option4) answerIndex = 3;

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

    private void loadQuestionsFromFirestore() {
        db.collection("activity").document(activityId).get().addOnSuccessListener(documentSnapshot -> {
            com.example.growmapapp.model.Activity activity = documentSnapshot.toObject(com.example.growmapapp.model.Activity.class);
            if (activity != null && activity.getQuestions() != null && !activity.getQuestions().isEmpty()) {
                questions.clear();
                questions.addAll(activity.getQuestions());
                currentQuestion = 0;
                score = 0;
                showQuestion();
            } else {
                Toast.makeText(this, "Nenhuma questão encontrada para este quiz.", Toast.LENGTH_SHORT).show();
                finish();
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Erro ao carregar quiz: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            finish();
        });
    }

    private void showQuestion(){
        if (questions.isEmpty()) return;

        radioGroup.clearCheck();
        Question q = questions.get(currentQuestion);

        txtQuestion.setText(q.getQuestion());
        
        List<String> options = q.getOptions();
        option1.setText(options.size() > 0 ? options.get(0) : "");
        option2.setText(options.size() > 1 ? options.get(1) : "");
        option3.setText(options.size() > 2 ? options.get(2) : "");
        option4.setText(options.size() > 3 ? options.get(3) : "");

        txtProgress.setText("Pergunta " + (currentQuestion + 1) + " de " + questions.size());
        progressBar.setMax(questions.size());
        progressBar.setProgress(currentQuestion + 1);
    }

    private void showFinalResult(){
        txtQuestion.setText("Quiz Finalizado! 🎯");
        radioGroup.setVisibility(RadioGroup.GONE);
        btnNext.setVisibility(Button.GONE);
        btnExit.setVisibility(Button.VISIBLE);
        
        // Salvar no Histórico (Back-end)
        saveQuizResultToFirestore();

        if (score >= (questions.size() * 0.6) && mAuth.getCurrentUser() != null && roadmapId != null && stepId != null) {
            updateStepStatus();
        }

        btnExit.setOnClickListener(v -> finish());
        txtResult.setText("Você acertou " + score + " de " + questions.size() + " perguntas!");

        if(score == questions.size()){
            txtResult.setTextColor(Color.CYAN);
            txtResult.append("\n\nExcelente! Você dominou o assunto! 🔥");
        }
        else if(score >= questions.size() * 0.7){
            txtResult.setTextColor(Color.GREEN);
            txtResult.append("\n\nMuito bom! Quase lá! 😎");
        }
        else{
            txtResult.setTextColor(Color.RED);
            txtResult.append("\n\nContinue praticando! 📚");
        }
    }

    private void saveQuizResultToFirestore() {
        if (mAuth.getCurrentUser() == null) return;
        String userId = mAuth.getCurrentUser().getUid();
        
        java.util.Map<String, Object> result = new java.util.HashMap<>();
        result.put("userId", userId);
        result.put("activityId", activityId);
        result.put("score", score);
        result.put("totalQuestions", questions.size());
        result.put("timestamp", com.google.firebase.Timestamp.now());
        
        db.collection("quiz_results").add(result);
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
                java.util.Arrays.asList("Código fonte original", "Código compilado para a JVM", "Um tipo de dado primitivo", "Um erro de compilação"),
                1
        ));

        questions.add(new Question(
                "Qual o objetivo da palavra-chave 'static'?",
                java.util.Arrays.asList("Tornar a variável constante", "Permitir acesso sem instanciar", "Ocultar o método", "Otimizar o uso de RAM"),
                1
        ));

        questions.add(new Question(
                "Qual destas não é uma característica do POO?",
                java.util.Arrays.asList("Encapsulamento", "Polimorfismo", "Compilação Dinâmica", "Abstração"),
                2
        ));

        questions.add(new Question(
                "O que acontece se um erro não for tratado com try-catch?",
                java.util.Arrays.asList("O app fecha (Crash)", "O erro é ignorado", "O Java autocorrige", "O compilador avisa"),
                0
        ));

        questions.add(new Question(
                "Para que serve a 'Garbage Collection'?",
                java.util.Arrays.asList("Limpar o código fonte", "Liberar memória não usada", "Excluir arquivos temporários", "Gerenciar banco de dados"),
                1
        ));
    }
}
