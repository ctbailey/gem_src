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

package gem.ui.board_image;

import static gem.Global.userInterface;

import java.awt.Image;

import gem.Global;
import gem.simulation.board.IBoardResetListener;
import gem.simulation.board.IBoardStateChangedListener;
import gem.simulation.board.ICell.CellState;
import gem.simulation.state.IState;
import gem.ui.UserDidNotConfirmException;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;


public class LinkAgeToOpacityStateRenderer extends SingleColorCellRenderer implements IBoardStateChangedListener, IBoardResetListener {
	private static final int IMPASSABLE_RED = 175;
	private static final int IMPASSABLE_BLUE = 175;
	private static final int IMPASSABLE_GREEN = 175;
	
	private static final int ALIVE_RED = 0;
	private static final int ALIVE_BLUE = 0;
	private static final int ALIVE_GREEN = 0;
	
	private static final int DEAD_RED = 255;
	private static final int DEAD_BLUE = 255;
	private static final int DEAD_GREEN = 255;
	
	private static final int OTHER_RED = 255;
	private static final int OTHER_BLUE = 0;
	private static final int OTHER_GREEN = 0;
	
	private static final int MINIMUM_OPACITY = 1;
	private static final int NON_LIVING_OPACITY = 255;
	
	private int[][] cellAges;
	private int fullOpacityAge; // 255 is opaque
	private int previousNumberOfIterations = 0;
	
	public LinkAgeToOpacityStateRenderer(int fullOpacityAge, float preferredMaximumOpacity) {
		super(preferredMaximumOpacity);
		this.previousNumberOfIterations = Global.simulator.getBoard().getNumberOfIterations();
		this.fullOpacityAge = fullOpacityAge;
		cellAges = new int[0][0];
		
		Global.simulator.getBoard().addBoardStateChangedListener(this);
		Global.simulator.getBoard().addBoardResetListener(this);
	}
	
	@Override
	public void boardStateChanged(IState newState, int numberOfIterations) {
		if(boardDidIterate(numberOfIterations)) {
			updateCellAge(newState);
		}
		setLatestImage(renderState(newState));
	}
	private boolean boardDidIterate(int newNumberOfIterations) {
		if(newNumberOfIterations == previousNumberOfIterations + 1) {
			previousNumberOfIterations = newNumberOfIterations;
			return true;
		} else if(newNumberOfIterations <= previousNumberOfIterations) {
			previousNumberOfIterations = newNumberOfIterations;
			return false;
		} else {
			throw new RuntimeException("Something unexpected happened - the automaton skipped ahead more than one iteration.");
		}
	}
	public void boardWasReset() {
		resetCellAges();
	}
	private void resetCellAges() {
		for(int x = 0; x < cellAges.length; x++) {
			for(int y = 0; y < cellAges[x].length; y++) {
				cellAges[x][y] = 0;
			}
		}
	}
	
	public void wasMadeSpurious(IStateRenderer replacement) {
		Global.simulator.getBoard().removeBoardStateChangedListener(this);
		Global.simulator.getBoard().removeBoardResetListener(this);
	}
	
	@Override
	public void refreshImage() {
		setLatestImage(renderState(Global.simulator.getBoard().getCurrentState()));
	}
	
	
	private Image renderState(IState newState) {
		int[] pixels = calculatePixels(newState, getPreferredOpacity());
		return createImageFromPixels(pixels, newState.getWidth(), newState.getHeight());
	}
	private int[] calculatePixels(IState newState, float normalizedMaximumOpacity) {
		int index = 0;
		int[] imageArray = new int[newState.getNumberOfCells()];
		
		try {
			for(int y = 0; y < newState.getHeight(); y++) {
				for(int x = 0; x < newState.getWidth(); x++) {
					int opacity = calculateOpacityForCell(x,y, newState.getCell(x, y).getState(), normalizedMaximumOpacity);
					imageArray[index] = calculatePixelValue(opacity, newState.getCell(x, y).getState());
					index++;
				}
			}
			return imageArray;
		} catch(IndexOutOfBoundsException e) {
			cellAges = new int[newState.getWidth()][newState.getHeight()];
			return calculatePixels(newState, normalizedMaximumOpacity);
		}
	}
	private int calculatePixelValue(int opacity, CellState cellState) {
		int pixel;
		switch(cellState) {
			case IMPASSABLE: // if the cell is impassable, set color to grey
				pixel = (opacity << 24) | (IMPASSABLE_RED << 16) | (IMPASSABLE_GREEN << 8) | (IMPASSABLE_BLUE);
				break;
					
			case DEAD: // if the cell is off, set color to white
				pixel = (opacity << 24) | (DEAD_RED << 16) | (DEAD_GREEN << 8) | (DEAD_BLUE);
				break;	
				
			case ALIVE: // if the cell is on, set color to black
				pixel = (opacity << 24) | (ALIVE_RED << 16) | (ALIVE_GREEN << 8) | (ALIVE_BLUE);
				break;
				
			default:
				pixel = (opacity << 24) | (OTHER_RED << 16) | (OTHER_GREEN << 8) | (OTHER_BLUE);
		}
		return pixel;
	}
	private int calculateOpacityForCell(int x, int y, CellState state, float normalizedMaximumOpacity) {
		int opacity;
		if(state == CellState.ALIVE) {
			double normalizedOpacity = (float)cellAges[x][y] / (float)fullOpacityAge;
			normalizedOpacity = Math.min(normalizedOpacity, 1.0); // now normalizedOpacity is between 0 and 1
			int tempOpacity = (int)(normalizedOpacity * 255);
			opacity = Math.max(tempOpacity, MINIMUM_OPACITY);
		} else {
			opacity = NON_LIVING_OPACITY;
		}
		
		return (int)(opacity * normalizedMaximumOpacity);
	}
	private void updateCellAge(IState newState) {
		try {
			for(int x = 0; x < newState.getWidth(); x++) {
				for(int y = 0; y < newState.getHeight(); y++) {
					if(newState.getCell(x, y).getState() == CellState.ALIVE) {
						cellAges[x][y]++;
					} else {
						cellAges[x][y] = 0;
					}
				}
			}
		} catch(IndexOutOfBoundsException e) {
			cellAges = new int[newState.getWidth()][newState.getHeight()];
		}
	}
	
	public static LinkAgeToOpacityStateRenderer createInstanceFromUserInput() throws UserDidNotConfirmException {
		// Create user input panel
		SpinnerModel fullOpacityAgeModel =
				new SpinnerNumberModel(	5, //initial value
										0, //min
										Integer.MAX_VALUE, //max
										1); //step
		JSpinner fullOpacityAgeSpinner = new JSpinner(fullOpacityAgeModel);
		
		JPanel userInputPanel = new JPanel();
		userInputPanel.add(new JLabel("Age resulting in full opacity: "));
		userInputPanel.add(fullOpacityAgeSpinner);
		
		int userSelection = JOptionPane.showConfirmDialog(userInterface.mainFrame, userInputPanel, "Linke Age to Cell Opacity", JOptionPane.OK_CANCEL_OPTION);
		if(userSelection == JOptionPane.OK_OPTION) {
			return new LinkAgeToOpacityStateRenderer((Integer)fullOpacityAgeModel.getValue(), 1.0f);
		} else {
			throw new UserDidNotConfirmException();
		}
	}
}
