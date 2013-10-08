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

import gem.ui.board_panel.BoardPanel.MouseButton;
import gem.ui.board_panel.board_image.ImageRenderer.ImageDisplaySettings;
import gem.simulation.ISimulationStoppedListener;
import gem.simulation.board.IBoardDidIterateListener;
import gem.simulation.state.IState;
import gem.simulation.state.ICell.CellState;
import gem.talk_to_outside_world.AutomatonSerializable;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.util.*;
import java.util.Map.*;

import javax.swing.*;
import javax.swing.event.*;
import java.io.*;

public class Geography implements AutomatonSerializable, IBoardDidIterateListener, ISimulationStoppedListener {

	JFrame geoWindow;
	GeocodingPanel geocodingPanel;
	JScrollPane boardScrollPane;
	JViewport viewport;
	Region[][] regions;
	
	public boolean calculateSpatialAutocorrelation = false;
	public double saDistance = 1; // Spatial autocorrelation distance
	HashMap<Point,double[]> referencePointMap; 
	/* Maps X,Y coordinates of cells to longitude and latitude values 
	 * which are stored in an int array of the form {longitude,latitude}
	 * 
	 * In other words, the Key is a Point object which encodes
	 * a cell's X,Y coordinates on the grid. The Value is an int[]
	 * whose first element represents a latitude value and whose
	 * second element represents a longitude value. 
	 * 
	 * Note: Longitude specifies east-west position, latitude specifies
	 * north-south position.
	 */
	
	SpinnerNumberModel longitudeSpinnerModel;
	SpinnerNumberModel latitudeSpinnerModel;
	
	JSpinner longitudeSpinner;
	JSpinner latitudeSpinner;
	
	Point currentCell = new Point(-1,-1);
		/* This variable is used to identify the
		 * x,y coordinates of the last cell clicked.
		 */
	
	public double[][] longitudeArray;
	public double[][] latitudeArray;
	public File spatialAutocorrelationLogFile;
	BufferedWriter spatialAutocorrelationLogger;
	
	public void buildWindow() {
		
		geoWindow = new JFrame();
		
		buildBoardArea();
		buildControlArea();
		
		geoWindow.setBounds(	userInterface.mainFrame.getX() + 20, 
								userInterface.mainFrame.getY() + 20, 
								(int) (userInterface.mainFrame.getWidth()*0.9),
								(int) (userInterface.mainFrame.getHeight()*0.9));
		
		geoWindow.setVisible(true);
		
		geocodingPanel.zoomToFit();
		geocodingPanel.refreshBoardImage();
		
	}
	
	void buildBoardArea() {
		
		geocodingPanel = new GeocodingPanel();
		
		// Put the map panel inside an inset panel which provides some visual padding,
		// then put the inset panel inside the scrollpane
		GeoInsetPanel insetPanel = new GeoInsetPanel(new GridBagLayout());
		insetPanel.add(geocodingPanel, new GridBagConstraints());
		
		boardScrollPane = new JScrollPane(insetPanel);
		viewport = boardScrollPane.getViewport();
		
		geoWindow.add(boardScrollPane, BorderLayout.CENTER);
		
	}
	
	void buildControlArea() {
		
		JPanel controlPanel = new JPanel();
		controlPanel.setLayout(new BorderLayout());
		
			JPanel longLatPanel = new JPanel();
			longLatPanel.setBorder(BorderFactory.createEmptyBorder(10,5,10,5));
			longLatPanel.setLayout(new GridLayout(2,2));
			
			GridBagConstraints longLatPanelConstraints = new GridBagConstraints();
			longLatPanelConstraints.anchor = GridBagConstraints.NORTH;
			
			JLabel longitudeLabel = new JLabel("Longitude:");
			longitudeSpinnerModel = new SpinnerNumberModel();
			longitudeSpinnerModel.setValue(0.0);
			longitudeSpinnerModel.setStepSize(0.01);
			longitudeSpinner = new JSpinner(longitudeSpinnerModel);
			longitudeSpinner.addChangeListener(new GeoSpinnerListener());
			
			JLabel latitudeLabel = new JLabel("Latitude:");
			latitudeSpinnerModel = new SpinnerNumberModel();
			latitudeSpinnerModel.setValue(0.0);
			latitudeSpinnerModel.setStepSize(0.01);
			latitudeSpinner = new JSpinner(latitudeSpinnerModel);
			latitudeSpinner.addChangeListener(new GeoSpinnerListener());
		
			longLatPanel.add(longitudeLabel);
			longLatPanel.add(longitudeSpinner);
			longLatPanel.add(latitudeLabel);
			longLatPanel.add(latitudeSpinner);
		
			controlPanel.add(longLatPanel, BorderLayout.NORTH);
			
		JPanel interpolatePanel = new JPanel(new GridBagLayout());
			JButton interpolateButton = new JButton("Interpolate");
			interpolateButton.addActionListener(new InterpolateListener());
			GridBagConstraints interpolateButtonConstraints = new GridBagConstraints();
			interpolateButtonConstraints.anchor = GridBagConstraints.SOUTH;
			interpolatePanel.add(interpolateButton);
		
		controlPanel.add(interpolatePanel, BorderLayout.CENTER);
		
		geoWindow.add(controlPanel, BorderLayout.WEST);
		
	}
	
