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

package gem.ui;

import static gem.AutomatonGlobal.*;

import gem.simulation.BoardDimensions;
import gem.ui.BoardPanel.MapDisplaySettings;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.*;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.*;
import java.util.List;


public class MapWizard {

	/*
	 * This class builds a utility which makes it easier for
	 * a user to select an amount of cells to display on a map
	 * while preserving its aspect ratio. While doing so, they
	 * can navigate the map image display in almost
	 * exactly the same way they can navigate the board
	 * in the main part of the automaton.
	 * 
	 */
	
	static final long serialVersionUID = 5;
	
	JFrame wizardWindow; // The window which contains the entire map wizard
	JScrollPane mapScrollPane; // The scrolling area which contains the map display
		JViewport mapViewport; // The "port hole" through which you view something that is bigger than the mapScrollPane
		MapPanel mapPanel; // The panel which displays the map
	JLabel boardDimensionsLabel; // Label which shows the current board dimensions ("columns x rows")
	JLabel totalCellsLabel; // Shows the total number of cells this configuration would yield (columns times rows)
	JLabel mapDeformityLabel; // Shows the difference in pixels between the original map size and the current map size for both dimensions 
	JSlider cellDensitySlider; // Used to control the amount of cells displayed on the map
		static final int CELL_DENSITY_MIN = 10; // The cell density value at the far left of the slider
		static final int CELL_DENSITY_MAX = 150000; // The cell density value at the far right of the slider
		static final int CELL_DENSITY_INIT = 10; // The starting cell density value. The slider starts at the far left, so it's the same as the minimum.
	private static final int BOARD_MIN_SIZE = 100;
	private List<IMapDisplayListener> mapDisplayListeners;
		
	BufferedImage map; // The map image to be displayed; selected when the map wizard opens.
	File mapFile;
	Image checkerboardImage; // The image displayed translucently over the map; meant to represent the board state of the regular automaton. Looks like a checkerboard to show individual cell sizes.
	
	int cellDensity = 10; // Current cell density value 
	double cellDimensions; // Current cell dimensions; calculated in findRowsAndColumns()
	int columns; // Number of columns calculated from cell density and area of the map image
	int rows; // Number of rows calculated from cell density and area of the map image
	int[] checkerboardArray; // The 1D array of pixel values used to create the checkerboard image
	
	// Starts the map wizard
	public void launch() {
		if(chooseMap()){ buildWizardWindow(); }
	}

