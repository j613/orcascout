package com.orca.backend.launch;

import com.orca.backend.sql.DatabaseConnection;
import java.sql.ResultSet;
import java.sql.SQLException;

public class MatchHandler {
    private final DatabaseConnection connection;

    public MatchHandler(DatabaseConnection c) {
        connection = c;
    }
    public boolean submitNewMatch(JSONObj obj){
        return false;
    }
    public static JSONObj matchToJSON(ResultSet rs, boolean gameStats, boolean submitBy) throws SQLException{
        JSONObj ret = new JSONObj();
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
