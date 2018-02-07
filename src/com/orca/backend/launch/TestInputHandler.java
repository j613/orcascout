package com.orca.backend.launch;

import java.io.BufferedWriter;
import java.io.IOException;

import com.orca.backend.server.HTTPInput;
import com.orca.backend.server.InputHandler;

public class TestInputHandler implements InputHandler {

	@Override
	public void handleFile(HTTPInput in, BufferedWriter out) throws IOException {
		in.setFile("/test.html");
		in.writeFile(out);
		System.out.println("SENDING FILE");
	}

}
