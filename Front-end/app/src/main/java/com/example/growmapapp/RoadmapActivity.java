package com.example.growmapapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.ArrayList;
import java.util.List;

public class RoadmapActivity extends AppCompatActivity {

    private List<String> selectedTrails = new ArrayList<>();
    
    // Estados do Roadmap (Simulando persistência)
    private boolean isJourneyStarted = false;
    private boolean logicaCompleted = false;
    private boolean ooUnlocked = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_roadmap);
        setupNavbar();
        setupCreateButton();
        updateRoadmapUI();
    }

    private void setupCreateButton() {
        View btnCreate = findViewById(R.id.btnCreateRoad);
        if (btnCreate != null) {
            btnCreate.setOnClickListener(v -> showCreateOptionsDialog());
        }
    }

    private void showCreateOptionsDialog() {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_create_roadmap, null);
        
        // Listener para o card todo ou especificamente para o botão "Entrar"
        View.OnClickListener createRoadmapAction = v -> {
            dialog.dismiss();
            showRoadmapFormDialog();
        };
        
        view.findViewById(R.id.optionCreateRoadmap).setOnClickListener(createRoadmapAction);
        view.findViewById(R.id.btnEnterCreateRoadmap).setOnClickListener(createRoadmapAction);
        
        view.findViewById(R.id.optionCreateTask).setOnClickListener(v -> {
            Toast.makeText(this, "Criar Tarefa clicado", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });
        
        view.findViewById(R.id.optionCreateTrail).setOnClickListener(v -> {
            Toast.makeText(this, "Criar Trilhas clicado", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });

        dialog.setContentView(view);
        dialog.show();
    }

    private void showRoadmapFormDialog() {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_form_create_roadmap, null);
        
        EditText etName = view.findViewById(R.id.etRoadmapName);
        EditText etDesc = view.findViewById(R.id.etRoadmapDesc);
        LinearLayout container = view.findViewById(R.id.trailsContainer);
        View btnAdd = view.findViewById(R.id.btnAddTrail);
        View btnSave = view.findViewById(R.id.btnSaveRoadmap);
        View btnCancel = view.findViewById(R.id.btnCancel);

        selectedTrails.clear();

        btnAdd.setOnClickListener(v -> {
            // Mocking selection
            String[] availableTrails = {"Lógica de Programação", "Java Básico", "Spring Boot", "MySQL", "React Native"};
            android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
            builder.setTitle("Selecione uma Trilha");
            builder.setItems(availableTrails, (d, which) -> {
                addTrailToView(container, availableTrails[which]);
            });
            builder.show();
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnSave.setOnClickListener(v -> {
            String name = etName.getText().toString();
            if (name.isEmpty()) {
                Toast.makeText(this, "Informe o nome do Roadmap", Toast.LENGTH_SHORT).show();
                return;
            }
            Toast.makeText(this, "Roadmap '" + name + "' criado com " + selectedTrails.size() + " trilhas!", Toast.LENGTH_LONG).show();
            dialog.dismiss();
        });

        dialog.setContentView(view);
        dialog.show();
    }

    private void addTrailToView(LinearLayout container, String trailName) {
        selectedTrails.add(trailName);
        
        View trailView = getLayoutInflater().inflate(R.layout.item_trail_selection, container, false);
        TextView tvOrder = trailView.findViewById(R.id.tvOrderNumber);
        TextView tvName = trailView.findViewById(R.id.tvTrailName);
        View btnRemove = trailView.findViewById(R.id.btnRemoveTrail);

        tvOrder.setText(String.valueOf(selectedTrails.size()));
        tvName.setText(trailName);

        btnRemove.setOnClickListener(v -> {
            selectedTrails.remove(trailName);
            container.removeView(trailView);
            updateOrderNumbers(container);
        });

        container.addView(trailView);
    }

    private void updateOrderNumbers(LinearLayout container) {
        for (int i = 0; i < container.getChildCount(); i++) {
            View v = container.getChildAt(i);
            TextView tvOrder = v.findViewById(R.id.tvOrderNumber);
            if (tvOrder != null) {
                tvOrder.setText(String.valueOf(i + 1));
            }
        }
    }

    private void setupNavbar() {
        View btnBack = findViewById(R.id.btnBack);
        ImageView btnThemeToggle = findViewById(R.id.btnThemeToggle);
        View btnDashboard = findViewById(R.id.btnDashboard);
        View btnCursos = findViewById(R.id.btnCursos);
        View btnSuporte = findViewById(R.id.btnSuporte);
        View userInfoHeader = findViewById(R.id.userInfoHeader);
        View userAvatar = findViewById(R.id.userAvatar);

        updateThemeIcon(btnThemeToggle);

        if (btnBack != null) btnBack.setOnClickListener(v -> finish());

        if (btnThemeToggle != null) {
            btnThemeToggle.setOnClickListener(v -> {
                int mode = AppCompatDelegate.getDefaultNightMode();
                if (mode == AppCompatDelegate.MODE_NIGHT_YES) {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                } else {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                }
                recreate();
            });
        }

        if (btnDashboard != null) {
            btnDashboard.setOnClickListener(v -> {
                Intent intent = new Intent(this, DashboardActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
            });
        }

        if (btnCursos != null) {
            btnCursos.setOnClickListener(v -> {
                // Já está em cursos
            });
        }

        if (btnSuporte != null) {
            btnSuporte.setOnClickListener(v -> {
                startActivity(new Intent(this, SupportActivity.class));
            });
        }

        if (userInfoHeader != null) {
            userInfoHeader.setOnClickListener(v -> {
                startActivity(new Intent(this, GerenciarUser.class));
            });
        }

        if (userAvatar != null) {
            userAvatar.setOnClickListener(v -> {
                startActivity(new Intent(this, GerenciarUser.class));
            });
        }

        setupRoadmapTabs();
        setupRoadmapSteps();
    }

    private void updateThemeIcon(ImageView btn) {
        if (btn == null) return;
        int mode = AppCompatDelegate.getDefaultNightMode();
        if (mode == AppCompatDelegate.MODE_NIGHT_YES) {
            btn.setImageResource(R.drawable.ic_sun);
        } else {
            btn.setImageResource(R.drawable.ic_moon);
        }
    }

    private void setupRoadmapTabs() {
        LinearLayout tabsContainer = findViewById(R.id.roadmapTabsContainer);
        if (tabsContainer != null) {
            for (int i = 0; i < tabsContainer.getChildCount(); i++) {
                View tab = tabsContainer.getChildAt(i);
                if (tab instanceof TextView) {
                    final String tabName = ((TextView) tab).getText().toString();
                    tab.setOnClickListener(v -> {
                        Toast.makeText(this, "Filtrando por: " + tabName, Toast.LENGTH_SHORT).show();
                    });
                }
            }
        }
    }

    private void setupRoadmapSteps() {
        // Botão de Início
        View btnStart = findViewById(R.id.btnStartJourney);
        if (btnStart != null) {
            btnStart.setOnClickListener(v -> {
                if (!isJourneyStarted) {
                    isJourneyStarted = true;
                    Toast.makeText(this, "🚀 Jornada Iniciada! Vamos para a Lógica.", Toast.LENGTH_SHORT).show();
                    updateRoadmapUI();
                } else {
                    Toast.makeText(this, "Sua jornada já começou!", Toast.LENGTH_SHORT).show();
                }
            });
        }

        // Card de Lógica
        View cardLogica = findViewById(R.id.cardLogica);
        if (cardLogica != null) {
            cardLogica.setOnClickListener(v -> {
                if (!isJourneyStarted) {
                    Toast.makeText(this, "Clique em 'Início da Jornada' primeiro!", Toast.LENGTH_SHORT).show();
                    return;
                }
                showModuleExerciseDialog("Lógica e Fundamentos", "Qual o resultado de 2 + 2?", "4", () -> {
                    logicaCompleted = true;
                    ooUnlocked = true;
                    Toast.makeText(this, "✅ Lógica concluída! Próximo nível: OO.", Toast.LENGTH_LONG).show();
                    updateRoadmapUI();
                });
            });
        }

        // Card de OO
        View cardOO = findViewById(R.id.cardOO);
        if (cardOO != null) {
            cardOO.setOnClickListener(v -> {
                if (!ooUnlocked) {
                    Toast.makeText(this, "Este módulo ainda está bloqueado!", Toast.LENGTH_SHORT).show();
                    return;
                }
                showModuleExerciseDialog("Orientação a Objetos", "O que é Herança?", "Reuso", () -> {
                    Toast.makeText(this, "🏆 Roadmap Java Finalizado! Parabéns!", Toast.LENGTH_LONG).show();
                });
            });
        }
    }

    private void updateRoadmapUI() {
        // Atualiza UI da Lógica
        View rowLogica = findViewById(R.id.rowModuleLogica);
        View glowLogica = findViewById(R.id.glowLogica);
        ImageView ivStatusLogica = findViewById(R.id.ivStatusLogica);
        
        if (isJourneyStarted) {
            if (rowLogica != null) rowLogica.setAlpha(1.0f);
            if (glowLogica != null) glowLogica.setVisibility(View.VISIBLE);
            if (ivStatusLogica != null) {
                if (logicaCompleted) {
                    ivStatusLogica.setImageResource(R.drawable.ic_target); // Ícone de Check/Sucesso
                    ivStatusLogica.setColorFilter(getResources().getColor(R.color.success_green));
                } else {
                    ivStatusLogica.setImageResource(R.drawable.ic_hourglass);
                    ivStatusLogica.setColorFilter(getResources().getColor(R.color.purple));
                }
            }
        } else {
            if (rowLogica != null) rowLogica.setAlpha(0.5f);
            if (glowLogica != null) glowLogica.setVisibility(View.GONE);
        }

        // Atualiza UI de OO
        View rowOO = findViewById(R.id.rowModuleOO);
        View glowOO = findViewById(R.id.glowOO);
        View containerOO = findViewById(R.id.containerOO);
        ImageView ivStatusOO = findViewById(R.id.ivStatusOO);

        if (ooUnlocked) {
            if (rowOO != null) rowOO.setAlpha(1.0f);
            if (glowOO != null) glowOO.setVisibility(View.VISIBLE);
            if (containerOO != null) containerOO.setBackgroundResource(R.drawable.bg_node_inprogress);
            if (ivStatusOO != null) {
                ivStatusOO.setImageResource(R.drawable.ic_hourglass);
                ivStatusOO.setColorFilter(getResources().getColor(R.color.purple));
            }
        } else {
            if (rowOO != null) rowOO.setAlpha(0.5f);
            if (glowOO != null) glowOO.setVisibility(View.GONE);
        }
        
        // Atualiza UI do Início
        View labelStart = findViewById(R.id.tvStartLabel);
        if (isJourneyStarted && labelStart instanceof TextView) {
            ((TextView) labelStart).setText("Jornada em Curso");
            ((TextView) labelStart).setTextColor(getResources().getColor(R.color.success_green));
        }
    }

    private void showModuleExerciseDialog(String title, String question, String correctAnswer, Runnable onComplete) {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        // Usando o mesmo estilo de form para simplificar
        View view = getLayoutInflater().inflate(R.layout.dialog_form_create_roadmap, null);
        
        TextView tvTitle = view.findViewById(R.id.tvDialogTitle);
        if (tvTitle != null) tvTitle.setText(title);
        
        EditText etAnswer = view.findViewById(R.id.etRoadmapName);
        if (etAnswer != null) {
            etAnswer.setHint(question);
            etAnswer.setText("");
        }

        view.findViewById(R.id.etRoadmapDesc).setVisibility(View.GONE);
        view.findViewById(R.id.trailsLabel).setVisibility(View.GONE);
        view.findViewById(R.id.btnAddTrail).setVisibility(View.GONE);
        view.findViewById(R.id.trailsContainer).setVisibility(View.GONE);

        TextView btnConfirm = view.findViewById(R.id.btnSaveRoadmap);
        if (btnConfirm != null) {
            btnConfirm.setText("Responder");
            btnConfirm.setOnClickListener(v -> {
                if (etAnswer.getText().toString().equalsIgnoreCase(correctAnswer)) {
                    onComplete.run();
                    dialog.dismiss();
                } else {
                    Toast.makeText(this, "Resposta incorreta! Tente novamente.", Toast.LENGTH_SHORT).show();
                }
            });
        }

        dialog.setContentView(view);
        dialog.show();
    }
}
