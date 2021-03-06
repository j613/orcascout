package com.orca.backend.sql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DatabaseConnection {

    public static final String driverPath = "com.mysql.jdbc.Driver";

    static {
        try {
            Class.forName(driverPath); //load Driver class
        } catch (ClassNotFoundException ex) {
            System.err.println("MySQL Driver not Found.");
            ex.printStackTrace();
        }
    }
    private final String hostname, username, password;
    private Connection connection;
    public DatabaseConnection(String hostname, String username, String password) {
        this.hostname = hostname;
        this.username = username;
        this.password = password;
    }
    public void close(){
        try {
            connection.close();
        } catch (SQLException ex) {
            ex.printStackTrace(System.out);
        }
    }
    public boolean connect(){
        try {
            connection = DriverManager.getConnection(hostname, username, password);
            return true;
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }
    public boolean isConnected(){
        if(connection == null)return false;
        try {
            return !connection.isClosed();
        } catch (SQLException ex) {
            ex.printStackTrace();
            return true;
        }
    }
    public boolean execute(String g) throws SQLException{
        return connection.createStatement().execute(g);
    }
    public ResultSet executeQuery(String q) throws SQLException{
        return connection.createStatement().executeQuery(q);
    }
    public ResultSet executeQuery(String q, Object ... args) throws SQLException{
        PreparedStatement ps =  connection.prepareStatement(q);
        for(int i = 0; i < args.length; i++){
            ps.setObject(i+1, args[i]);
        }
        return ps.executeQuery();
    }
    public PreparedStatement prepareStatement(String g) throws SQLException{
        return connection.prepareStatement(g);
    }
}
