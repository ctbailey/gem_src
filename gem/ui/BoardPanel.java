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

import gem.InvalidConfigurationException;
import gem.simulation.ICell.CellState;
import gem.talk_to_outside_world.AutomatonSerializable;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.*;

import javax.imageio.ImageIO;
import javax.swing.*;

import java.util.ArrayList;
import java.util.List;
 

public class BoardPanel extends JPanel implements AutomatonSerializable, IBoardImageChangedListener {
	static final long serialVersionUID = 6;
	public enum BoardDisplayConfigurationKey {
		LinkAgeToAlpha;
	}
	
	public enum MapDisplaySettings { STRETCH_MAP_TO_FIT_BOARD, SCALE_MAP_TO_FILL_BOARD }
	public enum MouseButton {LEFT, RIGHT, WHEEL, NONE}
	private static final int BOARD_MIN_SIZE = 100; // The minimum size of the board in "user space"
	private static final double DEFAULT_ZOOM_IN_SCALE = 1.2;
	private static final double DEFAULT_ZOOM_OUT_SCALE = 0.8;
	private static final CellState DEFAULT_USER_SELECTION = CellState.ALIVE;
	
	private AbstractBoardImageSource boardDisplayModel;
	public BufferedImage map;
	
	boolean showMap; // Boolean flag which declares whether to show the map panel
	private boolean linkAgeToAlpha; 
	private MapDisplaySettings mapDisplaySettings; // Enum instance
	private CellState userSelection = DEFAULT_USER_SELECTION; // Used to determine what cell to place when the user clicks/drags on the board
	
	// Event fields
	private List<IBoardInteractionListener> boardInteractionListeners = new ArrayList<IBoardInteractionListener>();
	private List<IUserCellTypeSelectionChangedListener> userCellTypeSelectionChangedListeners = new ArrayList<IUserCellTypeSelectionChangedListener>();
	
	public BoardPanel() {
		super();
		
		// Initially, the map image isn't being shown
		showMap = false;
		
		// The program defaults to stretching the map to fit the board
		mapDisplaySettings = MapDisplaySettings.STRETCH_MAP_TO_FIT_BOARD;
		
		setBoardImageSource(new BinaryBoardImageSource());
		boardDisplayModel.addBoardImageChangedListener(this);
		
		BoardMouseListener listener = new BoardMouseListener();
		
		this.addMouseListener(listener);
		this.addMouseMotionListener(listener);
		this.addBoardInteractionListener(automaton.getBoard());
		
		this.setToolTipText("");
		
		repaint();
	}
	
	public String getToolTipText(MouseEvent ev) {
		StringBuffer sb = new StringBuffer("<html>"); // used to permit multiple lines in the tooltip
		int cellX = (int) (	Math.floor(ev.getX()/cellWidth())	); // do some arithmetic to translate the mouse's location 
		int cellY = (int) ( Math.floor(ev.getY()/cellHeight())	); // on the board to an address in the displayArray
		
		if((geography.longitudeArray != null)
				&& (geography.latitudeArray != null)) {
			
			sb.append("Longitude: " + geography.longitudeArray[cellX][cellY] + "<br>");
			sb.append("Latitude: " + geography.latitudeArray[cellX][cellY] + "<br>");
			
		}
		if(metadata.metadataArray !=null) {
			Point toolTipPoint = new Point(cellX, cellY);
			if(metadata.inferredMetadataPoints.contains(toolTipPoint)) {
				sb.append("INFERRED <br>");
			}
			for(int i = 0; i < metadata.categoryIdentifiers.length; i++) {
				sb.append(metadata.categoryIdentifiers[i] + ": ");
				sb.append(metadata.metadataArray[cellX][cellY][i] + "<br>"); // <br> is analogous to \n; creates a new line
			}
		}
		sb.append("X: " + cellX + "<br>");
		sb.append("Y: " + cellY + "<br>");
		return sb.toString();
	}
	
	public void refreshBoardImage() {
		repaint();
	}
	
