package com.orca.backend.launch;

import java.io.IOException;
import com.orca.backend.server.Server;
import com.orca.backend.server.Utils;

public enum Main {
    //\u000d    ;
	public static void main(String... args) throws IOException {
        Prefs.refresh();
        final int hostPort = Prefs.getInt("host_port", 80);
        Server s = new Server(new OrcascoutHandler(), hostPort);
        s.start();
        Utils.logln("Server started on port: "+hostPort);
        //TODO: delete in production
        System.in.read();
        System.exit(0);
    }
}
