package org.xyp.demo.nio.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

public class NonblockingSingleThreadSelectorServer {

    private static final String POISON_PILL = "POISON_PILL";

    static volatile int run = 1;

    public static void main(String[] args) throws IOException, InterruptedException {
        try (Selector selector = Selector.open();
             ServerSocketChannel serverSocket = ServerSocketChannel.open()) {
            serverSocket.bind(new InetSocketAddress("localhost", 5454));
            serverSocket.configureBlocking(false);
            serverSocket.register(selector, SelectionKey.OP_ACCEPT);
            ByteBuffer buffer = ByteBuffer.allocate(4096);

            var thread = new Thread(() -> {
                try {
                    while (run > 0) {
                        selector.select();
                        Set<SelectionKey> selectedKeys = selector.selectedKeys();
                        Iterator<SelectionKey> iter = selectedKeys.iterator();
                        for (SelectionKey key : selectedKeys) {
//                        while (iter.hasNext()) {
//                            SelectionKey key = iter.next();
//
//
                            if (key.isAcceptable()) {
                                System.out.println("!!!! acceptable !!!!");
                                register(selector, serverSocket);
                            }

                            if (key.isReadable()) {
                                System.out.println("!!!! readable !!!!");
                                answerWithEcho(buffer, key);
                            }

//                            iter.remove();
                        }
                        selectedKeys.clear();
                    }
                    Thread.yield();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            thread.start();
            thread.join();
        }
    }

    private static void write(SelectionKey key)
        throws IOException, InterruptedException {

        ByteBuffer buffer = ByteBuffer.allocate(256);
        try (SocketChannel client = (SocketChannel) key.channel()) {
            Thread.sleep(2000);
            buffer.put("abc hello\n".getBytes());
            buffer.flip();
            client.write(buffer);
            buffer.clear();
        }
        System.out.println("write done");
    }

    private static void answerWithEcho(ByteBuffer buffer, SelectionKey key)
        throws IOException {

        SocketChannel client = (SocketChannel) key.channel();
        try {
            int r = client.read(buffer);
            if (r == -1 || new String(buffer.array()).trim().equals(POISON_PILL)) {
                System.out.println("Not accepting client messages anymore");
            } else {
                buffer.flip();
                final var arr = buffer.array();
                String s = new String(arr, 0, r);
                System.out.println(s);
                System.out.println("-----");
                System.out.println("-----");
                String response = "HTTP/1.1 200 OK\r\n" +
                    "Content-Type: text/plain\r\n" +
                    "Content-Length: 12\r\n" +
                    "\r\n" +
                    "Hello, World";
                client.write(ByteBuffer.wrap(response.getBytes()));
//                    client.write(buffer);

                buffer.clear();
            }
        } catch (Exception e) {
//            client.close();
//            e.printStackTrace();
        } finally {
            client.close();
        }
    }

    private static void register(Selector selector, ServerSocketChannel serverSocket)
        throws IOException {

        SocketChannel client = serverSocket.accept();
        client.configureBlocking(false);
        client.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE);
    }
}
