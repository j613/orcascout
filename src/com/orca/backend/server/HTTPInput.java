package com.orca.backend.server;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.HashMap;

/**
 * 
 * @author ethanf108 keys: "filePath": file path in TODO CHANGE
 *         "responseMessage": like 200 OK "method": GET, POST etc "httpVersion":
 *         like HTTP/1.1 in first line of request
 * 
 */
public class HTTPInput extends HashMap<String, String> {
	private boolean inReadRawDataMode = false;
	private String escapeSequence = null; // String that marks the start / end
											// of POST Data
	private boolean hasError = false;
	private final StringBuffer postDataBuffer = new StringBuffer(1024 * 1024);
	private boolean isFinished;

	public boolean isFinished() {
		return isFinished;
	}

	public void setFile(String fileName) {
		put("filePath", fileName);
	}

	public void setFile(String fileName, String responseCode) {
		put("filePath", fileName);
		put("responseMesage", responseCode);
	}


	public boolean hasError() {
		return hasError;
	}

	/**
	 * 
	 * @param s
	 *            the string to parse
	 * @return true if there was an error
	 */
	public boolean parseString(String s) {
		try {
			if (inReadRawDataMode) {
				postDataBuffer.append(s);
				if(postDataBuffer.length()>1024*1024){
					hasError = true;
					return true;
				}
			} else {
				if (s.matches("(POST|PUT|GET|HEAD) .+ HTTP\\/(1\\.1|2)\r?\n")) {
					String[] ins = s.split(" ");
					if (ins[0].matches("(PUT|HEAD)")) {
						hasError = true;
						return true;
					}
					put("method", ins[0]);
					put("filePath", ins[1]);
					put("httpVersion", ins[2].split("/")[1]);
				} else if (s.matches(".+: .+\r?\n")) {
					String field = s.substring(0, s.indexOf(":"));
					put(field, s.substring(s.indexOf(":") + 1));
				} else if (s.equals("\n") || s.equals("\r\n")) {
					if(get("method").equals("POST")){
						inReadRawDataMode = true;
					}else{
						isFinished = true;
					}
				} else {
					hasError = true;
					return true;
				}
			}
			return false;
		} catch (ArrayIndexOutOfBoundsException ex) {
			hasError = true;
			return true;
		}
	}

	private String readFile() { // TODO work with frontend to implement
		return "<html><head><title>test</title></head><body><h1>test file</h1></body></html>";
	}

	private String getContentType() {// TODO implement
		return "text/html; charset=utf-8";
	}

	public void writeFile(BufferedWriter outWriter) throws IOException {
		put("responseMessage","200 OK");
		String fileContents = readFile();
		outWriter.write("HTTP/1.1 " + get("responseMessage") + "\r\n");
		outWriter.write("Date: " + Utils.getHTTPDate() + "\r\n");
		outWriter.write("Content-Length: " + fileContents.length()+"\r\n");
		outWriter.write("Content-Type: " + getContentType()+"\r\n");
		outWriter.write("Content-Encoding: identity\r\n");
		outWriter.write("Server: " + Server.OrcaVersion + "\r\n");
		outWriter.write("Content-Language: en-US\r\n");
		outWriter.newLine();
		outWriter.write(fileContents);
		outWriter.newLine();
		outWriter.flush();
	}
}
