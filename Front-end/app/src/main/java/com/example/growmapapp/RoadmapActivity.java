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

import com.example.growmapapp.model.Activity;
import com.example.growmapapp.model.ActivityTrail;
import com.example.growmapapp.model.MapTrail;
import com.example.growmapapp.model.RoadMap;
import com.example.growmapapp.model.Trail;
import com.example.growmapapp.service.FirestoreService;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class RoadmapActivity extends AppCompatActivity {

    private List<Trail> selectedTrails = new ArrayList<>();
    private List<Activity> selectedActivities = new ArrayList<>();
    private FirestoreService firestoreService;
    
    // Estados do Roadmap (Simulando persistência)
    private boolean isJourneyStarted = false;
    private boolean logicaCompleted = false;
    private boolean ooUnlocked = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_roadmap);
        
        firestoreService = new FirestoreService();

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
        
        // Roadmap
        View.OnClickListener roadmapAction = v -> {
            dialog.dismiss();
            showRoadmapFormDialog();
        };
        view.findViewById(R.id.optionCreateRoadmap).setOnClickListener(roadmapAction);
        view.findViewById(R.id.btnEnterCreateRoadmap).setOnClickListener(roadmapAction);
        
        // Activity
        View.OnClickListener activityAction = v -> {
            dialog.dismiss();
            showActivityFormDialog();
        };
        view.findViewById(R.id.optionCreateTask).setOnClickListener(activityAction);
        view.findViewById(R.id.btnEnterCreateTask).setOnClickListener(activityAction);
        
        // Trail
        View.OnClickListener trailAction = v -> {
            dialog.dismiss();
            showTrailFormDialog();
        };
        view.findViewById(R.id.optionCreateTrail).setOnClickListener(trailAction);
        view.findViewById(R.id.btnEnterCreateTrail).setOnClickListener(trailAction);

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
            firestoreService.getAllTrails().addOnSuccessListener(queryDocumentSnapshots -> {
                List<Trail> trails = queryDocumentSnapshots.toObjects(Trail.class);
                String[] trailNames = new String[trails.size()];
                for (int i = 0; i < trails.size(); i++) trailNames[i] = trails.get(i).getTitle();

                android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
                builder.setTitle("Selecione uma Trilha");
                builder.setItems(trailNames, (d, which) -> {
                    addTrailToView(container, trails.get(which));
                });
                builder.show();
            });
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnSave.setOnClickListener(v -> {
            String name = etName.getText().toString();
            String desc = etDesc.getText().toString();
            if (name.isEmpty()) {
                Toast.makeText(this, "Informe o nome do Roadmap", Toast.LENGTH_SHORT).show();
                return;
            }

            RoadMap roadMap = new RoadMap(name, desc);
            firestoreService.addRoadMap(roadMap).addOnSuccessListener(docRef -> {
                String roadMapId = docRef.getId();
                // Save Pivot relations
                for (Trail t : selectedTrails) {
                    firestoreService.addMapTrail(new MapTrail(roadMapId, t.getId()));
                }
                Toast.makeText(this, "Roadmap criado com sucesso!", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            });
        });

        dialog.setContentView(view);
        dialog.show();
    }

    private void showTrailFormDialog() {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_form_create_trail, null);
        
        EditText etName = view.findViewById(R.id.etTrailName);
        EditText etDesc = view.findViewById(R.id.etTrailDesc);
        LinearLayout container = view.findViewById(R.id.activitiesContainer);
        View btnAdd = view.findViewById(R.id.btnAddActivity);
        View btnSave = view.findViewById(R.id.btnSaveTrail);
        View btnCancel = view.findViewById(R.id.btnCancel);

        selectedActivities.clear();

        btnAdd.setOnClickListener(v -> {
            firestoreService.getAllActivities().addOnSuccessListener(queryDocumentSnapshots -> {
                List<Activity> activities = queryDocumentSnapshots.toObjects(Activity.class);
                String[] activityNames = new String[activities.size()];
                for (int i = 0; i < activities.size(); i++) activityNames[i] = activities.get(i).getTitle();

                android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
                builder.setTitle("Selecione uma Atividade");
                builder.setItems(activityNames, (d, which) -> {
                    addActivityToView(container, activities.get(which));
                });
                builder.show();
            });
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnSave.setOnClickListener(v -> {
            String name = etName.getText().toString();
            String desc = etDesc.getText().toString();
            if (name.isEmpty()) {
                Toast.makeText(this, "Informe o nome da Trilha", Toast.LENGTH_SHORT).show();
                return;
            }

            Trail trail = new Trail(name, desc);
            firestoreService.addTrail(trail).addOnSuccessListener(docRef -> {
                String trailId = docRef.getId();
                for (Activity a : selectedActivities) {
                    firestoreService.addActivityTrail(new ActivityTrail(a.getId(), trailId));
                }
                Toast.makeText(this, "Trilha criada com sucesso!", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            });
        });

        dialog.setContentView(view);
        dialog.show();
    }

    private void showActivityFormDialog() {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_form_create_activity, null);
        
        EditText etName = view.findViewById(R.id.etActivityName);
        EditText etDesc = view.findViewById(R.id.etActivityDesc);
        View btnSave = view.findViewById(R.id.btnSaveActivity);
        View btnCancel = view.findViewById(R.id.btnCancel);

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnSave.setOnClickListener(v -> {
            String name = etName.getText().toString();
            String desc = etDesc.getText().toString();
            if (name.isEmpty()) {
                Toast.makeText(this, "Informe o nome da Atividade", Toast.LENGTH_SHORT).show();
                return;
            }

            Activity activity = new Activity(name, desc);
            firestoreService.addActivity(activity).addOnSuccessListener(docRef -> {
                Toast.makeText(this, "Atividade criada com sucesso!", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            });
        });

        dialog.setContentView(view);
        dialog.show();
    }

    private void addTrailToView(LinearLayout container, Trail trail) {
        selectedTrails.add(trail);
        
        View trailView = getLayoutInflater().inflate(R.layout.item_trail_selection, container, false);
        TextView tvOrder = trailView.findViewById(R.id.tvOrderNumber);
        TextView tvName = trailView.findViewById(R.id.tvTrailName);
        View btnRemove = trailView.findViewById(R.id.btnRemoveTrail);

        tvOrder.setText(String.valueOf(selectedTrails.size()));
        tvName.setText(trail.getTitle());

        btnRemove.setOnClickListener(v -> {
            selectedTrails.remove(trail);
            container.removeView(trailView);
            updateOrderNumbers(container);
        });

        container.addView(trailView);
    }

    private void addActivityToView(LinearLayout container, Activity activity) {
        selectedActivities.add(activity);
        
        View activityView = getLayoutInflater().inflate(R.layout.item_activity_selection, container, false);
        TextView tvOrder = activityView.findViewById(R.id.tvOrderNumber);
        TextView tvName = activityView.findViewById(R.id.tvActivityName);
        View btnRemove = activityView.findViewById(R.id.btnRemoveActivity);

        tvOrder.setText(String.valueOf(selectedActivities.size()));
        tvName.setText(activity.getTitle());

        btnRemove.setOnClickListener(v -> {
            selectedActivities.remove(activity);
            container.removeView(activityView);
            updateActivityOrderNumbers(container);
        });

        container.addView(activityView);
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

    private void updateActivityOrderNumbers(LinearLayout container) {
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
