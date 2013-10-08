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

import gem.simulation.board.BoardDimensions;
import gem.simulation.state.AbstractConwayCell;

import java.awt.Point;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultWeightedEdge;

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
	public static AbstractConwayCell[][] to2DArray(List<List<AbstractConwayCell>> cells) {
		AbstractConwayCell[][] cellArray = new AbstractConwayCell[cells.size()][cells.get(0).size()];
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
	public static <T> T[] concatenate(T[] first, T[] second) {
	  T[] result = Arrays.copyOf(first, first.length + second.length);
	  System.arraycopy(second, 0, result, first.length, second.length);
	  return result;
	}
	@SuppressWarnings("rawtypes")
	public static void removeRandomElement(List list) {
		if(list.size() != 0) {
			double highestIndex = list.size() - 1;
			int randomIndex = (int)(Math.random() * highestIndex);
			list.remove(randomIndex);
		}
	}
	@SuppressWarnings("unchecked")
	public static <T> T getRandomElement(T[][] nestedArrays) {
		return getRandomElement((T[])getRandomElement(nestedArrays));
	}
	public static <T> T getRandomElement(T[] array) {
		return array[getRandomIndex(array)];
	}
	public static <T> Point getRandomIndexPair(T[][] nestedArrays) {
		int xIndex = getRandomIndex(nestedArrays);
		int yIndex = getRandomIndex(nestedArrays[0]);
		return new Point(xIndex, yIndex);
	}
	public static <T> int getRandomIndex(T[] array) {
		return getRandomIntFromZeroToExclusive(array.length);
	}
	public static boolean isInBounds(int x, int y, BoardDimensions dimensions) {
		return isInBounds(x, y, dimensions.getWidth(), dimensions.getHeight());
	}
	public static boolean isInBounds(int x, int y, int width, int height) {
		return (x >= 0) 
			&& (y >= 0) 
			&& (x < width)
			&& (y < height);
	}
	public static Point getRandomPointOnBoard(BoardDimensions dimensions) {
		int x = getRandomIntFromZeroToExclusive(dimensions.getWidth());
		int y = getRandomIntFromZeroToExclusive(dimensions.getHeight());
		return new Point(x,y);
	}
	private static int getRandomIntFromZeroToExclusive(int limit) {
		return (int) Math.floor(Math.random() * (limit - 1));
	}
	public static Point[] toArray(Collection<Point> collection) {
		Point[] array = new Point[collection.size()];
		collection.toArray(array);
		return array;
	}
	public static DefaultWeightedEdge[] toArray(Set<DefaultWeightedEdge> collection) {
		DefaultWeightedEdge[] array = new DefaultWeightedEdge[collection.size()];
		collection.toArray(array);
		return array;
	}
	public static Point getOtherNode(Point node, DefaultWeightedEdge e, Graph<Point, DefaultWeightedEdge> g) {
		Point source = g.getEdgeSource(e);
		if(node.equals(source)) {
			return g.getEdgeTarget(e);
		} else if(node.equals(g.getEdgeTarget(e))) {
			return source;
		} else {
			throw new IllegalArgumentException("Node not touching the supplied edge.");
		}
	}
}