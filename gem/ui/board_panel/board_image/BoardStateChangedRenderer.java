package gem.ui.board_panel.board_image;

import gem.Global;
import gem.simulation.board.IBoardStateChangedListener;
import gem.simulation.state.IState;

import java.awt.Image;

public abstract class BoardStateChangedRenderer extends AbstractStateRenderer implements IBoardStateChangedListener {
	public BoardStateChangedRenderer(float preferredOpacity) {
		super(preferredOpacity);
		Global.simulator.getBoard().addBoardStateChangedListener(this);
	}

	@Override
	public void boardStateChanged(IState newState, int newNumberOfIterations) {
		setLatestImage(renderState(newState));
	}
	@Override
	public void refreshImage() {
		setLatestImage(renderState(Global.simulator.getBoard().getCurrentState()));
	}
	public void wasMadeSpurious(IStateRenderer replacement) {
		Global.simulator.getBoard().removeBoardStateChangedListener(this);
	}
	
	private Image renderState(IState newState) {
		PixelWrapper wrapper = calculatePixels(newState, getPreferredOpacity());
		return wrapper.toImage(Global.userInterface.boardPanel, newState.getWidth(), newState.getHeight());
	}
	private PixelWrapper calculatePixels(IState newState, float preferredOpacity) {
		PixelWrapper wrapper = new PixelWrapper(newState.getWidth(), newState.getHeight());
		for(int y = 0; y < newState.getHeight(); y++) {
			for(int x = 0; x < newState.getWidth(); x++) {
				RGBA rgba = getColorAtPoint(newState, x, y, preferredOpacity);
				wrapper.setColor(x, y, rgba.normalizedAlpha, rgba.red, rgba.green, rgba.blue); 
			}
		}
		return wrapper;
	}
	protected abstract RGBA getColorAtPoint(IState state, int x, int y, float preferredOpacity);
	
	protected class RGBA {
		public final int red;
		public final int green;
		public final int blue;
		public final float normalizedAlpha;
		public RGBA(int r, int g, int b, float normalizedAlpha) {
			if(r < 0 || r > 255
				|| g < 0 || g > 255
				|| b < 0 || b > 255
				|| normalizedAlpha < 0 || normalizedAlpha > 1.0f) {
				throw new IllegalArgumentException("Red, green, or blue value was out of range.");
			} else {
				red  = r;
				green = g;
				blue = b;
				this.normalizedAlpha = normalizedAlpha;
			}
		}
	}
}
