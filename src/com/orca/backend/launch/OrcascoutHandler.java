package com.orca.backend.launch;

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

    final DatabaseConnection connection = new DatabaseConnection("jdbc:mysql://localhost/orcascout?useSSL=false", "root", "NONO");
    final UserHandler userHandler = new UserHandler(connection);
    final MatchHandler matchHandler = new MatchHandler(connection);
    final PitScoutHandler teamHandler = new PitScoutHandler(connection);
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

    {
        if (!connection.connect()) {
            System.out.println("Error connecting to database. abort");
            System.exit(1);
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
                String t = userHandler.loginUser(obj);
                if (t != null) {
                    cokies.add("AuthToken=" + t + "; Expires=" + Utils.getHTTPDate(259200000) + ";");
                    sendFile(null, "204 No Content", null, out, cokies); //maybe change to 205?? but it doesnt work tho...
                    System.out.println("User " + obj.getString("username") + " logged in.");
                } else {
                    cokies.add("AuthToken=deleted; Expires=" + Utils.getHTTPDate(-1) + ";");
                    sendFile(getCachedFile("/errorFiles/401error.html"), "401 Unauthorized", null, out, cokies);
                }
                return false;
            case "logout":
                if (token == null || !userHandler.logoutUser(token)) {
                    sendFile(getCachedFile("/errorFiles/401error.html"), "401 Unauthorized", null, out, null);
                    return false;
                }
                cokies.add("AuthToken=deleted; Expires=" + Utils.getHTTPDate(-1) + ";");
                sendFile(null, "204 No Content", null, out, cokies);
                return false;
            case "approve":
                if (token == null || !userHandler.isLoggedIn(token)) {
                    System.out.println("Not Logged In");
                    sendFile(getCachedFile("/errorFiles/401error.html"), "401 Unauthorized", null, out, null);
                    return false;
                }
                obj = new JSONObj(in.getRawPostData());
                if (userHandler.approveUser(obj, token)) {
                    sendFile(null, "204 No Content", null, out, null);
                } else {
                    sendFile(getCachedFile("/errorFiles/401error.html"), "401 Unauthorized", null, out, null);
                }
                return false;
            case "create":
                obj = new JSONObj(in.getRawPostData());
                if (userHandler.addNewUser(obj)) {
                    sendFile(null, "204 No Content", null, out, null);
                } else {
                    sendFile(getCachedFile("/errorFiles/401error.html"), "401 Unauthorized", null, out, null);
                }
                return false;
            case "getinfo":
                if (token == null || !userHandler.isLoggedIn(token)) {
                    sendFile(getCachedFile("/errorFiles/401error.html"), "401 Unauthorized", null, out, null);
                    return false;
                }
                obj = userHandler.getUserInfo(token);
                sendFile(new ResponseFile(obj.toString(), "text/plain; charset=utf-8"), "200 OK", null, out, null);
                return false;
            case "getpending":
                if (token == null || !userHandler.isLoggedIn(token)) {
                    sendFile(getCachedFile("/errorFiles/401error.html"), "401 Unauthorized", null, out, null);
                    return false;
                }
                obj = userHandler.getPendingUsers();
                sendFile(new ResponseFile(obj.toString(), "text/plain; charset=utf-8"), "200 OK", null, out, null);
                return false;
            case "getmatches":
                if (token == null || !userHandler.isLoggedIn(token)) {
                    sendFile(getCachedFile("/errorFiles/401error.html"), "401 Unauthorized", null, out, null);
                    return false;
                }
                obj = userHandler.getMatchesScouted(token);
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
        args.put("Access-Control-Allow-Origin", "*");
        args.put("Access-Control-Allow-Headers", "*");
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
        String execs;
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
                execs = teamHandler.getTeams();
                if (execs.matches("\\d+")) {
                    args.put("X-Error-Code", execs);
                    sendFile(getCachedFile("/errorFiles/401error.html"), "401 Unauthorized", args, out, null);
                } else {
                    sendFile(new ResponseFile(execs, "text/plain; charset=utf-8"), "204 No Content", null, out, null);
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
        args.put("Access-Control-Allow-Origin", "*");
        args.put("Access-Control-Allow-Headers", "*");
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
                } else if (in.getRequestedFile().equalsIgnoreCase("/submitPit")) {
                    if (in.get("method").equalsIgnoreCase("OPTIONS")) {
                        return handlePitOptions(in, out);
                    }
                    return handlePitScout(in, out);
                } else if (in.getRequestedFile().equalsIgnoreCase("/submitMatch")) {
                    if (in.get("method").equalsIgnoreCase("OPTIONS")) {
                        //return handleMatchOptions(in, out); //TODO Implement
                    }
                    //return handleMatchScout(in, out);//TODO Implement
                    sendFile(getCachedFile("/errorFiles/404.html"), "404 Not Found", null, out, null);
                    return false;
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
