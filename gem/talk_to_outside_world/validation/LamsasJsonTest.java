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

import gem.simulation.ConwayCell;
import gem.simulation.ICell.CellState;

import com.google.gson.Gson;
import org.junit.Test;

public class LamsasJsonTest {

	private String jsonString = "{" +
			"\"id\":\"NY1\"," + 
			"\"x\":116," +
			"\"y\":37," +
			"\"pointmeta\": {" +
				"\"oldnumbe\":\"50\"," +
				"\"aux\":\"N\"," +
				"\"fw\":\"L\"," +
				"\"ws\":\"E\"," +
				"\"year\":\"1933\"," +
				"\"inftype\":\"I\"," +
				"\"exp_s\":\"B\"," +
				"\"cult\":\"N\"," +
				"\"sex\":\"M\"," +
				"\"age\":\"53\"," +
				"\"educ\":\"3\"," +
				"\"occup\":\"F\"," +
				"\"race\":\"W\"," +
				"\"commtype\":\"R\"," +
				"\"community\":\"Suffolk Co.\"," +
				"\"statelong\":\"NY\"," +
				"\"fedcomm\":\"36103\"" +
			"}" +
		"}";
	
	@Test
	public void testJsonSerializationAndDeserialization() {
		Gson gs = new Gson();
		SimpleValidationCell jsonCell = gs.fromJson(jsonString, SimpleValidationCell.class);
		int x = jsonCell.x;
		int y = jsonCell.y;
		LamsasCell cell = new LamsasCell(jsonCell);
		SimpleValidationCell secondJsonCell = new SimpleValidationCell(cell, x, y);
		String secondGenerationJsonString = gs.toJson(secondJsonCell);
		SimpleValidationCell thirdJsonCell = gs.fromJson(secondGenerationJsonString, SimpleValidationCell.class);
		LamsasCell secondCell = new LamsasCell(thirdJsonCell);
	}
	
	@Test
	public void testCellSerializationAndDeserialization() {
		Gson gs = new Gson();
		ConwayCell c = new ConwayCell(CellState.ALIVE);
		String s = gs.toJson(c);
	}

}
