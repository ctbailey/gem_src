package gem.ui;

import gem.simulation.board.ICell.CellState;
import gem.ui.board_image.AbstractBoardImageSource;
import gem.ui.board_image.IBoardImageChangedListener;

public interface IBoardPanel extends IBoardImageChangedListener {

	public void refreshBoardImage();
	public CellState getUserCellTypeSelection();
	public void setUserCellTypeSelection(CellState state);
	public AbstractBoardImageSource getBoardImageSource();
	public void setBoardImageSource(
			AbstractBoardImageSource newImageSource);
	
	public void zoomIn();
	public void zoomOut();
	public void zoom(double scale);
	public void zoomToFit();

	// Event methods
	public void addMouseMovedIntoNewCellListener(IMouseMovedIntoNewCellListener listener);
	public void removeMouseMovedIntoNewCellListener(IMouseMovedIntoNewCellListener listener);
	
	public void addBoardInteractionListener(IBoardInteractionListener listener);
	public void removeBoardInteractionListener(IBoardInteractionListener listener);
	
	public void addUserCellTypeSelectionChangedListener(IUserCellTypeSelectionChangedListener listener);
	public void removeUserCellTypeSelectionChangedListener(IUserCellTypeSelectionChangedListener listener);
}