package gem.simulation.state.neighbor_topology;

import java.awt.Point;

public class UndirectedMooreTopology extends MooreTopology {
	@Override
	protected INeighborGraph getInfluenceGraph() {
		return new SymmetricalInfluenceGraph();
	}
	@Override
	protected void addNeighborToGraph(INeighborGraph graph, Point currentPoint,
			Point neighbor) {
		graph.addEdge(currentPoint, neighbor);
	}
	
}