package gem.ui.board_panel.board_image;

import gem.Global;
import gem.simulation.board.BoardDimensions;
import gem.simulation.board.IBoardStateChangedListener;
import gem.simulation.state.IState;
import gem.ui.board_panel.BoardPanel;

public class SelectedCellColorer extends AbstractStateRenderer implements IBoardStateChangedListener {
	private static final int SELECTED_RED = 255;
	private static final int SELECTED_GREEN = 0;
	private static final int SELECTED_BLUE = 0;
	
	private static final int UNSELECTED_RED = 255;
	private static final int UNSELECTED_GREEN = 255;
	private static final int UNSELECTED_BLUE = 255;
	
	private PixelWrapper pixelWrapper;
	public SelectedCellColorer(float preferredOpacity, BoardPanel boardPanel) {
		super(preferredOpacity);
		BoardDimensions dimensions = Global.simulator.getBoard().getCurrentState().getDimensions();
		pixelWrapper = new PixelWrapper(dimensions.getWidth(), dimensions.getHeight());
		Global.simulator.getBoard().addBoardStateChangedListener(this);
	}

	private RGBA getCellColor(boolean isSelected) {
		int r;
		int g;
		int b;
		float a;
		if(isSelected) {
			r = SELECTED_RED;
			g = SELECTED_GREEN;
			b = SELECTED_BLUE;
			a = getPreferredOpacity();
		} else {
			r = UNSELECTED_RED;
			g = UNSELECTED_GREEN;
			b = UNSELECTED_BLUE;
			a = 0;
		}
		return new RGBA(r,g,b,a);
	}

	@Override
	public void refreshImage() {
		setLatestImage(pixelWrapper.toImage());
	}

	@Override
	public boolean makesSpurious(IStateRenderer otherRenderer) {
		return (otherRenderer instanceof SelectedCellColorer);
	}

	@Override
	public void wasMadeSpurious(IStateRenderer replacement) {
		Global.simulator.getBoard().removeBoardStateChangedListener(this);
	}

	@Override
	public void boardStateChanged(IState newState, int newNumberOfIterations) {
		PixelWrapper wrapper = new PixelWrapper(newState.getWidth(), newState.getHeight());
		for(int x = 0; x < newState.getWidth(); x++) {
			for(int y = 0; y < newState.getHeight(); y++) {
				wrapper.setColor(x, y, getCellColor(newState.getCell(x,y).isSelected()));
			}
		}
		setLatestImage(wrapper.toImage());
	}
}
