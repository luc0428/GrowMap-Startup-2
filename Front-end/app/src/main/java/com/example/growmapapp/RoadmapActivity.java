package com.example.growmapapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

public class RoadmapActivity extends AppCompatActivity {

    private static final String TAG = "RoadmapActivity";
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirestoreService firestoreService;
    
    private RecyclerView rvSteps;
    private TextView tvJornadaTitle;
    private LinearLayout btnFilterRoadmaps, btnFilterTrails, btnFilterActivities;
    private LinearLayout tabsContainer;
    
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
        tabsContainer = findViewById(R.id.roadmapTabsContainer);

        btnFilterRoadmaps = findViewById(R.id.filterRoadmaps);
        btnFilterTrails = findViewById(R.id.filterTrails);
        btnFilterActivities = findViewById(R.id.filterActivities);

        rvSteps.setLayoutManager(new LinearLayoutManager(this));

        setupNavbar();
        setupActions();
        setupFilters();
        loadUserInfo();
        
        // Initial state
        btnFilterRoadmaps.performClick();
        loadRoadmapTabs();
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
            // Restoration: Click card -> load sub-items, Click pencil -> edit
            rvSteps.setAdapter(new RoadMapAdapter(list, this::loadTrailsOfRoadmap, this::showRoadmapFormDialog, this::deleteRoadmap));
        });
    }

    private void loadAllTrails() {
        firestoreService.getAllTrails().addOnSuccessListener(querySnapshot -> {
            List<Trail> list = querySnapshot.toObjects(Trail.class);
            rvSteps.setAdapter(new TrailAdapter(list, this::loadActivitiesOfTrail, this::showTrailFormDialog, this::deleteTrail));
        });
    }

    private void loadAllActivities() {
        firestoreService.getAllActivities().addOnSuccessListener(querySnapshot -> {
            List<Activity> list = querySnapshot.toObjects(Activity.class);
            rvSteps.setAdapter(new ActivityAdapter(list, 
                act -> Toast.makeText(this, "Acessando atividade: " + act.getTitle(), Toast.LENGTH_SHORT).show(), 
                this::showActivityFormDialog, 
                this::deleteActivity));
        });
    }

    private void loadTrailsOfRoadmap(RoadMap roadMap) {
        tvJornadaTitle.setText("Jornada: " + roadMap.getTitle());
        firestoreService.getTrailsByRoadMap(roadMap.getId()).addOnSuccessListener(querySnapshot -> {
            List<MapTrail> pivots = querySnapshot.toObjects(MapTrail.class);
            pivots.sort(Comparator.comparingInt(MapTrail::getOrder));
            
            List<Trail> trailsList = new ArrayList<>();
            if (pivots.isEmpty()) {
                rvSteps.setAdapter(new TrailAdapter(trailsList, null, null, null));
                return;
            }

            AtomicInteger count = new AtomicInteger(pivots.size());
            Map<String, Trail> loadedMap = new HashMap<>();
            for (MapTrail pivot : pivots) {
                firestoreService.getTrail(pivot.getTrailId()).addOnSuccessListener(doc -> {
                    if (doc.exists()) loadedMap.put(pivot.getTrailId(), doc.toObject(Trail.class));
                    if (count.decrementAndGet() == 0) {
                        for (MapTrail p : pivots) {
                            Trail t = loadedMap.get(p.getTrailId());
                            if (t != null) trailsList.add(t);
                        }
                        rvSteps.setAdapter(new TrailAdapter(trailsList, this::loadActivitiesOfTrail, this::showTrailFormDialog, this::deleteTrail));
                    }
                });
            }
        });
    }

    private void loadActivitiesOfTrail(Trail trail) {
        tvJornadaTitle.setText("Trilha: " + trail.getTitle());
        firestoreService.getActivitiesByTrail(trail.getId()).addOnSuccessListener(querySnapshot -> {
            List<ActivityTrail> pivots = querySnapshot.toObjects(ActivityTrail.class);
            pivots.sort(Comparator.comparingInt(ActivityTrail::getOrder));

            List<Activity> actsList = new ArrayList<>();
            if (pivots.isEmpty()) {
                rvSteps.setAdapter(new ActivityAdapter(actsList, null, null, null));
                return;
            }

            AtomicInteger count = new AtomicInteger(pivots.size());
            Map<String, Activity> loadedMap = new HashMap<>();
            for (ActivityTrail pivot : pivots) {
                firestoreService.getActivity(pivot.getActivityId()).addOnSuccessListener(doc -> {
                    if (doc.exists()) loadedMap.put(pivot.getActivityId(), doc.toObject(Activity.class));
                    if (count.decrementAndGet() == 0) {
                        for (ActivityTrail p : pivots) {
                            Activity a = loadedMap.get(p.getActivityId());
                            if (a != null) actsList.add(a);
                        }
                        rvSteps.setAdapter(new ActivityAdapter(actsList, 
                            act -> Toast.makeText(this, "Tarefa: " + act.getTitle(), Toast.LENGTH_SHORT).show(),
                            this::showActivityFormDialog, this::deleteActivity));
                    }
                });
            }
        });
    }

    private void deleteRoadmap(RoadMap roadMap) {
        new android.app.AlertDialog.Builder(this)
            .setTitle("Excluir Roadmap")
            .setMessage("Excluir '" + roadMap.getTitle() + "'?")
            .setPositiveButton("Excluir", (d, w) -> {
                firestoreService.deleteRoadMap(roadMap.getId()).addOnSuccessListener(aVoid -> {
                    firestoreService.getTrailsByRoadMap(roadMap.getId()).addOnSuccessListener(qs -> {
                        for (DocumentSnapshot doc : qs) firestoreService.deleteMapTrail(doc.getId());
                    });
                    loadAllRoadmaps();
                });
            }).setNegativeButton("Cancelar", null).show();
    }

    private void deleteTrail(Trail trail) {
        new android.app.AlertDialog.Builder(this)
            .setTitle("Excluir Trilha")
            .setPositiveButton("Excluir", (d, w) -> {
                firestoreService.deleteTrail(trail.getId()).addOnSuccessListener(aVoid -> {
                    db.collection("mapTrail").whereEqualTo("trailId", trail.getId()).get().addOnSuccessListener(qs -> {
                        for (DocumentSnapshot doc : qs) firestoreService.deleteMapTrail(doc.getId());
                    });
                    firestoreService.getActivitiesByTrail(trail.getId()).addOnSuccessListener(qs -> {
                        for (DocumentSnapshot doc : qs) firestoreService.deleteActivityTrail(doc.getId());
                    });
                    loadAllTrails();
                });
            }).setNegativeButton("Cancelar", null).show();
    }

    private void deleteActivity(Activity activity) {
        new android.app.AlertDialog.Builder(this)
            .setTitle("Excluir Tarefa")
            .setPositiveButton("Excluir", (d, w) -> {
                firestoreService.deleteActivity(activity.getId()).addOnSuccessListener(aVoid -> {
                    db.collection("activityTrail").whereEqualTo("activityId", activity.getId()).get().addOnSuccessListener(qs -> {
                        for (DocumentSnapshot doc : qs) firestoreService.deleteActivityTrail(doc.getId());
                    });
                    loadAllActivities();
                });
            }).setNegativeButton("Cancelar", null).show();
    }

    private void loadUserInfo() {
        if (mAuth.getCurrentUser() == null) return;
        String userId = mAuth.getCurrentUser().getUid();
        db.collection("user").document(userId).get().addOnSuccessListener(doc -> {
            if (doc.exists()) {
                String name = doc.getString("fullname");
                TextView userName = findViewById(R.id.userName);
                TextView userAvatar = findViewById(R.id.userAvatar);
                if (userName != null) userName.setText(name);
                if (userAvatar != null && name != null && !name.isEmpty()) {
                    userAvatar.setText(name.substring(0,1).toUpperCase());
                }
            }
        });
    }

    private void loadRoadmapTabs() {
        firestoreService.getAllRoadMaps().addOnSuccessListener(qs -> {
            List<RoadMap> roadmaps = qs.toObjects(RoadMap.class);
            updateTabs(roadmaps);
        });
    }

    private void updateTabs(List<RoadMap> roadmaps) {
        if (tabsContainer == null) return;
        tabsContainer.removeAllViews();
        for (RoadMap rm : roadmaps) {
            View tabView = getLayoutInflater().inflate(R.layout.item_roadmap_tab, tabsContainer, false);
            ((TextView)tabView.findViewById(R.id.tabIcon)).setText("🗺️");
            ((TextView)tabView.findViewById(R.id.tabName)).setText(rm.getTitle());
            tabView.setOnClickListener(v -> loadTrailsOfRoadmap(rm));
            tabsContainer.addView(tabView);
        }
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

            firestoreService.getTrailsByRoadMap(roadMap.getId()).addOnSuccessListener(qs -> {
                List<MapTrail> pivots = qs.toObjects(MapTrail.class);
                pivots.sort(Comparator.comparingInt(MapTrail::getOrder));
                if (pivots.isEmpty()) return;
                
                AtomicInteger count = new AtomicInteger(pivots.size());
                Map<String, Trail> loaded = new HashMap<>();
                for (MapTrail p : pivots) {
                    firestoreService.getTrail(p.getTrailId()).addOnSuccessListener(doc -> {
                        if (doc.exists()) loaded.put(p.getTrailId(), doc.toObject(Trail.class));
                        if (count.decrementAndGet() == 0) {
                            selectedTrails.clear();
                            for (MapTrail pivot : pivots) {
                                Trail t = loaded.get(pivot.getTrailId());
                                if (t != null) selectedTrails.add(t);
                            }
                            selectionAdapter.notifyDataSetChanged();
                        }
                    }).addOnFailureListener(e -> {
                        if (count.decrementAndGet() == 0) selectionAdapter.notifyDataSetChanged();
                    });
                }
            });
        }

        view.findViewById(R.id.btnAddTrail).setOnClickListener(v -> {
            firestoreService.getAllTrails().addOnSuccessListener(qs -> {
                List<Trail> all = qs.toObjects(Trail.class);
                List<Trail> available = new ArrayList<>();
                for (Trail t : all) {
                    boolean sel = false;
                    for (Trail st : selectedTrails) if (st.getId().equals(t.getId())) { sel = true; break; }
                    if (!sel) available.add(t);
                }
                String[] names = new String[available.size()];
                for (int i=0; i<available.size(); i++) names[i] = available.get(i).getTitle();
                new android.app.AlertDialog.Builder(this).setTitle("Adicionar").setItems(names, (d, which) -> {
                    selectedTrails.add(available.get(which));
                    selectionAdapter.notifyItemInserted(selectedTrails.size()-1);
                }).show();
            });
        });

        btnSave.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            if (name.isEmpty()) return;
            RoadMap rm = roadMap != null ? roadMap : new RoadMap();
            rm.setTitle(name);
            rm.setDescription(etDesc.getText().toString());
            if (roadMap != null) rm.setId(roadMap.getId());

            if (roadMap == null) {
                firestoreService.addRoadMap(rm).addOnSuccessListener(ref -> {
                    for (int i=0; i<selectedTrails.size(); i++) firestoreService.addMapTrail(new MapTrail(ref.getId(), selectedTrails.get(i).getId(), false, i+1));
                    dialog.dismiss(); loadAllRoadmaps();
                });
            } else {
                firestoreService.updateRoadMap(rm).addOnSuccessListener(aVoid -> {
                    firestoreService.getTrailsByRoadMap(rm.getId()).addOnSuccessListener(qs -> {
                        for (DocumentSnapshot doc : qs) firestoreService.deleteMapTrail(doc.getId());
                        for (int i=0; i<selectedTrails.size(); i++) firestoreService.addMapTrail(new MapTrail(rm.getId(), selectedTrails.get(i).getId(), false, i+1));
                    });
                    dialog.dismiss(); loadAllRoadmaps();
                });
            }
        });
        view.findViewById(R.id.btnCancel).setOnClickListener(v -> dialog.dismiss());
        dialog.setContentView(view); dialog.show();
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

            firestoreService.getActivitiesByTrail(trail.getId()).addOnSuccessListener(qs -> {
                List<ActivityTrail> pivots = qs.toObjects(ActivityTrail.class);
                pivots.sort(Comparator.comparingInt(ActivityTrail::getOrder));
                if (pivots.isEmpty()) return;

                AtomicInteger count = new AtomicInteger(pivots.size());
                Map<String, Activity> loaded = new HashMap<>();
                for (ActivityTrail p : pivots) {
                    firestoreService.getActivity(p.getActivityId()).addOnSuccessListener(doc -> {
                        if (doc.exists()) loaded.put(p.getActivityId(), doc.toObject(Activity.class));
                        if (count.decrementAndGet() == 0) {
                            selectedActivities.clear();
                            for (ActivityTrail pivot : pivots) {
                                Activity a = loaded.get(pivot.getActivityId());
                                if (a != null) selectedActivities.add(a);
                            }
                            selectionAdapter.notifyDataSetChanged();
                        }
                    }).addOnFailureListener(e -> {
                        if (count.decrementAndGet() == 0) selectionAdapter.notifyDataSetChanged();
                    });
                }
            });
        }

        view.findViewById(R.id.btnAddActivity).setOnClickListener(v -> {
            firestoreService.getAllActivities().addOnSuccessListener(qs -> {
                List<Activity> all = qs.toObjects(Activity.class);
                List<Activity> available = new ArrayList<>();
                for (Activity a : all) {
                    boolean sel = false;
                    for (Activity sa : selectedActivities) if (sa.getId().equals(a.getId())) { sel = true; break; }
                    if (!sel) available.add(a);
                }
                String[] names = new String[available.size()];
                for (int i=0; i<available.size(); i++) names[i] = available.get(i).getTitle();
                new android.app.AlertDialog.Builder(this).setTitle("Adicionar").setItems(names, (d, which) -> {
                    selectedActivities.add(available.get(which));
                    selectionAdapter.notifyItemInserted(selectedActivities.size()-1);
                }).show();
            });
        });

        btnSave.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            if (name.isEmpty()) return;
            Trail t = trail != null ? trail : new Trail();
            t.setTitle(name);
            t.setDescription(etDesc.getText().toString());
            if (trail != null) t.setId(trail.getId());

            if (trail == null) {
                firestoreService.addTrail(t).addOnSuccessListener(ref -> {
                    for (int i=0; i<selectedActivities.size(); i++) firestoreService.addActivityTrail(new ActivityTrail(selectedActivities.get(i).getId(), ref.getId(), false, i+1));
                    dialog.dismiss(); loadAllTrails();
                });
            } else {
                firestoreService.updateTrail(t).addOnSuccessListener(aVoid -> {
                    firestoreService.getActivitiesByTrail(t.getId()).addOnSuccessListener(qs -> {
                        for (DocumentSnapshot doc : qs) firestoreService.deleteActivityTrail(doc.getId());
                        for (int i=0; i<selectedActivities.size(); i++) firestoreService.addActivityTrail(new ActivityTrail(selectedActivities.get(i).getId(), t.getId(), false, i+1));
                    });
                    dialog.dismiss(); loadAllTrails();
                });
            }
        });
        view.findViewById(R.id.btnCancel).setOnClickListener(v -> dialog.dismiss());
        dialog.setContentView(view); dialog.show();
    }

    private void showActivityFormDialog(Activity activity) {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_form_create_activity, null);
        EditText etName = view.findViewById(R.id.etActivityName);
        EditText etDesc = view.findViewById(R.id.etActivityDesc);
        if (activity != null) {
            ((TextView)view.findViewById(R.id.tvDialogTitle)).setText("Editar Atividade");
            etName.setText(activity.getTitle());
            etDesc.setText(activity.getDescription());
        }
        view.findViewById(R.id.btnSaveActivity).setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            if (name.isEmpty()) return;
            Activity act = activity != null ? activity : new Activity();
            act.setTitle(name); act.setDescription(etDesc.getText().toString());
            if (activity != null) act.setId(activity.getId());
            if (activity == null) firestoreService.addActivity(act).addOnSuccessListener(ref -> { dialog.dismiss(); loadAllActivities(); });
            else firestoreService.updateActivity(act).addOnSuccessListener(aVoid -> { dialog.dismiss(); loadAllActivities(); });
        });
        view.findViewById(R.id.btnCancel).setOnClickListener(v -> dialog.dismiss());
        dialog.setContentView(view); dialog.show();
    }

    static class RoadmapTab {
        String id, name, icon;
        RoadmapTab(String id, String name, String icon) { this.id = id; this.name = name; this.icon = icon; }
    }
}
