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

import java.io.File;
import java.io.FileWriter;

import com.google.common.base.Charsets;
import com.google.common.io.Files;

public class JsonLogger {
	public static void writeJsonToFile(File file, String json) {
		try {
			FileWriter fw = new FileWriter(file);
			fw.write(json);
			fw.close();
		} catch(Exception ex) { ex.printStackTrace(); }
	}
	public static String readJsonFromFile(File file) {
		String json = "";
		try {
			json = Files.toString(file, Charsets.UTF_8);
		} catch(Exception ex) { ex.printStackTrace(); }
		return json;
	}
}