	// UI building methods 
	public void buildWizardWindow() {
		
		/*
		 * Builds the map wizard UI
		 * 
		 * 	- The map area is where the map is shown
		 * 	- The western area has the controls for
		 * 	zooming, applying the map wizard to the
		 * 	main automaton, and so on.
		 * 	- The southern area contains the cell density
		 * 	slider and the map deformity label.
		 * 
		 */
		wizardWindow = new JFrame();
		
		buildMapArea();
		buildWesternArea();
		buildSouthernArea();
		wizardWindow.setBounds(	userInterface.mainFrame.getX() + 20, 
								userInterface.mainFrame.getY() + 20, 
								(int) (userInterface.mainFrame.getWidth()*0.9),
								(int) (userInterface.mainFrame.getHeight()*0.9));
		wizardWindow.setVisible(true);
		mapPanel.refreshCheckerboard(); // Update the checkerboard array and generate a new image
		mapPanel.zoomToFit();
	}
	public void buildMapArea() {
		
		/*
		 * Builds the map area section of the UI which contains...
		 * the map.
		 * 
		 */
		
		mapPanel = new MapPanel();
		
		// Put the map panel inside an inset panel which provides some visual padding,
		// then put the inset panel inside the scrollpane
		MapWizardInsetPanel insetPanel = new MapWizardInsetPanel();
		insetPanel.setLayout(new GridBagLayout());
		insetPanel.add(mapPanel, new GridBagConstraints());
		
		mapScrollPane = new JScrollPane(insetPanel);
		mapViewport = mapScrollPane.getViewport();
		
		mapPanel.findRowsAndColumns(); // calculate number of rows and columns based on cell density
		
		wizardWindow.add(mapScrollPane, BorderLayout.CENTER);
		
	}
	public void buildWesternArea() {
		
		/*
		 * Builds the western part of the UI. There are 5 sub-panels (from top to bottom):
		 * 
		 * 	- The invisoPanel is just padding, to make the other sub-panels
		 * 	appear lower down.
		 * 	- The loadMapPanel contains a button for selecting a new map even
		 * 	after the map wizard has been opened.
		 * 	- The zoomPanel contains buttons for zooming in, out, and to fit.
		 * 	- The saveAndApply panel contains a button for applying the current
		 * 	configuration to the main part of the automaton
		 * 	
		 */
		
		JPanel westPanel = new JPanel();
			westPanel.setLayout(new BoxLayout(westPanel, BoxLayout.Y_AXIS));
	
		// Build and populate the sub-panels
		JPanel invisoPanel = new JPanel();
			invisoPanel.setBorder(BorderFactory.createEmptyBorder(5,10,5,10));
			
		JPanel loadMapPanel = new JPanel();
			loadMapPanel.setBorder(BorderFactory.createEmptyBorder(5,10,5,10));
			JButton loadMapButton = new JButton("Load New Map");
			loadMapButton.addActionListener(new LoadMapButtonListener());
			loadMapPanel.add(loadMapButton);
		
		JPanel zoomPanel = new JPanel();
			zoomPanel.setLayout(new GridBagLayout());
			zoomPanel.setBorder(BorderFactory.createEmptyBorder(5,10,5,10));
			
			JButton zoomInButton = new JButton("Zoom In");
				zoomInButton.addActionListener(new ZoomInListener());
				GridBagConstraints zoomInConstraints = new GridBagConstraints();
				zoomInConstraints.gridx = 0;
				zoomInConstraints.gridy = 0;
				zoomInConstraints.gridwidth = 1;
				zoomInConstraints.gridheight = 1;
				zoomInConstraints.fill = GridBagConstraints.BOTH;
				zoomInButton.addActionListener(new ZoomInListener());
				zoomPanel.add(zoomInButton, zoomInConstraints);
			
			JButton zoomOutButton = new JButton("Zoom Out");
				zoomOutButton.addActionListener(new ZoomOutListener());
				GridBagConstraints zoomOutConstraints = new GridBagConstraints();
				zoomOutConstraints.gridx = 1;
				zoomOutConstraints.gridy = 0;
				zoomOutConstraints.gridwidth = 1;
				zoomOutConstraints.gridheight = 1;
				zoomOutConstraints.fill = GridBagConstraints.BOTH;
				zoomOutButton.addActionListener(new ZoomOutListener());
				zoomPanel.add(zoomOutButton, zoomOutConstraints);
			
			JButton zoomToFitButton = new JButton("Zoom to Fit");
				zoomToFitButton.addActionListener(new ZoomToFitListener());
				GridBagConstraints zoomToFitConstraints = new GridBagConstraints();
				zoomToFitConstraints.gridx = 0;
				zoomToFitConstraints.gridy = 1;
				zoomToFitConstraints.gridwidth = 2;
				zoomToFitConstraints.gridheight = 1;
				zoomToFitConstraints.fill = GridBagConstraints.BOTH;
				zoomToFitButton.addActionListener(new ZoomToFitListener());
				zoomPanel.add(zoomToFitButton, zoomToFitConstraints);
			
		JPanel saveAndApplyPanel = new JPanel();
			saveAndApplyPanel.setLayout(new GridBagLayout());
			saveAndApplyPanel.setBorder(BorderFactory.createEmptyBorder(5,10,5,10));
			
			JButton saveAndApplyButton = new JButton("Save and Apply");
				saveAndApplyButton.addActionListener(new SaveAndApplyListener());
				GridBagConstraints saveAndApplyConstraints = new GridBagConstraints();
				saveAndApplyConstraints.gridx = 0;
				saveAndApplyConstraints.gridy = 0;
				saveAndApplyConstraints.gridwidth = 2;
				saveAndApplyConstraints.gridheight = 2;
				saveAndApplyConstraints.fill = GridBagConstraints.BOTH;
				saveAndApplyPanel.add(saveAndApplyButton,saveAndApplyConstraints);	
			
		JPanel labelsPanel = new JPanel();
			labelsPanel.setBorder(BorderFactory.createEmptyBorder(5,10,5,10));
			labelsPanel.setLayout(new GridBagLayout());
			boardDimensionsLabel = new JLabel("Board dimensions: " + columns + "x" + rows);
				GridBagConstraints boardDimensionsLabelConstraints = new GridBagConstraints();
				boardDimensionsLabelConstraints.gridx = 0;
				boardDimensionsLabelConstraints.gridy = 0;
				boardDimensionsLabelConstraints.gridwidth = 1;
				boardDimensionsLabelConstraints.gridheight = 1;
			totalCellsLabel = new JLabel("Total cells: " + columns*rows);
				GridBagConstraints totalCellsLabelConstraints = new GridBagConstraints();
				totalCellsLabelConstraints.gridx = 0;
				totalCellsLabelConstraints.gridy = 1;
				totalCellsLabelConstraints.gridwidth = 1;
				totalCellsLabelConstraints.gridheight = 1;
			labelsPanel.add(boardDimensionsLabel, boardDimensionsLabelConstraints);
			labelsPanel.add(totalCellsLabel, totalCellsLabelConstraints);
			
		// Add the sub-panels to the west panel
		westPanel.add(invisoPanel);
		westPanel.add(loadMapPanel);
		westPanel.add(zoomPanel);
		westPanel.add(saveAndApplyPanel);
		westPanel.add(labelsPanel);
		
		// Add the west panel to the wizard window
		wizardWindow.add(westPanel, BorderLayout.WEST);
		
	}
	public void buildSouthernArea() {
		
		/*
		 * Builds the southern area, which contains
		 * the cell density slider and the map deformity label.
		 * 
		 */
		
		JPanel southPanel = new JPanel();
		southPanel.setLayout(new GridBagLayout());
		
		// Build the slider and its panel
		cellDensitySlider = new JSlider(	JSlider.HORIZONTAL,
											CELL_DENSITY_MIN,
											CELL_DENSITY_MAX,
											CELL_DENSITY_INIT);
		
			cellDensitySlider.addChangeListener(new SliderListener());
			
			cellDensitySlider.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
			JPanel sliderPanel = new JPanel();
			sliderPanel.add(cellDensitySlider);
		
		// Build the map deformity label and its panel
		mapDeformityLabel = new JLabel();
			mapDeformityLabel.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
			JPanel labelPanel = new JPanel();
			labelPanel.add(mapDeformityLabel);
		
		// Build the gridbag constraints
		GridBagConstraints labelConstraints = new GridBagConstraints();
			labelConstraints.gridx = 2;
			labelConstraints.gridwidth = 1;
			labelConstraints.gridheight = 1;
			labelConstraints.weightx = 1;
			labelConstraints.weighty = 1;
			
		GridBagConstraints sliderConstraints = new GridBagConstraints();
			sliderConstraints.gridx = 4;
			labelConstraints.gridwidth = 1;
			labelConstraints.gridheight = 1;
			labelConstraints.weightx = 1;
			labelConstraints.weighty = 1;
		
		// Add the label and slider panels to the south panel with the appropriate constraints
		southPanel.add(labelPanel, labelConstraints);
		southPanel.add(sliderPanel, labelConstraints);
		
		// Add the southpanel to the wizard window
		wizardWindow.add(southPanel, BorderLayout.SOUTH);
		
	}

