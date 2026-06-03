package com.example.growmapapp.model;

import com.google.firebase.firestore.Exclude;

public class User {
    private String id;
    private String fullname;
    private String role;
    private String gmail;
    private int percentage;
    private String time;
    private String password;
    private boolean adm;
    private String cargo;

    public User() {
        // Precisa do Firebase
    }

    public User(String id, String fullname, String role, String gmail, int percentage, String time, boolean adm, String cargo) {
        this.id = id;
        this.fullname = fullname;
        this.role = role;
        this.gmail = gmail;
        this.percentage = percentage;
        this.time = time;
        this.adm = adm;
        this.cargo = cargo;
    }

    public User(String id, String fullname, String gmail) {
        this.id = id;
        this.fullname = fullname;
        this.gmail = gmail;
        this.adm = false;
        this.cargo = "";
        this.role = "";
        this.percentage = 0;
        this.time = "0h";
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    @Exclude
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isAdm() {
        return adm;
    }

    public void setAdm(boolean adm) {
        this.adm = adm;
    }

    public String getCargo() {
        return cargo;
    }

    public void setCargo(String cargo) {
        this.cargo = cargo;
    }
}
