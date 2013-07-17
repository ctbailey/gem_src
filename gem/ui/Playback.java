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

/*
import static lap.ca.AutomatonGlobal.*;

import java.io.*;
import java.util.LinkedList;

import lap.ca.AutomatonGlobal;
import lap.ca.talk_to_outside_world.AutomatonSerializable;
*/

public class Playback /*implements Runnable, AutomatonSerializable*/ {

	/*
	boolean playForward = true;
	public boolean disablePlaybackSliderCalculations = false;
	int sliderMax;
	int newMax;
	
	static final long serialVersionUID = 5;
	
	Thread playThread; // Used to play back the sequence of board states

	public LinkedList<int[][]> playbackList;
	
	public int playbackLocation = 0;
	int sleepTime = 120; // int which determines how long (in milliseconds) the automaton waits between iterations

	public boolean shouldPlay = false; // Boolean flag which declares whether the automaton should be playing back board states (true) or not (false)
	public int prevSliderValue;

	public Playback() {
		
		playbackList = new LinkedList<int[][]>();
		
	}
	
	public void setDefaults() {
		
		// By default, the playback list has one array in it: the array currently displayed on the board.
		playbackList.add(boardPanel.displayArray);
		
	}
	
	public void run() {
		
		play();
		
	}
	
	public synchronized void play() {
		
		while(shouldPlay) {

			if(playForward) {
					
				iterateForward();

			} else {
					
				iterateBackward();
					
			}
					
			try{
				Thread.sleep(sleepTime);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}
	
	public synchronized void iterateForward() {
		
		if(playbackLocation < userInterface.playbackSlider.getMaximum()) {

			userInterface.playbackSlider.setValue(playbackLocation + 1);
			
		} else {
			
			playThread = null;
			shouldPlay = false;
			userInterface.refreshPlayButtons();
				
		}
			
	}
	
	public synchronized void iterateBackward() {
		
		if(automaton.generationNumber > 0) {
			
			userInterface.playbackSlider.setValue(playbackLocation - 1);
			
		} else {
			
			playThread = null;
			shouldPlay = false;
			userInterface.refreshPlayButtons();
			
		}
		
	}
	
	public void addBoardState(int[][] array){
		
		playbackList.add(array);
		
		sliderMax = userInterface.playbackSlider.getMaximum();
		sliderMax = sliderMax + 1;
		userInterface.playbackSlider.setMaximum(sliderMax);
		userInterface.sliderMaxLabel.setText("" + automaton.generationNumber);
		userInterface.sliderMaxLabel.setText("" + automaton.generationNumber);
		
	}
	
	public void removeBeyondCurrentGeneration() {
		
		for(int i = playbackList.size() - 1; i > playbackLocation; i--) {
			
			playbackList.removeLast();
			
		}
		
		AutomatonVars.memory.removeBeyondCurrentPlaybackList();
		
		automaton.worldArray = boardPanel.displayArray;
		
		newMax = playbackLocation + 1;
		userInterface.playbackSlider.setMaximum(newMax);
		userInterface.sliderMaxLabel.setText("" + automaton.generationNumber);
		
	}
	
	public void clearHistory() {
		
		AutomatonVars.automaton.clearDisplayArray();
		AutomatonVars.automaton.clearWorldArray();
		playbackList.clear();
		
		automaton.worldArray = boardPanel.displayArray;
		
		playbackList.add(0,boardPanel.displayArray);
		
		boardPanel.refreshBoardImage();
		
		userInterface.playbackSlider.setValue(0);
		playbackLocation = 0;
		userInterface.playbackSlider.setMaximum(1);
		userInterface.sliderMinLabel.setText("0");
		
		automaton.generationNumber = 0;
		userInterface.sliderMaxLabel.setText("" + automaton.generationNumber);
		userInterface.refreshGenerationNumberLabel();
		
	}
	
	public void save(ObjectOutputStream output) {
		
		Object[] savedPlayback = new Object[7];
		
		savedPlayback[0] = Integer.valueOf(userInterface.playbackSlider.getMaximum());
		savedPlayback[1] = playbackList;
		savedPlayback[2] = Integer.valueOf(playbackLocation);
		savedPlayback[3] = Integer.valueOf(sleepTime);
		savedPlayback[4] = Integer.valueOf(prevSliderValue);
		savedPlayback[5] = Integer.decode(userInterface.sliderMaxLabel.getText());
		savedPlayback[6] = Integer.decode(userInterface.sliderMinLabel.getText());
		
		try{
			output.writeObject(savedPlayback);
		} catch(Exception ex) {
			ex.printStackTrace();
		}
		
	}
	
	@SuppressWarnings("unchecked")
	public void load(ObjectInputStream input) {
		
		try {
			
			Object[] loadedPlayback = (Object[]) input.readObject();
			
			Integer loadedSliderMax = (Integer) loadedPlayback[0];
			LinkedList<int[][]> loadedPlaybackList = (LinkedList<int[][]>) loadedPlayback[1];
			Integer loadedPlaybackLocation = (Integer) loadedPlayback[2];
			Integer loadedSleepTime = (Integer) loadedPlayback[3];
			Integer loadedPrevSliderValue = (Integer) loadedPlayback[4];
			Integer loadedSliderMaxLabel = (Integer) loadedPlayback[5];
			Integer loadedSliderMinLabel = (Integer) loadedPlayback[6];
			
			sliderMax = loadedSliderMax.intValue();
			playbackList = loadedPlaybackList;
			playbackLocation = loadedPlaybackLocation.intValue();
			sleepTime = loadedSleepTime.intValue();
			prevSliderValue = loadedPrevSliderValue.intValue();
			userInterface.sliderMaxLabel.setText("" + loadedSliderMaxLabel.intValue());
			userInterface.sliderMinLabel.setText("" + loadedSliderMinLabel.intValue());
			
			disablePlaybackSliderCalculations = true;
			userInterface.playbackSlider.setMaximum(sliderMax);
			userInterface.playbackSlider.setValue(playbackLocation);
			disablePlaybackSliderCalculations = false;
			
		} catch(Exception ex) {
			ex.printStackTrace();
		}
		
	}
	*/
}
