package org.xyp.demo.nio.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class NioServer {
    public static void main(String[] args) throws IOException {

        ServerSocketChannel ssc = ServerSocketChannel.open();
        ssc.bind(new InetSocketAddress("localhost", 8080));
        ThreadPoolExecutor tpe = new ThreadPoolExecutor(8, 12, 10,
            TimeUnit.MINUTES,
            new LinkedBlockingDeque<>());
        while (true) {
            SocketChannel c = ssc.accept();
            tpe.submit(() -> Util.process(c));
        }
    }
}
