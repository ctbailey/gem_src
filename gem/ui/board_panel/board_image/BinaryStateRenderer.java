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

import gem.simulation.state.IState;

public class BinaryStateRenderer extends BoardStateChangedRenderer {
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
	
	public BinaryStateRenderer(float preferredOpacity) {
		super(preferredOpacity);
	}

	@Override
	public boolean makesSpurious(IStateRenderer otherRenderer) {
		return (otherRenderer instanceof BoardStateChangedRenderer);
	}

	@Override
	protected RGBA getColorAtPoint(IState state, int x, int y,
			float preferredOpacity) {
		RGBA color;
		switch(state.getCell(x, y).getState()) {
			case IMPASSABLE: // if the cell is impassable, set color to grey
				color = new RGBA(IMPASSABLE_RED, IMPASSABLE_GREEN, IMPASSABLE_BLUE, preferredOpacity);
				break;
					
			case DEAD: // if the cell is off, set color to white
				color = new RGBA(DEAD_RED, DEAD_GREEN, DEAD_BLUE, preferredOpacity);
				break;	
				
			case ALIVE: // if the cell is on, set color to black
				color = new RGBA(ALIVE_RED, ALIVE_GREEN, ALIVE_BLUE, preferredOpacity);
				break;
				
			default:
				color = new RGBA(OTHER_RED, OTHER_GREEN, OTHER_BLUE, preferredOpacity);
		}
		return color;
	}
}
