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

import java.awt.EventQueue;
import java.util.EventObject;
import java.util.List;
import java.util.function.BiPredicate;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.AbstractListModel;
import javax.swing.InputVerifier;
import javax.swing.JButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.border.TitledBorder;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import net.miginfocom.swing.MigLayout;
import ru.foxling.graphit.LoggerLabelHandler;
import ru.foxling.graphit.config.ConfigModel;
import ru.foxling.graphit.config.DataType;
import ru.foxling.graphit.config.Field;
import ru.foxling.graphit.config.FieldDelimiter;
import ru.foxling.graphit.config.FieldEvent;
import ru.foxling.graphit.config.FieldRole;
import ru.foxling.graphit.config.FieldValue;
import ru.foxling.graphit.config.Format;
import ru.foxling.graphit.utils.Resources;

import java.awt.Color;
import javax.swing.BoxLayout;
import java.awt.Component;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableModel;

import java.awt.Dimension;
import javax.swing.JPopupMenu;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import javax.swing.JMenuItem;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.Insets;
import javax.swing.border.EtchedBorder;

public class ConfigFrame extends JFrame {
	private static final long serialVersionUID = 3103016344816004897L;
	private static Logger LOG = Logger.getLogger(ConfigFrame.class.getName()); 
	private JPanel contentPane;
	private JTextField iFieldName;
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
	private FormatListModel mdlFormatlist;
	private byte[] configState;
	private JButton btnCancel;
	private JButton btnOkay;
	private JCheckBox iBitMask;
	private JCheckBox iHashsum;
	private JComboBox<FieldRole> iFieldRole;
	private JButton iColorChooser;
	private JPanel pnlMisc;
	private JLabel lblLogMessage;
	
	public ConfigFrame() {
		super("Настройки");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 850, 500);
        setIconImage(Resources.getFrameIcon());
		
