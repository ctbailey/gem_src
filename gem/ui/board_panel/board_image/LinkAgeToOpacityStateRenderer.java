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

package gem.ui.board_panel.board_image;

import static gem.Global.userInterface;

import gem.Global;
import gem.simulation.board.IBoardDidIterateListener;
import gem.simulation.board.IBoardResetListener;
import gem.simulation.state.IState;
import gem.simulation.state.ICell.CellState;
import gem.ui.UserDidNotConfirmException;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;

public class LinkAgeToOpacityStateRenderer extends BoardStateChangedRenderer implements IBoardDidIterateListener, IBoardResetListener {
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
	
	private static final float MINIMUM_OPACITY = 0.05f;
	private static final float NON_LIVING_OPACITY = 1;
	
	private int[][] cellAges;
	private int fullOpacityAge; // 255 is opaque
	
	public LinkAgeToOpacityStateRenderer(int fullOpacityAge, float preferredMaximumOpacity) {
		super(preferredMaximumOpacity);
		this.fullOpacityAge = fullOpacityAge;
		cellAges = new int[0][0];
		Global.simulator.getBoard().addBoardDidIterateListener(this);
		Global.simulator.getBoard().addBoardResetListener(this);
	}
	
	@Override
	public void boardDidCalculateNewState(IState newState,
			int updatedNumberOfIterations) {
		updateCellAge(newState);
	}
	
	@Override
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
		super.wasMadeSpurious(replacement);
		Global.simulator.getBoard().removeBoardDidIterateListener(this);
		Global.simulator.getBoard().removeBoardResetListener(this);
	}
	@Override
	protected RGBA getColorAtPoint(IState state, int x, int y,
			float normalizedMaxOpacity) {
		float opacity = -1;
		try{
			opacity = calculateOpacityForCell(x,y, state.getCell(x, y).getState(), normalizedMaxOpacity);
		}catch(ArrayIndexOutOfBoundsException ex) {
			cellAges = new int[state.getWidth()][state.getHeight()];
			opacity = calculateOpacityForCell(x,y, state.getCell(x, y).getState(), normalizedMaxOpacity);
		}
		RGBA color;
		switch(state.getCell(x, y).getState()) {
			case IMPASSABLE: // if the cell is impassable, set color to grey
				color = new RGBA(IMPASSABLE_RED, IMPASSABLE_GREEN, IMPASSABLE_BLUE, opacity);
				break;
					
			case DEAD: // if the cell is off, set color to white
				color = new RGBA(DEAD_RED, DEAD_GREEN, DEAD_BLUE, opacity);
				break;	
				
			case ALIVE: // if the cell is on, set color to black
				color = new RGBA(ALIVE_RED, ALIVE_GREEN, ALIVE_BLUE, opacity);
				break;
				
			default:
				color = new RGBA(OTHER_RED, OTHER_GREEN, OTHER_BLUE, opacity);
		}
		return color;
	}

	private float calculateOpacityForCell(int x, int y, CellState state, float normalizedMaximumOpacity) {
		float normalizedOpacity;
		if(state == CellState.ALIVE) {
			float tempOpacity = (float)cellAges[x][y] / (float)fullOpacityAge; // Between 0 and positive infinity
			normalizedOpacity = (float)Math.min(tempOpacity, 1.0f); // Between 0 and 1
		} else {
			normalizedOpacity = NON_LIVING_OPACITY;
		}
		normalizedOpacity *= normalizedMaximumOpacity;
		return Math.max(normalizedOpacity, MINIMUM_OPACITY);
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

	@Override
	public boolean makesSpurious(IStateRenderer otherRenderer) {
		return (otherRenderer instanceof BoardStateChangedRenderer);
	}
}
