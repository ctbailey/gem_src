package gem.simulation.state.neighbor_topology;

//import gem.Debug;
//import gem.metrics.Metrics;
import gem.simulation.board.BoardDimensions;
import gem.ui.IMenuItemProvider;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractTopologyManager implements IMenuItemProvider {
	private List<INeighborTopologyChangedListener> topologyChangedListeners = new ArrayList<INeighborTopologyChangedListener>();
	private static final INeighborTopology DEFAULT_TOPOLOGY = new UndirectedMooreTopology(); 
	private INeighborTopology currentTopology = DEFAULT_TOPOLOGY;
	protected void setTopology(INeighborTopology newTopology) {
		currentTopology = newTopology;
		notifyNeighborTopologyChangedListeners(newTopology);
	}
	protected INeighborTopology getCurrentTopology() {
		return currentTopology;
	}
	public INeighborGraph createNeighborGraphWithCurrentTopology(BoardDimensions dimensions) {
		INeighborGraph g = currentTopology.createGraphWithThisTopology(dimensions);
		
//		Debug.printLine("Calculating average path length.");
//		double avgPathLength = Metrics.calculateAveragePathLength(g);
//		Debug.printLine("Average path length: " + avgPathLength);
//		
//		Debug.printLine("Calculating clustering coefficient.");
//		double avgClusteringCoef = Metrics.calculateAverageClusteringCoefficient(g, dimensions);
//		Debug.printLine("Average clustering coefficient: " + avgClusteringCoef);
		return g;
	}
	// Event methods
	public void addNeighborTopologyChangedListener(INeighborTopologyChangedListener listener) {
		topologyChangedListeners.add(listener);
	}
	public boolean removeNeighborTopologyChangedListener(INeighborTopologyChangedListener listener) {
		return topologyChangedListeners.remove(listener);
	}
	private void notifyNeighborTopologyChangedListeners(INeighborTopology newTopology) {
		for(INeighborTopologyChangedListener listener : topologyChangedListeners) {
			listener.neighborTopologyChanged(newTopology);
		}
	}
}