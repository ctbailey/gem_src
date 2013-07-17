/*
	 * Copyright 2013, C. Thomas Bailey
	 * 
	 * This file is part of GEM: The Geographic Modeler.
	 *
     * GEM is free software: you can redistribute it and/or modify
     * it under the terms of the GNU General Public License as published by
     * the Free Software Foundation, either version 3 of the License, or
     * (at your option) any later version.
     * 
     * GEM is distributed in the hope that it will be useful,
     * but WITHOUT ANY WARRANTY; without even the implied warranty of
     * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     * GNU General Public License for more details.
     * 
     * You should have received a copy of the GNU General Public License
     * along with GEM.  If not, see <http://www.gnu.org/licenses/>.
	 */

package gem.ui;

import gem.simulation.IBoardStateChangedListener;
import gem.simulation.IState;

import java.awt.Image;
import java.awt.image.MemoryImageSource;
import java.util.ArrayList;
import java.util.List;

import static gem.AutomatonGlobal.*;

public abstract class AbstractBoardImageSource implements IBoardImageSource, IBoardStateChangedListener, IShouldShowMapListener {
	private IState mostRecentState;
	private List<IBoardImageChangedListener> boardImageChangedListeners;
	private Image boardImage;
	private boolean showMap = false;
	
	public AbstractBoardImageSource() {
		boardImageChangedListeners = new ArrayList<IBoardImageChangedListener>();
		mostRecentState = automaton.getBoard().getCurrentState();
	}
	
	public void isReplacing(AbstractBoardImageSource oldImageSource) {
		automaton.getBoard().addBoardStateChangedListener(this);
	}
	public void isBeingReplacedBy(AbstractBoardImageSource newImageSource) {
		for(IBoardImageChangedListener listener : boardImageChangedListeners) {
			newImageSource.addBoardImageChangedListener(listener);
		}
		boardImageChangedListeners.clear();
		automaton.getBoard().removeBoardStateChangedListener(this);
	}
	
	public void pullLatestBoardStateAndRefreshBoardImage() {
		updateMostRecentState();
		refreshBoardImage();
	}
	public void refreshBoardImage() {
		setBoardImage(calculateImage(mostRecentState));
	}
	private void setBoardImage(Image newImage) {
		boardImage = newImage;
		notifyBoardImageChangedListeners(boardImage);
	}
	
	@Override
	public Image getCurrentBoardImage() {
		return boardImage;
	}
	
	private Image calculateImage(IState boardState) {
		int[] pixels = calculatePixels(boardState, showMap);
		return createImageFromPixels(pixels, boardState.getWidth(), boardState.getHeight());
	}
	private Image createImageFromPixels(int[] pixels, int boardWidth, int boardHeight) {
		MemoryImageSource imageProducer = new MemoryImageSource(boardWidth, boardHeight, pixels, 0, boardWidth);
		Image image = userInterface.boardPanel.createImage(imageProducer);
		return image;
	}
	protected abstract int[] calculatePixels(IState newState, boolean showMap);
	
	private void updateMostRecentState() {
		mostRecentState = automaton.getBoard().getCurrentState();
	}

	public void addBoardImageChangedListener(IBoardImageChangedListener listener) {
		boardImageChangedListeners.add(listener);
	}
	public void removeBoardImageChangedListener(IBoardImageChangedListener listener) {
		boardImageChangedListeners.remove(listener);
	}
	private void notifyBoardImageChangedListeners(Image newImage) {
		for(IBoardImageChangedListener listener : boardImageChangedListeners) {
			listener.boardImageChanged(newImage);
		}
	}

	@Override
	public void boardStateChanged(IState newState, int newNumberOfIterations) {
		mostRecentState = newState;
		refreshBoardImage();
	}
	@Override
	public void shouldShowMapUpdated(boolean showMap) {
		this.showMap = showMap;
		refreshBoardImage();
	}
}
