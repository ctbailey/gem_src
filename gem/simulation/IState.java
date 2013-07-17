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

public interface IState {
	public int getWidth();
	public int getHeight();
	
	public boolean isInBounds(int x, int y);
	
	public BoardDimensions getDimensions();
	public int getNumberOfCells();
	public int getNumberOfCellsOfType(CellState state);
	public ICell getCell(int x, int y);
	public ICell getRandomCell();
	public ICell[] getNeighbors(int x, int y);
}
