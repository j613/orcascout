package com.orca.backend.launch.handlers;

import com.orca.backend.launch.User;
import com.orca.backend.launch.User.UserLevel;
import com.orca.backend.server.Utils;
import com.orca.backend.sql.DatabaseConnection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.json.JSONException;
import org.json.JSONObject;

public class CompetitionHandler {

    private final DatabaseConnection connection;

    public CompetitionHandler(DatabaseConnection c) {
        connection = c;
    }

    public static JSONObject compToJSON(ResultSet rs, boolean Id) throws SQLException {
        JSONObject ret = new JSONObject();
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
    public JSONObject getComps() {
        try {
            JSONObject ret = new JSONObject();
            ResultSet rs = connection.executeQuery("select * from COMPETITIONS");
            while (rs.next()) {
                ret.append("competitions", compToJSON(rs, false));
            }
            return ret;
        } catch (SQLException ex) {
            ex.printStackTrace(System.out);
            return Utils.errorJson(1);
        }
    }

    /**
     * registers a competition into the database
     *
     * @param data the data for the competition
     * @param u the user to register it
     * @return the error code<br>
     * Error codes:<br>
     * 0: No error<br>
     * 1: SQL Error<br>
     * 2: User is not Admin<br>
     * 3: Invalid JSON Object<br>
     * 4: Match ID is not valid<br>
     * 5: TBA API Connection Error<br>
     */
    public int registerComp(JSONObject data, User u) {
        try {
            if (u.getUserLevel() != UserLevel.ADMIN) {
                return 2;
            }
            int exec = BlueAllianceHandler.matchIsValid(data.getString("comp_id"));
            if(exec!=0){
                return exec+3;
            }
            PreparedStatement ps = connection.prepareStatement("insert into COMPETITIONS(NICKNAME, COMP_ID) values (?,?)");
            ps.setString(1, data.getString("nickname"));
            ps.setString(2, data.getString("comp_id"));
            ps.execute();
            return 0;
        } catch (SQLException ex) {
            ex.printStackTrace(System.out);
            return 1;
        } catch (JSONException e) {
            return 3;
        }
    }
}
