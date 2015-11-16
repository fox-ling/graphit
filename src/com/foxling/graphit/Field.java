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
	private Map<Object,String> valueSet;
	
	/** Object that converts string to field's {@link #datatype} */
	private Parser parser;
	
	public Field(String name, String description, DataType datatype, String delimiter, String format, String isOptional) throws Exception {
		try {
			setName(name);
			setDescription(description);
			setDatatype(datatype);
			setFormat(format);
			setDelimiter(delimiter);
			setOptional(isOptional);
			setParser(DefaultParser.getDefaultParser(datatype, format));
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
		this.optional = optional != null && (optional.equals("true") || optional.equals("yes") || optional.equals("1"));
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
	public void setValueSet(Map<String,String> valueSet) throws Exception {
		if (valueSet == null || valueSet.size() == 0) {
			this.valueSet = null;
			return;
		}
		
		Map<Object,String> _valueSet = new HashMap<Object,String>(valueSet.size());
		for (String key : valueSet.keySet()) {
			try {
				_valueSet.put(this.parser.parse(key), valueSet.get(key));
			} catch (Exception e) {
				throw new Exception(String.format("Не удалось конвертировать строку '%s' в тип %s", key, this.datatype.getCaption()));
			}
		}
		this.valueSet = _valueSet;
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
	public Map<Object, String> getValueSet() { return valueSet; }
	/** @see {@link #bitmask} */
	public boolean isBitmask() { return bitmask; }

	@Override
	public String toString() { return name; }
}
