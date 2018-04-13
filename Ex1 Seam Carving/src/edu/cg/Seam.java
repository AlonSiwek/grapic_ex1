package edu.cg;

public class Seam {
    private int[] pixels;
    private int tailPosition;

    public Seam(int length){
        pixels = new int[length];
        tailPosition = length - 1;
    }

    public void addPixelToTail(int j){
        pixels[tailPosition] = j;
        tailPosition --;
    }

    public int getPixCol(int i){
        return pixels[i];
    }

    public void shiftSeam(int i, int k){
        pixels[i] += k;
    }
}
