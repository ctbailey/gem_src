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

package gem.ui.board_panel.board_image;


import gem.talk_to_outside_world.AutomatonSerializable;
import gem.ui.IMenuItemProvider;

import java.awt.Image;
import java.io.Serializable;
import java.util.*;


public abstract class AbstractBoardImageSource implements IBoardImageSource, IMenuItemProvider, Serializable, AutomatonSerializable {
	private static final long serialVersionUID = 1L;
	private List<IBoardImageChangedListener> boardImageChangedListeners;
	private Image boardImage;
	
	public AbstractBoardImageSource() {
		boardImageChangedListeners = new ArrayList<IBoardImageChangedListener>();
	}
	
	public void isReplacing(AbstractBoardImageSource oldImageSource) {
		// Do nothing
	}
	public void isBeingReplacedBy(AbstractBoardImageSource newImageSource) {
		for(IBoardImageChangedListener listener : boardImageChangedListeners) {
			newImageSource.addBoardImageChangedListener(listener);
		}
		boardImageChangedListeners.clear();
	}
	protected void setBoardImage(Image newImage) {
		boardImage = newImage;
		notifyBoardImageChangedListeners(boardImage);
	}
	protected abstract Image calculateImage();
	
	@Override
	public Image getCurrentBoardImage() {
		return boardImage;
	}

	public void addBoardImageChangedListener(IBoardImageChangedListener listener) {
		boardImageChangedListeners.add(listener);
	}
	public void removeBoardImageChangedListener(IBoardImageChangedListener listener) {
		boardImageChangedListeners.remove(listener);
	}
	protected void notifyBoardImageChangedListeners(Image newImage) {
		for(IBoardImageChangedListener listener : boardImageChangedListeners) {
			listener.boardImageChanged(newImage);
		}
	}
}
