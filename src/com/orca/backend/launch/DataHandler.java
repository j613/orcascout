package com.orca.backend.launch;

import com.orca.backend.server.ResponseFile;
import com.orca.backend.server.Utils;
import com.orca.backend.sql.DatabaseConnection;
import java.awt.BorderLayout;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DataHandler {
    private final HashMap<String, JSONObj> templates = new HashMap<String, JSONObj>() {
        @Override
        public JSONObj get(Object key) {
            if (!(key instanceof String)) {
                return null;
            }
            return super.get(((String) key).replaceAll("\\\\", "/").toLowerCase().trim());
        }

    };
    //TODO: Implement SSL
    private final DatabaseConnection connection
            = new DatabaseConnection("jdbc:mysql://localhost/orcascout?useSSL=false", "root", "NONO");

    {
        try {
            Path p = new File(DataHandler.class.getResource("/com/orca/backend/templates/").toURI()).toPath();
            Files.walk(p).forEach(n -> {
                if (!n.toFile().isDirectory()) {
                    try {
                        String g = n.toString().replaceAll("\\\\", "/");
                        g = g.substring(g.indexOf("/com/orca/backend/templates/")
                                +"/com/orca/backend/templates/".length()).replaceAll("\\\\", "/").toLowerCase();
                        templates.put(g.substring(0,g.length()-5), new JSONObj(new String(Files.readAllBytes(n))));
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

    private boolean checkTemplate(String temp, JSONObj obj) {
        return templates.get(temp).similar(obj);
    }

    public boolean addNewUser(JSONObj g) {
        try {
            JSONObj obj =g;// new JSONObj(g);
            if (!checkTemplate("UserCreateTemplate", obj)) {
                return false;
            }
            PreparedStatement checkUserExists = connection.prepareStatement("select USERNAME from USERS where USERNAME = ?");
            checkUserExists.setString(1, obj.getString("username"));
            ResultSet rset = checkUserExists.executeQuery();
            if(rset.next()){
                return false;
            }
            String passhash = Utils.hashPassword(obj.getString("username"),obj.getString("password"));
            PreparedStatement newUser = connection.prepareStatement(
                    "insert into USERS(USERNAME, FIRSTNAME, LASTNAME, PASSWORD_HASH, USER_LEVEL, PENDING)"
                            + " values (?, ?, ?, ?, ?, ?)");
            newUser.setString(1, obj.getString("username"));
            newUser.setString(2, obj.getString("firstname"));
            newUser.setString(3, obj.getString("lastname"));
            newUser.setString(4, passhash);
            newUser.setString(5, "limited");
            newUser.setInt(6, 1);
            return newUser.execute();
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }

/*    public boolean approveUser() {

    }*/
    /**
     * TODO: CHECK USERNAME LOGINS, GIVE TOKEN
     * @param args 
     */
    public static void main(String ... args){
        DataHandler g = new DataHandler();
        System.out.println(g.addNewUser(g.templates.get("TeamTemplate")));
    }
}
