/*
	 * Copyright 2013, C. Thomas Bailey
	 * 
	 * This file is part of GEM: The Geographic Modeler.
	 *
     * GEM is free software: you can redistribute it and/or modify
     * it under the terms of the GNU General Public License as published by
     * the Free Software Foundation, either version 3 of the License, or
     * (at your option) any later version.
     * 
     * GEM is distributed in the hope that it will be useful,
     * but WITHOUT ANY WARRANTY; without even the implied warranty of
     * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     * GNU General Public License for more details.
     * 
     * You should have received a copy of the GNU General Public License
     * along with GEM.  If not, see <http://www.gnu.org/licenses/>.
	 */

package gem.simulation.board;
import gem.Global;
import gem.simulation.randomization.IRandomNumberSource;
import gem.simulation.randomization.NoRandomNumbersRemainingException;
import gem.simulation.rules.AbstractRuleSet;
import gem.simulation.rules.ConwayRule;
import gem.simulation.state.AbstractConwayState;
import gem.simulation.state.AbstractState;
import gem.simulation.state.AbstractConwayCell;
import gem.simulation.state.ConwaySerializedState;
import gem.simulation.state.ConwayState;
import gem.simulation.state.IState;
import gem.simulation.state.ICell.CellState;
import gem.simulation.state.neighbor_topology.INeighborGraph;
import gem.simulation.state.neighbor_topology.INeighborTopology;
import gem.talk_to_outside_world.validation.JsonLogger;
import gem.ui.UserDidNotConfirmException;
import gem.ui.board_panel.ICellChangeAction;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import java.util.*;

import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

public class ConwayBoard extends AbstractBoard {
	static final long serialVersionUID = 6;
	
	private AbstractConwayState currentState;
	private Stack<ConwaySerializedState> history;
	private ConwaySerializedState stateInClipboard;
	private ConwayRule currentRules;
	private int currentNumberOfIterations = 0;
	private static final BoardDimensions DEFAULT_DIMENSIONS = new BoardDimensions(AbstractState.DEFAULT_WIDTH, AbstractState.DEFAULT_HEIGHT); 
	
	private static final ConwayRule DEFAULT_RULE = ConwayRule.baileySet();
	
	public ConwayBoard() {
		this(DEFAULT_RULE, DEFAULT_DIMENSIONS);
	}
	public ConwayBoard(ConwayRule ruleConditions, BoardDimensions dimensions) {
		if(ruleConditions == null) {
			throw new NullPointerException("ConwayBoard does not take null rule conditions.");
		} else {
			this.currentRules = ruleConditions;
			setCurrentState(new ConwayState(dimensions));
			stateInClipboard = currentState.serialize();
			history = new Stack<ConwaySerializedState>();
		}
	}
	
	@Override
	public synchronized void iterate() {
		iterateTo(currentRules.calculateNextState(currentState));
	}
	@Override
	public synchronized boolean tryToIterate() {
		AbstractConwayState pendingState = currentRules.calculateNextState(currentState);
		if(isDuplicateState(pendingState)) {
			return false;
		} else {
			iterateTo(pendingState);
			return true;
		}
	}
	private synchronized void iterateTo(AbstractConwayState abstractConwayState) {
		notifyBoardWillIterateListeners(currentState);
		currentNumberOfIterations++;
		logAndUpdateState(abstractConwayState);
		notifyBoardDidIterateListeners(abstractConwayState, currentNumberOfIterations);
	}
	public synchronized boolean tryToGoBack() {
		boolean didGoBack;
		if(history.empty()) {
			didGoBack = false;
		} else {
			didGoBack = true;
			goBack();
		}
		return didGoBack;
	}
	private synchronized void goBack() {
		setCurrentState(history.pop().deserialize());
		currentNumberOfIterations--;
	}
	
	public void resizeBasedOnUserInput() {
		try {
			resize(currentState.getNewDimensionsFromUser());
		} catch(UserDidNotConfirmException ex) {
			// Don't change the current state
		}
	}
	public void resize(BoardDimensions newDimensions) {
		resetToState(currentState.createDefault(newDimensions));
		notifyBoardSizeChangedListeners(newDimensions);
	}
	
