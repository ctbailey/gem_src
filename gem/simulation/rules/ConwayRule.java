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

package gem.simulation.rules;

import gem.simulation.board.InvalidCellStateException;
import gem.simulation.state.AbstractConwayState;
import gem.simulation.state.AbstractConwayCell;
import gem.simulation.state.ICell;
import gem.simulation.state.ICell.CellState;
import gem.ui.UserDidNotConfirmException;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.swing.*;

public class ConwayRule extends AbstractRuleSet {
	private static final long serialVersionUID = 1L;
	private final Set<Integer> birthConditions;
	private final Set<Integer> lifeConditions;
	private static final int MAX_LIFE_CONDITIONS_USER_CAN_CHOOSE = 8;
	private static final int MAX_BIRTH_CONDITIONS_USER_CAN_CHOOSE = 8;
	
	public ConwayRule(Set<Integer> birthConditions, Set<Integer> lifeConditions) {
		if(birthConditions == null
				|| lifeConditions == null) {
			throw new NullPointerException("GameOfLifeConditions cannot take null birthConditions or lifeConditions");
		} else {
			this.birthConditions = birthConditions;
			this.lifeConditions = lifeConditions;
		}
	}
	
	public AbstractConwayState calculateNextState(AbstractConwayState currentState) {
		AbstractConwayCell[][] newCells = new AbstractConwayCell[currentState.getWidth()][currentState.getHeight()];
		for(int x = 0; x < currentState.getWidth(); x++) {
			for(int y = 0; y < currentState.getHeight(); y++) {
				newCells[x][y] = calculateNextCell(x,y, currentState);
			}
		}
		return currentState.getNextIteration(newCells);
	}
	private synchronized AbstractConwayCell calculateNextCell(int x, int y, AbstractConwayState currentState) {
		int livingNeighbors = currentState.getNumberOfNeighborsInState(x, y, CellState.ALIVE);
		ICell currentCell = currentState.getCell(x,y);
		CellState newCellState = calculateNextCellState(currentCell, livingNeighbors);
		return (AbstractConwayCell) currentCell.getModifiedCopy(newCellState);
	}
	private synchronized CellState calculateNextCellState(ICell cell, int livingNeighbors) {
		CellState state = cell.getState();
		switch(state) {
			case ALIVE:
				return nextStateForLivingCell(livingNeighbors);
			case DEAD:
				return nextStateForDeadCell(livingNeighbors);
			case IMPASSABLE:
				return CellState.IMPASSABLE;
			default:
				throw new InvalidCellStateException(cell.getClass(), state); 
		}
	}
	private CellState nextStateForLivingCell(int livingNeighbors) {
		if(lifeConditions.contains(livingNeighbors)) {
			return CellState.ALIVE;
		} else {
			return CellState.DEAD;
		}
	}
	private CellState nextStateForDeadCell(int livingNeighbors) {
		if(birthConditions.contains(livingNeighbors)) {
			return CellState.ALIVE;
		} else {
			return CellState.DEAD;
		}
	}

	public String visualizeForUser() {
		return visualize(birthConditions, "Birth conditions") + " " + visualize(lifeConditions, "Life conditions");
	}
	private String visualize(Set<Integer> conditions, String name) {
		StringBuilder sb = new StringBuilder(name + ": {");
		Iterator<Integer> iterator = conditions.iterator();
		while(iterator.hasNext()) {
			sb.append(iterator.next());
			if(iterator.hasNext()) {
				sb.append(", ");
			}
		}
		sb.append("}");
		return sb.toString();
	}
	
	public static ConwayRule conwaysGameOfLife() {
		Set<Integer> birthConditions = new HashSet<Integer>();
		birthConditions.add(3);
		Set<Integer> lifeConditions = new HashSet<Integer>();
		lifeConditions.add(2);
		lifeConditions.add(3);
		return new ConwayRule(birthConditions, lifeConditions);
	}
	public static ConwayRule baileySet() {
		Set<Integer> birthConditions = new HashSet<Integer>();
		birthConditions.add(2);
		birthConditions.add(3);
		birthConditions.add(4);
		Set<Integer> lifeConditions = new HashSet<Integer>();
		lifeConditions.add(5);
		lifeConditions.add(6);
		lifeConditions.add(7);
		lifeConditions.add(8);
		return new ConwayRule(birthConditions, lifeConditions);
	}
	