	public void boardImageChanged(Image newBoardImage) {
		repaint();
	}
	public void paint(Graphics g) {		
		Graphics2D g2d = (Graphics2D) g;
		tryDrawMap(g2d);
		g2d.drawImage(boardDisplayModel.getCurrentBoardImage(),0,0,this.getWidth(),this.getHeight(), null); // draw the board
	}
	private void tryDrawMap(Graphics2D g2d) {
		// TODO: Refactor
		if(	showMap &&
			(map != null) ) {
			
			switch(mapDisplaySettings) {
			
				case STRETCH_MAP_TO_FIT_BOARD:
					// Draw the map
					g2d.drawImage(map, // the image to draw
						0,0,this.getWidth(),this.getHeight(), // the dimensions of the image (draw the image so it is as wide and tall as the boardPanel)
						null); // the image observer
					break;
					
				case SCALE_MAP_TO_FILL_BOARD:
					// Calculate the appropriate scale
						double xScale = (double) this.getWidth()/map.getWidth();
						double yScale = (double) this.getHeight()/map.getHeight();
						double scale = Math.max(xScale,yScale);
						
						int mapWidth = (int) (map.getWidth()*scale);
						int mapHeight = (int) (map.getHeight()*scale);
						int x = (this.getWidth() - mapWidth)/2;
						int y = (this.getHeight() - mapHeight)/2;
					
					// Draw the map
					g2d.drawImage(map,
						x,y, // points of origin
						mapWidth, mapHeight, // dimensions of the image
						null); // the image observer
					break;	
			}
		}
	}
	
	public CellState getUserCellTypeSelection() {
		return userSelection;
	}
	public void setUserCellTypeSelection(CellState state) {
		userSelection = state;
		notifyUserCellTypeSelectionChangedListeners(state);
	}
	private double cellWidth() {
		double cellWidth = (double) this.getWidth() / (double)automaton.getBoard().getCurrentState().getWidth(); // assumes a rectangular world array
		return cellWidth;
	}
	private double cellHeight() {
		double cellHeight = (double) this.getHeight()/ (double)automaton.getBoard().getCurrentState().getHeight();		
		return cellHeight;
	}
	
	public IBoardImageSource getBoardImageSource() {
		return boardDisplayModel;
	}
	public void setBoardImageSource(AbstractBoardImageSource newImageSource) {
		if(boardDisplayModel != null) {
			boardDisplayModel.isBeingReplacedBy(newImageSource);
		}
		newImageSource.isReplacing(boardDisplayModel);
		boardDisplayModel = newImageSource;
	}
	
	public void zoomIn() {
		zoom(DEFAULT_ZOOM_IN_SCALE);
	}
	public void zoomOut() {
		zoom(DEFAULT_ZOOM_OUT_SCALE);
	}
	public void zoom(double scale) {
		Dimension current = getPreferredSize();
		double aspectRatio = (double) automaton.getBoard().getCurrentState().getWidth()/automaton.getBoard().getCurrentState().getHeight();
		int newWidth = (int) Math.round(current.getWidth()*scale);
		int newHeight = (int) Math.round(newWidth/aspectRatio);
		
		if(newWidth >= BOARD_MIN_SIZE 
				&& newHeight >= BOARD_MIN_SIZE) {
			Dimension newDimension = new Dimension(newWidth,newHeight);
			setPreferredSize(newDimension);
			setSize(newDimension);	
			revalidate();
		}
	}
	public void zoomToFit() {
		int scrollPaneWidth = userInterface.boardScrollPane.getWidth();
		int scrollPaneHeight = userInterface.boardScrollPane.getHeight();
					
		int boardWidth = getWidth();
		int boardHeight = getHeight();
					
		double xScale = (double) scrollPaneWidth/boardWidth;
		double yScale = (double) scrollPaneHeight/boardHeight;
					
		double scale = Math.min(xScale, yScale);
					
		zoom(scale*0.9);
	}
	
