package com.orca.backend.launch.handlers;

import com.orca.backend.sql.DatabaseConnection;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.json.JSONObject;

public class MatchHandler {
    private final DatabaseConnection connection;

    public MatchHandler(DatabaseConnection c) {
        connection = c;
    }
    public boolean submitNewMatch(JSONObject obj){//TODO Implement
        return false;
    }
    public static JSONObject matchToJSON(ResultSet rs, boolean gameStats, boolean submitBy) throws SQLException{
        JSONObject ret = new JSONObject();
        ret.put("regional_id", rs.getString("regional_id"));
        ret.put("team_number",rs.getString("team_number"));
        ret.put("match_number",rs.getString("match_number"));
        if(gameStats){
        ret.put("game_stats",rs.getString("game_stats"));
        }
        if(submitBy){
        ret.put("sumbit_by",rs.getString("submit_by"));
        }
        return ret;
    }
}
