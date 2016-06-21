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
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.event.EventListenerList;

public class Field
implements Serializable {
	private static final long serialVersionUID = 2916573767059483325L;
	private static Logger LOG = Logger.getLogger(Field.class.getName());
	
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
	
	/** The color of the line on the plot <br>
	 * *If we gonna draw this data */
	private Color color;
	
	/** Contains values that could appear in the field and its' descriptions */
	private List<FieldValue> valueList;
	
	/** Object that converts string to field's {@link #datatype} */
	transient private Parser<?> parser;
	
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
	public String getName() {
		return name;
	}
	
	/** Sets the field's name
	 * @param name non NULL and non empty name 
	 * @see {@link #name} */
	public boolean setName(String name) {
		if (name != null && !name.isEmpty() && !name.equals(this.name)) {
			this.name = name;
			fireFieldChanged(new FieldEvent(this, FieldEvent.UPDATE, "name"));
			return true;
		}
		return false;
	}
	
	
	/* ---=== DESCRIPTION ===---*/
	
	/** @see {@link #description} */
	public String getDescription() {
		return description;
	}
	
	/** @see {@link #description} */
	public boolean setDescription(String description) {
		if (description == null) {
			description = "";
		}
		
		if (!this.description.equals(description)) {
			this.description = description;
			fireFieldChanged(new FieldEvent(this, FieldEvent.UPDATE, "description"));
			return true;
		}
		return false;
	}
	
	
	/* ---=== DATATYPE ===--- */
	
	/** @see {@link #datatype} */
	public DataType getDatatype() {
		return datatype;
	}
	
	/** @see {@link #datatype} */
	public boolean setDatatype(DataType datatype) {
		if (datatype == null) {
			throw new NullPointerException("Тип данных не должен быть пустым");
		}
		
		if (!this.datatype.equals(datatype)) {
			DataType xDataType = this.datatype;
			this.datatype = datatype;
			
			// Trying to validate the field's role (checking if we can leave it unchanged)
			if (!validateRole(role, false)) {
				// ...if not, trying to reset it
				FieldRole role0 = datatype == DataType.TIME_SEQUENCE ? FieldRole.X_AXIS : FieldRole.NONE;
				if (!validateRole(role0, false) || !setRole(role0)) {
					// ... if unable to do anything, then rolling back (... and then most likely something fishing is going on)
					this.datatype = xDataType;
					LOG.log(Level.WARNING, "Не удалось изменить тип данных поля из-за конфликта с ролью");
					return false;
				}
			}

			fireFieldChanged(new FieldEvent(this, FieldEvent.UPDATE, "datatype"));
			
			// If format isn't compatible with new datatype, then setting a datatype's default format 
			if (datatype.getFormatList().indexOf(format) == -1) {
				setFormat(datatype.getDefaultFormat());
			}
			
			setParser(DefaultParser.getDefaultParser(this.datatype, format));
			return true;
		}
		return false;
	}
	
	
	/* ---=== FORMAT ===--- */
	
	/** @see {@link #format} */
	public Format getFormat() { return format; }
	
	/** @see {@link #format} */
	public boolean setFormat(String format) throws IllegalStateException {
		if (datatype == null)
			throw new IllegalStateException("Тип данных - пустой");
		
		if (format == null || format.equals("")) {
			return setFormat(datatype.getDefaultFormat());
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
			
			return setFormat(f);
		}
	}
	
	/** @see {@link #format} */
	public boolean setFormat(Format format) throws IllegalArgumentException {
		if (format == null)
			throw new IllegalArgumentException("Попытка установить NULL-формат");
			
		if (this.format.equals(format))
			return false;
		
		this.format = format;
		fireFieldChanged(new FieldEvent(this, FieldEvent.UPDATE, "format"));
		setParser(DefaultParser.getDefaultParser(this.datatype, this.format));
		return true;
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
	public FieldDelimiter getDelimiter() {
		return delimiter;
	}
	
	/** @see {@link #delimiter} */
	public boolean setDelimiter(FieldDelimiter delimiter) {
		if (delimiter == null)
			throw new IllegalArgumentException("Ограничитель не должен быть пустым");
		
		if (this.delimiter.equals(delimiter))
			return false;
		
		this.delimiter = delimiter;
		fireFieldChanged(new FieldEvent(this, FieldEvent.UPDATE, "delimiter"));
		return true;
	}
	
	
	/* ---=== OPTIONAL ===--- */
	
	/** @see {@link #optional} */
	public boolean isOptional() { return optional; }
	
	/** @see {@link #optional} */
	public boolean setOptional(boolean optional) {
		if (this.optional == optional)
			return false;
		
		this.optional = optional;
		fireFieldChanged(new FieldEvent(this, FieldEvent.UPDATE, "optional"));
		return true;
	}
	
	
	/* ---=== BITMASK ===--- */
	
	/** @see {@link #bitmask} */
	public boolean isBitmask() { return bitmask; }
	
	/** @see {@link #bitmask} */
	public boolean setBitmask(boolean bitmask) {
		if (this.bitmask == bitmask)
			return false;
		
		this.bitmask = bitmask;
		fireFieldChanged(new FieldEvent(this, FieldEvent.UPDATE, "bitmask"));
		return true;
	}
	
	
	/* ---=== HASHSUM ===--- */
	
	/** @see {@link #hashsum} */
	public boolean isHashsum() {
		return hashsum;
	}
	
	/** @see {@link #hashsum} */
	public boolean setHashsum(boolean hashsum) {
		if (this.hashsum == hashsum) {
			return false;
		}
		
		this.hashsum = hashsum;
		fireFieldChanged(new FieldEvent(this, FieldEvent.UPDATE, "hashsum"));
		return true;
	}
	
	
	/* ---=== ROLE ===--- */
	
	/** Returns field's role
	 * @see {@link #role} */
	public FieldRole getRole() {
		return role;
	}
	
	/** Sets field's role
	 * @param role a role to set
	 * @return <code>true</code> if property changed
	 * @see {@link #role} */
	public boolean setRole(FieldRole role) {
		if (this.role != role && validateRole(role, true)) {
			this.role = role;
			fireFieldChanged(new FieldEvent(this, FieldEvent.UPDATE, "role"));
			if (role != FieldRole.DRAW) {
				setColor(null);
			}
			return true;
		}
		return false;
	}
	
	/** Checks if the provided <code>role</code> suits the field
	 * @param role a role to check
	 * @param trace log encountered exceptions or not 
	 * @return validity */
	public boolean validateRole(FieldRole role, boolean trace) {
		try {
			if (role == null) {
				throw new IllegalArgumentException("Роль не может быть пустая");
			}
			
			if (role == FieldRole.DRAW && datatype == DataType.STRING) {
				throw new IllegalStateException("Строковые данные нельзя поместить на график");
			}
			
			if (datatype == DataType.TIME_SEQUENCE && role != FieldRole.X_AXIS) {
				throw new IllegalStateException(String.format("Тип данных '%s' есть смысл использовать только для роли '%s'", DataType.TIME_SEQUENCE, FieldRole.X_AXIS));
			}
			
			if (role == FieldRole.X_AXIS &&
					!Arrays.asList(DataType.DATE, DataType.TIME, DataType.DATETIME, DataType.TIME_SEQUENCE).contains(datatype)) {
				throw new IllegalStateException("В качестве данных для оси X поддерживаются только временнЫе типы (date/time/datetime/time_sequence)");
			}
		} catch (Exception e) {
			if (trace) {
				LOG.log(Level.WARNING, e.getMessage());
			}
			return false;
		}
		return true;
	}
	
	
	/* ---=== COLOR ===--- */
	
	public Color getColor() {
		return color;
	}
	
	/** Sets field's color
	 * @param color a color to set
	 * @return <code>true</code> if property changed
	 * @see {@link #color} */
	public boolean setColor(Color color) {
		if (this.color != null && this.color.equals(color) || this.color == null && color == null) {
			return false;
		}
		
		if (getRole() != FieldRole.DRAW && color != null) {
			LOG.log(Level.WARNING, "Невозможно установить цвет для нерисуемого поля");
			return false;
		}
		
		this.color = color;
		fireFieldChanged(new FieldEvent(this, FieldEvent.UPDATE, "color"));
		return true;
	}
	
	
	/* ---=== PARSER ===--- */

	/** @see {@link #parser} */
	public Parser<?> getParser() {
		return parser;
	}
	
	/** @see {@link #parser} */
	public boolean setParser(Parser<?> parser) {
		if (parser.equals(this.parser)) {
			return false;
		}
		
		this.parser = parser;
		fireFieldChanged(new FieldEvent(this, FieldEvent.UPDATE, "parser"));
		return true;
	}
	
	
	/* ---=== VALID ===--- */
	
	public boolean isValid() {
		return valid;
	}


	public boolean setValid(boolean valid) {
		if (this.valid == valid)
			return false;
		
		this.valid = valid;
		fireFieldChanged(new FieldEvent(this, FieldEvent.UPDATE, "valid"));
		return true;
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
		fireFieldChanged(new FieldEvent(this, FieldEvent.UPDATE, "valueList"));
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
		
		fireFieldChanged(new FieldEvent(this, FieldEvent.UPDATE, "valueList"));
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
		
		// Parser - isn't serializable.. gotta recreate parsers
		DataType type = getDatatype(); 
		if (Arrays.asList(DataType.DATE, DataType.TIME, DataType.DATETIME, DataType.TIME_SEQUENCE).contains(type))
			setParser(DefaultParser.datetimeFactory(type, getFormat()));
	}
	
}