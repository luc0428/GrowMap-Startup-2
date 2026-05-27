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

    public ActivityTrail() {}

    public ActivityTrail(String activityId, String trailId) {
        this.activityId = activityId;
        this.trailId = trailId;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getActivityId() { return activityId; }
    public void setActivityId(String activityId) { this.activityId = activityId; }

    public String getTrailId() { return trailId; }
    public void setTrailId(String trailId) { this.trailId = trailId; }
}
