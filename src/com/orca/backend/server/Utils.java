package com.orca.backend.server;

import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Random;
import java.util.TimeZone;
import org.json.JSONObject;
import org.mindrot.jbcrypt.BCrypt;

public final class Utils {

    
    private static final Random rand = new Random();
    
    public static boolean isErrorJSON(JSONObject obj) {
        return obj.keySet().contains("error");
    }
    /**
     * Utils cannot be instantiated
     */
    private Utils() {}

    /**
     * Thanks Hannes R. from Stack Overflow
     *
     * @return the date time in HTTP format
     */
    public static String getHTTPDate() {
        return getHTTPDate(0);
    }
    
    public static String getHTTPDate(long millisAdd) {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        return dateFormat.format(new Date(calendar.getTimeInMillis() + millisAdd));
    }
    
    public static String getTimeNoDate() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss", Locale.US);
        //dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        return dateFormat.format(calendar.getTime());
    }

    /**
     * Get This!<br>
     * Very Useful
     * @return This!
     */
    public Utils getThis() {
        return this;
    }
    
    public static String hashPassword(String salt, String password) {
        return BCrypt.hashpw(password, salt);
    }
    
    public static String genSalt() {
        return BCrypt.gensalt();
    }
    
    public static String generateToken(int size) {
        byte[] byts = new byte[size];
        rand.nextBytes(byts);
        return Base64.encode(byts).substring(0, size);
    }
    
    public static String exceptionStackTraceToString(Throwable t) {
        StringBuilder buff = new StringBuilder(1024);
        buff.append("Exception in thread \"")
                .append(Thread.currentThread().getName())
                .append("\" ")
                .append(t.getClass().getName())
                .append(": ")
                .append(t.getMessage() == null ? "" : t.getMessage());
        for (StackTraceElement s : t.getStackTrace()) {
            buff.append("        at ")
                    .append(s.toString());
        }
        return buff.toString();
    }
    
    public static JSONObject errorJson(int errorCode) {
        return new JSONObject("{\"error\":" + errorCode + "}");
    }
    
    public static boolean JSONSimilar(JSONObject a, JSONObject b) {
        for (String s : a.keySet()) {
            if (!b.keySet().contains(s)) {
                return false;
            }
        }
        return true;
    }
    
    public static synchronized void logln(String s) {
        StringBuilder buff = new StringBuilder();
        buff.append("[")
                .append(Thread.currentThread().getName())
                .append(" / ")
                .append(getTimeNoDate())
                .append("] ")
                .append(s)
                .append("\r\n");
        System.out.print(buff.toString());
    }
}
