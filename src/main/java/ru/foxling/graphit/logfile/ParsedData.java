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

package ru.foxling.graphit.logfile;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Period;
import java.time.ZoneOffset;
import java.util.BitSet;
import gnu.trove.list.array.TByteArrayList;
import gnu.trove.list.array.TDoubleArrayList;
import gnu.trove.list.array.TFloatArrayList;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.list.array.TLongArrayList;
import gnu.trove.list.array.TShortArrayList;
import ru.foxling.graphit.config.ConfigModel;
import ru.foxling.graphit.config.DataType;
import ru.foxling.graphit.config.Field;

public class ParsedData {
	private ConfigModel config;
	private Charset charset;
	private BitSet authentic;
	private Object[] data;
	private int size;
	
	public ParsedData(ConfigModel config, Charset charset) throws IllegalStateException {
		this.config = config;
		this.charset = charset;
		
		authentic = new BitSet();
		data = new Object[config.getFieldList().size()];
		for (int i = 0; i < config.getFieldList().size(); i++) {
			Field field = config.getFieldList().get(i);
			switch (field.getDatatype()) {
			case BOOLEAN: 	data[i] = new BitSet(); break;
			case BYTE: 		data[i] = new TByteArrayList(); break;
			case SHORT: 	data[i] = new TShortArrayList(); break;
			case INTEGER: 	data[i] = new TIntArrayList(); break;
			case FLOAT: 	data[i] = new TFloatArrayList(); break;
			case DOUBLE: 	data[i] = new TDoubleArrayList(); break;
			case DATE: 		data[i] = new TLongArrayList(); break;
			case DATETIME: 	data[i] = new TLongArrayList(); break;
			case TIME: 		data[i] = new TIntArrayList(); break;
			case OVERFLOWING_TIME_SEQUENCE: data[i] = new TimeSequence(); break;
			case STRING: 	data[i] = new TStringList(charset); break;
			default:
				data = null;
				throw new IllegalStateException(String.format("Невозможно создать модель %s. Поддержка типа %s.%s не реализована в %s",
						ParsedData.class.getSimpleName(),
						DataType.class.getName(),
						field.getDatatype().getValue(),
						ParsedData.class.getName()
				));
			}
		}
	}
	
	public int size() {
		return size;
	}
	
	public void addRecord(Object[] values, boolean authentic) throws IllegalArgumentException, IllegalStateException {
		if (values.length != config.getFieldList().size())
			throw new IllegalArgumentException("Несоответствие количеств полей в настройках и в массиве-агрументе");
		
		size++;
		for (int col = 0; col < values.length; col++) {
			Object value = values[col];
			Field field = config.getFieldList().get(col);
			switch (field.getDatatype()) {
			case BOOLEAN:
				if (value.equals(Boolean.TRUE)) {
					((BitSet)data[col]).set(size + 1);
				}
				break;
			case BYTE: {
				Byte val = (Byte) value;
				((TByteArrayList)data[col]).add(val == null ? 0 : val.byteValue());
				break;
			}
			case SHORT: {
				Short val = (Short) value;
				((TShortArrayList)data[col]).add(val == null ? 0 : val.shortValue());
				break;
			}
			case INTEGER: {
				Integer val = (Integer) value;
				((TIntArrayList)data[col]).add(val == null ? 0 : val.intValue());
				break;
			}
			case FLOAT:	{
				Float val = (Float) value;
				((TFloatArrayList)data[col]).add(val == null ? 0 : val.floatValue());
				break;
			}
			case DOUBLE: {
				Double val = (Double) value;
				((TDoubleArrayList)data[col]).add(val == null ? 0 : val.doubleValue());
				break;
			}
			case DATE: {
				LocalDate val = (LocalDate) value;
				((TLongArrayList)data[col]).add(val == null ? Long.MIN_VALUE : val.toEpochDay());
				break;
			}
			case DATETIME: {
				LocalDateTime val = (LocalDateTime) value;
				((TLongArrayList)data[col]).add(val == null ? Long.MIN_VALUE : val.toEpochSecond(ZoneOffset.UTC));
				break;
			}
			case TIME: {
				LocalTime val = (LocalTime) value;
				((TIntArrayList)data[col]).add(val == null ? Integer.MIN_VALUE : val.toSecondOfDay());
				break;
			}
			case OVERFLOWING_TIME_SEQUENCE:
				((TimeSequence)data[col]).add((LocalTime) value);
				break;
			case STRING:
				try {
					((TStringList)data[col]).add((String)value);
				} catch (UnsupportedEncodingException e) {
					rollback();
					throw new IllegalStateException(String.format("Невозможно добавить значение %s в поле #%d. Кодировка строки не соответствует %s",
							value.toString(),
							col,
							charset.name()
					));
				}
				break;
			default:
				rollback();
				throw new IllegalStateException(String.format("Невозможно добавить значение %s в поле #%d. Поддержка типа %s.%s не реализована в %s",
						value.toString(),
						col,
						DataType.class.getName(),
						field.getDatatype().getValue(),
						ParsedData.class.getName()
				));
			}
		}
	}
	
