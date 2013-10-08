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

package gem.talk_to_outside_world;

import static gem.Global.*;

import gem.Debug;
import gem.simulation.state.ICell.CellState;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import javax.swing.*;


public class Metadata implements AutomatonSerializable {
	public String[][][] metadataArray = null;
		/*
		 * The first two indices represent spatial coordinates. The third index
		 * represents the index of the piece of metadata. Thus:
		 * metadata[2][3][1] stores metadata about the cell
		 * located at (2,3). Since the third index is "1," it will
		 * return the value of whatever category has been indexed as 1.
		 * (E.g., maybe index 0 is race, index 1 is gender, index 3 is education, etc.)
		 * 
		 * See also the comment on String[] categoryIdentifier.
		 * 
		 */
	
	int[] columnIndexesOfInterest; 
		/*
		 *  Identifies what column numbers (in the original metadata file) contain the categories listed in categoryIdentifiers.
		 *  Thus, if categoryIdentifiers[0] = "race" and columnIndexesOfInterest[0] = 3, then
		 *  the "race" category is located in the 4th column (counting from 0) of the .csv metadata file.
		 *  
		 */
	
	public String[] categoryIdentifiers; 
		/*
		 * Category names are placed in this array in the same order
		 * the actual values of the categories are stored in the metadata array.
		 * Note this array contains only those categories the user selected
		 * before parsing the metadata. (I.e., it does not contain all
		 * the categories in the .csv file.)
		 * 
		 * Thus, e.g. - 
		 * 
		 * If categoryIdentifier[0] is "gender" and 
		 * metadataMap[2][3][0] returns "male",
		 * "male" is the value of "gender" for the cell
		 * located at (2,3).
		 */
	
	String[] columnHeadingsArray;
	
	ArrayList<Point> usedPoints; 
		/*
		 *  Don't want multiple metadata entries to share the same board location, so this
		 *  ArrayList keeps track of the used locations.
		 */
	ArrayList<Point> cellsWithoutMetadata;	
	
	File metadataFile;
	String splitter = null;
	JFrame metadataWindow;
	JCheckBox[] checkBoxArray;
	int longitudeIndex; // column number of longitude values
	int latitudeIndex; // column number latitude values
	String longitudeHeading = "long";
	String latitudeHeading = "lat";
	public ArrayList<Point> inferredMetadataPoints;
	int metadataEntries; // Number of lines in csv file - 1 (don't count the first one because it's the column headings)
	
	public void editMetadata() {
		
		if(	splitter == null
			&& metadataArray == null) {
			
			setUpMetadataWindow();
			
		} else {
			
			buildMetadataWindow(columnHeadingsArray);
			
		}
		
	}
	
	void setUpMetadataWindow() {
		
		splitter = JOptionPane.showInputDialog("Type just the delimiter character for your columns below." +
				"\nFor example, if your columns are separated by commas, type only a comma, with no additional spaces or other characters." +
				"\nThis program assumes different rows occur on different lines of the text file.");
	
		if(splitter != null) { // if the user chose to enter a delimiter character
		
			JFileChooser fileChooser = new JFileChooser();
			int userChose = fileChooser.showOpenDialog(userInterface.mainFrame);
			
			if(userChose == JFileChooser.APPROVE_OPTION) {
	
				BufferedReader in;
				
				try{
				
					metadataFile = fileChooser.getSelectedFile();	
					in = new BufferedReader(new FileReader(metadataFile));
					
					String firstLine = in.readLine();
					columnHeadingsArray = firstLine.split(splitter);
					
					in.close();

					int cellsInAutomaton = simulator.getBoard().getCurrentState().getWidth() * simulator.getBoard().getCurrentState().getHeight();
					metadataEntries = findNumberOfMetadataEntries();
					
					if(metadataEntries > cellsInAutomaton) {
						
						int ignoredEntries = metadataEntries - cellsInAutomaton;
						
						int userContinue = JOptionPane.showConfirmDialog(metadataWindow,"The specified metadata file has " + metadataEntries + " entries, but the board only has " +
								cellsInAutomaton + " cells, meaning " + ignoredEntries + " entries won't be assigned a cell in the automaton. Continue?");
						
						if(!(userContinue == JOptionPane.YES_OPTION)) {
							
							return; // if the user opted not to continue, don't continue parsing metadata, etc.
							
						}
						
					}
					
					buildMetadataWindow(columnHeadingsArray);
					
				} catch(Exception ex) {
					ex.printStackTrace();
				}
			}
		}
		
	}
	
