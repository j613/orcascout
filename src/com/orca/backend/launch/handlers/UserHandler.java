package com.orca.backend.launch.handlers;

import com.orca.backend.launch.JSONObj;
import com.orca.backend.launch.OrcascoutHandler;
import com.orca.backend.launch.User;
import com.orca.backend.launch.User.UserLevel;
import com.orca.backend.server.Utils;
import com.orca.backend.sql.DatabaseConnection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import org.json.JSONArray;

public class UserHandler {

    private final ArrayList<User> users = new ArrayList<>();
    private final DatabaseConnection connection;

    public UserHandler(DatabaseConnection c) {
        connection = c;
    }

    void timeoutUsers() {
        users.removeIf(n -> n.shouldLogout());
    }

    /**
     * returns a currently logged in user based off of the token
     *
     * @param token the token
     * @return the user
     */
    public User getUserByToken(String token) {
        timeoutUsers();
        return users.stream().filter(n -> n.getToken().equals(token)).findAny().orElse(null);
    }

    public static JSONObj userToJSON(ResultSet rs, boolean userLevel, boolean passhash) throws SQLException {
        JSONObj ret = new JSONObj();
        ret.put("username", rs.getString("USERNAME"));
        ret.put("firstname", rs.getString("FIRSTNAME"));
        ret.put("lastname", rs.getString("LASTNAME"));
        if (userLevel) {
            ret.put("userlevel", rs.getString("USERLEVEL"));
        }
        if (passhash) {
            ret.put("passhash", rs.getString("PASSHASH"));
        }
        return ret;
    }

    public JSONObj getPendingUsers() {
        try {
            JSONObj ret = new JSONObj();
            ResultSet rs = connection.executeQuery("select USERNAME, FIRSTNAME, LASTNAME from USERS where PENDING = 1;");
            while (rs.next()) {
                ret.append("users", userToJSON(rs, false, false));
            }
            return ret;
        } catch (SQLException ex) {
            ex.printStackTrace(System.out);
            return null;
        }
    }

    /**
     * sets the current Competition / Regional ID of a logged in user
     *
     * @param token the token of the logged in user
     * @param compid the competition id to set for the user
     * @return error code Error codes: 0: No Error 1: Comp ID doesn't exist 2:
     * SQL Error 3: user doesn't exist
     */
    public int setCompIDByToken(String token, String compid) {
        int exec = OrcascoutHandler.compHandler.compExists(compid);
        if (exec == 0) {
            User u = getUserByToken(token);
            if (u == null) {
                return 3;
            }
            u.setCurrentRegionalId(compid);
            return 0;
        } else {
            return exec;
        }
    }

