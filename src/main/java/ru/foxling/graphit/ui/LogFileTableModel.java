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

import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import ru.foxling.graphit.Core;
import ru.foxling.graphit.config.Field;
import ru.foxling.graphit.logfile.LogFile;
import ru.foxling.graphit.logfile.Record;

public class LogFileTableModel
extends AbstractTableModel {
	private static final long serialVersionUID = -6341608314922452350L;
	private List<Field> fieldList;
	
	/** Records index */
	private ArrayList<Record> index;
	
	public LogFileTableModel(LogFile logFile, boolean wrongLinesOnly) {
		super();
		fieldList = Core.getConfigModel().getFieldList();
		index = logFile.getRecords();
	}
	
	@Override
	public int getRowCount() {
		return index.size();
	}

	@Override
	public int getColumnCount() {
		return fieldList.size();
	}
	
	@Override
	public String getColumnName(int columnIndex) {
		return fieldList.get(columnIndex).getName();
	}
	
	@Override
	public Class<?> getColumnClass(int columnIndex) {
		return fieldList.get(columnIndex).getDatatype().get_class();
	}
	
	public Record getRecord(int index) {
		return this.index.get(index);
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		return getRecord(rowIndex).getValue(columnIndex);
	}
	
	@Override
    public boolean isCellEditable(int row, int column) {
       return false;
    }		
}