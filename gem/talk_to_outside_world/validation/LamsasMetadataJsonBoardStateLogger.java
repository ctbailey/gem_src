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

import gem.simulation.board.IBoardWillIterateListener;
import gem.simulation.state.ConwayState;
import gem.simulation.state.IState;

import java.io.File;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;


//import com.google.gson.Gson;

public class LamsasMetadataJsonBoardStateLogger implements IBoardWillIterateListener {
	//private BufferedWriter out;
	//private Gson gson = new Gson();
	
	public LamsasMetadataJsonBoardStateLogger(File targetFile) {
		/*
		AutomatonVars.automaton.addAutomatonWillIterateListener(this);
	
		try {
			FileWriter fw = new FileWriter(targetFile);
			out = new BufferedWriter(fw);
			out.write("[");
		} catch(Exception ex) {
			ex.printStackTrace();
		}
		*/
		throw new NotImplementedException();
	}
	public void automatonWillIterate(ConwayState boardState) {
		/*
		try {
			
			String jsonBoardState = gson.toJson(boardState);
			out.write(jsonBoardState);
			out.newLine();
			out.write(",");
		} catch(Exception ex) {
			ex.printStackTrace();
		}
		*/
		throw new NotImplementedException();
	}
	public void quitLogging() {
		/*
		try {
			out.write("]");
			out.close();
		} catch(Exception ex) {
			ex.printStackTrace();
		}
		AutomatonVars.automaton.removeAutomatonWillIterateListener(this);
		*/
		throw new NotImplementedException();
	}
	@Override
	public void boardWillIterate(IState currentState) {
		throw new NotImplementedException();
	}
}
