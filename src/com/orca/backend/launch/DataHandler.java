package com.orca.backend.launch;

import com.orca.backend.sql.DatabaseConnection;
import java.awt.BorderLayout;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONObject;

public class DataHandler {
    private JSONObject GameTemplate;
    //TODO: Implement SSL
    private final DatabaseConnection connection = new DatabaseConnection("jdbc:mysql://localhost/orcascout?useSSL=false", "root", "NONO");

    {
        try {
            GameTemplate = new JSONObject(new String(Files.readAllBytes(new File(OrcascoutHandler.class.getResource("/com/orca/backend/launch/TestTemplate.json").toURI()).toPath())));
        } catch (URISyntaxException | IOException ex) {
            System.out.println("Unable to parse game template. abort");
            ex.printStackTrace(System.out);
            System.exit(1);
        }
        if (!connection.connect()) {
            System.out.println("Error connecting to database. abort");
            System.exit(1);
        }
    }
    public boolean submitJSON(String g){
        return false;
    }
}
