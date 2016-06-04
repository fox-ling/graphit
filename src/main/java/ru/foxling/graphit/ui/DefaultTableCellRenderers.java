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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

import ru.foxling.graphit.Core;
import ru.foxling.graphit.config.DataType;

public class DefaultTableCellRenderers {
	private static Map<DataType, TableCellRenderer> map = new HashMap<>(6); 
	
	public static TableCellRenderer forDataType(DataType datatype) {
		if (map.containsKey(datatype)) {
			return map.get(datatype);
		} else {
			DefaultTableCellRenderer renderer;
			
			switch (datatype) {
			case DATETIME:
			case TIME_SEQUENCE:
				renderer = new DefaultTableCellRenderer() {
					private static final long serialVersionUID = 9123910403864393934L;
					
					@Override
					protected void setValue(Object value) {
						if (value == null) {
							setText("");
						} else
							setText(((LocalDateTime) value).format(Core.F_DATETIME));
					}
				};
				break;
			case DATE:
				renderer = new DefaultTableCellRenderer() {
					private static final long serialVersionUID = 1L;

					@Override
					protected void setValue(Object value) {
						if (value == null) {
							setText("");
						} else
							setText(((LocalDate) value).format(Core.F_DATE));
					}
				};
				break;
			case TIME:
				renderer = new DefaultTableCellRenderer() {
					private static final long serialVersionUID = 1L;
					
					@Override
					protected void setValue(Object value) {
						if (value == null) {
							setText("");
						} else
							setText(((LocalTime) value).format(Core.F_TIME));
					}
				};
				break;
			case STRING: 
				renderer = new DefaultTableCellRenderer();
				break;
			default: // Unlikely if someone gonna need a custom string renderer, so let's store default renderer in that key
				return forDataType(DataType.STRING);
			}
			renderer.setHorizontalAlignment(SwingConstants.CENTER);
			map.put(datatype, renderer);
			return renderer;
		}
	}
}
