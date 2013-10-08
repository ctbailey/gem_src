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

import gem.simulation.state.ICell;
import gem.simulation.state.ICell.CellState;

public class InvalidCellStateException extends RuntimeException {
	public final Class<? extends ICell> cellType;
	public final CellState invalidState;
	
	static final long serialVersionUID = 6;
	
	public InvalidCellStateException(Class<? extends ICell> cellType, CellState invalidState) {
		this(cellType, invalidState, "Cell with type " + cellType + " cannot be instantiated with state " + invalidState);
	}
	public InvalidCellStateException(Class<? extends ICell> cellType, CellState invalidState, String message) {
		super(message);
		this.cellType = cellType;
		this.invalidState = invalidState;
	}
}
