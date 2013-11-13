package gem.simulation.state.neighbor_topology;

public class TopologySpecification {
	public enum TopologyAlgorithm {
		WattsStrogatz {
			@Override
			public boolean isSmallWorld() {
				return true;
			}
		}, 
		EdgeSwap {
			@Override
			public boolean isSmallWorld() {
				return true;
			}
		}, 
		Moore {
			@Override
			public boolean isSmallWorld() {
				return false;
			}
		};
		public abstract boolean isSmallWorld();
	}
	public boolean isDirected;
	public TopologyAlgorithm algorithm;
	public boolean rewireOnlySelectedCells;
	public TopologySpecification(boolean isDirected, TopologyAlgorithm algorithm) {
		this.isDirected = isDirected;
		this.algorithm = algorithm;
	}
	public INeighborTopology createTopology(float smallWorldRewiringProbability) {
		INeighborTopology topology;
		switch(algorithm) {
			case WattsStrogatz:
				topology = isDirected ? 
						new DirectedWattsStrogatzTopology(smallWorldRewiringProbability, rewireOnlySelectedCells) 
						: new WattsStrogatzTopology(smallWorldRewiringProbability, rewireOnlySelectedCells);
				break;
			case EdgeSwap:
				topology = isDirected ? 
						new DirectedEdgeSwapTopology(smallWorldRewiringProbability, rewireOnlySelectedCells) 
						: new EdgeSwapTopology(smallWorldRewiringProbability, rewireOnlySelectedCells);
				break;
			case Moore:
				topology = isDirected ?
						new DirectedMooreTopology() 
						: new UndirectedMooreTopology();
				break;
			default:
				throw new RuntimeException("Didn't recognize the topology algorithm to be used.");
		}
		return topology;
	}
}