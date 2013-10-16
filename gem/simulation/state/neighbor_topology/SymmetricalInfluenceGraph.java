package gem.simulation.state.neighbor_topology;


import gem.simulation.Utility;

import java.awt.Point;
import java.io.Serializable;
import java.util.Set;

import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.ListenableUndirectedWeightedGraph;

public class SymmetricalInfluenceGraph extends ListenableUndirectedWeightedGraph<Point, DefaultWeightedEdge> implements INeighborGraph, Serializable {
	private static final long serialVersionUID = 6;
	/* Represents a neighborhood relationship where
	 * all cells mutually influence one another.
	 */
	private final SerializableNeighborIndex<Point, DefaultWeightedEdge> neighborIndex;
	
	public SymmetricalInfluenceGraph() {
		super(DefaultWeightedEdge.class);
		neighborIndex = new SerializableNeighborIndex<Point, DefaultWeightedEdge>(this);
		this.addGraphListener(neighborIndex);
	}
	
	public Point[] getLocationsInfluencedBy(int x, int y) {
		return getAllLocationsTouching(x, y);
	}
	public Point[] getLocationsThatInfluence(int x, int y) {
		return getAllLocationsTouching(x, y);
	}
	public Set<DefaultWeightedEdge> getOutgoingInfluence(int x, int y) {
		return edgesOf(new Point(x,y));
	}
	public Set<DefaultWeightedEdge> getIncomingInfluence(int x, int y) {
		return edgesOf(new Point(x,y));
	}
	public Set<DefaultWeightedEdge> getOutgoingInfluence(Point p) {
		return edgesOf(p);
	}
	public Set<DefaultWeightedEdge> getIncomingInfluence(Point p) {
		return edgesOf(p);
	}
	public Set<Point> getAllLocations() {
		return vertexSet();
	}
	
	public Point[] getAllLocationsTouching(int x, int y) {
		return getAllLocationsTouching(new Point(x,y));
	}
	public Point[] getAllLocationsTouching(Point p) {
		Set<Point> neighborSet = neighborIndex.neighborsOf(p);
		return Utility.toArray(neighborSet);
	}

	@Override
	public Point getOtherNodeInEdge(Point p, DefaultWeightedEdge e) {
		return Utility.getOtherNode(p, e, this);
	}

	@Override
	public boolean areNeighbors(Point p1, Point p2) {
		return neighborIndex.neighborsOf(p1).contains(p2);
	}
	
}
