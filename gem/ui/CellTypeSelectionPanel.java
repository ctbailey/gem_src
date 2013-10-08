package gem.ui;

import gem.simulation.state.ICell.CellState;
import gem.ui.board_panel.ICellTypeSelectionChangedListener;

import java.awt.GridBagConstraints;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

public class CellTypeSelectionPanel extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final CellState DEFAULT_SELECTION = CellState.ALIVE;
	private List<ICellTypeSelectionChangedListener> selectionChangedListeners = new ArrayList<ICellTypeSelectionChangedListener>();
	private CellState currentSelection = DEFAULT_SELECTION;
	private GridBagConstraints constraints;
	
	public CellTypeSelectionPanel() {
		constraints = new GridBagConstraints();
		constraints.gridx = 0;
		constraints.gridy = 7;
		constraints.weighty = 1;
		constraints.fill = GridBagConstraints.BOTH;
		this.setLayout(new GridLayout(0,1));
		this.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
			JLabel selectATileLabel = new JLabel("Select a cell type to place:");
			this.add(selectATileLabel);
			
			ButtonGroup paintPanelRBGroup = new ButtonGroup();
			JRadioButton livingCellRB = new JRadioButton("Living cell");
				livingCellRB.addActionListener(new LivingCellRBListener());
				paintPanelRBGroup.add(livingCellRB);
				livingCellRB.setSelected(true);
			
			JRadioButton impassableCellRB = new JRadioButton("Impassable cell");
				impassableCellRB.addActionListener(new ImpassableCellRBListener());
				paintPanelRBGroup.add(impassableCellRB);
				
				this.add(livingCellRB);
				this.add(impassableCellRB);
	}
	private void setCurrentlySelectedState(CellState state) {
		currentSelection = state;
		notifySelectionChangedListeners(state);
	}
	public CellState getCurrentlySelectedState() {
		return currentSelection;
	}
	public GridBagConstraints getGridBagConstraints() {
		return constraints;
	}
	class LivingCellRBListener implements ActionListener {
		public void actionPerformed(ActionEvent ev) {
			setCurrentlySelectedState(CellState.ALIVE);
		}
	}
	class ImpassableCellRBListener implements ActionListener {
		public void actionPerformed(ActionEvent ev) {
			setCurrentlySelectedState(CellState.IMPASSABLE);
		}
	}
	public void addCellTypeSelectionChangedListener(ICellTypeSelectionChangedListener listener) {
		selectionChangedListeners.add(listener);
	}
	public boolean removeCellTypeSelectionChangedListener(ICellTypeSelectionChangedListener listener) {
		return selectionChangedListeners.remove(listener);
	}
	private void notifySelectionChangedListeners(CellState newSelection) {
		for(ICellTypeSelectionChangedListener listener : selectionChangedListeners) {
			listener.userCellTypeSelectionChanged(newSelection);
		}
	}
}
