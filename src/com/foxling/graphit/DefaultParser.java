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
import java.util.Date;

public class DefaultParser {
	public static final Parser<Boolean> BOOLEAN = new Parser<Boolean>() {
		@Override
		public Boolean parse(String str) {
			if (str == null)
				throw new NullPointerException("Cannot parse null");
			
			return Boolean.parseBoolean(str);
		}
	};
	
	public static final Parser<Integer> INTEGER = new Parser<Integer>() {
		@Override
		public Integer parse(String str) throws ParseException, NullPointerException {
			if (str == null)
				throw new NullPointerException("Cannot parse null");
			
			try {
				return Integer.parseInt(str);
			} catch (NumberFormatException e) {
				throw new ParseException(str, 0);
			}
		}
	};
	
	public static final Parser<Float> FLOAT = new Parser<Float>() {
		@Override
		public Float parse(String str) throws ParseException, NullPointerException {
			if (str == null)
				throw new NullPointerException("Cannot parse null");
			
			try {
				return Float.parseFloat(str);
			} catch (NumberFormatException e) {
				throw new ParseException(str, 0);
			}
		}
	};

	public static final Parser<Double> DOUBLE = new Parser<Double>() {
		@Override
		public Double parse(String str) throws ParseException, NullPointerException {
			if (str == null)
				throw new NullPointerException("Cannot parse null");
			
			try {
				return Double.parseDouble(str);
			} catch (NumberFormatException e) {
				throw new ParseException(str, 0);
			}
		}
	};

	public static final Parser<String> STRING = new Parser<String>() {
		@Override
		public String parse(String str) throws NullPointerException {
			if (str == null)
				throw new NullPointerException("Cannot parse null");
			return str;
		}
	};
	
	/** Date parser factory
	 * @param format - Date/Time format pattern, for example: <code>"dd.MM.YYYY HH:mm:ss"</code>
	 * @see {@link java.text.SimpleDateFormat} */
	public static Parser<Date> datetime(final String format) {
		return new Parser<Date>() {
			private SimpleDateFormat f = new SimpleDateFormat(format);
			
			@Override
			public Date parse(String str) throws ParseException, NullPointerException {
				if (str == null)
					throw new NullPointerException("Cannot parse null");
				return f.parse(str);
			}
		};
	}
}
