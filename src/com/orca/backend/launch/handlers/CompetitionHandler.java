package com.orca.backend.launch.handlers;

import com.orca.backend.launch.JSONObj;
import com.orca.backend.launch.User;
import com.orca.backend.launch.User.UserLevel;
import com.orca.backend.server.Utils;
import com.orca.backend.sql.DatabaseConnection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CompetitionHandler {

    private final DatabaseConnection connection;

    public CompetitionHandler(DatabaseConnection c) {
        connection = c;
    }

    public static JSONObj compToJSON(ResultSet rs, boolean Id) throws SQLException {
        JSONObj ret = new JSONObj();
        ret.put("nickname", rs.getString("NICKNAME"));
        ret.put("comp_id", rs.getString("COMP_ID"));
        if (Id) {
            ret.put("id", rs.getString("ID"));
        }
        return ret;
    }

    /**
     * check to see if competition id is in the database
     *
     * @param compID
     * @return error code error codes:<br>
     * 0: no error<br>
     * 1: doesn't exist<br>
     * 2: SQL error
     */
    public int compExists(String compID) {
        try {
            PreparedStatement ps = connection.prepareStatement("select * from COMPETITIONS where COMP_ID = ?");
            ps.setString(1, compID);
            return ps.executeQuery().next() ? 0 : 1;
        } catch (SQLException ex) {
            ex.printStackTrace(System.out);
            return 2;
        }
    }

    /**
     * gets all of the competitions in the database
     *
     * @return a JSON list of all of the competitions currently in the database,
     * or error code Error codes:<br>
     * 1: SQL Error
     */
    public JSONObj getComps() {
        try {
            JSONObj ret = new JSONObj();
            ResultSet rs = connection.executeQuery("select * from COMPETITIONS");
            while (rs.next()) {
                ret.append("competitions", compToJSON(rs, false));
            }
            return ret;
        } catch (SQLException ex) {
            ex.printStackTrace();
            return Utils.errorJson(1);
        }
    }

    /**
     * registers a competition into the database
     *
     * @param data the data for the competition
     * @param u the user to register it
     * @return the error code error codes: 0: no error 1: SQL Error 2: User is
     * not Admin 3: incorrect template
     */
    public int registerComp(JSONObj data, User u) {
        try {
            //TODO Implement TBA API
            if (!JSONObj.checkTemplate("Competition", data)) {
                return 3;
            }
            if (u.getUserLevel() != UserLevel.ADMIN) {
                return 2;
            }
            PreparedStatement ps = connection.prepareStatement("insert into COMPETITIONS(NICKNAME, COMP_ID) values (?,?)");
            ps.setString(1, data.getString("nickname"));
            ps.setString(2, data.getString("comp_id"));
            ps.execute();
            return 0;
        } catch (SQLException ex) {
            ex.printStackTrace(System.out);
            return 1;
        }
    }
}
