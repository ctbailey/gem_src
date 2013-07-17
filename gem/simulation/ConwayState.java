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

import static gem.AutomatonGlobal.*;

import gem.simulation.ICell.CellState;
import gem.ui.UserDidNotConfirmException;

import java.util.*;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;

public class ConwayState extends AbstractState {
	private ConwayCell[][] cells;
	public static final CellState DEFAULT_CELL_STATE = CellState.DEAD;
	
	public ConwayState(BoardDimensions dimensions) {
		this(createDefaultCells(dimensions.getWidth(), dimensions.getHeight()));
	}
	public ConwayState(List<List<ConwayCell>> cells) {
		this(Utility.to2DArray(cells));
	}
	public ConwayState(ConwayCell[][] cells) {
		for(ConwayCell[] column : cells) {
			for(ConwayCell c : column) {
				if(c == null) {
					throw new NullPointerException("Tried to initialize a board state with at least one null entry in the cell array.");
				}
			}
		}
		this.cells = cells;
	}
	
	public BoardDimensions getDimensions() {
		return new BoardDimensions(getWidth(), getHeight());
	}
	public int getWidth() {
		return cells.length;
	}
	public int getHeight() {
		return cells[0].length;
	}
	
	@Override
	public ICell getCell(int x, int y) {
		return cells[x][y];
	}
	public ICell getRandomCell() {
		int xIndex = (int) Math.floor(Math.random() * (this.getWidth() - 1));
		int yIndex = (int) Math.floor(Math.random() * (this.getHeight() - 1));
		return cells[xIndex][yIndex];
	}

	protected ConwayCell[][] copyCells() {
		ConwayCell[][] cellsCopy = new ConwayCell[getWidth()][getHeight()];
		for(int x = 0; x < getWidth(); x++) {
			for(int y = 0; y < getHeight(); y++) {
				cellsCopy[x][y] = new ConwayCell(cells[x][y].getState());
			}
		}
		return cellsCopy;
	}
	
	public ICell[] getNeighbors(int x, int y) {
		List<ICell> cellList = new ArrayList<ICell>();
		for(int i = -1; i <= 1; i++) {
			for(int j = -1; j <= 1; j++) {
				if(	isInBounds(x + i, y + j)
					&& !(i == 0 && j ==0)
					) {
					cellList.add(getCell(x + i, y + j));
				}
			}
		}
		ICell[] neighbors = new ICell[cellList.size()];
		return cellList.toArray(neighbors);
	}
	public int getNumberOfCells() {
		return getWidth()*getHeight();
	}
	public int getNumberOfCellsOfType(CellState kindOfCell) {
		int total = 0;
		for(int x = 0; x < getWidth(); x++) {
			for(int y = 0; y < getHeight(); y++) {
				if(cells[x][y].getState() == kindOfCell) { total++; }
			}
		}
		return total;
	}
	public int getNumberOfNeighborsInState(int x, int y, CellState targetState) {
		ICell[] neighbors = getNeighbors(x, y);
		int neighborsInState = 0;
		for(ICell neighbor : neighbors) {
			if(neighbor.getState() == targetState) {
				neighborsInState++;
			}
		}
		return neighborsInState;
	}
	public int calculateCellTotalExcept(CellState stateToIgnore) {
		int total = 0;
		for(int x = 0; x < getWidth(); x++) {
			for(int y = 0; y < getHeight(); y++) {
				if(cells[x][y].getState() != stateToIgnore) { total++; }
			}
		}
		return total;
	}
	
	public ConwaySerializedState serialize() {
		return new ConwaySerializedState(this);
	}
	public boolean Equals(ConwayState otherState) {
		return ConwaySerializedState.convertToSerializationString(this).equals(ConwaySerializedState.convertToSerializationString(otherState));
	}
	
	protected static ConwayCell[][] createDefaultCells(int width, int height) {
		ConwayCell[][] cells = new ConwayCell[width][height];
		for(int x = 0; x < width; x++) {
			for(int y = 0; y < height; y++) {
				cells[x][y] = new ConwayCell(DEFAULT_CELL_STATE); // Puts the cells in their default state
			}
		}
		return cells;
	}
	
