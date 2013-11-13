package gem.simulation.state.neighbor_topology;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import gem.Debug;
import gem.simulation.Utility;
import gem.simulation.board.BoardDimensions;

public abstract class MooreTopology implements INeighborTopology {
	@Override
	public INeighborGraph createGraphWithThisTopology(BoardDimensions dimensions) {
		INeighborGraph graph = getInfluenceGraph();
		for(int x = 0; x < dimensions.getWidth(); x++) {
			for(int y = 0; y < dimensions.getHeight(); y++) {
				addMooreNeighborsToGraph(x, y, dimensions, graph);
			}
		}
		print(graph);
		return graph;
	}
	protected abstract INeighborGraph getInfluenceGraph();
	private void addMooreNeighborsToGraph(int x, int y, BoardDimensions dimensions, INeighborGraph graph) {
		Point currentPoint = new Point(x,y);
		if(!graph.containsVertex(currentPoint)) {
			graph.addVertex(currentPoint);
		}
		
		List<Point> neighbors = getMooreNeighbors(x, y, dimensions);
		for(Point neighbor : neighbors) {
			if(!graph.containsVertex(neighbor)) {
				graph.addVertex(neighbor);
			}
			addNeighborToGraph(graph, currentPoint, neighbor);
		}
	}
	protected abstract void addNeighborToGraph(INeighborGraph graph, Point currentPoint, Point neighbor);
	private List<Point> getMooreNeighbors(int x, int y, BoardDimensions dimensions) {
		List<Point> neighbors = new ArrayList<Point>(8);
		
		for(int i = -1; i <= 1; i++) {
			for(int j = -1; j <= 1; j++) {
				if(Utility.isInBounds(x + i, y + j, dimensions)
					&& (i != 0 || j != 0)) {
					neighbors.add(new Point(x+i,y+j));
				}
			}
		}
		
		return neighbors;
	}
	private void print(INeighborGraph g) {
		Set<Point> points = g.getAllLocations();
		java.util.Map<Integer, Integer> neighborQuantityToNumberOfCells = new java.util.LinkedHashMap<Integer, Integer>(); 
		for(Point p : points) {
			int incomingInfluence = g.getIncomingInfluence(p).size();
			if(!neighborQuantityToNumberOfCells.containsKey(incomingInfluence)) {
				neighborQuantityToNumberOfCells.put(incomingInfluence, 0);
			}
			neighborQuantityToNumberOfCells.put(incomingInfluence, neighborQuantityToNumberOfCells.get(incomingInfluence) + 1);
		}
		for(int neighborQuantity : neighborQuantityToNumberOfCells.keySet()) {
			Debug.printLine(neighborQuantity + ", " + neighborQuantityToNumberOfCells.get(neighborQuantity));
		}
	}
	@Override
	public float getRewiringProbability() {
		return 0;
	}
}
