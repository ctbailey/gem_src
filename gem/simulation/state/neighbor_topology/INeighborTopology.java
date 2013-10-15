package gem.simulation.state.neighbor_topology;

//import java.util.ArrayList;
//import java.util.List;

import gem.simulation.board.BoardDimensions;

public interface INeighborTopology {
	public static final boolean REWIRE_ONLY_SELECTED_CELLS = false;
	public INeighborGraph createGraphWithThisTopology(BoardDimensions dimensions);
}