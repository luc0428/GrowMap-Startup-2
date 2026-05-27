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

    public MapTrail() {}

    public MapTrail(String roadMapId, String trailId) {
        this.roadMapId = roadMapId;
        this.trailId = trailId;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getRoadMapId() { return roadMapId; }
    public void setRoadMapId(String roadMapId) { this.roadMapId = roadMapId; }

    public String getTrailId() { return trailId; }
    public void setTrailId(String trailId) { this.trailId = trailId; }
}
