package com.orca.backend.server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

public class ClientHandler extends Thread {
	public static final ThreadGroup ClientHandlerThreadGroup = new ThreadGroup("Orca Client Handler Group");
	private final Socket clientSocket;
	private final Server server;

	public ClientHandler(Socket cs, Server s) {
		super(ClientHandlerThreadGroup, "Client Handler for " + cs.getInetAddress());
		this.clientSocket = cs;
		server = s;
	}

	@Override
	public void run() {
		try {
			System.out.println("Connection from " + clientSocket.getInetAddress() + " opened");
			InputStream in = clientSocket.getInputStream();
			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
			while (!clientSocket.isClosed()) {
				/*while (!in.ready())
					Thread.sleep(100);*/
				long inputSize = 0;
				StringBuffer inputBuffer = new StringBuffer(1024);
				HTTPInput HTTPParser = new HTTPInput();
				int inc = in.read();
				for (; inc >= 0 && !clientSocket.isClosed(); inc = in.read()) {
					char c = (char)inc;
					inputSize++;
					System.out.print(c); // Debug print request
					inputBuffer.append(c);
					if (inputBuffer.length() > 1024 || inputSize > 1024 * 1024) {
						System.out.println("Request Too Long");
						HTTPParser.setError(true);
						break;
					}
					if (c == '\n') {
						if (HTTPParser.parseString(inputBuffer.toString())) {
							System.out.println("Bad request Header");
							HTTPParser.setError(true);
							break;
						}
						if (HTTPParser.isFinished()) {
							break;
						}
						inputBuffer = new StringBuffer(1024);
					}
				}
				System.out.println("Finished parsing");
				if(inc < 0){
					System.out.println("Input Returned null");
				}
				if (server.sendToInputHandler(HTTPParser, out) || inc < 0) {
					clientSocket.close();
				}
			}
		} catch (IOException ex) {
			System.err.println("IO Error in Thread " + getName());
			ex.printStackTrace();
			System.err.flush();
		/*} catch (InterruptedException e) {
			e.printStackTrace();*/
		} finally {
			System.out.println("Connection from " + clientSocket.getInetAddress() + " closed");
			try {
				clientSocket.close();
			} catch (IOException e) {
				System.err.println("is ok");
				e.printStackTrace();
			}
		}
	}
}