	// Input methods
	public boolean chooseMap() {
		
		/*
		 * Opens a file chooser for the user to select a map.
		 * If the user selects something and it is indeed an image, returns true,
		 * otherwise returns false.
		 * 
		 */
		
		JFileChooser fileChooser = new JFileChooser();
		
		int userChose = fileChooser.showOpenDialog(userInterface.mainFrame);
		
		if(userChose == JFileChooser.APPROVE_OPTION) {
			
			// Get the file selected by the user
			mapFile = fileChooser.getSelectedFile();
			
			// Read the object
			try {
				
				BufferedImage loadedMap = ImageIO.read(mapFile);
			
				if(isImage(mapFile)){
					
					map = loadedMap;
					return true;
					
				} else {
					
					JOptionPane.showMessageDialog(wizardWindow, 
							"Invalid file extension. " +
							"\n\nPlease make sure the file is an image of one of the following types" +
							"\nand has the appropriate extension:" +
							"\n\nbmp, wbmp, png, gif, jpg", 
							"File Extension Error", JOptionPane.ERROR_MESSAGE);
					return false;
				}
			
			} catch(Exception ex) {
				
				JOptionPane.showMessageDialog(wizardWindow, "Could not read the map image.",
						"File Input Error", JOptionPane.ERROR_MESSAGE);
				return false;
				
			}
		
		}
		
		return false;
	
	}
	public boolean isImage(File file) {
		
		/*
		 * Checks a file's name to see whether it has a
		 * BMP, WBMP, JPG, GIF, or PNG extension. If so,
		 * returns true, otherwise returns false.
		 * 
		 */
		
		String fileName = file.getName();
		
		int index = fileName.lastIndexOf(".");
		
		String extension = fileName.substring(index+1,fileName.length()).toLowerCase();
		
		if(	extension.equals("bmp")
			|| extension.equals("wbmp")
			|| extension.equals("jpg")
			|| extension.equals("gif")
			|| extension.equals("png")
		){
			
			return true;
			
		}
		
		return false;
		
	}
	
