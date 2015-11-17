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
import java.util.Arrays;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SpringLayout;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.AbstractListModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListModel;
import javax.swing.JComboBox;
import javax.swing.JCheckBox;
import javax.swing.border.TitledBorder;

import com.foxling.graphit.Core;
import com.foxling.graphit.DataType;
import com.foxling.graphit.Field;

import javax.swing.JTabbedPane;
import java.awt.FlowLayout;
import javax.swing.ButtonGroup;
import javax.swing.JRadioButton;
import javax.swing.SwingConstants;
import net.miginfocom.swing.MigLayout;
import java.awt.Color;
import javax.swing.BoxLayout;
import java.awt.Component;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import java.awt.Dimension;
import javax.swing.JSpinner;
import javax.swing.border.EtchedBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;
import javax.swing.border.SoftBevelBorder;
import javax.swing.border.BevelBorder;
import java.awt.Insets;

public class ConfigFrame extends JFrame {
	private static final long serialVersionUID = 3103016344816004897L;
	private JPanel contentPane;
	private JTextField iColumnName;
	private JComboBox<CBXItem<String>> iColumnDelimiter;		
	private JComboBox<DataType> iDataType;
	private JComboBox<String> iFormat;
	private JList<Field> iFieldList;
	private JTextField edtFormat;
	private JCheckBox iOptional;
	private JTable tValues;
	
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
	    
	    JComboBox iDefaultFieldDelimiter = new JComboBox();
	    panel.add(iDefaultFieldDelimiter, "cell 1 0,growx");
	    
	    JLabel lblNewLabel = new JLabel("Разделитель строк");
	    panel.add(lblNewLabel, "cell 0 1,alignx trailing");
	    
	    JComboBox iDefaultLineDelimiter = new JComboBox();
	    panel.add(iDefaultLineDelimiter, "cell 1 1,growx");
		
		JPanel pnlFields = new JPanel();
		tabbedPane.addTab("Настройка полей", null, pnlFields, null);
		pnlFields.setLayout(new MigLayout("", "[][]", "[]"));
		
		Insets toolButtonMargin = new Insets(2, 5, 2, 5);
		Dimension spinButtonDimension = new Dimension(20, 16);
		
		JPanel pnlFieldList = new JPanel();
		pnlFieldList.setPreferredSize(new Dimension(100, 100));
		pnlFields.add(pnlFieldList, "cell 0 0,grow");
		pnlFieldList.setLayout(new MigLayout("", "[grow][]", "[grow]"));
		
		JScrollPane spFieldList = new JScrollPane();
		pnlFieldList.add(spFieldList, "cell 0 0,growy");
		
		iFieldList = new JList<Field>();
		spFieldList.setViewportView(iFieldList);
		
		JPanel pnlFieldControls = new JPanel();
		pnlFieldList.add(pnlFieldControls, "cell 1 0,aligny top");
		pnlFieldControls.setLayout(new BoxLayout(pnlFieldControls, BoxLayout.Y_AXIS));
		
		JButton btnAddField = new JButton("+");
		btnAddField.setMargin(toolButtonMargin);
		pnlFieldControls.add(btnAddField);
		
		JButton btnRemoveField = new JButton("--");
		btnRemoveField.setMargin(toolButtonMargin);
		pnlFieldControls.add(btnRemoveField);
		
		JButton btnModifyField = new JButton("~");
		btnModifyField.setMargin(toolButtonMargin);
		pnlFieldControls.add(btnModifyField);
		
		JButton btnFieldUp = new JButton("˄");
		btnFieldUp.setMargin(toolButtonMargin);
		pnlFieldControls.add(btnFieldUp);
		
		JButton btnFieldDown = new JButton("˅");
		btnFieldDown.setMargin(toolButtonMargin);
		pnlFieldControls.add(btnFieldDown);
		
		JPanel pnlMisc = new JPanel();
		pnlFields.add(pnlMisc, "cell 1 0,grow");
		
		
		JLabel lblColumnName = new JLabel("Имя");
		JLabel lblColumnDelimiter = new JLabel("Ограничитель столбца");
		JLabel lblDataType = new JLabel("Тип данных");
		JLabel lblOptional = new JLabel("Необязательное");
		JLabel lblFormat = new JLabel("Формат");
		JPanel pnlFormat = new JPanel();
		pnlFormat.setBorder(new TitledBorder(null, "", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		initControls();
		
		iColumnName = new JTextField();
		iOptional = new JCheckBox();
		iFormat = new JComboBox<String>();
		edtFormat = new JTextField();
		pnlFormat.setLayout(new BoxLayout(pnlFormat, BoxLayout.Y_AXIS));
		pnlFormat.add(iFormat);
		pnlFormat.add(edtFormat);
		
		pnlMisc.setLayout(new MigLayout("", "[grow][grow]", "[][][][][][grow]"));
		pnlMisc.add(lblColumnName, "cell 0 0");
		pnlMisc.add(iColumnName, "cell 1 0,growx");
		pnlMisc.add(lblColumnDelimiter, "cell 0 1");
		pnlMisc.add(iColumnDelimiter, "cell 1 1,growx");
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
		
		JScrollPane spValues = new JScrollPane();
		pnlValues.add(spValues);
		
		tValues = new JTable();
		tValues.setModel(new DefaultTableModel(
			new Object[][] {
				{null, null},
			},
			new String[] { "Значение", "Описание" }
		));
		spValues.setViewportView(tValues);
	}
	
	private void initControls(){
		iColumnDelimiter = new JComboBox<CBXItem<String>>(new Vector<CBXItem<String>>(Arrays.asList(
				new CBXItem<String>(";{SPACE}", "; "),
				new CBXItem<String>("{CR}{LF}", "{CR}{LF}"),
				new CBXItem<String>("{CR}", "{CR}"),
				new CBXItem<String>("{LF}", "{LF}"),
				new CBXItem<String>("Точка с запятой {;}", ";"),
				new CBXItem<String>("Двоеточие {:}", ":"),
				new CBXItem<String>("Запятая {,}", ","),
				new CBXItem<String>("Табуляция {t}", "\t"),
				new CBXItem<String>("Вертикальная черта {|}", "|")
		)));
		
		iDataType = new JComboBox<DataType>();
		iDataType.setModel(new DefaultComboBoxModel<DataType>(DataType.values()));
		
		new ConfigController(iFieldList, Core.getConfigModel());
	}

	public static void main(String[] args) {
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

	private class CBXItem<T> {
		private String caption;
		private T value;
		
		public CBXItem(String caption, T value) {
			this.caption = caption;
			this.value = value;
		}

		public String getCaption() {
			return caption;
		}

		public T getValue() {
			return value;
		}
		
		public String toString(){
			return caption;
		}
	}
}
