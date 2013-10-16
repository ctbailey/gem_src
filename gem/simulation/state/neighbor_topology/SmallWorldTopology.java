package gem.simulation.state.neighbor_topology;

import java.awt.Point;
import java.util.Set;

import gem.Debug;
//import gem.metrics.Metrics;
import gem.simulation.board.BoardDimensions;

public abstract class SmallWorldTopology implements INeighborTopology {
	protected static final boolean REWIRE_ONLY_SELECTED_CELLS = true;
	@Override
	public INeighborGraph createGraphWithThisTopology(BoardDimensions dimensions) {
		INeighborGraph graph = createRegularGraph(dimensions);
		rewireToSmallWorld(dimensions, graph);
		printNeighborCharacteristics(graph);

//		Debug.printLine("Calculating path length.");
//		double avgPathLength = calculateAveragePathLength(graph);
//		Debug.printLine("Average path length: " + avgPathLength);
//		
//		Debug.printLine("Calculating clustering coefficient.");
//		double avgClusteringCoef = Metrics.calculateAverageClusteringCoefficient(graph, dimensions);
//		Debug.printLine("Average clustering coefficient: " + avgClusteringCoef);
		return graph;
	}
	protected abstract INeighborGraph createRegularGraph(BoardDimensions dimensions);
	protected abstract void rewireToSmallWorld(BoardDimensions dimensions, INeighborGraph graph);
	private static void printNeighborCharacteristics(INeighborGraph g) {
		Set<Point> points = g.getAllLocations();
		java.util.Map<Integer, Integer> neighborQuantityToNumberOfCells = new java.util.LinkedHashMap<Integer, Integer>(); 
		for(Point p : points) {
			int incomingInfluence = g.getOutgoingInfluence(p).size();
			if(!neighborQuantityToNumberOfCells.containsKey(incomingInfluence)) {
				neighborQuantityToNumberOfCells.put(incomingInfluence, 0);
			}
			neighborQuantityToNumberOfCells.put(incomingInfluence, neighborQuantityToNumberOfCells.get(incomingInfluence) + 1);
		}
		for(int neighborQuantity : neighborQuantityToNumberOfCells.keySet()) {
			Debug.printLine(neighborQuantity + ", " + neighborQuantityToNumberOfCells.get(neighborQuantity));
		}
	}
	
	
}