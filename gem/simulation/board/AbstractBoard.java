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
import gem.simulation.rules.AbstractRuleSet;
import gem.simulation.rules.IRulesChangedListener;
import gem.simulation.state.IState;
import gem.simulation.state.neighbor_topology.INeighborTopologyChangedListener;
import gem.ui.IMenuItemProvider;
import gem.ui.board_panel.ICellChangeActionListener;

import java.util.ArrayList;
import java.util.List;

import static gem.Global.*;

public abstract class AbstractBoard implements IBoard, ICellChangeActionListener, IMenuItemProvider, INeighborTopologyChangedListener {
	
	private List<IBoardStateChangedListener> boardStateChangedListeners = new ArrayList<IBoardStateChangedListener>(); 
	private List<IRulesChangedListener> rulesChangedListeners = new ArrayList<IRulesChangedListener>();
	private List<IBoardSizeChangedListener> boardSizeChangedListeners = new ArrayList<IBoardSizeChangedListener>();
	private List<IBoardWillIterateListener> boardWillIterateListeners = new ArrayList<IBoardWillIterateListener>();
	private List<IBoardDidIterateListener> boardDidIterateListeners = new ArrayList<IBoardDidIterateListener>();
	private List<IBoardResetListener> boardResetListeners = new ArrayList<IBoardResetListener>();
	
	public AbstractBoard() {
		Global.topologyManager.addNeighborTopologyChangedListener(this);
	}
	
	// When a board is replaced by another board
	public void isBeingReplacedBy(AbstractBoard newBoard) {
		basicIsBeingReplacedBy(newBoard);
		abstractIsBeingReplacedBy(newBoard);
	}
	public void isReplacing(AbstractBoard oldBoard) {
		basicIsReplacing(oldBoard);
		abstractIsReplacing(oldBoard);
	}
	
	private void basicIsBeingReplacedBy(AbstractBoard newBoard) {
		transferEventListeners(newBoard);
		unregisterThisAsListener();
	}
	protected abstract void abstractIsBeingReplacedBy(AbstractBoard newBoard);
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
		Global.userInterface.boardPanel.removeCellChangeActionListener(this);
	}
	
	private void basicIsReplacing(AbstractBoard oldBoard) {
		addThisAsListener();
		notifyIsReplacingListeners();
	}
	protected abstract void abstractIsReplacing(AbstractBoard oldBoard);
	private void addThisAsListener() {
		if(userInterface.boardPanel != null) {
			userInterface.boardPanel.addCellChangeActionListener(this);
		}
	}
	
	// Event methods
	public void addBoardResetListener(IBoardResetListener listener) {
		boardResetListeners.add(listener);
	}
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
	
	public boolean removeBoardResetListener(IBoardResetListener listener) {
		return boardResetListeners.remove(listener);
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
	protected void notifyBoardResetListeners() {
		for(IBoardResetListener listener : boardResetListeners) {
			listener.boardWasReset();
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
