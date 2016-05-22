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

/** Class for storing log-records */
public class BadRecord {
	/** Line number in the file */
	private final int lineno;
	
	/** Source string */
	private final String sourceStr;
	
	/** Parse error description */
	private final String errorMsg;
	
	/** Start position of the bad segment */
	private final int errorOffset;
	
	/** Length of the bad segment */
	private final int errorLength;
	
	/** Index of the field where error has been encountered */
	private final int fieldId;
	
	/**
	 * @param lineno - text file's line number
	 * @param sourceStr - text file's line
	 * @throws IllegalArgumentException
	 */
	public BadRecord(int lineno, String sourceStr, String errorMsg, int errorOffset, int errorLength, int fieldId) throws IllegalArgumentException {
		if (sourceStr == null)
			throw new IllegalArgumentException();
		
		this.lineno = lineno;
		this.sourceStr = sourceStr;
		this.errorMsg = errorMsg;
		this.errorOffset = errorOffset;
		this.errorLength = errorLength;
		this.fieldId = fieldId;
	}
	
	/** Returns line number */
	public int getLineno() {
		return lineno;
	}
	
	/** Returns the source string */
	public String getSourceStr() {
		return sourceStr;
	}
	
	/** Returns parse error description */
	public String getErrorMsg() {
		return errorMsg;
	}

	/** The method returns start position of the bad segment */
	public int getErrorOffset() {
		return errorOffset;
	}

	/** The method returns length of the bad segment */
	public int getErrorLength() {
		return errorLength;
	}

	/** Returns index of the field where error has been encountered */
	public int getFieldId() {
		return fieldId;
	}
}
