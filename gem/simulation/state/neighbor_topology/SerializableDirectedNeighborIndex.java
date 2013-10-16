package gem.simulation.state.neighbor_topology;

import java.io.Serializable;

import org.jgrapht.DirectedGraph;
import org.jgrapht.alg.DirectedNeighborIndex;
class SerializableDirectedNeighborIndexParent<V,E> extends DirectedNeighborIndex<V,E> {
	public SerializableDirectedNeighborIndexParent() {
		super(null);
	}
	public SerializableDirectedNeighborIndexParent(DirectedGraph<V,E> g) {
		super(g);
	}
}

public class SerializableDirectedNeighborIndex<V,E> extends SerializableDirectedNeighborIndexParent<V,E> implements Serializable {
	private static final long serialVersionUID = 1L;
	public SerializableDirectedNeighborIndex(DirectedGraph<V,E> g) {
		super(g);
	}
}