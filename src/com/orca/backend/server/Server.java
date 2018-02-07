package com.orca.backend.server;

import java.io.BufferedWriter;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server extends Thread {
	public static final String OrcaVersion = "Orca/0.1";
	private final InputHandler inputHandler;
	private final int serverPort;
	private boolean isRunning; // Maybe Volatile?
	private final ExecutorService execService = Executors.newCachedThreadPool();
	// private final boolean useSSL; //TODO implement SSL

	public Server(InputHandler inputHandler, int port) {
		super("Orca Server handler");
		setDaemon(false);
		serverPort = port;
		this.inputHandler = inputHandler;
	}

	public Server(InputHandler inputHandler) {
		this(inputHandler, 80);
	}

	public synchronized boolean sendToInputHandler(HTTPInput in, BufferedWriter out) throws IOException {
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
			ServerSocket server = new ServerSocket(serverPort);
			while (isRunning) {
				Socket connection = server.accept();
				ClientHandler chandle = new ClientHandler(connection, this);
				System.out.println("Accepted connection from: "+connection.getInetAddress());
				execService.submit(chandle);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
