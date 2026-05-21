package com.example.growmapapp.model;

public class User {
    private String fullname;
    private String role;
    private String gmail;
    private int percentage;
    private String time;
    private String password;

    public User() {
        // Precisa do Firebase
    }

    public User(String fullname, String role, String gmail, int percentage, String time) {
        this.fullname = fullname;
        this.role = role;
        this.gmail = gmail;
        this.percentage = percentage;
        this.time = time;
    }

    public User(String fullname, String gmail, String password) {
        this.fullname = fullname;
        this.gmail = gmail;
        this.password = password;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getGmail() {
        return gmail;
    }

    public void setGmail(String gmail) {
        this.gmail = gmail;
    }

    public int getPercentage() {
        return percentage;
    }

    public void setPercentage(int percentage) {
        this.percentage = percentage;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
