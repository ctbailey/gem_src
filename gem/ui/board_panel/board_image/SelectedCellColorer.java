package gem.ui.board_panel.board_image;

import gem.Global;
import gem.simulation.board.BoardDimensions;
import gem.simulation.board.IBoardSizeChangedListener;
import gem.ui.board_panel.BoardPanel;
import gem.ui.board_panel.CellChangeActions.SelectionAction;
import gem.ui.board_panel.ICellChangeAction;
import gem.ui.board_panel.ICellChangeActionListener;

public class SelectedCellColorer extends AbstractStateRenderer implements ICellChangeActionListener, IBoardSizeChangedListener {
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
		Global.simulator.getBoard().addBoardSizeChangedListener(this);
		boardPanel.addCellChangeActionListener(this);
	}

	@Override
	public void cellChangeActionPerformed(ICellChangeAction action, int cellX, int cellY) {
		if(action instanceof SelectionAction) {
			SelectionAction selectionAction = (SelectionAction)action;
			colorCell(cellX, cellY, selectionAction.setSelectedTo);
		}
	}
	private void colorCell(int x, int y, boolean isSelected) {
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
		pixelWrapper.setColor(x, y, a, r, g, b);
		setLatestImage(pixelWrapper.toImage());
	}
	
	@Override
	public void boardSizeChanged(BoardDimensions newBoardDimensions) {
		pixelWrapper = new PixelWrapper(newBoardDimensions.getWidth(), newBoardDimensions.getHeight());
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
		Global.simulator.getBoard().removeBoardSizeChangedListener(this);
		Global.userInterface.boardPanel.removeCellChangeActionListener(this);
	}
}