	public void setMapDisplaySettings(MapDisplaySettings setting) {
		mapDisplaySettings = setting;
		refreshBoardImage();
	}	
	public boolean getValueForConfigurationKey(BoardDisplayConfigurationKey key) {
		return linkAgeToAlpha;
	}
	public void setValueForConfigurationKey(BoardDisplayConfigurationKey key, boolean value) 
		throws InvalidConfigurationException {
		switch(key) {
			case LinkAgeToAlpha:
				setLinkAgeToAlpha(value);
				break;
			default:
				throw new InvalidConfigurationException("Unrecognized configuration key: " + key);
			
		}
	}
	private void setLinkAgeToAlpha(boolean value) {
		if(value && !linkAgeToAlpha) {
			try {
				LinkAgeToAlphaBoardImageSource newImageSource = LinkAgeToAlphaBoardImageSource.createInstanceFromUserInput();
				setBoardImageSource(newImageSource);
			} catch(UserDidNotConfirmException ex) {
				// Do nothing
			}
		} else {
			setBoardImageSource(new BinaryBoardImageSource());
		}
	}
	
	private boolean isPointOnBoard(int x, int y) {
		return (( x < getWidth() )
				&& ( y < getHeight() )
				&& ( x >= 0 )
				&& ( y >= 0 ));
	}
	private Point getCellLocation(int visualX, int visualY) {
		int cellX = (int) Math.floor(visualX/cellWidth()); 
		int cellY = (int) Math.floor(visualY/cellHeight());
		return new Point(cellX, cellY);
	}
	
	// Event methods
	public void addBoardInteractionListener(IBoardInteractionListener listener) {
		boardInteractionListeners.add(listener);
	}
	public void addUserCellTypeSelectionChangedListener(IUserCellTypeSelectionChangedListener listener) {
		userCellTypeSelectionChangedListeners.add(listener);
	}
	
	public void removeBoardInteractionListener(IBoardInteractionListener listener) {
		boardInteractionListeners.remove(listener);
	}
	public void removeUserCellTypeSelectionChangedListener(IUserCellTypeSelectionChangedListener listener) {
		userCellTypeSelectionChangedListeners.remove(listener);
	}
	
	private void notifyBoardInteractionListeners(CellState currentlySelectedByUser, MouseButton buttonPressed, int cellX, int cellY) {
		for(IBoardInteractionListener listener : boardInteractionListeners) {
			listener.userInteracted(currentlySelectedByUser, buttonPressed, cellX, cellY);
		}
	}
	private void notifyUserCellTypeSelectionChangedListeners(CellState newSelection) {
		for(IUserCellTypeSelectionChangedListener listener : userCellTypeSelectionChangedListeners) {
			listener.userCellTypeSelectionChanged(newSelection);
		}
	}
	
	public void save(ObjectOutputStream output) {
		
		Object[] savedBoardPanel = new Object[4];
		
		savedBoardPanel[0] = boardDisplayModel;
		savedBoardPanel[1] = getPreferredSize();
		
		savedBoardPanel[2] = new Boolean(showMap);
		
		try {
			
			if(map != null) {
				savedBoardPanel[3] = Boolean.valueOf(true); // when the program loads this save again, try and read an image after this array
			} else {
				savedBoardPanel[3] = Boolean.valueOf(false); // do not try and read an image after loading this array
			}
					
			output.writeObject(savedBoardPanel);
			
			if(map != null) {
				ImageIO.write(map,"jpg",output);
			}
			
		}catch(Exception ex) {
			ex.printStackTrace();
		}
	}
	public void load(ObjectInputStream input) {
		
		try{
			Object[] loadedBoardPanel = (Object[]) input.readObject();
			
			AbstractBoardImageSource loadedDisplayBoard = (AbstractBoardImageSource) loadedBoardPanel[0];
			Dimension loadedPreferredSize = (Dimension) loadedBoardPanel[1];
			
			Boolean loadedShowMap = (Boolean) loadedBoardPanel[2];
			Boolean readImage = (Boolean) loadedBoardPanel[3];
			
			BufferedImage loadedMap = null;
			
			if(readImage.booleanValue()) {
				loadedMap = (BufferedImage) ImageIO.read(input);
			}
			
			setBoardImageSource(loadedDisplayBoard);
			setPreferredSize(loadedPreferredSize);
			
			showMap = loadedShowMap.booleanValue();
			
			if(loadedMap != null) {
				
				map = loadedMap;
				
			}
			revalidate();
		} catch(Exception ex) {
			ex.printStackTrace();
		}
		
	}
	
