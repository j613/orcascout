package com.orca.backend.launch;

import java.io.IOException;
import com.orca.backend.server.Server;

public enum Main {
    ;
	public static void main(String... args) throws IOException {
        Server s = new Server(new OrcascoutHandler(), 80);
        s.start();
        System.out.println("STARTED");
        System.in.read();
        System.exit(0);
    }
}
