package gem.simulation.state.neighbor_topology;

public class TopologyFactory {
	private static final boolean USE_DIRECTED = true;
	public static INeighborTopology createTopology(boolean isSmallWorld, float rewiringProbability) {
		if(isSmallWorld) {
			if(USE_DIRECTED) {
				return new DirectedEdgeSwapTopology(rewiringProbability);
			} else {
				return new EdgeSwapTopology(rewiringProbability);
			}
		} else {
			if(USE_DIRECTED) {
				return new DirectedMooreTopology();
			} else {
				return new UndirectedMooreTopology();
			}
		}
	}
}