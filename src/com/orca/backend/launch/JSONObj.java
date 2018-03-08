/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.orca.backend.launch;

import com.orca.backend.server.LCHashMap;
import com.orca.backend.server.Utils;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class JSONObj extends JSONObject {

    private static final LCHashMap<JSONObj> templates = new LCHashMap<>();

    static {
        try {
            Path p = new File(JSONObj.class.getResource("/com/orca/backend/templates/").toURI()).toPath();
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
            e.printStackTrace(System.out);
        } catch (URISyntaxException ex) {
            ex.printStackTrace(System.out);
        }
    }

    public static boolean checkTemplate(String temp, JSONObj obj) {
        return templates
                .get(temp)
                .similar(obj);
    }

    public JSONObj(String source) throws JSONException {
        super(source);
    }

    public JSONObj() {
        super();
    }

    public JSONObj(JSONObj source) throws JSONException {
        super(source);
    }

    public JSONObj(Object source) throws JSONException {
        this(source.toString());
    }

    @Override
    public boolean similar(Object other) {
        try {
            if (!(other instanceof JSONObject)) {
                return false;
            }
            if (!this.keySet().equals(((JSONObject) other).keySet())) {
                return false;
            }
            for (final Map.Entry<String, ?> entry : this.entrySet()) {
                String name = entry.getKey();
                Object valueThis = entry.getValue();
                Object valueOther = ((JSONObject) other).get(name);
                /*if (valueThis == valueOther) {
                    continue;
                }
                if (valueThis == null) {
                    return false;
                }*/
                if (valueThis instanceof JSONObject) {
                    if (!((JSONObject) valueThis).similar(valueOther)) {
                        return false;
                    }
                } else if (valueThis instanceof JSONArray) {
                    if (!((JSONArray) valueThis).similar(valueOther)) {
                        return false;
                    }
                }/* else if (!valueThis.equals(valueOther)) {
                    return false;
                }*/
            }
            return true;
        } catch (Throwable exception) {
            return false;
        }
    }
    public static JSONObj JSONForError(int ecode, Throwable t){
        JSONObj j = new JSONObj();
        j.put("errorcode", ecode);
        //TODO: Add option for sending exception messages in Config file
        j.put("exception", Utils.exceptionStackTraceToString(t));
        return j;
    }
}
