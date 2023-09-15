package com.example.image;

import lombok.val;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.Raster;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import static java.awt.image.BufferedImage.TYPE_INT_ARGB_PRE;

public class ImageApplication {

    public static void main(String[] args) throws IOException {
        System.out.println("0000003");
        readImage("test.jpg");
    }

    public static void readImage(String name) throws IOException {
        BufferedImage image = ImageIO.read(Path.of("d:", name).toUri().toURL());
        val width = image.getWidth();
        val height = image.getHeight();
        val raster = image.getData();
        val pixels = ((DataBufferByte) raster.getDataBuffer()).getData();
        val graphics = image.getGraphics();
        if (graphics instanceof Graphics2D graphics2D) {
            System.out.println("2d " + graphics2D);
            System.out.println(pixels.length);
        } else {
            System.out.println(graphics);
        }

        val hasAlpha = image.getAlphaRaster() != null;
        System.out.println("has alpha raster? " + hasAlpha);
        val pixels2D = convert2DPixels(width, height, pixels, hasAlpha);
        write(pixels2D);
        System.out.println("finished!!!!");
    }

    public static int[][] convert2DPixels(int width, int height, byte[] pixels, boolean hasAlphaChannel) {
        System.out.println("width " + width + " | height " + height);
        int[][] result = new int[height][width];
        final int pixelStepLen = hasAlphaChannel ? 4 : 3;
        System.out.println("image pixels total length : " + pixels.length);
        val totalSteps = pixels.length / pixelStepLen;
        if (pixels.length % pixelStepLen != 0) {
            throw new RuntimeException("pixel length is not divided by step length "
                    + pixels.length + " / " + pixelStepLen);
        }

        int offset = hasAlphaChannel ? 0 : -1;
        int defaultAlpha = 0x0F000000; /*255 alpha == FF 00 00 00 == -16777216*/

        for (int pixelPos = 0, step = 0, row = 0, col = 0;
             step < totalSteps;
             pixelPos += pixelStepLen, step++) {

            int argb = hasAlphaChannel ?
                    ((int) pixels[pixelPos] & 0xff) << 24 :
                    defaultAlpha;

            argb += ((int) pixels[pixelPos + offset + 1] & 0xff); // blue
            argb += (((int) pixels[pixelPos + offset + 2] & 0xff) << 8); // green
            argb += (((int) pixels[pixelPos + offset + 3] & 0xff) << 16); // red

//            System.out.println("read rgb: " +
//                    (pixels[pixelPos + offset + 3] & 0xff) + " " +
//                    (pixels[pixelPos + offset + 2] & 0xff) + " " +
//                    (pixels[pixelPos + offset + 1] & 0xff) +
//                    " at row,col " + row + "," + col);

            result[row][col] = argb;
            col++;
            if (col == width) {
                col = 0;
                row++;
            }
        }
        return result;
    }

    public static void write(int[][] img) {

        System.out.println(img.length);
        System.out.println(img[0].length);

        int height = img.length;
        int width = img[0].length;

        //java.awt.image.BufferedImage:
        BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

//        int idx = 0;
        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                int rgbPixel = img[row][col];
                bi.setRGB(col, row, rgbPixel);
                if (row % 20 == 0 && col % 20 == 0) {
//                    System.out.println("draw rgb " + coord1 + " " + coord2 + " " + rgbPixel);
                }
            }
        }

        File f = new File("d:/myImage.png");
        if (f.exists()) {
            f.delete();
        }
        try {
            // javax.imageio.ImageIO:
            ImageIO.write(bi, "png", new File("d:/myImage.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }


        System.out.println(f.exists());

    }
}
