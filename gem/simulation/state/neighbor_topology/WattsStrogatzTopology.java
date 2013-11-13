package gem.simulation.state.neighbor_topology;

import java.awt.Point;
import java.util.Set;

import org.jgrapht.graph.DefaultWeightedEdge;

import gem.Global;
import gem.simulation.Utility;
import gem.simulation.board.BoardDimensions;
import gem.simulation.state.ICell;

public class WattsStrogatzTopology extends SmallWorldTopology {
	private final float rewiringProbability;
	public WattsStrogatzTopology(float rewiringProbability, boolean rewireOnlySelectedCells) {
		super(rewireOnlySelectedCells);
		this.rewiringProbability = rewiringProbability;
	}
	
	@Override
	protected INeighborGraph createRegularGraph(BoardDimensions dimensions) {
		return new UndirectedMooreTopology().createGraphWithThisTopology(dimensions);
	}

	@Override
	protected void rewireToSmallWorld(BoardDimensions dimensions,
			INeighborGraph graph) {
		Set<DefaultWeightedEdge> edges = graph.edgeSet();
		
		DefaultWeightedEdge[] edgeArray = new DefaultWeightedEdge[edges.size()];
		edges.toArray(edgeArray);
		
		for(int i = 0; i < edgeArray.length; i++) {
			Point source = graph.getEdgeSource(edgeArray[i]);
			Point target = graph.getEdgeTarget(edgeArray[i]);
			ICell sourceCell = Global.simulator.getBoard().getCurrentState().getCell(source.x, source.y);
			ICell targetCell = Global.simulator.getBoard().getCurrentState().getCell(target.x, target.y);
			if(sourceCell.isSelected() || targetCell.isSelected()
					|| !rewireOnlySelectedCells) {
				rewireEdge(edgeArray[i], dimensions, graph);
			}
		}
	}
	private void rewireEdge(DefaultWeightedEdge e, BoardDimensions dimensions, INeighborGraph graph) {
		if(Math.random() <= rewiringProbability) {
			Point newTarget = Utility.getRandomPointOnBoard(dimensions);
			Point existingSource = graph.getEdgeSource(e);
			while(!pointIsValidRewiringLocationForSmallWorldConnection(newTarget, existingSource, graph)) {
				// Select another random point until you get one that's valid
				newTarget = Utility.getRandomPointOnBoard(dimensions);
			}
			graph.removeEdge(e);
			graph.addEdge(existingSource, newTarget);
		}
	}
	private static boolean pointIsValidRewiringLocationForSmallWorldConnection(Point newTarget, Point oldSource, INeighborGraph g) {
		return !pointsHaveSameCoordinates(newTarget, oldSource)
				&& !g.containsEdge(oldSource, newTarget);
	}
	private static boolean pointsHaveSameCoordinates(Point p1, Point p2) {
		return p1.x == p2.x 
				&& p1.y == p2.y;
	}
	@Override
	public float getRewiringProbability() {
		return rewiringProbability;
	}
}