	public double getProcessedLongitudeSpinnerValue() {
		
		if(longitudeSpinner.getValue() instanceof Integer) {
			
			Integer value = (Integer) longitudeSpinner.getValue();
			
			return value;
			
		} else if(longitudeSpinner.getValue() instanceof Double) {
			
			Double value = (Double) longitudeSpinner.getValue();
			
			return value;
			
		} else {
			
			longitudeSpinner.setValue(0.0);
			return 0;
			
		}
		
	}
	
	public double getProcessedLatitudeSpinnerValue() {
		
		if(latitudeSpinner.getValue() instanceof Integer) {
			
			Integer value = (Integer) latitudeSpinner.getValue();
			
			return value;
			
		} else if(latitudeSpinner.getValue() instanceof Double) {
			
			Double value = (Double) latitudeSpinner.getValue();
			
			return value;
			
		} else {
			
			longitudeSpinner.setValue(0.0);
			return 0;
			
		}
		
	}
	
	double interpolateLongitude(int targetX, int targetY) 
			throws InterpolationException {
		
		double closestDistance = Double.MAX_VALUE - 1;
		double secondClosestDistance = Double.MAX_VALUE;
		
		Point closestRP = new Point(-1,-1); // RP = reference point
		Point secondClosestRP = new Point(-1,-1); // Actual cells in the automaton cannot have negative coordinates
		
		// Iterate through all the reference points and select the two
		// which are closest to the target cell and which also:
		// 1. Do not have the same x-values as each other.
		// 2. Do not have the same x-value as the target cell.
		for(Entry<Point, double[]> entry : referencePointMap.entrySet()) {
			
			Point referencePoint = entry.getKey();
			
			// Calculate distance (in cells) between target and reference point
			
			double xDifferenceSquared = Math.pow(referencePoint.x - targetX, 2); // (referencePoint.x - targetX)^2
			double yDifferenceSquared = Math.pow(referencePoint.y - targetY, 2); // (referencePoint.y - targetY)^2
			
			double distance = Math.sqrt(xDifferenceSquared + yDifferenceSquared);
			
			if(distance < closestDistance) {
				
				if(referencePoint.x != closestRP.x) { 	// If the current RP has a different x-coordinate than the former closest RP
														
					 // Mark the former closest RP as second-closest.
					secondClosestDistance = closestDistance;
					secondClosestRP = closestRP;
				}
				
				// The current RP is now closest
				closestDistance = distance;
				closestRP = referencePoint;
				
			} else if(	distance < secondClosestDistance
						&& referencePoint.x != closestRP.x) {
				
				// The current RP is now second closest
				secondClosestDistance = distance;
				secondClosestRP = referencePoint;
				
			}
			
		}
		
		if(	(closestRP.x == -1
				&& closestRP.y == -1)
			|| (secondClosestRP.x == -1
				&& secondClosestRP.y == -1)) {
			
			throw new InterpolationException("No reference points with different X coordinates.");
			
		}
		
		// Calculate longitude per cell using closestRP and secondClosestRP
		double closestLongitude = referencePointMap.get(closestRP)[0];
		double secondClosestLongitude = referencePointMap.get(secondClosestRP)[0];
		
		double rpLongitudeDifference = Math.abs(closestLongitude - secondClosestLongitude);
		double rpLocationDifference = Math.abs(closestRP.x - secondClosestRP.x); // distance in cells between closest and second-closest RP
		
		double longitudePerCell = rpLongitudeDifference / rpLocationDifference;
		
		int horizontalDistanceToTarget = targetX - closestRP.x; // positive if target is to the right of the closest reference point
		
		// Multiply longitudePerCell by the horizontal distance in cells from the closestRP to the target,
		// yielding distance in longitude 
		double targetLongitudeDifference = longitudePerCell * horizontalDistanceToTarget; // positive if target is to the right of the closest RP
		
		// Add targetLongitudeDifference to longitude of the closest RP to determine the longitude of the target cell
		double targetLongitude = targetLongitudeDifference + closestLongitude; 	// this operation ends up being subtraction if targetLongitudeDifference is negative 
																				//(i.e., if the target is to the left of the closest RP)
		
		// Correct longitude values to make sure they're within the appropriate range for longitude (i.e., -180 < longitude < 180)
		
		while(	targetLongitude > 180
				|| targetLongitude < -180) {
		
			while(targetLongitude > 180) {
				targetLongitude = targetLongitude - 360;
			}
		
			while(targetLongitude < -180) {
				targetLongitude = targetLongitude + 360;
			}
		
		}
		
		return targetLongitude;
			
	}
	
