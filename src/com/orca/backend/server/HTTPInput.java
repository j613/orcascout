package com.orca.backend.server;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author ethanf108 keys: "method": get pot etc "filePath": path requested
 * "httpVersion": HTTP/1.1 etc everything else is what is sent example: "Date: 2
 * june 2018" -> "Date":"2 june 2018" (value is trimmed)
 *
 */
public class HTTPInput extends HashMap<String, String> {

    private boolean inReadRawDataMode = false;
    private HashMap<String, String> phpArgs = new HashMap<>();
    /*
	 * List of Error codes:
	 * 0: no Error
	 * 1: request too long
	 * 2: post Body too long
	 * 3: no content-Length in post
	 * 4: general request header error
	 * 5: exception thrown in parsing
	 * 6: no Content-Length specified
     */
    private int error = 0;
    private StringBuffer postDataBuffer = null;
    private boolean isFinished;

    public boolean isFinished() {
        return isFinished;
    }

    public HashMap<String, String> getPhpArgs() {
        return phpArgs;
    }

    public String getRequestedFile() {
        return get("filePath"); //TODO: not important but if the client sends a request with the filePath header, it overwrites the GET/POST FIle
    }

    public String getCookie(String name) {
        if(!containsKey("Cookie")){
            return null;
        }
        String[] cokies = get("Cookie").split(";");
        for (String g : cokies) {
            if(g.split("=")[0].equalsIgnoreCase(name)){
                return g.split("=")[1];
            }
        }
        return null;
    }

    public void setError(int b) {
        error = b;
    }
    public boolean inPostMode(){
        return inReadRawDataMode;
    }
    public int getErrorCode() {
        return error;
    }

    public String getRawPostData() {
        return postDataBuffer == null ? null : postDataBuffer.toString();
    }
    
    /**
     * TODO: implement
     * Warning: Jenkiest code ever plz expect it to break.
     * gets post data
     * @return post data
     */
    //public PostData[] getActualPostData() {}

    /**
     *
     * @param s the string to parse
     * @return true if there was an error
     */
    public int parseString(String s) {
        try {
            if (inReadRawDataMode) {
                int clength = Integer.parseInt(getOrDefault("Content-Length", "0"));
                if (!containsKey("Content-Length")) {
                    Utils.logln("PARSER$ POST, but no content length");
                    error = 6;
                    return error;
                }
                if (clength > 1024 * 1024 || clength > Integer.parseInt(get("Content-Length"))) {
                    Utils.logln("PARSER$ Content-Legnth too big");
                    error = 2;
                    return error;
                }
                if (postDataBuffer == null) {
                    postDataBuffer = new StringBuffer(clength);
                }
                postDataBuffer.append(s);
                if (postDataBuffer.length() == clength) {
                    Utils.logln("PARSER$ Length exceeded Content-Length");
                    isFinished = true;
                }
            } else {
                if (s.matches("(POST|PUT|GET|HEAD|OPTIONS) [^ ]+ HTTP\\/(1\\.1|2|1)\r?\n")) {
                    String[] ins = s.split(" ");
                    if (ins[0].matches("(PUT|HEAD)")) {
                        error = 4;
                        return error;
                    }
                    String filePath = ins[1];
                    if (filePath.contains("?")) {
                        String args[] = filePath.split("\\?");
                        args = args[1].split("&");
                        for (String g : args) {
                            phpArgs.put(g.split("\\=")[0], g.split("\\=")[1]);
                        }
                    }
                    put("method", ins[0]);
                    put("filePath", ins[1].split("\\?")[0]);
                    put("httpVersion", ins[2].split("\\/")[1]);
                } else if (s.matches(".+: .+\r?\n")) {
                    String field = s.substring(0, s.indexOf(":"));
                    put(field, s.substring(s.indexOf(":") + 1).trim());
                } else if (s.equals("\n") || s.equals("\r\n")) {
                    if (get("method").equals("POST")) {
                        inReadRawDataMode = true;
                    } else {
                        isFinished = true;
                    }
                } else {
                    error = 4;
                    return error;
                }
            }
            return 0;
        } catch (Exception ex) {
            error = 5;
            Utils.logln("PARSER$ Exception occured in parsing.");
            ex.printStackTrace(System.out);
            return error;
        }
    }

}
