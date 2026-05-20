package com.example.growmapapp.model;

public class User {
    private String fullname;
    private String gmail;
    private String password;

    public User() {
        // Required for Firebase
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

    public String getGmail() {
        return gmail;
    }

    public void setGmail(String gmail) {
        this.gmail = gmail;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