	void determineCategoriesToParse() {
		
		ArrayList<String> selectedCategoriesArrayList = new ArrayList<String>();
		ArrayList<Integer> columnIndexesArrayList = new ArrayList<Integer>();
		
		for(int i = 0; i < checkBoxArray.length; i++) {
			
			if(checkBoxArray[i].isSelected()) {
				
				selectedCategoriesArrayList.add(checkBoxArray[i].getText());
				columnIndexesArrayList.add(i);
		 				
			}
			
		}
		
		categoryIdentifiers = (String[]) selectedCategoriesArrayList.toArray(new String[selectedCategoriesArrayList.size()]);
		
		columnIndexesOfInterest = new int[columnIndexesArrayList.size()];
		for(int i = 0; i < columnIndexesArrayList.size(); i++) {
			
			columnIndexesOfInterest[i] = columnIndexesArrayList.get(i);
			
		}
		
	}
	
	void parseMetadata() { 
		
		/*
		 * Things this method needs to do:
		 * - Parse long. and lat. of each row, lining those up with the
		 * x and y coordinates of the right cell
		 * - Parse the relevant columns, adding them to metadata[x][y][i], where i
		 * is the index of the relevant category in the re-instantiated categoryIdentifiers
		 * array
		 */
		
		findLongitudeAndLatitudeColumns(columnHeadingsArray);
		
		determineCategoriesToParse(); // Updates categoryIdentifiers to reflect the categories the user has selected.
		
		metadataArray = new String[simulator.getBoard().getCurrentState().getWidth()][simulator.getBoard().getCurrentState().getHeight()][categoryIdentifiers.length];
		initializeMetadataArray(metadataArray);
		
		usedPoints = new ArrayList<Point>();
		
		try{
			
			BufferedReader in = new BufferedReader(new FileReader(metadataFile));
			
			String line;
			
			in.readLine(); // read the first line, which is column headings rather than data
			
			while((line = in.readLine()) != null) {

				parseLine(line);
				
			}
			
			in.close();
			
		} catch(Exception ex) {
			
			ex.printStackTrace();
			
		}
	}
	
	void parseLine(String line) {
		
		String[] splitLine = line.split(splitter);
		double longitude = Double.parseDouble(splitLine[longitudeIndex]);
		double latitude = Double.parseDouble(splitLine[latitudeIndex]);
		
		Point closestPoint = findClosestPoint(longitude,latitude);
		
		if(	!(	closestPoint.x == -1
				|| closestPoint.y == -1)) {
				
			for(int i = 0; i < columnIndexesOfInterest.length; i++) {
			
				metadataArray[closestPoint.x][closestPoint.y][i] = splitLine[columnIndexesOfInterest[i]];
			
			}
		}
	}
	
	Point findClosestPoint(double targetLongitude, double targetLatitude) {
				
		double closestLongitudeDifference = 500; // Longitude ranges from -180 to 180, so actual max difference is 360
		double closestLatitudeDifference = 500; // Latitude ranges from -90 to 90, so actual max difference is 180
		
		double currentLongitudeDifference;
		double currentLatitudeDifference;
		
		Point currentPoint = new Point(-1,-1);
		Point closestPoint = new Point(-1,-1);
		
		for(int x = 0; x < simulator.getBoard().getCurrentState().getWidth(); x++) {
			for(int y = 0; y < simulator.getBoard().getCurrentState().getHeight(); y++) {
				
				currentPoint.setLocation(x,y);
				currentLongitudeDifference = Math.abs(targetLongitude - geography.longitudeArray[x][y]);
				currentLatitudeDifference = Math.abs(targetLatitude - geography.latitudeArray[x][y]);
				
				if(currentLongitudeDifference < closestLongitudeDifference
						&& currentLatitudeDifference < closestLatitudeDifference
						&& !usedPoints.contains(currentPoint)) {
				
					closestPoint.setLocation(x,y);
					closestLongitudeDifference = currentLongitudeDifference;
					closestLatitudeDifference = currentLatitudeDifference;
					
				}
			
			}
		}
		
		usedPoints.add(closestPoint);
		return closestPoint;
		
	}
	
