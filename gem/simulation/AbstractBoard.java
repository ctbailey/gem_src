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

import gem.AutomatonGlobal;
import gem.InvalidConfigurationException;
import gem.ui.IBoardInteractionListener;

import java.util.ArrayList;
import java.util.List;

import static gem.AutomatonGlobal.*;

public abstract class AbstractBoard implements IBoard, IBoardInteractionListener {
	public enum GameConfigurationKey {
		SmallWorld;
	}
	
	static final int DEFAULT_BOARD_WIDTH = 50;
	static final int DEFAULT_BOARD_HEIGHT = 50;
	
	private List<IBoardStateChangedListener> boardStateChangedListeners = new ArrayList<IBoardStateChangedListener>(); 
	private List<IRulesChangedListener> rulesChangedListeners = new ArrayList<IRulesChangedListener>();
	private List<IBoardSizeChangedListener> boardSizeChangedListeners = new ArrayList<IBoardSizeChangedListener>();
	private List<IBoardWillIterateListener> boardWillIterateListeners = new ArrayList<IBoardWillIterateListener>();
	private List<IBoardDidIterateListener> boardDidIterateListeners = new ArrayList<IBoardDidIterateListener>();
	
	// When a board is replaced by another board
	public void isBeingReplacedBy(AbstractBoard newBoard) {
		basicIsBeingReplacedBy(newBoard);
		classSpecificIsBeingReplacedBy(newBoard);
	}
	public void isReplacing(AbstractBoard oldBoard) {
		basicIsReplacing(oldBoard);
		classSpecificIsReplacing(oldBoard);
	}
	
	private void basicIsBeingReplacedBy(AbstractBoard newBoard) {
		transferEventListeners(newBoard);
		unregisterThisAsListener();
	}
	protected abstract void classSpecificIsBeingReplacedBy(AbstractBoard newBoard);
	private void transferEventListeners(AbstractBoard newBoard) {
		for(IBoardStateChangedListener listener : boardStateChangedListeners) {
			newBoard.addBoardStateChangedListener(listener);
		}
		boardStateChangedListeners.clear();
		
		for(IRulesChangedListener listener : rulesChangedListeners) {
			newBoard.addRulesChangedListener(listener);
		}
		rulesChangedListeners.clear();
		
		for(IBoardSizeChangedListener listener : boardSizeChangedListeners) {
			newBoard.addBoardSizeChangedListener(listener);
		}
		boardSizeChangedListeners.clear();
		
		for(IBoardWillIterateListener listener : boardWillIterateListeners) {
			newBoard.addBoardWillIterateListener(listener);
		}
		boardWillIterateListeners.clear();
		
		for(IBoardDidIterateListener listener : boardDidIterateListeners) {
			newBoard.addBoardDidIterateListener(listener);
		}
		boardDidIterateListeners.clear();
	}
	private void unregisterThisAsListener() {
		AutomatonGlobal.userInterface.boardPanel.removeBoardInteractionListener(this);
	}
	
	private void basicIsReplacing(AbstractBoard oldBoard) {
		addThisAsListener();
		notifyIsReplacingListeners();
	}
	protected abstract void classSpecificIsReplacing(AbstractBoard oldBoard);
	private void addThisAsListener() {
		if(userInterface.boardPanel != null) {
			userInterface.boardPanel.addBoardInteractionListener(this);
		}
	}
	
	// Board settings
	public abstract boolean getValueForConfigurationKey(GameConfigurationKey key);
	public abstract void setValueForConfigurationKey(GameConfigurationKey key, boolean value) throws InvalidConfigurationException;
	
	// Event methods
 	public void addBoardWillIterateListener(IBoardWillIterateListener listener) {
		boardWillIterateListeners.add(listener);
	}
	public void addBoardDidIterateListener(IBoardDidIterateListener listener) {
		boardDidIterateListeners.add(listener);		
	}
	public void addBoardStateChangedListener(IBoardStateChangedListener listener) {
		boardStateChangedListeners.add(listener);
	}
	public void addRulesChangedListener(IRulesChangedListener listener) {
		rulesChangedListeners.add(listener);
	}
	public void addBoardSizeChangedListener(IBoardSizeChangedListener listener) {
		boardSizeChangedListeners.add(listener);
	}
	
	public void removeBoardWillIterateListener(IBoardWillIterateListener listener) {
		boardWillIterateListeners.remove(listener);
	}
	public void removeBoardDidIterateListener(IBoardDidIterateListener listener) {
		boardDidIterateListeners.remove(listener);
	}
	public void removeBoardStateChangedListener(IBoardStateChangedListener listener) {
		boardStateChangedListeners.remove(listener);
	}
	public void removeRulesChangedListener(IRulesChangedListener listener) {
		rulesChangedListeners.remove(listener);
	}
	public void removeBoardSizeChangedListener(IBoardSizeChangedListener listener) {
		boardSizeChangedListeners.remove(listener);
	}
	
	private void notifyIsReplacingListeners() {
		for(IBoardStateChangedListener listener : boardStateChangedListeners) {
			listener.boardStateChanged(this.getCurrentState(), this.getNumberOfIterations());
		}
		for(IRulesChangedListener listener : rulesChangedListeners) {
			listener.rulesChanged(this.getRules());
		}
		for(IBoardSizeChangedListener listener : boardSizeChangedListeners) {
			listener.boardSizeChanged(this.getCurrentState().getDimensions());
		}
	}
	protected synchronized void notifyBoardStateChangedListeners(IState newState, int newNumberOfIterations) {
		for(IBoardStateChangedListener listener : boardStateChangedListeners) {
			listener.boardStateChanged(newState, newNumberOfIterations);
		}
	}
	protected void notifyRulesChangedListeners(AbstractRuleSet newRules) {
		for(IRulesChangedListener listener : rulesChangedListeners) {
			listener.rulesChanged(newRules);
		}
	}
	protected void notifyBoardSizeChangedListeners(BoardDimensions newBoardDimensions) {
		for(IBoardSizeChangedListener listener : boardSizeChangedListeners) {
			listener.boardSizeChanged(newBoardDimensions);
		}
	}
	protected void notifyBoardWillIterateListeners(IState currentState) {
		for(IBoardWillIterateListener listener: boardWillIterateListeners) {
			listener.boardWillIterate(currentState);
		}
	}
	protected void notifyBoardDidIterateListeners(IState newState, int updatedNumberOfIterations) {
		for(IBoardDidIterateListener listener : boardDidIterateListeners) {
			listener.boardDidCalculateNewState(newState, updatedNumberOfIterations);
		}
	}
	
}