	public BoardDimensions getNewDimensionsFromUser() throws UserDidNotConfirmException {
		// Create user input panel
		SpinnerModel widthModel =
				new SpinnerNumberModel(	50, //initial value
										0, //min
										Integer.MAX_VALUE, //max
										1); //step
		SpinnerModel heightModel = new SpinnerNumberModel(50, 0, Integer.MAX_VALUE, 1);
		JSpinner widthSpinner = new JSpinner(widthModel);
		JSpinner heightSpinner = new JSpinner(heightModel);
		
		JPanel userInputPanel = new JPanel();
		userInputPanel.setLayout(new BoxLayout(userInputPanel,BoxLayout.X_AXIS));
		userInputPanel.add(new JLabel("Width: "));
		userInputPanel.add(widthSpinner);
		userInputPanel.add(new JLabel("Height: "));
		userInputPanel.add(heightSpinner);
		
		int userSelection = JOptionPane.showConfirmDialog(userInterface.mainFrame, userInputPanel, "New board dimensions", JOptionPane.OK_CANCEL_OPTION);
		if(userSelection == JOptionPane.OK_OPTION) {
			BoardDimensions newBoardDimensions = new BoardDimensions((Integer)widthSpinner.getModel().getValue(), (Integer)heightSpinner.getModel().getValue());
			return newBoardDimensions;
		} else {
			throw new UserDidNotConfirmException();
		}
	}
	
	protected ConwayCell[][] randomlyModifyCellState(IRandomNumberSource randomNumberSource, double threshold, CellState stateToRandomize) 
			throws NoRandomNumbersRemainingException {
		ConwayCell[][] randomlyGeneratedCells = new ConwayCell[getWidth()][getHeight()];
		
		for(int x = 0; x < getWidth(); x++) {
			for(int y = 0; y < getHeight(); y++) {
				CellState currentCellState = getCell(x, y).getState();
				if(currentCellState == stateToRandomize
						|| currentCellState == CellState.DEAD) {
					randomlyGeneratedCells[x][y] = new ConwayCell(getRandomCellState(randomNumberSource, threshold, stateToRandomize));
				} else {
					randomlyGeneratedCells[x][y] = new ConwayCell(currentCellState);
				}
			}
		}
		
		return randomlyGeneratedCells;
	}
	private CellState getRandomCellState(IRandomNumberSource randomNumberSource, double threshold, CellState stateToRandomize) 
			throws NoRandomNumbersRemainingException {
		if(randomNumberSource.getNextRandomNumber() < threshold) { return stateToRandomize; }
		else { return CellState.DEAD; }
	}

	// Methods that return a ConwayState (need to be overridden in subclasses)
	public ConwayState getNextIteration(ConwayCell[][] newCells) {
		return new ConwayState(newCells);
	}
	public ConwayState getModifiedCopy(CellState newState, int cellX, int cellY) {
		ConwayCell[][] cellsCopy = copyCells();
		cellsCopy[cellX][cellY].setState(newState);
		return new ConwayState(cellsCopy);
	}
	public ConwayState getCopyWithClearedCellType(CellState toBeCleared) {
		ConwayCell[][] cellsCopy = copyCells();
		for(int x = 0; x < getWidth(); x++) {
			for(int y = 0; y < getHeight(); y++) {
				if(cellsCopy[x][y].getState() == toBeCleared) {
					cellsCopy[x][y].setState(CellState.DEAD);
				}
			}
		}
		return new ConwayState(cellsCopy);
	}
	public ConwayState createDefaultBoardState(BoardDimensions dimensions) {
		return new ConwayState(createDefaultCells(dimensions.getWidth(), dimensions.getHeight()));
	}
	public ConwayState getCopyWithRandomizedState(IRandomNumberSource randomNumberSource, double threshold, CellState stateToRandomize) 
			throws NoRandomNumbersRemainingException {
		return new ConwayState(randomlyModifyCellState(randomNumberSource, threshold, stateToRandomize));
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		for(int y = 0; y < getHeight(); y++) {
			for(int x = 0; x < getWidth(); x++) {
				sb.append(getCell(x,y).getState() + " ");
			}
			sb.append("\n");
		}
		return sb.toString();
	}
}