	void inferMetadataValues() {
		
		if(metadataArray != null) {
			
			inferredMetadataPoints = new ArrayList<Point>();
			
			ArrayList<HashMap<String,Integer>> metadataQuantities = calculateMetadataQuantities(); 
				/*
				 * Returns the number of entries for each metadata value.
				 */
			
			ArrayList<HashMap<String,Integer>> metadataQuotas = calculateMetadataQuotas(metadataQuantities); 
				/* 
				 * Calculates how many metadata-less cells in the automaton need to be assigned each
				 * metadata value in order for it to reflect the ratios in
				 * the .csv file.
				 */
			
			fulfillQuotas(metadataQuotas);
			
		}
		
	}
	
	ArrayList<HashMap<String,Integer>> calculateMetadataQuantities() {
		
		ArrayList<HashMap<String,Integer>> metadataQuantities = new ArrayList<HashMap<String,Integer>>();
		
		try{
		
			BufferedReader in = new BufferedReader(new FileReader(metadataFile));
			in.readLine(); // get past the first line containing the column headings, which we're not interested in
			
			for(int i = 0; i < categoryIdentifiers.length; i++) {
				
				HashMap<String,Integer> valuesAndQuantity = new HashMap<String,Integer>();
					/*
					 * There is one of these hashmaps per metadata category. Each entry
					 * in the hashmap maps an actual metadata value (e.g., "male") to
					 * the number of entries in the .csv file that have that value.
					 */
				
				metadataQuantities.add(valuesAndQuantity);
				
			}
			
			String line;
		
			while((line = in.readLine()) != null) {
				
				String[] splitLine = line.split(splitter);
				
				String currentValue;
				for(int i = 0; i < categoryIdentifiers.length; i++) {
					
					//String currentCategory = categoryIdentifiers[i];
					int currentCategoryIndex = columnIndexesOfInterest[i];
					currentValue = splitLine[currentCategoryIndex];
					HashMap<String,Integer> categoryValues = metadataQuantities.get(i);
					
					if(categoryValues.containsKey(currentValue)) {
					
						Integer currentNumber = categoryValues.get(currentValue);
						categoryValues.put(currentValue, currentNumber + 1);
							
					} else {
						
						categoryValues.put(currentValue, 1);
						
					}
				}
				
			}		
			
		in.close();
			
		} catch(Exception ex) {
			ex.printStackTrace();
		}
		
		return metadataQuantities;
		
	}
	
	ArrayList<HashMap<String,Integer>> calculateMetadataQuotas(ArrayList<HashMap<String,Integer>> metadataQuantities) {
		
		int totalCellsInAutomaton = simulator.getBoard().getCurrentState().getNumberOfCells() - simulator.getBoard().getCurrentState().getNumberOfCellsOfType(CellState.IMPASSABLE);
		int cellsWithoutMetadata = totalCellsInAutomaton - metadataEntries;
		ArrayList<HashMap<String,Integer>> metadataQuotas = new ArrayList<HashMap<String,Integer>>();
		
		// Initialize metadataQuotas
		for(int i = 0; i < categoryIdentifiers.length; i++) {
			
			HashMap<String,Integer> valuesAndQuota = new HashMap<String,Integer>();
				/*
				 * There is one of these hashmaps per metadata category. Each entry
				 * in the hashmap maps an actual metadata value (e.g., "male") to
				 * the number of entries in the .csv file that have that value.
				 */
			
			metadataQuotas.add(valuesAndQuota);
			
		}
		
		for(int i = 0; i < metadataQuantities.size(); i++) {

			Debug.printLine("Cells without metadata: " + cellsWithoutMetadata);
			Debug.printLine("Number of informants: " + metadataEntries);
			Debug.printLine("metadataQuantities length: " + metadataQuantities.size());
			
			int cellsAssignedForThisCategory = 0;
			
			HashMap<String,Integer> metadataQuantitiesCategory = metadataQuantities.get(i);
			HashMap<String,Integer> metadataQuotasCategory = metadataQuotas.get(i);
			
			for(Map.Entry<String,Integer> entry : metadataQuantitiesCategory.entrySet()) {
								
				double ratio = (double) cellsWithoutMetadata/metadataEntries;
				Debug.printLine("Ratio: " + ratio);
				
				int quota = (int) (entry.getValue() * ratio); 
				/*
				 *  Casting to an int truncates the calculated value, it does not round it.
				 *  This is good because it causes us to underestimate the number of cells
				 *  to assign inferred metadata values for, after which we can calculate
				 *  the number of still metadata-less cells and apportion them metadata as
				 *  desired.
				 */
				
				Debug.printLine("For entry " + entry + " the quota was " + quota);
				
				metadataQuotasCategory.put(entry.getKey(), quota);
				cellsAssignedForThisCategory += quota;
				
			}
			
			int remainderForThisCategory = cellsWithoutMetadata - cellsAssignedForThisCategory;
			Debug.printLine("Remainder: " + remainderForThisCategory);
			
			handleRemainders(metadataQuotasCategory, remainderForThisCategory); // Adds to the quotas to take care of any remainders
		}
		
		return metadataQuotas;
		
	}
	
