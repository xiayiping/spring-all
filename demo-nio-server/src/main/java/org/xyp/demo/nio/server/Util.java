package org.xyp.demo.nio.server;

import lombok.val;

import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class Util {
    public static int transmogrify(int data) {
        if (Character.isLetter(data)) {
            return data ^ ' ';
        }
        return data;
    }

    static void process(Socket s) {
        System.out.println("accept connection from " + s + " " + Thread.currentThread().getName());
        try (val inputStream = s.getInputStream();
             val outputStream = s.getOutputStream()) {

            int data;
            while ((data = inputStream.read()) != -1) {
                data = transmogrify(data);
                outputStream.write(data);

            }
        } catch (Exception e) {
            System.out.println("problem : " + e + " " + Thread.currentThread().getName());
        }
    }

    static void process(SocketChannel s) {
        System.out.println("accept connection from " + s + " " + Thread.currentThread().getName());
        try {
            ByteBuffer buffer = ByteBuffer.allocate(1024);
            while (s.read(buffer) != -1) {
                buffer.flip();  // same as:  buffer.limit(buffer.position()).position(0);
                for (int i = 0; i < buffer.limit(); i++) {
                    buffer.put(i, (byte) transmogrify(buffer.get(i)));
                }
                s.write(buffer);
                buffer.clear();
            }
        } catch (Exception e) {
            System.out.println("problem : " + e + " " + Thread.currentThread().getName());
        }
    }
}
