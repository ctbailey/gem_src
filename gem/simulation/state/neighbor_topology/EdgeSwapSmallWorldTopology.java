package gem.simulation.state.neighbor_topology;

import java.awt.Point;
import java.util.LinkedHashSet;
import java.util.Random;
import java.util.Set;

import org.jgrapht.graph.DefaultWeightedEdge;

import gem.simulation.Utility;
import gem.simulation.board.BoardDimensions;

public class EdgeSwapSmallWorldTopology extends SmallWorldTopology {
	private final float rewiringProbability;
	private static final Random randomNumberGenerator = new Random();
	public EdgeSwapSmallWorldTopology(float rewiringProbability) {
		this.rewiringProbability = rewiringProbability; 
	}
	
	@Override
	protected INeighborGraph createRegularGraph(BoardDimensions dimensions) {
		return new UndirectedMooreTopology().createGraphWithThisTopology(dimensions);
	}

	@Override
	protected void rewireToSmallWorld(BoardDimensions dimensions,
			INeighborGraph graph) {
		Point[] nodes = Utility.toArray(graph.vertexSet());
		Set<DefaultWeightedEdge> edgesAlreadyIteratedOver = new LinkedHashSet<DefaultWeightedEdge>();
		for(Point p : nodes) {
			rewireSingleNode(p, dimensions, graph, edgesAlreadyIteratedOver);
		}
	}
	private void rewireSingleNode(Point p, BoardDimensions dimensions, INeighborGraph graph, Set<DefaultWeightedEdge> edgesAlreadyIteratedOver) {
		DefaultWeightedEdge[] edges = Utility.toArray(graph.edgesOf(p));
		for(int i = 0; i < edges.length; i++) {
			DefaultWeightedEdge e = edges[i];
			if(!edgesAlreadyIteratedOver.contains(e)) {
				edgesAlreadyIteratedOver.add(e);
				if(Math.random() <= rewiringProbability) {
					rewireSingleEdge(p, e, dimensions, graph);
				}
			}
		}
	}
	private void rewireSingleEdge(Point source1, DefaultWeightedEdge edge1, BoardDimensions dimensions, INeighborGraph graph) {
		Point source2 = Utility.getRandomPointOnBoard(dimensions);
		DefaultWeightedEdge edge2 = getRandomEdge(source2, graph);
		Point target1 = graph.getOtherNodeInEdge(source1, edge1);
		Point target2 = graph.getOtherNodeInEdge(source2, edge2);
		while(!isValidSwap(source1, source2, target1, target2, graph)) {
			source2 = Utility.getRandomPointOnBoard(dimensions);
			edge2 = getRandomEdge(source2, graph);
			target2 = graph.getOtherNodeInEdge(source2, edge2);
		}
		swapEdges(source1, source2, target1, target2, edge1, edge2, graph);
	}
	private void swapEdges(
			Point source1, Point source2, Point target1, Point target2, 
			DefaultWeightedEdge edge1, DefaultWeightedEdge edge2, 
			INeighborGraph g) {
		
		// Before:
		// source1 -> target1
		// source2 -> target2
		
		// After:
		// source1 -> target2
		// source2 -> target1
		
		// Technically since the graph
		// is undirected, there aren't
		// any sources or targets,
		// but it's easier to
		// think about if we pretend
		// there are.
		
		g.removeEdge(edge1);
		g.removeEdge(edge2);
		g.addEdge(source1, target2);
		g.addEdge(source2, target1);
	}
	private DefaultWeightedEdge getRandomEdge(Point node, INeighborGraph g) {
		Set<DefaultWeightedEdge> edges = g.edgesOf(node);
		int randomIndex = randomNumberGenerator.nextInt(edges.size());
		int i = 0;
		for(DefaultWeightedEdge e : edges) {
			if(i == randomIndex) {
				return e;
			}
			i++;
		}
		throw new RuntimeException("Random index was too big.");
	}
	private boolean isValidSwap(Point source1, Point source2, Point target1, Point target2, INeighborGraph graph) {
		return !source1.equals(source2) // Sources can't be the same node
				&& !target1.equals(target2) // Targets can't be the same node
				&& !source1.equals(target2) // No loops (source1 will be connected to target 2 in the swap)
				&& !source2.equals(target1) // Ditto
				&& !graph.areNeighbors(source1, target2) // source1 can't already be a neighbor of target2 (or swapping would add an extra edge between source1 and target 2) 
				&& !graph.areNeighbors(source2, target1); // ditto
		
	}
}
