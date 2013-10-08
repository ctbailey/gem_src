package gem.ui.board_panel;

import gem.simulation.state.ICell;

public interface ICellChangeAction {
	public ICell applyActionToCell(ICell cell);
}
