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

package gem.ui.board_panel;

import gem.Global;
import gem.ui.board_panel.board_image.AbstractBoardImageSource;
import gem.ui.board_panel.board_image.ChainBoardImageSource;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.*;

import javax.imageio.ImageIO;
import javax.swing.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
 

public class BoardPanel extends AbstractBoardPanel {
	static final long serialVersionUID = 6;
	
	public enum MouseButton {LEFT, RIGHT, WHEEL, NONE}
	private static final int BOARD_MIN_SIZE = 100; // The minimum size of the board in "user space"
	private static final double DEFAULT_ZOOM_IN_SCALE = 1.2;
	private static final double DEFAULT_ZOOM_OUT_SCALE = 0.8;
	
	private BoardPanelMouseManager mouseManager;
	private AbstractBoardImageSource boardImageSource;
	public BufferedImage map;
	
	private AbstractCellChangeActionNotifier cellChangeActionNotifier;
	// Event fields
	private List<IBoardInteractionListener> boardInteractionListeners = new ArrayList<IBoardInteractionListener>();
	private List<IMouseMovedIntoNewCellListener> mouseMovedIntoNewCellListeners = new ArrayList<IMouseMovedIntoNewCellListener>();
	
	public BoardPanel() {
		super();
		
		setBoardImageSource(ChainBoardImageSource.getDefault());
		boardImageSource.addBoardImageChangedListener(this);
		
		mouseManager = new BoardPanelMouseManager();
		
		this.addMouseListener(mouseManager);
		this.addMouseMotionListener(mouseManager);
		
		cellChangeActionNotifier = new CellChangeActionNotifier();
		this.addBoardInteractionListener(cellChangeActionNotifier);
		cellChangeActionNotifier.addCellChangeActionListener(Global.simulator.getBoard());
		
		this.setToolTipText("");
		
		repaint();
	}
	
	public String getToolTipText(MouseEvent ev) {
		StringBuffer sb = new StringBuffer("<html>"); // used to permit multiple lines in the tooltip
		int cellX = (int) (	Math.floor(ev.getX()/cellWidth())	); // do some arithmetic to translate the mouse's location 
		int cellY = (int) ( Math.floor(ev.getY()/cellHeight())	); // on the board to an address in the displayArray
		
		if((Global.geography.longitudeArray != null)
				&& (Global.geography.latitudeArray != null)) {
			
			sb.append("Longitude: " + Global.geography.longitudeArray[cellX][cellY] + "<br>");
			sb.append("Latitude: " + Global.geography.latitudeArray[cellX][cellY] + "<br>");
			
		}
		if(Global.metadata.metadataArray !=null) {
			Point toolTipPoint = new Point(cellX, cellY);
			if(Global.metadata.inferredMetadataPoints.contains(toolTipPoint)) {
				sb.append("INFERRED <br>");
			}
			for(int i = 0; i < Global.metadata.categoryIdentifiers.length; i++) {
				sb.append(Global.metadata.categoryIdentifiers[i] + ": ");
				sb.append(Global.metadata.metadataArray[cellX][cellY][i] + "<br>"); // <br> is analogous to \n; creates a new line
			}
		}
		sb.append("X: " + cellX + "<br>");
		sb.append("Y: " + cellY + "<br>");
		return sb.toString();
	}
	
	@Override
	public void refreshBoardImage() {
		repaint();
	}
	
	@Override
	public void boardImageChanged(Image newBoardImage) {
		repaint();
	}
	public void paint(Graphics g) {		
		Graphics2D g2d = (Graphics2D) g;
		g2d.clearRect(0, 0, this.getWidth(), this.getHeight());
		g2d.drawImage(boardImageSource.getCurrentBoardImage(),0,0,this.getWidth(),this.getHeight(), null); // draw the board
	}
	
	private double cellWidth() {
		double cellWidth = (double) this.getWidth() / (double)Global.simulator.getBoard().getCurrentState().getWidth(); // assumes a rectangular world array
		return cellWidth;
	}
	private double cellHeight() {
		double cellHeight = (double) this.getHeight()/ (double)Global.simulator.getBoard().getCurrentState().getHeight();		
		return cellHeight;
	}
	
