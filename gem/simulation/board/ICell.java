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


import java.util.Arrays;
import java.util.List;

public interface ICell {
	public static enum CellState {
		ALIVE, DEAD, IMPASSABLE;
		
		public static List<Boolean> ToBitList(CellState state) {
			switch(state) {
				// Least significant bit on the left, so ALIVE = 1
				case DEAD: return Arrays.asList(false, false);
				case ALIVE: return Arrays.asList(true, false);
				case IMPASSABLE: return Arrays.asList(false, true);
				default: throw new InvalidCellStateException(ICell.class, state);
			}
		}
		public static CellState convert(int i) {
			switch(i) {
				case 0: return CellState.DEAD;
				case 1: return CellState.ALIVE;
				case 2: return CellState.IMPASSABLE;
				default: throw new IllegalArgumentException("Int " + i + " was out of range.");
			}
		}
		public static CellState parse(List<Boolean> bits) {
			if(bits.size() != 2) {
				throw new IllegalArgumentException("Tried to convert a list of bits with too many elements.");
			}
			if(bits.get(0)) {
				if(bits.get(1)) {
					throw new IllegalArgumentException("Can't convert two true bits to cell state.");
				} else {
					return CellState.ALIVE;
				}
			} else {
				if(bits.get(1)) {
					return CellState.IMPASSABLE;
				} else {
					return CellState.DEAD;
				}
			}
		}
		public static String toAdjective(CellState state) {
			switch(state) {
			case ALIVE: return "living";
			case DEAD: return "dead";
			case IMPASSABLE: return "impassable";
			default: throw new IllegalArgumentException("toAdjective didn't recognize cell state " + state);
			}
		}
	};

	public CellState getState();
}
