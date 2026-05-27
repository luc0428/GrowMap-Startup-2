package com.example.growmapapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.growmapapp.adapter.ActivityAdapter;
import com.example.growmapapp.adapter.LearningPathAdapter;
import com.example.growmapapp.adapter.RoadMapAdapter;
import com.example.growmapapp.adapter.SelectionAdapter;
import com.example.growmapapp.adapter.TrailAdapter;
import com.example.growmapapp.model.Activity;
import com.example.growmapapp.model.ActivityTrail;
import com.example.growmapapp.model.MapTrail;
import com.example.growmapapp.model.RoadMap;
import com.example.growmapapp.model.Trail;
import com.example.growmapapp.service.FirestoreService;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

public class RoadmapActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirestoreService firestoreService;
    
    private RecyclerView rvSteps;
    private TextView tvJornadaTitle;
    private LinearLayout btnFilterRoadmaps, btnFilterTrails, btnFilterActivities;
    
    private List<Trail> selectedTrails = new ArrayList<>();
    private List<Activity> selectedActivities = new ArrayList<>();
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_roadmap);
        
        firestoreService = new FirestoreService();
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        rvSteps = findViewById(R.id.rvRoadmapSteps);
        tvJornadaTitle = findViewById(R.id.tvJornadaTitle);

        btnFilterRoadmaps = findViewById(R.id.filterRoadmaps);
        btnFilterTrails = findViewById(R.id.filterTrails);
        btnFilterActivities = findViewById(R.id.filterActivities);

        rvSteps.setLayoutManager(new LinearLayoutManager(this));

        setupNavbar();
        setupActions();
        setupFilters();
        loadUserInfo();
        
        // Iniciar com Roadmaps selecionado por padrão
        btnFilterRoadmaps.performClick();
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
        
        View btnBack = findViewById(R.id.btnBackToDashboard);
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());
        
        View btnHome = findViewById(R.id.btnNavHome);
        if (btnHome != null) btnHome.setOnClickListener(v -> {
            Intent intent = new Intent(this, DashboardActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        });

        View btnSupport = findViewById(R.id.btnNavSuporte);
        if (btnSupport != null) btnSupport.setOnClickListener(v -> startActivity(new Intent(this, SupportActivity.class)));

        View userInfo = findViewById(R.id.userInfoHeader);
        if (userInfo != null) userInfo.setOnClickListener(v -> startActivity(new Intent(this, GerenciarUser.class)));
        
        View avatar = findViewById(R.id.userAvatar);
        if (avatar != null) avatar.setOnClickListener(v -> startActivity(new Intent(this, GerenciarUser.class)));
    }

    private void setupActions() {
        View btnCreate = findViewById(R.id.btnCreateRoad);
        if (btnCreate != null) btnCreate.setOnClickListener(v -> showCreateOptionsDialog());
    }

    private void setupFilters() {
        btnFilterRoadmaps.setOnClickListener(v -> {
            updateFilterUI(btnFilterRoadmaps);
            tvJornadaTitle.setText("Todos os Roadmaps");
            loadAllRoadmaps();
        });

        btnFilterTrails.setOnClickListener(v -> {
            updateFilterUI(btnFilterTrails);
            tvJornadaTitle.setText("Todas as Trilhas");
            loadAllTrails();
        });

        btnFilterActivities.setOnClickListener(v -> {
            updateFilterUI(btnFilterActivities);
            tvJornadaTitle.setText("Todas as Tarefas");
            loadAllActivities();
        });
    }

    private void updateFilterUI(LinearLayout selected) {
        btnFilterRoadmaps.setBackgroundResource(R.drawable.bg_card_alt);
        btnFilterTrails.setBackgroundResource(R.drawable.bg_card_alt);
        btnFilterActivities.setBackgroundResource(R.drawable.bg_card_alt);

        int secondaryColor = ContextCompat.getColor(this, R.color.text_secondary);
        ((TextView)btnFilterRoadmaps.getChildAt(1)).setTextColor(secondaryColor);
        ((TextView)btnFilterTrails.getChildAt(1)).setTextColor(secondaryColor);
        ((TextView)btnFilterActivities.getChildAt(1)).setTextColor(secondaryColor);

        selected.setBackgroundResource(R.drawable.bg_card_darkblue);
        ((TextView)selected.getChildAt(1)).setTextColor(android.graphics.Color.WHITE);
    }

    private void loadAllRoadmaps() {
        firestoreService.getAllRoadMaps().addOnSuccessListener(querySnapshot -> {
            List<RoadMap> list = querySnapshot.toObjects(RoadMap.class);
            rvSteps.setAdapter(new RoadMapAdapter(list, this::viewRoadmap, this::showRoadmapFormDialog, this::deleteRoadmap));
        });
    }

    private void viewRoadmap(RoadMap roadMap) {
        tvJornadaTitle.setText("Trilhas de: " + roadMap.getTitle());
        firestoreService.getTrailsByRoadMap(roadMap.getId()).addOnSuccessListener(querySnapshot -> {
            List<MapTrail> pivots = querySnapshot.toObjects(MapTrail.class);
            pivots.sort(Comparator.comparingInt(MapTrail::getOrder));
            
            List<Trail> trails = new ArrayList<>();
            AtomicInteger count = new AtomicInteger(pivots.size());
            if (pivots.isEmpty()) {
                rvSteps.setAdapter(null);
                return;
            }

            for (MapTrail p : pivots) {
                firestoreService.getTrail(p.getTrailId()).addOnSuccessListener(doc -> {
                    if (doc.exists()) trails.add(doc.toObject(Trail.class));
                    if (count.decrementAndGet() == 0) {
                        rvSteps.setAdapter(new TrailAdapter(trails, this::viewTrail, this::showTrailFormDialog, this::deleteTrail));
                    }
                });
            }
        });
    }

    private void loadAllTrails() {
        firestoreService.getAllTrails().addOnSuccessListener(querySnapshot -> {
            List<Trail> list = querySnapshot.toObjects(Trail.class);
            rvSteps.setAdapter(new TrailAdapter(list, this::viewTrail, this::showTrailFormDialog, this::deleteTrail));
        });
    }

    private void viewTrail(Trail trail) {
        tvJornadaTitle.setText("Jornada: " + trail.getTitle());
        firestoreService.getActivitiesByTrail(trail.getId()).addOnSuccessListener(querySnapshot -> {
            List<ActivityTrail> pivots = querySnapshot.toObjects(ActivityTrail.class);
            pivots.sort(Comparator.comparingInt(ActivityTrail::getOrder));

            List<Activity> activities = new ArrayList<>();
            AtomicInteger count = new AtomicInteger(pivots.size());
            if (pivots.isEmpty()) {
                rvSteps.setAdapter(null);
                return;
            }

            for (ActivityTrail p : pivots) {
                firestoreService.getActivity(p.getActivityId()).addOnSuccessListener(doc -> {
                    if (doc.exists()) activities.add(doc.toObject(Activity.class));
                    if (count.decrementAndGet() == 0) {
                        rvSteps.setAdapter(new LearningPathAdapter(activities, this::startQuizActivity));
                    }
                });
            }
        });
    }

    private void startQuizActivity(Activity activity) {
        Intent intent = new Intent(this, GameQuizActivity.class);
        intent.putExtra("activityId", activity.getId());
        startActivity(intent);
    }

    private void loadAllActivities() {
        firestoreService.getAllActivities().addOnSuccessListener(querySnapshot -> {
            List<Activity> list = querySnapshot.toObjects(Activity.class);
            rvSteps.setAdapter(new ActivityAdapter(list, this::startQuizActivity, this::showActivityFormDialog, this::deleteActivity));
        });
    }

    private void deleteRoadmap(RoadMap roadMap) {
        new android.app.AlertDialog.Builder(this)
            .setTitle("Excluir Roadmap")
            .setMessage("Tem certeza que deseja excluir '" + roadMap.getTitle() + "'? Isso removerá todos os vínculos com trilhas.")
            .setPositiveButton("Excluir", (d, w) -> {
                firestoreService.deleteRoadMap(roadMap.getId()).addOnSuccessListener(aVoid -> {
                    firestoreService.getTrailsByRoadMap(roadMap.getId()).addOnSuccessListener(querySnapshot -> {
                        for (DocumentSnapshot doc : querySnapshot) firestoreService.deleteMapTrail(doc.getId());
                    });
                    Toast.makeText(this, "Roadmap excluído!", Toast.LENGTH_SHORT).show();
                    loadAllRoadmaps();
                });
            })
            .setNegativeButton("Cancelar", null)
            .show();
    }

    private void deleteTrail(Trail trail) {
        new android.app.AlertDialog.Builder(this)
            .setTitle("Excluir Trilha")
            .setMessage("Tem certeza que deseja excluir '" + trail.getTitle() + "'? Isso removerá os vínculos em roadmaps e tarefas.")
            .setPositiveButton("Excluir", (d, w) -> {
                firestoreService.deleteTrail(trail.getId()).addOnSuccessListener(aVoid -> {
                    db.collection("mapTrail").whereEqualTo("trailId", trail.getId()).get().addOnSuccessListener(qs -> {
                        for (DocumentSnapshot doc : qs) firestoreService.deleteMapTrail(doc.getId());
                    });
                    firestoreService.getActivitiesByTrail(trail.getId()).addOnSuccessListener(qs -> {
                        for (DocumentSnapshot doc : qs) firestoreService.deleteActivityTrail(doc.getId());
                    });
                    Toast.makeText(this, "Trilha excluída!", Toast.LENGTH_SHORT).show();
                    loadAllTrails();
                });
            })
            .setNegativeButton("Cancelar", null)
            .show();
    }

    private void deleteActivity(Activity activity) {
        new android.app.AlertDialog.Builder(this)
            .setTitle("Excluir Tarefa")
            .setMessage("Tem certeza que deseja excluir '" + activity.getTitle() + "'?")
            .setPositiveButton("Excluir", (d, w) -> {
                firestoreService.deleteActivity(activity.getId()).addOnSuccessListener(aVoid -> {
                    db.collection("activityTrail").whereEqualTo("activityId", activity.getId()).get().addOnSuccessListener(qs -> {
                        for (DocumentSnapshot doc : qs) firestoreService.deleteActivityTrail(doc.getId());
                    });
                    Toast.makeText(this, "Tarefa excluída!", Toast.LENGTH_SHORT).show();
                    loadAllActivities();
                });
            })
            .setNegativeButton("Cancelar", null)
            .show();
    }

    private void loadUserInfo() {
        if (mAuth.getCurrentUser() == null) return;
        String userId = mAuth.getCurrentUser().getUid();
        db.collection("user").document(userId).get().addOnSuccessListener(doc -> {
            if (doc.exists()) {
                String name = doc.getString("fullname");
                String role = doc.getString("role");
                TextView userName = findViewById(R.id.userName);
                TextView userAvatar = findViewById(R.id.userAvatar);
                TextView userRole = findViewById(R.id.userRole);
                if (userName != null) userName.setText(name != null ? name : "Usuário");
                if (userRole != null) userRole.setText(role != null ? role : "Explorador");
                if (userAvatar != null && name != null && !name.isEmpty()) {
                    userAvatar.setText(name.substring(0,1).toUpperCase());
                }
            }
        });
    }

    private void showCreateOptionsDialog() {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_create_roadmap, null);
        view.findViewById(R.id.optionCreateRoadmap).setOnClickListener(v -> { dialog.dismiss(); showRoadmapFormDialog(null); });
        view.findViewById(R.id.optionCreateTrail).setOnClickListener(v -> { dialog.dismiss(); showTrailFormDialog(null); });
        view.findViewById(R.id.optionCreateTask).setOnClickListener(v -> { dialog.dismiss(); showActivityFormDialog(null); });
        dialog.setContentView(view);
        dialog.show();
    }

    private void showRoadmapFormDialog(RoadMap roadMap) {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_form_create_roadmap, null);
        
        TextView tvDialogTitle = view.findViewById(R.id.tvDialogTitle);
        EditText etName = view.findViewById(R.id.etRoadmapName);
        EditText etDesc = view.findViewById(R.id.etRoadmapDesc);
        RecyclerView rvSelected = view.findViewById(R.id.rvSelectedTrails);
        TextView btnSave = view.findViewById(R.id.btnSaveRoadmap);

        selectedTrails.clear();

        SelectionAdapter<Trail> selectionAdapter = new SelectionAdapter<>(selectedTrails, R.layout.item_trail_selection, Trail::getTitle, (item, position) -> {
            selectedTrails.remove(position);
            Objects.requireNonNull(rvSelected.getAdapter()).notifyItemRemoved(position);
            rvSelected.getAdapter().notifyItemRangeChanged(0, selectedTrails.size());
        });
        rvSelected.setLayoutManager(new LinearLayoutManager(this));
        rvSelected.setAdapter(selectionAdapter);

        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP | ItemTouchHelper.DOWN, 0) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                selectionAdapter.onItemMove(viewHolder.getAdapterPosition(), target.getAdapterPosition());
                return true;
            }
            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {}
        }).attachToRecyclerView(rvSelected);

        if (roadMap != null) {
            tvDialogTitle.setText("Editar Roadmap");
            etName.setText(roadMap.getTitle());
            etDesc.setText(roadMap.getDescription());
            btnSave.setText("Salvar Alterações");

            firestoreService.getTrailsByRoadMap(roadMap.getId()).addOnSuccessListener(querySnapshot -> {
                List<MapTrail> pivots = querySnapshot.toObjects(MapTrail.class);
                pivots.sort(Comparator.comparingInt(MapTrail::getOrder));
                if (pivots.isEmpty()) return;
                
                AtomicInteger count = new AtomicInteger(pivots.size());
                Map<String, Trail> loadedTrails = new HashMap<>();

                for (MapTrail p : pivots) {
                    firestoreService.getTrail(p.getTrailId()).addOnSuccessListener(doc -> {
                        if (doc.exists()) {
                            loadedTrails.put(p.getTrailId(), doc.toObject(Trail.class));
                        }
                        if (count.decrementAndGet() == 0) {
                            selectedTrails.clear();
                            for (MapTrail pivot : pivots) {
                                Trail t = loadedTrails.get(pivot.getTrailId());
                                if (t != null) selectedTrails.add(t);
                            }
                            selectionAdapter.notifyDataSetChanged();
                        }
                    });
                }
            });
        }

        view.findViewById(R.id.btnCancel).setOnClickListener(v -> dialog.dismiss());

        view.findViewById(R.id.btnAddTrail).setOnClickListener(v -> {
            firestoreService.getAllTrails().addOnSuccessListener(querySnapshot -> {
                List<Trail> allTrails = querySnapshot.toObjects(Trail.class);
                List<Trail> available = new ArrayList<>();
                for (Trail t : allTrails) {
                    boolean selected = false;
                    for (Trail st : selectedTrails) if (Objects.equals(st.getId(), t.getId())) { selected = true; break; }
                    if (!selected) available.add(t);
                }
                String[] names = new String[available.size()];
                for (int i=0; i<available.size(); i++) names[i] = available.get(i).getTitle();
                new android.app.AlertDialog.Builder(this).setTitle("Adicionar Trilha").setItems(names, (d, which) -> {
                    selectedTrails.add(available.get(which));
                    selectionAdapter.notifyItemInserted(selectedTrails.size() - 1);
                }).show();
            });
        });

        btnSave.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            if (name.isEmpty()) return;
            RoadMap rm = roadMap != null ? roadMap : new RoadMap();
            rm.setTitle(name);
            rm.setDescription(etDesc.getText().toString());
            
            if (roadMap == null) {
                firestoreService.addRoadMap(rm).addOnSuccessListener(ref -> {
                    for (int i = 0; i < selectedTrails.size(); i++) {
                        firestoreService.addMapTrail(new MapTrail(ref.getId(), selectedTrails.get(i).getId(), false, i + 1));
                    }
                    dialog.dismiss();
                    loadAllRoadmaps();
                });
            } else {
                firestoreService.updateRoadMap(rm).addOnSuccessListener(aVoid -> {
                    // Sync Pivot: Clear and Rebuild
                    firestoreService.getTrailsByRoadMap(roadMap.getId()).addOnSuccessListener(qs -> {
                        for (DocumentSnapshot doc : qs) firestoreService.deleteMapTrail(doc.getId());
                        for (int i = 0; i < selectedTrails.size(); i++) {
                            firestoreService.addMapTrail(new MapTrail(roadMap.getId(), selectedTrails.get(i).getId(), false, i + 1));
                        }
                    });
                    dialog.dismiss();
                    loadAllRoadmaps();
                });
            }
        });

        dialog.setContentView(view);
        dialog.show();
    }

    private void showTrailFormDialog(Trail trail) {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_form_create_trail, null);
        
        TextView tvDialogTitle = view.findViewById(R.id.tvDialogTitle);
        EditText etName = view.findViewById(R.id.etTrailName);
        EditText etDesc = view.findViewById(R.id.etTrailDesc);
        RecyclerView rvSelected = view.findViewById(R.id.rvSelectedActivities);
        TextView btnSave = view.findViewById(R.id.btnSaveTrail);

        selectedActivities.clear();

        SelectionAdapter<Activity> selectionAdapter = new SelectionAdapter<>(selectedActivities, R.layout.item_activity_selection, Activity::getTitle, (item, position) -> {
            selectedActivities.remove(position);
            Objects.requireNonNull(rvSelected.getAdapter()).notifyItemRemoved(position);
            rvSelected.getAdapter().notifyItemRangeChanged(0, selectedActivities.size());
        });
        rvSelected.setLayoutManager(new LinearLayoutManager(this));
        rvSelected.setAdapter(selectionAdapter);

        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP | ItemTouchHelper.DOWN, 0) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                selectionAdapter.onItemMove(viewHolder.getAdapterPosition(), target.getAdapterPosition());
                return true;
            }
            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {}
        }).attachToRecyclerView(rvSelected);

        if (trail != null) {
            tvDialogTitle.setText("Editar Trilha");
            etName.setText(trail.getTitle());
            etDesc.setText(trail.getDescription());
            btnSave.setText("Salvar Alterações");

            firestoreService.getActivitiesByTrail(trail.getId()).addOnSuccessListener(querySnapshot -> {
                List<ActivityTrail> pivots = querySnapshot.toObjects(ActivityTrail.class);
                pivots.sort(Comparator.comparingInt(ActivityTrail::getOrder));
                if (pivots.isEmpty()) return;

                AtomicInteger count = new AtomicInteger(pivots.size());
                Map<String, Activity> loadedActivities = new HashMap<>();

                for (ActivityTrail p : pivots) {
                    firestoreService.getActivity(p.getActivityId()).addOnSuccessListener(doc -> {
                        if (doc.exists()) {
                            loadedActivities.put(p.getActivityId(), doc.toObject(Activity.class));
                        }
                        if (count.decrementAndGet() == 0) {
                            selectedActivities.clear();
                            for (ActivityTrail pivot : pivots) {
                                Activity a = loadedActivities.get(pivot.getActivityId());
                                if (a != null) selectedActivities.add(a);
                            }
                            selectionAdapter.notifyDataSetChanged();
                        }
                    });
                }
            });
        }

        view.findViewById(R.id.btnCancel).setOnClickListener(v -> dialog.dismiss());

        view.findViewById(R.id.btnAddActivity).setOnClickListener(v -> {
            firestoreService.getAllActivities().addOnSuccessListener(querySnapshot -> {
                List<Activity> allActivities = querySnapshot.toObjects(Activity.class);
                List<Activity> available = new ArrayList<>();
                for (Activity a : allActivities) {
                    boolean selected = false;
                    for (Activity sa : selectedActivities) if (Objects.equals(sa.getId(), a.getId())) { selected = true; break; }
                    if (!selected) available.add(a);
                }
                String[] names = new String[available.size()];
                for (int i=0; i<available.size(); i++) names[i] = available.get(i).getTitle();
                new android.app.AlertDialog.Builder(this).setTitle("Adicionar Atividade").setItems(names, (d, which) -> {
                    selectedActivities.add(available.get(which));
                    selectionAdapter.notifyItemInserted(selectedActivities.size() - 1);
                }).show();
            });
        });

        btnSave.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            if (name.isEmpty()) return;
            Trail t = trail != null ? trail : new Trail();
            t.setTitle(name);
            t.setDescription(etDesc.getText().toString());

            if (trail == null) {
                firestoreService.addTrail(t).addOnSuccessListener(ref -> {
                    for (int i = 0; i < selectedActivities.size(); i++) {
                        firestoreService.addActivityTrail(new ActivityTrail(selectedActivities.get(i).getId(), ref.getId(), false, i + 1));
                    }
                    dialog.dismiss();
                    loadAllTrails();
                });
            } else {
                firestoreService.updateTrail(t).addOnSuccessListener(aVoid -> {
                    firestoreService.getActivitiesByTrail(trail.getId()).addOnSuccessListener(qs -> {
                        for (DocumentSnapshot doc : qs) firestoreService.deleteActivityTrail(doc.getId());
                        for (int i = 0; i < selectedActivities.size(); i++) {
                            firestoreService.addActivityTrail(new ActivityTrail(selectedActivities.get(i).getId(), trail.getId(), false, i + 1));
                        }
                    });
                    dialog.dismiss();
                    loadAllTrails();
                });
            }
        });

        dialog.setContentView(view);
        dialog.show();
    }

    private void showActivityFormDialog(Activity activity) {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_form_create_activity, null);

        TextView tvDialogTitle = view.findViewById(R.id.tvDialogTitle);
        EditText etName = view.findViewById(R.id.etActivityName);
        EditText etDesc = view.findViewById(R.id.etActivityDesc);
        RecyclerView rvQuestions = view.findViewById(R.id.rvQuestions);
        TextView btnAddQuestion = view.findViewById(R.id.btnAddQuestion);
        TextView btnSave = view.findViewById(R.id.btnSaveActivity);

        List<Question> questionsList = new ArrayList<>();
        if (activity != null && activity.getQuestions() != null) {
            questionsList.addAll(activity.getQuestions());
        } else if (activity == null) {
            questionsList.add(new Question("", Arrays.asList("", "", "", ""), 0));
        }

        com.example.growmapapp.adapter.QuestionFormAdapter adapter = new com.example.growmapapp.adapter.QuestionFormAdapter(questionsList);
        rvQuestions.setLayoutManager(new LinearLayoutManager(this));
        rvQuestions.setAdapter(adapter);

        if (activity != null) {
            tvDialogTitle.setText("Editar Atividade");
            etName.setText(activity.getTitle());
            etDesc.setText(activity.getDescription());
            btnSave.setText("Salvar Alterações");
        }

        btnAddQuestion.setOnClickListener(v -> {
            questionsList.add(new Question("", Arrays.asList("", "", "", ""), 0));
            adapter.notifyItemInserted(questionsList.size() - 1);
        });

        view.findViewById(R.id.btnCancel).setOnClickListener(v -> dialog.dismiss());

        btnSave.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            if (name.isEmpty()) return;

            Activity act = activity != null ? activity : new Activity();
            act.setTitle(name);
            act.setDescription(etDesc.getText().toString());
            act.setQuestions(questionsList);
            act.setType("quiz");

            if (activity == null) {
                firestoreService.addActivity(act).addOnSuccessListener(ref -> {
                    dialog.dismiss();
                    loadAllActivities();
                });
            } else {
                firestoreService.updateActivity(act).addOnSuccessListener(aVoid -> {
                    dialog.dismiss();
                    loadAllActivities();
                });
            }
        });

        dialog.setContentView(view);
        dialog.show();
    }
}
