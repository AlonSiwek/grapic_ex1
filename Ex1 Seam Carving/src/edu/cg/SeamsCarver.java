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
		energyMatrix = new long[inWidth][inHeight];
		costMatrix = new long[inWidth][inHeight];
		workWidth = inWidth;
		initEnergyMatrix();
	}

	private void initEnergyMatrix() {
		BufferedImage mag = gradientMagnitude();

		forEach((y, x) -> {
			Color pix = new Color(mag.getRGB(x, y));
			energyMatrix[x][y] = pix.getBlue();
		});
	}

	private void calculateCostMatrix() {

//		 IMPLEMENT FORWARD LOOKING //
			forEach((y, x) -> {
				long left = (((x - 1) > 0) && ((y - 1) > 0)) ? energyMatrix[x - 1][y - 1] : 255;
				long up = ((x - 1) > 0) ? energyMatrix[x - 1][y] : 255;
				long right = (((x - 1) > 0) && ((y + 1) < inHeight)) ? energyMatrix[x - 1][y + 1] : 255;
				costMatrix[x][y] = energyMatrix[x][y] + Math.min(up, Math.min(left, right));
			});
	}

	private Seam findMinSeam(){
		Seam seam = new Seam(inHeight);
		long min = Long.MAX_VALUE;
		for (int i = 0; i < workWidth; i++) {
			if(energyMatrix[i][inHeight] < min){
				min = i;
			}
		}
		return seam;
	}

	private void removeSeam(){

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
			// workingImage = removeSeam(seam)
		}

		BufferedImage image =  newEmptyOutputSizedImage();
		return image;///image;
//		throw new UnimplementedMethodException("reduceImageWidth");
	}

	private BufferedImage increaseImageWidth() {
		//TODO: Implement this method, remove the exception.
		throw new UnimplementedMethodException("increaseImageWidth");
	}

	public BufferedImage showSeams(int seamColorRGB) {
		//TODO: Implement this method (bonus), remove the exception.
		throw new UnimplementedMethodException("showSeams");
	}
}
