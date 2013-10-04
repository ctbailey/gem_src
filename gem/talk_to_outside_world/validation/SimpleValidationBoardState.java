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

package gem.talk_to_outside_world.validation;

import gem.Global;
import gem.simulation.board.BoardDimensions;
import gem.simulation.state.ConwayCell;
import gem.simulation.state.ConwayState;
import gem.simulation.state.IState;

import java.io.File;
import java.io.IOException;


import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.google.gson.Gson;

public class SimpleValidationBoardState {
	
	private SimpleValidationCell[] cells;
	
	public SimpleValidationBoardState(IState state) {
		cells = populateCells(state);
	}
	public SimpleValidationBoardState(SimpleValidationCell[] cells) {
		this.cells = cells;
	}
	
	public ConwayState toConwayState() {
		int xMax = findHighestX(cells);
		int yMax = findHighestY(cells);
		
		ConwayCell[][] conwayCells = new ConwayCell[xMax + 1][yMax + 1];
		for(SimpleValidationCell c : cells) {
			conwayCells[c.x][c.y] = c.toConwayCell();
		}
		return new ConwayState(conwayCells, Global.topologyManager.createNeighborGraphWithCurrentTopology(new BoardDimensions(conwayCells)));
	}
	private int findHighestX(SimpleValidationCell[] cs) {
		int highestSoFar = 0;
		for(SimpleValidationCell c : cs) {
			if(c.x > highestSoFar) { highestSoFar = c.x; }
		}
		return highestSoFar;
	}
	private int findHighestY(SimpleValidationCell[] cs) {
		int highestSoFar = 0;
		for(SimpleValidationCell c : cs) {
			if(c.y > highestSoFar) { highestSoFar = c.y; }
		}
		return highestSoFar;
	}
	
	public String toJson() {
		Gson gs = new Gson();
		return gs.toJson(cells);
	}
	private SimpleValidationCell[] populateCells(IState state) {
		SimpleValidationCell[] cellArray = new SimpleValidationCell[state.getWidth()*state.getHeight()];
		int flattenedIndex = 0;
		for(int x = 0; x < state.getWidth(); x++) {
			for(int y = 0; y < state.getHeight(); y++) {
				cellArray[flattenedIndex] = new SimpleValidationCell(state.getCell(x, y), x, y);
				flattenedIndex++;
			}
		}
		return cellArray;
	}
	
	public static ConwayState conwayStateFromJsonFile(File file) {
		String json = "";
		try {
			json = Files.toString(file, Charsets.UTF_8);
		} catch (IOException e) {
			e.printStackTrace();
		}
		Gson gs = new Gson();
		SimpleValidationCell[] cs = gs.fromJson(json, SimpleValidationCell[].class);
		return new SimpleValidationBoardState(cs).toConwayState();
	}
}
