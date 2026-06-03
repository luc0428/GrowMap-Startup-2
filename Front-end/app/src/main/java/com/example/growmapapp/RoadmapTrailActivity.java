package com.example.growmapapp;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.growmapapp.adapter.RoadmapNodeAdapter;
import com.example.growmapapp.model.Activity;
import com.example.growmapapp.model.ActivityTrail;
import com.example.growmapapp.model.MapTrail;
import com.example.growmapapp.model.Trail;
import com.example.growmapapp.service.FirestoreService;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class RoadmapTrailActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirestoreService firestoreService;
    private RecyclerView rvTrailPath;
    private String roadmapId;
    private List<RoadmapNodeAdapter.RoadmapItem> roadmapItems = new ArrayList<>();
    private boolean isCurrentFound = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_roadmap_trail);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        firestoreService = new FirestoreService();

        rvTrailPath = findViewById(R.id.rvTrailPath);
        rvTrailPath.setLayoutManager(new LinearLayoutManager(this));

        roadmapId = getIntent().getStringExtra("roadmapId");
        String roadmapTitle = getIntent().getStringExtra("roadmapTitle");

        if (roadmapTitle != null) {
            TextView tvTitle = findViewById(R.id.tvTrailTitleContent);
            if (tvTitle != null) tvTitle.setText(roadmapTitle);
        }

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        ImageView btnTheme = findViewById(R.id.btnThemeToggle);
        if (btnTheme != null) {
            updateThemeIcon(btnTheme);
            btnTheme.setOnClickListener(v -> {
                int mode = AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES ?
                        AppCompatDelegate.MODE_NIGHT_NO : AppCompatDelegate.MODE_NIGHT_YES;
                AppCompatDelegate.setDefaultNightMode(mode);
                recreate();
            });
        }

        loadUserInfo();
        loadRoadmapActivities();
    }

    private void loadRoadmapActivities() {
        if (roadmapId == null) return;

        roadmapItems.clear();
        isCurrentFound = false;

        // 1. Get all Trails for this Roadmap
        firestoreService.getTrailsByRoadMap(roadmapId).addOnSuccessListener(querySnapshot -> {
            List<MapTrail> mapTrails = querySnapshot.toObjects(MapTrail.class);
            mapTrails.sort(Comparator.comparingInt(MapTrail::getOrder));

            if (mapTrails.isEmpty()) return;

            AtomicInteger trailsLoaded = new AtomicInteger(0);
            
            // This will hold the complete list of items per trail index
            List<List<RoadmapNodeAdapter.RoadmapItem>> trailsContent = new ArrayList<>();
            for (int i = 0; i < mapTrails.size(); i++) trailsContent.add(new ArrayList<>());

            for (int i = 0; i < mapTrails.size(); i++) {
                final int trailIndex = i;
                String trailId = mapTrails.get(i).getTrailId();

                // 2a. Fetch Trail details (Title/Desc)
                firestoreService.getTrail(trailId).addOnSuccessListener(trailDoc -> {
                    Trail trail = trailDoc.toObject(Trail.class);
                    String tTitle = trail != null ? trail.getTitle() : "Trilha";
                    String tDesc = trail != null ? trail.getDescription() : "";

                    // 2b. Get all Activities for each Trail
                    firestoreService.getActivitiesByTrail(trailId).addOnSuccessListener(actQuery -> {
                        List<ActivityTrail> pivotActs = actQuery.toObjects(ActivityTrail.class);
                        pivotActs.sort(Comparator.comparingInt(ActivityTrail::getOrder));

                        List<RoadmapNodeAdapter.RoadmapItem> currentTrailList = trailsContent.get(trailIndex);
                        
                        // Add Header
                        currentTrailList.add(new RoadmapNodeAdapter.RoadmapItem(0, tTitle, tDesc));

                        if (pivotActs.isEmpty()) {
                            currentTrailList.add(new RoadmapNodeAdapter.RoadmapItem(2, tTitle, ""));
                            checkIfAllLoaded(trailsLoaded, mapTrails.size(), trailsContent);
                            return;
                        }

                        AtomicInteger actsInTrailLoaded = new AtomicInteger(0);
                        for (ActivityTrail pivot : pivotActs) {
                            firestoreService.getActivity(pivot.getActivityId()).addOnSuccessListener(actDoc -> {
                                if (actDoc.exists()) {
                                    Activity activity = actDoc.toObject(Activity.class);
                                    RoadmapNodeAdapter.ActivityNode node = new RoadmapNodeAdapter.ActivityNode(
                                            activity, pivot.getId(), pivot.isCompleted() ? 0 : 2, false);
                                    
                                    // Add to current trail list (index 0 is header, so we add after or sort later)
                                    synchronized (currentTrailList) {
                                        currentTrailList.add(new RoadmapNodeAdapter.RoadmapItem(node));
                                    }
                                }

                                if (actsInTrailLoaded.incrementAndGet() == pivotActs.size()) {
                                    // Re-sort activities within currentTrailList based on pivot order, keeping header at 0
                                    List<RoadmapNodeAdapter.RoadmapItem> sortedTrailContent = new ArrayList<>();
                                    sortedTrailContent.add(currentTrailList.get(0)); // Header
                                    
                                    for (ActivityTrail pa : pivotActs) {
                                        for (int k = 1; k < currentTrailList.size(); k++) {
                                            RoadmapNodeAdapter.RoadmapItem ri = currentTrailList.get(k);
                                            if (ri.activityNode != null && ri.activityNode.activity.getId().equals(pa.getActivityId())) {
                                                sortedTrailContent.add(ri);
                                                break;
                                            }
                                        }
                                    }
                                    // Add Footer
                                    sortedTrailContent.add(new RoadmapNodeAdapter.RoadmapItem(2, tTitle, ""));
                                    
                                    trailsContent.set(trailIndex, sortedTrailContent);
                                    checkIfAllLoaded(trailsLoaded, mapTrails.size(), trailsContent);
                                }
                            });
                        }
                    });
                });
            }
        });
    }

    private void checkIfAllLoaded(AtomicInteger counter, int total, List<List<RoadmapNodeAdapter.RoadmapItem>> allData) {
        if (counter.incrementAndGet() == total) {
            roadmapItems.clear();
            for (List<RoadmapNodeAdapter.RoadmapItem> trailItems : allData) {
                roadmapItems.addAll(trailItems);
            }

            // 3. Calculate states globally (Done, Current, Locked)
            isCurrentFound = false;
            int activityCount = 0;
            for (int i = 0; i < roadmapItems.size(); i++) {
                RoadmapNodeAdapter.RoadmapItem item = roadmapItems.get(i);
                if (item.type == 1) { // Activity
                    RoadmapNodeAdapter.ActivityNode node = item.activityNode;
                    node.sideLeft = (activityCount % 2 == 0);
                    activityCount++;

                    if (node.status == 0) {
                        // Already marked as done
                    } else if (!isCurrentFound) {
                        node.status = 1; // Current
                        isCurrentFound = true;
                    } else {
                        node.status = 2; // Locked
                    }
                }
            }

            // 4. Set Adapter
            rvTrailPath.setAdapter(new RoadmapNodeAdapter(roadmapItems, this::showActivityModal));
        }
    }

    private void showActivityModal(RoadmapNodeAdapter.ActivityNode node) {
        if (node.status == 2) {
            Toast.makeText(this, "Esta atividade está bloqueada!", Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_activity_detail, null);

        TextView tvTitle = view.findViewById(R.id.tvModalTitle);
        TextView tvDesc = view.findViewById(R.id.tvModalDesc);
        CheckBox cbCompleted = view.findViewById(R.id.cbCompleted);
        Button btnSave = view.findViewById(R.id.btnSave);
        Button btnClose = view.findViewById(R.id.btnClose);

        tvTitle.setText(node.activity.getTitle());
        tvDesc.setText(node.activity.getDescription());
        cbCompleted.setChecked(node.status == 0);

        AlertDialog dialog = builder.setView(view).create();
        if (dialog.getWindow() != null) dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        btnSave.setOnClickListener(v -> {
            db.collection("activityTrail").document(node.pivotId)
                    .update("completed", cbCompleted.isChecked())
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Progresso atualizado!", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                        loadRoadmapActivities();
                    });
        });

        btnClose.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
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

    private void loadUserInfo() {
        if (mAuth.getCurrentUser() == null) return;
        String userId = mAuth.getCurrentUser().getUid();
        db.collection("user").document(userId).get().addOnSuccessListener(doc -> {
            if (doc.exists()) {
                String name = doc.getString("fullname");
                String role = doc.getString("role");

                TextView tvAvatar = findViewById(R.id.tvHeaderAvatar);
                TextView tvUserName = findViewById(R.id.tvHeaderUserName);
                TextView tvUserRole = findViewById(R.id.tvHeaderUserRole);

                if (name != null && !name.isEmpty()) {
                    if (tvAvatar != null) tvAvatar.setText(String.valueOf(name.charAt(0)).toUpperCase());
                    if (tvUserName != null) tvUserName.setText(name);
                }

                if (tvUserRole != null && role != null) tvUserRole.setText(role);
            }
        });
    }
}
