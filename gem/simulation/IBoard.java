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

package gem.simulation;
import gem.InvalidConfigurationException;
import gem.simulation.AbstractBoard.GameConfigurationKey;
import gem.simulation.ICell.CellState;

import java.io.File;

import javax.swing.JFrame;


public interface IBoard {
	public void iterate();
	public boolean tryToIterate();
	public boolean tryToGoBack();
	public void clearCurrentStateAndHistory();
	public void clearCellTypeFromCurrentState(CellState state);
	public abstract boolean getValueForConfigurationKey(GameConfigurationKey key);
	public abstract void setValueForConfigurationKey(GameConfigurationKey key, boolean value) throws InvalidConfigurationException;
	
	public IState getCurrentState();
	public int getNumberOfIterations();
	public void updateRulesFromUserInput(JFrame mainFrame);
	public AbstractRuleSet getRules();
	
	public void copyStateToClipboard();
	public void pasteFromClipboard();
	public void loadCurrentStateFromFile(File file);

	public void resizeBasedOnUserInput();
	public void resize(BoardDimensions newDimensions);
	
	public void randomizeBoard(IRandomNumberSource randomNumberSource, double threshold, CellState stateToRandomize);
	
	public void addBoardStateChangedListener(IBoardStateChangedListener listener);
	public void removeBoardStateChangedListener(IBoardStateChangedListener listener);
	public void addRulesChangedListener(IRulesChangedListener listener);
	public void removeRulesChangedListener(IRulesChangedListener listener);
	public void addBoardSizeChangedListener(IBoardSizeChangedListener listener);
	public void removeBoardSizeChangedListener(IBoardSizeChangedListener listener);
	public void addBoardWillIterateListener(IBoardWillIterateListener listener);
	public void removeBoardWillIterateListener(IBoardWillIterateListener listener);
	public void addBoardDidIterateListener(IBoardDidIterateListener listener);
	public void removeBoardDidIterateListener(IBoardDidIterateListener listener);
}
