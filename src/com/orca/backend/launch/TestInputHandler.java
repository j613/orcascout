package com.orca.backend.launch;

import java.io.BufferedWriter;
import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;

import com.orca.backend.server.HTTPInput;
import com.orca.backend.server.InputHandler;
import com.orca.backend.server.ResponseFile;

public class TestInputHandler implements InputHandler {

	private static final HashMap<String, ResponseFile> memCachedFiles = new HashMap<>();
	static {
		try {
			memCachedFiles.put("/test.html", ResponseFile.readFromFile(
					TestInputHandler.class.getResourceAsStream("/frontend/test.html"), "text/html; charset=utf-8"));
			memCachedFiles.put("/404.html", ResponseFile.readFromFile(
					TestInputHandler.class.getResourceAsStream("/frontend/404.html"), "text/html; charset=utf-8"));
		} catch (IOException e) {
			System.err.println("Error reading files from disk. abort");
			e.printStackTrace();
		}
	}

	@Override
	public boolean handleRequest(HTTPInput in, BufferedWriter out) throws IOException {
		ResponseFile sendFile;
		String respMessage = "200 OK";
		System.out.println("requested File: " + in.getRequestedFile());
		if (!memCachedFiles.containsKey(in.getRequestedFile())) {
			respMessage = "404 Not Found";
			System.out.println("File not Found");
			sendFile = memCachedFiles.get("/404.html");
		} else {
			sendFile = memCachedFiles.get(in.getRequestedFile());
		}
		System.out.println("SENDING FILE");
		sendFile(sendFile, respMessage, null, out);
		
		System.out.println("LLL"+in.getOrDefault("Connection", "close")+"LLL");
		return in.getOrDefault("Connection", "close").equals("close");
	}

	@Override
	public ResponseFile getResponseFile(String fileName) throws IOException {
		return null;
	}
}