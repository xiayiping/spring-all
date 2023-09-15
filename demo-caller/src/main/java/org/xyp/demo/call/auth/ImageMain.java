package org.xyp.demo.call.auth;

import lombok.val;
import org.apache.http.util.Asserts;
import org.springframework.util.Assert;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

public class ImageMain {
    public static void main(String[] args) throws IOException {

        File input = Path.of("d:", "sss.png").toFile();
        File output = Path.of("d:", "abc3.png").toFile();
        BufferedImage bufferedImage = ImageIO.read(input);
        int width = bufferedImage.getWidth();
        int height = bufferedImage.getHeight();

        boolean alpha = bufferedImage.getAlphaRaster() != null;

        int step = 3;
        if (alpha) step = 4;

        Raster raster = bufferedImage.getData();
        System.out.println(width * height);
        val pixels = bufferedImage.getData().getPixels(0, 0, width, height,
            new int[width * height * step]);

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        val pixels2D = new int[height][width];

        int idx = 0;
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                int offset = alpha ? 0 : -1;

                var ok1 = pixels[idx + 1 + offset]; // expect red should << 16
                var k1 = ok1 << 16;

                var ok2 = pixels[idx + 2 + offset]; // expect green should << 8
                var k2 = ok2 << 8;

                var ok3 = pixels[idx + 3 + offset]; // expect blue should << 0
                var k3 = ok3 << 0;

                var mask = alpha ? pixels[idx] : 0xff000000;

                val nk = mask
                    + k1
                    + k2
                    + k3
                    /**/;

                pixels2D[i][j] = nk;
                idx += step;
            }
        }
        int[][] transfered = transfer(pixels2D);
        Color backgroundColor = Color.WHITE;
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                image.setRGB(j, i, transfered[i][j]);
            }
        }

        ImageIO.write(image, "PNG", output);
        System.out.println(output.exists());
        System.out.println(output);
    }

    public static int[][] transfer(int[][] pixels2D) {
        int height = pixels2D.length;
        int width = pixels2D[0].length;
        int[][] result = new int[height][width];
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                val halfStep = 1;
//                System.out.transfer2println("r");
                val r = transfer2(pixels2D, i, j, halfStep, height, width, 0x00ff0000);
//                System.out.println("g");
//                val g = transfer(pixels2D, i, j, halfStep, height, width, 0x0000ff00);
//                System.out.println("b");
//                val b = transfer(pixels2D, i, j, halfStep, height, width, 0x000000ff);
//                val tmp = r | g | b;
//                Assert.isTrue(tmp < 0x01000000, "should not occupy mask bits");

                result[i][j] = 0xBF000000
                    | r
                    | r >> 8
                    | r >> 16
                /**/
                ;
            }
        }
        return result;
    }

    public static int transfer(int[][] input, int ci, int cj, int halfStep,
                               int height, int width, int mask) {
        val tailZeros = Integer.numberOfTrailingZeros(mask);
        var r = 0;
        for (int i = ci - halfStep; i <= ci + halfStep; i++) {
            for (int j = cj - halfStep; j <= cj + halfStep; j++) {

                int tmp = (i < 0 || j < 0 || i >= height || j >= width) ?
                    0 : (input[i][j] & mask) >> tailZeros;
                r += tmp;
            }
        }

        val rTemp = (r / (halfStep + 1) / (halfStep + 1));
        return Math.min(rTemp, 255) << tailZeros;
    }

    public static int transfer1(int[][] input, int ci, int cj, int halfStep,
                                int height, int width, int mask) {
        val tailZeros = Integer.numberOfTrailingZeros(mask);
        double r = 0;
        for (int i = ci - halfStep; i <= ci + halfStep; i++) {
            for (int j = cj - halfStep; j <= cj + halfStep; j++) {

                double tmp = (i < 0 || j < 0 || i >= height || j >= width) ?
                    0 : (input[i][j] & mask) >> tailZeros;

                double gs = (ci - i > gsHeight
                    || cj - j > gsWidth
                    || i - ci > gsHeight
                    || j - cj > gsWidth) ?
                    0D : gaussianMatrix[i - ci + gsHeight][j - cj + gsWidth];
                r += tmp * gs;
            }
        }

        return ((int) r) << tailZeros;
    }

    double[] db = new double[]{1, 2, 3};


    static int gsHeight = 2;
    static int gsWidth = 2;
    static double[][] gaussianMatrix = new double[][]{
        {
            0.01, 0.01, 0.01, 0.01, 0.01
        },
        {
            0.01, 0.05, 0.05, 0.05, 0.01
        },
        {
            0.01, 0.05, 0.43, 0.05, 0.01
        },
        {
            0.01, 0.05, 0.05, 0.05, 0.01
        },
        {
            0.01, 0.01, 0.10, 0.01, 0.01
        }
        /////
    };


    public static int transfer2(int[][] input, int i, int j, int halfStep,
                                int height, int width, int mask) {
        val tailZeros = Integer.numberOfTrailingZeros(mask);
        double r = 0;
        val x0 = (j == 0 ? 0 : input[i][j - 1]) & mask >> tailZeros;
        val x1 = (input[i][j]) & mask >> tailZeros;
        val x2 = (j == width - 1 ? 0 : input[i][j + 1]) & mask >> tailZeros;

        val y0 = (i == 0 ? 0 : input[i - 1][j]) & mask >> tailZeros;
        val y1 = (input[i][j]) & mask >> tailZeros;
        val y2 = (i == height - 1 ? 0 : input[i + 1][j]) & mask >> tailZeros;

        val xd = ((float) (x2 - x1 - x1 + x0)) / 2;
        val yd = ((float) (y2 - y1 - y1 + y0)) / 2;

        r = ((int) (xd + yd)) ;
        r = ~((int)Math.max(0, r)) & 0xff;
        System.out.println(xd + "    " + yd + "    " + r);

        return ((int) r) << tailZeros;
    }
}