	public Object getValue(int row, int col) {
		Field field = config.getFieldList().get(col);
		switch (field.getDatatype()) {
		case BOOLEAN:	return ((BitSet)data[col]).get(row);
		case BYTE:		return ((TByteArrayList)data[col]).get(row);
		case SHORT:		return ((TShortArrayList)data[col]).get(row);
		case INTEGER:	return ((TIntArrayList)data[col]).get(row);
		case FLOAT:		return ((TFloatArrayList)data[col]).get(row);
		case DOUBLE:	return ((TDoubleArrayList)data[col]).get(row);
		case DATE: {
			long value = ((TLongArrayList)data[col]).get(row);
			return value == Long.MIN_VALUE ? null : LocalDate.ofEpochDay(value);
		}
		case DATETIME: {
			long value = ((TLongArrayList)data[col]).get(row);
			return value == Long.MIN_VALUE ? null : LocalDateTime.ofEpochSecond(value, 0, ZoneOffset.UTC);
		}
		case TIME: {
			int value = ((TIntArrayList)data[col]).get(row);
			return value == Integer.MIN_VALUE ? null : LocalTime.ofSecondOfDay(value);
		}
		case OVERFLOWING_TIME_SEQUENCE: return ((TimeSequence)data[col]).getDateTime(row);
		case STRING: return ((TStringList)data[col]).get(row);
		default:
			data = null;
			throw new IllegalStateException(String.format("Невозможно получить значение из поля #%d. Поддержка типа %s.%s не реализована в %s",
					col,
					DataType.class.getName(),
					field.getDatatype().getValue(),
					ParsedData.class.getName()
			));
		}
	}
	
	/** Performs a binary search for <code>epochMilli</code> in the column #<code>xFieldId</code>
	 * @param xFieldId column to search (has to be a sorted <code>TLongArrayList</code>, <code>TIntArrayList</code>, or <code>TimeSequence</code>)
	 * @param localDateTime the value to search for
	 * @return the first index of the value, or -1 if it is not in the list. 
	 * @see {@link TimeSequence}, {@link TLongArrayList}, {@link TIntArrayList} */
	public int getRowId(int xFieldId, LocalDateTime datetime) {
		switch (config.getFieldList().get(xFieldId).getDatatype()) {
		case OVERFLOWING_TIME_SEQUENCE: {
			TimeSequence ts = (TimeSequence) data[xFieldId];
			int offset = Period.between(ts.getBeginDate(), datetime.toLocalDate()).getDays();
			return ts.binarySearch(offset * 86400 + datetime.toLocalTime().toSecondOfDay());
		}
		case TIME: {
			return ((TIntArrayList) data[xFieldId]).binarySearch(datetime.toLocalTime().toSecondOfDay());
		}
		case DATE: {
			return ((TLongArrayList) data[xFieldId]).binarySearch(datetime.toLocalDate().toEpochDay());
		}
		case DATETIME: {
			return ((TLongArrayList) data[xFieldId]).binarySearch(datetime.toEpochSecond(ZoneOffset.UTC));
		}
		default:
			throw new IllegalStateException(String.format("Type %s isn't supported by the method", data[xFieldId].getClass().getSimpleName()));
		}
		// TODO
	}

	public boolean isAuthentic(int index) {
		return authentic.get(index);
	}
	
	private void rollback() {
		size--;
		authentic.set(size - 1, authentic.size(), false);
		for (int col = 0; col < config.getFieldList().size(); col++) {
			Field field = config.getFieldList().get(col);
			switch (field.getDatatype()) {
			case BOOLEAN: {
				BitSet bitset = (BitSet)data[col];
				if (bitset.size() > size) {
					bitset.set(size - 1, bitset.size(), false);
				}
				break;
			}
			case BYTE: {
				TByteArrayList list = (TByteArrayList)data[col];
				if (list.size() > size) {
					list.remove(size - 1, list.size() - size);
				}
				break;
			}
			case SHORT: {
				TShortArrayList list = (TShortArrayList) data[col];
				if (list.size() > size) {
					list.remove(size - 1, list.size() - size);
				}
				break;
			}
			case INTEGER:
			case TIME: {
				TIntArrayList list = (TIntArrayList) data[col];
				if (list.size() > size) {
					list.remove(size - 1, list.size() - size);
				}
				break;
			}
			case FLOAT: {
				TFloatArrayList list = (TFloatArrayList) data[col];
				if (list.size() > size) {
					list.remove(size - 1, list.size() - size);
				}
				break;
			}
			case DOUBLE: {
				TDoubleArrayList list = (TDoubleArrayList) data[col];
				if (list.size() > size) {
					list.remove(size - 1, list.size() - size);
				}
				break;
			}
			case DATE:
			case DATETIME: {
				TLongArrayList list = (TLongArrayList) data[col];
				if (list.size() > size) {
					list.remove(size - 1, list.size() - size);
				}
				break;
			}
			case OVERFLOWING_TIME_SEQUENCE: {
				TimeSequence list = (TimeSequence) data[col];
				if (list.size() > size) {
					list.remove(size - 1, list.size() - size);
				}
				break;
			}
			case STRING: {
				TStringList list = (TStringList)data[col];
				if (list.size() > size) {
					list.remove(size - 1, list.size() - size);
				}
				break;
			}
			default:
				data = null;
				throw new IllegalStateException(String.format("Невозможно получить значение из поля #%d. Поддержка типа %s.%s не реализована в %s",
						col,
						DataType.class.getName(),
						field.getDatatype().getValue(),
						ParsedData.class.getName()
				));
			}
		}
	}
	
	/** Sets the current date for TimeSequence field */
	public void setCurrentDatetime(LocalDateTime datetime) {
		for (int i = 0; i < data.length; i++) {
			if (data[i] instanceof TimeSequence) {
				((TimeSequence) data[i]).setCurrentDatetime(datetime);
			}
		}
	}
}
