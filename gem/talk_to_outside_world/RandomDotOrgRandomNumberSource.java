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

import gem.simulation.randomization.IRandomNumberSource;
import gem.simulation.randomization.NoRandomNumbersRemainingException;

import java.net.*;
import java.io.*;
import java.util.*;

public class RandomDotOrgRandomNumberSource implements IRandomNumberSource {	
	private static final int DEFAULT_CACHE_SIZE = 500;
	private static final int MINIMUM_RANDOM_NUMBER_VALUE = 0;
	private static final int MAXIMUM_RANDOM_NUMBER_VALUE = 100;
	private static final String QUOTA_URL = "http://www.random.org/quota/?format=plain";
	private static final String QUOTA_REACHED_EXPLANATION = 
			"This IP address has reached its quota for random.org." + "\n" + 
			"For more information, visit http://www.random.org/quota/" + "\n" +
			"If you'd still like to randomize, try changing the randomization mode to pseudo-random in the menu.";
	int cacheSize;
	Stack<Byte> randomNumbersCache;
	
	static final long serialVersionUID = 6;

	public RandomDotOrgRandomNumberSource() {
		this(DEFAULT_CACHE_SIZE);
	}
	public RandomDotOrgRandomNumberSource(int cacheSize) {
		this.cacheSize = cacheSize;
		randomNumbersCache = new Stack<Byte>();
	}
	
	public double getNextRandomNumber() throws NoRandomNumbersRemainingException {
		try {
			if(randomNumbersCache.empty()) {
				refillCache();
			}
			int randomInt = randomNumbersCache.pop();
			return scaleRandomInt(randomInt);
		} catch(RandomDotOrgQuotaReachedException ex) {
			throw new NoRandomNumbersRemainingException(QUOTA_REACHED_EXPLANATION);
		}
	}
	
	private double scaleRandomInt(int i) {
		double d = (double)i;
		double minD = (double)MINIMUM_RANDOM_NUMBER_VALUE;
		double maxD = (double)MAXIMUM_RANDOM_NUMBER_VALUE;
		return (d - minD)/maxD;
	}
	private void refillCache() throws RandomDotOrgQuotaReachedException {
		if(getRemainingQuota() > 0) {
			try {
				// Connect to random.org
				URL randomdotorg = new URL("http://www.random.org/integers/?num=" + cacheSize + "&min=" + MINIMUM_RANDOM_NUMBER_VALUE + "&max=" + MAXIMUM_RANDOM_NUMBER_VALUE + "&col=1&base=10&format=plain&rnd=new");
				URLConnection connection = randomdotorg.openConnection();
				connection.setDoInput(true);
				
				// Set up input stream
				InputStream inStream = connection.getInputStream();
				BufferedReader input = new BufferedReader(new InputStreamReader(inStream));
				
				// Loop through all data in the input stream,
				String numberAsString = "";
				while((numberAsString = input.readLine()) != null) {
					randomNumbersCache.push(Byte.parseByte(numberAsString));
				}
				// Close streams
				input.close();
			
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		} else {
			throw new RandomDotOrgQuotaReachedException();
		}
	}
	private int getRemainingQuota() {
		int quota = 0;
		
		try {
			// Connect to random.org
			URL randomdotorg = new URL(QUOTA_URL);
			URLConnection connection = randomdotorg.openConnection();
			connection.setDoInput(true);

			// Set up input stream
			InputStream inStream = connection.getInputStream();
			BufferedReader input = new BufferedReader(new InputStreamReader(inStream));
		
			String quotaString = input.readLine();
			quota = Integer.valueOf(quotaString);
			input.close();
				
		} catch(Exception ex) {
			ex.printStackTrace();
		}
		return quota;
	}
}