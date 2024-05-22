package org.xyp.demo.nio.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;

public class NonblockingSingleThreadPollingServer {
    public static Collection<SocketChannel> sockets = Collections.newSetFromMap(
        new HashMap<SocketChannel, Boolean>()
    );

    public static void main(String[] args) throws IOException {
        ServerSocketChannel ssc = ServerSocketChannel.open();
        ssc.bind(new InetSocketAddress("localhost", 8080));
        ssc.configureBlocking(false);
        while (!Thread.currentThread().isInterrupted()) {
            SocketChannel s = ssc.accept();

            if (null != s) {
                System.out.println("connection from " + s);
                s.configureBlocking(false);
                sockets.add(s);
            }

            for (Iterator<SocketChannel> it = sockets.iterator(); it.hasNext(); ) {
                SocketChannel socket = it.next();
                try {
                    ByteBuffer buf = ByteBuffer.allocateDirect(1024);
                    int read = socket.read(buf);
                    if (-1 == read) {
                        System.out.println("remove socket " + socket);
                        it.remove();
                        socket.close();
                    } else if (read != 0) {
                        buf.flip();
                        for (int i = 0; i < buf.limit(); i++) {
                            buf.put(i, (byte) Util.transmogrify(buf.get(i)));
                        }
                        socket.write(buf);
                        buf.clear();
                    }
                } catch (IOException e) {
                    System.err.println("connection problem " + e.getMessage());
                }
            }
        }
    }
}