	class BoardMouseListener implements MouseListener, MouseMotionListener {
		private Point currentCell = new Point(-1,-1);
		private Point oldMousePosition = new Point(0,0);
		private MouseButton currentButtonPressed;
		
		@Override
		public void mousePressed(MouseEvent e) {
			int cellX = (int) (	Math.floor(e.getX()/cellWidth())	); // do some arithmetic to translate the mouse's location 
			int cellY = (int) ( Math.floor(e.getY()/cellHeight())	); // on the board to an address in the displayArray
			
			switch(e.getButton()) { // if the left mouse button was clicked
				case MouseEvent.BUTTON1:
					currentButtonPressed = MouseButton.LEFT;
					break;
				case MouseEvent.BUTTON3: // but if the right mouse button was clicked
					currentButtonPressed = MouseButton.RIGHT;
					break;
				default: // if the user clicked neither with the left or right buttons, assume they wanted to scroll
					currentButtonPressed = MouseButton.WHEEL;
					oldMousePosition.setLocation(e.getX(), e.getY());
					currentCell.setLocation(-1,-1);
					break;
			}
			notifyBoardInteractionListeners(userSelection, currentButtonPressed, cellX, cellY);
		}
		public void mouseDragged(MouseEvent e) {
			Point cellLocation = getCellLocation(e.getX(), e.getY());
			if(shouldReportDragInteraction(cellLocation, e.getX(), e.getY())) { 
				if(currentButtonPressed == MouseButton.LEFT || currentButtonPressed == MouseButton.RIGHT) {
					currentCell.setLocation(cellLocation.x, cellLocation.y);
				} else if(currentButtonPressed == MouseButton.WHEEL) {
					dragScroll(e.getX(), e.getY());
				}
				notifyBoardInteractionListeners(userSelection, currentButtonPressed, cellLocation.x, cellLocation.y);
			}
		}
		private void dragScroll(int mouseX, int mouseY) {
			// Calculate how much to change the view
			int xDif = (int) oldMousePosition.x - mouseX;
			int yDif = (int) oldMousePosition.y - mouseY;
			int newX = userInterface.boardViewport.getViewPosition().x + xDif;
			int newY = userInterface.boardViewport.getViewPosition().y + yDif;
			
			Point newViewPosition = new Point(userInterface.boardViewport.getViewPosition().x, userInterface.boardViewport.getViewPosition().y);
			
			if( ( 0 < newX ) // If there's some board panel out of view in the x-direction
					&& ((newX + userInterface.boardViewport.getWidth()) < getWidth())) {
				newViewPosition.x = newX;
			}
			if( (0 < newY ) // If there's some board panel out of view in the y-direction
					&& ((newY + userInterface.boardViewport.getHeight()) < getHeight())
				) {
				newViewPosition.y = newY;
			}
			userInterface.boardViewport.setViewPosition(newViewPosition);
			revalidate();
			oldMousePosition.setLocation(mouseX, mouseY);
			// TODO: Does oldMousePosition need to be updated here?
		}
		
		@Override
		public void mouseReleased(MouseEvent e){
			currentButtonPressed = MouseButton.NONE;
		}
		
		private boolean hasInteractionWithCellBeenReported(int x, int y) {
			return ((x == currentCell.getX()) 
					&& (y == currentCell.getY()));
		}
		private boolean shouldReportDragInteraction(Point cellLocation, int mouseX, int mouseY) {
			return (!hasInteractionWithCellBeenReported(cellLocation.x, cellLocation.y)
					&& isPointOnBoard(mouseX, mouseY)
					&& (currentButtonPressed != MouseButton.NONE));
		}
		
		public void mouseEntered(MouseEvent e){}
		public void mouseExited(MouseEvent e){}
		public void mouseClicked(MouseEvent e){}
		public void mouseMoved(MouseEvent e){}
	}
}
