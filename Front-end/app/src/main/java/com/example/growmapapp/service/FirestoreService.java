package com.example.growmapapp.service;

import com.example.growmapapp.model.Activity;
import com.example.growmapapp.model.ActivityTrail;
import com.example.growmapapp.model.MapTrail;
import com.example.growmapapp.model.RoadMap;
import com.example.growmapapp.model.Trail;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.List;

/**
 * Serviço centralizado para operações CRUD no Firestore.
 */
public class FirestoreService {

    private final FirebaseFirestore db;

    // Nomes das coleções
    private static final String COL_ACTIVITY = "activity";
    private static final String COL_ROADMAP = "roadMap";
    private static final String COL_TRAIL = "trail";
    private static final String COL_MAP_TRAIL = "mapTrail";
    private static final String COL_ACTIVITY_TRAIL = "activityTrail";

    public FirestoreService() {
        this.db = FirebaseFirestore.getInstance();
    }

    // --- CRUD ACTIVITY ---

    public Task<DocumentReference> addActivity(Activity activity) {
        return db.collection(COL_ACTIVITY).add(activity);
    }

    public Task<QuerySnapshot> getAllActivities() {
        return db.collection(COL_ACTIVITY).get();
    }

    public Task<Void> updateActivity(Activity activity) {
        return db.collection(COL_ACTIVITY).document(activity.getId()).set(activity);
    }

    public Task<Void> deleteActivity(String id) {
        return db.collection(COL_ACTIVITY).document(id).delete();
    }

    // --- CRUD ROADMAP ---

    public Task<DocumentReference> addRoadMap(RoadMap roadMap) {
        return db.collection(COL_ROADMAP).add(roadMap);
    }

    public Task<QuerySnapshot> getAllRoadMaps() {
        return db.collection(COL_ROADMAP).get();
    }

    public Task<Void> updateRoadMap(RoadMap roadMap) {
        return db.collection(COL_ROADMAP).document(roadMap.getId()).set(roadMap);
    }

    public Task<Void> deleteRoadMap(String id) {
        return db.collection(COL_ROADMAP).document(id).delete();
    }

    // --- CRUD TRAIL ---

    public Task<DocumentReference> addTrail(Trail trail) {
        return db.collection(COL_TRAIL).add(trail);
    }

    public Task<QuerySnapshot> getAllTrails() {
        return db.collection(COL_TRAIL).get();
    }

    public Task<Void> updateTrail(Trail trail) {
        return db.collection(COL_TRAIL).document(trail.getId()).set(trail);
    }

    public Task<Void> deleteTrail(String id) {
        return db.collection(COL_TRAIL).document(id).delete();
    }

    // --- CRUD MAP_TRAIL (Pivot) ---

    public Task<DocumentReference> addMapTrail(MapTrail mapTrail) {
        return db.collection(COL_MAP_TRAIL).add(mapTrail);
    }

    public Task<QuerySnapshot> getTrailsByRoadMap(String roadMapId) {
        return db.collection(COL_MAP_TRAIL).whereEqualTo("roadMapId", roadMapId).get();
    }

    public Task<Void> deleteMapTrail(String id) {
        return db.collection(COL_MAP_TRAIL).document(id).delete();
    }

    // --- CRUD ACTIVITY_TRAIL (Pivot) ---

    public Task<DocumentReference> addActivityTrail(ActivityTrail activityTrail) {
        return db.collection(COL_ACTIVITY_TRAIL).add(activityTrail);
    }

    public Task<QuerySnapshot> getActivitiesByTrail(String trailId) {
        return db.collection(COL_ACTIVITY_TRAIL).whereEqualTo("trailId", trailId).get();
    }

    public Task<Void> deleteActivityTrail(String id) {
        return db.collection(COL_ACTIVITY_TRAIL).document(id).delete();
    }
}
