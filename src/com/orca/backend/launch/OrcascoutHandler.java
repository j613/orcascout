package com.orca.backend.launch;

import com.orca.backend.server.HTTPInput;
import com.orca.backend.server.InputHandler;
import com.orca.backend.server.ResponseFile;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;

public class OrcascoutHandler implements InputHandler {

	private static final HashMap<String, ResponseFile> memCachedFiles = new HashMap<String, ResponseFile>() {
		@Override
		public ResponseFile get(Object key) {
			if (!(key instanceof String))
				return null;
			return super.get(((String) key).replaceAll("\\\\", "/").toLowerCase());
		}

	};

	static {
		try {
			Path p = new File(OrcascoutHandler.class.getResource("/frontend/").toURI()).toPath();
			Files.walk(p).forEach(n -> {
				if (!n.toFile().isDirectory()) {
					try {
						String g = n.toString();
						g = g.substring(g.indexOf("frontend") + 8).replaceAll("\\\\", "/");
						memCachedFiles.put(g, ResponseFile.readFromFile(n));
					} catch (IOException ex) {
						ex.printStackTrace();
					}
				}
			});
		} catch (IOException e) {
			System.err.println("Error reading files from disk. abort");
			e.printStackTrace();
		} catch (URISyntaxException ex) {
			ex.printStackTrace();
		}
	}

	private ResponseFile getCachedFile(String f) {
		return memCachedFiles.get(f);
	}
	private boolean shouldCloseConnectionErrorCode(int code) {
		return code > 0;
	}
	@Override
	public boolean handleRequest(HTTPInput in, BufferedWriter out) throws IOException {
		ResponseFile sendFile;
		String respMessage = "200 OK";
		System.out.println("requested File: " + in.getRequestedFile());
		if (!memCachedFiles.containsKey(in.getRequestedFile())) {
			respMessage = "404 Not Found";
			System.out.println("File not Found");
			sendFile = getCachedFile("/errorFiles/404error.html");
		} else if (in.getErrorCode() == 4) {
			respMessage = "400 Bad Request";
			sendFile = getCachedFile("/errorFiles/400error.html");
		} else if(in.getErrorCode() >= 1 && in.getErrorCode() <= 3){
			respMessage = "413 Payload Too Large";
			sendFile = getCachedFile("/errorfiles/413error.html");
		} else if(in.getErrorCode() == 5) {
			respMessage = "500 Internal Server Error";
			sendFile = getCachedFile("/errorFiles/500error.html");
		}else if(in.getErrorCode() == 6){
			respMessage = "411 Length Required";
			sendFile = getCachedFile("/errorFiles/411error.html");
		}else {
			sendFile = getCachedFile(in.getRequestedFile());
		}
		System.out.println("SENDING FILE");
		sendFile(sendFile, respMessage, null, out);
		System.out.println("File Sent");
		return in.getOrDefault("Connection", "keep-alive").equals("close") || shouldCloseConnectionErrorCode(in.getErrorCode());
	}
	/*
	 * @Override public ResponseFile getResponseFile(String fileName) throws
	 * IOException { return null; }
	 */
}
