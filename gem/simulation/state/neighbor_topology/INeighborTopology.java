package gem.simulation.state.neighbor_topology;

//import java.util.ArrayList;
//import java.util.List;

import gem.simulation.board.BoardDimensions;

public interface INeighborTopology {
	public INeighborGraph createGraphWithThisTopology(BoardDimensions dimensions);
	public float getRewiringProbability();
}