		saveConfigState();
		
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(1, 1, 1, 1));
		setContentPane(contentPane);
		contentPane.setLayout(new BorderLayout(0, 0));
		
		JPanel pnlFields = new JPanel();
		contentPane.add(pnlFields, BorderLayout.CENTER);
		pnlFields.setLayout(new MigLayout("", "[][grow]", "[]"));
		
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
		
		pmFieldList.addSeparator();
		
		pnlMisc = new JPanel();
		pnlFields.add(pnlMisc, "cell 1 0,grow");
		
		
		JLabel lblFieldName = new JLabel("Имя");
		JLabel lblFieldDelimiter = new JLabel("Ограничитель столбца");
		JLabel lblDataType = new JLabel("Тип данных");
		JLabel lblOptional = new JLabel("Необязательное");
		JLabel lblFormat = new JLabel("Формат");
		JPanel pnlFormat = new JPanel();
		pnlFormat.setBorder(new TitledBorder(null, "", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		
		
		iFieldName = new JTextField();
		iFieldDelimiter = new JComboBox<FieldDelimiter>(new DefaultComboBoxModel<>(FieldDelimiter.values()));
		iDataType = new JComboBox<DataType>(new DefaultComboBoxModel<>(DataType.values()));
		iOptional = new JCheckBox();
		iFormat = new JComboBox<Format>();
		edtFormat = new JTextField();
		pnlFormat.setLayout(new BoxLayout(pnlFormat, BoxLayout.Y_AXIS));
		pnlFormat.add(iFormat);
		pnlFormat.add(edtFormat);
		
		pnlMisc.setLayout(new MigLayout("", "[]20[grow]", "[][][][][][][][][grow]"));
		pnlMisc.add(lblFieldName, "cell 0 0");
		pnlMisc.add(iFieldName, "cell 1 0,growx");
		pnlMisc.add(lblFieldDelimiter, "cell 0 1");
		pnlMisc.add(iFieldDelimiter, "cell 1 1,growx");
		pnlMisc.add(lblDataType, "cell 0 2");
		pnlMisc.add(iDataType, "cell 1 2,growx");
		pnlMisc.add(lblFormat, "cell 0 3");
		pnlMisc.add(pnlFormat, "cell 1 3,grow");
		
		JLabel lblRole = new JLabel("Роль");
		pnlMisc.add(lblRole, "cell 0 4");
		
		iFieldRole = new JComboBox<FieldRole>(new DefaultComboBoxModel<>(FieldRole.values()));
		pnlMisc.add(iFieldRole, "cell 1 4,growx,split 2");
		
		iColorChooser = new JButton();
		iColorChooser.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		iColorChooser.setMinimumSize(new Dimension(18, 18));
		iColorChooser.setMaximumSize(new Dimension(18, 18));
		iColorChooser.setMargin(new Insets(0, 0, 0, 0));
		pnlMisc.add(iColorChooser);
		pnlMisc.add(lblOptional, "cell 0 6");
		pnlMisc.add(iOptional, "cell 1 6,growx");
		
		JLabel lblHashsum = new JLabel("Хэш-сумма");
		pnlMisc.add(lblHashsum, "cell 0 7");
		
		iHashsum = new JCheckBox();
		iHashsum.setEnabled(false);
		pnlMisc.add(iHashsum, "cell 1 7");
		
		JPanel pnlValues = new JPanel();
		pnlValues.setBorder(new TitledBorder(null, "Набор значений", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		pnlMisc.add(pnlValues, "cell 0 8 2 1,grow");
		pnlValues.setLayout(new BorderLayout(0, 0));
		
		spValues = new JScrollPane();
		pnlValues.add(spValues);
		
		tValues = new JTable() {
			private static final long serialVersionUID = 7567104525794311551L;
			private String[] columnToolTips = {
					"Значение из лог файла",
					"Короткое пояснение, отображается в таблице",
					"Длинное описание, отображается в всплывающей подсказке таблицы"};
			{
				this.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
			}
			
			//Implement table header tool tips.
			protected JTableHeader createDefaultTableHeader() {
				return new JTableHeader(columnModel) {
					private static final long serialVersionUID = 5363995066011938534L;

					public String getToolTipText(MouseEvent e) {
						java.awt.Point p = e.getPoint();
						int index = columnModel.getColumnIndexAtX(p.x);
						int realIndex = columnModel.getColumn(index).getModelIndex();
						return columnToolTips[realIndex];
					}
				};
			}
		};
		spValues.setViewportView(tValues);
		
		JPopupMenu pmValueList = new JPopupMenu();
		addPopup(spValues, pmValueList);
		addPopup(tValues, pmValueList);
		
		miAddValue = new JMenuItem("Добавить");
		pmValueList.add(miAddValue);
		
		miRemoveValue = new JMenuItem("Удалить");
		pmValueList.add(miRemoveValue);
		
		iBitMask = new JCheckBox("Значения - битовая маска");
		iBitMask.setToolTipText("Значение поля может содержать несколько значений из данного набора");
		pnlValues.add(iBitMask, BorderLayout.NORTH);
		
		JPanel pnlStatusBar = new JPanel();
		pnlStatusBar.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		contentPane.add(pnlStatusBar, BorderLayout.SOUTH);
		
		pnlStatusBar.setLayout(new MigLayout("insets 2", "[grow][][]", "[]"));
		
		lblLogMessage = new JLabel();
		lblLogMessage.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2 && !e.isConsumed()) {
				     e.consume();
				     EventJournalFrame.launch();
				}
			}
		});
		lblLogMessage.setMinimumSize(new Dimension(14, 14));
		pnlStatusBar.add(lblLogMessage, "growx");
		
		btnOkay = new JButton("ОК");
		pnlStatusBar.add(btnOkay);
		
		btnCancel = new JButton("Отмена");
		pnlStatusBar.add(btnCancel);
		
		Logger.getLogger(getClass().getPackage().getName()).addHandler(new LoggerLabelHandler(lblLogMessage, Level.INFO));
		configController();
	}

	private void saveConfigState(){
		ObjectOutputStream out = null;
		try {
			ByteArrayOutputStream stream = new ByteArrayOutputStream();
			out = new ObjectOutputStream(stream);
			out.writeObject(ConfigModel.getInstance());
			configState = stream.toByteArray();
			out.close();
		} catch(IOException ex) {
			ex.printStackTrace();
		}
	}
	
	private void loadConfigState() {
		try {
			ByteArrayInputStream stream = new ByteArrayInputStream(configState);
			ObjectInputStream in = new ObjectInputStream(stream);
			ConfigModel.setInstance((ConfigModel) in.readObject());
		} catch(IOException | ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	public static void launch() {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					ConfigFrame frame = new ConfigFrame();
					frame.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
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
	
	private void configController(){
		mdlFieldList = new FieldListModel();
		iFieldList.setModel(mdlFieldList);
		iFieldList.setCellRenderer(new FieldListCellRenderer());
		
		// Field List - Context Menu > Add field
		miAddField.addActionListener((ActionEvent arg0) -> {
			ConfigModel.getInstance().addFieldAfter(getSelectedField());
		});
		
		// Field List - Context Menu > Remove field(-s)
		miRemoveField.addActionListener((ActionEvent arg0) -> {
			List<Field> list = iFieldList.getSelectedValuesList();
			int id = iFieldList.getSelectedIndex(); 
			if (id > 0) {
				iFieldList.setSelectedIndex(id - 1);
			} else {
				iFieldList.clearSelection();
			}
			ConfigModel.getInstance().removeFields(list);
		});
		
		
		// ComboBox "Format" model
		mdlFormatlist = new FormatListModel();
		iFormat.setModel(mdlFormatlist);
		
		// Table "Field Values" model
		tValues.setModel(BlankTableModel.instance());
		
		// Config model >> Field List >> onChange 
		ConfigModel.getInstance().addFieldListener((evt) -> {
			Field field = (Field) evt.getSource();
			Field selected = getSelectedField();
			
			mdlFieldList.refresh();
			
			if (evt.getType() == FieldEvent.UPDATE && field == selected) {
				refreshFieldInfo(evt);
			}
		});
		
		// Select field
		iFieldList.addListSelectionListener(e -> refreshFieldInfo(e));

		iFieldDelimiter.addActionListener(e -> {
				Field field = getSelectedField();
				if (field == null)
					return;
				
				if (!iFieldDelimiter.getSelectedItem().equals(field.getDelimiter()))
					ConfigModel.getInstance().setFieldDelimiter(field, (FieldDelimiter) iFieldDelimiter.getSelectedItem());
		});
		
		iDataType.addActionListener(e -> {
			Field field = getSelectedField();
			if (field == null)
				return;
			
			if (!iDataType.getSelectedItem().equals(field.getDatatype()))
				ConfigModel.getInstance().setFieldDatatype(field, (DataType) iDataType.getSelectedItem());
		});
		
		iFieldRole.addActionListener(e -> {
			Field field = getSelectedField();
			if (field == null)
				return;
			
			if (!iFieldRole.getSelectedItem().equals(field.getRole()))
				if (!ConfigModel.getInstance().setFieldRole(field, (FieldRole) iFieldRole.getSelectedItem()))
					iFieldRole.setSelectedItem(field.getRole());
		});
		
		iColorChooser.addActionListener(e -> {
			Field field = getSelectedField();
			if (field == null)
				return;
			
			Color color = JColorChooser.showDialog(this, "Выберите цвет для графика", field.getColor());
			if (color != null)
				ConfigModel.getInstance().setFieldColor(getSelectedField(), color);
		});

		// FieldName Change
		MyVerifier nameVerifier = new MyVerifier((c, a) -> {
			ConfigModel.getInstance().setFieldName(getSelectedField(), iFieldName.getText());
			return true;
		});
		iFieldName.setInputVerifier(nameVerifier);
		iFieldName.addActionListener(nameVerifier);
		
		// Format edit Change
		MyVerifier formatVerifier = new MyVerifier((c, a) -> {
			ConfigModel.getInstance().setFieldFormat(getSelectedField(), edtFormat.getText());
			return true;
		});
		edtFormat.setInputVerifier(formatVerifier);
		edtFormat.addActionListener(formatVerifier);
		
		updateContolState();
		
		miAddValue.addActionListener(e -> {
			Field field = getSelectedField();
			int index = tValues.getSelectedRow();
			FieldValue value = new FieldValue();
			if (index == -1) {
				ConfigModel.getInstance().addFieldValueAt(field, null, value);
			} else {
				ConfigModel.getInstance().addFieldValueAt(field, index + 1, value);
			}
		});
		
		miRemoveValue.addActionListener((e) -> {
			ConfigModel.getInstance().removeFieldValue(getSelectedField(), tValues.getSelectedRows());
		});
		
		btnOkay.addActionListener(e -> {
			ConfigModel.getInstance().saveConfig();
			super.dispose();
		});
		
		btnCancel.addActionListener(e -> {
			loadConfigState();
			super.dispose();
		});
		
		iOptional.addActionListener(e -> {
			if (!ConfigModel.getInstance().setFieldOptional(getSelectedField(), iOptional.isSelected()))
				iOptional.setSelected(getSelectedField().isOptional());			
		});
		
		iHashsum.addActionListener(e -> {
			if (!ConfigModel.getInstance().setFieldHashsum(getSelectedField(), iHashsum.isSelected()))
				iHashsum.setSelected(getSelectedField().isHashsum());			
		});
		
		iBitMask.addActionListener(e -> {
			ConfigModel.getInstance().setFieldBitmask(getSelectedField(), iBitMask.isSelected());
		});
	}
	
	/** Updates control state: enabled/disabled, clear values */
	private void updateContolState() {
		Field field = getSelectedField();
		boolean isFieldSelected = field != null,
				isBitmaskVisible = false,
				isColorChooserVisible = false;
		
		iFieldName.setEnabled(isFieldSelected);
		iFieldDelimiter.setEnabled(isFieldSelected);
		iDataType.setEnabled(isFieldSelected);
		iOptional.setEnabled(isFieldSelected);
		tValues.setEnabled(isFieldSelected);
		spValues.setEnabled(isFieldSelected);
		iHashsum.setEnabled(isFieldSelected);
		
		
		edtFormat.setEnabled(false);
		iFormat.setEnabled(false);
		iBitMask.setVisible(false);
		iFieldRole.setEnabled(false);
		if (isFieldSelected) {
			DataType datatype = field.getDatatype();
			if (datatype != null) {
				edtFormat.setEnabled(!datatype.isFixedFormatList());
				iFormat.setEnabled(!(datatype.getFormatList().size() == 0 ||
									datatype.getFormatList().size() == 1 &&
									datatype.getFormatList().get(0).equals(Format.EMPTY_FORMAT)));
				isBitmaskVisible = datatype == DataType.BYTE || datatype == DataType.SHORT || datatype == DataType.INTEGER;
				iFieldRole.setEnabled(datatype != DataType.STRING);
			}
			isColorChooserVisible = field.getRole() == FieldRole.DRAW;
		}
		iBitMask.setVisible(isBitmaskVisible);
		iColorChooser.setVisible(isColorChooserVisible);
	}
	
	/** Returns the selected field in <code><i>JList</i></code> {@link #iFieldList}<br>
	 * If two or more fields are selected, returns <code>null</code> 
	 * */
	private Field getSelectedField() {
		if (iFieldList.getSelectedIndices().length == 1) {
			return iFieldList.getSelectedValue();
		} else {
			return null;
		}
	}
	
	private void refreshFieldInfo(EventObject evt) {
		iColorChooser.setBackground(pnlMisc.getBackground());
		
		Field field = getSelectedField();
		if (field != null) {
			iFieldName.setText(field.getName());
			iFieldDelimiter.setSelectedItem(field.getDelimiter());
			iDataType.setSelectedItem(field.getDatatype());
			iOptional.setSelected(field.isOptional());
			edtFormat.setText(field.getFormatValue());
			mdlFormatlist.refresh();
			iFieldRole.setSelectedItem(field.getRole());
			iBitMask.setSelected(field.isBitmask());
			iHashsum.setSelected(field.isHashsum());
			if (field.getRole() == FieldRole.DRAW && field.getColor() != null)
				iColorChooser.setBackground(field.getColor());
			
			TableModel model = tValues.getModel();
			if (model instanceof BlankTableModel || ((ValueListModel) model).getField() != field) {
				tValues.setModel(new ValueListModel(field));
			} else {
				((ValueListModel) model).refresh();
			}
		} else {
			iFieldName.setText("");
			iFieldDelimiter.setSelectedIndex(-1);
			iDataType.setSelectedIndex(-1);
			edtFormat.setText("");
			iFormat.setSelectedIndex(-1);
			iFieldRole.setSelectedIndex(-1);
			iOptional.setSelected(false);
			iBitMask.setSelected(false);
			iHashsum.setSelected(false);
			if (!(tValues.getModel() instanceof BlankTableModel)) {
				tValues.setModel(BlankTableModel.instance());
			}
		}
		
		updateContolState();
	}
	
	private class FieldListModel
	extends AbstractListModel<Field> {
		private static final long serialVersionUID = -6222628541194781163L;

		@Override
		public Field getElementAt(int index) {
			return ConfigModel.getInstance().getFieldList().get(index);
		}

		@Override
		public int getSize() {
			return ConfigModel.getInstance().getFieldList().size();
		}
		
		/** Refreshes whole list */
		public void refresh() {
			fireContentsChanged(this, -1, -1);
		}
	}
	
	private class FieldListCellRenderer
	extends JLabel
	implements ListCellRenderer<Field> {
		private static final long serialVersionUID = -119483135875095517L;
		public Component getListCellRendererComponent(JList<? extends Field> list,
														Field field,             // value to display
														int index,               // cell index
														boolean isSelected,      // is the cell selected
														boolean cellHasFocus) {  // does the cell have focus
			Color color;
			if (field.isValid()) {
				color = Color.BLACK;
			} else
				color = Color.RED;
			
			setText(field.toString());
			if (isSelected) {
				setBackground(color);
				setForeground(Color.WHITE);
			} else {
				setBackground(Color.WHITE);
				setForeground(color);
			}
						
			setEnabled(list.isEnabled());
			setFont(list.getFont());
			setOpaque(true);
			return this;
		}
	}
	
	private class FormatListModel
	extends AbstractListModel<Format>
	implements ComboBoxModel<Format> {
		private static final long serialVersionUID = -3972948053898888801L;
		
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
			Field field = getSelectedField();
			if (field != null) {
				return field.getFormat();
			} else
				return null;
		}

		@Override
		public void setSelectedItem(Object object) {
			Format selectedItem = (Format) getSelectedItem();
			if (selectedItem == null && object == null ||
					selectedItem != null && selectedItem.equals(object) ||
					object != null && getIndexOf(object) == -1)
						return;
			
			ConfigModel.getInstance().setFieldFormat(getSelectedField(), (Format) object);
			refresh();
		}
		
		/** Refreshes whole list */
		public void refresh() {
			fireContentsChanged(this, -1, -1);
		}
		
		/** Returns the model's item list */
		private List<Format> getFormatList() {
			Field field = getSelectedField();
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
		
		private final String[] COLS = { "Значение", "Метка", "Описание" };
		private final Class<?>[] COL_CLASS = {String.class, String.class, String.class};
		
		private Field field;
		List<FieldValue> valueList;

		public ValueListModel(Field field) {
			if (field == null) {
				throw new IllegalArgumentException("field-param shouldn't be null");
			}
			this.field = field;
			this.valueList = field.getValueList();
		}
		
		@Override
		public int getColumnCount() {
			return COLS.length;
		}
		
		@Override
		public String getColumnName(int col) {
			return COLS[col];
		}
		
		@Override
		public Class<?> getColumnClass(int col) {
			return COL_CLASS[col];
		}

		@Override
		public int getRowCount() {
			return valueList.size();
		}

		@Override
		public Object getValueAt(int row, int col) {
			if (row >= valueList.size())
				return null;
			
			switch (col) {
			case 0:
				return valueList.get(row).source;
			case 1:
				return valueList.get(row).caption;
			case 2:
				return valueList.get(row).description;
			default:
				return null;
			}
		}
		
		@Override
		public void setValueAt(Object value, int row, int col) {
			if (row >= valueList.size())
				return;
			switch (col) {
			case 0:
				String src = (String) value;
				FieldValue fValue = valueList.get(row);
				if (src == null || src.trim().isEmpty()) {
					fValue.source = null;
					fValue.value = null;
					return;
				}
				try {
					valueList.get(row).value = field.getParser().parse(src);
					valueList.get(row).source = src;
				} catch (Exception e) {
					LOG.log(Level.WARNING, String.format("Не удалось сконвертировать строку '%s' в тип '%s'", value, field.getDatatype()), e);
				}
				break;
			case 1:
				valueList.get(row).caption = value.toString();
				break;
			case 2:
				valueList.get(row).description = value.toString();
				break;
			}
		}
		
		@Override
		public boolean isCellEditable(int rowIndex, int columnIndex) {
			return true;
		}
		
		public Field getField() {
			return field;
		}
		
		public void refresh() {
			this.fireTableDataChanged();
		}
	}
	
	/** The format verifier handles both pressing Enter key and Focus loosing */
	class MyVerifier
		extends InputVerifier
	    implements ActionListener {
		
		BiPredicate<JComponent,ActionEvent> processor;
		
		public MyVerifier(BiPredicate<JComponent,ActionEvent> processor) {
			this.processor = processor;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			processor.test(null, e);
		}

		@Override
		public boolean verify(JComponent input) {
			return processor.test(input, null);
		}
	}
}
