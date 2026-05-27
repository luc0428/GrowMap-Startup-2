package com.example.growmapapp;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.growmapapp.model.ActivityTrail;
import com.example.growmapapp.model.MapTrail;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.List;

public class RoadmapTrailActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private LinearLayout roadmapContainer;
    private String roadmapId;
    private boolean isCurrentActivityFound = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_roadmap_trail);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        roadmapContainer = findViewById(R.id.roadmapContainer);

        roadmapId = getIntent().getStringExtra("roadmapId");
        String roadmapTitle = getIntent().getStringExtra("roadmapTitle");

        if (roadmapTitle != null) {
            TextView tvTitle = findViewById(R.id.tvRoadmapTitle);
            if (tvTitle != null) tvTitle.setText(roadmapTitle);
        }

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        loadUserInfo();
        loadDynamicRoadmap();
    }

    private void loadDynamicRoadmap() {
        if (roadmapId == null) return;

        roadmapContainer.removeAllViews();
        isCurrentActivityFound = false;

        db.collection("mapTrail")
                .whereEqualTo("roadMapId", roadmapId)
                .orderBy("order", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(mapTrailSnap -> {
                    for (DocumentSnapshot mapTrailDoc : mapTrailSnap) {
                        MapTrail mapTrail = mapTrailDoc.toObject(MapTrail.class);
                        if (mapTrail != null) {
                            renderTrail(mapTrail.getTrailId());
                        }
                    }
                });
    }

    private void renderTrail(String trailId) {
        db.collection("trail").document(trailId).get().addOnSuccessListener(trailDoc -> {
            String trailTitle = trailDoc.getString("title");
            addTrailHeader("Fim da " + (trailTitle != null ? trailTitle : "Trilha"));

            db.collection("activityTrail")
                    .whereEqualTo("trailId", trailId)
                    .orderBy("order", Query.Direction.DESCENDING)
                    .get()
                    .addOnSuccessListener(actTrailSnap -> {
                        List<DocumentSnapshot> docs = actTrailSnap.getDocuments();
                        // Iterar para renderizar as atividades
                        for (int i = 0; i < docs.size(); i++) {
                            DocumentSnapshot doc = docs.get(i);
                            ActivityTrail actTrail = doc.toObject(ActivityTrail.class);
                            if (actTrail != null) {
                                boolean sideLeft = (i % 2 == 0); 
                                fetchAndRenderActivity(actTrail, doc.getId(), sideLeft);
                            }
                        }
                    });
        });
    }

    private void addTrailHeader(String text) {
        TextView tv = new TextView(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.CENTER_HORIZONTAL;
        params.setMargins(0, 40, 0, 10);
        tv.setLayoutParams(params);
        tv.setText(text);
        tv.setTextColor(ContextCompat.getColor(this, R.color.cyan));
        tv.setTextSize(12);
        tv.setTypeface(null, android.graphics.Typeface.BOLD);
        roadmapContainer.addView(tv);
    }

    private void fetchAndRenderActivity(ActivityTrail actTrail, String pivotId, boolean sideLeft) {
        db.collection("activity").document(actTrail.getActivityId()).get().addOnSuccessListener(doc -> {
            if (!doc.exists()) return;
            
            String title = doc.getString("title");
            String desc = doc.getString("description");
            
            int status; 
            if (actTrail.isCompleted()) {
                status = 0;
            } else if (!isCurrentActivityFound) {
                status = 1;
                isCurrentActivityFound = true;
            } else {
                status = 2;
            }

            addActivityRow(pivotId, title, desc, status, sideLeft, actTrail.isCompleted());
        });
    }

    private void addActivityRow(String pivotId, String title, String desc, int status, boolean sideLeft, boolean completed) {
        View row = LayoutInflater.from(this).inflate(R.layout.item_roadmap_dynamic_row, roadmapContainer, false);
        
        ImageView iconNode = row.findViewById(R.id.iconNode);
        View glow = row.findViewById(R.id.nodeGlow);
        View nodeBg = row.findViewById(R.id.nodeBg);
        
        TextView tvTitle = row.findViewById(R.id.tvTitle);
        TextView tvDesc = row.findViewById(R.id.tvDesc);

        tvTitle.setText(title);
        tvDesc.setText(desc);

        switch (status) {
            case 0: // Concluída (Azul)
                glow.setBackgroundResource(R.drawable.bg_node_glow_cyan);
                nodeBg.setBackgroundResource(R.drawable.bg_node_start);
                iconNode.setImageResource(android.R.drawable.checkbox_on_background);
                break;
            case 1: // Atual (Roxa)
                glow.setBackgroundResource(R.drawable.bg_node_glow_purple);
                nodeBg.setBackgroundResource(R.drawable.bg_node_inprogress);
                iconNode.setImageResource(R.drawable.ic_hourglass);
                break;
            case 2: // Bloqueada (Amarela)
                glow.setBackgroundResource(R.drawable.bg_node_glow_yellow);
                nodeBg.setBackgroundResource(R.drawable.bg_node_locked);
                iconNode.setImageResource(R.drawable.ic_lock);
                break;
        }

        LinearLayout rootRow = (LinearLayout) row;
        if (!sideLeft) {
            View spacer = row.findViewById(R.id.spacer);
            View node = row.findViewById(R.id.nodeContainer);
            View card = row.findViewById(R.id.cardContainer);
            
            rootRow.removeAllViews();
            rootRow.addView(card);
            rootRow.addView(node);
            rootRow.addView(spacer);
            
            LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) card.getLayoutParams();
            lp.setMargins(0, 0, 12, 0);
            card.setLayoutParams(lp);
        }

        row.setOnClickListener(v -> {
            if (status == 2) {
                Toast.makeText(this, "Essa atividade está bloqueada, finalize a anteriores para progredir", Toast.LENGTH_SHORT).show();
            } else {
                showActivityModal(pivotId, title, desc, completed);
            }
        });

        roadmapContainer.addView(row);
    }

    private void showActivityModal(String pivotId, String title, String desc, boolean isCompletedInitial) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_activity_detail, null);

        TextView tvTitleModal = view.findViewById(R.id.tvModalTitle);
        TextView tvDescModal = view.findViewById(R.id.tvModalDesc);
        CheckBox cbCompleted = view.findViewById(R.id.cbCompleted);
        Button btnSave = view.findViewById(R.id.btnSave);
        Button btnClose = view.findViewById(R.id.btnClose);

        tvTitleModal.setText(title);
        tvDescModal.setText(desc);
        cbCompleted.setChecked(isCompletedInitial);

        AlertDialog dialog = builder.setView(view).create();
        if (dialog.getWindow() != null) dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        btnSave.setOnClickListener(v -> {
            db.collection("activityTrail").document(pivotId)
                    .update("completed", cbCompleted.isChecked())
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Progresso atualizado!", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                        loadDynamicRoadmap();
                    });
        });

        btnClose.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void loadUserInfo() {
        if (mAuth.getCurrentUser() == null) return;
        String userId = mAuth.getCurrentUser().getUid();
        db.collection("user").document(userId).get().addOnSuccessListener(doc -> {
            if (doc.exists()) {
                String name = doc.getString("fullname");
                String cargo = doc.getString("cargo");
                String role = doc.getString("role");

                TextView tvAvatar = findViewById(R.id.userAvatar);
                TextView tvFirstName = findViewById(R.id.userNameFirst);
                TextView tvLastName = findViewById(R.id.userNameLast);
                TextView tvUserRole = findViewById(R.id.userRole);

                if (name != null && !name.isEmpty()) {
                    if (tvAvatar != null) tvAvatar.setText(String.valueOf(name.charAt(0)).toUpperCase());
                    String[] parts = name.split(" ");
                    if (tvFirstName != null) tvFirstName.setText(String.format("%s ", parts[0]));
                    if (tvLastName != null) tvLastName.setText(parts.length > 1 ? parts[1] : "");
                }

                String displayRole = (cargo != null && !cargo.isEmpty()) ? cargo : role;
                if (tvUserRole != null && displayRole != null) tvUserRole.setText(displayRole);
            }
        });
    }
}
