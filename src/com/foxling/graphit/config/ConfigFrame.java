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

import java.awt.EventQueue;
import java.util.List;
import java.util.logging.ConsoleHandler;
import java.util.logging.Logger;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.AbstractListModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JComboBox;
import javax.swing.JCheckBox;
import javax.swing.border.TitledBorder;
import com.foxling.graphit.Core;

import javax.swing.JTabbedPane;
import java.awt.FlowLayout;
import javax.swing.ButtonGroup;
import javax.swing.ComboBoxModel;
import javax.swing.JRadioButton;
import javax.swing.SwingConstants;
import net.miginfocom.swing.MigLayout;
import java.awt.Color;
import javax.swing.BoxLayout;
import java.awt.Component;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import java.awt.Dimension;
import javax.swing.JPopupMenu;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JMenuItem;
import java.awt.event.ActionEvent;

public class ConfigFrame extends JFrame {
	private static final long serialVersionUID = 3103016344816004897L;
	private JPanel contentPane;
	private JTextField iColumnName;
	private JComboBox<FieldDelimiter> iFieldDelimiter;		
	private JComboBox<DataType> iDataType;
	private JComboBox<Format> iFormat;
	private FieldListModel mdlFieldList;
	private JList<Field> iFieldList;
	private JMenuItem miAddField;
	private JMenuItem miRemoveField;
	private JMenuItem miAddValue;
	private JMenuItem miRemoveValue;
	private JTextField edtFormat;
	private JCheckBox iOptional;
	private JTable tValues;
	private JScrollPane spValues;
	
