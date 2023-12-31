package org.xyp.demo.api;

import java.util.Optional;

public class WordPoi {

    public static void main(String[] args) throws InterruptedException {
        Optional.of(1)
            .map(i -> i + 1)
            .flatMap(i -> Optional.of("" + i));

        var at = new Thread();
        at.start();
        at.join();
    }

}
