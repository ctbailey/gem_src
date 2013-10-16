package gem.simulation.state.neighbor_topology;


import gem.simulation.Utility;

import java.awt.Point;
import java.io.Serializable;
import java.util.Set;

import org.jgrapht.graph.*;

public class AsymmetricalInfluenceGraph extends ListenableDirectedWeightedGraph<Point, DefaultWeightedEdge> implements INeighborGraph, Serializable {
	private static final long serialVersionUID = 6;

	/* Represents neighbor relationships that
	 * are potentially asymmetrical: i.e.,
	 * where a may influence b, but not
	 * be influenced by b.
	 */
	private SerializableDirectedNeighborIndex<Point, DefaultWeightedEdge> neighborIndex;
	
	public AsymmetricalInfluenceGraph() {
		super(DefaultWeightedEdge.class);
		neighborIndex = new SerializableDirectedNeighborIndex<Point, DefaultWeightedEdge>(this);
		this.addGraphListener(neighborIndex);
	}
	
	public Point[] getLocationsInfluencedBy(int x, int y) {
		Set<Point> successors = neighborIndex.successorsOf(new Point(x,y));
		return Utility.toArray(successors);
	}
	public Point[] getLocationsThatInfluence(int x, int y) {
		Set<Point> predecessors = neighborIndex.predecessorsOf(new Point(x,y));
		return Utility.toArray(predecessors);
	}
	public Set<DefaultWeightedEdge> getOutgoingInfluence(int x, int y) {
		return getOutgoingInfluence(new Point(x,y));
	}
	public Set<DefaultWeightedEdge> getIncomingInfluence(int x, int y) {
		return getIncomingInfluence(new Point(x,y));
	}
	public Set<DefaultWeightedEdge> getOutgoingInfluence(Point p) {
		return outgoingEdgesOf(p);
	}
	public Set<DefaultWeightedEdge> getIncomingInfluence(Point p) {
		return incomingEdgesOf(p);
	}
	public Set<Point> getAllLocations() {
		return vertexSet();
	}
	public Point[] getAllLocationsTouching(int x, int y) {
		return getAllLocationsTouching(new Point(x,y));
		
	}
	public Point[] getAllLocationsTouching(Point p) {
		Set<Point> predecessors = neighborIndex.predecessorsOf(p);
		Set<Point> successors = neighborIndex.successorsOf(p);
		Point[] allNeighbors = new Point[predecessors.size() + successors.size()];
		int i = 0;
		for(Point predecessor : predecessors) {
			allNeighbors[i] = predecessor;
			i++;
		}
		for(Point successor : successors) {
			allNeighbors[i] = successor;
			i++;
		}
		return allNeighbors;
	}

	@Override
	public Point getOtherNodeInEdge(Point p, DefaultWeightedEdge e) {
		return Utility.getOtherNode(p, e, this);
	}

	@Override
	public boolean areNeighbors(Point p1, Point p2) {
		return neighborIndex.successorsOf(p1).contains(p2)
				|| neighborIndex.predecessorsOf(p1).contains(p2);
	}
	
}
