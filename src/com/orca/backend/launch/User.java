package com.orca.backend.launch;

public class User{
    private final String username,
                         token,
                         userLevel,
                         firstname,
                         lastname;
    private final int ID;
    public User(int ID, String username, String token, String userLevel, String firstname, String lastname) {
        this.username = username;
        this.token = token;
        this.userLevel = userLevel;
        this.firstname = firstname;
        this.lastname = lastname;
        this.ID = ID;
    }

    public String getFirstname() {
        return firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public String getUsername() {
        return username;
    }

    public String getToken() {
        return token;
    }

    public int getID() {
        return ID;
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