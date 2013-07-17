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

package gem.simulation;
import gem.InvalidConfigurationException;
import gem.simulation.ICell.CellState;
import gem.talk_to_outside_world.validation.SimpleValidationBoardState;
import gem.ui.UserDidNotConfirmException;
import gem.ui.BoardPanel.MouseButton;

import java.io.File;

import static gem.AutomatonGlobal.userInterface;

import java.util.*;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

public class ConwayBoard extends AbstractBoard {
	static final long serialVersionUID = 6;
	
	private ConwayState currentState;
	private Stack<ConwaySerializedState> history;
	private ConwaySerializedState stateInClipboard;
	private ConwayRule currentRules;
	private int currentNumberOfIterations = 0;
	private boolean usingSmallWorld;
	
	private static final ConwayRule DEFAULT_RULE = ConwayRule.baileySet();
	
	public ConwayBoard() {
		this(DEFAULT_RULE, DEFAULT_BOARD_WIDTH, DEFAULT_BOARD_HEIGHT);
	}
	public ConwayBoard(ConwayRule ruleConditions, int width, int height) {
		if(ruleConditions == null) {
			throw new NullPointerException("ConwayBoard does not take null rule conditions.");
		} else {
			this.currentRules = ruleConditions;
			BoardDimensions startingDimensions = new BoardDimensions(width, height);
			currentState = new ConwayState(startingDimensions);
			stateInClipboard = new ConwaySerializedState(new ConwayState(startingDimensions));
			history = new Stack<ConwaySerializedState>();
		}
	}
	
	@Override
	public synchronized void iterate() {
		iterate(currentRules.calculateNextState(currentState));
	}
	@Override
	public synchronized boolean tryToIterate() {
		ConwayState pendingState = currentRules.calculateNextState(currentState);
		if(isDuplicateState(pendingState)) {
			return false;
		} else {
			iterate(pendingState);
			return true;
		}
	}
	private synchronized void iterate(ConwayState newState) {
		notifyBoardWillIterateListeners(currentState);
		currentNumberOfIterations++;
		logAndUpdateState(newState);
		notifyBoardDidIterateListeners(newState, currentNumberOfIterations);
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
		resetToState(currentState.createDefaultBoardState(newDimensions));
		notifyBoardSizeChangedListeners(newDimensions);
	}
	@Override
	public boolean getValueForConfigurationKey(GameConfigurationKey key) {
		boolean value;
		switch(key) {
			case SmallWorld:
				value = usingSmallWorld;
				break;
			default:
				value = false;
				break;
		}
		return value;
	}
	@Override
	public void setValueForConfigurationKey(GameConfigurationKey key, boolean value) 
			throws InvalidConfigurationException {
		switch(key) {
			case SmallWorld:
				setUsingSmallWorld(value);
				break;
			default:
				throw new InvalidConfigurationException("Unrecognized configuration key." + key);
			}
	}
	private void setUsingSmallWorld(boolean value) {
		usingSmallWorld = value;
		try {
			ConwayState newState;
			if(usingSmallWorld) {
				newState = new SmallWorldConwayState(SmallWorldConwayState.getNumberOfSmallWorldNeighborsPerCellFromUser(), currentState.getDimensions());
			} else {
				newState = new ConwayState(currentState.getDimensions());
			}
			resetToState(newState);
		} catch(UserDidNotConfirmException ex) {
			// Do nothing. Specifically, don't reset the current state.
		}
	}
	
	@Override
	public synchronized void clearCurrentStateAndHistory() {
		resetToState(currentState.createDefaultBoardState(currentState.getDimensions()));
	}
	private synchronized void resetToState(ConwayState newState) {
		currentNumberOfIterations = 0;
		setCurrentState(newState);
		history.clear();
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
		JOptionPane.showMessageDialog(userInterface.mainFrame, errorMessage);
	}
	
	@Override
	public synchronized void copyStateToClipboard() {
		stateInClipboard = new ConwaySerializedState(currentState);
	}
	@Override
	public synchronized void pasteFromClipboard() {
		setCurrentState(stateInClipboard.deserialize());
	}
	public synchronized void loadCurrentStateFromFile(File file) {
		setCurrentState(SimpleValidationBoardState.conwayStateFromJsonFile(file));
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
	
	public void userInteracted(CellState currentlySelectedByUser, MouseButton buttonPressed, int cellX, int cellY) {
		if(buttonPressed == MouseButton.LEFT || buttonPressed == MouseButton.RIGHT) {
			setCurrentState(modifyStateWithUserInput(currentlySelectedByUser, buttonPressed, cellX, cellY));
		}
	}
	private ConwayState modifyStateWithUserInput(CellState userStateSelection, MouseButton buttonPressed, int cellX, int cellY) {
		CellState newCellState = calculateNewCellStateFromUserInput(userStateSelection, buttonPressed, cellX, cellY);
		return currentState.getModifiedCopy(newCellState, cellX, cellY);
	}
	private CellState calculateNewCellStateFromUserInput(CellState userStateSelection, MouseButton buttonPressed, int cellX, int cellY) {
		CellState newCellState;
		CellState oldCellState = currentState.getCell(cellX, cellY).getState();
		
		if(	(userStateSelection == oldCellState)
				&& (buttonPressed == MouseButton.RIGHT) 
				) {
			newCellState = CellState.DEAD;
		} else if(	(oldCellState == CellState.DEAD)
					&& (buttonPressed == MouseButton.LEFT)) {
			newCellState = userStateSelection;
		} else {
			newCellState = oldCellState;
		}
		return newCellState;
	}
	
	private synchronized void logAndUpdateState(ConwayState newState) {
		// Log the old state
		history.push(new ConwaySerializedState(currentState));
		setCurrentState(newState);
	}
	private synchronized void setCurrentState(ConwayState state) {
		currentState = state;
		notifyBoardStateChangedListeners(state, currentNumberOfIterations);
	}
	
	private synchronized boolean isDuplicateState(ConwayState pendingState) {
		return currentState.equals(pendingState);
	}

	@Override
	protected void classSpecificIsReplacing(AbstractBoard oldBoard) {
		// Do nothing
	}
	@Override
	protected void classSpecificIsBeingReplacedBy(AbstractBoard newBoard) {
		// Do nothing
	}
}