	// Display refresh methods
	public void refreshBoardDimensionsLabel() {
		
		/*
		 * Updates the text on the board dimensions label to reflect the 
		 * current number of rows and columns.
		 * 
		 */
		
		boardDimensionsLabel.setText("Board dimensions: " + columns + "x" + rows);
		
	}
	public void refreshTotalCellsLabel() {
		
		/*
		 * Updates the text on the total cells label to reflect the
		 * projected number of cells.
		 * 
		 */
		
		totalCellsLabel.setText("Total cells: " + columns*rows);
		
	}
	public void refreshMapDeformityLabel() {

		/*
		 * Updates the text on the map deformity label to reflect the
		 * difference in size between the original map image and
		 * the current size of the map image. 
		 * 
		 * Note: Map deformity does not necessarily correlate with
		 * deformation of aspect ratio. As long as the map deformity
		 * values for each dimension are close together, the aspect
		 * ratio deformation will be slight.
		 * 
		 */
		
		mapDeformityLabel.setText("Map deformity (X,Y): (" + (mapPanel.getWidth() - map.getWidth()) + ","
															+ (mapPanel.getHeight() - map.getHeight()) + ")\n");
		
	}

	// Event methods
	public void addMapDisplayListener(IMapDisplayListener listener) {
		mapDisplayListeners.add(listener);
	}
	public void removeMapDisplayListener(IMapDisplayListener listener) {
		mapDisplayListeners.remove(listener);
	}
	private void notifyMapDisplayListeners(boolean showMap, Image map, int widthInCells, int heightInCells) {
		for(IMapDisplayListener listener : mapDisplayListeners) {
			listener.mapInfoChanged(showMap, map, widthInCells, heightInCells);
		}
	}
	
	// UI listeners
	class SliderListener implements ChangeListener {
		
		/*
		 * The event listener for the cell density slider. 
		 * 
		 * First gets the value of the slider, then:
		 * 
		 * 	1. Calculates how many rows and columns there ought to be
		 * 	2. Update the checkerboard overlay
		 * 	3. Update the size of the map panel
		 * 	4. Updates the board dimensions label,
		 *		total cells label, and map deformity label
		 * 
		 */
		
		public void stateChanged(ChangeEvent e) {
			
			/*
			 * Called when the slider is moved.
			 */
			
			cellDensity = cellDensitySlider.getValue();
			mapPanel.findRowsAndColumns();
			mapPanel.refreshCheckerboard();
			mapPanel.setPreferredSize();
			refreshBoardDimensionsLabel();
			refreshTotalCellsLabel();
			refreshMapDeformityLabel();

		}
		
	}
	class LoadMapButtonListener implements ActionListener {
		
		/*
		 * The listener for the load map button.
		 * 
		 * Simply allows the user to select a new map to be
		 * displayed in the map panel. Performs the appropriate
		 * procedures to make sure the map displays properly after changing.
		 */
		
		public void actionPerformed(ActionEvent e) {
			
			/*
			 * Called when the button is pressed.
			 */
			
			if(chooseMap()) { // If the user chose an image file with a proper extension
				
				mapPanel.findRowsAndColumns();
				mapPanel.setPreferredSize();
				mapPanel.repaint();
				mapPanel.revalidate();
				refreshMapDeformityLabel();
			
			}
			
		}
		
	}
	class ZoomInListener implements ActionListener {
		
		/*
		 * Listener for the zoom in button. Zooms by 105%.
		 */
		
		public void actionPerformed(ActionEvent ev) {
			
			mapPanel.zoom(1.05);
			
		}
		
	}
	class ZoomOutListener implements ActionListener {
		
		/*
		 * Listener for the zoom out button. Zooms by 95%.
		 */
		
