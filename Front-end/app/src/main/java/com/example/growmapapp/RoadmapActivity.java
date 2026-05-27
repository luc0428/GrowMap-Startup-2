package com.example.growmapapp;

import android.content.Intent;
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
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.growmapapp.adapter.ActivityAdapter;
import com.example.growmapapp.adapter.RoadMapAdapter;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class RoadmapActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirestoreService firestoreService;
    
    private RecyclerView rvSteps;
    private TextView tvJornadaTitle;
    private LinearLayout btnFilterRoadmaps, btnFilterTrails, btnFilterActivities;
    
    private List<Trail> selectedTrails = new ArrayList<>();
    private List<Activity> selectedActivities = new ArrayList<>();
    
    // Mapas para controle de registros Pivot existentes (ID do item -> ID do documento Pivot)
    private Map<String, String> trailPivotIds = new HashMap<>(); 
    private Map<String, String> activityPivotIds = new HashMap<>();

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
        
        // Iniciar com Roadmaps selecionado
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
            rvSteps.setAdapter(new RoadMapAdapter(list, this::showRoadmapFormDialog));
        });
    }

    private void loadAllTrails() {
        firestoreService.getAllTrails().addOnSuccessListener(querySnapshot -> {
            List<Trail> list = querySnapshot.toObjects(Trail.class);
            rvSteps.setAdapter(new TrailAdapter(list, this::showTrailFormDialog));
        });
    }

    private void loadAllActivities() {
        firestoreService.getAllActivities().addOnSuccessListener(querySnapshot -> {
            List<Activity> list = querySnapshot.toObjects(Activity.class);
            rvSteps.setAdapter(new ActivityAdapter(list, this::showActivityFormDialog));
        });
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
        view.findViewById(R.id.optionCreateTask).setOnClickListener(v -> { dialog.dismiss(); showActivityFormDialog(null); });
        view.findViewById(R.id.optionCreateTrail).setOnClickListener(v -> { dialog.dismiss(); showTrailFormDialog(null); });
        dialog.setContentView(view);
        dialog.show();
    }

    private void showRoadmapFormDialog(RoadMap roadMap) {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_form_create_roadmap, null);
        
        TextView tvDialogTitle = view.findViewById(R.id.tvDialogTitle);
        EditText etName = view.findViewById(R.id.etRoadmapName);
        EditText etDesc = view.findViewById(R.id.etRoadmapDesc);
        LinearLayout container = view.findViewById(R.id.trailsContainer);
        TextView btnSave = view.findViewById(R.id.btnSaveRoadmap);

        selectedTrails.clear();
        trailPivotIds.clear();

        if (roadMap != null) {
            tvDialogTitle.setText("Editar Roadmap");
            etName.setText(roadMap.getTitle());
            etDesc.setText(roadMap.getDescription());
            btnSave.setText("Salvar Alterações");

            firestoreService.getTrailsByRoadMap(roadMap.getId()).addOnSuccessListener(querySnapshot -> {
                for (DocumentSnapshot doc : querySnapshot) {
                    String trailId = doc.getString("trailId");
                    trailPivotIds.put(trailId, doc.getId());
                    firestoreService.getTrail(trailId).addOnSuccessListener(trailDoc -> {
                        if (trailDoc.exists()) addTrailToView(container, trailDoc.toObject(Trail.class));
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
                    boolean alreadySelected = false;
                    for (Trail st : selectedTrails) {
                        if (Objects.equals(st.getId(), t.getId())) { alreadySelected = true; break; }
                    }
                    if (!alreadySelected) available.add(t);
                }

                String[] names = new String[available.size()];
                for (int i=0; i<available.size(); i++) names[i] = available.get(i).getTitle();
                
                new android.app.AlertDialog.Builder(this)
                    .setTitle("Adicionar Trilha")
                    .setItems(names, (d, which) -> addTrailToView(container, available.get(which)))
                    .show();
            });
        });

        btnSave.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            if (name.isEmpty()) { etName.setError("Obrigatório"); return; }
            RoadMap rm = roadMap != null ? roadMap : new RoadMap();
            rm.setTitle(name);
            rm.setDescription(etDesc.getText().toString());
            
            if (roadMap == null) {
                firestoreService.addRoadMap(rm).addOnSuccessListener(ref -> {
                    for (Trail t : selectedTrails) firestoreService.addMapTrail(new MapTrail(ref.getId(), t.getId()));
                    dialog.dismiss();
                    loadAllRoadmaps();
                });
            } else {
                firestoreService.updateRoadMap(rm).addOnSuccessListener(aVoid -> {
                    // Sync Pivot: Delete
                    for (Map.Entry<String, String> entry : trailPivotIds.entrySet()) {
                        boolean stillSelected = false;
                        for (Trail st : selectedTrails) if (Objects.equals(st.getId(), entry.getKey())) { stillSelected = true; break; }
                        if (!stillSelected) firestoreService.deleteMapTrail(entry.getValue());
                    }
                    // Sync Pivot: Add
                    for (Trail t : selectedTrails) {
                        if (!trailPivotIds.containsKey(t.getId())) firestoreService.addMapTrail(new MapTrail(roadMap.getId(), t.getId()));
                    }
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
        LinearLayout container = view.findViewById(R.id.activitiesContainer);
        TextView btnSave = view.findViewById(R.id.btnSaveTrail);

        selectedActivities.clear();
        activityPivotIds.clear();

        if (trail != null) {
            tvDialogTitle.setText("Editar Trilha");
            etName.setText(trail.getTitle());
            etDesc.setText(trail.getDescription());
            btnSave.setText("Salvar Alterações");

            firestoreService.getActivitiesByTrail(trail.getId()).addOnSuccessListener(querySnapshot -> {
                for (DocumentSnapshot doc : querySnapshot) {
                    String activityId = doc.getString("activityId");
                    activityPivotIds.put(activityId, doc.getId());
                    firestoreService.getActivity(activityId).addOnSuccessListener(actDoc -> {
                        if (actDoc.exists()) addActivityToView(container, actDoc.toObject(Activity.class));
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
                    boolean alreadySelected = false;
                    for (Activity sa : selectedActivities) {
                        if (Objects.equals(sa.getId(), a.getId())) { alreadySelected = true; break; }
                    }
                    if (!alreadySelected) available.add(a);
                }

                String[] names = new String[available.size()];
                for (int i=0; i<available.size(); i++) names[i] = available.get(i).getTitle();
                
                new android.app.AlertDialog.Builder(this)
                    .setTitle("Adicionar Atividade")
                    .setItems(names, (d, which) -> addActivityToView(container, available.get(which)))
                    .show();
            });
        });

        btnSave.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            if (name.isEmpty()) { etName.setError("Obrigatório"); return; }
            Trail t = trail != null ? trail : new Trail();
            t.setTitle(name);
            t.setDescription(etDesc.getText().toString());

            if (trail == null) {
                firestoreService.addTrail(t).addOnSuccessListener(ref -> {
                    for (Activity a : selectedActivities) firestoreService.addActivityTrail(new ActivityTrail(a.getId(), ref.getId()));
                    dialog.dismiss();
                    loadAllTrails();
                });
            } else {
                firestoreService.updateTrail(t).addOnSuccessListener(aVoid -> {
                    // Sync Pivot: Delete
                    for (Map.Entry<String, String> entry : activityPivotIds.entrySet()) {
                        boolean stillSelected = false;
                        for (Activity sa : selectedActivities) if (Objects.equals(sa.getId(), entry.getKey())) { stillSelected = true; break; }
                        if (!stillSelected) firestoreService.deleteActivityTrail(entry.getValue());
                    }
                    // Sync Pivot: Add
                    for (Activity a : selectedActivities) {
                        if (!activityPivotIds.containsKey(a.getId())) firestoreService.addActivityTrail(new ActivityTrail(a.getId(), trail.getId()));
                    }
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
        TextView btnSave = view.findViewById(R.id.btnSaveActivity);

        if (activity != null) {
            tvDialogTitle.setText("Editar Atividade");
            etName.setText(activity.getTitle());
            etDesc.setText(activity.getDescription());
            btnSave.setText("Salvar Alterações");
        }

        view.findViewById(R.id.btnCancel).setOnClickListener(v -> dialog.dismiss());

        btnSave.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            if (name.isEmpty()) { etName.setError("Obrigatório"); return; }
            Activity act = activity != null ? activity : new Activity();
            act.setTitle(name);
            act.setDescription(etDesc.getText().toString());

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

    private void addTrailToView(LinearLayout container, Trail trail) {
        selectedTrails.add(trail);
        View v = getLayoutInflater().inflate(R.layout.item_trail_selection, container, false);
        ((TextView)v.findViewById(R.id.tvTrailName)).setText(trail.getTitle());
        v.findViewById(R.id.btnRemoveTrail).setOnClickListener(view -> {
            selectedTrails.remove(trail);
            container.removeView(v);
        });
        container.addView(v);
    }

    private void addActivityToView(LinearLayout container, Activity activity) {
        selectedActivities.add(activity);
        View v = getLayoutInflater().inflate(R.layout.item_activity_selection, container, false);
        ((TextView)v.findViewById(R.id.tvActivityName)).setText(activity.getTitle());
        v.findViewById(R.id.btnRemoveActivity).setOnClickListener(view -> {
            selectedActivities.remove(activity);
            container.removeView(v);
        });
        container.addView(v);
    }
}
