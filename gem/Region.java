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

package gem;
import static gem.Global.*;
import gem.simulation.state.ICell.CellState;

public class Region {
	
	int xOffset; // Indicates the first column that's located in the region
	int yOffset; // Indicates the first row that's located in the region
	int width;
	int height;
	
	public int countCellsInRegion() {
		
		int total = 0;
		
		for(int x = xOffset; x < xOffset + width; x++) {
			for(int y = yOffset; y < yOffset + height; y++) {
				
				if(simulator.getBoard().getCurrentState().getCell(x, y).getState() == CellState.ALIVE) {
					total++;
				}
				
			}
		}
		
		return total;
		
	}
	
	// Setters
	
	public void setWidth(int w) {
		
		width = w;
		
	}
	
	public void setHeight(int h) {
		
		height = h;
		
	}
	
	public void setXOffset(int x) {
		
		xOffset = x;
		
	}
	
	public void setYOffset(int y) {
		
		yOffset = y;
		
	}
	
	public int getWidth() {
		
		return width;
		
	}
	
	public int getHeight() {
		
		return height;
		
	}
	
	public int getXOffset() {
		
		return xOffset;
		
	}
	
	public int getYOffset() {
		
		return yOffset;
		
	}

}
