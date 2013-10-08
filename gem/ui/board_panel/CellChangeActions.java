package gem.ui.board_panel;

import gem.simulation.state.ICell;
import gem.simulation.state.ICell.CellState;

public class CellChangeActions {
	public static class ChangeStateAction implements ICellChangeAction {
		public final CellState resultingState;
		public ChangeStateAction(CellState resultingState) {
			this.resultingState = resultingState;
		}
		@Override
		public ICell applyActionToCell(ICell cell) {
			return cell.getModifiedCopy(resultingState);
		}
	}
	public static class SelectionAction implements ICellChangeAction {
		public final boolean setSelectedTo;
		public SelectionAction(boolean setSelectedTo) {
			this.setSelectedTo = setSelectedTo;
		}
		@Override
		public ICell applyActionToCell(ICell cell) {
			return cell.getModifiedCopy(setSelectedTo);
		}
	}
}
