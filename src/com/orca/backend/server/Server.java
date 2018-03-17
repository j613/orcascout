package com.orca.backend.server;

import com.orca.backend.launch.Prefs;
import java.io.BufferedWriter;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server extends Thread {

    public static final String OrcaVersion = Prefs.getString("server_name", "Orca/Nan");
    private final InputHandler inputHandler;
    private final int serverPort;
    private boolean isRunning; // Maybe Volatile?
    private final ExecutorService execService;
    // private final boolean useSSL; //TODO implement SSL


    public Server(InputHandler inputHandler, int port) {
        super("Orca Server handler");
        setDaemon(false);
        serverPort = port;
        this.inputHandler = inputHandler;
        int execSize = Prefs.getInt("server_threadpool_size", 10);
        execService = Executors.newFixedThreadPool(execSize);
    }

    public Server(InputHandler inputHandler) {
        this(inputHandler, 80);
    }

    synchronized boolean sendToInputHandler(HTTPInput in, BufferedWriter out) throws IOException {
        return inputHandler.handleRequest(in, out);
    }

    public void stopServer() {
        execService.shutdown();
        isRunning = false;
    }

    @Override
    public void start() {
        isRunning = true;
        super.start();
    }

    @Override
    public void run() {
        try {
            ServerSocket server = new ServerSocket(serverPort, Prefs.getInt("host_connection_backlog", 50),
                    InetAddress.getByName(Prefs.getString("host_bind_ip", "0.0.0.0")));
            while (isRunning) {
                Socket connection = server.accept();
                int soTimeout = Prefs.getInt("server_socket_timeout", 0);
                connection.setSoTimeout(soTimeout);
                ClientHandler chandle = new ClientHandler(connection, this);
                Utils.logln("Accepted connection from: " + connection.getInetAddress());
                execService.submit(chandle);

            }
        } catch (IOException e) {
            e.printStackTrace(System.out);
        }
    }
}
