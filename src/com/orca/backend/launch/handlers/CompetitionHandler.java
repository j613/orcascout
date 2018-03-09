package com.orca.backend.launch.handlers;

import com.orca.backend.launch.JSONObj;
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
     * @return error code error codes: 0: no error 1: doesn't exist 2: SQL error
     */
    public int compExists(String compID) {
        try {
            PreparedStatement ps = connection.prepareStatement("select * from COMPETITIONS where COMP_ID = ?");
            return ps.executeQuery().next() ? 0 : 1;
        } catch (SQLException ex) {
            ex.printStackTrace(System.out);
            return 2;
        }
    }
    /**
     * gets all of the competitions in the database
     * @return a JSON list of all of the competitions currently in the database, or error code
     * Error codes:
     * 1: SQL Error
     */
    public JSONObj getComps(){
        try {
            JSONObj ret = new JSONObj();
            ResultSet rs = connection.executeQuery("select * from COMPETITIONS");
            while(rs.next()){
                ret.append("competitions",compToJSON(rs, false));
            }
            return ret;
        } catch (SQLException ex) {
            ex.printStackTrace();
            return Utils.errorJson(1);
        }
    }
}
