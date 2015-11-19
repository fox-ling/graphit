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

import java.text.ParseException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Field {
	/** Short name of the field, it's gonna appear<br>
	 *  in the MainFrame's table and at graph's legend */
	private String name;
	
	/** Description of the field, appears in tooltips */
	private String description;
	
	/** Field's Data Type */
	private DataType datatype;
	
	/** Format of the data in the log file. */
	private String format;
	
	/** Divisor between current and next fields */
	private String delimiter;
	
	/** Optional field could not appear in a line of the log file.<br>
	 * *Only one field can be optional */
	private boolean optional;
	
	/** <code>True</code> if value of the field could be sum of {@link #valueSet} */
	private boolean bitmask;
	
	/** Contains values that could appear in the field and its' descriptions */
	private List<Item<Object>> valueSet;
	
	/** Object that converts string to field's {@link #datatype} */
	private Parser parser;
	
	public Field(String name, String description, DataType datatype, String delimiter, String format, String isOptional, String bitmask) throws Exception {
		try {
			setName(name);
			setDescription(description);
			setDatatype(datatype);
			setFormat(format);
			setDelimiter(delimiter);
			setOptional(isOptional);
			setParser(DefaultParser.getDefaultParser(datatype, format));
			setBitmask(bitmask);
		} catch (Exception e) {
			throw new Exception("Ошибка при создании поля: " + e.getMessage());
		}
	}
	
	/** @see {@link #name} */
	public void setName(String name) throws IllegalArgumentException {
		if (name == null || name.equals(""))
			throw new IllegalArgumentException("Имя не должно быть пустым");
		
		this.name = name;
	}
	
	/** @see {@link #description} */
	public void setDescription(String description) {
		this.description = description;
	}
	
	/** @see {@link #datatype} */
	public void setDatatype(String datatype){
		DataType value = DataType.valueOf(datatype);
		if (value == null)
			throw new IllegalArgumentException("Неподдерживаемый тип данных");
		
		setDatatype(value);
	}
	
	/** @see {@link #datatype} */
	public void setDatatype(DataType datatype) {
		if (datatype == null)
			throw new IllegalArgumentException("Тип данных не должен быть пустым");
		
		this.datatype = datatype;
	}
	
	/** @see {@link #format} */
	public void setFormat(String format) {
		if (datatype == DataType.DATETIME && (format == null || format.equals("")))
			throw new IllegalArgumentException("Необходимо задать формат даты/времени");
		
		this.format = format;
	}
	
	/** @see {@link #delimiter} */
	public void setDelimiter(String delimiter) {
		if (delimiter == null || delimiter.equals(""))
			throw new IllegalArgumentException("Ограничитель не должен быть пустым");
		
		this.delimiter = delimiter;
	}
	
	/** @see {@link #optional} */
	public void setOptional(String optional) {
		this.optional = parseBoolean(optional);
	}

	/** @see {@link #optional} */
	public void setOptional(boolean optional) {
		this.optional = optional;
	}

	/** @see {@link #parser} */
	public void setParser(Parser parser) {
		this.parser = parser;
	}
	
	/** @throws ParseException 
	 * @see {@link #valueSet} */
	public void setValueSet(List<Item<Object>> valueSet) throws Exception {
		if (valueSet == null || valueSet.size() == 0) {
			this.valueSet = null;
			return;
		}
		
		for (Item<Object> key : valueSet) {
			try {
				key.value = this.parser.parse(key.source);
			} catch (Exception e) {
				throw new Exception(String.format("Не удалось конвертировать строку '%s' в тип %s", key.source, this.datatype.getCaption()));
			}
		}
		this.valueSet = valueSet;
	}
	
	/** @see {@link #bitmask} */
	public void setBitmask(String bitmask) {
		this.bitmask = parseBoolean(bitmask);
	}
	
	/** @see {@link #bitmask} */
	public void setBitmask(boolean bitmask) {
		this.bitmask = bitmask;
	}
	
	/** @see {@link #name} */
	public String getName() { return name; }
	/** @see {@link #description} */
	public String getDescription() { return description; }
	/** @see {@link #datatype} */
	public DataType getDatatype() { return datatype; }
	/** @see {@link #delimiter} */
	public String getDelimiter() { return delimiter; }
	/** @see {@link #format} */
	public String getFormat() { return format; }
	/** @see {@link #parser} */
	public Parser getParser() { return parser; }
	/** @see {@link #optional} */
	public boolean isOptional() { return optional; }
	/** @see {@link #valueSet} */
	public List<Item<Object>> getValueSet() { return valueSet; }
	/** @see {@link #bitmask} */
	public boolean isBitmask() { return bitmask; }

	@Override
	public String toString() { return name; }
	
	/** String to boolean converter
	 * @return <code>true</code> if <code><b>text</b></code> in ["true", "yes", "1"] */
	private boolean parseBoolean(String text) {
		return text != null && (text.equals("true") || text.equals("yes") || text.equals("1"));
	}
}
