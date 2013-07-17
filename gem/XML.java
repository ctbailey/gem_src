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

package gem;
/*
import java.io.*;

import javax.xml.stream.events.*;
import javax.xml.stream.*;
import javax.xml.namespace.*;
*/
public class XML {
	
	/*
	File temp;
	
	XMLEventReader r;
	XMLEventWriter w;
	XMLEventFactory eventFactory;

	final String NAMESPACE = "lap.ca";
	final QName SEQUENCE = new QName(NAMESPACE,"sequence");
	final QName BLOCK = new QName(NAMESPACE,"block");
	
	public XML() {
		
		try {
			
			temp = new File("temp.xml");
			
			BufferedReader reader = new BufferedReader(new FileReader(temp));
			XMLInputFactory inputFactory = XMLInputFactory.newInstance();
			r = inputFactory.createXMLEventReader(reader);
			
			BufferedWriter writer = new BufferedWriter(new FileWriter(temp));
			XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
			w = outputFactory.createXMLEventWriter(writer);
			
			eventFactory = XMLEventFactory.newInstance();
			
			w.add(eventFactory.createStartDocument());
			w.add(eventFactory.createIgnorableSpace("\n\n"));
			w.add(eventFactory.createStartElement(SEQUENCE,null,null));
			
		} catch(Exception ex) {
			
			ex.printStackTrace();
			
		}
		
		
	}
	
	public void gimmeATry() {
		
		try {
			
			w.add(eventFactory.createStartElement(BLOCK,null,null));
			
			w.add(eventFactory.createIgnorableSpace("\n"));
			writeArray(AutomatonVars.worldArray);
			w.add(eventFactory.createIgnorableSpace("\n"));
			
			w.add(eventFactory.createEndElement(BLOCK,null));
			w.add(eventFactory.createEndElement(SEQUENCE,null));
			
			w.add(eventFactory.createEndDocument());
			
			w.flush();
			
		} catch(Exception ex) {
			
			ex.printStackTrace();
			
		}
	}
	
	public boolean readNextBlock() {
		
		try{
		
			switch(r.peek().getEventType()) {
				
				case XMLStreamConstants.START_ELEMENT:
					StartElement element = r.nextEvent().asStartElement();
			
					if(element.getName().toString() == "block") {
				
						Debug.print("Characters: " + r.getElementText());
						Debug.print("Returned true.");
						return true;
					
					} else {
						
						Debug.print("Wasn't a BLOCK node. Trying again.");
						readNextBlock();
						
					}
				
				case XMLStreamConstants.END_DOCUMENT:
					Debug.print("Found end of document.");
					return false;
					
				default:
					r.nextEvent();
					Debug.print("Wasn't a START element. Trying again.");
					readNextBlock();
					break;
				
			}
			
			
		} catch(Exception ex) { 
			ex.printStackTrace();
		}
		
		Debug.print("Returned false.");
		return false;
	
	}
	
	public void writeArray(int[][] array) {
		
		String string = "";
		
		for(int y = 0; y < AutomatonVars.worldArray[0].length; y++) { // Iterate through the columns of the world array
			for(int x = 0; x < AutomatonVars.worldArray.length; x++) { // Iterate through the rows of the world array
		
				string = string + array[x][y] + " ";
			}
			
			string = string + "\n";
			
		}
		
		writeToXML(string);
		
	}
	
	public void writeToXML(String string) {
		
		try{
		
			w.add(eventFactory.createCharacters(string));
		
		} catch(Exception ex) {
			
			ex.printStackTrace();
			Debug.print("Error writing to XML file.");
			
		}
			
	}
	*/
}
