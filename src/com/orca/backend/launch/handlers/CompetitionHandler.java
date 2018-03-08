package com.orca.backend.launch.handlers;

import com.orca.backend.sql.DatabaseConnection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class CompetitionHandler {

    private final DatabaseConnection connection;

    public CompetitionHandler(DatabaseConnection c) {
        connection = c;
    }

    /**
     * check to see if competition id is in the database
     *
     * @param compID
     * @return error code error codes: 0: no error 1: doesn't exist 2: SQL error
     */
    public int compExists(String compID) {
        try {
            PreparedStatement ps = connection.prepareStatement("select * from COMPETITIONS where COMP_ID = ?");
            ps.setString(1, compID);
            return ps.executeQuery().next() ? 0 : 1;
        } catch (SQLException ex) {
            ex.printStackTrace(System.out);
            return 2;
        }
    }
}
