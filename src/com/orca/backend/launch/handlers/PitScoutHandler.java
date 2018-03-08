package com.orca.backend.launch.handlers;

import com.orca.backend.launch.JSONObj;
import com.orca.backend.launch.User;
import com.orca.backend.launch.User.UserLevel;
import com.orca.backend.sql.DatabaseConnection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONArray;

public class PitScoutHandler {

    private final DatabaseConnection connection;

    public PitScoutHandler(DatabaseConnection c) {
        connection = c;
    }
    public static JSONObj pitScoutToJSON(ResultSet rs, boolean ID, boolean image) throws SQLException{
        JSONObj ret = new JSONObj();
        ret.put("notes", rs.getString("notes"));
        ret.put("team_number",rs.getString("team_number"));
        ret.put("team_name",rs.getString("team_name"));
        ret.put("submit_by",rs.getString("submit_by"));
        ret.put("drivetrain",rs.getString("drivetrain"));
        ret.put("regional_id",rs.getString("regional_id"));
        if(ID){
        ret.put("id",rs.getString("ID"));
        }
        if(image){
        ret.put("image",rs.getString("image"));
        }
        return ret;
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
     * 4: Pit Scout Data Already Exists (Use method=update?)
     * 5: No Comp ID
     */
    public int newTeam(JSONObj obj, User u) {
        if (!JSONObj.checkTemplate("PitScoutTemplate", obj)) {
            return 1;
        }
        if(u.getUserLevel() == UserLevel.LIMITED){
            return 2;
        }
        if(u.getCurrentRegionalId()==null){
            return 5;
        }
        try {
            PreparedStatement ps = connection.prepareStatement("select * from PITS where TEAM_NUMBER = ?, REGIONAL_ID = ?");
            ps.setString(1, obj.getString("teamnumber"));
            ps.setString(2, u.getCurrentRegionalId());
            if(ps.executeQuery().next()){
                return 4;
            }
            ps = connection.prepareStatement("insert into PITS"
                    + "(TEAM_NAME, TEAM_NUMBER, REGIONAL_ID, IMAGE, NOTES, SUBMIT_BY, DRIVETRAIN_STYLE)"
                    + " values(?,?,?,?,?,?,?)");
            ps.setString(1, obj.getString("teamname"));
            ps.setString(2, obj.getString("teamnumber"));
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
     * competition checking not implemented
     * @return JSON, or error code
     * error codes:
     * 1: SQL Exception
     */
    public String getTeams(){
        try {
            JSONObj ret = new JSONObj();
            ResultSet res = connection.executeQuery("select * from PITS");
            ret.put("teams",new JSONArray());
            while(res.next()){
                ret.append("teams", pitScoutToJSON(res, false, true));
            }
            return ret.toString();
        } catch (SQLException ex) {
            ex.printStackTrace(System.out);
            return "1";
        }
    }
}