		public void actionPerformed(ActionEvent ev) {
			
			mapPanel.zoom(0.95);
			
		}
		
	}
	class ZoomToFitListener implements ActionListener {
		
		/*
		 * Listener for the zoom to fit button. Zooms however
		 * much is necessary for the map panel to fit nicely
		 * within the scrollpane.
		 * 
		 */
		
		public void actionPerformed(ActionEvent ev) {
			
			mapPanel.zoomToFit();
			
		}
		
	}
	class SaveAndApplyListener implements ActionListener {
		
		/*
		 * Listener for the save and apply button.
		 * 
		 * Sets the state of the automaton proper to reflect
		 * the choices made by the user within the map wizard.
		 * (E.g. width and height of the world array, size of the
		 * board panel, etc.)
		 * 
		 */
		
		public void actionPerformed(ActionEvent ev) {
			
			/*
			 * Opens a dialog to make sure the user wants to apply the selected settings and exit,
			 * then:
			 * 
			 * 	1. Resizes the automaton's board panel to the same size as the current map panel.
			 * 	2. Sets the automaton's map to be the map currently displayed in the map wizard.
			 * 	3. Sets the flag to display the map.
			 * 	4. Sets the automaton to stretch the map to fit the size of the board panel.
			 * 	5. Refreshes the board image.
			 * 	6. Gets rid of the map wizard window so it doesn't continue to consume resources.
			 */
			
			int optionSelected = JOptionPane.showOptionDialog(wizardWindow, "Apply these settings and exit the map wizard?", "Save and Apply", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE,null,null,null);
			
			if(optionSelected == JOptionPane.YES_OPTION) {
				Dimension mapPanelDimensions = mapPanel.getPreferredSize();
				
				automaton.getBoard().resize(new BoardDimensions(columns, rows));
				userInterface.boardPanel.setPreferredSize(mapPanelDimensions);
				
				userInterface.showMapMenuItem.setSelected(true);
				
				userInterface.boardPanel.setMapDisplaySettings(MapDisplaySettings.STRETCH_MAP_TO_FIT_BOARD);
				userInterface.stretchMapToFitBoardRB.setSelected(true);
				
				notifyMapDisplayListeners(true, map, columns, rows);
				
				wizardWindow.dispose();
			}
		}
		
	}
	
	// Map area sub-classes
	@SuppressWarnings("serial")
	class MapPanel extends JPanel {
		
		/*
		 * The component which displays the map and the checkerboard.
		 */
		
		public MapPanel() {
			
			/*
			 * Constructor. Takes care of creating and adding the mouselistener.
			 */
			
			MapMouseListener listener = new MapMouseListener();
			this.addMouseListener(listener);
			this.addMouseMotionListener(listener);
			
		}
		
		public void paint(Graphics g) {
			
			/*
			 * The paint procedure. First calls JPanel's paint() method,
			 * then draws the map image underneath the checkerboard.
			 * 
			 */
			
			super.paint(g);
			
			Graphics2D g2d = (Graphics2D) g;
			
			// Draw the images
			g2d.drawImage(map, // the image to draw
					0,0,this.getWidth(),this.getHeight(), // the dimensions of the image (draw the image so it is as wide and tall as the map)
					null); // the image observer
			
			g2d.drawImage(checkerboardImage,
					0,0,this.getWidth(),this.getHeight(),
					null); // the image observer
			
		}
	
		public void setPreferredSize() {
			
			/*
			 * Takes the width or height of the automaton
			 * and calculates the other dimension using
			 * the ratio of columns to rows, then sets
			 * the new width and new height as the preferred
			 * size of the map panel. Also sets the new
			 * dimensions as the actual size. 
			 * 
			 */
			
			double aspectRatio = (double) columns/rows;

			Dimension current = mapPanel.getPreferredSize();
			
			int newWidth = (int) Math.round(current.getWidth());
			int newHeight = (int) Math.round(newWidth/aspectRatio);
				
			Dimension newDimension = new Dimension(newWidth,newHeight);
				
			setPreferredSize(newDimension);
			setSize(newDimension);			
			mapPanel.revalidate();
			
		}
		
