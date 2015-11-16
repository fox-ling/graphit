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
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Date;

public class DefaultParser {
	public static final Parser<Boolean> BOOLEAN = new Parser<Boolean>() {
		@Override
		public Boolean parse(String str) {
			return Boolean.parseBoolean(str);
		}
	};
	
	public static final Parser<Float> FLOAT = new Parser<Float>() {
		@Override
		public Float parse(String str) throws NullPointerException, NumberFormatException {
			return Float.parseFloat(str);
		}
	};

	public static final Parser<Double> DOUBLE = new Parser<Double>() {
		@Override
		public Double parse(String str) throws NullPointerException, NumberFormatException {
			return Double.parseDouble(str);
		}
	};

	public static final Parser<String> STRING = new Parser<String>() {
		@Override
		public String parse(String str) throws NullPointerException {
			if (str == null)
				throw new NullPointerException();
			return str;
		}
	};
	
	public static Parser numericFactory(DataType datatype, final String format) throws IllegalArgumentException {
		final int _radix;
		final String _prefix;
		switch (format) {
		case "HEX":
			_radix = 16;
			_prefix = "0x";
			break;
		case "OCT":
			_radix = 8;
			_prefix = "0o";
			break;
		case "BIN":
			_radix = 2;
			_prefix = "0b";
			break;
		default: // DEC
			_radix = 10;
			_prefix = "";
		}
		
		switch (datatype) {
		case BYTE:
			return new Parser<Byte>(){
				private final int RADIX = _radix;
				private final String PREFIX = _prefix;
				
				@Override
				public Byte parse(String str) throws NullPointerException, NumberFormatException {
					str = checkNumeric(str, PREFIX);
					return Byte.parseByte(str, RADIX);
				}
			};
		case SHORT:
			return new Parser<Short>(){
				private final int RADIX = _radix;
				private final String PREFIX = _prefix;
				
				@Override
				public Short parse(String str) throws NullPointerException, NumberFormatException {
					str = checkNumeric(str, PREFIX);
					return Short.parseShort(str, RADIX);
				}
			};
		case INTEGER:
			return new Parser<Integer>(){
				private final int RADIX = _radix;
				private final String PREFIX = _prefix;
				
				@Override
				public Integer parse(String str) throws NullPointerException, NumberFormatException {
					str = checkNumeric(str, PREFIX);
					return Integer.parseInt(str, RADIX);
				}
			};
		default:
			throw new IllegalArgumentException("Неподдерживаемый числовой тип данных - " + datatype);
		}
	}

	private static String checkNumeric(String str, String prefix) throws NullPointerException {
		if (!prefix.equals("") && str.startsWith(prefix)) {
			return str.substring(2);
		} else
			return str;
	}
	
	/** Date parser factory
	 * @param format - Date/Time format pattern, for example: <code>"dd.MM.YYYY HH:mm:ss"</code>
	 * @see {@link java.text.SimpleDateFormat} */
	public static Parser datetimeFactory(DataType datatype, final String format) throws IllegalArgumentException {
		DateTimeFormatter _formatter = DateTimeFormatter.ofPattern(format);
		
		switch (datatype) {
		case DATE:
			return new Parser<LocalDate>() {
				private final DateTimeFormatter F = _formatter;
				
				@Override
				public LocalDate parse(String str) throws DateTimeParseException {
					return LocalDate.parse(str, F);
				}
			};
		case TIME:
			return new Parser<LocalTime>() {
				private final DateTimeFormatter F = _formatter;
				
				@Override
				public LocalTime parse(String str) throws DateTimeParseException {
					return LocalTime.parse(str, F);
				}
			};
		case DATETIME:
			return new Parser<LocalDateTime>() {
				private final DateTimeFormatter F = _formatter;
				
				@Override
				public LocalDateTime parse(String str) throws DateTimeParseException {
					return LocalDateTime.parse(str, F);
				}
			};
		default:
			throw new IllegalArgumentException("Неподдерживаемый тип данных даты/времени - " + datatype);
		}
		
		
	}
	
	public static Parser getDefaultParser(DataType datatype, String format) throws IllegalArgumentException{
		if (format == null) 
				format = "";
		switch (datatype) {
			case BOOLEAN:
				return DefaultParser.BOOLEAN;
			case BYTE:
			case SHORT:
			case INTEGER:
				return DefaultParser.numericFactory(datatype, format);
			case FLOAT:
				return DefaultParser.FLOAT;
			case DOUBLE:
				return DefaultParser.DOUBLE;
			case STRING:
				return DefaultParser.STRING;
			case DATE:
			case TIME:
			case DATETIME:
				if (format == "")
					throw new IllegalArgumentException("Формат даты не предоставлен");
				return DefaultParser.datetimeFactory(datatype, format);
			default:
				throw new IllegalArgumentException("Не удалось подобрать парсер по умолчанию - неподдерживаемый тип данных (" + datatype + ")");
		}
	}
}
