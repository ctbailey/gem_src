package gem.ui.board_image;

import static gem.Global.userInterface;

import java.awt.Image;
import java.awt.image.MemoryImageSource;

public abstract class SingleColorCellRenderer extends AbstractStateRenderer {
	
	public SingleColorCellRenderer(float preferredOpacity) {
		super(preferredOpacity);
	}
	
	protected Image createImageFromPixels(int[] pixels, int boardWidth, int boardHeight) {
		MemoryImageSource imageProducer = new MemoryImageSource(boardWidth, boardHeight, pixels, 0, boardWidth);
		Image image = userInterface.boardPanel.createImage(imageProducer);
		return image;
	}
	@Override
	public boolean makesSpurious(IStateRenderer otherRenderer) {
		return (otherRenderer instanceof SingleColorCellRenderer);
	}
}