	double interpolateLatitude(int targetX, int targetY) 
		throws InterpolationException {
		
		double closestDistance = Double.MAX_VALUE - 1;
		double secondClosestDistance = Double.MAX_VALUE;
		
		Point closestRP = new Point(-1,-1); // RP = reference point
		Point secondClosestRP = new Point(-1,-1); // Actual cells in the automaton cannot have negative coordinates
		
		// Iterate through all the reference points and select the two
		// which are closest to the target cell and which also:
		// 1. Do not have the same x-values as each other.
		// 2. Do not have the same x-value as the target cell.
		for(Entry<Point, double[]> entry : referencePointMap.entrySet()) {
			
			Point referencePoint = entry.getKey();
			
			// Calculate distance (in cells) between target and reference point
			
			double xDifferenceSquared = Math.pow(referencePoint.x - targetX, 2); // (referencePoint.x - targetX)^2
			double yDifferenceSquared = Math.pow(referencePoint.y - targetY, 2); // (referencePoint.y - targetY)^2
			
			double distance = Math.sqrt(xDifferenceSquared + yDifferenceSquared);
			
			if(distance < closestDistance) {
				
				if(referencePoint.y != closestRP.y) { 	// If the current RP has a different x-coordinate than the former closest RP
														
					 // Mark the former closest RP as second-closest.
					secondClosestDistance = closestDistance;
					secondClosestRP = closestRP;
				}
				
				// The current RP is now closest
				closestDistance = distance;
				closestRP = referencePoint;
				
			} else if(	distance < secondClosestDistance
						&& referencePoint.y != closestRP.y) {
				
				// The current RP is now second closest
				secondClosestDistance = distance;
				secondClosestRP = referencePoint;
				
			}
			
		}
		
		if(	(closestRP.x == -1
				&& closestRP.y == -1)
			|| (secondClosestRP.x == -1
				&& secondClosestRP.y == -1)) {
			
			throw new InterpolationException("No reference points with different Y coordinates.");
			
		}
		
		// Calculate longitude per cell using closestRP and secondClosestRP
		double closestLatitude = referencePointMap.get(closestRP)[1];
		double secondClosestLatitude = referencePointMap.get(secondClosestRP)[1];
		
		double rpLatitudeDifference = Math.abs(closestLatitude - secondClosestLatitude);
		double rpLocationDifference = Math.abs(closestRP.y - secondClosestRP.y); // distance in cells between closest and second-closest RP
		
		double LatitudePerCell = rpLatitudeDifference / rpLocationDifference;
		
		int verticalDistanceToTarget = closestRP.y - targetY; 	// positive if target is above the closest reference point
																// (Remember, the positive Y-axis points downward in Java)
		
		// Multiply latitudePerCell by the vertical distance in cells from the closestRP to the target,
		// yielding distance in latitude 
		double targetLatitudeDifference = LatitudePerCell * verticalDistanceToTarget; // positive if target is above the closest RP
		
		// Add targetLatitudeDifference to longitude of the closest RP to determine the longitude of the target cell
		double targetLatitude = targetLatitudeDifference + closestLatitude; 	// this operation ends up being subtraction if targetLongitudeDifference is negative 
																				//(i.e., if the target is to the left of the closest RP)
		
		// Correct longitude values to make sure they're within the appropriate range for longitude (i.e., -180 < longitude < 180)
		
		while(	targetLatitude > 180
				|| targetLatitude < -180) {
		
			while(targetLatitude > 180) {
				targetLatitude = targetLatitude - 360;
			}
		
			while(targetLatitude < -180) {
				targetLatitude = targetLatitude + 360;
			}
		
		}
		
		return targetLatitude;
		
	}
	
	void interpolateEntireBoard() {
		
		longitudeArray = new double[simulator.getBoard().getCurrentState().getWidth()][simulator.getBoard().getCurrentState().getHeight()];
		latitudeArray = new double[simulator.getBoard().getCurrentState().getWidth()][simulator.getBoard().getCurrentState().getHeight()];
		
		try{
		
			for(int x = 0; x < longitudeArray.length; x++) {
				for(int y = 0; y < longitudeArray[x].length; y++) {		
					
					double longitude = interpolateLongitude(x,y);
					double latitude = interpolateLatitude(x,y);
					
					longitudeArray[x][y] = longitude;
					latitudeArray[x][y] = latitude;
											
				}		
			}
		
		} catch(InterpolationException ex) {
		
			ex.printStackTrace();
			
		}
		
	}
	
	@Override
	public void automatonStopped(IState finalBoardState) {
		closeSpatialAutocorrelationLog();
	}

	@Override
	public void boardDidCalculateNewState(IState newState, int generationNumber) {
		if(calculateSpatialAutocorrelation) {			
			geography.calculateSpatialAutocorrelation(newState);
		}
	}
	
