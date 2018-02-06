package com.orca.backend.server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

import com.orca.backend.server.utils.SendFile;

public class ClientHandler extends Thread {
	public static final ThreadGroup ClientHandlerThreadGroup = new ThreadGroup("Orca Client Handler Group");
	private final Socket clientSocket;

	public ClientHandler(Socket cs) {
		super(ClientHandlerThreadGroup, "Client Handler for " + cs.getInetAddress());
		this.clientSocket = cs;
	}

	@Override
	public void run() {
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
			while(!in.ready())Thread.sleep(100);
			while (!clientSocket.isClosed()) {
				StringBuffer inputBuffer = new StringBuffer(1024*1024);
				SendFile sendFile = new SendFile();
				for(;;) {
					inputBuffer.append(((char)in.read()));
					if(inputBuffer.length()>1024*1024) {
						sendFile.setFile("400Error.html", "400 Bad Request"); //TODO Change File Name
					}
				}
				
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
