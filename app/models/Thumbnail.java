/*Copyright (c) 2015 "hbz"

This file is part of thumby.

thumby is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package models;

import java.io.File;
import java.io.StringWriter;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author Jan Schnasse
 *
 */
public class Thumbnail {
    /**
     * id in cache
     */
    public String id;
    /**
     * a name
     */
    public String name;
    /**
     * the actual thumbnail
     */
    public File thumb;
    /**
     * the former content type
     */
    public String originalContentType;

    @Override
    public String toString() {
	ObjectMapper mapper = new ObjectMapper();
	StringWriter w = new StringWriter();
	try {
	    mapper.writeValue(w, this);
	} catch (Exception e) {
	    e.printStackTrace();
	    return super.toString();
	}
	return w.toString();
    }
}
