package org.xyp.demo.nio.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class SimpleThreadedServer {
    public static void main(String[] args) throws IOException {
        ServerSocket ss = new ServerSocket(8080);

        while (true) {
            Socket s = ss.accept();
            new Thread(() -> Util.process(s)).start();
        }
    }

}
