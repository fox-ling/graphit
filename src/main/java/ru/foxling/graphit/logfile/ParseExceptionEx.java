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

import java.text.ParseException;

public class ParseExceptionEx
extends ParseException {
	private static final long serialVersionUID = 5531804567945196861L;
	
	/** Length of the bad segment */
	private final int errorLength;
	
	/** Index of the bad field */
	private final int fieldId;
	
	/**
	 * @param msg say something what happened
	 * @param offset start position of the bad segment
	 * @param length length of the bad segment
	 */
	public ParseExceptionEx(String msg, int offset, int length) {
		super(msg, offset);
		this.errorLength = length;
		this.fieldId = -1;
	}

	/** 
	 * @param msg say something what happened
	 * @param offset start position of the bad segment
	 * @param length length of the bad segment
	 * @param fieldId field index
	 */
	public ParseExceptionEx(String msg, int offset, int length, int fieldId) {
		super(msg, offset);
		this.errorLength = length;
		this.fieldId = fieldId;
	}
	
	/** The method returns index of the bad field */
	public int getFieldId() {
		return fieldId;
	}

	/** The method returns length of the bad segment
	* @return the end
	*/
	public int getErrorLength() {
		return errorLength;
	}
}
