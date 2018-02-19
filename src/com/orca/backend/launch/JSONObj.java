/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.orca.backend.launch;

import java.util.Map;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class JSONObj extends JSONObject{

    public JSONObj(String source) throws JSONException {
        super(source);
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
}
