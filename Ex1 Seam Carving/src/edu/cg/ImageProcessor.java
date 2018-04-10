package edu.cg;

import java.awt.Color;
import java.awt.image.BufferedImage;

public class ImageProcessor extends FunctioalForEachLoops {
	
	//MARK: Fields
	public final Logger logger;
	public final BufferedImage workingImage;
	public final RGBWeights rgbWeights;
	public final int inWidth;
	public final int inHeight;
	public final int workingImageType;
	public final int outWidth;
	public final int outHeight;
	
	//MARK: Constructors
	public ImageProcessor(Logger logger, BufferedImage workingImage,
			RGBWeights rgbWeights, int outWidth, int outHeight) {
		super(); //Initializing for each loops...
		
		this.logger = logger;
		this.workingImage = workingImage;
		this.rgbWeights = rgbWeights;
		inWidth = workingImage.getWidth();
		inHeight = workingImage.getHeight();
		workingImageType = workingImage.getType();
		this.outWidth = outWidth;
		this.outHeight = outHeight;
		setForEachInputParameters();
	}
	
	public ImageProcessor(Logger logger,
			BufferedImage workingImage,
			RGBWeights rgbWeights) {
		this(logger, workingImage, rgbWeights,
				workingImage.getWidth(), workingImage.getHeight());
	}
	
	//MARK: Change picture hue - example
	public BufferedImage changeHue() {
		logger.log("Prepareing for hue changing...");
		
		int r = rgbWeights.redWeight;
		int g = rgbWeights.greenWeight;
		int b = rgbWeights.blueWeight;
		int max = rgbWeights.maxWeight;
		
		BufferedImage ans = newEmptyInputSizedImage();
		
		forEach((y, x) -> {
			Color c = new Color(workingImage.getRGB(x, y));
			int red = r*c.getRed() / max;
			int green = g*c.getGreen() / max;
			int blue = b*c.getBlue() / max;
			Color color = new Color(red, green, blue);
			ans.setRGB(x, y, color.getRGB());
		});
		
		logger.log("Changing hue done!");
		
		return ans;
	}
	
	
	//MARK: Unimplemented methods
	public BufferedImage greyscale() {
		logger.log("Prepareing for greyscale changing...");

		int r = rgbWeights.redWeight;
		int g = rgbWeights.greenWeight;
		int b = rgbWeights.blueWeight;
		int max = rgbWeights.maxWeight;

		BufferedImage ans = newEmptyInputSizedImage();

		forEach((y, x) -> {
			Color c = new Color(workingImage.getRGB(x, y));
			int red = r * c.getRed();
			int green = g * c.getGreen();
			int blue = b * c.getBlue();
			int grey = (red + green + blue) / (r + g + b);
			Color color = new Color(grey, grey, grey);
			ans.setRGB(x, y, color.getRGB());
		});

		logger.log("Changing greyscale done!");

		return ans;

	}

	public BufferedImage gradientMagnitude() {
		logger.log("Prepareing for greyscale changing...");
		//In case the image dimenssions are too small
		if (inWidth < 2)
			throw new IllegalArgumentException("Width value is too small - Change the value");
		if (inHeight < 2)
			throw new IllegalArgumentException("Height value is too small - Change the value");

		BufferedImage ans = newEmptyInputSizedImage();
		BufferedImage greyImg = greyscale();

		forEach((y, x) -> {
			int nextXpix = x + 1;
			int nextYpix = y + 1;

			if (nextXpix == inWidth)
				nextXpix = x - 1;
			else
				nextXpix = x + 1;
			if (nextYpix == inHeight)
				nextYpix = y - 1;
			else
				nextYpix = y + 1;

			Color currPixGrey = new Color(greyImg.getRGB(x, y));
			Color nextXpixGrey = new Color(greyImg.getRGB(nextXpix, y));
			Color nextYpixGrey = new Color(greyImg.getRGB(x, nextYpix));

			double dxRed = Math.pow(currPixGrey.getRed() - nextXpixGrey.getRed(), 2);
			double dyRed = Math.pow(currPixGrey.getRed() - nextYpixGrey.getRed(), 2);
			int magRed = (int) Math.sqrt((dxRed + dyRed) / 2);

			double dxGreen = Math.pow(currPixGrey.getGreen() - nextXpixGrey.getGreen(), 2);
			double dyGreen = Math.pow(currPixGrey.getGreen() - nextYpixGrey.getGreen(), 2);
			int magGreen = (int) Math.sqrt((dxGreen + dyGreen) / 2);

			double dxBlue = Math.pow(currPixGrey.getBlue() - nextXpixGrey.getBlue(), 2);
			double dyBlue = Math.pow(currPixGrey.getBlue() - nextYpixGrey.getBlue(), 2);
			int magBlue = (int) Math.sqrt((dxBlue + dyBlue) / 2);

			Color cMag = new Color(magRed, magGreen, magBlue);
			ans.setRGB(x, y, cMag.getRGB());
		});


		logger.log("Changing greyscale done!");
		return  ans;
	}
	
	public BufferedImage nearestNeighbor() {
		logger.log("Prepareing for nearest neighbor changing...");

		//the relation between in and out image
		double newX = inWidth / (double)(outWidth + 1);
		double newY = inHeight / (double)(outHeight + 1);
		BufferedImage ans = newEmptyOutputSizedImage();
		setForEachOutputParameters();

		forEach((y, x) -> {
			int x_nearestNeighbor = (int) Math.round(x * newX);
			int y_nearestNeighbor = (int) Math.round(y * newY);

			if (x_nearestNeighbor > inWidth - 1)
				x_nearestNeighbor = inWidth - 1;
			if (y_nearestNeighbor > inHeight - 1)
				y_nearestNeighbor = inHeight - 1;

			Color color = new Color(workingImage.getRGB(x_nearestNeighbor, y_nearestNeighbor));

			ans.setRGB(x, y, color.getRGB());
		});


		logger.log("Changing nearest neighbor done!");
		return ans;


	}
	
	public BufferedImage bilinear() {
		//TODO: Implement this method, remove the exception.
		throw new UnimplementedMethodException("bilinear");
	}
	
	
	//MARK: Utilities
	public final void setForEachInputParameters() {
		setForEachParameters(inWidth, inHeight);
	}
	
	public final void setForEachOutputParameters() {
		setForEachParameters(outWidth, outHeight);
	}
	
	public final BufferedImage newEmptyInputSizedImage() {
		return newEmptyImage(inWidth, inHeight);
	}
	
	public final BufferedImage newEmptyOutputSizedImage() {
		return newEmptyImage(outWidth, outHeight);
	}
	
	public final BufferedImage newEmptyImage(int width, int height) {
		return new BufferedImage(width, height, workingImageType);
	}
	
	public final BufferedImage duplicateWorkingImage() {
		BufferedImage output = newEmptyInputSizedImage();
		
		forEach((y, x) -> 
			output.setRGB(x, y, workingImage.getRGB(x, y))
		);
		
		return output;
	}
}
