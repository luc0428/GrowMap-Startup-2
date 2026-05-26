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

        Button btnNext;

        ArrayList<Question> questions = new ArrayList<>();

        int currentQuestion = 0;
        int score = 0;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_game_quiz);


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

            btnNext.setEnabled(false);
            btnNext.setText("FINALIZADO");

            txtResult.setText("Você acertou " + score + " de 5 perguntas!");

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

        private void loadQuestions(){

            questions.add(new Question(
                    "Qual palavra cria objetos em Java?",
                    new String[]{"void", "new", "class", "int"},
                    1
            ));

            questions.add(new Question(
                    "Java é uma linguagem:",
                    new String[]{"Banco de Dados", "Sistema", "Orientada a Objetos", "Hardware"},
                    2
            ));

            questions.add(new Question(
                    "Qual método inicia o programa?",
                    new String[]{"run()", "main()", "start()", "java()"},
                    1
            ));

            questions.add(new Question(
                    "Qual símbolo finaliza comandos?",
                    new String[]{".", ";", ",", ":"},
                    1
            ));

            questions.add(new Question(
                    "Qual tipo armazena texto?",
                    new String[]{"boolean", "double", "String", "int"},
                    2
            ));
        }
    }