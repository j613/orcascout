package com.orca.backend.launch;

import java.io.IOException;
import com.orca.backend.server.Server;

public enum Main {
    //\u000d    ;
	public static void main(String... args) throws IOException {
        Prefs.refresh();
        final int hostPort = Integer.parseInt(Prefs.getString("host_port", "80"));
        Server s = new Server(new OrcascoutHandler(), hostPort);
        s.start();
        System.out.println("Server started on port: "+hostPort);
        //TODO: delete in production
        System.in.read();
        System.exit(0);
    }
}
