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

import static gem.AutomatonGlobal.*;

public class Debug {
	static final long serialVersionUID = 6;
	
	public static void printLine(String string) {
		if(userInterface.showDebugArea) {
			userInterface.debugArea.append(string + "\n");
		}
	}
	public static void newLine() {
		if(userInterface.showDebugArea) {
			userInterface.debugArea.append("\n");
		}
	}
	public static void print2dArrayElements(double[][] array) {
		if(userInterface.showDebugArea) {
			newLine();
			for(int i = 0; i < array.length; i++) {
				newLine();
				for(int j = 0; j < array[i].length; j++) {
					userInterface.debugArea.append(" " + array[i][j] + " ");
				}
			}
			newLine();
		}
	}
		
}
