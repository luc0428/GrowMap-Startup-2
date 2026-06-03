package com.example.growmapapp.api;

public class CardDto {
    private Long id;
    private String icon;
    private String title;
    private String description;

    public CardDto() { }

    public CardDto(String icon, String title, String description) {
        this.icon = icon;
        this.title = title;
        this.description = description;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
