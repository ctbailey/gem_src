package gem.ui.board_panel;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import gem.ui.board_panel.BoardPanel.MouseButton;

public abstract class AbstractCellChangeActionNotifier implements IBoardInteractionListener, ICellChangeActionNotifier {
	private List<ICellChangeActionListener> cellChangeActionListeners = new ArrayList<ICellChangeActionListener>();
	
	@Override
	public void userInteracted(MouseButton buttonPressed, Set<String> keysPressed, int cellX, int cellY) {
		ICellChangeAction action = createAction(buttonPressed, keysPressed, cellX, cellY);
		notifyCellChangeActionListeners(action, cellX, cellY);
	}
	protected abstract ICellChangeAction createAction(MouseButton buttonPressed, Set<String> keysPressed, int cellX, int cellY);
	@Override
	public void addCellChangeActionListener(ICellChangeActionListener listener) {
		cellChangeActionListeners.add(listener);
	}

	@Override
	public boolean removeCellChangeActionListener(ICellChangeActionListener listener) {
		return cellChangeActionListeners.remove(listener);
	}
	private void notifyCellChangeActionListeners(ICellChangeAction action, int cellX, int cellY) {
		for(ICellChangeActionListener listener : cellChangeActionListeners) {
			listener.cellChangeActionPerformed(action, cellX, cellY);
		}
	}
}
