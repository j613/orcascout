package com.orca.backend.server;

import java.io.BufferedWriter;
import java.io.IOException;

public interface InputHandler {
	public void handleFile(HTTPInput in, BufferedWriter out) throws IOException;
	
}
