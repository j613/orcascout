package com.orca.backend.launch.handlers;

import com.orca.backend.launch.OrcascoutHandler;
import com.orca.backend.launch.User;
import com.orca.backend.launch.User.UserLevel;
import com.orca.backend.server.Utils;
import com.orca.backend.sql.DatabaseConnection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Optional;
import org.json.JSONArray;
import org.json.JSONObject;

public class UserHandler {

    private final ArrayList<User> users = new ArrayList<>();
    private final DatabaseConnection connection;

    public UserHandler(DatabaseConnection c) {
        connection = c;
    }

    private void timeoutUsers() {
        users.removeIf(n -> n.shouldLogout());
    }

    /**
     * checks to see if the password given matches the Password Requirements in
     * the Prefs file
     *
     * @param password the password to check
     * @return true if the password matches the Requirements
     */
    public boolean passwordMatchesCustomReqs(String password) {
        return true;//just for now
    }

    public User getUserByID(int id) throws SQLException {
        Optional<User> u = users.stream().filter(n -> n.getID() == id).findAny();
        if (u.isPresent()) {
            return u.get();
        }
        PreparedStatement ps = connection.prepareStatement("select * from USERS where ID = ?");
        ps.setInt(1, id);
        ResultSet rs = ps.executeQuery();
        if (!rs.next()) {
            return null;
        }
        return new User(rs.getInt("ID"), rs.getString("username"), null,
                rs.getString("USER_LEVEL"), rs.getString("FIRSTNAME"), rs.getString("LASTNAME"));
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

    /**
     * converts a SQL Result to a JSON Object containing the User information
     *
     * @param rs the SQL ResultSet that contains the data
     * @param userLevel if true, include the User Level in the JSON Object
     * @param passhash if true, include the hashed password in the JSON Object
     * @return a JSON Object containing the User's information
     * @throws SQLException if there is an error reading the information
     */
    public static JSONObject userToJSON(ResultSet rs, boolean userLevel, boolean passhash) throws SQLException {
        JSONObject ret = new JSONObject();
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

    /**
     * Gets a list of the pending Users(Not currently accepted)
     *
     * @return A JSONObject with either the list, or the error code<br>
     * Error Codes:<br>
     * 2: SQL Error<br>
     */
    public JSONObject getPendingUsers() {
        try {
            JSONObject ret = new JSONObject();
            ResultSet rs = connection.executeQuery("select USERNAME, FIRSTNAME, LASTNAME from USERS where PENDING = 1;");
            while (rs.next()) {
                ret.append("users", userToJSON(rs, false, false));
            }
            return ret;
        } catch (SQLException ex) {
            ex.printStackTrace(System.out);
            return Utils.errorJson(1);
        }
    }

    /**
     * sets the current Competition / Regional ID of a logged in user
     *
     * @param token the token of the logged in user
     * @param compid the competition id to set for the user
     * @return error code<br>
     * Error codes:<br>
     * 0: No Error<br>
     * 1: Comp ID doesn't exist<br>
     * 2: SQL Error<br>
     * 3: user doesn't exist
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

    public JSONObject getMatchesScouted(String token) {
        try {
            JSONObject ret = new JSONObject();
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

    /**
     * Gets the info for a given user
     *
     * @param token
     * @return the data, or null
     */
    public JSONObject getUserInfo(String token) {
        User us = getUserByToken(token);
        if (us == null) {
            return null;
        }
        JSONObject tmp = new JSONObject();
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
        return this.getUserByToken(token) != null;
    }

    public boolean userExists(String username) {
        try {
            PreparedStatement checkUser = connection.prepareStatement("select * from USERS where USERNAME = ?");
            checkUser.setString(1, username);
            ResultSet rs = checkUser.executeQuery();
            return rs.next();
        } catch (SQLException ex) {
            ex.printStackTrace(System.out);
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
            ex.printStackTrace(System.out);
            return false;
        }
    }

    /**
     * Adds a new user with limited access, in pending stage (have to be
     * approved by admin to access website)
     *
     * @param obj the JSON object that contains the necessary information. This
     * object should match 'UserCreateTemplate'
     * @return error code<br>
     * Error Codes:<br>
     * 0: Success<br>
     * 1: SQL Error<br>
     * 2: Invalid Template<br>
     * 3: User Exists<br>
     * 4: Password is greater that 128 characters<br>
     * 5: Password does not match the password security requirements<br>
     */
    public int addNewUser(JSONObject obj) {
        try {
            if (!Utils.checkTemplate("UserCreateTemplate", obj)) {
                return 2;
            }
            if (userExists(obj.getString("username"))) {
                return 3;
            }
            if (obj.getString("password").length() > 128 || obj.getString("password").isEmpty()) {
                return 3;
            }
            if (!passwordMatchesCustomReqs(obj.getString("password"))) {
                return 5;
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
            newUser.execute();
            return 0;
        } catch (SQLException ex) {
            ex.printStackTrace(System.out);
            return 1;
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

    /**
     * Sets a User's Pending state to false, and sets their user level
     *
     * @param obj the data to set
     * @param token the token of the approving user
     * @return the Error Code<br>
     * Error Codes:<br>
     * 0: No error<br>
     * 1: SQL Error<br>
     * 2: Doesn't match template<br>
     * 3: Invalid User type<br>
     * 4: User doesn't exist<br>
     * 5: Sending User isn't valid<br>
     */
    public int approveUser(JSONObject obj, String token) {
        if (!Utils.checkTemplate("UserAcceptTemplate", obj)) {
            return 2;
        }
        try {
            if (!isValidUserType(obj.getString("userlevel")) && !obj.getString("userlevel").equalsIgnoreCase("delete")) {
                return 3;
            }
            if (!userExists(obj.getString("username")) || getUserByToken(token) == null) {
                return 4;
            }
            if (getUserByToken(token).getUserLevel() != UserLevel.ADMIN) {
                return 5;
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
            exec.execute();
            return 0;
        } catch (SQLException ex) {
            ex.printStackTrace(System.out);
            return 1;
        }
    }

    /**
     * Changes the password for a specified user
     *
     * @param token the token for the user
     * @param obj the data
     * @return Error code<br>
     * Error Codes:<br>
     * 0: No Error<br>
     * 1: SQL Error<br>
     * 2: Doesn't match template<br>
     * 3: User not logged in<br>
     * 4: Old Password Does not match<br>
     */
    public int changePassword(String token, JSONObject obj) {
        try {
            if (!Utils.checkTemplate("UserChangePassTemplate", obj)) {
                return 2;
            }
            User u = getUserByToken(token);
            if (u == null) {
                return 3;
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
                return 0;
            } else {
                return 4;
            }
        } catch (SQLException ex) {
            ex.printStackTrace(System.out);
            return 1;
        }
    }

    public boolean isUserLoggedIn(String username) {
        return users.stream().anyMatch(n -> n.getUsername().equalsIgnoreCase(username));
    }

    /**
     * given a JSON matching UserLoginTemplate, returns a token for user login
     *
     * @param obj the JSON object containing the data
     * @return a token that is given to the client to keep a session, or error
     * code<br>
     * Error codes:<br>
     * 1: SQL Error<br>
     * 2: Does not match Template<br>
     * 3: User does not exist / Password is invalid<br>
     */
    public JSONObject loginUser(JSONObject obj) {
        Utils.logln("USERS LOGGED IN: " + users);
        if (!Utils.checkTemplate("UserLoginTemplate", obj)) {
            return Utils.errorJson(2);
        }
        try {
            if (!userExists(obj.getString("username"))) {
                return Utils.errorJson(3);
            }
            PreparedStatement exec
                    = connection.prepareStatement("select * from USERS where USERNAME = ? ");
            exec.setString(1, obj.getString("username"));
            ResultSet rs = exec.executeQuery();
            if (!rs.next()) {
                return Utils.errorJson(3);
            }
            String passhash = Utils.hashPassword(rs.getString("PASSWORD_SALT"), obj.getString("password"));
            if (rs.getString("PASSWORD_HASH").equals(passhash)) {
                String token;
                if (isUserLoggedIn(rs.getString("USERNAME"))) {
                    final String usernamewhy = rs.getString("USERNAME");
                    token = users.stream().filter(n -> n.getUsername().equalsIgnoreCase(usernamewhy))
                            .findAny().get().getToken();
                } else {
                    while (true) {
                        token = Utils.generateToken(256);
                        final String why = token;
                        if (users.stream().noneMatch(n -> n.getToken().equals(why))) {
                            break;
                        }
                    }
                }
                users.add(new User(rs.getInt("ID"), obj.getString("username"), token,
                        rs.getString("USER_LEVEL"), rs.getString("FIRSTNAME"), rs.getString("LASTNAME")));
                return new JSONObject("{\"token\":\"" + token + "\"}");
            } else {
                return Utils.errorJson(3);
            }
        } catch (SQLException ex) {
            ex.printStackTrace(System.out);
            return Utils.errorJson(1);
        }
    }

    //Only for temp debugging
    public static void main(String... args) throws SQLException {
        UserHandler g = new UserHandler(new DatabaseConnection("jdbc:mysql://localhost/orcascout?useSSL=false", "root", "NONO"));
        g.connection.connect();

    }
}
