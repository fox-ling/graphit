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
package com.foxling.graphit.config;

import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.ListModel;

import com.foxling.graphit.Field;

public class ConfigController {
	private ConfigModel model;
	private JList<Field> list;
	private ListModel<Field> listModel;
	
	public ConfigController(JList<Field> list, ConfigModel model) {
		this.list = list;
		listModel = new AbstractListModel<Field>(){
			private static final long serialVersionUID = -6222628541194781163L;

			@Override
			public Field getElementAt(int index) {
				return model.getField(index);
			}

			@Override
			public int getSize() {
				return model.getFieldSetSize();
			}
		};
		list.setModel(listModel);
		list.repaint();
	}
	
}
