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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public enum DataType {
	BOOLEAN("Логический", "BOOLEAN", Boolean.class, null),
	BYTE("Целое число (1 байт)", "BYTE", Byte.class, null),
	SHORT("Целое число (2 байта)", "SHORT", Short.class, null),
	INTEGER("Целое число (4 байта)", "INTEGER", Integer.class, new String[] {"DEC [10]", "HEX [16]", "BOOL [2]", "OCT [8]"}),
	FLOAT("Float", "FLOAT", Float.class, null),
	DOUBLE("Double", "DOUBLE", Double.class, null),
	STRING("Строка", "STRING", String.class, null),
	DATE("Дата", "DATE", LocalDate.class, new String[] {"dd.MM.YYYY"}),
	TIME("Время", "DATE", LocalTime.class, new String[] {"HH:mm:ss"}),
	DATETIME("Дата/Время", "DATE", LocalDateTime.class, new String[] {"dd.MM.YYYY HH:mm:ss"});
	
	private final String caption;
	private final String value;

	private final Class _class;
	private final String[] formatList;
	
	/** @param parser default parser */
	DataType(String caption, String value, Class _class, String[] formatList){
		this.caption = caption;
		this.value = value;
		this._class = _class;
		this.formatList = formatList;
	}

	public String getCaption() {
		return caption;
	}
	
	public String getValue() {
		return value;
	}
	public Class get_class() {
		return _class;
	}

	public String[] getFormatList() {
		return formatList;
	}
	
	@Override
	public String toString() {
		return String.format("%s [%s]", caption, value);
	}
}