	public ConfigFrame() {
		super("Настройки");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 593, 400);
		
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(1, 1, 1, 1));
		setContentPane(contentPane);
		contentPane.setLayout(new BorderLayout(0, 0));
		
		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		contentPane.add(tabbedPane, BorderLayout.CENTER);
		
		JPanel pnlGeneral = new JPanel();
		tabbedPane.addTab("Общее", null, pnlGeneral, null);
		pnlGeneral.setLayout(new BorderLayout(0, 0));
		
		JPanel pnlConfigLocation = new JPanel();
		pnlConfigLocation.setBorder(new TitledBorder(null, "", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		pnlGeneral.add(pnlConfigLocation, BorderLayout.NORTH);
		pnlConfigLocation.setLayout(new FlowLayout(FlowLayout.LEADING, 5, 5));
		
		JLabel lblConfigLocation = new JLabel("Хранилище настроек:");
		lblConfigLocation.setVerticalAlignment(SwingConstants.BOTTOM);
		pnlConfigLocation.add(lblConfigLocation);
		
		JRadioButton iWorkDir = new JRadioButton("Рабочая папка");
		iWorkDir.setSelected(true);
		pnlConfigLocation.add(iWorkDir);
		
		JRadioButton iCurrUser = new JRadioButton("Папка текущего пользователя");
		pnlConfigLocation.add(iCurrUser);
		
		ButtonGroup groupConfigLocation = new ButtonGroup();
	    groupConfigLocation.add(iWorkDir);
	    groupConfigLocation.add(iCurrUser);
	    
	    JPanel panel = new JPanel();
	    panel.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Значения по умолчанию", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
	    pnlGeneral.add(panel, BorderLayout.CENTER);
	    panel.setLayout(new MigLayout("", "[][grow]", "[][]"));
	    
	    JLabel label = new JLabel("Разделитель столбцов");
	    panel.add(label, "cell 0 0,alignx trailing");
	    
	    JComboBox<FieldDelimiter> iDefaultFieldDelimiter = new JComboBox<FieldDelimiter>();
	    panel.add(iDefaultFieldDelimiter, "cell 1 0,growx");
	    
	    JLabel lblNewLabel = new JLabel("Разделитель строк");
	    panel.add(lblNewLabel, "cell 0 1,alignx trailing");
	    
	    JComboBox<FieldDelimiter> iDefaultLineDelimiter = new JComboBox<FieldDelimiter>();
	    panel.add(iDefaultLineDelimiter, "cell 1 1,growx");
		
		JPanel pnlFields = new JPanel();
		tabbedPane.addTab("Настройка полей", null, pnlFields, null);
		pnlFields.setLayout(new MigLayout("", "[][]", "[]"));
		
		JPanel pnlFieldList = new JPanel();
		pnlFieldList.setPreferredSize(new Dimension(100, 100));
		pnlFields.add(pnlFieldList, "cell 0 0,grow");
		pnlFieldList.setLayout(new BoxLayout(pnlFieldList, BoxLayout.X_AXIS));
		
		JScrollPane spFieldList = new JScrollPane();
		pnlFieldList.add(spFieldList);
		
		iFieldList = new JList<Field>();
		spFieldList.setViewportView(iFieldList);
		
		JPopupMenu pmFieldList = new JPopupMenu();
		addPopup(iFieldList, pmFieldList);
		
		miAddField = new JMenuItem("Добавить");
		pmFieldList.add(miAddField);
		
		miRemoveField = new JMenuItem("Удалить");
		pmFieldList.add(miRemoveField);
		
		JPanel pnlMisc = new JPanel();
		pnlFields.add(pnlMisc, "cell 1 0,grow");
		
		
		JLabel lblColumnName = new JLabel("Имя");
		JLabel lblColumnDelimiter = new JLabel("Ограничитель столбца");
		JLabel lblDataType = new JLabel("Тип данных");
		JLabel lblOptional = new JLabel("Необязательное");
		JLabel lblFormat = new JLabel("Формат");
		JPanel pnlFormat = new JPanel();
		pnlFormat.setBorder(new TitledBorder(null, "", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		
		
		iColumnName = new JTextField();
		iFieldDelimiter = new JComboBox<FieldDelimiter>(new DefaultComboBoxModel<FieldDelimiter>(FieldDelimiter.values()));
		iDataType = new JComboBox<DataType>();
		iDataType.setModel(new DefaultComboBoxModel<DataType>(DataType.values()));
		iOptional = new JCheckBox();
		iFormat = new JComboBox<Format>();
		edtFormat = new JTextField();
		pnlFormat.setLayout(new BoxLayout(pnlFormat, BoxLayout.Y_AXIS));
		pnlFormat.add(iFormat);
		pnlFormat.add(edtFormat);
		
		pnlMisc.setLayout(new MigLayout("", "[grow][grow]", "[][][][][][grow]"));
		pnlMisc.add(lblColumnName, "cell 0 0");
		pnlMisc.add(iColumnName, "cell 1 0,growx");
		pnlMisc.add(lblColumnDelimiter, "cell 0 1");
		pnlMisc.add(iFieldDelimiter, "cell 1 1,growx");
		pnlMisc.add(lblDataType, "cell 0 2");
		pnlMisc.add(iDataType, "cell 1 2,growx");
		pnlMisc.add(lblFormat, "cell 0 3");
		pnlMisc.add(pnlFormat, "cell 1 3,grow");
		pnlMisc.add(lblOptional, "cell 0 4");
		pnlMisc.add(iOptional, "cell 1 4,growx");
		
		JPanel pnlValues = new JPanel();
		pnlValues.setBorder(new TitledBorder(null, "\u041D\u0430\u0431\u043E\u0440 \u0437\u043D\u0430\u0447\u0435\u043D\u0438\u0439", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		pnlMisc.add(pnlValues, "cell 0 5 2 1,grow");
		pnlValues.setLayout(new BoxLayout(pnlValues, BoxLayout.Y_AXIS));
		
		spValues = new JScrollPane();
		pnlValues.add(spValues);
		
		tValues = new JTable();
		spValues.setViewportView(tValues);
		
		JPopupMenu pmValueList = new JPopupMenu();
		addPopup(spValues, pmValueList);
		addPopup(tValues, pmValueList);
		
		miAddValue = new JMenuItem("Добавить");
		pmValueList.add(miAddValue);
		
		miRemoveValue = new JMenuItem("Удалить");
		pmValueList.add(miRemoveValue);
		
		fieldListController();
		fieldEditorController();
	}

	public static void main(String[] args) {
		Logger.getLogger("core").addHandler(new ConsoleHandler());
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			e.printStackTrace();
		}
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					ConfigFrame frame = new ConfigFrame();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	private static void addPopup(Component component, final JPopupMenu popup) {
		component.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				if (e.isPopupTrigger()) {
					showMenu(e);
				}
			}
			public void mouseReleased(MouseEvent e) {
				if (e.isPopupTrigger()) {
					showMenu(e);
				}
			}
			private void showMenu(MouseEvent e) {
				Component component = e.getComponent(); 
				if (component != null && component.isEnabled())
					popup.show(component, e.getX(), e.getY());
			}
		});
	}
	
	private void fieldListController(){
		mdlFieldList = new FieldListModel();
		iFieldList.setModel(mdlFieldList);
		
		Core.getConfigModel().addFieldListListener((evt) -> {
			mdlFieldList.refresh();
		});
		
		miAddField.addActionListener((ActionEvent arg0) -> {
			Core.getConfigModel().addFieldAfter(iFieldList.getSelectedValue());
		});
		
		miRemoveField.addActionListener((ActionEvent arg0) -> {
			List<Field> list = iFieldList.getSelectedValuesList();
			int id = iFieldList.getSelectedIndex(); 
			if (id > 0) {
				iFieldList.setSelectedIndex(id - 1);
			} else
				iFieldList.clearSelection();
			Core.getConfigModel().removeFields(list);
		});
	}
	
	private void fieldEditorController(){
		// ComboBox "Format List" model
		FormatListModel mdlFormatlist = new FormatListModel();
		iFormat.setModel(mdlFormatlist);
		
		// Table "Field Values" model
		ValueListModel mdlValueList = new ValueListModel();
		tValues.setModel(mdlValueList);
		
		// Config model >> Field List >> onChange 
		Core.getConfigModel().addFieldListListener((evt) -> {
			if (evt.getField() == null || evt.getField() == iFieldList.getSelectedValue()) {
				mdlValueList.setField(iFieldList.getSelectedValue());
			}
		});
		
		// Select field
		iFieldList.addListSelectionListener((evt) -> {
			Field field = iFieldList.getSelectedValue();
			if (field != null) {
				DataType datatype = field.getDatatype();
				iColumnName.setText(field.getName());
				iFieldDelimiter.setSelectedItem(field.getDelimiter());
				iDataType.setSelectedItem(datatype);
				edtFormat.setText(field.getFormatValue());
				Format format = field.getFormat();
				iFormat.setSelectedItem(format);
				iOptional.setSelected(field.isOptional());
				
				mdlFormatlist.refresh();
				mdlValueList.setField(field);
			} else {
				iColumnName.setText("");
				iFieldDelimiter.setSelectedIndex(-1);
				iDataType.setSelectedIndex(-1);
				edtFormat.setText("");
				iFormat.setSelectedIndex(-1);
				iOptional.setSelected(false);
			}
			
			updateContolState();
		});
		
		iDataType.addActionListener((evt) -> {
			//TODO
		});
		
		updateContolState();
		
		miAddValue.addActionListener((ActionEvent arg0) -> {
			Field field = iFieldList.getSelectedValue();
			int index = tValues.getSelectedRow();
			FieldValue value = new FieldValue("");
			if (index == -1) {
				Core.getConfigModel().addFieldValueAt(field, null, value);
			} else {
				Core.getConfigModel().addFieldValueAt(field, index + 1, value);
			}
		});
		
		miRemoveValue.addActionListener((ActionEvent arg0) -> {
			Core.getConfigModel().removeFieldValue(iFieldList.getSelectedValue(), tValues.getSelectedRows());
		});
	}
	
	/** Updates control state: enabled/disabled, clear values */
	private void updateContolState(){
		Field currField = iFieldList.getSelectedValue();
		boolean enabled = currField != null;
		
		iColumnName.setEnabled(enabled);
		iFieldDelimiter.setEnabled(enabled);
		iDataType.setEnabled(enabled);
		edtFormat.setEnabled(enabled);
		iFormat.setEnabled(enabled);
		iOptional.setEnabled(enabled);
		tValues.setEnabled(enabled);
		spValues.setEnabled(enabled);
	}
	
	private Field getSelectedField() {
		return iFieldList.getSelectedValue();
	}
	
	private class FieldListModel
	extends AbstractListModel<Field> {
		private static final long serialVersionUID = -6222628541194781163L;

		@Override
		public Field getElementAt(int index) {
			return Core.getConfigModel().getField(index);
		}

		@Override
		public int getSize() {
			return Core.getConfigModel().getFieldSetSize();
		}
		
		/** Refreshes whole list */
		public void refresh() {
			fireContentsChanged(this, -1, -1);
		}
	}
	
	private class DataTypeListModel
	extends AbstractListModel<DataType>
	implements ComboBoxModel<DataType> {
		private static final long serialVersionUID = -3972948053898888801L;
		
		@Override
		public DataType getElementAt(int index) {
			return DataType.values()[index];
		}

		@Override
		public int getSize() {
			return DataType.values().length;
		}

		@Override
		public Object getSelectedItem() {
			Field field = iFieldList.getSelectedValue();
			if (field != null)
				return field.getDatatype();
			
			return null;
		}

		@Override
		public void setSelectedItem(Object object) {
			Object selectedItem = getSelectedItem();
			if (selectedItem == null && object == null ||
				selectedItem != null && selectedItem.equals(object) ||
				object != null && getIndexOf(object) == -1)
					return;
			System.out.println("format setted");
			
			getSelectedField().setDatatype((DataType) object);
			refresh();
		}
		
		/** Refreshes whole list */
		public void refresh() {
			fireContentsChanged(this, -1, -1);
		}
		
		/**
		 * Returns the index of the specified element in the model's item list.
		 * @param object  the element.
		 * @return The index of the specified element in the model's item list or -1 if it's not in the list.
		 * */
		private int getIndexOf(Object object) {
			DataType[] values = DataType.values();
			for (int i = 0; i < values.length; i++) {
				if (values[i].equals(object))
					return i;
			}
			return -1;
		}
	}
	
	private class FormatListModel
	extends AbstractListModel<Format>
	implements ComboBoxModel<Format> {
		private static final long serialVersionUID = -3972948053898888801L;
		
		/** The selected item */
		private Format selectedItem;
		
		@Override
		public Format getElementAt(int index) {
			List<Format> formatList = getFormatList();
			if (formatList != null) {
				return formatList.get(index);
			}
			return null;
		}

		@Override
		public int getSize() {
			List<Format> formatList = getFormatList();
			if (formatList != null) {
				return formatList.size();
			}
			return 0;
		}

		@Override
		public Object getSelectedItem() {
			return selectedItem;
		}

		@Override
		public void setSelectedItem(Object object) {
			if (selectedItem == null && object == null ||
				selectedItem != null && selectedItem.equals(object) ||
				object != null && getIndexOf(object) == -1)
					return;
			System.out.println("format setted");
			selectedItem = (Format) object;
			refresh();
		}
		
		/** Refreshes whole list */
		public void refresh() {
			fireContentsChanged(this, -1, -1);
		}
		
		/** Returns the model's item list */
		private List<Format> getFormatList() {
			Field field = iFieldList.getSelectedValue();
			if (field != null) {
				DataType datatype = field.getDatatype();
				if (datatype != null)
					return datatype.getFormatList();
			}
			return null;
		}
		
		/**
		 * Returns the index of the specified element in the model's item list.
		 * @param format  the element.
		 * @return The index of the specified element in the model's item list.
		 * */
		private int getIndexOf(Object format) {
			List<Format> list = getFormatList();
			if (list != null) {
				return list.indexOf(format);
			} else
				return -1;
		}
	}
	
	private class ValueListModel
	extends AbstractTableModel {
		private static final long serialVersionUID = 3742047021848215242L;
		private final String[] COLS = { "Значение", "Описание" };
		private final Class[] COL_CLASS = {Object.class, String.class};
		private List<FieldValue> valueList;
		
		@Override
		public int getColumnCount() {
			return COLS.length;
		}
		
		@Override
		public String getColumnName(int col) {
			return COLS[col];
		}
		
		@Override
		public Class getColumnClass(int col) {
			return COL_CLASS[col];
		}

		@Override
		public int getRowCount() {
			if (valueList != null) {
				return valueList.size();
			} else
				return 0;
		}

		@Override
		public Object getValueAt(int row, int col) {
			if (valueList != null) {
				if (row >= valueList.size())
					return null;
				switch (col) {
				case 0:
					return valueList.get(row).value;
				case 1:
					return valueList.get(row).caption;
				default:
					return null;
				}
			} else
				return null;
		}
		
		@Override
		public void setValueAt(Object value, int row, int col) {
			if (valueList != null) {
				if (row >= valueList.size())
					return;
				switch (col) {
				case 0:
					valueList.get(row).value = value;
					break;
				case 1:
					valueList.get(row).caption = value.toString();
					break;
				}
			}
		}
		
		@Override
		public boolean isCellEditable(int rowIndex, int columnIndex) {
			return true;
		}
		
		public void setField(Field field) {
			if (field != null) {
				valueList = field.getValueList();
				fireTableDataChanged();
			} else
				if (valueList != null) {
					valueList = null;
					fireTableDataChanged();
				}
		}
	}
}
