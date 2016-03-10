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

package ru.foxling.graphit.ui;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.table.DefaultTableCellRenderer;

import ru.foxling.graphit.config.Field;
import ru.foxling.graphit.config.FieldValue;

public class FieldValueRenderer
extends DefaultTableCellRenderer {
	private static final Logger LOG = Logger.getLogger(FieldValueRenderer.class.getName());
    private static final long serialVersionUID = 4264832765857567868L;
    private Field field;
    
	public FieldValueRenderer(Field field) {
		super();
		this.field = field;
	}

    public void setValue(Object value) {
    	if (value == null) {
    		setText("");
    		return;
    	}
    	setText(value.toString());
    	
    	if (!field.isBitmask()) {
    		for (FieldValue fValue : field.getValueList())
    			if (fValue.value.equals(value)) {
    				if (fValue.caption != null) setText(fValue.caption);
    				if (fValue.description != null) setToolTipText(fValue.source + ": " + fValue.description);
					return;
				}
    	} else {
			try {
				StringBuilder result = new StringBuilder();
				int iVal = objectToInt(value);
				if (iVal == 0)
					return;
				
				for (FieldValue fValue : field.getValueList()) {
					if (fValue.value.equals(value)) {
						if (fValue.caption != null) setText(fValue.caption);
	    				if (fValue.description != null) setToolTipText(fValue.source + ": " + fValue.description);
						return;
					}
					
					int ifVal = objectToInt(fValue.value);
					if ((iVal & ifVal) > 0 && fValue.description != null && !fValue.description.isEmpty())
						result.append(fValue.source).append(": ").append(fValue.description).append("<br>");
				}

				if (result.length() != 0) {
					setToolTipText(result.insert(0, "<html>").append("</html>").toString());
				}
			} catch (Exception e) {
				LOG.log(Level.WARNING, "Ошибка при попытке собрать всплывающую подсказку", e);
				setText("");
				return;
			}
		}
    }

    /** Converts object representation of a whole number to int.
	 * @param num the object
	 * @return <code>intValue()</code> or <code>0</code> */
	private int objectToInt(Object num) {
		if (num == null)
			return 0;
		
		if (num instanceof Byte) {
			return ((Byte) num).intValue();
		} else if (num instanceof Short) {
			return ((Short) num).intValue();
		} else if (num instanceof Integer) {
			return ((Integer) num).intValue();
		}
		
		return 0;
	}
    
}
