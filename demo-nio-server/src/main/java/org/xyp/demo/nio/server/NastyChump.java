package org.xyp.demo.nio.server;

import java.io.IOException;
import java.net.Socket;

public class NastyChump {
    public static void main(String[] args) {
        for (int i = 0; i < 3000; i++) {
            try {
                new Socket("localhost", 8777);
                System.out.println(i);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