	@Override
	public ConwayRule getRuleFromUserInput(JFrame mainFrame) throws UserDidNotConfirmException {
		UserInputGrabber grabber = new UserInputGrabber(mainFrame);
		return grabber.getInput();
	}
	private class UserInputGrabber {
		private JFrame mainFrame;
		private Set<Integer> tempBirthConditions;
		private Set<Integer> tempLifeConditions;
		
		UserInputGrabber(JFrame mainFrame) {
			this.mainFrame = mainFrame;
			tempBirthConditions = new HashSet<Integer>();
			tempBirthConditions.addAll(birthConditions);
			tempLifeConditions = new HashSet<Integer>();
			tempLifeConditions.addAll(lifeConditions);
		}
		ConwayRule getInput() throws UserDidNotConfirmException {
			JPanel rulePanel = new JPanel();
			rulePanel.setLayout(new BoxLayout(rulePanel, BoxLayout.X_AXIS));
			
			JPanel birthConditionsPanel = new JPanel();
			birthConditionsPanel.setLayout(new BoxLayout(birthConditionsPanel, BoxLayout.Y_AXIS));
			birthConditionsPanel.add(new JLabel("Birth conditions"));
			for(int i = 0; i < MAX_BIRTH_CONDITIONS_USER_CAN_CHOOSE + 1; i++) {
				JCheckBox cb = new JCheckBox("" + i, birthConditions.contains(i));
				cb.addItemListener(new BirthConditionsCheckBoxListener());
				
				birthConditionsPanel.add(cb);
			}
			birthConditionsPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
			
			JPanel lifeConditionsPanel = new JPanel();
			lifeConditionsPanel.setLayout(new BoxLayout(lifeConditionsPanel, BoxLayout.Y_AXIS));
			lifeConditionsPanel.add(new JLabel("Life conditions"));
			for(int i = 0; i < MAX_LIFE_CONDITIONS_USER_CAN_CHOOSE + 1; i++) {
				JCheckBox cb = new JCheckBox("" + i, lifeConditions.contains(i));
				cb.addItemListener(new LifeConditionsCheckBoxListener());
				lifeConditionsPanel.add(cb);
			}
			
			rulePanel.add(birthConditionsPanel);
			rulePanel.add(lifeConditionsPanel);
			
			int userAction = JOptionPane.showConfirmDialog(mainFrame, rulePanel, "Input Rule", JOptionPane.OK_CANCEL_OPTION);
			if(userAction == JOptionPane.OK_OPTION) {
				return createConwayConditions();
			} else {
				throw new UserDidNotConfirmException();
			}
		}
		
		private ConwayRule createConwayConditions() {
			return new ConwayRule(tempBirthConditions, tempLifeConditions);
		}
		
		class BirthConditionsCheckBoxListener implements ItemListener {
			public void itemStateChanged(ItemEvent ev) {
				JCheckBox checkBox = (JCheckBox)ev.getSource();
				int checkBoxInt = Integer.parseInt(checkBox.getText());
				if(checkBox.isSelected()) {
					tempBirthConditions.add(checkBoxInt);
				} else {
					tempBirthConditions.remove(checkBoxInt);
				}
			}
		}
		class LifeConditionsCheckBoxListener implements ItemListener {
			public void itemStateChanged(ItemEvent ev) {
				JCheckBox checkBox = (JCheckBox)ev.getSource();
				int checkBoxInt = Integer.parseInt(checkBox.getText());
				if(checkBox.isSelected()) {
					tempLifeConditions.add(checkBoxInt);
				} else {
					tempLifeConditions.remove(checkBoxInt);
				}
			}
		}
	}
	
}