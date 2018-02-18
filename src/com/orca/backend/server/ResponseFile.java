package com.orca.backend.server;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class ResponseFile {
	private final String contents, type;
	public ResponseFile(String contents, String type) {
		this.contents = contents;
		this.type = type;
	}

	public String getContents() {
		return contents;
	}

	public String getContentType() {
		return type;
	}

	public static String predictContentType(String fileName) {// TODO implement
		return "text/html; charset=utf-8";
	}

	public static ResponseFile readFromFile(Path in) throws IOException {
		return new ResponseFile(new String(Files.readAllBytes(in)), predictContentType(in.getFileName().toString()));
	}

	public static ResponseFile readFromFile(InputStream in, String type) throws IOException {
		StringBuffer buff = new StringBuffer(1024 * 1024);
		int read = 0;
		while((read = in.read())!=-1){
			buff.append((char)read);
		}
		return new ResponseFile(buff.toString(), type);
	}
	public int getFileLength(){
		return contents.length();
	}
}
