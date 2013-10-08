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

package gem.talk_to_outside_world.validation;

import static gem.LamsasMetadataModel.*;
import gem.simulation.state.AbstractConwayCell;
import gem.simulation.state.ICell;

public class LamsasCell extends AbstractConwayCell {
	
	private final String id;
	private final String oldNumber;
	private final boolean aux;
	private final String fw;
	private final String ws;
	private final short year;
	private final InformantType informantType;
	private final String exp_s;
	private final CultureLevel cultureLevel;
	private final Sex sex;
	private final byte age;
	private final EducationLevel educationLevel;
	private final String occupation;
	private final Race race;
	private final String communityType;
	private final String communityName;
	private final String stateAbbreviation;
	private final int fedComm;
	
	public LamsasCell(SimpleValidationCell jsonCell) {
		this(	jsonCell.x,
				jsonCell.y,
				jsonCell.getState(),
				jsonCell.id,
				jsonCell.actualPointMeta.oldnumbe,
				jsonCell.actualPointMeta.aux,
				jsonCell.actualPointMeta.fw,
				jsonCell.actualPointMeta.ws,
				jsonCell.actualPointMeta.year,
				jsonCell.actualPointMeta.inftype,
				jsonCell.actualPointMeta.exp_s,
				jsonCell.actualPointMeta.cult,
				jsonCell.actualPointMeta.sex,
				jsonCell.actualPointMeta.age,
				jsonCell.actualPointMeta.educ,
				jsonCell.actualPointMeta.occup,
				jsonCell.actualPointMeta.race,
				jsonCell.actualPointMeta.commtype,
				jsonCell.actualPointMeta.community,
				jsonCell.actualPointMeta.statelong,
				jsonCell.actualPointMeta.fedcomm
				);
	}
			
	public LamsasCell(
			int x,
			int y,
			CellState cellState,
			String id,
			String oldNumber,
			String aux,
			String fw,
			String ws,
			String year,
			String infType,
			String exp_s,
			String cultureLevel,
			String sex,
			String age,
			String educationLevel,
			String occupation,
			String race,
			String communityType,
			String communityName,
			String stateAbbreviation,
			String fedComm
			) {
		super(cellState, false);
		
		this.id = id;
		this.oldNumber = oldNumber;
		this.aux = parseAux(aux);
		this.fw = fw;
		this.ws = ws;
		this.year = Short.parseShort(year);
		this.informantType = parseInformantType(infType);
		this.exp_s = exp_s;
		this.cultureLevel = parseCultureLevel(cultureLevel);
		this.sex = parseSex(sex);
		
		if(age.equals("?")) { this.age = -1; }
		else { this.age = Byte.parseByte(age); } 
		
		this.educationLevel = parseEducationLevel(educationLevel);
		this.occupation = occupation;
		this.race = parseRace(race);
		this.communityType = communityType;
		this.communityName = communityName;
		this.stateAbbreviation = stateAbbreviation;
		this.fedComm = Integer.parseInt(fedComm);
	}
	
	private boolean parseAux(String aux) {
		if(auxMap.containsValue(aux)) { return auxMap.inverse().get(aux); }
		else {
			throw new IllegalArgumentException("Tried to parse aux that didn't match a value in the auxMap.");
		}
	}
	private InformantType parseInformantType(String infType) {
		if(informantTypeMap.containsValue(infType)) { return informantTypeMap.inverse().get(infType); }
		else {
			throw new IllegalArgumentException("Tried to parse inftype, but the string wasn't one of the pre-defined strings: " + infType);
		}
	}
	private CultureLevel parseCultureLevel(String cult) {
		if(cultureLevelMap.containsValue(cult)) { return cultureLevelMap.inverse().get(cult); }
		else {
			throw new IllegalArgumentException("Tried to parse culture level that didn't match any of the pre-defined strings.");
		}
	}
	private Sex parseSex(String sex) {
		if(sexMap.containsValue(sex)) { return sexMap.inverse().get(sex); }
		else {
			throw new IllegalArgumentException("Tried to parse sex " + sex + " but string didn't match predefined m/f strings.");
		}
	}
	private Race parseRace(String race) {
		if(raceMap.containsValue(race)) { return raceMap.inverse().get(race); }
		else {
			throw new IllegalArgumentException("Tried to parse race string, but didn't match any pre-defined values: " + race);
		}
	}
	private EducationLevel parseEducationLevel(String educ) {
		if(educationLevelMap.containsValue(educ)) { return educationLevelMap.inverse().get(educ); }
		else {
			throw new IllegalArgumentException("Tried to parse education level that didn't match any of the pre-defined strings.");
		}
	}

	public String getId() {
		return id;
	}
	public boolean isAux() {
		return aux;
	}
	public String getOldNumber() {
		return oldNumber;
	}
	public String getFw() {
		return fw;
	}
	public String getWs() {
		return ws;
	}
	public short getYear() {
		return year;
	}
	public InformantType getInformantType() {
		return informantType;
	}
	public String getExp_s() {
		return exp_s;
	}
	public CultureLevel getCultureLevel() {
		return cultureLevel;
	}
	public Sex getSex() {
		return sex;
	}
	public byte getAge() {
		return age;
	}
	public EducationLevel getEducationLevel() {
		return educationLevel;
	}
	public String getOccupation() {
		return occupation;
	}
	public Race getRace() {
		return race;
	}
	public String getCommunityType() {
		return communityType;
	}
	public String getCommunityName() {
		return communityName;
	}
	public String getStateAbbreviation() {
		return stateAbbreviation;
	}
	public int getFedComm() {
		return fedComm;
	}

	@Override
	public ICell getModifiedCopy(CellState newState) {
		throw new RuntimeException("Not implemented yet.");
	}

	@Override
	public AbstractConwayCell deepCopy() {
		throw new RuntimeException("Not implemented yet.");
	}

	@Override
	public ICell getModifiedCopy(boolean isSelected) {
		throw new RuntimeException("Not implemented yet.");
	}

	@Override
	public ICell getModifiedCopy(CellState newState, boolean isSelected) {
		throw new RuntimeException("Not implemented yet.");
	}
}
