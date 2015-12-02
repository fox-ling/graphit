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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Field {
	private static int g_counter;
	private int id;
	/** Short name of the field, it's gonna appear<br>
	 *  in the MainFrame's table and at graph's legend */
	private String name;
	
	/** Description of the field, appears in tooltips */
	private String description;
	
	/** Field's Data Type */
	private DataType datatype;
	
	/** Format of the data in the log file. */
	private Format format;
	
	/** Divisor between current and next fields */
	private FieldDelimiter delimiter;
	
	/** Optional field could not appear in a line of the log file.<br>
	 * *Only one field can be optional */
	private boolean optional;
	
	/** <code>True</code> if value of the field could be sum of {@link #valueList} */
	private boolean bitmask;
	
	/** Contains values that could appear in the field and its' descriptions */
	private List<FieldValue> valueList;
	
	/** Object that converts string to field's {@link #datatype} */
	private Parser parser;
	
	{
		id = ++Field.g_counter;
		name = String.format("<Поле #%d>", id);
	}
	
	public Field() {}
	
	public Field(String name) throws IllegalArgumentException {
		setName(name);
	}
	
	public Field(String name, String description, DataType datatype, String delimiter, String format, String isOptional, String bitmask) throws Exception {
		try {
			setName(name);
			setDescription(description);
			setDatatype(datatype);
			setFormat(format);
			setDelimiter(delimiter);
			setOptional(isOptional);
			setBitmask(bitmask);
			
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
	public void setDatatype(String datatype) throws IllegalArgumentException {
		DataType value = DataType.valueOf(datatype);
		if (value == null)
			throw new IllegalArgumentException("Неподдерживаемый тип данных");
		
		setDatatype(value);
	}
	
	/** @see {@link #datatype} */
	public void setDatatype(DataType datatype) throws IllegalArgumentException {
		if (datatype == null)
			throw new IllegalArgumentException("Тип данных не должен быть пустым");
		
		this.datatype = datatype;
	}
	
	/** @see {@link #format} */
	public void setFormat(Format format) {
		this.format = format;		
	}
	
	/** @see {@link #format} */
	public void setFormat(String format) throws IllegalStateException {
		if (format == null || format.equals("")) {
			if (datatype.getFormatList() == null) {
				this.format = null;
			} else if (datatype.getFormatList().size() > 0) {
				this.format = datatype.getFormatList().get(0);
			} else if (datatype.isFormatRequired()) {
				throw new IllegalStateException(String.format("Тип данных %s требует явного указания формата", datatype.getCaption()));
			}	
		} else {
			Format f = datatype.getFormat(format);
			if (f == null) {
				if (!datatype.isFixedFormatList()) {
					f = new Format("*[" + getName() + "]", format);
				} else
					throw new IllegalStateException(String.format("У типа данных %s фиксированный набор форматов и формата \"%s\" в нём нет", datatype.getCaption(), format));
			}
			
			this.format = f;
		}
	}
	
	/** @see {@link #delimiter} */
	public void setDelimiter(String delimiter) {
		if (delimiter == null || delimiter.equals(""))
			throw new IllegalArgumentException("Ограничитель не должен быть пустым");
		setDelimiter(FieldDelimiter.valueOf(delimiter));
	}
	
	/** @see {@link #delimiter} */
	public void setDelimiter(FieldDelimiter delimiter) {
		if (delimiter == null)
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
	
	/** @throws Exception 
	 * @see {@link #valueList} */
	public void validateValue(FieldValue value) throws Exception {
		try {
			value.value = parser.parse(value.source);
		} catch (Exception e) {
			value.value = null;
			throw new Exception(String.format("Не удалось конвертировать строку '%s' в тип %s", value.source, datatype.getCaption()));
		}
	}
	
	public void addValue(FieldValue value) throws Exception {
		int size;
		if (valueList == null) {
			size = 0;
		} else
			size = valueList.size();
		
		addValueAt(size, value);
	}
	
	public void addValueAt(int index, FieldValue value) throws Exception {
		if (value == null)
			throw new IllegalArgumentException("Попытка вставить null-значение поля");
		
		if (valueList == null)
			valueList = new ArrayList<FieldValue>();
		
		if (index < 0 || index > valueList.size())
			throw new IndexOutOfBoundsException(String.format("Попытка вставить значение поля в некорректную позицию (%d)", index));
		
		valueList.add(index, value);
	}
	
	public void removeValues(int[] index) throws IndexOutOfBoundsException, NullPointerException {
		if (valueList == null)
			throw new NullPointerException("Список значений не инициализирован - нечего удалять");
		
		int size = valueList.size();
		
		Arrays.sort(index);
		for (int i = index.length - 1; i >= 0; i--) {
			if (index[i] < 0 || index[i] >= size)
				throw new IndexOutOfBoundsException();
			
			valueList.remove(index[i]);
		}
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
	public FieldDelimiter getDelimiter() { return delimiter; }
	/** @see {@link #format} */
	public Format getFormat() { return format; }
	/** @see {@link #format} */
	public String getFormatValue() {
		if (format != null) {
			return format.value;
		} else
			return null;
	}
	/** @see {@link #parser} */
	public Parser getParser() { return parser; }
	/** @see {@link #optional} */
	public boolean isOptional() { return optional; }
	/** @see {@link #valueList} */
	public List<FieldValue> getValueList() { return valueList; }
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
