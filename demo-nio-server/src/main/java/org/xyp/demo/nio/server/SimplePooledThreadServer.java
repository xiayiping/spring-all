package org.xyp.demo.nio.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.*;

public class SimplePooledThreadServer {
    public static void main(String[] args) throws IOException {
        ServerSocket ss = new ServerSocket(8080);

        ThreadPoolExecutor tpe = new ThreadPoolExecutor(8, 12, 10,
            TimeUnit.MINUTES,
            new LinkedBlockingDeque<>());
        while (true) {
            Socket s = ss.accept();
            tpe.submit(() -> Util.process(s));
        }
    }

}
