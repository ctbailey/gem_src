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

import gem.*;
import gem.simulation.board.AbstractBoard;
import gem.simulation.board.ConwayBoard;
import gem.simulation.board.ICell.CellState;
import gem.simulation.state.IState;
import gem.talk_to_outside_world.AutomatonSerializable;
import gem.ui.IGoForwardListener;
import gem.ui.IPlaybackSpeedChangedListener;
import gem.ui.IMenuProvider;

import java.io.*;
import java.util.*;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import static gem.Global.*;

public class Simulator implements AutomatonSerializable, Runnable, IGoForwardListener, IMenuProvider {
	private static final int DEFAULT_DELAY_BETWEEN_ITERATIONS = 150;
	
	private ArrayList<ISimulationStartedListener> automatonStartedListeners = new ArrayList<ISimulationStartedListener>();
	private ArrayList<ISimulationStoppedListener> automatonStoppedListeners = new ArrayList<ISimulationStoppedListener>();
	private ArrayList<IPlaybackSpeedChangedListener> playbackSpeedChangedListeners = new ArrayList<IPlaybackSpeedChangedListener>();
	
	private Thread runThread = null; // Used to calculate new board states
	
	private AbstractBoard board;
	
	private boolean playing = false; // Boolean flag which declares whether the automaton should be running (true) or not (false)
	private boolean goForward = true;
	private int delayBetweenIterations = DEFAULT_DELAY_BETWEEN_ITERATIONS;
	
	static final long serialVersionUID = 6;
	
	// Constructors
	public Simulator() {
		this(new ConwayBoard());
	}
	public Simulator(AbstractBoard board) {
		if(board == null) {
			throw new IllegalArgumentException("Automaton does not take null AbstractBoards.");
		}
		this.setBoard(board); 
		userInterface.goForwardNotifier.addGoForwardListener(this);
	}

	// Run simulation
	public void beginSimulationInSeparateThreadIfNotAlreadyRunning() {
		if(runThread == null) {
			playing = true;
			runThread = new Thread(this);
			runThread.start();
			notifyAutomatonStartedListeners();
		}
	}
	public void run() {
		while(playing && tryToIterateAutomaton()) { 
			try {
				Thread.sleep(delayBetweenIterations);
			} catch(Exception ex) {
				ex.printStackTrace();
			}
		}
	}
	public void stopSimulationIfRunning() {
		if(runThread != null) {
			playing = false;
			try { runThread.join(); } 
			catch(Exception ex) { 
				JOptionPane.showMessageDialog(Global.userInterface.mainFrame, "Error attempting to stop automaton: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE); 
			}
			runThread = null;
			notifyAutomatonStoppedListeners(getBoard().getCurrentState());
		}
	}
	public void setShouldGoForward(boolean goForward) {
		this.goForward = goForward;
	}
	public synchronized boolean tryToIterateAutomaton() {
		boolean didMove;
		if(goForward) {
			didMove = getBoard().tryToIterate();	
		} else {
			didMove = getBoard().tryToGoBack();
		}
		return didMove;
	}

	public synchronized void clearBoard() {
		stopSimulationIfRunning();
		getBoard().clearCurrentStateAndHistory();
	}
	public synchronized double getCellStateAverage() {
		
		/* Returns the average of all cell states.
		 * Living cells are coded as 1, non-living
		 * cells are coded as 0.
		 */
		
		double cellStateTotal = 0;
		
		for(int x = 0; x < getBoard().getCurrentState().getWidth(); x++) {
			for(int y = 0; y < getBoard().getCurrentState().getHeight(); y++) {
				if(getBoard().getCurrentState().getCell(x, y).getState() == CellState.ALIVE) {
					cellStateTotal++;
				}
			}
		}
		double numberOfCells = getBoard().getCurrentState().getNumberOfCells();
		double average = cellStateTotal/numberOfCells;
		return average;
	}
	
	public int getDelayBetweenIterations() {
		return delayBetweenIterations;
	}
	public void setDelayBetweenIterations(int newDelay) {
		if(newDelay < 0) {
			throw new IllegalArgumentException("Delay can't be less than 0.");
		} else {
			delayBetweenIterations = newDelay;
		}
	}
	public boolean isPlaying() {
		return playing;
	}
	public boolean getShouldGoForward() {
		return goForward;
	}
	public void incrementPlaybackSpeed() {
		int newDelay = delayBetweenIterations - 20;
		if(newDelay > 0) { delayBetweenIterations = newDelay; }
		notifyPlaybackSpeedChangedListeners(delayBetweenIterations);
	}
	public void decrementPlaybackSpeed() {
		delayBetweenIterations += 20;
		notifyPlaybackSpeedChangedListeners(delayBetweenIterations);
	}
	public AbstractBoard getBoard() {
		return board;
	}
	public void setBoard(AbstractBoard newBoard) {
		if(newBoard == null) {
			throw new IllegalArgumentException("Cannot set the current board to null.");
		}
		if(board != null) board.isBeingReplacedBy(newBoard);
		newBoard.isReplacing(board);
		board = newBoard;
	}

	// Save/load methods
	public void save(ObjectOutputStream output) {
		
		Object[] savedAutomaton = new Object[1];
		
		savedAutomaton[0] = getBoard();
		
		try {
			output.writeObject(savedAutomaton);
		} catch(Exception ex) {
			ex.printStackTrace();
		}
	}
	public void load(ObjectInputStream input) {
		try{
			Object[] loadedAutomaton = (Object[]) input.readObject();
			AbstractBoard loadedBoard = (AbstractBoard) loadedAutomaton[0];			
			setBoard(loadedBoard);
		} catch(Exception ex) {
			ex.printStackTrace();
		}
	}
	
	// Event methods
	public void addAutomatonStartedListener(ISimulationStartedListener listener) {
		automatonStartedListeners.add(listener);
	}
	public void addAutomatonStoppedListener(ISimulationStoppedListener listener) {
		automatonStoppedListeners.add(listener);
	}
	public void addPlaybackSpeedChangedListener(IPlaybackSpeedChangedListener listener) {
		playbackSpeedChangedListeners.add(listener);
	}
	
	public void removeAutomatonStartedListener(ISimulationStartedListener listener) {
		automatonStartedListeners.remove(listener);
	}
	public void removeAutomatonStoppedListener(ISimulationStoppedListener listener) {
		automatonStoppedListeners.remove(listener);
	}
	public void removePlaybackSpeedChangedListener(IPlaybackSpeedChangedListener listener) {
		playbackSpeedChangedListeners.remove(listener);
	}
	
	private synchronized void notifyAutomatonStartedListeners() {
		for(ISimulationStartedListener listener : automatonStartedListeners) {
			listener.simulationStarted();
		}
	}
	private synchronized void notifyAutomatonStoppedListeners(IState finalState) {
		for(ISimulationStoppedListener listener : automatonStoppedListeners) {
			listener.automatonStopped(finalState);
		}
	}
	private void notifyPlaybackSpeedChangedListeners(int newPlaybackSpeed) {
		for(IPlaybackSpeedChangedListener listener : playbackSpeedChangedListeners) {
			listener.playbackSpeedChanged(newPlaybackSpeed);
		}
	}

	// UI methods
	public JMenu getJMenu() {
		JMenu simulationMenu = new JMenu("Simulation");
		List<JMenuItem> menuItems = board.getMenuItems();
		menuItems.addAll(Global.topologyManager.getMenuItems());
		for(JMenuItem menuItem : menuItems) {
			simulationMenu.add(menuItem);
		}
		return simulationMenu;
	}
}