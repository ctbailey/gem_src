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

import static gem.LamsasMetadataModel.*;
import gem.simulation.ConwayCell;
import gem.simulation.ICell;
import gem.simulation.InvalidCellStateException;

public class SimpleValidationCell implements ICell {
	
	public String id;
	public int x;
	public int y;
	public PointMeta actualPointMeta;
	public String pointmeta = "";
	public boolean alive;
	public boolean boundary;
	
	public SimpleValidationCell() { /* Do nothing. Required for Gson serialization. */ }
	public SimpleValidationCell(ICell cell, int x, int y) {
		id = "";
		this.x = x;
		this.y = y;
		actualPointMeta = null;
		assignAlive(cell);
		assignBoundary(cell);
	}
	public SimpleValidationCell(LamsasCell cell, int x, int y) {
		this.id = cell.getId();
		this.x = x;
		this.y = y;
		this.actualPointMeta = new PointMeta(cell);
		assignAlive(cell);
		assignBoundary(cell);
	}
	
	public CellState getState() {
		CellState currentState;
		if(boundary) {
			currentState = CellState.IMPASSABLE;
		} else if(alive) {
			currentState = CellState.ALIVE;
		} else {
			currentState = CellState.DEAD;
		}
		return currentState;
	}
	public ConwayCell toConwayCell() {
		return new ConwayCell(this.getState());
	}
	
	private void assignAlive(ICell cell) {
		switch(cell.getState()) {
			case ALIVE: 
				alive = true;
				break;
			case DEAD:
				alive = false;
				break;
			case IMPASSABLE:
				alive = false;
				break;
			default:
				throw new InvalidCellStateException(cell.getClass(), cell.getState(), "Tried to create a SimpleValidationCell from a LamsasCell with an unrecognized cell state.");
		}
	}
	private void assignBoundary(ICell cell) {
		boundary = (cell.getState() == CellState.IMPASSABLE);
	}
	
	public class PointMeta {
		public String oldnumbe;
		public String aux;
		public String fw;
		public String ws;
		public String year;
		public String inftype;
		public String exp_s;
		public String cult;
		public String sex;
		public String age;
		public String educ;
		public String occup;
		public String race;
		public String commtype;
		public String community;
		public String statelong;
		public String fedcomm;
		
		public PointMeta() { /* Do nothing. Required by Gson. */ }
		
		public PointMeta(LamsasCell cell) {
			oldnumbe = cell.getOldNumber();
			aux = auxMap.get(cell.isAux());
			fw = cell.getFw();
			ws = cell.getWs();
			year = cell.getYear() + "";
			inftype = informantTypeMap.get(cell.getInformantType());
			exp_s = cell.getExp_s();
			cult = cultureLevelMap.get(cell.getCultureLevel());
			sex = sexMap.get(cell.getSex());
			age = cell.getAge() + "";
			educ = educationLevelMap.get(cell.getEducationLevel());
			occup = cell.getOccupation();
			race = raceMap.get(cell.getRace());
			commtype = cell.getCommunityType();
			community = cell.getCommunityName();
			statelong = cell.getStateAbbreviation();
			fedcomm = cell.getFedComm() + "";
		}
	}
}
