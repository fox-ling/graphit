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

package ru.foxling.graphit.ui;

import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

public class BlankTableModel implements TableModel {
	private static final BlankTableModel instance = new BlankTableModel();
	
	private BlankTableModel() {}
	
	public static BlankTableModel instance() {
		return instance;
	}
	
	@Override
	public int getRowCount() {
		return 0;
	}
	
	@Override
	public int getColumnCount() {
		return 0;
	}
	
	@Override
	public String getColumnName(int columnIndex) {
		return null;
	}
	
	@Override
	public Class<?> getColumnClass(int columnIndex) {
		return null;
	}
	
	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return false;
	}
	
	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		return null;
	}
	
	@Override
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {}
	
	@Override
	public void addTableModelListener(TableModelListener l) {}
	
	@Override
	public void removeTableModelListener(TableModelListener l) {}
}
