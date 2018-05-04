package edu.cg;

// simple class to describe a seam
public class Seam {
    private int[] pixels;
    private int[] seamShifts;
    private int tailPosition;

    public Seam(int length){
        pixels = new int[length];
        seamShifts = new int[length];
        tailPosition = length - 1;
    }

    public void addPixelToTail(int j){
        pixels[tailPosition] = j;
        tailPosition --;
    }

    public void shiftSeam(int i, int shift){
        seamShifts[i] = shift;
    }

    public int getPixCol(int i){
        return pixels[i];
    }

    public int getColShift(int i){
        return seamShifts[i];
    }
}