	@Override
	public synchronized void clearCurrentStateAndHistory() {
		resetToState(currentState.createDefault(currentState.getDimensions()));
	}
	private synchronized void resetToState(AbstractConwayState newState) {
		currentNumberOfIterations = 0;
		setCurrentState(newState);
		history.clear();
		notifyBoardResetListeners();
	}
	public void clearSelection() {
		setCurrentState(currentState.getCopyWithClearedSelection());
	}
	@Override
	public synchronized void clearCellTypeFromCurrentState(CellState state) {
		setCurrentState(currentState.getCopyWithClearedCellType(state));
	}
	
	@Override
	public void randomizeBoard(IRandomNumberSource randomNumberSource, double threshold, CellState stateToRandomize) {
		try{
			setCurrentState(currentState.getCopyWithRandomizedState(randomNumberSource, threshold, stateToRandomize));
		} catch(NoRandomNumbersRemainingException exception) {
			showNoRandomNumbersRemainingDialog(exception.explanationForUser);
		}
	}	
	private void showNoRandomNumbersRemainingDialog(String explanationForUser) {
		String errorMessage = "Randomization couldn't be completed." + "\n" + explanationForUser;
		JOptionPane.showMessageDialog(Global.userInterface.mainFrame, errorMessage);
	}
	
	@Override
	public synchronized void copyStateToClipboard() {
		stateInClipboard = currentState.serialize();
	}
	@Override
	public synchronized void pasteFromClipboard() {
		setCurrentState(stateInClipboard.deserialize());
	}
	public synchronized void loadCurrentStateFromFile(File file) {
		setCurrentState((AbstractConwayState)JsonLogger.readStateFromFile(file));
	}
	
	@Override
	public IState getCurrentState() {
		return currentState;
	}
	@Override
	public int getNumberOfIterations() {
		return currentNumberOfIterations;
	}
	public AbstractRuleSet getRules() {
		return currentRules;
	}
	public void updateRulesFromUserInput(JFrame mainFrame) {
		try {
			ConwayRule newRule = currentRules.getRuleFromUserInput(mainFrame);
			currentRules = newRule;
			notifyRulesChangedListeners(currentRules);
		} catch(UserDidNotConfirmException ex) {
			// Do nothing
		}
	}
	
	public void cellChangeActionPerformed(ICellChangeAction action, int cellX, int cellY) {
		setCurrentState(modifyStateWithUserInput(action, cellX, cellY));
	}
	private AbstractConwayState modifyStateWithUserInput(ICellChangeAction action, int cellX, int cellY) {
		AbstractConwayCell newCell = (AbstractConwayCell) action.applyActionToCell(currentState.getCell(cellX, cellY));
		return currentState.getModifiedCopy(newCell, cellX, cellY);
	}
	
	private synchronized void logAndUpdateState(AbstractConwayState abstractConwayState) {
		// Log the old state
		history.push(currentState.serialize());
		setCurrentState(abstractConwayState);
	}
	public synchronized void setCurrentState(AbstractConwayState newState) {
		currentState = newState;
		notifyBoardStateChangedListeners(newState, currentNumberOfIterations);
	}
	
	private synchronized boolean isDuplicateState(AbstractConwayState pendingState) {
		return currentState.equals(pendingState);
	}

	@Override
	protected void abstractIsReplacing(AbstractBoard oldBoard) {
		// Do nothing
	}
	@Override
	protected void abstractIsBeingReplacedBy(AbstractBoard newBoard) {
		// Do nothing
	}

	@Override
	public void neighborTopologyChanged(INeighborTopology newTopology) {
		BoardDimensions currentDimensions = currentState.getDimensions();
		INeighborGraph g = newTopology.createGraphWithThisTopology(currentDimensions);
		resetToState(currentState.getModifiedCopy(g));
	}
	
	// User interface
	public List<JMenuItem> getMenuItems() {
		List<JMenuItem> menuItems = new ArrayList<JMenuItem>(2);
		
		JMenuItem userBoardSizeItem = new JMenuItem("Resize board");
		userBoardSizeItem.addActionListener(new ResizeBoardListener());
		menuItems.add(userBoardSizeItem);
		return menuItems;
	}
	
	// UI event listeners
	private class ResizeBoardListener implements ActionListener {
		public void actionPerformed(ActionEvent ev) {
			resizeBasedOnUserInput();
		}
	}	
}
