package edu.cg;

import java.awt.*;
import java.awt.image.BufferedImage;

public class SeamsCarver extends ImageProcessor {

	//MARK: An inner interface for functional programming.
	@FunctionalInterface
	interface ResizeOperation {
		BufferedImage apply();
	}

	//MARK: Fields
	private int numOfSeams;
	private ResizeOperation resizeOp;
	private long[][] energyMatrix;
	private long[][] costMatrix;
	private int[][] imageMatrix;
	private char[][] trackingtMatrix;
	private int workWidth;


	//MARK: Constructor
	public SeamsCarver(Logger logger, BufferedImage workingImage,
					   int outWidth, RGBWeights rgbWeights) {
		super(logger, workingImage, rgbWeights, outWidth, workingImage.getHeight());

		numOfSeams = Math.abs(outWidth - inWidth);

		if(inWidth < 2 | inHeight < 2)
			throw new RuntimeException("Can not apply seam carving: workingImage is too small");

		if(numOfSeams > inWidth/2)
			throw new RuntimeException("Can not apply seam carving: too many seams...");

		//Sets resizeOp with an appropriate method reference
		if(outWidth > inWidth)
			resizeOp = this::increaseImageWidth;
		else if(outWidth < inWidth)
			resizeOp = this::reduceImageWidth;
		else
			resizeOp = this::duplicateWorkingImage;

		//TODO: Initialize your additional fields and apply some preliminary calculations:
		workWidth = inWidth;
		initialCalculations();
	}

	private void initialCalculations() {
		BufferedImage mag = gradientMagnitude();
		energyMatrix = new long[inHeight][workWidth];
		imageMatrix = new int[inHeight][workWidth];

		for (int i = 0; i < inHeight; i++) {
			for (int j = 0; j < workWidth; j++) {
				Color pix = new Color(mag.getRGB(j, i));
				energyMatrix[i][j] = pix.getBlue();
				imageMatrix[i][j] = workingImage.getRGB(j, i);
			}
		}
	}

	private void calculateCostMatrix() {
		costMatrix = new long[inHeight][workWidth];
		trackingtMatrix = new char[inHeight][workWidth];

		long min, left, right, up;

//		 IMPLEMENT FORWARD LOOKING //
		for (int i = 0; i < inHeight; i++) {
			for (int j = 0; j < workWidth; j++) {

				// first row
				if(i == 0){
					costMatrix[i][j] = energyMatrix[i][j];
					trackingtMatrix[i][j] = 's';
				} else {
					up = costMatrix[i - 1][j] + forwardLookingCost(i,j,'u');
					right = (j < workWidth - 1) ? costMatrix[i - 1][j + 1] + forwardLookingCost(i,j,'r'): Long.MAX_VALUE;
					left = (j > 0) ? costMatrix[i - 1][j - 1] + forwardLookingCost(i,j,'l'): Long.MAX_VALUE;
					min = Math.min(left, Math.min(up, right));
					costMatrix[i][j] = energyMatrix[i][j] + min;

					if(min == left){
						trackingtMatrix[i][j] = 'l';
					} else if (min == up) {
						trackingtMatrix[i][j] = 'u';
					} else {
						trackingtMatrix[i][j] = 'r';
					}
				}
			}
		}
	}

	private long forwardLookingCost(int i, int j, char dir){
		long res = 0;

		if (!(j + 1 < workWidth && j >0) || true){
			return 0;
		}

		res += Math.abs(imageMatrix[i][j + 1] - imageMatrix[i][j - 1]);

		switch (dir){
			case 'l':
				res += Math.abs(imageMatrix[i - 1][j] - imageMatrix[i][j - 1]);
				break;
			case 'r':
				res += Math.abs(imageMatrix[i - 1][j] - imageMatrix[i][j + 1]);
				break;
			default:
				break;
		}

		return res;
	}

