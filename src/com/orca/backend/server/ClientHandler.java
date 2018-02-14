package com.orca.backend.server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.SocketTimeoutException;

public class ClientHandler extends Thread {

    public static final ThreadGroup ClientHandlerThreadGroup = new ThreadGroup("Orca Client Handler Group");
    private final Socket clientSocket;
    private final Server server;

    public ClientHandler(Socket cs, Server s) {
        super(ClientHandlerThreadGroup, "Client Handler for " + cs.getInetAddress());
        this.clientSocket = cs;
        server = s;
    }
    private synchronized void logln(String g){
        Server.logln(clientSocket.getInetAddress().toString(),g);
    }
    @Override
    public void run() { //TODO make isClosed calls more clean we dont need three of them
        int inc = 0;
        try {
            logln("Connection from " + clientSocket.getInetAddress() + " opened" + getName());
            InputStream in = clientSocket.getInputStream();
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));

            while (!clientSocket.isClosed()) {
                /*while (!in.ready())
					Thread.sleep(100);*/
                long inputSize = 0;
                StringBuffer inputBuffer = new StringBuffer(1024);
                HTTPInput HTTPParser = new HTTPInput();
                logln("SHUTDOWN OUT: " + clientSocket.isOutputShutdown() + " IN: " + clientSocket.isInputShutdown() + "CLOSED: " + clientSocket.isClosed() + "avaliable: " + in.available());

                inc = in.read();
                for (; inc >= 0 && !clientSocket.isClosed(); inc = in.read()) {
                    char c = (char) inc;
                    inputSize++;
                    //System.out.print(c);//+""+inc+" "); // Debug print request
                    inputBuffer.append(c);
                    if (inputBuffer.length() > 1024 || inputSize > 1024 * 1024) {
                        logln("Request Too Long");
                        HTTPParser.setError(true);
                        break;
                    }
                    if (c == '\n') {
                        if (HTTPParser.parseString(inputBuffer.toString())) {
                            logln("Bad request Header");
                            HTTPParser.setError(true);
                            break;
                        }
                        if (HTTPParser.isFinished()) {
                            logln("LLLLLL");
                            logln(HTTPParser.getActualPostData());
                            break;
                        }
                        inputBuffer = new StringBuffer(1024);
                    }
                }
                logln("Finished parsing");
                if (inc < 0) {
                    logln("Input Returned null");
                    break;
                } else if (server.sendToInputHandler(HTTPParser, out)) {
                    clientSocket.close();
                    logln("Intentiaonally Closed");
                }
            }
        } catch(SocketTimeoutException e){
            logln("Socket timed out");
        }catch (IOException ex) {
            logln("IO Error in Thread " + getName());
            logln("inc: " + inc);
            ex.printStackTrace(System.out);
            //System.err.flush();
            /*} catch (InterruptedException e) {
			e.printStackTrace();*/
        } finally {
            logln("Connection from " + clientSocket.getInetAddress() + " closed");
            try {
                clientSocket.close();
            } catch (IOException e) {
                //System.err.println("is ok");
            }
        }
    }
}
