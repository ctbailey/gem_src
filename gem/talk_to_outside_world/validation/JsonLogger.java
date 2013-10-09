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

import gem.simulation.state.AbstractConwayCell;
import gem.simulation.state.AbstractConwayState;
import gem.simulation.state.ConwayCell;
import gem.simulation.state.ConwayState;
import gem.simulation.state.IState;
import gem.simulation.state.ICell.CellState;
import gem.simulation.state.neighbor_topology.INeighborGraph;
import gem.simulation.state.neighbor_topology.SymmetricalInfluenceGraph;

import java.io.File;
import java.io.FileWriter;
import java.lang.reflect.Type;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.InstanceCreator;

public class JsonLogger {
	private static final boolean USE_ILKKA_FORMAT = true;
	public static void writeStateToFile(File file, IState state) {
		String jsonState;
		if(USE_ILKKA_FORMAT) {
			SimpleValidationBoardState validationState = new SimpleValidationBoardState(state);
			jsonState = validationState.toJson();
		} else {
			Gson gs = new Gson();
			jsonState = gs.toJson(state);
		}
		writeJsonToFile(file, jsonState);
	}
	private static void writeJsonToFile(File file, String json) {
		try {
			FileWriter fw = new FileWriter(file);
			fw.write(json);
			fw.close();
		} catch(Exception ex) { ex.printStackTrace(); }
	}
	private static String readJsonFromFile(File file) {
		String json = "";
		try {
			json = Files.toString(file, Charsets.UTF_8);
		} catch(Exception ex) { ex.printStackTrace(); }
		return json;
	}
	public static AbstractConwayState readStateFromFile(File file) {
		String json = readJsonFromFile(file);
		if(USE_ILKKA_FORMAT) {
			Gson gson = new Gson();
			SimpleValidationCell[] cells = gson.fromJson(json, SimpleValidationCell[].class);
			return new SimpleValidationBoardState(cells).toConwayState();
		} else {
			InstanceCreator<AbstractConwayCell> cellCreator = new InstanceCreator<AbstractConwayCell>() {
				@Override
				public AbstractConwayCell createInstance(Type arg0) {
					return new ConwayCell(CellState.DEAD, false);
				}
			};
			InstanceCreator<INeighborGraph> neighborGraphCreator = new InstanceCreator<INeighborGraph>() {
				@Override
				public INeighborGraph createInstance(Type arg0) {
					return new SymmetricalInfluenceGraph();
				}
			};
			Gson gson = new GsonBuilder().
				registerTypeAdapter(AbstractConwayCell.class, cellCreator).
				registerTypeAdapter(INeighborGraph.class, neighborGraphCreator).
				create();
			ConwayState state = gson.fromJson(json, ConwayState.class);
			return state;
		}
	}
}