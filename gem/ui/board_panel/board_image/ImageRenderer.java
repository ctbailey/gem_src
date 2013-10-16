package gem.ui.board_panel.board_image;

import java.awt.AlphaComposite;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;

import gem.Global;

public class ImageRenderer extends AbstractStateRenderer {
	private static final long serialVersionUID = 1L;
	private BufferedImage image;
	public enum ImageDisplaySettings {
		STRETCH_TO_FIT_BOARD, SCALE_TO_FILL_BOARD
	}
	private ImageDisplaySettings imageDisplaySettings;
	
	public ImageRenderer(BufferedImage i, float preferredOpacity) {
		super(preferredOpacity);
		image = i;
	}
	
	@Override
	public void setPreferredOpacity(float preferredOpacity) {
		super.setPreferredOpacity(preferredOpacity);
		if(image != null) {
			Graphics2D g = (Graphics2D)image.getGraphics();
			g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, getPreferredOpacity()));
		}
	}
	
	@Override
	public Image getLatestImage() {
		if(image != null) {
			Image adjustedImage;
			gem.ui.board_panel.BoardPanel boardPanel = Global.userInterface.boardPanel;
			
			switch(imageDisplaySettings) {
				case SCALE_TO_FILL_BOARD:
					// Calculate the appropriate scale
					double xScale = (double) boardPanel.getWidth()/image.getWidth();
					double yScale = (double) boardPanel.getHeight()/image.getHeight();
					double scale = Math.max(xScale,yScale);
					
					int imageWidth = (int) (image.getWidth()*scale);
					int imageHeight = (int) (image.getHeight()*scale);
					int x = (boardPanel.getWidth() - imageWidth)/2;
					int y = (boardPanel.getHeight() - imageHeight)/2;
					adjustedImage = new BufferedImage(boardPanel.getWidth(), boardPanel.getHeight(), BufferedImage.TYPE_INT_ARGB);
					Graphics g = adjustedImage.getGraphics();
					g.drawImage(image,
						x,y, // points of origin
						imageWidth, imageHeight, // dimensions of the image
						null); // the image observer
					break;
					
				default: // STRETCH_TO_FIT_BOARD
					adjustedImage = image.getScaledInstance(boardPanel.getWidth(), boardPanel.getHeight(), 0);
					break;
			}
						
			return adjustedImage;
		} else {
			return new BufferedImage(Global.userInterface.boardPanel.getWidth(),Global.userInterface.boardPanel.getHeight(),BufferedImage.TYPE_INT_ARGB);
		}
	}

	public void setImage(BufferedImage i) {
		image = i;
	}
	public BufferedImage getImage() {
		return image;
	}
	public void setStretchOrScale(ImageDisplaySettings stretchOrScale) {
		imageDisplaySettings = stretchOrScale;
	}
	public ImageDisplaySettings getImageDisplaySettings() {
		return imageDisplaySettings;
	}
	
	@Override
	public boolean makesSpurious(IStateRenderer otherRenderer) {
		return false;
	}
	
	public void wasMadeSpurious(IStateRenderer replacement) {
		image = null;
	}
	
	@Override
	public void refreshImage() {
		setLatestImage(image);
	}
}