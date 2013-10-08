package gem.ui.board_panel;

import javax.swing.JPanel;

import gem.talk_to_outside_world.AutomatonSerializable;
import gem.ui.IMenuProvider;
import gem.ui.board_panel.board_image.IBoardImageChangedListener;

public abstract class AbstractBoardPanel extends JPanel implements AutomatonSerializable, IBoardImageChangedListener, IBoardPanel, ICellChangeActionNotifier, IMenuProvider {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

}
