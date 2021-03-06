package com.orca.backend.server;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Set;

public interface InputHandler {

    public default void sendFile(ResponseFile file, String responseMessage, HashMap<String, String> args,
            BufferedWriter outWriter, Set<String> cookies) throws IOException {

        outWriter.write("HTTP/1.1 " + responseMessage + "\r\n");
        outWriter.write("Date: " + Utils.getHTTPDate() + "\r\n");
        if (file != null) {
            outWriter.write("Content-Length: " + file.getFileLength() + "\r\n");
            outWriter.write("Content-Type: " + file.getContentType() + "\r\n");
        }
        outWriter.write("Content-Encoding: identity\r\n");
        outWriter.write("Server: " + Server.OrcaVersion + "\r\n");
        outWriter.write("Content-Language: en-US\r\n");
        if(cookies!=null){
            for(String g : cookies){
                outWriter.write("Set-Cookie: "+g+"\r\n");
            }
        }
        if (args != null) {
            for (String k : args.keySet()) {
                outWriter.write(k + ": " + args.get(k) + "\r\n");
            }
        }
        if (file != null) {
            outWriter.newLine();
            outWriter.write(file.getContents());
            if (!file.getContents().endsWith("\n")) {
                outWriter.newLine();
            }
        }
        outWriter.newLine();
        outWriter.flush();
    }

    /**
     *
     * @param in
     * @param out
     * @return true if should close socket
     * @throws IOException
     */
    public boolean handleRequest(HTTPInput in, BufferedWriter out);

    //public ResponseFile getResponseFile(String fileName) throws IOException;
}
