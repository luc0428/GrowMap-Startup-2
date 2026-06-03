package com.example.growmapapp.model;

import com.example.growmapapp.Question;
import com.google.firebase.firestore.DocumentId;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Activity {
    @DocumentId
    private String id;
    private String title;
    private String description;
    private String type; // "quiz" or "content"
    private List<Question> questions;

    public Activity() {
        this.questions = new ArrayList<>();
        this.type = "quiz";
    }

    public Activity(String title, String description) {
        this();
        this.title = title;
        this.description = description;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public List<Question> getQuestions() { return questions; }
    public void setQuestions(List<Question> questions) { this.questions = questions; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Activity activity = (Activity) o;
        return Objects.equals(id, activity.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
