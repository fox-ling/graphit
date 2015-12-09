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

package com.foxling.graphit.config;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public enum DataType {
	BOOLEAN("Логический", "BOOLEAN", Boolean.class, true, null),
	
	BYTE("Целое число (1 байт)", "BYTE", Byte.class, true, Arrays.asList(
			new Format("DEC", "10"),
			new Format("HEX", "16"),
			new Format("BOOL", "2"),
			new Format("OCT", "8")
	)),
	SHORT("Целое число (2 байта)", "SHORT", Short.class, true, BYTE.getFormatList()),
	INTEGER("Целое число (4 байта)", "INTEGER", Integer.class, true, BYTE.getFormatList()),
	
	FLOAT("Float", "FLOAT", Float.class, true, null),
	DOUBLE("Double", "DOUBLE", Double.class, true, null),
	
	STRING("Строка", "STRING", String.class, true, null),
	
	DATE("Дата", "DATE", LocalDate.class, false, Arrays.asList(
			new Format("dd.MM.YYYY", "dd.MM.YYYY")
	)),
	TIME("Время", "TIME", LocalTime.class, false, Arrays.asList(
			new Format("HH:mm:ss", "HH:mm:ss")
	)),
	DATETIME("Дата/Время", "DATETIME", LocalDateTime.class, false, Arrays.asList(
			new Format("dd.MM.YYYY HH:mm:ss", "dd.MM.YYYY HH:mm:ss")
	));
	
	private final String caption;
	private final String value;
	private final Class _class;
	private final boolean fixedFormatList;
	private final List<Format> formatList;
	
	DataType(String caption, String value, Class _class, boolean fixedFormatList, List<Format> formatList){
		this.caption = caption;
		this.value = value;
		this._class = _class.getClass();
		this.fixedFormatList = fixedFormatList;
		
		if (formatList == null)
			formatList = new LinkedList<Format>();
		
		if (fixedFormatList) {
			this.formatList = Collections.unmodifiableList(formatList);
		} else 
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

	public boolean isFixedFormatList() {
		return fixedFormatList;
	}
	
	public boolean isFormatRequired() {
		return this.formatList != null;
	}

	public List<Format> getFormatList() {
		return formatList;
	}
	
	public Format getFormat(String value) throws IllegalArgumentException{
		if (value == null)
			throw new IllegalArgumentException("Ошибка при попытка поиска формата с NULL-значением");
		
		for (Format format : formatList) {
			if (format.value.equals(value))
				return format;
		}
		
		return null;
	}
	
	public Format getDefaultFormat() {
		if (formatList == null && formatList.size() == 0) {
			return Format.EMPTY_FORMAT;
		} else
			return formatList.get(0);
	}
	
	public static DataType getDefaultDataType() {
		return DataType.STRING;
	}
	
	@Override
	public String toString() {
		return String.format("%s [%s]", caption, value);
	}
}
