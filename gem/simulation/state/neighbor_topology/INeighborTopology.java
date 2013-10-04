package gem.simulation.state.neighbor_topology;

//import java.util.ArrayList;
//import java.util.List;

import gem.simulation.board.BoardDimensions;

public interface INeighborTopology {
	/*
	public enum GraphConstructionAlgorithm {
		UNDIRECTED_MOORE {
			@Override
			public INeighborTopology getTopologyFromUser() {
				// TODO Auto-generated method stub
				return null;
			}
		}, DIRECTED_MOORE,
		// Small world
		WATTS_STROGATZ_SMALL_WORLD, DIRECTED_WATTS_STROGATZ, EDGE_SWAP_SMALL_WORLD;
		
		public abstract INeighborTopology getTopologyFromUser();
		public GraphConstructionAlgorithm[] getAll() {
			GraphConstructionAlgorithm[] algorithms = { UNDIRECTED_MOORE, DIRECTED_MOORE, WATTS_STROGATZ_SMALL_WORLD, DIRECTED_WATTS_STROGATZ, EDGE_SWAP_SMALL_WORLD };
			return algorithms;
		}
	}*/
	public INeighborGraph createGraphWithThisTopology(BoardDimensions dimensions);
}