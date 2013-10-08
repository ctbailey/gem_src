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

package gem.simulation.state;

import gem.simulation.board.BoardDimensions;
import gem.simulation.randomization.IRandomNumberSource;
import gem.simulation.randomization.NoRandomNumbersRemainingException;
import gem.simulation.state.ICell.CellState;
import gem.simulation.state.neighbor_topology.INeighborGraph;

public final class ConwayState extends AbstractConwayState {
	
	public ConwayState(BoardDimensions dimensions) {
		super(dimensions);
	}
	public ConwayState(BoardDimensions dimensions, INeighborGraph neighborGraph) {
		super(dimensions, neighborGraph);
	}
	public ConwayState(AbstractConwayCell[][] cells, INeighborGraph neighborGraph) {
		super(cells, neighborGraph);
	}
	// Methods that return a ConwayState (need to be overridden in subclasses)
	public ConwayState getNextIteration(AbstractConwayCell[][] newCells) {
		return new ConwayState(newCells, neighborGraph);
	}
	public ConwayState getModifiedCopy(ICell newCell, int cellX, int cellY) {
		AbstractConwayCell[][] cellsCopy = copyCells();
		cellsCopy[cellX][cellY] = (AbstractConwayCell)newCell.deepCopy();
		return new ConwayState(cellsCopy, neighborGraph);
	}
	public ConwayState getCopyWithClearedCellType(CellState toBeCleared) {
		AbstractConwayCell[][] cellsCopy = copyCells();
		for(int x = 0; x < getWidth(); x++) {
			for(int y = 0; y < getHeight(); y++) {
				if(cellsCopy[x][y].getState() == toBeCleared) {
					cellsCopy[x][y].setState(CellState.DEAD);
				}
			}
		}
		return new ConwayState(cellsCopy, neighborGraph);
	}

	public ConwayState createDefault(BoardDimensions dimensions) {
		return new ConwayState(dimensions);
	}
	public ConwayState createDefault(BoardDimensions dimensions, INeighborGraph neighborGraph) {
		return new ConwayState(dimensions, neighborGraph);
	}
 	public ConwayState getCopyWithRandomizedState(IRandomNumberSource randomNumberSource, double threshold, CellState stateToRandomize) 
			throws NoRandomNumbersRemainingException {
		return new ConwayState(randomlyModifyCellState(randomNumberSource, threshold, stateToRandomize), neighborGraph);
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
