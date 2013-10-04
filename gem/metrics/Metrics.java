package gem.metrics;

import gem.simulation.Utility;
import gem.simulation.board.BoardDimensions;
import gem.simulation.state.neighbor_topology.INeighborGraph;
import gem.simulation.state.neighbor_topology.UndirectedMooreTopology;

import java.awt.Point;
import java.util.List;

import org.jgrapht.GraphPath;
import org.jgrapht.alg.FloydWarshallShortestPaths;
import org.jgrapht.graph.DefaultWeightedEdge;

public class Metrics {
	private Metrics() {}
	public static double calculateAveragePathLength(INeighborGraph g) {
		double sumOfPathLengths = 0;
		Point[] nodes = Utility.toArray(g.vertexSet());
		FloydWarshallShortestPaths<Point, DefaultWeightedEdge> shortestPathLengths = new FloydWarshallShortestPaths<Point, DefaultWeightedEdge>(g);
		for(Point node : nodes) {
			List<GraphPath<Point, DefaultWeightedEdge>> paths = shortestPathLengths.getShortestPaths(node);
			for(GraphPath<Point, DefaultWeightedEdge> path : paths) {
				sumOfPathLengths += path.getEdgeList().size();
			}
		}
		double averageOfPathLengths = sumOfPathLengths / (nodes.length * (nodes.length - 1));
		return averageOfPathLengths;
	}
	public static double calculateAverageClusteringCoefficient(INeighborGraph g, BoardDimensions dimensions) {
		double sumOfLocalClusteringCoefficients = 0;
		// The Moore neighbor graph represents the maximum connectivity for a node in a given neighborhood
		INeighborGraph mooreNeighborGraph = new UndirectedMooreTopology().createGraphWithThisTopology(dimensions);
		for(Point p : g.vertexSet()) {
			double actualNumberOfConnectionsBetweenMooreNeighbors = getNumberOfConnectionsBetweenMooreNeighbors(p, g, mooreNeighborGraph);
			double numberOfMooreNeighbors = mooreNeighborGraph.getAllLocationsTouching(p).length;
			double maxNumberOfConnectionsBetweenMooreNeighbors = (numberOfMooreNeighbors * (numberOfMooreNeighbors - 1.0d)) / 2.0d;
			double localClusteringCoefficient = actualNumberOfConnectionsBetweenMooreNeighbors / maxNumberOfConnectionsBetweenMooreNeighbors;
			sumOfLocalClusteringCoefficients += localClusteringCoefficient;
		}
		double averageClusteringCoefficient = sumOfLocalClusteringCoefficients / g.vertexSet().size();
		return averageClusteringCoefficient;
	}
	private static double getNumberOfConnectionsBetweenMooreNeighbors(Point p, INeighborGraph g, INeighborGraph mooreNeighborGraph) {
		double numberOfConnectionsBetweenMooreNeighbors = 0;
		Point[] mooreNeighbors = mooreNeighborGraph.getAllLocationsTouching(p);
		for (int i = 0; i < mooreNeighbors.length; i++) {
			for(int j = i; j < mooreNeighbors.length; j++) {
				Point neighbor1 = mooreNeighbors[i];
				Point neighbor2 = mooreNeighbors[j];
				if(!neighbor1.equals(neighbor2)
					&& g.areNeighbors(neighbor1, neighbor2)) {
					numberOfConnectionsBetweenMooreNeighbors++;
				}
			}
		}
		return numberOfConnectionsBetweenMooreNeighbors;
	}
}
