package gem.simulation.state.neighbor_topology;

import java.awt.Point;

public class DirectedMooreTopology extends MooreTopology {
	@Override
	protected INeighborGraph getInfluenceGraph() {
		return new AsymmetricalInfluenceGraph();
	}

	@Override
	protected void addNeighborToGraph(INeighborGraph graph, Point currentPoint, Point neighbor) {
		graph.addEdge(currentPoint, neighbor);
		graph.addEdge(neighbor, currentPoint);
	}
}
