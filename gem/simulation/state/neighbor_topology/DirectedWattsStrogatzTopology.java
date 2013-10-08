package gem.simulation.state.neighbor_topology;

import java.awt.Point;
import java.util.Set;

import org.jgrapht.graph.DefaultWeightedEdge;

import gem.simulation.Utility;
import gem.simulation.board.BoardDimensions;

public class DirectedWattsStrogatzTopology extends SmallWorldTopology {
	private final boolean rewireIncomingEdges;
	private final float rewiringProbability;
	public DirectedWattsStrogatzTopology(float rewiringProbability, boolean rewireIncomingEdges) {
		this.rewiringProbability = rewiringProbability;
		this.rewireIncomingEdges = rewireIncomingEdges;
	}
	@Override
	protected INeighborGraph createRegularGraph(BoardDimensions dimensions) {
		return new DirectedMooreTopology().createGraphWithThisTopology(dimensions);
	}
	@Override
	protected void rewireToSmallWorld(BoardDimensions dimensions,
			INeighborGraph graph) {
		Set<Point> points = graph.vertexSet();
		if(rewireIncomingEdges) {
			for(Point p : points) {
				rewireIncomingEdges(p, graph, dimensions);
			}
		} else {
			for(Point p : points) {
				rewireOutgoingEdges(p, graph, dimensions);
			}
		}
	}
	private void rewireIncomingEdges(Point p, INeighborGraph g, BoardDimensions dimensions) {
		Set<DefaultWeightedEdge> edges = g.getIncomingInfluence(p);
		DefaultWeightedEdge[] edgeArray = new DefaultWeightedEdge[edges.size()];
		edges.toArray(edgeArray);
		for(int i = 0; i < edgeArray.length; i++) {
			if(Math.random() <= rewiringProbability) {
				rewireSingleIncomingEdge(p, edgeArray[i], dimensions, g);
			}
		}
	}
	
	private void rewireSingleIncomingEdge(Point target, DefaultWeightedEdge edge, BoardDimensions dimensions, INeighborGraph g) {
		Point newSource = Utility.getRandomPointOnBoard(dimensions);
		while(!pointIsValidRewiringLocationForIncomingSmallWorldConnection(newSource, target, g)) {
			// Select another random point until you get one that's valid
			newSource = Utility.getRandomPointOnBoard(dimensions);
		}
		g.removeEdge(edge);
		g.addEdge(newSource, target);
	}
	private static boolean pointIsValidRewiringLocationForIncomingSmallWorldConnection(Point source, Point target, INeighborGraph g) {
		return !target.equals(source) // equals() returns true if they have the same coordinates
				&& !g.containsEdge(source, target);
	}
	private void rewireOutgoingEdges(Point p, INeighborGraph g, BoardDimensions dimensions) {
		Set<DefaultWeightedEdge> edges = g.getOutgoingInfluence(p);
		DefaultWeightedEdge[] edgeArray = new DefaultWeightedEdge[edges.size()];
		edges.toArray(edgeArray);
		for(int i = 0; i < edgeArray.length; i++) {
			if(Math.random() <= rewiringProbability) {
				rewireSingleOutgoingEdge(p, edgeArray[i], dimensions, g);
			}
		}
	}
	private void rewireSingleOutgoingEdge(Point source, DefaultWeightedEdge edge, BoardDimensions dimensions, INeighborGraph g) {
		Point newTarget = Utility.getRandomPointOnBoard(dimensions);
		while(!pointIsValidRewiringLocationForOutgoingSmallWorldConnection(source, newTarget, g)) {
			// Select another random point until you get one that's valid
			newTarget = Utility.getRandomPointOnBoard(dimensions);
		}
		g.removeEdge(edge);
		g.addEdge(source, newTarget);
	}
	private static boolean pointIsValidRewiringLocationForOutgoingSmallWorldConnection(Point source, Point target, INeighborGraph g) {
		return !source.equals(target) // equals() returns true if they have the same coordinates
				&& !g.containsEdge(source, target);
	}
}