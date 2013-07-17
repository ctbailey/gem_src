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

package gem.talk_to_outside_world;

/*
import static lap.ca.AutomatonGlobal.*;

import java.io.*;
import java.util.*;
*/

public class Memory /*implements AutomatonSerializable*/ {
/*
	int counter = 0;
	public int checkResourcesInterval = 100;
		// check whether to dump playback lists in RAM to disk every 100 iterations
	long maxAvailableMemory;
	double threshold;
	final double THRESHOLD_RATIO = 0.50;
	public boolean isTransitioning = false;
	int lastUsedID = -100; // the first playback list is always assigned -100
	ArrayList<File> playbackListMap;
	
	static final long serialVersionUID = 5;
	
	public Memory() {
	
		maxAvailableMemory = Runtime.getRuntime().maxMemory();
		threshold = maxAvailableMemory * THRESHOLD_RATIO;
		playbackListMap = new ArrayList<File>();
		
		// By default, the playback list map has one file in it; the one for the playback list currently in memory
		try{
			File file = File.createTempFile("" + -100, ".laptemp");
			file.deleteOnExit();
			playbackListMap.add(file);
		}catch (Exception ex) {
			ex.printStackTrace();
		}
		
	}

	public boolean count() {

		// Returns true if checkResources() dumps
		// the playback list to disk

		counter++;

		if (counter >= checkResourcesInterval) {

			counter = 0;
			if (checkResources()) {

				return true;

			}

		}

		return false;

	}

	void refreshMaxAvailableMemory() {
		
		maxAvailableMemory = Runtime.getRuntime().maxMemory();
		threshold = maxAvailableMemory * THRESHOLD_RATIO;
		
	}
	
	public synchronized boolean checkResources() {

		// Returns true if the playback list ends up getting dumped
		// to disk
		
		counter = 0;

		long usedMemory = maxAvailableMemory - Runtime.getRuntime().freeMemory();

		if (	usedMemory >= threshold 
				&& !currentPlaybackListHasBeenDumped()) {
			
			isTransitioning = true;
			
			LinkedList<int[][]> newList = new LinkedList<int[][]>();

			int[][] currentIDArray = {{currentID()}};
			newList.addFirst(currentIDArray); 	// this ID refers to the playback
											// list that is about to be dumped
			newList.addLast(playback.playbackList.removeLast());

			int[][] unusedIDArray = {{getUnusedID()}}; // refers to the new playback list
			playback.playbackList.add(unusedIDArray);

			writeCurrentPlaybackListToFile();
			
			userInterface.sliderMinLabel.setText(""
					+ automaton.generationNumber);
			
			playback.playbackList = newList;
			
			try{
				File file = File.createTempFile("" + currentID(), ".laptemp");
				file.deleteOnExit();
				playbackListMap.add(file);
			} catch(Exception ex) {
				ex.printStackTrace();
			}

			playback.prevSliderValue = -1;
			playback.playbackLocation = 1;
			userInterface.playbackSlider.setValue(2);
			userInterface.playbackSlider.setMaximum(4);
			userInterface.sliderMaxLabel.setText("" + automaton.generationNumber);

			isTransitioning = false;
			
			return true;

		}
		
		return false;
	}

	public boolean currentPlaybackListHasBeenDumped() {

		if (playback.playbackList.getLast()[0][0] < -99
				&& playback.playbackList.size() > 1) {

			return true;

		}

		return false;

	}

	public int getUnusedID() {

		if (lastUsedID == -100) {

			lastUsedID = -101;
			return -101;

		} else {

			lastUsedID -= 1;
			return lastUsedID;

		}

	}

	public synchronized void transitionTo(int[][] id) {

		isTransitioning = true;
		
		if (id[0][0] < currentID()) { // Remember, ID's are negative

			transitionForward(id[0][0]);

		} else {

			transitionBack(id[0][0]);

		}
		
		isTransitioning = false;
		
	}

	public synchronized void transitionForward(int id) {

		writeCurrentPlaybackListToFile();
		readFileToCurrentPlaybackList(id);

		playback.prevSliderValue = -1;

		/*
		 * Note: playback location represents the state BEFORE the new value is
		 * set on the playback slider. As such, here it should be one less than
		 * the argument of setValue().
		 */
	/*
		playback.playbackLocation = 0;
		userInterface.playbackSlider.setValue(1);
		userInterface.playbackSlider.setMaximum(playback.playbackList
				.size());

		userInterface.sliderMinLabel.setText(""
				+ automaton.generationNumber);

		if (playback.playbackList.getLast()[0][0] < -99) {

			int newMaxLabelValue = automaton.generationNumber
					+ (playback.playbackList.size() - 3);
			userInterface.sliderMaxLabel.setText(""
					+ newMaxLabelValue);

		} else {

			int newMaxLabelValue = automaton.generationNumber
					+ (playback.playbackList.size() - 2);
			userInterface.sliderMaxLabel.setText(""
					+ newMaxLabelValue);

		}

	}

	public synchronized void transitionBack(int id) {

		writeCurrentPlaybackListToFile();
		readFileToCurrentPlaybackList(id);

		if (playback.prevSliderValue 
				< userInterface.playbackSlider.getValue()) {

			playback.prevSliderValue = -1;

			/*
			 * Note: playback location represents the state BEFORE the new value
			 * is set on the playback slider. As such, here it should be one
			 * less than the argument of setValue().
			 */
	/*
			playback.playbackLocation = 0;
			userInterface.playbackSlider.setValue(1);
			userInterface.playbackSlider.setMaximum(playback.playbackList.size());

		} else {

			if (playback.playbackList.size() > userInterface.playbackSlider
					.getMaximum()) {

				playback.disablePlaybackSliderCalculations = true;
				userInterface.playbackSlider.setMaximum(playback.playbackList.size());

				playback.prevSliderValue = playback.playbackList.size(); // set prevSliderValue to correct value
				playback.disablePlaybackSliderCalculations = false;
				playback.playbackLocation = playback.playbackList.size() - 1;

				userInterface.playbackSlider.setValue(playback.playbackList.size() - 2);

			} else {

				playback.prevSliderValue = playback.playbackList.size();
				playback.playbackLocation = playback.playbackList
						.size() - 1;

				userInterface.playbackSlider
						.setValue(playback.playbackList.size() - 2);
				userInterface.playbackSlider
						.setMaximum(playback.playbackList.size());

			}

		}

		if (playback.playbackList.getFirst()[0][0] < -99) { // if the first elt. in this playback list is an ID to another playback list

			int newMinLabelValue = automaton.generationNumber 
					- (playback.playbackList.size() - 3);
			userInterface.sliderMinLabel.setText("" + newMinLabelValue);

		} else {

			int newMinLabelValue = automaton.generationNumber
					- (playback.playbackList.size() - 2);
			userInterface.sliderMinLabel.setText(""
					+ newMinLabelValue);

		}

		userInterface.sliderMaxLabel.setText(""
				+ automaton.generationNumber);

	}
	
	public void writeCurrentPlaybackListToFile() {

		int mapIndexOfCurrentID = idToMapIndex(currentID());
		File file = playbackListMap.get(mapIndexOfCurrentID);
		
		try{
			
			FileOutputStream fileOut = new FileOutputStream(file);
			ObjectOutputStream objectOut = new ObjectOutputStream(fileOut);
			
			objectOut.writeObject(playback.playbackList);
		
			objectOut.close();
			
		} catch(Exception ex) {
			ex.printStackTrace();
		}

	}
	
	public void readFileToCurrentPlaybackList(int id) {
				
		int mapIndexOfID = idToMapIndex(id);
		File file = playbackListMap.get(mapIndexOfID);
		
		try {
			
			FileInputStream fileIn = new FileInputStream(file);
			ObjectInputStream objectIn = new ObjectInputStream(fileIn);
			
			@SuppressWarnings("unchecked")
			LinkedList<int[][]> loadedList = (LinkedList<int[][]>) objectIn.readObject();
			
			playback.playbackList = loadedList;
			
			objectIn.close();
			
		} catch(Exception ex) {
			
			ex.printStackTrace();
			
		}
		
	}

	public int idToMapIndex(int id) {

		return Math.abs(id) - 100;

	}

	public void removeBeyondCurrentPlaybackList() {

		int index = idToMapIndex(currentID());
		lastUsedID = currentID();
		
		int objectsToBeRemoved = playbackListMap.size()
				- (index + 1);

		counter = playback.playbackLocation - 1;
		
		for (int i = 0; i < objectsToBeRemoved; i++) {

			playbackListMap.remove(playbackListMap.size() - 1);

		}

	}

	public int currentID() {

		if(beyondFirstList()) {

			int idOfPrevious = playback.playbackList.getFirst()[0][0];
			int currentID = idOfPrevious - 1;
			return currentID;

		} else {

			return -100;

		}

	}

	public boolean beyondFirstList() {

		try {

			if (playback.playbackList.getFirst()[0][0] < -99) {

				return true;

			} else {

				return false;

			}

		} catch (IndexOutOfBoundsException ex) {

			return false;

		}

	}

	public void clearHistory() {

		for(int i = 0; i < playbackListMap.size(); i++) {
			
			playbackListMap.get(i).delete();
			
		}
		
		lastUsedID = -100;

		playbackListMap.clear();
		
		try{
		File file = File.createTempFile("-100",".laptemp");
		file.deleteOnExit();
		playbackListMap.add(file);
		} catch(Exception ex) {
			ex.printStackTrace();
		}
		
		counter = 0;
		
	}

	public void save(ObjectOutputStream output) {
		
		final int NUM_OF_OTHER_OBJECTS = 3;
		
		int savedMemorySize = NUM_OF_OTHER_OBJECTS + playbackListMap.size();
		Object[] savedMemory = new Object[savedMemorySize];
		
		savedMemory[0] = Integer.valueOf(counter);
		savedMemory[1] = Integer.valueOf(checkResourcesInterval);
		savedMemory[2] = playbackListMap;
		
		for(int i = 0; i < playbackListMap.size() - 1; i++) {
			
			File currentFile = playbackListMap.get(i);
			
			try{
				
				FileInputStream fileInputStream = new FileInputStream(currentFile);
				ObjectInputStream input = new ObjectInputStream(fileInputStream);
				savedMemory[NUM_OF_OTHER_OBJECTS + i] = input.readObject();
				input.close();
				
			} catch(Exception ex) {
				ex.printStackTrace();
			}
		}
		
		try {
			output.writeObject(savedMemory);
		} catch(Exception ex) {
			ex.printStackTrace();
		}
	}
	
	@SuppressWarnings("unchecked")
	public void load(ObjectInputStream input) {
		
		try{
			
			Object[] loadedMemory = (Object[]) input.readObject();
			
			Integer loadedCounter = (Integer) loadedMemory[0];
			Integer loadedCheckResourcesInterval = (Integer) loadedMemory[1];
			ArrayList<File> loadedPlaybackListMap = (ArrayList<File>) loadedMemory[2];
			
			int numberOfFilesInLoadedMemory = loadedMemory.length - 3;
			
			for(int i = 0; i < numberOfFilesInLoadedMemory; i++) {
				
				FileOutputStream fileOutputStream = new FileOutputStream(loadedPlaybackListMap.get(i));
				ObjectOutputStream output = new ObjectOutputStream(fileOutputStream);
				
				output.writeObject(loadedMemory[i + 3]);
				
				output.close();
				
			}
			
			counter = loadedCounter.intValue();
			checkResourcesInterval = loadedCheckResourcesInterval.intValue();
			playbackListMap = loadedPlaybackListMap;
			
		} catch(Exception ex) {
			ex.printStackTrace();
		}
	}
*/	
}
