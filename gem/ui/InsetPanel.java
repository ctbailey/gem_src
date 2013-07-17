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

import java.awt.*;
import java.awt.event.*;
import javax.swing.JPanel;

import static gem.AutomatonGlobal.*;

public class InsetPanel extends JPanel implements MouseWheelListener {

	/*
	 * See the AutomatonVars class for a number of important variables that aren't declared here.
	 */
	
	/*
	 * Simply a space-filling panel. Surrounds the board panel in the scroll pane and keeps it centered.
	 * Also implements custom zooming with the mouse wheel. (It's implemented here rather than in the
	 * board panel so that the user can use the mouse wheel to zoom anywhere in the board area.)
	 * 
	 */
	
	static final long serialVersionUID = 5;
	double scale = 1.0; // The amount to zoom
	
// Constructor
	
	public InsetPanel(LayoutManager lm) {
		
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
			userInterface.boardPanel.zoom(scale);
		} else { // otherwise the wheel was moved upwards
			scale = Math.pow(1.05, e.getScrollAmount()); // Multiply 1.05 by the amount the mouse wheel was moved; call this scale
			userInterface.boardPanel.zoom(scale);
		}

	}

}
