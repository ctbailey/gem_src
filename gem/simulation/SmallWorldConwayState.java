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

import gem.simulation.ICell.CellState;
import gem.ui.UserDidNotConfirmException;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import static gem.AutomatonGlobal.*;

public class SmallWorldConwayState extends ConwayState {
	private static final int DEFAULT_NUMBER_OF_SMALL_WORLD_NEIGHBORS_PER_CELL = 3;
	private int smallWorldNeighborsPerCell;

	public SmallWorldConwayState() {
		this(DEFAULT_NUMBER_OF_SMALL_WORLD_NEIGHBORS_PER_CELL);
	}
	public SmallWorldConwayState(int smallWorldNeighborsPerCell) {
		this(smallWorldNeighborsPerCell, ConwayState.createDefaultCells(ConwayBoard.DEFAULT_BOARD_WIDTH, ConwayBoard.DEFAULT_BOARD_HEIGHT));
	}
	public SmallWorldConwayState(BoardDimensions boardDimensions) {
		this(DEFAULT_NUMBER_OF_SMALL_WORLD_NEIGHBORS_PER_CELL, ConwayState.createDefaultCells(boardDimensions.getWidth(), boardDimensions.getHeight()));
	}
	public SmallWorldConwayState(int smallWorldNeighborsPerCell, BoardDimensions dimensions) {
		this(smallWorldNeighborsPerCell, ConwayState.createDefaultCells(dimensions.getWidth(), dimensions.getHeight()));
	}
	public SmallWorldConwayState(int smallWorldNeighborsPerCell, ConwayCell[][] cells) {
		super(cells);
		this.smallWorldNeighborsPerCell = smallWorldNeighborsPerCell;
	}
	@Override
	public ICell[] getNeighbors(int x, int y) {
		List<ICell> immediateNeighbors = getImmediateNeighbors(x, y);
		List<ICell> smallWorldNeighbors = getSmallWorldNeighbors(x, y);
		immediateNeighbors.addAll(smallWorldNeighbors);
		ICell[] allNeighbors = new ICell[immediateNeighbors.size()];
		return immediateNeighbors.toArray(allNeighbors);
	}
	private List<ICell> getImmediateNeighbors(int x, int y) {
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
		return cellList;
	}
	private List<ICell> getSmallWorldNeighbors(int x, int y) {
		List<ICell> smallWorldNeighbors = new ArrayList<ICell>();
		for(int i = 0; i < smallWorldNeighborsPerCell; i++) {
			smallWorldNeighbors.add(getRandomCell());
		}
		return smallWorldNeighbors;
	}

	@Override
	public SmallWorldConwayState getModifiedCopy(CellState newState, int cellX, int cellY) {
		ConwayCell[][] cellsCopy = copyCells();
		cellsCopy[cellX][cellY].setState(newState);
		return new SmallWorldConwayState(smallWorldNeighborsPerCell, cellsCopy);
	}
	@Override
	public ConwayState getCopyWithClearedCellType(CellState toBeCleared) {
		ConwayCell[][] cellsCopy = copyCells();
		for(int x = 0; x < getWidth(); x++) {
			for(int y = 0; y < getHeight(); y++) {
				if(cellsCopy[x][y].getState() == toBeCleared) {
					cellsCopy[x][y].setState(CellState.DEAD);
				}
			}
		}
		return new SmallWorldConwayState(smallWorldNeighborsPerCell, cellsCopy);
	}
	@Override
	public SmallWorldConwayState getNextIteration(ConwayCell[][] newCells) {
		return new SmallWorldConwayState(smallWorldNeighborsPerCell, newCells);
	}
	
	@Override
	public SmallWorldConwayState createDefaultBoardState(BoardDimensions dimensions) {
		return new SmallWorldConwayState(smallWorldNeighborsPerCell, ConwayState.createDefaultCells(dimensions.getWidth(), dimensions.getHeight()));
	}
	
	@Override
	public SmallWorldConwayState getCopyWithRandomizedState(IRandomNumberSource randomNumberSource, double threshold, CellState stateToRandomize)
		throws NoRandomNumbersRemainingException {
			return new SmallWorldConwayState(smallWorldNeighborsPerCell, randomlyModifyCellState(randomNumberSource, threshold, stateToRandomize));
	}
	
	public static int getNumberOfSmallWorldNeighborsPerCellFromUser() throws UserDidNotConfirmException {
		SmallWorldNeighborsPerCellGrabber grabber = new SmallWorldNeighborsPerCellGrabber(userInterface.mainFrame);
		return grabber.getInput();
	}
}

class SmallWorldNeighborsPerCellGrabber {
	private JFrame frame;
	private SpinnerNumberModel smallWorldNeighborsPerCellModel;
	
	SmallWorldNeighborsPerCellGrabber(JFrame frame) {
		this.frame = frame;
	}
	int getInput() throws UserDidNotConfirmException {
		JPanel statePanel = new JPanel();
		smallWorldNeighborsPerCellModel = new SpinnerNumberModel();
		smallWorldNeighborsPerCellModel.setValue(5);
		smallWorldNeighborsPerCellModel.setStepSize(1);
		smallWorldNeighborsPerCellModel.setMinimum(1);
		smallWorldNeighborsPerCellModel.setMaximum(10000);
		JSpinner neighborsPerCellSpinner = new JSpinner(smallWorldNeighborsPerCellModel);
		
		statePanel.add(new JLabel("Number of small world neighbors per cell"));
		statePanel.add(neighborsPerCellSpinner);
		
		int userAction = JOptionPane.showConfirmDialog(frame, statePanel, "Number of small world neighbors per cell?", JOptionPane.OK_CANCEL_OPTION);
		if(userAction == JOptionPane.OK_OPTION) {
			return (Integer)smallWorldNeighborsPerCellModel.getNumber();
		} else {
			throw new UserDidNotConfirmException();
		}
	}
}
