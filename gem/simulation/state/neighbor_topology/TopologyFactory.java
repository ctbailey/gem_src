package gem.simulation.state.neighbor_topology;

public class TopologyFactory {
	public static INeighborTopology createTopology(boolean isDirected, boolean isSmallWorld, float rewiringProbability) {
		if(isSmallWorld) {
			return new DirectedWattsStrogatzTopology(rewiringProbability);
		} else {
			if(isDirected) {
				return new DirectedMooreTopology();
			} else {
				return new UndirectedMooreTopology();
			}
		}
	}
}