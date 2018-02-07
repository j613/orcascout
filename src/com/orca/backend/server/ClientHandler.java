package com.orca.backend.server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
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
			BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
			while(!in.ready())Thread.sleep(100);
			while (!clientSocket.isClosed()) {
				long inputSize=0;
				StringBuffer inputBuffer = new StringBuffer(1024);
				HTTPInput HTTPParser = new HTTPInput();
				for(;;){
					char c = (char)in.read();
					inputSize++;
					System.out.print(c);
					inputBuffer.append(c);
					if(inputBuffer.length()>1024|| inputSize>1024*1024) {
						HTTPParser.setFile("400Error.html", "400 Bad Request"); //TODO Change File Name
						System.out.println("Rquest Too Long");
						HTTPParser.put("disconnect", "true");
						break;
					}
					if(c=='\n'){
						if(HTTPParser.parseString(inputBuffer.toString())){
							HTTPParser.setFile("400Error.html", "400 Bad Request"); //TODO Change File Name
							System.out.println("Bad request Header");
							HTTPParser.put("disconnect", "true");
							break;
						}
						if(HTTPParser.isFinished()){
							break;
						}
						inputBuffer = new StringBuffer(1024);
					}
				}
				server.sendToInputHandler(HTTPParser, out);
			}
		} catch (IOException ex) {
			System.err.println("IO Error in Thread "+getName());
			ex.printStackTrace();
			System.err.flush();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
