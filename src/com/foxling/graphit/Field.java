/* graphit - log file browser
 * Copyright© 2015 Shamil Absalikov, foxling@live.com
 *
 * graphit is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * graphit is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.foxling.graphit;

public class Field {
	private final Class dataType;
	private final String name;
	/** Number of digits to the right of the decimal point in a number */
	private int scale;
	
	public Field(Class dataType, String name) {
		this.dataType = dataType;
		this.name = name;
	}


	public Field(Class dataType, String name, int scale) {
		this.dataType = dataType;
		this.name = name;
		this.scale = scale;
	}

	public Class getDataType() {
		return dataType;
	}

	public String getName() {
		return name;
	}

	/** @see {@link Field#scale} */
	public int getScale() {
		return scale;
	}

	/** @see {@link Field#scale} */
	public void setScale(int scale) {
		this.scale = scale;
	}
}
