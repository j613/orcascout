package com.orca.backend.launch;

import com.orca.backend.launch.User.UserLevel;
import com.orca.backend.sql.DatabaseConnection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PitScoutHandler {

    private final DatabaseConnection connection;

    public PitScoutHandler(DatabaseConnection c) {
        connection = c;
    }
    /**
     * 
     * @param obj
     * @param u
     * @return the error code
     * Error codes:
     * 0: success
     * 1: invalid template
     * 2: user is limited
     * 3: SQL Error
     */
    public int newTeam(JSONObj obj, User u) {
        if (!JSONObj.checkTemplate("PitScoutTemplate", obj)) {
            return 1;
        }
        if(u.getUserLevel() == UserLevel.LIMITED){
            return 2;
        }
        
        try {
            PreparedStatement ps = connection.prepareStatement("insert into PITS"
                    + "(TEAM_NAME, TEAM_NUMBER, REGIONAL_ID, IMAGE, NOTES, SUBMIT_BY, DRIVETRAIN_STYLE)"
                    + " values(?,?,?,?,?,?,?)");
            ps.setString(1, obj.getString("teamname"));
            ps.setString(2, obj.getString("teamnumber"));
            //INSERT REGIONAL_ID CODE HERE
            ps.setString(4, obj.getString("image"));
            ps.setString(5, obj.getString("notes"));
            ps.setInt(6, u.getID());
            ps.setString(7, obj.getString("drivetrain"));
            ps.execute();
            return 0;
        } catch (SQLException ex) {
            ex.printStackTrace(System.out);
            return 3;
        }
    }
}