	@Override
	public AbstractBoardImageSource getBoardImageSource() {
		return boardImageSource;
	}
	@Override
	public void setBoardImageSource(AbstractBoardImageSource newImageSource) {
		if(boardImageSource != null) {
			boardImageSource.isBeingReplacedBy(newImageSource);
		}
		newImageSource.isReplacing(boardImageSource);
		boardImageSource = newImageSource;
	}
	@Override
	public void zoomIn() {
		zoom(DEFAULT_ZOOM_IN_SCALE);
	}
	@Override
	public void zoomOut() {
		zoom(DEFAULT_ZOOM_OUT_SCALE);
	}
	@Override
	public void zoom(double scale) {
		Dimension current = getPreferredSize();
		double aspectRatio = (double) Global.simulator.getBoard().getCurrentState().getWidth()/Global.simulator.getBoard().getCurrentState().getHeight();
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
	@Override
	public void zoomToFit() {
		int scrollPaneWidth = Global.userInterface.boardScrollPane.getWidth();
		int scrollPaneHeight = Global.userInterface.boardScrollPane.getHeight();
					
		int boardWidth = getWidth();
		int boardHeight = getHeight();
					
		double xScale = (double) scrollPaneWidth/boardWidth;
		double yScale = (double) scrollPaneHeight/boardHeight;
					
		double scale = Math.min(xScale, yScale);
					
		zoom(scale*0.9);
	}

	public Point getCellLocationMouseIsOver() {
		return mouseManager.getCellLocationMouseIsOver();
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
	
	// Mouse manager
	private class BoardPanelMouseManager implements MouseListener, MouseMotionListener {
		// Keeps up-to-date information about the state of the mouse relative to
		// the board. Serves as a central resource for several inner classes
		// (e.g., BoardInteractionNotifier) which all use that information.
		
		private MouseButton currentButtonPressed;
		private Point currentCell = new Point(-1,-1);
		private List<MouseListener> mouseListeners;
		private List<MouseMotionListener> mouseMotionListeners;
		
		BoardPanelMouseManager() {
			mouseListeners = new ArrayList<MouseListener>();
			mouseMotionListeners = new ArrayList<MouseMotionListener>();
			
			BoardInteractionNotifier boardInteractionNotifier = new BoardInteractionNotifier();
			mouseListeners.add(boardInteractionNotifier);
			mouseMotionListeners.add(boardInteractionNotifier);
			
			Scroller scroller = new Scroller(Global.userInterface.boardViewport, Global.userInterface.boardPanel);
			mouseListeners.add(scroller);
			mouseMotionListeners.add(scroller);
			
			MouseMovedIntoNewCellNotifier mouseMovedIntoNewCellNotifier = new MouseMovedIntoNewCellNotifier();
			//TODO: Figure out a way to do this so it doesn't throw a null reference exception.  Global.userInterface.mouseLeftBoardNotifier.addMouseLeftBoardListener(mouseMovedIntoNewCellNotifier);
			mouseMotionListeners.add(mouseMovedIntoNewCellNotifier);
		}
		
		public Point getCellLocationMouseIsOver() {
			return new Point(currentCell.x, currentCell.y);
		}
		
		@Override
		public void mousePressed(MouseEvent e) {
			currentCell = getCellLocation(e.getX(), e.getY());
			
			switch(e.getButton()) { 
				case MouseEvent.BUTTON1: // if the left mouse button was clicked
					currentButtonPressed = MouseButton.LEFT;
					break;
				case MouseEvent.BUTTON3: // but if the right mouse button was clicked
					currentButtonPressed = MouseButton.RIGHT;
					break;
				default: // if the user clicked neither with the left or right buttons
					currentButtonPressed = MouseButton.WHEEL;
					break;
			}
			notifyMousePressedListeners(e);
		}
		@Override
		public void mouseReleased(MouseEvent e){
			currentButtonPressed = MouseButton.NONE;
			notifyMouseReleasedListeners(e);
		}
		@Override
		public void mouseDragged(MouseEvent e) {
			currentCell = getCellLocation(e.getX(), e.getY());
			notifyMouseDraggedListeners(e);
		}
		
		@Override
		public void mouseMoved(MouseEvent e) {
			currentCell = getCellLocation(e.getX(), e.getY());
			notifyMouseMovedListeners(e);
		}
		@Override
		public void mouseClicked(MouseEvent e) {
			notifyMouseClickedListeners(e);
		}
		@Override
		public void mouseEntered(MouseEvent e) {
			notifyMouseEnteredListeners(e);
		}
		@Override
		public void mouseExited(MouseEvent e) {
			notifyMouseExitedListeners(e);
		}
		
		private void notifyMousePressedListeners(MouseEvent e) {
			for(MouseListener listener : mouseListeners) {
				listener.mousePressed(e);
			}
		}
		private void notifyMouseReleasedListeners(MouseEvent e) {
			for(MouseListener listener : mouseListeners) {
				listener.mouseReleased(e);
			}
		}
		private void notifyMouseDraggedListeners(MouseEvent e) {
			for(MouseMotionListener listener : mouseMotionListeners) {
				listener.mouseDragged(e);
			}
		}
		private void notifyMouseMovedListeners(MouseEvent e) {
			for(MouseMotionListener listener : mouseMotionListeners) {
				listener.mouseMoved(e);
			}
		}
		private void notifyMouseClickedListeners(MouseEvent e) {
			for(MouseListener listener : mouseListeners) {
				listener.mouseClicked(e);
			}
		}
		private void notifyMouseEnteredListeners(MouseEvent e) {
			for(MouseListener listener : mouseListeners) {
				listener.mouseEntered(e);
			}
		}
		private void notifyMouseExitedListeners(MouseEvent e) {
			for(MouseListener listener : mouseListeners) {
				listener.mouseExited(e);
			}
		}
		
		// Inner classes
		private class BoardInteractionNotifier implements MouseListener, MouseMotionListener {
			private Point previousCell = new Point(-1, -1);
			
			@Override
			public void mousePressed(MouseEvent e) {
				previousCell.setLocation(currentCell);
				notifyBoardInteractionListeners(currentButtonPressed, Global.userInterface.keyboard.getKeysPressed(), currentCell.x, currentCell.y);
			}
			@Override
			public void mouseDragged(MouseEvent e) {
				if(shouldReportDragInteraction(currentCell, e.getX(), e.getY())) {
					previousCell.setLocation(currentCell);
					notifyBoardInteractionListeners(currentButtonPressed, Global.userInterface.keyboard.getKeysPressed(), currentCell.x, currentCell.y);
				}
			}
			
			private boolean mouseEnteredNewCell() {
				return ((previousCell.x != currentCell.getX()) 
						|| (previousCell.y != currentCell.getY()));
			}
			private boolean shouldReportDragInteraction(Point cellLocation, int mouseX, int mouseY) {
				return (mouseEnteredNewCell()
						&& isPointOnBoard(mouseX, mouseY)
						&& (currentButtonPressed != MouseButton.NONE));
			}
			
			public void mouseReleased(MouseEvent e){}
			public void mouseEntered(MouseEvent e){}
			public void mouseExited(MouseEvent e){}
			public void mouseClicked(MouseEvent e){}
			public void mouseMoved(MouseEvent e){}
		}
		private class Scroller implements MouseListener, MouseMotionListener {
			private Point oldMousePosition = new Point(0,0);
			private MouseButton scrollButton;
			private JViewport viewport;
			private JComponent componentToScroll;
			
			public Scroller(JViewport viewport, JComponent componentToScroll) {
				this(MouseButton.WHEEL, viewport, componentToScroll);
			}
			public Scroller(MouseButton scrollButton, JViewport viewport, JComponent componentToScroll) {
				this.scrollButton = scrollButton;
				this.viewport = viewport;
				this.componentToScroll = componentToScroll;
			}
			
			@Override
			public void mousePressed(MouseEvent e) {
				if(currentButtonPressed == scrollButton) { 
					oldMousePosition.setLocation(e.getX(), e.getY());
				}
			}
			public void mouseDragged(MouseEvent e) {
				if(shouldScroll(e.getX(), e.getY())) { 
					dragScroll(e.getX(), e.getY());
				}
			}
			private void dragScroll(int mouseX, int mouseY) {
				// Calculate how much to change the view
				int xDif = (int) oldMousePosition.x - mouseX;
				int yDif = (int) oldMousePosition.y - mouseY;
				int newX = viewport.getViewPosition().x + xDif;
				int newY = viewport.getViewPosition().y + yDif;
				
				Point newViewPosition = new Point(viewport.getViewPosition().x, viewport.getViewPosition().y);
				
				if( (0 < newX) // If there's some board panel out of view in the x-direction
					&& ((newX + viewport.getWidth()) < componentToScroll.getWidth())) {
					newViewPosition.x = newX;
				}
				if( (0 < newY) // If there's some board panel out of view in the y-direction
					&& ((newY + viewport.getHeight()) < componentToScroll.getHeight())
					) {
					newViewPosition.y = newY;
				}
				viewport.setViewPosition(newViewPosition);
				componentToScroll.revalidate();
				oldMousePosition.setLocation(mouseX, mouseY);
			}
			
			private boolean shouldScroll(int mouseX, int mouseY) {
				return (currentButtonPressed == scrollButton)
						&& isPointOnBoard(mouseX, mouseY);
			}
			
			public void mouseReleased(MouseEvent e){}
			public void mouseEntered(MouseEvent e){}
			public void mouseExited(MouseEvent e){}
			public void mouseClicked(MouseEvent e){}
			public void mouseMoved(MouseEvent e){}
			
		}
		private class MouseMovedIntoNewCellNotifier implements MouseMotionListener, IMouseLeftBoardListener {
			private Point previousCell = new Point(-1, -1);
			
			@Override
			public void mouseDragged(MouseEvent e) {
				tryNotifyListeners();
			}
			@Override
			public void mouseMoved(MouseEvent e) {
				tryNotifyListeners();
			}
			private boolean tryNotifyListeners() {
				boolean mouseEnteredNewCell = mouseEnteredNewCell();
				if(mouseEnteredNewCell) {
					previousCell.setLocation(currentCell);
					notifyMouseMovedIntoNewCellListeners(currentCell.x, currentCell.y);
				}
				return mouseEnteredNewCell;
			}
			private boolean mouseEnteredNewCell() {
				return (previousCell == null)
						|| (previousCell.x != currentCell.x)
						|| (previousCell.y != currentCell.y);
			}
			@Override
			public void mouseLeftBoard() {
				previousCell = null;
			}
		}
		
	}
	
	// Event methods
	@Override
	public void addBoardInteractionListener(IBoardInteractionListener listener) {
		boardInteractionListeners.add(listener);
	}
	@Override
	public void removeBoardInteractionListener(IBoardInteractionListener listener) {
		boardInteractionListeners.remove(listener);
	}
	private void notifyBoardInteractionListeners(MouseButton buttonPressed, Set<String> keysPressed, int cellX, int cellY) {
		for(IBoardInteractionListener listener : boardInteractionListeners) {
			listener.userInteracted(buttonPressed, keysPressed, cellX, cellY);
		}
	}

	public void addMouseMovedIntoNewCellListener(IMouseMovedIntoNewCellListener listener) {
		mouseMovedIntoNewCellListeners.add(listener);
	}
	public void removeMouseMovedIntoNewCellListener(IMouseMovedIntoNewCellListener listener) {
		mouseMovedIntoNewCellListeners.remove(listener);
	}
	private void notifyMouseMovedIntoNewCellListeners(int cellX, int cellY) {
		for(IMouseMovedIntoNewCellListener listener : mouseMovedIntoNewCellListeners) {
			listener.mouseMovedIntoNewCell(cellX, cellY);
		}
	}
	
	@Override
	public void addCellChangeActionListener(ICellChangeActionListener listener) {
		cellChangeActionNotifier.addCellChangeActionListener(listener);
	}

	@Override
	public boolean removeCellChangeActionListener(ICellChangeActionListener listener) {
		return cellChangeActionNotifier.removeCellChangeActionListener(listener);
	}
	
	// Save and load methods
	public void save(ObjectOutputStream output) {
		
		Object[] savedBoardPanel = new Object[2];
		
		savedBoardPanel[0] = boardImageSource;
		savedBoardPanel[1] = getPreferredSize();
				
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
			
			Boolean readImage = (Boolean) loadedBoardPanel[3];
			
			BufferedImage loadedMap = null;
			
			if(readImage.booleanValue()) {
				loadedMap = (BufferedImage) ImageIO.read(input);
			}
			
			setBoardImageSource(loadedDisplayBoard);
			setPreferredSize(loadedPreferredSize);
			
			if(loadedMap != null) {
				map = loadedMap;
			}
			revalidate();
		} catch(Exception ex) {
			ex.printStackTrace();
		}
		
	}

	
	// UI methods
	@Override
	public JMenu getJMenu() {
		JMenu viewMenu = new JMenu("View");
		List<JMenuItem> bisMenuItems = getBoardImageSource().getMenuItems();
		for(JMenuItem menuItem : bisMenuItems) {
			viewMenu.add(menuItem);
		}
		return viewMenu;
	}
}