		public void findRowsAndColumns() {
			
			/*
			 *  Calculates an appropriate number of rows and columns
			 *  based on user-selected cell density. Does this using
			 *  the area of the map.
			 */
			
			
			double mapArea = (double) map.getWidth()*map.getHeight();
			double areaPerCell = mapArea/cellDensity;
			cellDimensions = Math.sqrt(areaPerCell); 
				/* Since cells are square, a cell's width and height are equal.
				 * Area = width*height
				 * Area = width*width
				 * Area = width^2
				 * sqrt(area) = width (or height)
				 */
			
			// The number of columns is the map width/cell width. 
			// Round to the nearest integer.
			columns = (int) Math.round((double) map.getWidth()/cellDimensions);
			
			// The number of rows is the map height/cell height.
			// Round to the nearest integer.
			rows = (int) Math.round((double) map.getHeight()/cellDimensions);
			
		}
		
		public void refreshCheckerboard() {
			
			/*
			 * Takes care of all the procedures needed
			 * to refresh the checkerboard image. First
			 * updates the checkerboard array to reflect
			 * the number of rows and columns, then
			 * creates a new image from the checkerboard array.
			 * 
			 */
			
			updateCheckerboardArray();
			checkerboardImage = createImage(new MemoryImageSource(columns, rows, checkerboardArray, 0, columns));
			
		}
		
		public void updateCheckerboardArray() {
			
			/*
			 * Iterates diagonally through the checkerboard,
			 * making all cells on a given diagonal the same color,
			 * then alternating cell colors between diagonals. This
			 * results in a checkerboard pattern where every cell
			 * alternates between black and white.
			 * 
			 * Creates a 1-dimensional array of pixel values.
			 * 
			 */
			
			@SuppressWarnings("unused")
			int index = 0; // Used to find the appropriate index in the dimensional array
		    int alternator = 0; // Alternates between 0 and 1 (determines cell color)
		    checkerboardArray = new int[rows*columns]; 
		    	// The 1-dimensional array must have as many locations
		    	// as there are squares on the board.

		    for (int slice = 0; slice < rows + columns - 1; ++slice) {
		        
		    	int z1 = slice < columns ? 0 : slice - columns + 1;
		        int z2 = slice < rows ? 0 : slice - rows + 1;
		        
		        alternator = (alternator == 0 ? 1 : 0); // If alternator = 1, set it to 0, and vice versa.
		        
		        for (int j = slice - z2; j >= z1; --j) {
		        	
		        	switch(alternator) {
		        	
				        case 0:
							checkerboardArray[(slice-j)+(j*columns)] = (100 << 24) | (255 << 16) | (255 << 8) | 255;
							index++;
							break;	
						
						case 1:
							checkerboardArray[(slice-j)+(j*columns)] = (100 << 24) | (0 << 16) | (0 << 8) | 0;
							index++;
							break;
		        
		        	}
						
		        }

		    }
			    
		}	

		public void zoom(double scale) {
			
			/*
			 * Scales the automaton's size and preferred size
			 * according to the value of the "scale" argument.
			 * (E.g., if the argument is 1.05, the size of the
			 * automaton increases 105%.)
			 * 
			 * Does this by multiplying the current width by "scale"
			 * to get the new width, then multiplying the new width
			 * by the aspect ratio to get the new height. Doing it
			 * this way instead of multiplying them both by scale means
			 * that there is no gradual distortion of the aspect ratio. 
			 * 
			 */
			
			Dimension current = mapPanel.getPreferredSize();
			double aspectRatio = (double) columns/rows;
			int newWidth = (int) Math.round(current.getWidth()*scale);
			int newHeight = (int) Math.round(newWidth/aspectRatio);
			
			if(newWidth >= BOARD_MIN_SIZE && newHeight >= BOARD_MIN_SIZE) {
				
				Dimension newDimension = new Dimension(newWidth,newHeight);

				mapPanel.setPreferredSize(newDimension);
				mapPanel.setSize(newDimension);	
				mapPanel.revalidate();
			
				refreshMapDeformityLabel();
				
			}
			
		}
		
