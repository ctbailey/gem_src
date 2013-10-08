package gem.ui.board_panel.board_image;

import java.awt.Image;

public interface IStateRenderer {
	void setPreferredOpacity(float normalizedPreferredOpacity);
	float getPreferredOpacity();
	
	Image getLatestImage();
	
	// Make sure the image returned by getLatestImage() is up-to-date
	void refreshImage();
	
	// Return true if this state renderer should cause otherRenderer to be removed from the rendering chain
	boolean makesSpurious(IStateRenderer otherRenderer);
	
	// Called if another stateRenderer thinks this renderer should be removed from the rendering chain
	public void wasMadeSpurious(IStateRenderer replacement);
	
	// Add a listener for when the image changes
	void addImageUpdatedListener(IImageUpdatedListener listener);
	
	// Remove a listener for when the image changes
	boolean removeImageUpdatedListener(IImageUpdatedListener listener);
}