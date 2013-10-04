package gem.ui.board_image;

import gem.Global;

import java.awt.Point;

public class IncomingInfluenceCellHighlighter extends CellHighlighter {
	private static final float DEFAULT_NORMALIZED_PREFERRED_OPACITY = 0.5f;
	private static final int DEFAULT_RED = 0;
	private static final int DEFAULT_GREEN = 0;
	private static final int DEFAULT_BLUE = 255;
	
	public IncomingInfluenceCellHighlighter() {
		super(DEFAULT_NORMALIZED_PREFERRED_OPACITY, DEFAULT_RED, DEFAULT_GREEN, DEFAULT_BLUE);
	}
	
	@Override
	protected Point[] getPointsOfInterest(int x, int y) {
		return Global.simulator.getBoard().getCurrentState().getPointsThatInfluenceCellAt(x, y);
	}
}
