package gem.ui.board_panel.board_image;

import gem.Global;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public abstract class AbstractStateRenderer implements IStateRenderer {
	private List<IImageUpdatedListener> imageUpdatedListeners;
	private Image latestImage;
	private float preferredOpacity = 1.0f;

	public AbstractStateRenderer(float preferredOpacity) {
		setPreferredOpacity(preferredOpacity);
		imageUpdatedListeners = new ArrayList<IImageUpdatedListener>();
	}
	
	public void setPreferredOpacity(float opacity) {
		if(preferredOpacity < 0 || preferredOpacity > 1) {
			throw new IllegalArgumentException("Normalized opacity not between 0 and 1.");
		}
		preferredOpacity = opacity;
	}
	public float getPreferredOpacity() {
		return preferredOpacity;
	}
	
	protected void setLatestImage(Image newImage) {
		latestImage = newImage;
		notifyImageUpdatedListeners();
	}
	public Image getLatestImage() {
		if(latestImage == null) {
			return new BufferedImage(Global.userInterface.boardPanel.getWidth(), Global.userInterface.boardPanel.getHeight(), BufferedImage.TYPE_INT_ARGB);
		} else {
			return latestImage;
		}
	}	
	public void addImageUpdatedListener(IImageUpdatedListener listener) {
		imageUpdatedListeners.add(listener);
	}
	public boolean removeImageUpdatedListener(IImageUpdatedListener listener) {
		return imageUpdatedListeners.remove(listener);
	}
	private void notifyImageUpdatedListeners() {
		for(IImageUpdatedListener listener : imageUpdatedListeners) {
			listener.requestImageUpdate();
		}
	}
}