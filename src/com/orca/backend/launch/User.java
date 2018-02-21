package com.orca.backend.launch;

public class User{
    private final String username,
                         token,
                         userLevel;

    public User(String Username, String Token, String level) {
        this.username = Username;
        this.token = Token;
        this.userLevel = level;
    }

    public String getUsername() {
        return username;
    }

    public String getToken() {
        return token;
    }
/*
    @Override
    public String toString() {
        return "User{" + "Username=" + Username + ", Token=" + Token + '}';
    }*/

    public String getUserLevel() {
        return userLevel;
    }
    
    
}