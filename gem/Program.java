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
import gem.simulation.Automaton;
import gem.talk_to_outside_world.Metadata;
import gem.ui.MapWizard;
import gem.ui.UserInterface;
 
public class Program {
	static final long serialVersionUID = 6;
	
	public static void execute(String[] args) {
		buildAutomatonModules();
		AutomatonGlobal.userInterface.buildUI();
	}
	public static void buildAutomatonModules() {
		AutomatonGlobal.userInterface = new UserInterface();
		AutomatonGlobal.automaton = new Automaton();
		AutomatonGlobal.mapWizard = new MapWizard();
		AutomatonGlobal.geography = new Geography();
		AutomatonGlobal.metadata = new Metadata();
	}
}