		public void zoomToFit() {
			
			/*
			 * If the automaton is wider than it is tall,
			 * sets the width to 90% of the scroll pane's width,
			 * then calculates the new height using the aspect
			 * ratio of columns to rows.
			 * 
			 * If the automaton is taller than it is wide,
			 * sets the height to 90% of the scroll pane's width,
			 * then calculates the new width using the aspect ratio.
			 * 
			 * Also refreshes the map deformity label.
			 * 
			 */

			int scrollPaneWidth = mapScrollPane.getWidth();
			int scrollPaneHeight = mapScrollPane.getHeight();
						
			int boardWidth = getWidth();
			int boardHeight = getHeight();
						
			double xScale = (double) scrollPaneWidth/boardWidth;
			double yScale = (double) scrollPaneHeight/boardHeight;
						
			double scale = Math.min(xScale, yScale);
						
			zoom(scale*0.9);
		
			refreshMapDeformityLabel();
		
		}
		
		class MapMouseListener implements MouseListener, MouseMotionListener {
	
			/*
			 * Implements custom scrolling using the mouse. The user is able to
			 * click and drag the map, changing the location of the viewport.
			 */
			
			Point oldMousePosition = new Point(0,0);
			
			int XDif;
			int YDif;
			int newX;
			int newY;
			
			@Override
			public void mousePressed(MouseEvent e) {
					
				oldMousePosition.setLocation(e.getX(), e.getY());

			}
		
			public void mouseDragged(MouseEvent e) {
				
				if(		( e.getX() >= mapPanel.getWidth() ) // if the drag location is outside the mapPanel
						|| ( e.getY() >= mapPanel.getHeight() )
						|| ( e.getX() <= 0 )
						|| ( e.getY() <= 0 ) 
					) { 
					
					// do nothing
				
				} else {
							
					// Calculate how much to change the view
					XDif = (int) oldMousePosition.x - e.getX();
					YDif = (int) oldMousePosition.y - e.getY();
					newX = mapViewport.getViewPosition().x + XDif;
					newY = mapViewport.getViewPosition().y + YDif;
							
					// Then update the view position
					if(	( newX > 0 )
						&& ((newX + mapViewport.getWidth()) < getWidth())
						&& ( newY > 0 )
						&& ((newY + mapViewport.getHeight()) < getHeight())
						) {
								
							mapViewport.setViewPosition(new Point(newX,newY));
							revalidate();
								
					} else if( ( 0 < newX ) // // Else, if there's some board panel out of view in just the x direction
								&& ((newX + mapViewport.getWidth()) < getWidth())
								) {
							
									mapViewport.setViewPosition(new Point(newX,newY-YDif)); // only move the view in the X dimension
									revalidate();
						
					} else if( (0 < newY ) // Else, if there's some board panel out of view in just the Y direction
								&& ((newY + mapViewport.getHeight()) < getHeight())
								) {
								
									mapViewport.setViewPosition(new Point(newX-XDif, newY)); // only move the view in the Y dimension
									revalidate();
								
					}							
				}			
			}
			
			public void mouseReleased(MouseEvent e){}
		
			public void mouseEntered(MouseEvent e){}
			
			public void mouseExited(MouseEvent e){}
			
			public void mouseClicked(MouseEvent e){}
				
			public void mouseMoved(MouseEvent e){}
			
		}
		
	}
	@SuppressWarnings("serial")
	class MapWizardInsetPanel extends JPanel implements MouseWheelListener {
		
		/*
		 * Implements custom scrolling. Allows the user to zoom in and out
		 * on the map panel as long as the mouse is anywhere in the map area.
		 */
		
		double scale = 1.0;
		
		public MapWizardInsetPanel() {
			
			/*
			 * Constructor. Creates and adds a mouse wheel listener.
			 */
			
			super();
			addMouseWheelListener(this);
			
		}
		
		public MapWizardInsetPanel(LayoutManager lm) {
			
			/*
			 * As above, but also handles a layout manager argument.
			 */
			
			super(lm);
			addMouseWheelListener(this);
			
		}
		
		@Override
		public void mouseWheelMoved(MouseWheelEvent e) {

			/*
			 * If the mouse wheel is moved upwards, zoom in.
			 * If the mouse wheel is moved downwards, zoom out.
			 * 
			 */
			
			if(e.getWheelRotation() > 0) { // if the wheel was moved downwards
				
				scale = Math.pow(0.95, e.getScrollAmount()); // Multiply 0.95 by the amount the mouse wheel was moved; call this scale
				
				mapPanel.zoom(scale);
				
			
			} else { // otherwise the wheel was moved upwards
				
				scale = Math.pow(1.05, e.getScrollAmount()); // Multiply 1.05 by the amount the mouse wheel was moved; call this scale
				
				mapPanel.zoom(scale);
				
			}

		}

	}
}
