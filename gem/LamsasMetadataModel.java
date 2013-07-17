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

import com.google.common.collect.*;

public class LamsasMetadataModel {
	
	/*
	 * Enums for modeling data.
	 */

	public static enum InformantType {UNKNOWN, ONE, TWO, THREE };
	public static enum Sex { MALE, FEMALE };
	public static enum Race { WHITE, BLACK };
	public static enum EducationLevel { UNKNOWN, ZERO, ONE, TWO, THREE, FOUR, FIVE, SIX };
	public static enum CultureLevel { UNKNOWN, NOT_CULTURED, CULTURED };
	
	/*
	 * String constants for converting from JSON to more structured data.
	 */
	
	// String values that correspond to items in the InformantType enum
	private static final String CULT_LEVEL_UNKNOWN_STRING = "?";
	private static final String CULT_LEVEL_NOT_CULTURED_STRING = "N";
	private static final String CULT_LEVEL_CULTURED_STRING = "Y";
	// String values that correspond to items in the InformantType enum
	private static final String INF_TYPE_UNKNOWN_STRING = "?";
	private static final String INF_TYPE_ONE_STRING = "I";
	private static final String INF_TYPE_TWO_STRING = "II";
	private static final String INF_TYPE_THREE_STRING = "III";
	// String values that correspond to items in the Sex enum
	private static final String MALE_STRING = "M";
	private static final String FEMALE_STRING = "F";
	// String values that correspond to items in the Race enum
	private static final String WHITE_STRING = "W";
	private static final String BLACK_STRING = "B";
	// String values that correspond to items in the EducationLevel enum
	private static final String EDUC_LEVEL_UNKNOWN_STRING = "?";
	private static final String EDUC_LEVEL_ZERO_STRING = "0";
	private static final String EDUC_LEVEL_ONE_STRING = "1";
	private static final String EDUC_LEVEL_TWO_STRING = "2";
	private static final String EDUC_LEVEL_THREE_STRING = "3";
	private static final String EDUC_LEVEL_FOUR_STRING = "4";
	private static final String EDUC_LEVEL_FIVE_STRING = "5";
	private static final String EDUC_LEVEL_SIX_STRING = "6";
	
	/* 
	 * Bi-directional maps to easily convert from string representations to structured
	 * representations and vice versa.
	 */
	
	public static final ImmutableBiMap<Sex, String> sexMap = ImmutableBiMap.of(
			Sex.MALE, MALE_STRING,
			Sex.FEMALE, FEMALE_STRING);
	public static final ImmutableBiMap<InformantType, String> informantTypeMap = ImmutableBiMap.of(
			InformantType.UNKNOWN, INF_TYPE_UNKNOWN_STRING,
			InformantType.ONE, INF_TYPE_ONE_STRING,
			InformantType.TWO, INF_TYPE_TWO_STRING,
			InformantType.THREE, INF_TYPE_THREE_STRING
			);
	public static final ImmutableBiMap<Race, String> raceMap = ImmutableBiMap.of(
			Race.WHITE, WHITE_STRING,
			Race.BLACK, BLACK_STRING
			);
	public static final ImmutableBiMap<EducationLevel, String> educationLevelMap = 
			new ImmutableBiMap.Builder<EducationLevel, String>()
			.put(EducationLevel.UNKNOWN, EDUC_LEVEL_UNKNOWN_STRING)
			.put(EducationLevel.ZERO, EDUC_LEVEL_ZERO_STRING)
			.put(EducationLevel.ONE, EDUC_LEVEL_ONE_STRING)
			.put(EducationLevel.TWO, EDUC_LEVEL_TWO_STRING)
			.put(EducationLevel.THREE, EDUC_LEVEL_THREE_STRING)
			.put(EducationLevel.FOUR, EDUC_LEVEL_FOUR_STRING)
			.put(EducationLevel.FIVE, EDUC_LEVEL_FIVE_STRING)
			.put(EducationLevel.SIX, EDUC_LEVEL_SIX_STRING)
			.build();
	public static final ImmutableBiMap<CultureLevel, String> cultureLevelMap = ImmutableBiMap.of(
			CultureLevel.UNKNOWN, CULT_LEVEL_UNKNOWN_STRING,
			CultureLevel.NOT_CULTURED, CULT_LEVEL_NOT_CULTURED_STRING,
			CultureLevel.CULTURED, CULT_LEVEL_CULTURED_STRING
			);
	public static final ImmutableBiMap<Boolean, String> auxMap = ImmutableBiMap.of(
			true, "Y",
			false, "N"
			);
}
