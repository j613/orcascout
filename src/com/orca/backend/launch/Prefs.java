package com.orca.backend.launch;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONObject;

public class Prefs {

    private Prefs() {
    }
    private static HashMap<String, Object> prefs = new HashMap<>();

    public static void refresh() {
        try {
            JSONObject prefsObj = new JSONObject(new String(
                    Files.readAllBytes(new File(Prefs.class.getResource("/com/orca/backend/launch/Preferences.json")
                            .toURI()).toPath())));
            prefsObj.keySet().forEach(k -> {
                prefs.put(k.toLowerCase(), prefsObj.get(k));
            });
        } catch (URISyntaxException | IOException ex) {
            ex.printStackTrace(System.out);
        }
    }

    public static String getString(String key) {
        return (String) prefs.get(key.toLowerCase());
    }

    public static String getString(String k, String def) {
        return (String) prefs.getOrDefault(k.toLowerCase(), def);
    }

    public static Object get(String k) {
        return prefs.get(k.toLowerCase());
    }

    public static Object get(String k, Object def) {
        return prefs.getOrDefault(k.toLowerCase(), def);
    }
    public static int getInt(String key){
        return Integer.parseInt(prefs.get(key.toLowerCase()).toString());
    }
    public static int getInt(String key, int def){
        return Integer.parseInt(prefs.getOrDefault(key.toLowerCase(),def).toString());
    }
}
