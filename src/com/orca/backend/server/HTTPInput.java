package com.orca.backend.server;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.HashMap;

/**
 * 
 * @author ethanf108 keys:
 * 		"method": get pot etc
 * 		"filePath": path requested
 * 		"httpVersion": HTTP/1.1 etc
 * 		everything else is what is sent
 * 		example: "Date: 2 june 2018" -> "Date":"2 june 2018" (value is trimmed)
 * 
 */
public class HTTPInput extends HashMap<String, String> {
	private boolean inReadRawDataMode = false;
	private String escapeSequence = null; // String that marks the start / end
											// of POST Data
	private boolean hasError = false;
	private StringBuffer postDataBuffer = null;
	private boolean isFinished;

	public boolean isFinished() {
		return isFinished;
	}
	public String getRequestedFile(){
		return get("filePath"); //TODO: not important but if the client sends a request with the filePath header, it overwrites the GET/POST FIle
	}

	public void setError(boolean b){
		hasError = b;
	}
	public boolean hasError() {
		return hasError;
	}
	public String getRawPostData(){
		return postDataBuffer==null?null:postDataBuffer.toString();
	}
	public String getActualPostData(){
		String data = getRawPostData();
		if(data==null)return null;
		String WKBound = data.split("\r?\n")[0];
		String before = data.split(WKBound)[1];
		return before.replaceAll(before.split("\r?\n\r?\n")[0], "");
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
				int clength = Integer.parseInt(getOrDefault("Content-Length","0"));
				if(!containsKey("Content-Length")){
					System.out.println("POST, but no content length");
					hasError = true;
					return true;
				}
				if(clength>1024*1024){
					System.out.println("Content-Legnth too big");
					hasError = true;
					return true;
				}
				if(postDataBuffer==null){
					postDataBuffer = new StringBuffer(clength);
				}
				postDataBuffer.append(s);
				if(postDataBuffer.length()==clength){
					//System.out.println("Length exceeded Content-Length");
					isFinished = true;
					
				}
			} else {
				if (s.matches("(POST|PUT|GET|HEAD) [^ ]+ HTTP\\/(1\\.1|2|1)\r?\n")) {
					String[] ins = s.split(" ");
					if (ins[0].matches("(PUT|HEAD)")) {
						hasError = true;
						return true;
					}
					put("method", ins[0]);
					put("filePath", ins[1]);
					put("httpVersion", ins[2].split("\\/")[1]);
				} else if (s.matches(".+: .+\r?\n")) {
					String field = s.substring(0, s.indexOf(":"));
					put(field, s.substring(s.indexOf(":") + 1).trim());
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
		} catch (Exception ex) {
			hasError = true;
			return true;
		}
	}

}
