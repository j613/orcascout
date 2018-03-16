package com.orca.backend.launch;

import com.orca.backend.launch.handlers.CompetitionHandler;
import com.orca.backend.launch.handlers.MatchHandler;
import com.orca.backend.launch.handlers.PitScoutHandler;
import com.orca.backend.launch.handlers.UserHandler;
import com.orca.backend.server.HTTPInput;
import com.orca.backend.server.InputHandler;
import com.orca.backend.server.LCHashMap;
import com.orca.backend.server.ResponseFile;
import com.orca.backend.server.Utils;
import com.orca.backend.sql.DatabaseConnection;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;

public class OrcascoutHandler implements InputHandler {

    private static final DatabaseConnection connection;

    static {
        String SqlUrl = "jdbc:mysql://" + Prefs.getString("sql_url", "localhost") + ":"
                + Prefs.getInt("sql_port", 2206) + "/"
                + Prefs.getString("sql_database", "orcascout") + "?"
                + Prefs.getString("sql_args");
        connection = new DatabaseConnection(SqlUrl, Prefs.getString("sql_username"), Prefs.getString("sql_password"));
        if (!connection.connect()) {
            System.out.println("Error connecting to database. abort");
            System.exit(1);
        }
    }
    public static final UserHandler userHandler = new UserHandler(connection);
    public static final MatchHandler matchHandler = new MatchHandler(connection);
    public static final PitScoutHandler teamHandler = new PitScoutHandler(connection);
    public static final CompetitionHandler compHandler = new CompetitionHandler(connection);
    private static final LCHashMap<ResponseFile> memCachedFiles = new LCHashMap<>();

