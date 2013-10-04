package gem.ui.board_image;

import java.awt.Image;
import java.awt.Point;

import gem.Global;
import gem.simulation.state.IState;
import gem.ui.IMouseLeftBoardListener;
import gem.ui.IMouseMovedIntoNewCellListener;
import gem.ui.board_image.AbstractStateRenderer;

public abstract class CellHighlighter extends AbstractStateRenderer implements IMouseMovedIntoNewCellListener, IMouseLeftBoardListener {
	private int neighborRedValue;
	private int neighborGreenValue;
	private int neighborBlueValue;
	
	public CellHighlighter(float normalizedPreferredOpacity, int neighborRedValue, int neighborGreenValue, int neighborBlueValue) {
		super(normalizedPreferredOpacity);
		this.neighborRedValue = neighborRedValue;
		this.neighborGreenValue = neighborGreenValue;
		this.neighborBlueValue = neighborBlueValue;
		Global.userInterface.boardPanel.addMouseMovedIntoNewCellListener(this);
		Global.userInterface.mouseLeftBoardNotifier.addMouseLeftBoardListener(this);
	}
	
	@Override
	public void refreshImage() {
		Point mouseCellLocation = Global.userInterface.boardPanel.getCellLocationMouseIsOver();
		highlightCellsOfInterest(mouseCellLocation.x, mouseCellLocation.y);
	}

	@Override
	public boolean makesSpurious(IStateRenderer otherRenderer) {
		return (otherRenderer instanceof CellHighlighter);
	}

	@Override
	public void wasMadeSpurious(IStateRenderer replacement) {
		removeThisAsEventListener();
	}

	@Override
	public void mouseMovedIntoNewCell(int x, int y) {
		highlightCellsOfInterest(x,y);
	}
	@Override
	public void mouseLeftBoard() {
		setLatestImage(createBlankImage());
	}
	private void highlightCellsOfInterest(int x, int y) {
		Point[] influenceSourceLocations = getPointsOfInterest(x,y);
		Image highlightedImage = highlightLocations(influenceSourceLocations);
		setLatestImage(highlightedImage);
	}
	protected abstract Point[] getPointsOfInterest(int x, int y);
	private void removeThisAsEventListener() {
		Global.userInterface.mouseLeftBoardNotifier.removeMouseLeftBoardListener(this);
		Global.userInterface.boardPanel.removeMouseMovedIntoNewCellListener(this);
	}
	private Image highlightLocations(Point[] locations) {
		PixelWrapper wrapper = new PixelWrapper(
				Global.simulator.getBoard().getCurrentState().getWidth(), 
				Global.simulator.getBoard().getCurrentState().getHeight());
		for(Point p : locations) {
			wrapper.setColor(p.x, p.y, getPreferredOpacity(), neighborRedValue, neighborGreenValue, neighborBlueValue);
		}
		IState currentState = Global.simulator.getBoard().getCurrentState();
		return wrapper.toImage(Global.userInterface.boardPanel, currentState.getWidth(), currentState.getHeight());
	}
	private Image createBlankImage() {
		IState currentState = Global.simulator.getBoard().getCurrentState();
		return PixelWrapper.blankImage(
				currentState.getWidth(), 
				currentState.getHeight(), 
				Global.userInterface.boardPanel, 
				currentState.getWidth(), 
				currentState.getHeight());
	}

}
