/* Graphit - log file browser
 * Copyright© 2015 Shamil Absalikov, foxling@live.com
 *
 * Graphit is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Graphit is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package ru.foxling.graphit.config;

import java.awt.Color;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EventListener;
import java.util.List;
import javax.swing.event.EventListenerList;

public class Field
implements Serializable {
	private static final long serialVersionUID = 2916573767059483325L;
	
	/** Global field counter */
	private static int gCounter;
	
	/** Current field's id, given by global field counter */
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
	
	/** <code>True</code> if the field contains hash sum of the log line */
	private boolean hashsum;
	
	/** The field role... what to do with the field's data */
	private FieldRole role;
	
	/** The color of the line <br>
	 * *If we gonna draw this data */
	private Color color;
	
	/** Contains values that could appear in the field and its' descriptions */
	private List<FieldValue> valueList;
	
	/** Object that converts string to field's {@link #datatype} */
	private Parser<?> parser;
	
	private boolean valid;

	protected EventListenerList listenerList = new EventListenerList();
	
	public Field() {
		id = ++Field.gCounter;
		name = String.format("<Поле #%d>", id);
		description = "";
		datatype = DataType.getDefaultDataType();
		format = datatype.getDefaultFormat();
		delimiter = FieldDelimiter.getDefaultFieldDelimiter();
		valueList = new ArrayList<FieldValue>(5);
		parser = DefaultParser.getDefaultParser(datatype, format);
		role = FieldRole.getDefaultFieldState();
		setValid(false);
	}
	
	
	/* ---=== NAME ===--- */

	/** @see {@link #name} */
	public String getName() { return name; }
	
	/** @see {@link #name} */
	public void setName(String name) throws IllegalArgumentException {
		if (name == null || name.equals(""))
			throw new IllegalArgumentException("Имя не должно быть пустым");
		
		if (name.equals(this.name))
			return;
		
		this.name = name;
		fireFieldChanged(new FieldEvent(this, FieldEvent.UPDATE, "name"));
	}
	
	
	/* ---=== DESCRIPTION ===---*/
	
	/** @see {@link #description} */
	public String getDescription() { return description; }
	
	/** @see {@link #description} */
	public void setDescription(String description) {
		if (description == null)
			setDescription("");
		
		if (description.equals(this.description))
			return;
		
		this.description = description;
		fireFieldChanged(new FieldEvent(this, FieldEvent.UPDATE, "description"));
	}
	
	
	/* ---=== DATATYPE ===--- */
	/** @see {@link #datatype} */
	public DataType getDatatype() { return datatype; }
	
	/** @see {@link #datatype} */
	public void setDatatype(DataType datatype) throws IllegalArgumentException {
		if (datatype == null)
			throw new IllegalArgumentException("Тип данных не должен быть пустым");
		
		if (this.datatype.equals(datatype))
			return;
		
		this.datatype = datatype;
		fireFieldChanged(new FieldEvent(this, FieldEvent.UPDATE, "datatype"));
		
		if (datatype.getFormatList().indexOf(getFormat()) == -1)
			setFormat(datatype.getDefaultFormat());
		
		setParser(DefaultParser.getDefaultParser(this.datatype, this.format));
	}
	
	
	/* ---=== FORMAT ===--- */
	
	/** @see {@link #format} */
	public Format getFormat() { return format; }
	
	/** @see {@link #format} */
	public void setFormat(String format) throws IllegalStateException {
		if (datatype == null)
			throw new IllegalStateException("Тип данных - пустой");
		
		if (format == null || format.equals("")) {
			setFormat(datatype.getDefaultFormat());
		} else {
			Format f = datatype.indexOfFormatV(format);
			if (f == null) {
				if (!datatype.isFixedFormatList()) {
					String caption = "*[" + getName() + "]";
					
					// first, trying to find the same format  
					f = datatype.indexOfFormatV(format);
					if (f == null) {
						f = datatype.indexOfFormatC(caption);
						if (f != null)
							f.value = format;
					}
					
					// if haven't succeeded, making a new one
					if (f == null) {
						f = new Format(caption, format);
						datatype.getFormatList().add(f);
					}
				} else
					throw new IllegalStateException(String.format("У типа данных %s фиксированный набор форматов и формата \"%s\" в нём нет", datatype.getCaption(), format));
			}
			
			setFormat(f);
		}
	}
	
	/** @see {@link #format} */
	public void setFormat(Format format) throws IllegalArgumentException {
		if (format == null)
			throw new IllegalArgumentException("Попытка установить NULL-формат");
			
		if (this.format.equals(format))
			return;
		
		this.format = format;
		fireFieldChanged(new FieldEvent(this, FieldEvent.UPDATE, "format"));
		setParser(DefaultParser.getDefaultParser(this.datatype, this.format));
	}
	
	/** @see {@link #format} */
	public String getFormatValue() {
		if (format != null) {
			return format.value;
		} else
			return null;
	}
	
	
	/* ---=== DELIMITER ===--- */
	
	/** @see {@link #delimiter} */
	public FieldDelimiter getDelimiter() { return delimiter; }
	
	/** @see {@link #delimiter} */
	public void setDelimiter(FieldDelimiter delimiter) {
		if (delimiter == null)
			throw new IllegalArgumentException("Ограничитель не должен быть пустым");
		
		this.delimiter = delimiter;
		fireFieldChanged(new FieldEvent(this, FieldEvent.UPDATE, "delimiter"));
	}
	
	
	/* ---=== OPTIONAL ===--- */
	
	/** @see {@link #optional} */
	public boolean isOptional() { return optional; }
	
	/** @see {@link #optional} */
	public void setOptional(boolean optional) {
		if (optional == this.optional)
			return;
		
		this.optional = optional;
		fireFieldChanged(new FieldEvent(this, FieldEvent.UPDATE, "optional"));
	}
	
	
	/* ---=== BITMASK ===--- */
	
	/** @see {@link #bitmask} */
	public boolean isBitmask() { return bitmask; }
	
	/** @see {@link #bitmask} */
	public void setBitmask(boolean bitmask) {
		if (this.bitmask == bitmask)
			return;
		
		this.bitmask = bitmask;
		fireFieldChanged(new FieldEvent(this, FieldEvent.UPDATE, "bitmask"));
	}
	
	
	/* ---=== HASHSUM ===--- */
	
	/** @see {@link #hashsum} */
	public boolean isHashsum() { return hashsum; }
	
	/** @see {@link #hashsum} */
	public void setHashsum(boolean hashsum) {
		if (this.hashsum == hashsum)
			return;
		
		this.hashsum = hashsum;
		fireFieldChanged(new FieldEvent(this, FieldEvent.UPDATE, "hashsum"));
	}
	
	
	/* ---=== ROLE ===--- */
	
	/** @see {@link #role} */
	public FieldRole getRole() { return role; }
	
	/** @see {@link #role} */
	public void setRole(FieldRole role) throws IllegalArgumentException, IllegalStateException {
		if (role == null)
			throw new IllegalArgumentException("Состояние не может быть NULL");
		
		if (this.role == role)
			return;
		
		this.role = role;
		fireFieldChanged(new FieldEvent(this, FieldEvent.UPDATE, "role"));
	}
	
	
	/* ---=== COLOR ===--- */
	
	public Color getColor() { return color;	}
	
	public void setColor(Color color) {
		if (this.color != null && this.color.equals(color))
			return;
		
		this.color = color;
		fireFieldChanged(new FieldEvent(this, FieldEvent.UPDATE, "color"));
	}
	
	
	/* ---=== PARSER ===--- */

	/** @see {@link #parser} */
	public Parser<?> getParser() { return parser; }
	
	/** @see {@link #parser} */
	public void setParser(Parser<?> parser) {
		if (parser.equals(this.parser))
			return;
		
		this.parser = parser;
		fireFieldChanged(new FieldEvent(this, FieldEvent.UPDATE, "parser"));
	}
	
	
	/* ---=== VALID ===--- */
	
	public boolean isValid() {
		return valid;
	}


	public void setValid(boolean valid) {
		if (this.valid == valid)
			return;
		
		this.valid = valid;
		fireFieldChanged(new FieldEvent(this, FieldEvent.UPDATE, "valid"));
	}


	/* ---=== FIELD VALUES ===--- */
	
	/** @see {@link #valueList} */
	public List<FieldValue> getValueList() { return valueList; }
	
	public void addValue(FieldValue value) throws IllegalArgumentException, IndexOutOfBoundsException {
		if (value == null)
			throw new IllegalArgumentException("Попытка вставить null-значение поля");
		
		addValueAt(valueList.size(), value);
	}
	
	public void addValueAt(int index, FieldValue value) throws IllegalArgumentException, IndexOutOfBoundsException {
		if (value == null)
			throw new IllegalArgumentException("Попытка вставить null-значение поля");
		
		if (index < 0 || index > valueList.size())
			throw new IndexOutOfBoundsException(String.format("Попытка вставить значение поля в некорректную позицию (%d)", index));
		
		valueList.add(index, value);
		fireFieldChanged(new FieldEvent(this, FieldEvent.INSERT, "valueList"));
	}
	
	public void removeValues(int[] index) throws IndexOutOfBoundsException, NullPointerException {
		if (index.length == 0)
			return;
		
		Arrays.sort(index);
		for (int i = index.length - 1; i >= 0; i--) {
			if (index[i] < 0 || index[i] >= valueList.size())
				throw new IndexOutOfBoundsException();
			
			valueList.remove(index[i]);
		}
		
		fireFieldChanged(new FieldEvent(this, FieldEvent.DELETE, "valueList"));
	}
	
	
	/* ---=== EVENTS ===--- */	

	public void addFieldListener(FieldListener listener) {
		listenerList.add(FieldListener.class, listener);
	}
	
	public void removeFieldListener(FieldListener listener) {
		listenerList.remove(FieldListener.class, listener); 
	}
	
	public <T extends EventListener> T[] getListeners(Class<T> listenerType) {
		return listenerList.getListeners(listenerType);
	}
	
	public void fireFieldChanged(FieldEvent evt) {
		for (FieldListener listener : getListeners(FieldListener.class))
			listener.fieldChanged(evt);
	}
	
	
	/* ---=== STUFF ===--- */
	
	@Override
	public String toString() { return name; }
		
	/** Serialization customization */
	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException{
		in.defaultReadObject();
		
		DataType type = getDatatype(); 
		/* DateTimeFormatter - isn't serializable.. gotta recreate parsers */
		if (type == DataType.DATE || type == DataType.TIME || type == DataType.DATETIME)
			setParser(DefaultParser.datetimeFactory(type, getFormat()));
	}
	
}