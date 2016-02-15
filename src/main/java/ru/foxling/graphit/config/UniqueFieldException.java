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


/** The <code>UniqueFieldException</code> raises 
 * when a field tries to get an unique property*
 * that already taken by another one.<br>
 * <i>*When only one field in the field list can has the property</i> */ 
public class UniqueFieldException
extends Exception {
	private static final long serialVersionUID = 5531804567945196861L;
	
	/** The field that being checked (want to get the unique property) */
	private final Field field;
	
	/** Primal field, that already having the unique property */
	private final Field primalField;
	
	/** Name of the property that makes a field unique */
	private final String uniqueProperty;
	
	/**
	 * @param field the field that being checked (want to get a unique status) 
	 * @param primalField the field that already having unique property
	 * @param uniqueProperty name of the property that makes a field unique
	 */
	public UniqueFieldException(Field field, Field primalField, String uniqueProperty) {
		this(null, field, primalField, uniqueProperty);
	}
	
	/**
	 * @param field the field that being checked (want to get a unique status) 
	 * @param primalField the field that already having unique property
	 * @param uniqueProperty name of the property that makes a field unique
	 */
	public UniqueFieldException(String msg, Field field, Field primalField, String uniqueProperty) {
		super(msg);
		this.field = field;
		this.primalField = primalField;
		this.uniqueProperty = uniqueProperty;
	}
	
	/** Returns the field that being checked (want to get a unique status) */
	public Field getField() {
		return field;
	}
	
	/** Returns the field that already having unique property */
	public Field getPrimalField() {
		return primalField;
	}
	
	/** Returns name of the property that makes a field unique */
	public String getUniqueProperty() {
		return uniqueProperty;
	}
}
