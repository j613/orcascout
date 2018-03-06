package com.orca.backend.launch;

public class User {

    public static enum UserLevel {
        LIMITED,
        REGULAR,
        ADMIN;
    }
    private final String username,
            firstname,
            lastname;
    private final int ID;
    private final long loginTime;
    private final UserLevel userLevel;
    private transient String token,
            currentRegionalId;

    public User(int ID, String username, String token, UserLevel userLevel, String firstname, String lastname) {
        this.username = username;
        this.token = token;
        this.userLevel = userLevel;
        this.firstname = firstname;
        this.lastname = lastname;
        this.ID = ID;
        loginTime = System.currentTimeMillis();
    }

    public User(int ID, String username, String token, String userLevel, String firstname, String lastname) {
        this(ID, username, token, UserLevel.valueOf(userLevel.toUpperCase()), firstname, lastname);
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

    @Override
    public String toString() {
        return "User{" + "username=" + username + ", token=" + token + ", userLevel=" + userLevel + ", firstname=" + firstname + ", lastname=" + lastname + ", ID=" + ID + '}';
    }

    /*
    @Override
    public String toString() {
        return "User{" + "Username=" + Username + ", Token=" + Token + '}';
    }*/
    public UserLevel getUserLevel() {
        return userLevel;
    }

    public boolean shouldLogout() {
        return System.currentTimeMillis() - loginTime > 259200000;
    }

    public String getCurrentRegionalId() {
        return currentRegionalId;
    }

    public void setCurrentRegionalId(String currentRegionalId) {
        this.currentRegionalId = currentRegionalId;
    }

}
