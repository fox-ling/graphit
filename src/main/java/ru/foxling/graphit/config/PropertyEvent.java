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

import java.util.EventObject;

public class PropertyEvent extends EventObject {
	/** {@value} */
	public static final int DELETE = -1;
	/** {@value} */
	public static final int INSERT = 1;
	/** {@value} */
	public static final int UPDATE = 0;
	
	private static final long serialVersionUID = 7587760715994607198L;
	private final String propertyName;
	private final String value;
	private final String xValue;
	/** Event type: {@link #DELETE}, {@link #INSERT}, {@link #UPDATE} */
	private final int type;
	
	
	public PropertyEvent(Object source, String propertyName, String value, int type) {
		super(source);
		this.propertyName = propertyName;
		this.value = value;
		this.xValue = null;
		this.type = type;
	}
	
	public PropertyEvent(Object source, String propertyName, String value, String xValue) {
		super(source);
		this.propertyName = propertyName;
		this.value = value;
		this.xValue = xValue;
		this.type = UPDATE;
	}

	public String getPropertyName() {
		return propertyName;
	}

	public String getValue() {
		return value;
	}

	public String getxValue() {
		return xValue;
	}

	/** Returns the event type {@link #type} */
	public int getType() {
		return type;
	}
}
