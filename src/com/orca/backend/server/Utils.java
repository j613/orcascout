package com.orca.backend.server;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public final class Utils {

    private static final Key KEYPAIR;

    static {
        Key t;
        try {
            InputStream keystoreStream = Utils.class.getResourceAsStream("/com/orca/backend/server/KeyStore.jceks");
            KeyStore keystore = KeyStore.getInstance("JCEKS");
            keystore.load(keystoreStream, "orcascout".toCharArray());
            if (!keystore.containsAlias("www.orcascout.com")) {
                throw new RuntimeException("Alias for key not found");
            }
            t = keystore.getKey("www.orcascout.com", "orcascout".toCharArray());
       } catch (IOException ex) {
            t = null;
            System.err.println("Error Reading Key Store");
            ex.printStackTrace();
            System.exit(1);
        } catch (KeyStoreException | NoSuchAlgorithmException | CertificateException | UnrecoverableKeyException ex) {
            t = null;
            System.err.println("Error Reading Key Store");
            ex.printStackTrace();
            System.exit(1);
        }
        KEYPAIR = t;
    }

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

    public static String hashPassword(String salt, String password) {
        return encrypt(salt + password);
    }

    public static String encrypt(String g) {
        try {
            Cipher c = Cipher.getInstance("AES");
            c.init(Cipher.ENCRYPT_MODE, KEYPAIR);
            String s = new String(c.doFinal(g.getBytes()));
            return s;
        } catch (NoSuchAlgorithmException | NoSuchPaddingException
                | InvalidKeyException | IllegalBlockSizeException | BadPaddingException ex) {
            System.err.println("Error Generating an encryption key");
            ex.printStackTrace();
        }
        return null;
    }
}
