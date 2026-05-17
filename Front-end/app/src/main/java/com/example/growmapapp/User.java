package com.example.novatelaupx;

public class User {
    private String name;
    private String role;
    private String email;
    private int score;
    private String tenure;

    public User(String name, String role, String email, int score, String tenure) {
        this.name = name;
        this.role = role;
        this.email = email;
        this.score = score;
        this.tenure = tenure;
    }

    public String getName() { return name; }
    public String getRole() { return role; }
    public String getEmail() { return email; }
    public int getScore() { return score; }
    public String getTenure() { return tenure; }
}