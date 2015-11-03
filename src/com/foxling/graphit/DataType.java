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

public enum DataType {
	BOOLEAN("Логический [BOOLEAN]"),
	INTEGER("Целое число [INTEGER]"),
	FLOAT("Float [FLOAT]"),
	DOUBLE("Double [DOUBLE]"),
	STRING("Строка [STRING]"),
	DATE("Дата [DATE]"),
	DATETIME("Дата/Время [DATETIME]");
	
	private final String caption;
	
	/** @param parser default parser */
	DataType(String caption){
		this.caption = caption;
	}

	public String getCaption() {
		return caption;
	}
	
	@Override
	public String toString() {
		return caption;
	}
}
