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
package ru.foxling.graphit.logfile;

import ru.foxling.graphit.Core;

/** Class for storing log-records */
public class Record {
	/** Line number in the file */
	private int lineno;
	
	/** Source string */
	private String sourceStr;
	
	/** Field values */
	private Object[] fieldValues;
	
	/** Hash sum is correct */
	private boolean authentic;
	
	/** Parse error if the record has encountered any */
	private ParseExceptionEx parseError;
	
	/** The record is dirty if has any parse error */
	private boolean dirty;
	
	/**
	 * @param lineno - номер строки в файле
	 * @param sourceStr - исходная строка
	 * @throws IllegalArgumentException
	 */
	public Record(int lineno, String sourceStr, int fieldsCount) throws IllegalArgumentException {
		if (sourceStr == null)
			throw new IllegalArgumentException();
		
		this.lineno = lineno;
		this.sourceStr = sourceStr;
		this.fieldValues = new Object[fieldsCount];
		this.dirty = false;
	}
	
	/** Returns line number */
	public int getLineno() {
		return lineno;
	}
	
	/** Returns the source string */
	public String getSourceStr() {
		return sourceStr;
	}

	public boolean isAuthentic() {
		return authentic;
	}

	public void setAuthentic(boolean authentic) {
		this.authentic = authentic;
	}
	
	public Object getValue(int fieldno) {
		return fieldValues[fieldno];
	}
	
	public void setValue(int fieldno, Object value) {
		fieldValues[fieldno] = value;
	}

	/**
	 * @return the parseError
	 */
	public ParseExceptionEx getParseError() {
		return parseError;
	}

	/**
	 * @param parseError the parseError to set
	 */
	public void setParseError(ParseExceptionEx parseError) {
		this.parseError = parseError;
		this.dirty = parseError != null;
	}

	/** The record is dirty if has any parse error
	 * @return the dirty status
	 */
	public boolean isDirty() {
		return dirty;
	}
}
