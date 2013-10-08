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

package gem.simulation.state;

import gem.simulation.state.neighbor_topology.INeighborGraph;

import java.awt.Point;

public abstract class AbstractState implements IState {
	public static final int DEFAULT_WIDTH = 50;
	public static final int DEFAULT_HEIGHT = 50;
	
	protected INeighborGraph neighborGraph;
	
	public AbstractState(INeighborGraph neighborGraph) {
		this.neighborGraph = neighborGraph;
	}
	public ICell[] getCellsThatInfluenceCellAt(int x, int y) {
		Point[] neighborLocations =  neighborGraph.getLocationsThatInfluence(x,y);
		return getCells(neighborLocations);
	}
	public ICell[] getCellsInfluencedByCellAt(int x, int y) {
		Point[] neighborLocations = neighborGraph.getLocationsInfluencedBy(x,y);
		return getCells(neighborLocations);
	}
	public ICell[] getCells(Point[] locations) {
		ICell[] cells = new ICell[locations.length];
		int index = 0;
		for(Point p : locations) {
			cells[index] = getCell(p.x, p.y);
			index++;
		}
		return cells;
	}
	public Point[] getPointsThatInfluenceCellAt(int x, int y) {
		ICell[] influencers = getCellsThatInfluenceCellAt(x,y);
		return getLocationsOf(influencers);
	}
	public Point[] getPointsInfluencedByCellAt(int x, int y) {
		ICell[] influenceRecipients = getCellsInfluencedByCellAt(x,y);
		return getLocationsOf(influenceRecipients);
	}
	private Point[] getLocationsOf(ICell[] cells) {
		Point[] locations = new Point[cells.length];
		for(int i = 0; i < locations.length; i++) {
			locations[i] = getLocationOf(cells[i]);
		}
		return locations;
	}
	protected abstract Point getLocationOf(ICell c);
	public boolean isInBounds(int x, int y) {
		return (   x >= 0
				&& y >= 0
				&& x < getWidth()
				&& y < getHeight());
	}
}
