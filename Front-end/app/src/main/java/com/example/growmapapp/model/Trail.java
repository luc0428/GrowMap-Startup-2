package com.example.growmapapp.model;

import com.google.firebase.firestore.DocumentId;
import java.util.Objects;

public class Trail {
    @DocumentId
    private String id;
    private String title;
    private String description;

    public Trail() {}

    public Trail(String title, String description) {
        this.title = title;
        this.description = description;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Trail trail = (Trail) o;
        return Objects.equals(id, trail.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
