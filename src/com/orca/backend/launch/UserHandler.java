package com.orca.backend.launch;

import com.orca.backend.server.LCHashMap;
import com.orca.backend.server.ResponseFile;
import com.orca.backend.server.Utils;
import com.orca.backend.sql.DatabaseConnection;
import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;
import java.awt.BorderLayout;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UserHandler {

    private final LCHashMap<JSONObj> templates = new LCHashMap<>();
    private final ArrayList<User> users = new ArrayList<>();
    //TODO: Implement SSL
    private final DatabaseConnection connection;

    public UserHandler(DatabaseConnection c) {
        connection = c;
        try {
            Path p = new File(UserHandler.class.getResource("/com/orca/backend/templates/").toURI()).toPath();
            Files.walk(p).forEach(n -> {
                if (!n.toFile().isDirectory()) {
                    try {
                        String g = n.getFileName().toString().toLowerCase();
                        templates.put(g.split("\\.")[0], new JSONObj(new String(Files.readAllBytes(n))));
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
        if (!connection.connect()) {
            System.out.println("Error connecting to database. abort");
            System.exit(1);
        }
    }

    public boolean isValidUserType(String g) {
        return g.equalsIgnoreCase("admin") || g.equalsIgnoreCase("limited") || g.equalsIgnoreCase("regular");
    }

    public boolean userExists(String username) {
        try {
            PreparedStatement checkUser = connection.prepareStatement("select * from USERS where USERNAME = ?");
            checkUser.setString(1, username);
            ResultSet rs = checkUser.executeQuery();
            return rs.next();
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public boolean userExists(int userID) {
        try {
            PreparedStatement checkUser = connection.prepareStatement("select * from USERS where ID = ?");
            checkUser.setInt(1, userID);
            ResultSet rs = checkUser.executeQuery();
            return rs.next();
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    private boolean checkTemplate(String temp, JSONObj obj) {
        return templates
                .get(temp)
                .similar(obj);
    }

    /**
     * Adds a new user with limited access, in pending stage (have to be
     * approved by admin to access website)
     *
     * @param obj the JSON object that contains the necessary information. This
     * object should match 'UserCreateTemplate'
     * @return true if the operation was a success
     */
    public boolean addNewUser(JSONObj obj) {
        try {
            if (!checkTemplate("UserCreateTemplate", obj)) {
                return false;
            }
            if (userExists(obj.getString("username"))) {
                return false;
            }
            if (obj.getString("password").length() > 128 || obj.getString("password").length() < 8) {
                return false;
            }
            String passhash = Utils.hashPassword(obj.getString("username"), obj.getString("password"));
            passhash = Base64.encode(passhash.getBytes());
            PreparedStatement newUser = connection.prepareStatement(
                    "insert into USERS(USERNAME, FIRSTNAME, LASTNAME, PASSWORD_HASH, USER_LEVEL, PENDING)"
                    + " values (?, ?, ?, ?, ?, ?)");
            newUser.setString(1, obj.getString("username"));
            newUser.setString(2, obj.getString("firstname"));
            newUser.setString(3, obj.getString("lastname"));
            newUser.setString(4, passhash);
            newUser.setString(5, "limited");
            newUser.setInt(6, 1);
            return !newUser.execute();
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public boolean logoutUser(String token) {
        User u = users.stream()
                .filter(n -> n.getToken().equals(token))
                .findAny()
                .orElse(null);
        if (u != null) {
            users.remove(u);
            return true;
        }
        return false;
    }

    public boolean approveUser(JSONObj obj, String token) {
        if (!checkTemplate("UserAcceptTemplate", obj)) {
            return false;
        }
        try {
            if (!isValidUserType(obj.getString("userlevel")) && !obj.getString("userlevel").equalsIgnoreCase("delete")) {
                return false;
            }
            if (!userExists(obj.getString("username"))) {
                return false;
            }
            if(!users.stream().anyMatch(n->n.getToken().equals(token)&&n.getUsername().equals(obj.getString("username")))){
                return false;
            }
            PreparedStatement exec;
            if (obj.getString("userlevel").equalsIgnoreCase("delete")) {
                exec = connection.prepareStatement("delete from USERS where USERNAME = ?");
                exec.setString(1, obj.getString("username"));
            } else {
                exec = connection.prepareStatement("update USERS set USER_LEVEL = ?, PENDING = false where USERNAME = ?");
                exec.setString(1, obj.getString("userlevel").toLowerCase());
                exec.setString(2, obj.getString("username"));
            }
            return !exec.execute();
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    /**
     * given a JSON matching UserLoginTemplate, returns a token for user login
     *
     * @param obj the JSON object containing the data
     * @return a token that is given to the client to keep a session
     */
    public String loginUser(JSONObj obj) {
        if (!checkTemplate("UserLoginTemplate", obj)) {
            return null;
        }
        try {
            if (!userExists(obj.getString("username"))) {
                return null;
            }
            if (users.stream().anyMatch(n -> n.getUsername().equalsIgnoreCase(obj.getString("username")))) {
                return null;
            }
            PreparedStatement exec
                    = connection.prepareStatement("select * from USERS where USERNAME = ? ");
            String passhash = Utils.hashPassword(obj.getString("username"), obj.getString("password"));
            passhash = Base64.encode(passhash.getBytes());
            exec.setString(1, obj.getString("username"));
            ResultSet rs = exec.executeQuery();
            if (rs.next() && rs.getString("PASSWORD_HASH").equals(passhash)) {
                String token;
                while (true) {
                    token = Utils.generateToken(256);
                    final String why = token;
                    if (users.stream().noneMatch(n -> n.getToken().equals(why))) {
                        break;
                    }
                }
                users.add(new User(obj.getString("username"), token));
                return token;
            } else {
                return null;
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    //Adds a user based off the JSON in TeamTemplate
    //Only for temp debugging
    public static void main(String... args) {
        UserHandler g = new UserHandler(new DatabaseConnection("jdbc:mysql://localhost/orcascout?useSSL=false", "root", "NONO"));
        System.out.println(g.loginUser(new JSONObj(g.templates.get("TestInsertTemplate").get("loginuser"))));
        System.out.println(g.users);
    }
}
