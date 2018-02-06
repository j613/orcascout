package com.orca.backend.server.utils;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.HashMap;
import com.orca.backend.launch.Main;

/**
 * 
 * @author ethanf108 keys: "filePath": file path in TODO CHANGE
 *         "responseMessage": like 200 OK
 * 
 */
public class SendFile extends HashMap<String, String> {
	public void setFile(String fileName) {
		put("filePath", fileName);
	}
	public void setFile(String fileName, String responseCode) {
		put("filePath", fileName);
		put("responseMesage", responseCode);
	}

	private String readFile() { // TODO work with frontend to implement
		return "<html><head><title>test</title></head><body><h1>test file</h1></body></html>";
	}

	private String getContentType() {// TODO implement
		return "text/html; charset=utf-8";
	}

	public void writeFile(BufferedWriter outWriter) throws IOException {
		String fileContents = readFile();
		outWriter.write("HTTP/1.1 " + get("responseMessage") + "\r\n");
		outWriter.write("Date: " + Utils.getHTTPDate());
		outWriter.write("Content-Length: " + fileContents.length());
		outWriter.write("Content-Type: " + getContentType());
		outWriter.write("Content-Encoding: identity");
		outWriter.write("Server: " + Main.OrcaVersion);
		outWriter.write("Content-Language: en-US");
		outWriter.flush();
	}
}
