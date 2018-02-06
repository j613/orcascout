package com.orca.backend.server.utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import com.orca.backend.launch.Main;

/**
 * 
 * @author ethanf108 keys:
 * 		   "filePath": file path in TODO CHANGE
 *         "responseMessage": like 200 OK
 *         "method": GET, POST etc
 *         "httpVersion": like HTTP/1.1 in first line of request
 * 
 */
public class HTTPInput extends HashMap<String, String> {
	private boolean inReadRawDataMode = false;
	private String escapeSequence = null; //String that marks the start / end of POST Data
	private final StringBuffer postDataBuffer = new StringBuffer(1024*1024);
	private boolean isFinished;
	public boolean isFinished(){
		return isFinished;
	}
	public void setFile(String fileName) {
		put("filePath", fileName);
	}
	public void setFile(String fileName, String responseCode) {
		put("filePath", fileName);
		put("responseMesage", responseCode);
	}
	public boolean isInPostDataMode(){
		return inReadRawDataMode;
	}
	/**
	 * 
	 * @param s the string to parse
	 * @return true if there was an error
	 */
	public boolean parseString(String s){
		try{
		if(inReadRawDataMode){
			
		}else{
			if(s.matches("(POST|PUT|GET|HEAD) [^ ]+ HTTP/(1\\.1|2)")){
				String[] ins = s.split(" ");
				if(ins[0].matches("(PUT|HEAD")){
					return true;
				}
				put("method",ins[0]);
				put("filePath",ins[1]);
				put("httpVersion",ins[2].split("/")[1]);
			}else if(s.matches("\\w+: \\w+")){
				String field = s.substring(0,s.indexOf(":"));
				put(field,s.substring(s.indexOf(":")+1));
			}else if(s.equals("\n")||s.equals("\r\n")){
				
			}else{
				return true;
			}
		}
		return false;
		}catch(ArrayIndexOutOfBoundsException ex){
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
