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

/*
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

import lap.ca.AutomatonGlobal;
import lap.ca.simulation.IBoardWillIterateListener;
import lap.ca.simulation.ConwayState;

import com.google.gson.Gson;
*/

public class Json {

	/*
	 * Other fields
	 */
	/*
	public static final String defaultJsonLocation = "lamsas-meta-ilkka.json";
	private static final Gson gson = new Gson();
	private static CompactJsonBoardStateLogger compactJsonBoardStateLogger;
	private static LamsasMetadataJsonBoardStateLogger lamsasMetadataJsonBoardStateLogger;
	
	public static LamsasCell[][] getLamsasCellsFromDefaultLocation() {
		return getLamsasMetadataCellsFromFile(new File(defaultJsonLocation));
	}
	public static LamsasCell[][] getLamsasMetadataCellsFromFile(File file) {
		SimpleValidationCell[] jsonCells = parseLamsasMetadataJsonFile(file);
		return makeLamsasCells(jsonCells);
	}
	private static SimpleValidationCell[] parseLamsasMetadataJsonFile(File file) {

		try {
			BufferedReader in = new BufferedReader(new FileReader(file));
			return (SimpleValidationCell[]) gson.fromJson(in, SimpleValidationCell[].class);
		} catch(Exception ex) {
			ex.printStackTrace();
		}
		
		throw new RuntimeException("Wasn't able to read Lamsas JSON file.");
	}
	private static LamsasCell[][] makeLamsasCells(SimpleValidationCell[] jsonCells) {
		int xMax = getMaximumX(jsonCells);
		int yMax = getMaximumY(jsonCells);
		
		LamsasCell[][] lamsasCells = new LamsasCell[xMax + 1][yMax + 1];
		for(SimpleValidationCell jsonCell : jsonCells) {
			int x = Integer.parseInt(jsonCell.x);
			int y = Integer.parseInt(jsonCell.y);
			
			lamsasCells[x][y] = new LamsasCell(jsonCell);
		}
		return lamsasCells;
	}
	private static int getMaximumX(SimpleValidationCell[] jsonCells) {
		int highestSoFar = 0;
		for(SimpleValidationCell jsonCell : jsonCells) {
			int x = Integer.parseInt(jsonCell.x);
			if(x > highestSoFar) { highestSoFar = x; }
		}
		return highestSoFar;
	}
	private static int getMaximumY(SimpleValidationCell[] jsonCells) {
		int highestSoFar = 0;
		for(SimpleValidationCell jsonCell : jsonCells) {
			int y = Integer.parseInt(jsonCell.y);
			if(y > highestSoFar) { highestSoFar = y; }
		}
		return highestSoFar;
	}
	
	public static ConwayState getBoardStateFromCompactJsonFile(File file) {
		try {
			BufferedReader in = new BufferedReader(new FileReader(file));
			return (ConwayState) gson.fromJson(in, ConwayState.class);
		} catch(Exception ex) {
			ex.printStackTrace();
		}
		
		throw new RuntimeException("Wasn't able to read compact JSON file.");
	}
	
	public static void beginLoggingBoardStatesToCompactJsonFile(File targetFile) {
		if(compactJsonBoardStateLogger == null) { 
			compactJsonBoardStateLogger = new CompactJsonBoardStateLogger(targetFile); 
		}
		else { throw new RuntimeException("There's already one compact JSON board state logger."); }
	}
	public static void beginLoggingBoardStatesToMetadataJsonFile(File targetFile) {
		if(lamsasMetadataJsonBoardStateLogger == null) {
			lamsasMetadataJsonBoardStateLogger = new LamsasMetadataJsonBoardStateLogger(targetFile);
		} else {
			throw new RuntimeException("There's already one metadata JSON board state logger.");
		}
	}
	public static void quitLoggingBoardStatesToCompactJsonFile() {
		compactJsonBoardStateLogger.quitLogging();
		compactJsonBoardStateLogger = null;
	}
	public static void quitLoggingBoardStatesToMetadataJsonFile() {
		lamsasMetadataJsonBoardStateLogger.quitLogging();
		lamsasMetadataJsonBoardStateLogger = null;
	}
	*/
}