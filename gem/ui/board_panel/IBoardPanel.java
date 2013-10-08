package gem.ui.board_panel;

import gem.ui.board_panel.board_image.AbstractBoardImageSource;
import gem.ui.board_panel.board_image.IBoardImageChangedListener;

public interface IBoardPanel extends IBoardImageChangedListener {

	public void refreshBoardImage();
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
}