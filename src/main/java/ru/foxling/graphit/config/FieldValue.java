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

package ru.foxling.graphit.config;

import java.io.Serializable;

public class FieldValue
implements Serializable{
	private static final long serialVersionUID = 7149480690094544822L;

	/** Source string of value.<br>
	 * <i>*will be shown in the tooltip</i> */
	public String source;
	
	/** Parsed value */
	public Object value;

	/** Caption of the value.<br>
	 * <i>*short description, shown in the table</i> */
	public String caption;
	
	/** Full description of the <code>value</code>.<br>
	 * <i>*will be shown in the tooltip</i> */
	public String description;
	
	public FieldValue() { }
	
	public FieldValue(Object value){
		this.value = value;
		this.description = value.toString();
	}
	
	public FieldValue(Object value, String caption) {
		this.value = value;
		this.caption = caption;
	}
	
	public FieldValue(Object value, String caption, String source) {
		this.value = value;
		this.caption = caption;
		this.source = source;
	}
	
	public FieldValue(Object value, String caption, String description, String source) {
		this.value = value;
		this.caption = caption;
		this.description = description;
		this.source = source;
	}
	
	/** @throws Exception 
	 * @see {@link #valueList} */
	public void validateValue(Parser<?> parser) throws Exception {
		value = parser.parse(source);
	}
	
	public String toString(){
		if (caption != null) {
			return caption;
		} else if (description != null) {
			return description;
		} else if (value != null) {
			return value.toString();
		} else
			return null;
	}
}
