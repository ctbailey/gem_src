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

public class Debug {
	static final long serialVersionUID = 6;
	
	public static void printLine(String string) {
		safePrint(string + "\n");
	}
	public static void newLine() {
		safePrint("\n");
	}
	public static <T> void print(T[][] array) {
		newLine();
		for(int i = 0; i < array.length; i++) {
			newLine();
			for(int j = 0; j < array[i].length; j++) {
				safePrint(" " + array[i][j] + " ");
			}
		}
		newLine();
	}
	public static void print(int[][] array) {
		Integer[][] newArray = new Integer[array.length][array[0].length];
		for(int i = 0; i < array.length; i++) {
			for(int j = 0; j < array[i].length; j++) {
				newArray[i][j] = array[i][j];
			}
		}
		print(newArray);
	}
	public static void print(double[][] array) {
		Double[][] newArray = new Double[array.length][array[0].length];
		for(int i = 0; i < array.length; i++) {
			for(int j = 0; j < array[i].length; j++) {
				newArray[i][j] = array[i][j];
			}
		}
		print(newArray);
	}
	private static void safePrint(String string) {
		if(userInterface.debugArea != null) {
			userInterface.debugArea.append(string);
		} else {
			System.out.print(string);
		}
	}
		
}
