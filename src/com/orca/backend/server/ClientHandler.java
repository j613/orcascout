package com.orca.backend.server;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;

public class ClientHandler extends Thread {

    public static final ThreadGroup clientHandlerThreadGroup = new ThreadGroup("Orca Client Handler Group");
    private final Socket clientSocket;
    private final Server server;

    public ClientHandler(Socket cs, Server s) {
        super(clientHandlerThreadGroup, "This won't affect anything anyway");
        this.clientSocket = cs;
        server = s;
    }

    @Override
    public void run() { //TODO make isClosed calls more clean we dont need three of them
        int inc = 0;
        try {
            Thread.currentThread().setName("Handler for " + clientSocket.getInetAddress());
            Utils.logln(clientSocket.getInetAddress() + " opened");
            InputStream in = clientSocket.getInputStream();
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));

            while (!clientSocket.isClosed()) {
                /*while (!in.ready())
					Thread.sleep(100);*/
                long inputSize = 0;
                StringBuffer inputBuffer = new StringBuffer(1024);
                HTTPInput HTTPParser = new HTTPInput();
                inc = in.read();
                for (; inc >= 0 && !clientSocket.isClosed(); inc = in.read()) {
                    char c = (char) inc;
                    inputSize++;
//                    System.out.print(c);//+""+inc+" "); // Debug print request
                    inputBuffer.append(c);
                    if (inputBuffer.length() > 1024 || inputSize > 1024 * 1024) {
                        Utils.logln("PARSER$ Request Too Long");
                        HTTPParser.setError(1);
                        break;
                    }
                    if (HTTPParser.inPostMode()) {
                        HTTPParser.parseString(inputBuffer.toString());
                        if (HTTPParser.isFinished()) {
                            //logln(HTTPParser.getActualPostData());
                            break;
                        }
                        inputBuffer = new StringBuffer(1024);
                    } else if (c == '\n') {
                        if (HTTPParser.parseString(inputBuffer.toString()) != 0) {
                            Utils.logln("PARSER$ Bad request Header");
                            break;
                        }
                        if (HTTPParser.isFinished()) {
                            //logln(HTTPParser.getActualPostData());
                            break;
                        }
                        inputBuffer = new StringBuffer(1024);
                    }
                }
                Utils.logln("Finished parsing");
                if (inc < 0) {
                    Utils.logln("Input Returned null");
                    break;
                } else if (server.sendToInputHandler(HTTPParser, out)) {
                    clientSocket.close();
                    Utils.logln("Socket Intentiaonally Closed");
                }
            }
        } catch (SocketTimeoutException e) {
            Utils.logln("Socket timed out");
        } catch (IOException ex) {

            Utils.logln("IO Error in Thread " + getName());
            // logln("inc: " + inc);
            if (ex instanceof SocketException && ex.getMessage().trim().equalsIgnoreCase("Connection Reset")) {
                Utils.logln("Connection reset");
            } else {
                ex.printStackTrace(System.out);
            }
            //System.err.flush();
            /*} catch (InterruptedException e) {
			e.printStackTrace();*/
        } finally {
            Utils.logln("Connection from " + clientSocket.getInetAddress() + " closed");
            try {
                clientSocket.close();
            } catch (IOException e) {
                //System.err.println("is ok");
            }
        }
    }
}
