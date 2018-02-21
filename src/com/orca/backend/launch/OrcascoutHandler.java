package com.orca.backend.launch;

import com.orca.backend.server.HTTPInput;
import com.orca.backend.server.InputHandler;
import com.orca.backend.server.LCHashMap;
import com.orca.backend.server.PostData;
import com.orca.backend.server.ResponseFile;
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

    private ResponseFile getCachedFile(String f) {
        return memCachedFiles.get(f);
    }

    private boolean shouldCloseConnectionErrorCode(int code) {
        return code > 0;
    }

    /**
     *
     * @param in
     * @return return false if the POST request cant be processed
     */
    private String handleUserSubmit(HTTPInput in) {
        if (!in.getPhpArgs().containsKey("method")) {
            return null;
        }
        String token = in.getCookie("AuthToken");
        JSONObj obj;
        switch (in.getPhpArgs().get("method").toLowerCase()) {
            case "login":
                obj = new JSONObj(in.getActualPostData()[0]);
                return userHandler.loginUser(obj);
            case "logout":
                if (token == null) {
                    return null;
                }
                return userHandler.logoutUser(token) ? "" : null;
            case "approve":
                if (token == null) {
                    return null;
                }
                obj = new JSONObj(in.getActualPostData()[0]);
                return userHandler.approveUser(obj, token) ? "" : null;
            case "create":
                obj = new JSONObj(in.getActualPostData()[0]);
                return userHandler.addNewUser(obj) ? "" : null;
        }
        return null;
    }

    @Override
    public boolean handleRequest(HTTPInput in, BufferedWriter out) throws IOException {
        ResponseFile sendFile;
        String respMessage = "200 OK";
        System.out.println("requested File: " + in.getRequestedFile());
        System.out.println("Error Code: " + in.getErrorCode());
        HashSet<String> cookies = new HashSet<>();
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
            } else if (in.getRequestedFile().toLowerCase().equalsIgnoreCase("/submitUser")) {
                String ret = handleUserSubmit(in);
                if(ret!=null && !ret.isEmpty()){
                    cookies.add("AuthToken=")
                }
                respMessage = ret == null ? "201 Created" : "400 Bad Request";
                sendFile = null;
            } else if (!memCachedFiles.containsKey(in.getRequestedFile())) {
                respMessage = "404 Not Found";
                System.out.println("File not Found");
                sendFile = getCachedFile("/errorFiles/404error.html");
            } else {
                sendFile = getCachedFile(in.getRequestedFile());
            }
        }

        System.out.println("SENDING FILE");
        sendFile(sendFile, respMessage, null, out, cookies);
        System.out.println("File Sent");
        return in.getOrDefault("Connection", "keep-alive").equals("close") || shouldCloseConnectionErrorCode(in.getErrorCode());
    }
    /*
	 * @Override public ResponseFile getResponseFile(String fileName) throws
	 * IOException { return null; }
     */
}
