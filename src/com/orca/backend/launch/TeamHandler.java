package com.orca.backend.launch;

import com.orca.backend.sql.DatabaseConnection;

public class TeamHandler {
    private final DatabaseConnection connection;

    public TeamHandler(DatabaseConnection c) {
        connection = c;
    }
    public boolean newTeam(JSONObj obj){
        return false;
    }
}