    static {
        try {
            Path p = new File(OrcascoutHandler.class.getResource("/frontend/").toURI()).toPath();//TODO CHANGE WHEN JARRING
            Files.walk(p).forEach(n -> {
                if (!n.toFile().isDirectory()) {
                    try {
                        String g = n.toString();
                        g = g.substring(g.indexOf("frontend") + 8).replaceAll("\\\\", "/").toLowerCase();
                        memCachedFiles.put(g, ResponseFile.readFromFile(n));
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

    private ResponseFile getCachedFile(String f) {
        return memCachedFiles.get(f);
    }

    private boolean shouldCloseConnectionErrorCode(int code) {
        return code > 0;
    }

    /**
     *
     * @param in
     * @return return false if the POST/GET request cant be processed
     */
    private boolean handleUser(HTTPInput in, BufferedWriter out) throws IOException {
        if (!in.getPhpArgs().containsKey("method")) {
            sendFile(getCachedFile("/errorFiles/400error.html"), "400 Bad Request", null, out, null);
            return true;
        }

        HashSet<String> cokies = new HashSet<>();
        HashMap<String, String> args = new HashMap<>();
        String token = in.getCookie("AuthToken");
        JSONObj obj;
        int exec = 0;
        switch (in.getPhpArgs().get("method").toLowerCase()) {
            case "login":
                //System.out.println("LL"+in.getActualPostData()[0]);
                obj = new JSONObj(in.getRawPostData());
                obj = userHandler.loginUser(obj);
                if (obj.keySet().contains("token")) {
                    cokies.add("AuthToken=" + obj.getString("token") + "; Expires=" + Utils.getHTTPDate(User.userTimeoutMillis()) + ";");
                    sendFile(null, "204 No Content", null, out, cokies); //maybe change to 205?? but it doesnt work tho...
                    System.out.println("User " + obj.getString("username") + " logged in.");
                } else {
                    cokies.add("AuthToken=deleted; Expires=" + Utils.getHTTPDate(-1) + ";");
                    args.put("X-Error-Code", "" + obj.get("error"));
                    sendFile(getCachedFile("/errorFiles/401error.html"), "401 Unauthorized", args, out, cokies);
                }
                return false;
            case "logout":
                if (token == null || !userHandler.logoutUser(token)) {
                    //Do I need an error code here?
                    sendFile(getCachedFile("/errorFiles/401error.html"), "401 Unauthorized", null, out, null);
                    return false;
                }
                cokies.add("AuthToken=deleted; Expires=" + Utils.getHTTPDate(-1) + ";");
                sendFile(null, "204 No Content", null, out, cokies);
                return false;
            case "approve":
                if (token == null || !userHandler.isLoggedIn(token)) {
                    args.put("X-Error-Code", "" + 1);
                    sendFile(getCachedFile("/errorFiles/401error.html"), "401 Unauthorized", args, out, null);
                    return false;
                }
                obj = new JSONObj(in.getRawPostData());
                exec = userHandler.approveUser(obj, token);
                if (exec == 0) {
                    sendFile(null, "204 No Content", null, out, null);
                } else {
                    args.put("X-Error-Code", "" + exec);
                    sendFile(getCachedFile("/errorFiles/401error.html"), "401 Unauthorized", args, out, null);
                }
                return false;
            case "create":
                obj = new JSONObj(in.getRawPostData());
                exec = userHandler.addNewUser(obj);
                if (exec == 0) {
                    sendFile(null, "204 No Content", null, out, null);
                } else {
                    args.put("X-Error-Code", "" + exec);
                    sendFile(getCachedFile("/errorFiles/401error.html"), "401 Unauthorized", args, out, null);
                }
                return false;
            case "getinfo":
                if (token == null || !userHandler.isLoggedIn(token)) {
                    sendFile(getCachedFile("/errorFiles/401error.html"), "401 Unauthorized", null, out, null);
                    return false;
                }
                obj = userHandler.getUserInfo(token);
                if(obj==null){
                    args.put("X-Error-Code", "1");
                    sendFile(getCachedFile("/errorFiles/401error.html"), "401 Unauthorized", args, out, null);
                    return false;
                }
                sendFile(new ResponseFile(obj.toString(), "text/plain; charset=utf-8"), "200 OK", null, out, null);
                return false;
                /**
                 * get the current pending Users<br>
                 * Used by Admins to accept / deny Users<br>
                 */
            case "getpending":
                if (token == null || !userHandler.isLoggedIn(token)) {
                    args.put("X-Error-Code", "1"); //Not Logged In
                    sendFile(getCachedFile("/errorFiles/401error.html"), "401 Unauthorized", args, out, null);
                    return false;
                }
                obj = userHandler.getPendingUsers();
                if(obj.keySet().contains("error")){
                    args.put("X-Error-Code", obj.get("error")+""); //Not Logged In
                    sendFile(getCachedFile("/errorFiles/401error.html"), "401 Unauthorized", args, out, null);
                    return false;
                }
                sendFile(new ResponseFile(obj.toString(), "text/plain; charset=utf-8"), "200 OK", null, out, null);
                return false;
            case "getmatches":
                if (token == null || !userHandler.isLoggedIn(token)) {
                    args.put("X-Error-Code", "1"); //Not Logged In
                    sendFile(getCachedFile("/errorFiles/401error.html"), "401 Unauthorized", args, out, null);
                    return false;
                }
                obj = userHandler.getMatchesScouted(token);
                if(obj.isErrorJSON()){
                    args.put("X-Error-Code", obj.get("error")+""); 
                    sendFile(getCachedFile("/errorFiles/401error.html"), "401 Unauthorized", args, out, null);
                    return false;
                }
                sendFile(new ResponseFile(obj.toString(), "text/plain; charset=utf-8"), "200 OK", null, out, null);
                return false;
            case "changepassword":
                if (token == null || !userHandler.isLoggedIn(token)) {
                    sendFile(getCachedFile("/errorFiles/401error.html"), "401 Unauthorized", null, out, null);
                    return false;
                }
                obj = new JSONObj(in.getRawPostData());
                if (userHandler.changePassword(token, obj)) {
                    sendFile(null, "204 No Content", null, out, null);
                } else {
                    sendFile(getCachedFile("/errorFiles/401error.html"), "401 Unauthorized", null, out, null);
                }
                return false;
            case "setcomp":
                if (token == null || !userHandler.isLoggedIn(token)) {
                    sendFile(getCachedFile("/errorFiles/401error.html"), "401 Unauthorized", null, out, null);
                    return false;
                }
                obj = new JSONObj(in.getRawPostData());
                exec = userHandler.setCompIDByToken(token, obj.getString("comp_id"));
                if (exec == 0) {
                    sendFile(null, "204 No Content", null, out, null);
                } else {
                    args.put("X-Error-Code", "" + exec);
                    sendFile(getCachedFile("/errorFiles/400error.html"), "400 Bad Request", args, out, null);
                }
                return false;
        }
        sendFile(getCachedFile("/errorFiles/401error.html"), "401 Unauthorized", null, out, null);
        return false;
    }

    public boolean handleUserOptions(HTTPInput in, BufferedWriter out) throws IOException {
        if (!in.getPhpArgs().containsKey("method")) {
            sendFile(getCachedFile("/errorFiles/400error.html"), "400 Bad Request", null, out, null);
            return true;
        }
        HashMap<String, String> args = new HashMap<>();
        switch (in.getPhpArgs().get("method").toLowerCase()) {
            case "login":
            case "changepassword":
            case "logout":
            case "create":
            case "approve":
            case "setcomp":
                args.put("Access-Control-Allow-Methods", "POST");
                sendFile(null, "200 OK", args, out, null);
                return false; //TODO: MAYBE CHANGE CUZ CLOSE CONNECTION?
            case "getinfo":
            case "getmatches":
            case "getpits":
            case "getpending":
                args.put("Access-Control-Allow-Methods", "GET");
                sendFile(null, "200 OK", args, out, null);
                return false;
            default:
                sendFile(memCachedFiles.get("/400error.html"), "400 Bad Request", null, out, null);
                return false;
        }
    }

    private boolean handlePitScout(HTTPInput in, BufferedWriter out) throws IOException {
        if (!in.getPhpArgs().containsKey("method")) {
            sendFile(getCachedFile("/errorFiles/400error.html"), "400 Bad Request", null, out, null);
            return true;
        }
        HashSet<String> cokies = new HashSet<>();
        String token = in.getCookie("AuthToken");
        if (token == null || !userHandler.isLoggedIn(token)) {
            sendFile(getCachedFile("/errorFiles/401error.html"), "401 Unauthorized", null, out, null);
            return false;
        }
        User user = userHandler.getUserByToken(token);
        JSONObj obj;
        HashMap<String, String> args = new HashMap<>();
        int exec;
        switch (in.getPhpArgs().get("method").toLowerCase()) {
            case "create":
                obj = new JSONObj(in.getRawPostData());
                exec = teamHandler.newTeam(obj, user);
                if (exec == 0) {
                    sendFile(null, "204 No Content", null, out, null);
                } else {
                    args.put("X-Error-Code", "" + exec);
                    sendFile(getCachedFile("/errorFiles/401error.html"), "401 Unauthorized", args, out, null);
                }
                return false;
            case "getteams":
                obj = teamHandler.getTeams(user);
                if (obj.keySet().contains("error")) {
                    args.put("X-Error-Code", "" + obj.get("error"));
                    sendFile(getCachedFile("/errorFiles/401error.html"), "401 Unauthorized", args, out, null);
                } else {
                    sendFile(new ResponseFile(obj.toString(), "text/plain; charset=utf-8"), "200 OK", null, out, null);
                }
                return false;
        }
        sendFile(getCachedFile("/errorFiles/401error.html"), "401 Unauthorized", null, out, null);
        return false;
    }

    public boolean handlePitOptions(HTTPInput in, BufferedWriter out) throws IOException {
        if (!in.getPhpArgs().containsKey("method")) {
            sendFile(getCachedFile("/errorFiles/400error.html"), "400 Bad Request", null, out, null);
            return true;
        }
        HashMap<String, String> args = new HashMap<>();
        switch (in.getPhpArgs().get("method").toLowerCase()) {
            case "create":
                args.put("Access-Control-Allow-Methods", "POST");
                sendFile(null, "200 OK", args, out, null);
                return false; //TODO: MAYBE CHANGE CUZ CLOSE CONNECTION?
            case "getteams":
                args.put("Access-Control-Allow-Methods", "GET");
                sendFile(null, "200 OK", args, out, null);
                return false;
            default:
                sendFile(memCachedFiles.get("/400error.html"), "400 Bad Request", null, out, null);
                return false;
        }
    }

    private boolean handleComp(HTTPInput in, BufferedWriter out) throws IOException {
        if (!in.getPhpArgs().containsKey("method")) {
            sendFile(getCachedFile("/errorFiles/400error.html"), "400 Bad Request", null, out, null);
            return true;
        }
        HashSet<String> cokies = new HashSet<>();
        String token = in.getCookie("AuthToken");
        if ((token == null || !userHandler.isLoggedIn(token)) && !in.getPhpArgs().get("method").equalsIgnoreCase("getcomps")) {
            sendFile(getCachedFile("/errorFiles/401error.html"), "401 Unauthorized", null, out, null);
            return false;
        }
        User user = userHandler.getUserByToken(token);
        JSONObj obj;
        HashMap<String, String> args = new HashMap<>();
        int exec;
        switch (in.getPhpArgs().get("method").toLowerCase()) {
            case "getcomps":
                obj = compHandler.getComps();
                exec = obj.optInt("error", 0);
                if (exec != 0) {
                    args.put("X-Error-Code", "" + exec);
                    sendFile(getCachedFile("/errorFiles/401error.html"), "401 Unauthorized", args, out, null);
                } else {
                    sendFile(new ResponseFile(obj.toString(), "text/plain; charset=utf-8"), "200 OK", null, out, null);
                }
                return false;
            case "register":
                obj = new JSONObj(in.getRawPostData());
                exec = compHandler.registerComp(obj, user);
                if (exec != 0) {
                    args.put("X-Error-Code", "" + exec);
                    sendFile(getCachedFile("/errorFiles/401error.html"), "401 Unauthorized", args, out, null);
                } else {
                    sendFile(new ResponseFile(obj.toString(), "text/plain; charset=utf-8"), "200 OK", null, out, null);
                }
        }
        sendFile(getCachedFile("/errorFiles/401error.html"), "401 Unauthorized", null, out, null);
        return false;
    }

    public boolean handleCompOptions(HTTPInput in, BufferedWriter out) throws IOException {
        if (!in.getPhpArgs().containsKey("method")) {
            sendFile(getCachedFile("/errorFiles/400error.html"), "400 Bad Request", null, out, null);
            return true;
        }
        HashMap<String, String> args = new HashMap<>();
        switch (in.getPhpArgs().get("method").toLowerCase()) {
            case "register":
                args.put("Access-Control-Allow-Methods", "POST");
                sendFile(null, "200 OK", args, out, null);
                return false; //TODO: MAYBE CHANGE CUZ CLOSE CONNECTION?
            case "getcomps":
                args.put("Access-Control-Allow-Methods", "GET");
                sendFile(null, "200 OK", args, out, null);
                return false;
            default:
                sendFile(memCachedFiles.get("/400error.html"), "400 Bad Request", null, out, null);
                return false;
        }
    }


    private boolean handleMatch(HTTPInput in, BufferedWriter out) throws IOException {
        if (!in.getPhpArgs().containsKey("method")) {
            sendFile(getCachedFile("/errorFiles/400error.html"), "400 Bad Request", null, out, null);
            return true;
        }
        HashSet<String> cokies = new HashSet<>();
        String token = in.getCookie("AuthToken");
        if ((token == null || !userHandler.isLoggedIn(token)) && !in.getPhpArgs().get("method").equalsIgnoreCase("getcomps")) {
            sendFile(getCachedFile("/errorFiles/401error.html"), "401 Unauthorized", null, out, null);
            return false;
        }
        User user = userHandler.getUserByToken(token);
        JSONObj obj;
        HashMap<String, String> args = new HashMap<>();
        int exec;
        switch (in.getPhpArgs().get("method").toLowerCase()) {
        }
        sendFile(getCachedFile("/errorFiles/401error.html"), "401 Unauthorized", null, out, null);
        return false;
    }

    public boolean handleMatchOptions(HTTPInput in, BufferedWriter out) throws IOException {
        if (!in.getPhpArgs().containsKey("method")) {
            sendFile(getCachedFile("/errorFiles/400error.html"), "400 Bad Request", null, out, null);
            return true;
        }
        HashMap<String, String> args = new HashMap<>();
        switch (in.getPhpArgs().get("method").toLowerCase()) {
            case "CHANGE":
                args.put("Access-Control-Allow-Methods", "POST");
                sendFile(null, "200 OK", args, out, null);
                return false; //TODO: MAYBE CHANGE CUZ CLOSE CONNECTION?
            case "CHANGE1":
                args.put("Access-Control-Allow-Methods", "GET");
                sendFile(null, "200 OK", args, out, null);
                return false;
            default:
                sendFile(memCachedFiles.get("/400error.html"), "400 Bad Request", null, out, null);
                return false;
        }
    }

    @Override
    public boolean handleRequest(HTTPInput in, BufferedWriter out) {
        try {
            ResponseFile sendFile;
            String respMessage = "200 OK";
            System.out.println("Requested File: " + in.getRequestedFile());
            System.out.println("Error Code: " + in.getErrorCode());
            if (in.getErrorCode() == 4) {
                respMessage = "400 Bad Request";
                sendFile = getCachedFile("/errorFiles/400error.html");
            } else if (in.getErrorCode() >= 1 && in.getErrorCode() <= 3) {
                respMessage = "413 Payload Too Large";
                sendFile = getCachedFile("/errorfiles/413error.html");
            } else if (in.getErrorCode() == 5) {
                respMessage = "500 Internal Server Error";
                sendFile = getCachedFile("/errorFiles/500error.html");
            } else if (in.getErrorCode() == 6) {
                respMessage = "411 Length Required";
                sendFile = getCachedFile("/errorFiles/411error.html");
            } else {
                if (in.getRequestedFile().toLowerCase().startsWith("/errorfiles")) {
                    sendFile = getCachedFile("/errorFiles/404error.html");
                    respMessage = "404 Not Found";
                } else if (in.getRequestedFile().equalsIgnoreCase("/submitUser")) {
                    if (in.get("method").equalsIgnoreCase("OPTIONS")) {
                        return handleUserOptions(in, out);
                    }
                    return handleUser(in, out);
                } else if (in.getRequestedFile().equalsIgnoreCase("/submitComp")) {
                    if (in.get("method").equalsIgnoreCase("OPTIONS")) {
                        return handleCompOptions(in, out);
                    }
                    return handleComp(in, out);
                } else if (in.getRequestedFile().equalsIgnoreCase("/submitPit")) {
                    if (in.get("method").equalsIgnoreCase("OPTIONS")) {
                        return handlePitOptions(in, out);
                    }
                    return handlePitScout(in, out);
                } else if (in.getRequestedFile().equalsIgnoreCase("/submitMatch")) {
                    if (in.get("method").equalsIgnoreCase("OPTIONS")) {
                        return handleMatchOptions(in, out); //TODO Implement
                    }
                    return handleMatch(in, out);//TODO Implement
                } else if (!memCachedFiles.containsKey(in.getRequestedFile())) {
                    respMessage = "404 Not Found";
                    System.out.println("File not Found");
                    sendFile = getCachedFile("/errorFiles/404error.html");
                } else {
                    sendFile = getCachedFile(in.getRequestedFile());
                }
            }

            System.out.println("SENDING FILE");
            sendFile(sendFile, respMessage, null, out, null);
            System.out.println("File Sent");
            return in.getOrDefault("Connection", "keep-alive").equals("close") || shouldCloseConnectionErrorCode(in.getErrorCode());
        } catch (Exception e) {
            e.printStackTrace(System.out);
            try {
                sendFile(getCachedFile("/errorFiles/500error.html"), "500 Internal Server Error", null, out, null);
            } catch (IOException g) {
            }
            return true;
        }
    }
}
