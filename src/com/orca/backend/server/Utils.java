package com.orca.backend.server;

import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Random;
import java.util.TimeZone;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public final class Utils {

    private static final Random rand = new Random();

    private Utils() {
    }

    /**
     * Thanks Hannes R. from Stack Overflow
     *
     * @return the date time in HTTP format
     */
    public static String getHTTPDate() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        return dateFormat.format(calendar.getTime());
    }

    public static String getHTTPDate(long millisAdd) {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        return dateFormat.format(new Date(calendar.getTimeInMillis() + millisAdd));
    }

    public static String hashPassword(String salt, String password) {
        return genHash(salt + password);
    }

    public static String genHash(String g) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            g = Base64.encode(digest.digest(
                    g.getBytes()));
            return g;
        } catch (NoSuchAlgorithmException ex) {
            System.err.println("Error Generating an encryption key");
            ex.printStackTrace();
        }
        return null;
    }

    public static String generateToken(int size) {
        byte[] byts = new byte[size];
        rand.nextBytes(byts);
        return Base64.encode(byts).substring(0, size);
    }
}