	public void calculateSpatialAutocorrelation(IState currentBoardState) {
		
		double cellStateAverage = simulator.getCellStateAverage();
		double weightTotal = 0;
		double numerator = 0;
		double denominatorLeft = 0;
		
		for(int x = 0; x < currentBoardState.getWidth(); x++) {
			for(int y = 0; y < currentBoardState.getHeight(); y++) {
							
				double currentValue = 0;
				double neighborValue = 0;
				
				Point currentPoint = new Point(x,y);
				ArrayList<Point> neighbors = getNeighborsWithin(saDistance,currentPoint);
				
				if(currentBoardState.getCell(x, y).getState() == CellState.ALIVE) { currentValue = 1; }
				
				denominatorLeft += Math.pow(currentValue - cellStateAverage,2);
								
				for(Point neighbor : neighbors) { // For all combinations of i and j where w = 1, whether the cell value is 1 or not
										
					if(currentBoardState.getCell(neighbor.x, neighbor.y).getState() == CellState.ALIVE) { neighborValue = 1; }
					
					numerator += (currentValue - cellStateAverage)*(neighborValue - cellStateAverage);
					weightTotal++;
					
				}
				
			}
			
		}
		
		numerator *= currentBoardState.getNumberOfCells();
		
		double moransI = numerator/(denominatorLeft*weightTotal);
		Debug.printLine("I: " + moransI);
		Debug.newLine();
		
		try{
			spatialAutocorrelationLogger.write(moransI + ",");
		} catch(Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public synchronized ArrayList<Point> getNeighborsWithin(double distance, Point target) {
		
		// Excludes the cell itself
		
		ArrayList<Point> neighbors = new ArrayList<Point>();
		
		for(int x = 0; x < simulator.getBoard().getCurrentState().getWidth(); x++) {
			for(int y = 0; y < simulator.getBoard().getCurrentState().getHeight(); y++) {
			
				Point currentPoint = new Point(x,y);
				if(currentPoint.distance(target) <= distance
						&& !currentPoint.equals(target)) {
					neighbors.add(currentPoint);
				}
				
			}
		}
		
		return neighbors;
		
	}

	public void setRegionDimensions(int x, int y) {
		
		/*
		 *  This creates AT LEAST x horizontal regions and y vertical regions.
		 *  If x doesn't divide equally into the board width, it creates x + 1
		 *  horizontal regions. If y doesn't divide equally into board height,
		 *  it creates y + 1 regions.
		 */
		
		int boardWidth = simulator.getBoard().getCurrentState().getWidth();
		int boardHeight = simulator.getBoard().getCurrentState().getHeight();
	
		int regionWidth = (int) Math.floor(boardWidth/x);
		int regionHeight = (int) Math.floor(boardHeight/y);
		
		int remainderWidth = boardWidth - (regionWidth*x);
		int remainderHeight = boardHeight - (regionHeight*y);
		
		int numberOfHorizontalRegions;
		int numberOfVerticalRegions;
		
		if(remainderWidth != 0) {
			numberOfHorizontalRegions = x + 1;
		} else {
			numberOfHorizontalRegions = x;
		}
		
		if(remainderHeight != 0) {
			numberOfVerticalRegions = y + 1;
		} else {
			numberOfVerticalRegions = y;
		}

		regions = new Region[numberOfHorizontalRegions][numberOfVerticalRegions];
		
		Debug.printLine("Board width = " + boardWidth);
		Debug.printLine("Board height = " + boardHeight);
		Debug.printLine("Region width = " + regionWidth);
		Debug.printLine("Region height = " + regionHeight);
		Debug.printLine("Remainder width = " + remainderWidth);
		Debug.printLine("Remainder height = " + remainderHeight);
	
		int finalHorizontalIndex = regions.length - 1;
		int finalVerticalIndex = regions[0].length - 1;
		
		for(int i = 0; i < regions.length; i++) { // Loop through all the regions that have
			for(int j = 0; j < regions[i].length; j++) { // a full regionWidth and regionHeight
				Region currentRegion = new Region();
				
				currentRegion.setXOffset(i*regionWidth);
				currentRegion.setYOffset(j*regionHeight);
				
				if(	i == finalHorizontalIndex
					&& remainderWidth != 0 ) { // If you're in the last column and there is a remainder
					currentRegion.setWidth(remainderWidth);
				} else {
					currentRegion.setWidth(regionWidth);	
				}
				
				if( j == finalVerticalIndex
						&& remainderHeight != 0) {
					currentRegion.setHeight(remainderHeight);
				} else {
					currentRegion.setHeight(regionHeight);
				}
				regions[i][j] = currentRegion;
			}
		}
	
		// For debugging
		for(int i = 0; i < regions.length; i++) {
			for(int j = 0; j < regions[i].length; j++) {
				Debug.printLine("Region " + i + "," + j + " has width " + regions[i][j].getWidth() + " and height " + regions[i][j].getHeight());
				Debug.printLine("X offset is: " + regions[i][j].getXOffset() + " and Y offset is: " + regions[i][j].getYOffset());
				Debug.newLine();
			}
		}
	}
	
	public synchronized void tallyRegions(File file) {	
		try {
			FileWriter out = new FileWriter(file);
			BufferedWriter writer = new BufferedWriter(out);
			
			for(int x = 0; x < regions.length; x++) {
				for(int y = 0; y < regions[x].length; y++) {
					writer.write(regions[x][y].countCellsInRegion() + "");
					
					if(	x != regions.length - 1
						|| y != regions[x].length - 1) {
						writer.write(",");
					}
				}
			}
			writer.write("\n");
			writer.close();
		} catch(Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public void openSpatialAutocorrelationLog() {
		
		try {
			FileWriter out = new FileWriter(spatialAutocorrelationLogFile, true);
			spatialAutocorrelationLogger = new BufferedWriter(out);
			spatialAutocorrelationLogger.write("\n");
		} catch(Exception ex) {
			
			ex.printStackTrace();
			
		}
	}
	
	public void closeSpatialAutocorrelationLog() {
		
		try {
			
			spatialAutocorrelationLogger.close();
			
		} catch(Exception ex) {
			
			//ex.printStackTrace(); TODO: UNCOMMENT THIS AND ACTUALLY MAKE A SOLUTION
			
		}
		
	}
	
	public void save(ObjectOutputStream out) {
		
		try{
			
			Object[] savedGeography = new Object[2];
			savedGeography[0] = longitudeArray;
			savedGeography[1] = latitudeArray;
			
			out.writeObject(savedGeography);
			
		} catch(Exception ex) {
			
			ex.printStackTrace();
			
		}
		
	}
	
	public void load(ObjectInputStream in) {
		
		try{
			
			Object[] loadedGeography = (Object[]) in.readObject();
			
			double[][] loadedLongitudeArray = (double[][]) loadedGeography[0];
			double[][] loadedLatitudeArray = (double[][]) loadedGeography[1];
			
			longitudeArray = loadedLongitudeArray;
			latitudeArray = loadedLatitudeArray;
			
		} catch(Exception ex) {
			
			ex.printStackTrace();
			
		}
		
	}
	
	class InterpolateListener implements ActionListener {
		
		public void actionPerformed(ActionEvent e) {
				
			interpolateEntireBoard();
				
			Debug.printLine("Longitude array:");
			Debug.print(longitudeArray);
				
			Debug.newLine();
			Debug.newLine();
			
			Debug.printLine("Latitude array:");
			Debug.print(latitudeArray);
			
			Debug.newLine();
			Debug.newLine();
			
			userInterface.boardPanel.setToolTipText("");
			
		}
		
	}
	
	class GeocodingPanel extends JPanel {
		
		public static final long serialVersionUID = 5;
		
		double longTemp = 0;
		double latTemp = 0;
		private static final int BOARD_MIN_SIZE = 100;
		
		MouseButton currentButtonPressed;
		
		int[][] displayArray; 
			/*
			 *  Has the same form as the world array; 
			 *  used to display the iteration of the automaton
			 *  that is currently playing back.
			 */
		
		MemoryImageSource boardImageSource; 
			/*
			 *  Object used to create an image from the boardImageArray; 
			 *  the image created by this object gets painted to the board 
			 *  and displays the current state of the automaton
			 */
		
		int[] boardImageArray; 
			/*
			 *  1D int array containing pixel values; 
			 *  used by MemoryImageSource to generate the automaton board image 
			 *  from the display array
			 */
		
		Image boardImage;	
			/*
			 * The checkerboard-like image that shows the state of the board; 
			 * generated by boardImageSource from an array of pixel values 
			 */
		
		BufferedImage map;
		
		boolean showMap; // Boolean flag which declares whether to show the map panel
		
		ImageDisplaySettings mapDisplaySettings; // Enum instance
		
		public GeocodingPanel() {
			
			/*
			 * Constructs a new board panel. Also adds this
			 * as a mouse listener and mouse motion listener 
			 * for itself. (These listeners are used for
			 * capturing user modification of the board state and
			 * custom scrolling.) Finally, creates a MemoryImageSource
			 * which converts arrays into the visualization
			 * of the automaton's state.
			 * 
			 */
			
			super();
			
			showMap = true;
							
			map = userInterface.boardPanel.map;
			
			// The program defaults to stretching the map to fit the board
			mapDisplaySettings = ImageDisplaySettings.STRETCH_TO_FIT_BOARD;
			
			displayArray = new int[simulator.getBoard().getCurrentState().getWidth()][simulator.getBoard().getCurrentState().getHeight()];
			boardImageArray = new int[displayArray.length*displayArray[0].length];
			
			referencePointMap = new HashMap<Point,double[]>();
			
			GeoMouseListener listener = new GeoMouseListener();
			
			this.addMouseListener(listener);
			this.addMouseMotionListener(listener);
			
			// Create the boardImageSource (used by the boardPanel) which takes the 
			// boardImageArray and turns it into an image (stored in the boardImage variable)		
			boardImageSource = new MemoryImageSource(
						displayArray.length, displayArray[0].length,
						boardImageArray, 0, displayArray.length
					);
		}
		
		public void paint(Graphics g) {
			
			/*
			 * Paints the board area.
			 * 
			 * Distinguishes between two cases:
			 * 	
			 * 	- If there is a map in memory and the user has opted to load the map:
			 * 		Draw the map first at full opacity, then draw the boardImage (which was created with only partial opacity).
			 * 		(Scale or stretch the map as appropriate, according to the user's preferences.)
			 * 
			 * 	- Else:
			 * 		Draw just the board image at full opacity.
			 * 
			 * The board image is the checkerboard looking image which visualizes the current cell
			 * states of the world array.
			 * 
			 */
			
			Graphics2D g2d = (Graphics2D) g;
			
			// If there is a current map image, and the user has opted to display maps
			if(		showMap &&
					(map != null) ) {
				
				switch(mapDisplaySettings) {
				
					case STRETCH_TO_FIT_BOARD:
						// Draw the map
						g2d.drawImage(map, // the image to draw
							0,0,this.getWidth(),this.getHeight(), // the dimensions of the image (draw the image so it is as wide and tall as the boardPanel)
							null); // the image observer
						
						// Draw the board image
						g2d.drawImage(boardImage,0,0,this.getWidth(),this.getHeight(), null); // draw the board
						
						break;
						
					case SCALE_TO_FILL_BOARD:
						// Calculate the appropriate scale
							double xScale = (double) this.getWidth()/map.getWidth();
							double yScale = (double) this.getHeight()/map.getHeight();
							double scale = Math.max(xScale,yScale);
							
							int mapWidth = (int) (map.getWidth()*scale);
							int mapHeight = (int) (map.getHeight()*scale);
							int x = (Global.userInterface.boardPanel.getWidth() - mapWidth)/2;
							int y = (Global.userInterface.boardPanel.getHeight() - mapHeight)/2;
						
						// Draw the map
						g2d.drawImage(map,
							x,y, // points of origin
							mapWidth, mapHeight, // dimensions of the image
							null); // the image observer
						
						// Draw the board image
						g2d.drawImage(boardImage,0,0,this.getWidth(),this.getHeight(), null); // draw the board
						
						break;	
						
				}
			
			} else {
			
				// Draw the board image
				g2d.drawImage(boardImage,0,0,this.getWidth(),this.getHeight(), null);
				
			}
			
		}
		
		public void refreshBoardImage() {
			
			/*
			 * Takes care of all the actions needed to
			 * refresh the board image. First updates
			 * the boardImageArray to reflect the current
			 * state of the world array, then creates an
			 * image from the array using the board image source.
			 * Finishes by repainting the board panel.
			 * 
			 */
			
			updateBoardImageArray();
			
			// Build the image
			boardImage = createImage(boardImageSource);
			
			repaint();
			
		}
		
		public void updateBoardImageArray() {
			
			/*
			 * Converts the two-dimensional, integer-containing world array
			 * into a one-dimensional, pixel-information-containing array.
			 * 
			 * If the the cell is alive (state 1), the pixel is black.
			 * If the cell is dead (state 0), the pixel is white.
			 * If the cell is impassable (state -2), the pixel is grey.
			 * 
			 * If the user has set to display maps and there is a map to show,
			 * the pixels are encoded with only partial opacity.
			 * 
			 */
			
			// Used to turn the two-dimensional world-array into a 1-dimensional array
			int index = 0;
			
			if(		showMap &&
					(map != null)) { // if the user has chosen to show the map panel and there is a map to show, fill the image array with translucent colors
			
				for(int y = 0; y < displayArray[0].length; y++) {
					for(int x = 0; x < displayArray.length; x++) {
						
						switch(displayArray[x][y]) {
							
							case -2: // if the cell is impassable, set color to grey
								boardImageArray[index] = (100 << 24) | (125 << 16) | (125 << 8) | 125;
								index++;
								break;
								
							case 0: // if the cell is off, set color to white
								boardImageArray[index] = (100 << 24) | (255 << 16) | (255 << 8) | 255;
								index++;
								break;	
							
							case 1: // if the cell is on, set color to black
								boardImageArray[index] = (100 << 24) | (0 << 16) | (0 << 8) | 0;
								index++;
								break;
							
						}
					}
				}
			
			} else { // otherwise, fill the image array with opaque colors
				
				for(int y = 0; y < displayArray[0].length; y++) {
					for(int x = 0; x < displayArray.length; x++) {
						
						switch(displayArray[x][y]) {
							
							case -2: // if the cell is impassable, set color to grey
								boardImageArray[index] = (255 << 24) | (125 << 16) | (125 << 8) | 125;
								index++;
								break;
								
							case 0: // if the cell is off, set color to white
								boardImageArray[index] = (255 << 24) | (255 << 16) | (255 << 8) | 255;
								index++;
								break;	
							
							case 1: // if the cell is on, set color to black
								boardImageArray[index] = (255 << 24) | (0 << 16) | (0 << 8) | 0;
								index++;
								break;
							
						}
					}
				}
			
			}
		}

		public double cellWidth() {
		
			/*
			 * Calculates and returns how wide a cell should be based on how many
			 * cells are in the horizontal dimension of the world array:
			 * 
			 * (width of board panel)/(number of columns in world array)
			 * 
			 */
			
			double cellWidth = (double) this.getWidth()/displayArray.length; // assumes a rectangular world array
			
			return cellWidth;
			
		}
		
		public double cellHeight() {
			
			double cellHeight = (double) this.getHeight()/displayArray[0].length; // assumes a rectangular world array
			
			/*
			 * Calculates and returns how tall a cell should be based on how many
			 * cells are in the vertical dimension of the world array:
			 * 
			 * (height of board panel)/(number of rows in world array)
			 * 
			 */
			
			return cellHeight;
			
		}
		
		public void zoom(double scale) {
						
			/*
			 * Zooms the automaton by increasing the size of the board panel.
			 * 
			 * Works by calculating the aspect ratio of the world array (i.e.
			 * columns/rows) then scaling the width of the board panel the
			 * desired amount. The zoomed height is calculated using the zoomed width
			 * and the aspect ratio:
			 * 
			 * zoomed height = (zoomed width)/(aspect ratio)
			 * 
			 */
			
			Dimension current = getPreferredSize();
			double aspectRatio = (double) displayArray.length/displayArray[0].length;
			int newWidth = (int) Math.round(current.getWidth()*scale);
			int newHeight = (int) Math.round(newWidth/aspectRatio);
			
			if(newWidth >= BOARD_MIN_SIZE 
					&& newHeight >= BOARD_MIN_SIZE) {
				
				Dimension newDimension = new Dimension(newWidth,newHeight);
				this.setPreferredSize(newDimension);
				this.setSize(newDimension);	
				revalidate();
				
			}
			
		}
		
		public void zoomToFit() {
			
			/*
			 * Zooms the board so that it is slightly smaller than the display area. Uses the following algorithm:
			 * 
			 * 	1. Calculate xScale, the ratio of the display area's width to the width of the board.
			 * 		Note that (board width)*xScale = (display area width)
			 * 	2. Calculate yScale, the ratio of the display area's height to the height of the board.
			 * 		Similar to xScale, (board height)*yScale = (display area height)
			 * 	3. Find which is less, xScale or yScale. Call it simply "scale." Multiplying both board
			 * 		dimensions by the lesser of the two ensures both dimensions fit within the display area dimensions.
			 * 
			 * 	4. Since multiplying both board dimensions by scale makes the board's largest dimension
			 * 		the same size as the corresponding display area dimension, we want to multiply by slightly
			 * 		less than scale so that the board fits neatly within the display area. As such, multiply
			 * 		both board dimensions by scale*0.9.
			 * 
			 */

			
			
			int scrollPaneWidth = boardScrollPane.getWidth();
			int scrollPaneHeight = boardScrollPane.getHeight();
			
			int boardWidth = getWidth();
			int boardHeight = getHeight();
						
			double xScale = (double) scrollPaneWidth/boardWidth;
			double yScale = (double) scrollPaneHeight/boardHeight;
						
			double scale = Math.min(xScale, yScale);
						
			zoom(scale*0.9);
			
		}
		
		public void stretchMapToFitBoard() {
			
			mapDisplaySettings = ImageDisplaySettings.STRETCH_TO_FIT_BOARD;
			
		}
		
		public void scaleMapToFillBoard() {
			
			mapDisplaySettings = ImageDisplaySettings.SCALE_TO_FILL_BOARD;
			
		}
		
		class GeoMouseListener implements MouseListener, MouseMotionListener {
			
			/*
			 * This class handles mouse interactions with the board. It
			 * distinguishes between three different kinds of mouse clicks/drags:
			 * 
			 * 	- BUTTON1 (left click) adds cells to the reference point map.
			 * 	- WHEEL (mouse wheel) drags the location of the viewport in the opposite direction as the
			 * 		mouse is moved, as if the board were an object and one were actually dragging it (custom scrolling). 
			 * 	- BUTTON3 (right click) removes cells from the reference point map.
			 * 
			 */
			
			Point oldMousePosition = new Point(0,0);
			
			int XDif;
			int YDif;
			int newX;
			int newY;
			
			@Override
			public void mousePressed(MouseEvent e) {
				
				//if(!memory.isTransitioning) {	
				
					int cellX = (int) (	Math.floor(e.getX()/cellWidth())	); // do some arithmetic to translate the mouse's location 
					int cellY = (int) ( Math.floor(e.getY()/cellHeight())	); // on the board to an address in the displayArray
					
					switch(e.getButton()) { // if the left mouse button was clicked
						
						case MouseEvent.BUTTON1:
							currentButtonPressed = MouseButton.LEFT;
							currentCell.setLocation(cellX,cellY);
							
							if(displayArray[cellX][cellY] == 0) { // if the selected cell is off
																
								double[] initialArray = {0,0};

								displayArray[cellX][cellY] = 1; // place whatever kind of cell the user has selected
								referencePointMap.put((Point) currentCell.clone(), initialArray);
								
								refreshBoardImage(); // then refresh the board
							
							}
							
							longitudeSpinnerModel.setValue(referencePointMap.get(currentCell)[0]);
							latitudeSpinnerModel.setValue(referencePointMap.get(currentCell)[1]);
							
							oldMousePosition.setLocation(e.getX(), e.getY()); // used for dragging to set lat. and long. values
							break;
							
						case MouseEvent.BUTTON3: // but if the right mouse button was clicked
							currentButtonPressed = MouseButton.RIGHT;
							if(displayArray[cellX][cellY] == 1) { // and the cell is in whatever state the user is placing
								
								currentCell.setLocation(cellX,cellY);
								
								displayArray[cellX][cellY] = 0; // turn the cell off
								referencePointMap.remove(currentCell);
								
								refreshBoardImage(); // then refresh the board
								
							}
							break;
						
						default: // if the user clicked neither with the left or right buttons, assume they wanted to scroll
							currentButtonPressed = MouseButton.WHEEL;
							oldMousePosition.setLocation(e.getX(), e.getY());
							currentCell.setLocation(-1,-1);
							break;
					}
					
				//}
				
			}

			public void mouseDragged(MouseEvent e) {
				
				/*
				 * First, make sure that the drag location is within the board panel and
				 * that the current cell wasn't the last one modified by a click or drag event.
				 * 
				 * Then, for left and right dragging, act on the cell as if the user had just
				 * clicked on it normally. (I.e., modify the cell state.)
				 * 
				 * If the user dragged with the mouse wheel depressed, scroll the viewport as long as you're not
				 * at the edge of the board
				 * 
				 */
				
				int cellX = (int) Math.floor(e.getX()/cellWidth()); // do some arithmetic to translate the mouse's location 
				int cellY = (int) Math.floor(e.getY()/cellHeight()); // on the board to an address in the displayArray 
				
				switch(currentButtonPressed) {
				
					case LEFT:
						
						longTemp = referencePointMap.get(currentCell)[0];
						latTemp = referencePointMap.get(currentCell)[1];
						
						if(	longTemp >= -180 // if longitude is between -180 and 180
							&& longTemp <= 180
							) {
						
							XDif = e.getX() - oldMousePosition.x;
							int newLong = (int) (longTemp + 0.7*XDif); // ideally would be a double, but looks messy
							
							if(newLong > 180) {
								newLong = 180;
							} else if (newLong < -180) {
								newLong = -180;
							}
							
							longTemp = newLong;
							
						}
						
						if(	latTemp >= -90 // if latitude is between -90 and 90
							&& latTemp <= 90
							){
							
							YDif = e.getY() - oldMousePosition.y;
							int newLat = (int) (latTemp - 0.7*YDif); // ideally would be a double, but looks messy
							
							if(newLat > 90) {
								newLat = 90;
							} else if (newLat < -90) {
								newLat = -90;
							}
							
							latTemp = newLat;
								
						}
						
						longitudeSpinnerModel.setValue(longTemp);
						latitudeSpinnerModel.setValue(latTemp);
						
						break;
					
					case RIGHT:
						if(		((cellX == currentCell.getX()) // if the cell at the current location has already been modified
								&& (cellY == currentCell.getY()) )
								|| ( e.getX() >= getWidth() ) // or if the drag location is outside the boardPanel
								|| ( e.getY() >= getHeight() )
								|| ( e.getX() <= 0 )
								|| ( e.getY() <= 0 ) 
							) { 
							
							// do nothing
						
						} else {
							
							if(displayArray[cellX][cellY] == 1) { // and the cell is in whatever state the user is placing
								
								currentCell.setLocation(cellX,cellY); // and prevent it from being modified again before exiting the cell
								
								displayArray[cellX][cellY] = 0; // turn the cell off
								referencePointMap.remove(currentCell);
								
								refreshBoardImage(); // then refresh the board
							
							}
							
						}
						break;
						
					case WHEEL:
						// Calculate how much to change the view
						XDif = (int) oldMousePosition.x - e.getX();
						YDif = (int) oldMousePosition.y - e.getY();
						newX = viewport.getViewPosition().x + XDif;
						newY = viewport.getViewPosition().y + YDif;
						
						// Then update the view position
						if(		( 0 < newX ) // If there's some board panel out of view in both directions
								&& ((newX + viewport.getWidth()) < getWidth())
								&& (0 < newY )
								&& ((newY + viewport.getHeight()) < getHeight())
							) {
							
							viewport.setViewPosition(new Point(newX,newY));
							revalidate();
							
						} else if( ( 0 < newX ) // Else, if there's some board panel out of view in just the x direction
								&& ((newX + viewport.getWidth()) < getWidth())
							) {
						
							viewport.setViewPosition(new Point(newX,newY-YDif)); // only move the view in the X dimension
							revalidate();
							
						} else if( (0 < newY ) // Else, if there's some board panel out of view in just the Y direction
								&& ((newY + viewport.getHeight()) < getHeight())
							) {
							
							viewport.setViewPosition(new Point(newX-XDif, newY)); // only move the view in the Y dimension
							revalidate();
							
						}
						break;
						
					case NONE:
						// Do nothing
			
				}
				
			}
			
			@Override
			public void mouseReleased(MouseEvent e){
				
				/*
				 * When a mouse button is released, set the variable to show that
				 * no button is being pressed. Not strictly necessary for program
				 * accuracy, but included so that currentButtonPressed accurately
				 * reflects the state of the mouse.
				 * 
				 */
				if(currentButtonPressed == MouseButton.LEFT) {
					referencePointMap.get(currentCell)[0] = getProcessedLongitudeSpinnerValue();
					referencePointMap.get(currentCell)[1] = getProcessedLatitudeSpinnerValue();
					
					longTemp = 0;
					latTemp = 0;
				}
				
				currentButtonPressed = MouseButton.NONE;
				
			}

			public void mouseEntered(MouseEvent e){}
			
			public void mouseExited(MouseEvent e){}
			
			public void mouseClicked(MouseEvent e){}
				
			public void mouseMoved(MouseEvent e){}
			
		}

	}
	
	class GeoInsetPanel extends JPanel implements MouseWheelListener {
		
		/*
		 * Simply a space-filling panel. Surrounds the geo-coding panel in the scroll pane and keeps it centered.
		 * Also implements custom zooming with the mouse wheel. (It's implemented here rather than in the
		 * board panel so that the user can use the mouse wheel to zoom anywhere in the board area.)
		 * 
		 */
		
		static final long serialVersionUID = 5;
		double scale = 1.0; // The amount to zoom
		
	// Constructor
		
		public GeoInsetPanel(LayoutManager lm) {
			
			/*
			 * Adds this as its own mouse wheel listener and
			 * calls the JPanel constructor to take care of
			 * the layout manager.
			 * 
			 */
			
			super(lm);
			addMouseWheelListener(this);
			
		}
		
	// Mouse wheel methods
		
		@Override
		public void mouseWheelMoved(MouseWheelEvent e) {

			/*
			 * If the mouse wheel is moved upwards, zoom in.
			 * If the mouse wheel is moved downwards, zoom out.
			 * 
			 */
			
			if(e.getWheelRotation() > 0) { // if the wheel was moved downwards
				
				scale = Math.pow(0.95, e.getScrollAmount()); // Multiply 0.95 by the amount the mouse wheel was moved; call this scale
				
				geocodingPanel.zoom(scale);
			
			} else { // otherwise the wheel was moved upwards
				
				scale = Math.pow(1.05, e.getScrollAmount()); // Multiply 1.05 by the amount the mouse wheel was moved; call this scale
				
				geocodingPanel.zoom(scale);
				
			}

		}
		
	}
	
	class GeoSpinnerListener implements ChangeListener {
		
		public void stateChanged(ChangeEvent ev) {
						
			if(ev.getSource() == longitudeSpinner) {
			
				double value = getProcessedLongitudeSpinnerValue();
				
				if(value < -180) {
					
					longitudeSpinnerModel.setValue(-180);
					
				} else if(value > 180) {
					
					longitudeSpinnerModel.setValue(180);
					
				} else {
				
					referencePointMap.get(currentCell)[0] = value;
				
				}
			}
			
			if(ev.getSource() == latitudeSpinner) {
				
				double value = getProcessedLatitudeSpinnerValue();
				
				if(value < -90) {
					
					latitudeSpinnerModel.setValue(-90);
					
				} else if(value > 90) {
					
					latitudeSpinnerModel.setValue(90);
					
				} else {
				
					referencePointMap.get(currentCell)[1] = value;
				
				}
				
			}
			
		}
		
	}
	
}
