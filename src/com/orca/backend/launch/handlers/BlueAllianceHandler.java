package com.orca.backend.launch.handlers;

import com.orca.backend.launch.Prefs;

public class BlueAllianceHandler{
    private static final String API_KEY = Prefs.getString("tba_api_key");
    private BlueAllianceHandler(){} //forbids instantiation
    /**
     * TODO IMPLEMENT
     * Checks if a match ID is valid
     * @param matchID the match ID to check
     * @return Error Code<br>
     * Error Codes:<br>
     * 0: No Error<br>
     */
    public static int matchIsValid(String matchID){
        return 0; 
    }
    
}