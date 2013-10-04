package gem.simulation.state.neighbor_topology;

import java.awt.Point;
import java.util.Set;

import org.jgrapht.graph.DefaultWeightedEdge;

import gem.simulation.Utility;
import gem.simulation.board.BoardDimensions;

public class WattsStrogatzTopology extends SmallWorldTopology {
	private final float rewiringProbability;
	public WattsStrogatzTopology(float rewiringProbability) {
		this.rewiringProbability = rewiringProbability;
	}
	
	@Override
	protected INeighborGraph createRegularGraph(BoardDimensions dimensions) {
		return new UndirectedMooreTopology().createGraphWithThisTopology(dimensions);
	}

	@Override
	protected void rewireToSmallWorld(BoardDimensions dimensions,
			INeighborGraph graph) {
		Set<DefaultWeightedEdge> edges = graph.edgeSet();
		
		DefaultWeightedEdge[] edgeArray = new DefaultWeightedEdge[edges.size()];
		edges.toArray(edgeArray);
		
		for(int i = 0; i < edgeArray.length; i++) {
			if(Math.random() <= rewiringProbability) {
				Point newTarget = Utility.getRandomPointOnBoard(dimensions);
				Point existingSource = graph.getEdgeSource(edgeArray[i]);
				while(!pointIsValidRewiringLocationForSmallWorldConnection(newTarget, existingSource, graph)) {
					// Select another random point until you get one that's valid
					newTarget = Utility.getRandomPointOnBoard(dimensions);
				}
				graph.removeEdge(edgeArray[i]);
				graph.addEdge(existingSource, newTarget);
			}
		}
	}
	private static boolean pointIsValidRewiringLocationForSmallWorldConnection(Point newTarget, Point oldSource, INeighborGraph g) {
		return !pointsHaveSameCoordinates(newTarget, oldSource)
				&& !g.containsEdge(oldSource, newTarget);
	}
	private static boolean pointsHaveSameCoordinates(Point p1, Point p2) {
		return p1.x == p2.x 
				&& p1.y == p2.y;
	}
}
