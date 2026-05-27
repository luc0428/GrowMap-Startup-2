package com.example.growmapapp;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.growmapapp.model.Activity;
import com.example.growmapapp.model.ActivityTrail;
import com.example.growmapapp.model.MapTrail;
import com.example.growmapapp.model.RoadMap;
import com.example.growmapapp.model.Trail;
import com.example.growmapapp.service.FirestoreService;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.HashMap;
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
import java.util.Map;

public class RoadmapActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private RecyclerView rvSteps;
    private StepAdapter adapter;
    private ProgressBar progressBar;
    private TextView tvProgressPercent;
    private LinearLayout tabsContainer;
    private String currentRoadmapId;
    private View selectedTabView;

    static class Step {
        String id, title, desc, status; 
        int order;
        Step(String id, String t, String d, String s, int o){ 
            this.id = id; title=t; desc=d; status=s; order=o;
        }
    }

    static class Roadmap {
        String id, name, icon;
        Roadmap(String id, String name, String icon) {
            this.id = id; this.name = name; this.icon = icon;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_roadmap);
        
        firestoreService = new FirestoreService();

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        rvSteps = findViewById(R.id.rvRoadmapSteps);
        progressBar = findViewById(R.id.roadmapProgress);
        tvProgressPercent = findViewById(R.id.tvProgressPercent);
        tabsContainer = findViewById(R.id.roadmapTabsContainer);

        setupRecyclerView();
        setupNavbar();
        setupActions();
        loadUserInfo();
        loadRoadmaps();
    }

    private void setupRecyclerView() {
        if (rvSteps == null) return;
        rvSteps.setLayoutManager(new LinearLayoutManager(this));
        adapter = new StepAdapter(new ArrayList<>(), step -> {
            if (!"locked".equals(step.status)) {
                Intent intent = new Intent(this, GameQuizActivity.class);
                intent.putExtra("stepId", step.id);
                intent.putExtra("roadmapId", currentRoadmapId);
                startActivity(intent);
            } else {
                Toast.makeText(this, "Esta etapa ainda está bloqueada! Complete as anteriores.", Toast.LENGTH_SHORT).show();
            }
        });
        rvSteps.setAdapter(adapter);
    }

    private void loadRoadmaps() {
        if (mAuth.getCurrentUser() == null) {
            applyMockRoadmaps();
            return;
        }
        String userId = mAuth.getCurrentUser().getUid();

        db.collection("users").document(userId).collection("roadmaps")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
                        applyMockRoadmaps();
                    } else {
                        List<Roadmap> roadmaps = new ArrayList<>();
                        for (DocumentSnapshot doc : queryDocumentSnapshots) {
                            roadmaps.add(new Roadmap(doc.getId(), doc.getString("name"), doc.getString("icon")));
                        }
                        updateTabs(roadmaps);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Erro ao carregar do Firebase. Usando offline.", Toast.LENGTH_SHORT).show();
                    applyMockRoadmaps();
                });
    }

    private void applyMockRoadmaps() {
        List<Roadmap> mocks = new ArrayList<>();
        mocks.add(new Roadmap("mock_java", "Java Core", "☕"));
        mocks.add(new Roadmap("mock_android", "Android UI", "📱"));
        updateTabs(mocks);
    }

    private void updateTabs(List<Roadmap> roadmaps) {
        if (tabsContainer == null) return;
        tabsContainer.removeAllViews();

        if (roadmaps.isEmpty()) {
            findViewById(R.id.tvJornadaTitle).setVisibility(View.GONE);
            return;
        }

        for (int i = 0; i < roadmaps.size(); i++) {
            Roadmap roadmap = roadmaps.get(i);
            View tabView = getLayoutInflater().inflate(R.layout.item_roadmap_tab, tabsContainer, false);
            TextView tvIcon = tabView.findViewById(R.id.tabIcon);
            TextView tvName = tabView.findViewById(R.id.tabName);
            
            tvIcon.setText(roadmap.icon != null ? roadmap.icon : "📍");
            tvName.setText(roadmap.name);

            tabView.setOnClickListener(v -> selectTab(v, roadmap.id));
            tabsContainer.addView(tabView);

            if (currentRoadmapId == null && i == 0) {
                selectTab(tabView, roadmap.id);
            } else if (roadmap.id.equals(currentRoadmapId)) {
                selectTab(tabView, roadmap.id);
            }
        }
    }

    private void selectTab(View view, String roadmapId) {
        if (selectedTabView != null) {
            selectedTabView.setAlpha(0.6f);
            selectedTabView.setBackgroundResource(R.drawable.bg_card_alt);
            TextView oldName = selectedTabView.findViewById(R.id.tabName);
            if (oldName != null) oldName.setTextColor(getResources().getColor(R.color.text_primary));
        }
        selectedTabView = view;
        selectedTabView.setAlpha(1.0f);
        selectedTabView.setBackgroundResource(R.drawable.bg_card_darkblue);
        TextView newName = selectedTabView.findViewById(R.id.tabName);
        if (newName != null) newName.setTextColor(android.graphics.Color.WHITE);
        
        if (roadmapId.startsWith("mock_")) {
            loadMockSteps(roadmapId);
        } else {
            loadStepsForRoadmap(roadmapId);
        }
    }

    private void loadMockSteps(String mockId) {
        List<Step> mocks = new ArrayList<>();
        mocks.add(new Step("m1", "Início da Jornada", "Bem-vindo ao conteúdo mockado.", "completed", 0));
        mocks.add(new Step("m2", "Próximo Passo", "Aqui você verá conteúdo real em breve.", "in_progress", 1));
        mocks.add(new Step("m3", "Destino Final", "Continue estudando!", "locked", 2));
        adapter.updateSteps(mocks);
        updateProgressUI(3, 1);
    }

    private void loadStepsForRoadmap(String roadmapId) {
        this.currentRoadmapId = roadmapId;
        String userId = mAuth.getCurrentUser().getUid();
        
        db.collection("users").document(userId).collection("roadmaps").document(roadmapId).collection("steps")
                .orderBy("order")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Step> steps = new ArrayList<>();
                    int completedCount = 0;
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        String status = doc.getString("status");
                        if ("completed".equals(status)) completedCount++;
                        steps.add(new Step(
                                doc.getId(),
                                doc.getString("title"),
                                doc.getString("description"),
                                status,
                                doc.getLong("order") != null ? doc.getLong("order").intValue() : 0
                        ));
                    }
                    adapter.updateSteps(steps);
                    updateProgressUI(steps.size(), completedCount);
                    
                    TextView tvJornadaTitle = findViewById(R.id.tvJornadaTitle);
                    db.collection("users").document(userId).collection("roadmaps").document(roadmapId).get()
                            .addOnSuccessListener(doc -> {
                                if (tvJornadaTitle != null && doc.exists()) {
                                    tvJornadaTitle.setVisibility(View.VISIBLE);
                                    tvJornadaTitle.setText("Jornada " + doc.getString("name"));
                                }
                            });
                });
    }

    private void updateProgressUI(int total, int completed) {
        int percent = total > 0 ? (completed * 100) / total : 0;
        if (progressBar != null) progressBar.setProgress(percent);
        if (tvProgressPercent != null) tvProgressPercent.setText(percent + "% concluído");
    }

    private void setupActions() {
        View btnCreate = findViewById(R.id.btnCreateRoad);
        if (btnCreate != null) btnCreate.setOnClickListener(v -> showSelectionDialog());
    }

    private void showSelectionDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_create_roadmap, null);
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
        dialog.setContentView(dialogView);

        dialogView.findViewById(R.id.btnEnterCreateRoadmap).setOnClickListener(v -> {
            dialog.dismiss();
            showRoadmapForm();
        });

        dialogView.findViewById(R.id.btnEnterCreateTask).setOnClickListener(v -> {
            dialog.dismiss();
            showTaskForm();
        });

        dialog.show();
    }

    private void showRoadmapForm() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_form_create_roadmap, null);
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        dialog.setContentView(dialogView);

        EditText etName = dialogView.findViewById(R.id.etRoadmapName);
        EditText etDesc = dialogView.findViewById(R.id.etRoadmapDesc);
        LinearLayout trailsContainer = dialogView.findViewById(R.id.trailsContainer);
        View btnAddTrail = dialogView.findViewById(R.id.btnAddTrail);
        
        List<String> stepTitles = new ArrayList<>();

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
        if (btnAddTrail != null) {
            btnAddTrail.setOnClickListener(v -> {
                EditText etStep = new EditText(this);
                etStep.setHint("Nome da etapa (ex: Loops)");
                // Corrigido para usar cores do tema em vez de branco hardcoded
                etStep.setTextColor(getResources().getColor(R.color.text_primary));
                etStep.setHintTextColor(getResources().getColor(R.color.text_muted));
                trailsContainer.addView(etStep);
            });
        }

        dialogView.findViewById(R.id.btnSaveRoadmap).setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String desc = etDesc.getText().toString().trim();
            
            stepTitles.clear();
            for (int i = 0; i < trailsContainer.getChildCount(); i++) {
                View child = trailsContainer.getChildAt(i);
                if (child instanceof EditText) {
                    String stepTitle = ((EditText) child).getText().toString().trim();
                    if (!stepTitle.isEmpty()) stepTitles.add(stepTitle);
                }
            }

            if (!name.isEmpty()) {
                saveNewRoadmap(name, desc, stepTitles, dialog);
            } else {
                etName.setError("Nome obrigatório");
            }
        });
        
        dialogView.findViewById(R.id.btnCancel).setOnClickListener(v -> dialog.dismiss());
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
    private void showTaskForm() {
        if (currentRoadmapId == null) {
            Toast.makeText(this, "Selecione ou crie um Roadmap primeiro!", Toast.LENGTH_SHORT).show();
            return;
        }
        
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_form_create_roadmap, null);
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        dialog.setContentView(dialogView);

        tvOrder.setText(String.valueOf(selectedTrails.size()));
        tvName.setText(trail.getTitle());

        btnRemove.setOnClickListener(v -> {
            selectedTrails.remove(trail);
            container.removeView(trailView);
            updateOrderNumbers(container);
        TextView tvTitle = dialogView.findViewById(R.id.tvDialogTitle);
        tvTitle.setText("Nova Tarefa");
        
        EditText etName = dialogView.findViewById(R.id.etRoadmapName);
        etName.setHint("Ex: Dominando Callbacks");
        
        EditText etDesc = dialogView.findViewById(R.id.etRoadmapDesc);
        etDesc.setHint("Explique o que deve ser feito...");

        dialogView.findViewById(R.id.btnSaveRoadmap).setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String desc = etDesc.getText().toString().trim();
            if (!name.isEmpty()) {
                saveNewTask(name, desc, dialog);
            } else {
                etName.setError("Título obrigatório");
            }
        });

        dialogView.findViewById(R.id.btnCancel).setOnClickListener(v -> dialog.dismiss());
        dialog.show();
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
    private void saveNewRoadmap(String name, String desc, List<String> stepTitles, BottomSheetDialog dialog) {
        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(this, "Você precisa estar logado para criar!", Toast.LENGTH_SHORT).show();
            return;
        }
        String userId = mAuth.getCurrentUser().getUid();
        Map<String, Object> data = new HashMap<>();
        data.put("name", name);
        data.put("description", desc);
        data.put("icon", "🚀");
        data.put("createdAt", Timestamp.now());

        db.collection("users").document(userId).collection("roadmaps").add(data)
                .addOnSuccessListener(ref -> {
                    if (stepTitles == null || stepTitles.isEmpty()) {
                        createDefaultSteps(ref.getId());
                    } else {
                        saveRoadmapSteps(ref.getId(), stepTitles);
                    }
                    dialog.dismiss();
                    loadRoadmaps();
                    Toast.makeText(this, "Roadmap '" + name + "' criado com sucesso!", Toast.LENGTH_LONG).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Erro ao salvar no Firestore: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void saveRoadmapSteps(String roadmapId, List<String> titles) {
        String userId = mAuth.getCurrentUser().getUid();
        for (int i = 0; i < titles.size(); i++) {
            Map<String, Object> step = new HashMap<>();
            step.put("title", titles.get(i));
            step.put("description", "Etapa " + (i + 1) + " da trilha");
            step.put("status", i == 0 ? "in_progress" : "locked");
            step.put("order", i);
            db.collection("users").document(userId).collection("roadmaps").document(roadmapId).collection("steps").add(step);
        }
    }

    private void saveNewTask(String title, String desc, BottomSheetDialog dialog) {
        String userId = mAuth.getCurrentUser().getUid();
        Map<String, Object> data = new HashMap<>();
        data.put("title", title);
        data.put("description", desc);
        data.put("status", "locked");
        data.put("order", adapter.getItemCount());

        db.collection("users").document(userId).collection("roadmaps").document(currentRoadmapId)
                .collection("steps").add(data)
                .addOnSuccessListener(ref -> {
                    dialog.dismiss();
                    loadStepsForRoadmap(currentRoadmapId);
                    Toast.makeText(this, "Tarefa adicionada à sequência!", Toast.LENGTH_SHORT).show();
                });
    }

    private void createDefaultSteps(String roadmapId) {
        String userId = mAuth.getCurrentUser().getUid();
        String[] titles = {"Introdução", "Conceitos Fundamentais", "Desafio Prático"};
        for (int i = 0; i < titles.length; i++) {
            Map<String, Object> step = new HashMap<>();
            step.put("title", titles[i]);
            step.put("description", "Etapa inicial da trilha " + titles[i]);
            step.put("status", i == 0 ? "in_progress" : "locked");
            step.put("order", i);
            db.collection("users").document(userId).collection("roadmaps").document(roadmapId).collection("steps").add(step);
        }
    }

    private void loadUserInfo() {
        if (mAuth.getCurrentUser() == null) {
            // Mock data fallback
            applyMockUserInfo();
            return;
        }
        String userId = mAuth.getCurrentUser().getUid();
        db.collection("user").document(userId).get().addOnSuccessListener(doc -> {
            if (doc.exists()) {
                String name = doc.getString("fullname");
                String role = doc.getString("role");
                if (role == null) role = doc.getString("cargo"); // Tenta 'cargo' se 'role' for nulo
                
                TextView userName = findViewById(R.id.userName);
                TextView userAvatar = findViewById(R.id.userAvatar);
                TextView userRole = findViewById(R.id.userRole);
                if (userName != null) userName.setText(name != null ? name : "Usuário");
                if (userRole != null) userRole.setText(role != null ? role : "Explorador");
                if (userAvatar != null && name != null && !name.isEmpty()) {
                    userAvatar.setText(name.substring(0,1).toUpperCase());
                }
            } else {
                applyMockUserInfo();
            }
        }).addOnFailureListener(e -> applyMockUserInfo());
    }

    private void applyMockUserInfo() {
        TextView userName = findViewById(R.id.userName);
        TextView userAvatar = findViewById(R.id.userAvatar);
        TextView userRole = findViewById(R.id.userRole);
        if (userName != null) userName.setText("Dev Explorador");
        if (userRole != null) userRole.setText("Estudante de Tecnologia");
        if (userAvatar != null) userAvatar.setText("D");
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
        ImageView btnTheme = findViewById(R.id.btnThemeToggle);
        if (btnTheme != null) {
            btnTheme.setOnClickListener(v -> {
                int mode = AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES ? 
                           AppCompatDelegate.MODE_NIGHT_NO : AppCompatDelegate.MODE_NIGHT_YES;
                AppCompatDelegate.setDefaultNightMode(mode);
                recreate();
            });
        }
        findViewById(R.id.btnNavHome).setOnClickListener(v -> finish());
        findViewById(R.id.userInfoHeader).setOnClickListener(v -> startActivity(new Intent(this, ProfileActivity.class)));
        findViewById(R.id.userAvatar).setOnClickListener(v -> startActivity(new Intent(this, ProfileActivity.class)));
    }

    static class StepAdapter extends RecyclerView.Adapter<StepAdapter.VH> {
        private final List<Step> steps;
        private final OnStepClickListener listener;

        StepAdapter(List<Step> steps, OnStepClickListener listener) {
            this.steps = steps; this.listener = listener;
        }

        public void updateSteps(List<Step> newSteps) {
            this.steps.clear();
            this.steps.addAll(newSteps);
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new VH(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_roadmap_step, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull VH holder, int position) {
            Step step = steps.get(position);
            holder.tvTitle.setText(step.title);
            holder.tvDesc.setText(step.desc);

            int colorGray = 0xFF94A3B8, colorGreen = 0xFF16A34A, colorCyan = 0xFF0891B2;
            if ("completed".equals(step.status)) {
                holder.statusBg.setBackgroundTintList(ColorStateList.valueOf(colorGreen));
                holder.statusIcon.setImageResource(android.R.drawable.checkbox_on_background);
                holder.btnAction.setImageResource(android.R.drawable.ic_menu_revert);
            } else if ("in_progress".equals(step.status)) {
                holder.statusBg.setBackgroundTintList(ColorStateList.valueOf(colorCyan));
                holder.statusIcon.setImageResource(android.R.drawable.ic_menu_edit);
                holder.btnAction.setImageResource(android.R.drawable.ic_media_play);
            } else {
                holder.statusBg.setBackgroundTintList(ColorStateList.valueOf(colorGray));
                holder.statusIcon.setImageResource(android.R.drawable.ic_lock_idle_lock);
                holder.btnAction.setImageResource(android.R.drawable.ic_lock_lock);
                holder.btnAction.setImageAlpha(128);
            }
            holder.itemView.setOnClickListener(v -> listener.onStepClick(step));
        }

        @Override
        public int getItemCount() { return steps.size(); }

        static class VH extends RecyclerView.ViewHolder {
            TextView tvTitle, tvDesc;
            View statusBg;
            ImageView statusIcon, btnAction;
            VH(View v) {
                super(v);
                tvTitle = v.findViewById(R.id.tvStepTitle);
                tvDesc = v.findViewById(R.id.tvStepDescription);
                statusBg = v.findViewById(R.id.stepStatusBackground);
                statusIcon = v.findViewById(R.id.stepStatusIcon);
                btnAction = v.findViewById(R.id.btnStepAction);
            }
        }
    }
    interface OnStepClickListener { void onStepClick(Step step); }
}
