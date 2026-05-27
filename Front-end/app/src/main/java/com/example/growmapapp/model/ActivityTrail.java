package com.example.growmapapp.model;

import com.google.firebase.firestore.DocumentId;

/**
 * Modelo Pivot: União entre Activity e Trail.
 */
public class ActivityTrail {
    @DocumentId
    private String id;
    private String activityId;
    private String trailId;
    private boolean completed;
    private int order;

    public ActivityTrail() {}

    public ActivityTrail(String activityId, String trailId, boolean completed, int order) {
        this.activityId = activityId;
        this.trailId = trailId;
        this.completed = completed;
        this.order = order;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getActivityId() { return activityId; }
    public void setActivityId(String activityId) { this.activityId = activityId; }

    public String getTrailId() { return trailId; }
    public void setTrailId(String trailId) { this.trailId = trailId; }

    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }

    public int getOrder() { return order; }
    public void setOrder(int order) { this.order = order; }
}
