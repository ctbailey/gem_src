package gem.simulation.state.neighbor_topology;

import java.io.Serializable;

import org.jgrapht.Graph;
import org.jgrapht.alg.NeighborIndex;

class SerializableNeighborIndexParent<V,E> extends NeighborIndex<V,E> {
	public SerializableNeighborIndexParent() {
		super(null);
	}
	public SerializableNeighborIndexParent(Graph<V,E> g) {
		super(g);
	}
}

public class SerializableNeighborIndex<V,E> extends SerializableNeighborIndexParent<V,E> implements Serializable {
	private static final long serialVersionUID = 1L;
	public SerializableNeighborIndex(Graph<V,E> g) {
		super(g);
	}
}