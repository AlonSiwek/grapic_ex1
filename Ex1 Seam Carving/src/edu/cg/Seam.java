package edu.cg;

public class Seam {
    private int[][] pixels;
    private int tailPosition;

    public Seam(int length){
        pixels = new int[length][2];
        tailPosition = length - 1;
    }

    private void addPixelToTail(int x, int y){
        pixels[tailPosition][0] = x;
        pixels[tailPosition][1] = y;
        tailPosition --;
    }
}
