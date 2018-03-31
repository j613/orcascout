package com.orca.backend.launch.handlers;

import com.orca.backend.launch.User;
import com.orca.backend.server.Utils;
import com.orca.backend.sql.DatabaseConnection;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MatchHandler {

    private final DatabaseConnection connection;

    public MatchHandler(DatabaseConnection c) {
        connection = c;
    }
    private static final HashMap<Integer, JSONObject> gameTemplates = new HashMap<>();

    static {
        try {
            Path p = new File(MatchHandler.class.getResource("/frontend/gameTemplates/").toURI()).toPath();//TODO CHANGE WHEN JARRING
            Files.walk(p).forEach(n -> {
                if (!n.toFile().isDirectory()) {
                    try {
                        if (n.getFileName().toString().matches("\\d{4}\\.json")) {
                            gameTemplates.put(Integer.parseInt(n.getFileName().toString().split("\\.")[0]),
                                    new JSONObject(new String(Files.readAllBytes(n))));
                        }
                    } catch (IOException ex) {
                        ex.printStackTrace(System.out);
                    }
                }
            });
        } catch (IOException e) {
            System.err.println("Error reading files from disk. abort");
            e.printStackTrace(System.out);
        } catch (URISyntaxException ex) {
            ex.printStackTrace(System.out);
        }
    }

    private static boolean doesFieldMatch(JSONObject field, JSONObject data) throws JSONException {
        String fname = field.getString("name");
        String type = field.getString("type");
        switch (type) {
            case "int": {
                int value = data.getInt(fname);
                if (field.keySet().contains("max_value") && value > field.getInt("max_value")) {
                    return false;
                }
                if (field.keySet().contains("min_value") && value < field.getInt("min_value")) {
                    return false;
                }
                return true;
            }
            case "string": {
                String value = data.getString(fname);
                if (value.contains("\n") || value.contains("\r")
                        || (field.keySet().contains("max_length") && value.length() > field.getInt("max_length"))) {
                    return false;
                }
                return true;
            }
            case "text_box": {
                String value = data.getString(fname);
                if (field.keySet().contains("max_length") && value.length() > field.getInt("max_length")) {
                    return false;
                }
                return true;
            }
            case "enum": {
                String value = data.getString(fname);
                return field.getJSONArray("values").toList().contains(value);
            }
            case "boolean":
                data.getBoolean(fname); //Checks if boolean (im sorry this is literally the best way)
                return true;
        }
        Utils.logln("ERROR IN GAME TEMPLATE: " + type + " IS NOT A VALID FIELD TYPE");
        return false;
    }

    public static boolean doesMatchDataMatchTemplate(int year, JSONObject data) {
        try {
            JSONObject templ = gameTemplates.get(year);
            for (String type : new String[]{"autonomous", "teleop"}) {
                JSONArray tmplAuto = templ.getJSONObject(type).getJSONArray("fields");
                //System.out.println(tmplAuto.iterator());
                Iterator it = tmplAuto.iterator();
                while (it.hasNext()) {
                    Object fieldo = it.next();
                    JSONObject field = (JSONObject) fieldo;
                    if (!doesFieldMatch(field, data.getJSONObject(type))) {
                        return false;
                    }
                }
            }
            return true;
        } catch (JSONException e) {//if key does not exist
            e.printStackTrace(System.out);
            return false;
        }
    }

    /**
     * Adds a scouted match to the database
     *
     * @param u the user who submitted the
     * @param obj the data to submit
     * @return Error code<br>
     * Error Codes:<br>
     * 0: No Error<br>
     * 1: SQL Error<br>
     * 2: Data does not match game data template<br>
     * 3: Template for given Game Year does not exist<br>
     * 4: Match already Scouted<br>
     */
    public int submitNewMatch(User u, JSONObject obj) {
        int gyear = Integer.parseInt(u.getCurrentRegionalId().substring(0, 3));
        if (!gameTemplates.containsKey(gyear)) {
            return 3;
        }
        if (!doesMatchDataMatchTemplate(gyear, obj.getJSONObject("data"))) {
            return 2;
        }
        try {
            PreparedStatement ps = connection.prepareStatement("select * from MATCHES where "
                    + "REGIONAL_ID = ? and TEAM_NUMBER = ? and MATCH_NUMBER = ?");
            ps.setString(1,u.getCurrentRegionalId());
            ps.setInt(2, obj.getInt("team_number"));
            ps.setString(3, obj.getString("match_number"));
            if(!ps.executeQuery().next()){
                return 4;
            }
            ps = connection.prepareStatement("insert into MATCHES"
                    + "(REGIONAL_ID, TEAM_NUMBER, MATCH_NUMBER, GAME_STATS, SUBMIT_BY) values(?,?,?,?,?)");
            ps.setString(1, u.getCurrentRegionalId());
            ps.setInt(2, obj.getInt("team_number"));
            ps.setString(3, obj.getString("match_number"));
            ps.setString(4, obj.getJSONObject("data").toString());
            ps.setInt(5, u.getID());
            ps.execute();
            return 0;
        } catch (SQLException ex) {
            ex.printStackTrace(System.out);
            return 1;
        } catch(JSONException ex){
            ex.printStackTrace(System.out);
            return 5;//???
        }
    }
    /**
     * Checks to see if a match is currently in the database
     * @param u the user to get the current Comp ID from
     * @param obj the data
     * @return Error Code<br>
     * Error Codes:<br>
     * 0: No Error<br>
     * 1: SQL Error<br>
     * 2: JSON Error<br>
     * 3: Match Does Not Exist<br>
     */
    /*
    public int matchExists(User u, JSONObject obj){
        try {
            PreparedStatement ps = connection.prepareStatement("select * from MATCHES where "
                    + "REGIONAL_ID = ? and TEAM_NUMBER = ? and MATCH_NUMBER = ?");
            ps.setString(1,u.getCurrentRegionalId());
            ps.setInt(2, obj.getInt("team_number"));
            ps.setString(3, obj.getString("match_number"));
            return ps.executeQuery().next()?0:3;
        } catch (SQLException ex) {
            ex.printStackTrace(System.out);
            return 1;
        }catch(JSONException ex){
            ex.printStackTrace(System.out);
            return 2;
        }
    }*/
    /**
     * Gets all of the currently scouted matches for the user based off of their current comp id
     * @param u the user from which the comp id will be taken
     * @return the list of matches, or error code<br>
     * Error Codes:<br>
     * 1: SQL Error<br>
     */
    public JSONObject getMatches(User u){
        try {
            PreparedStatement ps = connection.prepareStatement("select * from MATCHES where REGIONAL_ID = ?");
            ps.setString(1,  u.getCurrentRegionalId());
            ResultSet rs = ps.executeQuery();
            JSONObject ret = new JSONObject();
            ret.put("matches", new JSONArray());
            while(rs.next()){
                ret.append("matches", matchToJSON(rs, false, true));
            }
            return ret;
        } catch (SQLException ex) {
            ex.printStackTrace(System.out);
            return Utils.errorJson(1);
        }
    }
    public static JSONObject matchToJSON(ResultSet rs, boolean gameStats, boolean submitBy) throws SQLException {
        JSONObject ret = new JSONObject();
        ret.put("regional_id", rs.getString("regional_id"));
        ret.put("team_number", rs.getString("team_number"));
        ret.put("match_number", rs.getString("match_number"));
        if (gameStats) {
            ret.put("game_stats", rs.getString("game_stats"));
        }
        if (submitBy) {
            ret.put("sumbit_by", rs.getString("submit_by"));
        }
        return ret;
    }
}
