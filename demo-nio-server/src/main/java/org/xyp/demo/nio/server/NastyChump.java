package org.xyp.demo.nio.server;

import lombok.val;

import java.io.IOException;
import java.net.Socket;

public class NastyChump {
    public static void main(String[] args) {
        for (int i = 0; i < 3000; i++) {
            try {
                val socket = new Socket("localhost", 8080);
                System.out.println("------- " + i);
//                socket.getOutputStream().write((byte) 's');
//                System.out.println(socket.getInputStream().read());
//
//                new Thread(() -> {
//                    try {
//                        Thread.sleep(13000);
//                        socket.close();
//                    } catch (Exception e) {
//                    } finally {
//                    }
//                }).start();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

}
