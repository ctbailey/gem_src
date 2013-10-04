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

public class BoardDimensions {
	private final int width;
	private final int height;
	private static final String SEPARATOR = "x";
	public BoardDimensions(ICell[][] cells) {
		this(cells.length, cells[0].length);
	}
	public BoardDimensions(int width, int height) {
		if(width < 0 || height < 0) {
			throw new IllegalArgumentException("Cannot create board dimensions of height or width less than 0.");
		} else {
			this.width = width;
			this.height = height;
		}
	}
	public int getWidth() {
		return width;
	}
	public int getHeight() {
		return height;
	}
	@Override
	public String toString() {
		return width + SEPARATOR + height;
	}
	@Override
	public boolean equals(Object otherObject) {
		if(otherObject instanceof BoardDimensions) {
			BoardDimensions otherDimension = (BoardDimensions)otherObject;
			return otherDimension.getHeight() == this.getHeight() 
					&& otherDimension.getWidth() == this.getWidth();
		}
		return false;
	}
		
	
}
