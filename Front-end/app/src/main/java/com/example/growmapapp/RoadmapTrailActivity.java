package com.example.growmapapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.growmapapp.adapter.LearningPathAdapter;
import com.example.growmapapp.model.Activity;
import com.example.growmapapp.model.ActivityTrail;
import com.example.growmapapp.service.FirestoreService;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class RoadmapTrailActivity extends AppCompatActivity {

    private String trailId, trailTitle;
    private RecyclerView rvPath;
    private TextView tvTrailTitle, tvUserName, tvUserRole, tvUserAvatar;
    private FirestoreService firestoreService;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_roadmap_trail);

        trailId = getIntent().getStringExtra("trailId");
        trailTitle = getIntent().getStringExtra("trailTitle");

        firestoreService = new FirestoreService();
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        rvPath = findViewById(R.id.rvTrailPath);
        tvTrailTitle = findViewById(R.id.tvTrailTitleContent);
        tvUserName = findViewById(R.id.tvHeaderUserName);
        tvUserRole = findViewById(R.id.tvHeaderUserRole);
        tvUserAvatar = findViewById(R.id.tvHeaderAvatar);

        rvPath.setLayoutManager(new LinearLayoutManager(this));

        if (trailTitle != null) tvTrailTitle.setText(trailTitle);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        
        loadUserInfo();
        loadTrailActivities();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadTrailActivities(); // Atualiza o status de conclusão ao voltar
    }

    private void loadUserInfo() {
        if (mAuth.getCurrentUser() == null) return;
        db.collection("user").document(mAuth.getCurrentUser().getUid()).get().addOnSuccessListener(doc -> {
            if (doc.exists()) {
                String name = doc.getString("fullname");
                String role = doc.getString("role");
                if (tvUserName != null) tvUserName.setText(name);
                if (tvUserRole != null) tvUserRole.setText(role);
                if (tvUserAvatar != null && name != null && !name.isEmpty()) {
                    tvUserAvatar.setText(name.substring(0,1).toUpperCase());
                }
            }
        });
    }

    private void loadTrailActivities() {
        if (trailId == null) return;

        firestoreService.getActivitiesByTrail(trailId).addOnSuccessListener(querySnapshot -> {
            List<ActivityTrail> pivots = querySnapshot.toObjects(ActivityTrail.class);
            pivots.sort(Comparator.comparingInt(ActivityTrail::getOrder));

            if (pivots.isEmpty()) {
                rvPath.setAdapter(null);
                return;
            }

            List<LearningPathAdapter.TrailStep> steps = new ArrayList<>();
            AtomicInteger count = new AtomicInteger(pivots.size());
            
            LearningPathAdapter.TrailStep[] orderedSteps = new LearningPathAdapter.TrailStep[pivots.size()];

            for (int i = 0; i < pivots.size(); i++) {
                final int index = i;
                ActivityTrail pivot = pivots.get(i);
                firestoreService.getActivity(pivot.getActivityId()).addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        Activity activity = doc.toObject(Activity.class);
                        orderedSteps[index] = new LearningPathAdapter.TrailStep(activity, pivot);
                    }
                    if (count.decrementAndGet() == 0) {
                        for (LearningPathAdapter.TrailStep step : orderedSteps) {
                            if (step != null) steps.add(step);
                        }
                        rvPath.setAdapter(new LearningPathAdapter(steps, activity -> {
                            Intent intent = new Intent(this, GameQuizActivity.class);
                            intent.putExtra("activityId", activity.getId());
                            intent.putExtra("trailId", trailId);
                            startActivity(intent);
                        }));
                    }
                });
            }
        });
    }
}