    public JSONObj getMatchesScouted(String token) {
        try {
            JSONObj ret = new JSONObj();
            ret.put("matches", new JSONArray());
            User u = getUserByToken(token);
            PreparedStatement ps = connection.prepareStatement("select MATCH_NUMBER from MATCHES where SUBMIT_BY = ?");
            ps.setInt(1, u.getID());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                ret.append("matches", MatchHandler.matchToJSON(rs, false, false));
            }
            return ret;
        } catch (SQLException ex) {
            ex.printStackTrace(System.out);
            return null;
        }
    }

    public JSONObj getUserInfo(String token) {
        User us = getUserByToken(token);
        if (us == null) {
            return null;
        }
        JSONObj tmp = new JSONObj();
        tmp.put("username", us.getUsername());
        tmp.put("firstname", us.getFirstname());
        tmp.put("lastname", us.getLastname());
        tmp.put("level", us.getUserLevel());
        return tmp;
    }

    public boolean isValidUserType(String g) {
        return g.equalsIgnoreCase("admin") || g.equalsIgnoreCase("limited") || g.equalsIgnoreCase("regular");
    }

    public boolean isLoggedIn(String token) {
        System.out.println("LL" + users);
        return this.getUserByToken(token) != null;
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
            if (!JSONObj.checkTemplate("UserCreateTemplate", obj)) {
                return false;
            }
            if (userExists(obj.getString("username"))) {
                return false;
            }
            if (obj.getString("password").length() > 128 || obj.getString("password").length() < 8) {
                return false;
            }
            final String passwordSalt = Utils.genSalt();
            String passhash = Utils.hashPassword(passwordSalt, obj.getString("password"));
            PreparedStatement newUser = connection.prepareStatement(
                    "insert into USERS(USERNAME, FIRSTNAME, LASTNAME, PASSWORD_HASH, USER_LEVEL, PENDING, PASSWORD_SALT)"
                    + " values (?, ?, ?, ?, ?, ?, ?)");
            newUser.setString(1, obj.getString("username"));
            newUser.setString(2, obj.getString("firstname"));
            newUser.setString(3, obj.getString("lastname"));
            newUser.setString(4, passhash);
            newUser.setString(5, "limited");
            newUser.setInt(6, 1);
            newUser.setString(7, passwordSalt);
            return !newUser.execute();
        } catch (SQLException ex) {
            ex.printStackTrace(System.out);
            return false;
        }
    }

    public boolean logoutUser(String token) {
        User u = getUserByToken(token);
        if (u != null) {
            users.remove(u);
            return true;
        }
        return false;
    }

    public boolean approveUser(JSONObj obj, String token) {
        if (!JSONObj.checkTemplate("UserAcceptTemplate", obj)) {
            return false;
        }
        try {
            if (!isValidUserType(obj.getString("userlevel")) && !obj.getString("userlevel").equalsIgnoreCase("delete")) {
                return false;
            }
            if (!userExists(obj.getString("username")) || getUserByToken(token) == null) {
                return false;
            }
            if (getUserByToken(token).getUserLevel() != UserLevel.ADMIN) {
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
            ex.printStackTrace(System.out);
            return false;
        }
    }

    public boolean changePassword(String token, JSONObj obj) {
        try {
            if (!JSONObj.checkTemplate("UserChangePassTemplate", obj)) {
                return false;
            }
            User u = getUserByToken(token);
            if (u == null) {
                return false;
            }
            PreparedStatement ps = connection.prepareStatement("select PASSWORD_HASH, "
                    + "PASSWORD_SALT from USERS where USERNAME = ?");
            ps.setString(1, u.getUsername());
            ResultSet rs = ps.executeQuery();
            if (rs.next() && rs.getString("PASSWORD_HASH").equals(
                    Utils.hashPassword(rs.getString("PASSWORD_SALT"), obj.getString("oldpassword")))) {
                ps = connection.prepareStatement("update USERS set PASSWORD_HASH = ?, PASSWORD_SALT = ? where USERNAME = ?");
                final String passwordSalt = Utils.genSalt();
                ps.setString(1, Utils.hashPassword(passwordSalt, obj.getString("newpassword")));
                ps.setString(2, passwordSalt);
                ps.setString(3, u.getUsername());
                ps.execute();
                return true;
            } else {
                return false;
            }
        } catch (SQLException ex) {
            ex.printStackTrace(System.out);
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
        if (!JSONObj.checkTemplate("UserLoginTemplate", obj)) {
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
            exec.setString(1, obj.getString("username"));
            ResultSet rs = exec.executeQuery();
            if (!rs.next()) {
                return null;
            }
            String passhash = Utils.hashPassword(rs.getString("PASSWORD_SALT"), obj.getString("password"));
            if (rs.getString("PASSWORD_HASH").equals(passhash)) {
                String token;
                while (true) {
                    token = Utils.generateToken(256);
                    final String why = token;
                    if (users.stream().noneMatch(n -> n.getToken().equals(why))) {
                        break;
                    }
                }
                users.add(new User(rs.getInt("ID"), obj.getString("username"), token,
                        rs.getString("USER_LEVEL"), rs.getString("FIRSTNAME"), rs.getString("LASTNAME")));
                return token;
            } else {
                return null;
            }
        } catch (SQLException ex) {
            ex.printStackTrace(System.out);
            return null;
        }
    }

    //Only for temp debugging
    public static void main(String... args) throws SQLException {
        UserHandler g = new UserHandler(new DatabaseConnection("jdbc:mysql://localhost/orcascout?useSSL=false", "root", "NONO"));
        g.connection.connect();

    }
}