	void handleRemainders(HashMap<String,Integer> metadataQuotasCategory, int remainder) {
		for(int i = remainder; i > 0; i--) {
			
			String[] metadataQuotasKeys = metadataQuotasCategory.keySet().toArray(new String[1]);
			int randomIndex = (int) (metadataQuotasKeys.length*Math.random());
			String randomMetadataValue = metadataQuotasKeys[randomIndex];
			int previousQuota = metadataQuotasCategory.get(randomMetadataValue);
			metadataQuotasCategory.put(randomMetadataValue, previousQuota + 1);
			
		}
	}
	
	void fulfillQuotas(ArrayList<HashMap<String,Integer>> metadataQuotas) {
		
		cellsWithoutMetadata = getCellsWithoutMetadata(); // Also excludes impassable cells
		
		for(int i = 0; i < metadataQuotas.size(); i++) {
			
			HashMap<String,Integer> currentCategory = metadataQuotas.get(i);
			
			for(Map.Entry<String,Integer> entry : currentCategory.entrySet()) {
			
				String currentMetadataValue = (String) entry.getKey();
				
				for(int currentQuota = (Integer) entry.getValue(); currentQuota > 0; currentQuota--) {
					
					try {
						
						assignMetadataValueToRandomCell(currentMetadataValue, i);
							/* Assigns the currentMetadataValue to a random cell.
							 * Throws an exception if there are no cells remaining without
							 * metadata.
							 */
					} catch(Exception ex) {
						ex.printStackTrace();
					}
					
				}
				
			}
		}
		
	}

	void assignMetadataValueToRandomCell(String metadataValue, int metadataIndex) throws NoFreeCellsException {
		
		int randomCellIndex = (int) (cellsWithoutMetadata.size() * Math.random());
		Point randomCell = cellsWithoutMetadata.get(randomCellIndex);
				
		if(metadataArray[randomCell.x][randomCell.y][metadataIndex] == null) {
			metadataArray[randomCell.x][randomCell.y][metadataIndex] = metadataValue;
			inferredMetadataPoints.add(randomCell);
		} else {
			
			HashSet<Point> forbiddenCellIndexes = new HashSet<Point>();
			forbiddenCellIndexes.add(randomCell);
			
			assignMetadataValueToRandomCell(metadataValue, metadataIndex, forbiddenCellIndexes);
			
		}
	}
	
	void assignMetadataValueToRandomCell(String metadataValue, int metadataIndex, HashSet<Point> forbiddenCellIndexes) throws NoFreeCellsException {
		
		if(forbiddenCellIndexes.size() >= cellsWithoutMetadata.size()) {
			
			throw new NoFreeCellsException();
			
		} else {
			
			int randomCellIndex = (int) (cellsWithoutMetadata.size() * Math.random());
			Point randomCell = cellsWithoutMetadata.get(randomCellIndex);
					
			if(metadataArray[randomCell.x][randomCell.y][metadataIndex] == null) {
				metadataArray[randomCell.x][randomCell.y][metadataIndex] = metadataValue;
				inferredMetadataPoints.add(randomCell);
			} else {
				
				forbiddenCellIndexes.add(randomCell);
				
				try{
					assignMetadataValueToRandomCell(metadataValue, metadataIndex, forbiddenCellIndexes);
				} catch(Exception ex) {
					
					ex.printStackTrace();
					
				}
			}
			
		}
		
	}
	
	void initializeMetadataArray(String[][][] array) {
		
		for(int x = 0; x < metadataArray.length; x++) {
			for(int y = 0; y < metadataArray[x].length; y++) {
				for(int m = 0; m < metadataArray[x][y].length; m++) { // "m" stands for metadata
					
					array[x][y][m] = null;
					
				}
			}
		}
		
	}
	
	ArrayList<Point> getCellsWithoutMetadata() {
		
		ArrayList<Point> cellsWithoutMetadata = new ArrayList<Point>();
		
		for(int x = 0; x < metadataArray.length; x++) {
			for(int y = 0; y < metadataArray[x].length; y++) {
				
				if(metadataArray[x][y][0] == null) {
					cellsWithoutMetadata.add(new Point(x,y));
				}
				
			}
		}
		
		return cellsWithoutMetadata;
		
	}
	
