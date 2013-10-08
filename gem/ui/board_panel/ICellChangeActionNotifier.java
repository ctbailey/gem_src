package gem.ui.board_panel;

public interface ICellChangeActionNotifier {
	public void addCellChangeActionListener(ICellChangeActionListener listener);
	public boolean removeCellChangeActionListener(ICellChangeActionListener listener);
}