	private Seam findMinSeam(){
		Seam seam = new Seam(inHeight);
		int col = 0;
		long min = Long.MAX_VALUE;
		for (int j = 0; j < workWidth; j++) {
			if(costMatrix[inHeight - 1][j] < min){
				min = costMatrix[inHeight - 1][j];
				col = j;
			}
		}

//		System.out.println("Found min: " + min + " at index: " + col);

		// backtrack
		for (int i = inHeight - 1; i > 0; i--) {
			seam.addPixelToTail(col);
			switch (trackingtMatrix[i][col]){
				case 'l':
					col =  col - 1;
					break;
				case 'r':
					col = col + 1;
					break;
				case 's':
					break;
				default:
					break;
			}
		}
		return seam;
	}

	private void removeSeam(Seam seam){

		workWidth--;
		long [][] tmpEnergyMatrix = new long[inHeight][workWidth];
		int[][] tmpImageMatrix = new int[inHeight][workWidth];
		int shift, shiftCol;

		for (int i = 0; i < inHeight; i++) {
			shift = 0;
			shiftCol = seam.getPixCol(i);
			for (int j = 0; j < workWidth; j++) {
				if(j == shiftCol){
					shift = 1;
				}
				tmpImageMatrix[i][j] = imageMatrix[i][j + shift];
				tmpEnergyMatrix[i][j] = energyMatrix[i][j + shift];
			}
		}

		imageMatrix = tmpImageMatrix;
		energyMatrix = tmpEnergyMatrix;
	}

	//MARK: Methods
	public BufferedImage resize() {
		return resizeOp.apply();
	}

	//MARK: Unimplemented methods
	private BufferedImage reduceImageWidth() {
		for (int i = 0; i < numOfSeams; i++) {
//			initEnergyMatrix();
			calculateCostMatrix();
			Seam seam = findMinSeam();
			removeSeam(seam);
		}

		BufferedImage outImage =  newEmptyOutputSizedImage();
		for (int i = 0; i < inHeight; i++) {
			for (int j = 0; j < workWidth; j++) {
				outImage.setRGB(j,i, imageMatrix[i][j]);
			}
		}

		return outImage;
	}

	private BufferedImage increaseImageWidth() {
		Seam[] seams = new Seam[numOfSeams];
		int shift;

		for (int i = 0; i < numOfSeams; i++) {
//			initEnergyMatrix();
			calculateCostMatrix();
			Seam seam = findMinSeam();
			seams[i] = seam;
			removeSeam(seam);
		}

		// convert seams to original positions
		for (int i = 0; i < inHeight; i++) {
			for (int k = 0; k < numOfSeams; k++) {
				shift = 0;
				for (int l = k - 1; l >= 0; l--) {

					//account for previous shifts
					if(seams[k].getPixCol(i) + shift >= seams[l].getPixCol(i)){
						shift ++;
					}
				}
				seams[k].shiftSeam(i, shift);
			}
		}

		BufferedImage outImage = newEmptyOutputSizedImage();

		for (int i = 0; i < inHeight; i++) {
			shift = 0;
//			boolean flag = true;

			for (int j = 0; j < outWidth; j++) {

//				if(j-shift<0 || j -shift > inWidth){
//					System.out.println(" ------------------------------------- row " + i  + " col " + j + " shift " + shift);
//
//				}

				outImage.setRGB(j, i, workingImage.getRGB((j - shift), i ));

				for (int k = 0; k < numOfSeams; k++) {
					if (seams[k].getPixCol(i) + seams[k].getColShift(i) == j ) {
						shift++;
//						System.out.println("row " + i + " col j " + j + " seam " + k +" seamPos " + seams[k].getPixCol(i)  + " shift " + seams[k].getColShift(i));
					}
				}



//				if(shift == numOfSeams && flag) {
////					System.out.println("row " + i  + " col " + j + " shift " + shift);
////					flag =false;
//				}

			}
		}
		return outImage;
	}

	public BufferedImage showSeams(int seamColorRGB) {
		//TODO: Implement this method (bonus), remove the exception.
		throw new UnimplementedMethodException("showSeams");
	}
}
