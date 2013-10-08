package gem.ui.board_panel;

import java.util.Set;

import gem.Global;
import gem.simulation.state.ICell.CellState;
import gem.ui.board_panel.BoardPanel.MouseButton;
import gem.ui.board_panel.CellChangeActions.*;

public class CellChangeActionNotifier extends AbstractCellChangeActionNotifier {
	@Override
	protected ICellChangeAction createAction(MouseButton buttonPressed, Set<String> pressedKeys, int cellX, int cellY) {
		if(buttonPressed != MouseButton.RIGHT 
				&& buttonPressed != MouseButton.LEFT) {
			throw new IllegalArgumentException("The board state only changes when the user presses the left or right mouse button.");
		}
		if(pressedKeys.contains("S")) {
			return new SelectionAction(buttonPressed == MouseButton.LEFT);
		}
		
		if(buttonPressed == MouseButton.LEFT) {
			return new ChangeStateAction(Global.userInterface.selectedCellTypePanel.getCurrentlySelectedState());
		} else  { // if(buttonPressed == MouseButton.RIGHT)
			return new ChangeStateAction(CellState.DEAD);
		}
	}
	
}