package com.orca.backend.launch;

public class User{
    private final String Username,
                         Token;

    public User(String Username, String Token) {
        this.Username = Username;
        this.Token = Token;
    }

    public String getUsername() {
        return Username;
    }

    public String getToken() {
        return Token;
    }

    @Override
    public String toString() {
        return "User{" + "Username=" + Username + ", Token=" + Token + '}';
    }
    
}