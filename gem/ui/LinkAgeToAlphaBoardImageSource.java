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

import static gem.AutomatonGlobal.userInterface;
import gem.simulation.IState;
import gem.simulation.ICell.CellState;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;


public class LinkAgeToAlphaBoardImageSource extends AbstractBoardImageSource {
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
	
	private static final int MINIMUM_OPACITY = 10;
	private static final int NON_LIVING_OPACITY = 255;
	
	private byte[][] cellAges;
	private int fullOpacityAge; // 255 is opaque
	
	public LinkAgeToAlphaBoardImageSource(int fullOpacityAge) {
		this.fullOpacityAge = fullOpacityAge;
		cellAges = new byte[0][0];
	}
	@Override
	protected int[] calculatePixels(IState newState, boolean showMap) {
		updateCellAge(newState);
		
		int index = 0;
		int[] imageArray = new int[newState.getNumberOfCells()];
		
		for(int y = 0; y < newState.getHeight(); y++) {
			for(int x = 0; x < newState.getWidth(); x++) {
				imageArray[index] = calculatePixelValue(calculateOpacityForCell(x,y, newState.getCell(x, y).getState()), newState.getCell(x, y).getState());
				index++;
			}
		}
		return imageArray;
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
	private int calculateOpacityForCell(int x, int y, CellState state) {
		if(state == CellState.ALIVE) {
			double normalizedOpacity = cellAges[x][y] / fullOpacityAge;
			normalizedOpacity = Math.min(normalizedOpacity, 1.0);
			int opacity = (int)normalizedOpacity*255;
			return Math.max(opacity, MINIMUM_OPACITY);
		} else {
			return NON_LIVING_OPACITY;
		}
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
		} catch(IndexOutOfBoundsException ex) {
			cellAges = new byte[newState.getWidth()][newState.getHeight()];
			for(int x = 0; x < newState.getWidth(); x++) {
				for(int y = 0; y < newState.getHeight(); y++) {
					cellAges[x][y] = 0;
				}
			}
		}
	}

	public static LinkAgeToAlphaBoardImageSource createInstanceFromUserInput() throws UserDidNotConfirmException {
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
			return new LinkAgeToAlphaBoardImageSource((Integer)fullOpacityAgeModel.getValue());
		} else {
			throw new UserDidNotConfirmException();
		}
	}
}
