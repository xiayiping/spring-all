package org.xyp.demo.nio.server;

import lombok.val;

import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;

public class NonblockingSingleThreadSelectorServer {
    public static Collection<SocketChannel> sockets = Collections.newSetFromMap(
        new HashMap<SocketChannel, Boolean>()
    );

    public static void main(String[] args) throws Exception {
        ServerSocketChannel ssc = ServerSocketChannel.open();
        ssc.bind(new InetSocketAddress("localhost", 8080));
        ssc.configureBlocking(false);
        Selector selector1 = Selector.open();
        Selector selector2 = Selector.open();
        val key1 = ssc.register(selector1, SelectionKey.OP_ACCEPT);
        val key2 = ssc.register(selector2, SelectionKey.OP_ACCEPT);

        while (!Thread.currentThread().isInterrupted()) {
            System.out.println("before select1");
            selector1.select();
            System.out.println("before select2");
            selector2.select();
            System.out.println("after select2");

            for (SelectionKey key : selector1.selectedKeys()) {
                if (key.isValid() && key.isAcceptable()) {
                    System.out.println("key1 " + System.identityHashCode(key)
                        + " " + System.identityHashCode(key1)
                        + " " + System.identityHashCode(key2));
                    accept(key, selector1);
                }
            }
            for (SelectionKey key : selector2.selectedKeys()) {

                if (key.isValid() && key.isAcceptable()) {
                    System.out.println("key2 " + System.identityHashCode(key)
                        + " " + System.identityHashCode(key1)
                        + " " + System.identityHashCode(key2));
                    accept(key, selector2);
                }
            }

//
//
//            SocketChannel s = ssc.accept();
//            if (null != s) {
//                System.out.println("connection from " + s);
//                s.configureBlocking(false);
//                sockets.add(s);
//            }
//
//            for (Iterator<SocketChannel> it = sockets.iterator(); it.hasNext(); ) {
//                SocketChannel socket = it.next();
//                try {
//                    ByteBuffer buf = ByteBuffer.allocateDirect(1024);
//                    int read = socket.read(buf);
//                    if (-1 == read) {
//                        System.out.println("remove socket " + socket);
//                        it.remove();
//                        socket.close();
//                    } else if (read != 0) {
//                        buf.flip();
//                        for (int i = 0; i < buf.limit(); i++) {
//                            buf.put(i, (byte) Util.transmogrify(buf.get(i)));
//                        }
//                        socket.write(buf);
//                        buf.clear();
//                    }
//                } catch (IOException e) {
//                    System.err.println("connection problem " + e.getMessage());
//                }
//            }
        }
    }

    private static void accept(SelectionKey key, Selector selector) throws Exception {
        // because it's a accept, so has to be ServerSocketChannel......
        ServerSocketChannel ssc = (ServerSocketChannel) key.channel();
        System.out.println("channel " + ssc);
//        SocketChannel socketChannel = ssc.accept(); // this is non-blocking;
//        // because it's selected, so must have something, will not return null.
//
//        socketChannel.configureBlocking(false);
//        socketChannel.register(key.selector(), SelectionKey.OP_READ);
//        sockets.add(socketChannel);
    }
}
