package gem.ui.board_image;

import java.awt.Component;
import java.awt.Image;
import java.awt.image.MemoryImageSource;

public class PixelWrapper {
	// A wrapper around a 1D array of pixel values that allows classes to set colors using x and y coordinates,
	
	private int[] pixels;
	private int imageWidth;
	private int imageHeight;
	public PixelWrapper(int imageWidth, int imageHeight) {
		pixels = new int[imageWidth*imageHeight];
		this.imageWidth = imageWidth;
		this.imageHeight = imageHeight;
	}
	
	public void setColor(int x, int y, int opacity, int red, int green, int blue) {
		int pixel = (opacity << 24) | (red << 16) | (green << 8) | (blue);
		int index = (y * imageWidth) + x;
		pixels[index] = pixel;
	}
	public void setColor(int x, int y, float normalizedOpacity, int red, int green, int blue) {
		int opacity = (int)(normalizedOpacity * 255.0f);
		setColor(x, y, opacity, red, green, blue);
	}
	public int getImageWidth() {
		return imageWidth;
	}
	public int getImageHeight() {
		return imageHeight;
	}
	public int[] getPixels() {
		return pixels;
	}
	public Image toImage(Component imageCreator, int widthInCells, int heightInCells) {
		MemoryImageSource imageProducer = new MemoryImageSource(widthInCells, heightInCells, pixels, 0, widthInCells);
		Image image = imageCreator.createImage(imageProducer);
		return image;
	}
	public static Image blankImage(int imageWidth, int imageHeight, Component imageCreator, int widthInCells, int heightInCells) {
		return new PixelWrapper(
				imageWidth, 
				imageHeight).toImage(imageCreator, widthInCells, heightInCells);
	}
}