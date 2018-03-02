package com.orca.backend.launch;

import com.orca.backend.launch.User.UserLevel;
import com.orca.backend.sql.DatabaseConnection;

public class PitScoutHandler {

    private final DatabaseConnection connection;

    public PitScoutHandler(DatabaseConnection c) {
        connection = c;
    }

    public boolean newTeam(JSONObj obj, User u) {
        if (!JSONObj.checkTemplate("PitScoutTemplate", obj)) {
            return false;
        }
        if(u.getUserLevel() == UserLevel.LIMITED){
            return false;
        }
        return false;
    }
}
