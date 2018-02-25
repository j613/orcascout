package com.orca.backend.launch;

import com.orca.backend.server.HTTPInput;
import com.orca.backend.server.InputHandler;
import com.orca.backend.server.LCHashMap;
import com.orca.backend.server.PostData;
import com.orca.backend.server.ResponseFile;
import com.orca.backend.server.Utils;
import com.orca.backend.sql.DatabaseConnection;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

public class OrcascoutHandler implements InputHandler {

    private final DatabaseConnection connection = new DatabaseConnection("jdbc:mysql://localhost/orcascout?useSSL=false", "root", "NONO");
    private final UserHandler userHandler = new UserHandler(connection);
    private static final LCHashMap<ResponseFile> memCachedFiles = new LCHashMap<>();

    static {
        try {
            Path p = new File(OrcascoutHandler.class.getResource("/frontend/").toURI()).toPath();
            Files.walk(p).forEach(n -> {
                if (!n.toFile().isDirectory()) {
                    try {
                        String g = n.toString();
                        g = g.substring(g.indexOf("frontend") + 8).replaceAll("\\\\", "/").toLowerCase();
                        memCachedFiles.put(g, ResponseFile.readFromFile(n));
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
            });
        } catch (IOException e) {
            System.err.println("Error reading files from disk. abort");
            e.printStackTrace();
        } catch (URISyntaxException ex) {
            ex.printStackTrace();
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
        String token = in.getCookie("AuthToken");
        JSONObj obj;
        switch (in.getPhpArgs().get("method").toLowerCase()) {
            case "login":
                //System.out.println("LL"+in.getActualPostData()[0]);
                obj = new JSONObj(in.getActualPostData()[0].getPostData());
                String t = userHandler.loginUser(obj);
                if (t != null) {
                    cokies.add("AuthToken=" + t + "; Expires=" + Utils.getHTTPDate(259200000) + ";");
                    sendFile(null, "204 No Content", null, out, cokies); //maybe change to 205?? but it doesnt work tho...
                    System.out.println("User " + obj.getString("username") + " logged in.");
                } else {
                    cokies.add("AuthToken=deleted; Expires=" + Utils.getHTTPDate(-1) + ";");
                    sendFile(getCachedFile("/errorFiles/403error.html"), "403 Forbidden", null, out, cokies);
                }
                return false;
            case "logout":
                if (token == null || !userHandler.logoutUser(token)) {
                    sendFile(getCachedFile("/errorFiles/403error.html"), "403 Forbidden", null, out, null);
                    return false;
                }
                cokies.add("AuthToken=deleted; Expires=" + Utils.getHTTPDate(-1) + ";");
                sendFile(null, "204 No Content", null, out, cokies);
                return false;
            case "approve":
                if (token == null || !userHandler.isLoggedIn(token)) {
                    sendFile(getCachedFile("/errorFiles/403error.html"), "403 Forbidden", null, out, null);
                    return false;
                }
                obj = new JSONObj(in.getActualPostData()[0].getPostData());
                if (userHandler.approveUser(obj, token)) {
                    sendFile(null, "204 No Content", null, out, null);
                } else {
                    sendFile(getCachedFile("/errorFiles/403error.html"), "403 Forbidden", null, out, null);
                }
                return false;
            case "create":
                obj = new JSONObj(in.getActualPostData()[0].getPostData());
                if (userHandler.addNewUser(obj)) {
                    sendFile(null, "204 No Content", null, out, null);
                } else {
                    sendFile(getCachedFile("/errorFiles/403error.html"), "403 Forbidden", null, out, null);
                }
                return false;
            case "getinfo":
                if (token == null || !userHandler.isLoggedIn(token)) {
                    sendFile(getCachedFile("/errorFiles/403error.html"), "403 Forbidden", null, out, null);
                    return false;
                }
                obj = userHandler.getUserInfo(token);
                sendFile(new ResponseFile(obj.toString(), "text/plain; charset=utf-8"), "200 OK", null, out, null);
                return false;
            case "getpending":
                if (token == null || !userHandler.isLoggedIn(token)) {
                    sendFile(getCachedFile("/errorFiles/403error.html"), "403 Forbidden", null, out, null);
                    return false;
                }
                obj = userHandler.getPendingUsers();
                sendFile(new ResponseFile(obj.toString(), "text/plain; charset=utf-8"), "200 OK", null, out, null);
                return false;
            case "getmatches":
                if(token==null || !userHandler.isLoggedIn(token)){
                    sendFile(getCachedFile("/errorFiles/403error.html"), "403 Forbidden", null, out, null);
                    return false;
                }
                obj = userHandler.getMatchesScouted(token);
                sendFile(new ResponseFile(obj.toString(), "text/plain; charset=utf-8"), "200 OK", null, out, null);
                return false;
        }
        sendFile(getCachedFile("/errorFiles/403error.html"), "403 Forbidden", null, out, null);
        return false;
    }
    private boolean handleTeam(HTTPInput in, BufferedWriter out) throws IOException{
         if (!in.getPhpArgs().containsKey("method")) {
            sendFile(getCachedFile("/errorFiles/400error.html"), "400 Bad Request", null, out, null);
            return true;
        }
        HashSet<String> cokies = new HashSet<>();
        String token = in.getCookie("AuthToken");
        JSONObj obj;
        switch (in.getPhpArgs().get("method").toLowerCase()) {
            case "create"
        }
        sendFile(getCachedFile("/errorFiles/403error.html"), "403 Forbidden", null, out, null);
        return false;
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
                    return handleUser(in, out);
                } else if (in.getRequestedFile().equalsIgnoreCase("/submitTeam")) {
                    return handleTeam(in, out);
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
