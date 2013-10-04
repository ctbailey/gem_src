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

package gem.simulation.state;

import gem.simulation.Utility;
import gem.simulation.board.InvalidCellStateException;
import gem.simulation.board.ICell.CellState;
import gem.simulation.state.neighbor_topology.INeighborGraph;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class ConwaySerializedState implements ISerializedState {
	private final String state;
	private final INeighborGraph uncompressedNeighborGraph;
	
	public ConwaySerializedState(AbstractConwayState abstractConwayState, INeighborGraph uncompressedNeighborGraph) {
		this.state = convertToSerializationString(abstractConwayState);  
		this.uncompressedNeighborGraph = uncompressedNeighborGraph;
	}
	
	public ConwayState deserialize() {
		return deserialize(state, uncompressedNeighborGraph);
	}
	
	public static String convertToSerializationString(AbstractConwayState state) {
		List<Byte> cellBytes = new ArrayList<Byte>();
		List<Boolean> tempByte = new ArrayList<Boolean>();
		List<Boolean> columnTerminator = Arrays.asList(true, true);
		
		for(int x = 0; x < state.getWidth(); x++) {
			for(int y = 0; y < state.getHeight(); y++) {
				List<Boolean> bits = CellState.ToBitList(state.getCell(x, y).getState());
				validateCellState(bits);
				tempByte.addAll(bits);
				
				if(tempByte.size() == Byte.SIZE) {
					cellBytes.add(Utility.ToByte(tempByte));
					tempByte.clear();
				}
			}
			tempByte.addAll(columnTerminator);
			if(tempByte.size() == Byte.SIZE) {
				cellBytes.add(Utility.ToByte(tempByte));
				tempByte.clear();
			}
		}
		tempByte.addAll(columnTerminator);
		cellBytes.add(Utility.ToByte(tempByte));
		
		return ByteBuffer.wrap(Utility.toBytePrimitiveArray(cellBytes)).asCharBuffer().toString();
	}
	public static ConwayState deserialize(String string, INeighborGraph uncompressedNeighborGraph) {
		byte[] byteArray = new byte[string.length()];
		for (int i = 0; i < byteArray.length; i++) {
			byteArray[i] = (byte) string.charAt(i);
		}
		
		List<List<ConwayCell>> cellList = new ArrayList<List<ConwayCell>>();
		cellList.add(new ArrayList<ConwayCell>());
		
		boolean[] bits = new boolean[Byte.SIZE];
		for(byte b : byteArray) {
			bits = Utility.toBooleanArray(b);
			
			for(int i = 0; i < Byte.SIZE / 2; i++) {
				if(bits[i] && bits[i+1]) { // If there's a column terminator
					if(cellList.get(cellList.size() - 1).size() == 0) { // If the last element is an empty list
						cellList.remove(cellList.size() - 1);
						break;
					} else {
						cellList.add(new ArrayList<ConwayCell>());
					}
				} else {
					cellList.get(cellList.size() - 1).add(
							new ConwayCell(
									CellState.parse(
											Arrays.asList(bits[i], bits[i+1])
									)
							));
				}
			}
		}
		
		return new ConwayState(Utility.to2DArray(cellList), uncompressedNeighborGraph);
	}
	private static void validateCellState(List<Boolean> bits) {
		int val = Utility.ToByte(bits);
		if(	val < 0 || val > 2) {
			throw new InvalidCellStateException(ConwayCell.class, CellState.convert(val));		
		}
	}
}
