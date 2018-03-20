package com.orca.backend.launch.handlers;

import com.orca.backend.launch.OrcascoutHandler;
import com.orca.backend.launch.User;
import com.orca.backend.launch.User.UserLevel;
import com.orca.backend.server.Utils;
import com.orca.backend.sql.DatabaseConnection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.json.JSONArray;
import org.json.JSONObject;

public class PitScoutHandler {

    private final DatabaseConnection connection;

    public PitScoutHandler(DatabaseConnection c) {
        connection = c;
    }

    public static JSONObject pitScoutToJSON(ResultSet rs, boolean ID, boolean image, boolean userInfo) throws SQLException {
        JSONObject ret = new JSONObject();
        ret.put("notes", rs.getString("notes"));
        ret.put("team_number", rs.getString("team_number"));
        ret.put("team_name", rs.getString("team_name"));
        ret.put("submit_by", rs.getString("submit_by"));
        ret.put("drivetrain", rs.getString("drivetrain_style"));
        ret.put("regional_id", rs.getString("regional_id"));
        if (ID) {
            ret.put("id", rs.getString("ID"));
        }
        if (image) {
            ret.put("image", rs.getString("image"));
        }
        if(userInfo){
            ret.put("user_info", OrcascoutHandler.userHandler.getUserByID(rs.getInt("submit_by")).asJSON(true));
        }
        return ret;
    }

    /**
     *
     * @param obj
     * @param u
     * @return the error code<br>
     * Error codes:<br>
     * 0: success<br>
     * 1: invalid template<br>
     * 2: user is limited<br>
     * 3: SQL Error<br>
     * 4: Pit Scout Data Already Exists (Use method=update?)<br>
     * 5: No Comp ID<br>
     */
    public int newTeam(JSONObject obj, User u) {
        if (!Utils.checkTemplate("PitScoutTemplate", obj)) {
            return 1;
        }
        if (u.getUserLevel() == UserLevel.LIMITED) {
            return 2;
        }
        if (u.getCurrentRegionalId() == null) {
            return 5;
        }
        try {
            PreparedStatement ps = connection.prepareStatement("select * from PITS where TEAM_NUMBER = ? AND REGIONAL_ID = ?");
            ps.setInt(1, obj.getInt("teamnumber"));
            ps.setString(2, u.getCurrentRegionalId());
            if (ps.executeQuery().next()) {
                return 4;
            }
            ps = connection.prepareStatement("insert into PITS"
                    + "(TEAM_NAME, TEAM_NUMBER, REGIONAL_ID, IMAGE, NOTES, SUBMIT_BY, DRIVETRAIN_STYLE)"
                    + " values(?,?,?,?,?,?,?)");
            ps.setString(1, obj.getString("teamname"));
            ps.setInt(2, obj.getInt("teamnumber"));
            ps.setString(3, u.getCurrentRegionalId());
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

    /**
     * gets list of pit scouts for current competition
     *
     * @param u the user to get the competitions for(Based off of cached TBA COMP ID)
     * @return JSON, or error code error codes:<br>
     * 1: SQL Exception<br>
     */
    public JSONObject getTeams(User u) {
        try {
            JSONObject ret = new JSONObject();
            PreparedStatement ps = connection.prepareStatement("select * from PITS where REGIONAL_ID = ?");
            ps.setString(1, u.getCurrentRegionalId());
            ResultSet res = ps.executeQuery();
            ret.put("teams", new JSONArray());
            while (res.next()) {
                ret.append("teams", pitScoutToJSON(res, true, false, true));
            }
            return ret;
        } catch (SQLException ex) {
            ex.printStackTrace(System.out);
            return Utils.errorJson(1);
        }
    }
}