	int findNumberOfMetadataEntries() {
		
		int i = 0;
		
		try{
			
			BufferedReader in = new BufferedReader(new FileReader(metadataFile));
			
			in.readLine(); // read the first line, which is column headings rather than data
			
			while(in.readLine() != null) {
				
				i++;
				
			}
			
			in.close();
			
		} catch(Exception ex) {
			
			ex.printStackTrace();
			
		}
		
		return i;
		
	}
	
	void findLongitudeAndLatitudeColumns(String[] array) {
		
		int i = 0;
		for(String s : array) {
			
			if(s.equals(longitudeHeading)) {
				
				longitudeIndex = i;
				
			}
			
			if(s.equals(latitudeHeading)) {
				
				latitudeIndex = i;
				
			}
			
			i++;
		}
		
	}
	
	void buildMetadataWindow(String[] columnHeadingsArray) {
		
		metadataWindow = new JFrame("Metadata"); 
		JPanel metadataPanel = new JPanel();
		metadataPanel.setLayout(new FlowLayout(FlowLayout.CENTER,5,5));
		
		checkBoxArray = new JCheckBox[columnHeadingsArray.length];
		
		for(int i = 0; i < columnHeadingsArray.length; i++) {
			
			JCheckBox checkBox = new JCheckBox(columnHeadingsArray[i]);
			checkBoxArray[i] = checkBox;
			metadataPanel.add(checkBox);
			
		}
		
		JPanel buttonsPanel = new JPanel(new GridBagLayout());
			JButton parseMetadataButton = new JButton("Parse metadata");
			parseMetadataButton.addActionListener(new ParseMetadataListener());
			JButton inferMetadataButton = new JButton("Infer metadata");
			inferMetadataButton.addActionListener(new InferMetadataListener());
			
			buttonsPanel.add(parseMetadataButton);
			buttonsPanel.add(inferMetadataButton);
		
		JLabel instructionsLabel = new JLabel(" Select the columns that you want to include as metadata in the automaton.");
		
		metadataWindow.add(instructionsLabel,BorderLayout.NORTH);
		metadataWindow.add(metadataPanel,BorderLayout.CENTER);
		metadataWindow.add(buttonsPanel,BorderLayout.SOUTH);
		
		metadataWindow.setBounds(	userInterface.mainFrame.getX() + 20, 
				userInterface.mainFrame.getY() + 20, 
				300,
				300);

		metadataWindow.setVisible(true);
		
	}
	
	public void save(ObjectOutputStream out) {
		
		Object[] savedMetadata = new Object[5];
		
		savedMetadata[0] = metadataArray;
		savedMetadata[1] = columnIndexesOfInterest;
		savedMetadata[2] = categoryIdentifiers;
		savedMetadata[3] = columnHeadingsArray;
		savedMetadata[4] = metadataFile;
		
		try{
			
			out.writeObject(savedMetadata);
			
		}catch(Exception ex) {
			
			ex.printStackTrace();
			
		}
	}
	
	public void load(ObjectInputStream in) {
		
		try{
			
			Object[] loadedMetadata = (Object[]) in.readObject();
			
			String[][][] loadedMetadataArray = (String[][][]) loadedMetadata[0];
			int[] loadedColumnIndexesOfInterest = (int[]) loadedMetadata[1];
			String[] loadedCategoryIdentifiers = (String[]) loadedMetadata[2];
			String[] loadedColumnHeadingsArray = (String[]) loadedMetadata[3];
			File loadedMetadataFile = (File) loadedMetadata[4];
			
			metadataArray = loadedMetadataArray;
			columnIndexesOfInterest = loadedColumnIndexesOfInterest;
			categoryIdentifiers = loadedCategoryIdentifiers;
			columnHeadingsArray = loadedColumnHeadingsArray;
			metadataFile = loadedMetadataFile;
			
			if(metadataArray != null) {
				userInterface.boardPanel.setToolTipText("");
			}
			
		} catch(Exception ex) {
			ex.printStackTrace();
		}
		
	}
	
	class ParseMetadataListener implements ActionListener {
		public void actionPerformed(ActionEvent ev) {
			parseMetadata();
		}
	}
	
	class InferMetadataListener implements ActionListener {
		
		public void actionPerformed(ActionEvent ev) {
			inferMetadataValues();
		}
		
	}
	
}
