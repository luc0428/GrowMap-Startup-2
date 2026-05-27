package com.example.growmapapp.model;

import com.google.firebase.firestore.DocumentId;

/**
 * Modelo Pivot: União entre RoadMap e Trail.
 */
public class MapTrail {
    @DocumentId
    private String id;
    private String roadMapId;
    private String trailId;
    private boolean completed;
    private int order;

    public MapTrail() {}

    public MapTrail(String roadMapId, String trailId, boolean completed, int order) {
        this.roadMapId = roadMapId;
        this.trailId = trailId;
        this.completed = completed;
        this.order = order;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getRoadMapId() { return roadMapId; }
    public void setRoadMapId(String roadMapId) { this.roadMapId = roadMapId; }

    public String getTrailId() { return trailId; }
    public void setTrailId(String trailId) { this.trailId = trailId; }

    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }

    public int getOrder() { return order; }
    public void setOrder(int order) { this.order = order; }
}
