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
package com.foxling.graphit.logfile;

import java.util.List;

import com.foxling.graphit.Core;
import com.foxling.graphit.config.Field;

/** Class for storing log-line */
public class Line {
	/** Line number in the file */
	private int lineNo;
	
	/** Source string */
	private String source;
	
	/** Field values */
	private Object[] fieldValues;
	
	/** Hash sum is correct */
	private boolean authentic;
	
	/**
	 * @param lineNo - номер строки в файле
	 * @param sourceString - исходная строка
	 * @throws IllegalArgumentException
	 */
	public Line(int lineNo, String sourceString) throws IllegalArgumentException {
		if (sourceString == null)
			throw new IllegalArgumentException();
		
		this.lineNo = lineNo;
		this.source = sourceString;
		this.fieldValues = new Object[Core.getConfigModel().getFieldSetSize()];
	}
	
	/** Returns line number */
	public int getLineNo() {
		return lineNo;
	}
	
	/** Returns the source string */
	public String getSource() {
		return source;
	}

	public boolean isAuthentic() {
		return authentic;
	}

	public void setAuthentic(boolean authentic) {
		this.authentic = authentic;
	}
	
	public Object getValue(int fieldNo) {
		return fieldValues[fieldNo];
	}
}
