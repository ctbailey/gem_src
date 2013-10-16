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

import java.io.Serializable;

import gem.simulation.board.InvalidCellStateException;

public abstract class AbstractConwayCell implements ICell, Serializable {
	private static final long serialVersionUID = 1L;
	protected static final CellState DEFAULT_STATE = CellState.DEAD;
	private boolean isSelected;
	private CellState cellState;
	public AbstractConwayCell(CellState state, boolean isSelected) {
		if(cellStateIsValid(state)) {
			cellState = state;
			this.isSelected = isSelected;
		} else {
			throw new InvalidCellStateException(this.getClass(), state);
		}
	}
	
	public CellState getState() { return cellState; }	
	public void setState(CellState stateArg) { cellState = stateArg; }
	public boolean isSelected() { return isSelected; }
	public void setSelected(boolean isMarkedArg) { isSelected = isMarkedArg; }
	public abstract AbstractConwayCell deepCopy();
	@Override
	public boolean equals(Object otherObject) {
		return this == otherObject;
	}
	
	private boolean cellStateIsValid(CellState state) {
		return (state == CellState.ALIVE
				|| state == CellState.DEAD
				|| state == CellState.IMPASSABLE);
	}
}