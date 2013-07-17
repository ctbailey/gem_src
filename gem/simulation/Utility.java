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

import java.util.List;

public class Utility {
	public static byte[] toBytePrimitiveArray(List<Byte> byteList) {
		byte[] byteArray = new byte[byteList.size()];
		for(int i = 0; i < byteList.size(); i++) {
			byteArray[i] = byteList.get(i);
		}
		return byteArray;
	}
	public static boolean[] toBooleanArray(byte b) {
		boolean[] val = new boolean[Byte.SIZE];
		for(int i = 0; i < Byte.SIZE; i++) {
			val[i] = (b % 2) == 1;
			b >>= 1;
		}
		return val;
	}
	public static byte ToByte(List<Boolean> bits) {
		byte val = 0;
		for (boolean b : bits)
		{
			val <<= 1;
			if (b) { val += 1; }
		}
		return val;
	}
	public static ConwayCell[][] to2DArray(List<List<ConwayCell>> cells) {
		ConwayCell[][] cellArray = new ConwayCell[cells.size()][cells.get(0).size()];
		for(int x = 0; x < cells.size(); x++) {
			for(int y = 0; y < cells.get(x).size(); y++) {
				if(cells.get(x).get(y) == null) {
					throw new IllegalArgumentException("Cannot initialize a board state with a null cell.");
				}
				cellArray[x][y] = cells.get(x).get(y);
			}
		}
		return cellArray;
	}
}
