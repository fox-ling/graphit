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

public class ConfigFrame extends JFrame {
	private static final long serialVersionUID = 3103016344816004897L;
	private JPanel contentPane;
	private JTextField iColumnName;
	private JComboBox<CBXItem<String>> iColumnDelimiter;		
	private JComboBox<DataType> iDataType;
	private JComboBox<String> iFormat;
	private JList<Field> iFieldList;
	
	public ConfigFrame() {
		super("Настройки");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 464, 400);
		
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
		SpringLayout sl_pnlFields = new SpringLayout();
		pnlFields.setLayout(sl_pnlFields);
		
		JButton btnAddCol = new JButton("Добавить");
		sl_pnlFields.putConstraint(SpringLayout.NORTH, btnAddCol, 6, SpringLayout.NORTH, pnlFields);
		sl_pnlFields.putConstraint(SpringLayout.WEST, btnAddCol, 10, SpringLayout.WEST, pnlFields);
		pnlFields.add(btnAddCol);
		
		JButton btnRemoveCol = new JButton("Удалить");
		sl_pnlFields.putConstraint(SpringLayout.NORTH, btnRemoveCol, 0, SpringLayout.NORTH, btnAddCol);
		sl_pnlFields.putConstraint(SpringLayout.WEST, btnRemoveCol, 6, SpringLayout.EAST, btnAddCol);
		pnlFields.add(btnRemoveCol);
		
		JScrollPane scrollPane = new JScrollPane();
		sl_pnlFields.putConstraint(SpringLayout.NORTH, scrollPane, 6, SpringLayout.SOUTH, btnAddCol);
		sl_pnlFields.putConstraint(SpringLayout.WEST, scrollPane, 0, SpringLayout.WEST, btnAddCol);
		sl_pnlFields.putConstraint(SpringLayout.EAST, scrollPane, 0, SpringLayout.EAST, btnRemoveCol);
		sl_pnlFields.putConstraint(SpringLayout.SOUTH, scrollPane, -6, SpringLayout.SOUTH, pnlFields);
		pnlFields.add(scrollPane);
		
		iFieldList = new JList<Field>();
		scrollPane.setViewportView(iFieldList);
		
		JPanel pnlMisc = new JPanel();
		sl_pnlFields.putConstraint(SpringLayout.NORTH, pnlMisc, 0, SpringLayout.NORTH, btnAddCol);
		sl_pnlFields.putConstraint(SpringLayout.WEST, pnlMisc, 6, SpringLayout.EAST, btnRemoveCol);
		sl_pnlFields.putConstraint(SpringLayout.EAST, pnlMisc, -6, SpringLayout.EAST, pnlFields);
		sl_pnlFields.putConstraint(SpringLayout.SOUTH, pnlMisc, -6, SpringLayout.SOUTH, pnlFields);
		pnlFields.add(pnlMisc);
		
		JLabel lblColumnName = new JLabel("Имя");
		JLabel lblColumnDelimiter = new JLabel("Ограничитель столбца");
		JLabel lblDataType = new JLabel("Тип данных");
		JLabel lblFormat = new JLabel("Формат");
		
		initControls();
		
		iColumnName = new JTextField();
		//iColumnDelimiter = new JComboBox<CBXItem<String>>();		
		//iDataType = new JComboBox<CBXItem<Class>>();
		iFormat = new JComboBox<String>();
		
		SpringLayout sl_pnlMisc = new SpringLayout();
		
		sl_pnlMisc.putConstraint(SpringLayout.NORTH, iColumnName, 6, SpringLayout.NORTH, pnlMisc);
		sl_pnlMisc.putConstraint(SpringLayout.WEST, iColumnName, 122, SpringLayout.WEST, pnlMisc);
		sl_pnlMisc.putConstraint(SpringLayout.EAST, iColumnName, -6, SpringLayout.EAST, pnlMisc);
		
		sl_pnlMisc.putConstraint(SpringLayout.NORTH, iColumnDelimiter, 5, SpringLayout.SOUTH, iColumnName);
		sl_pnlMisc.putConstraint(SpringLayout.WEST, iColumnDelimiter, 0, SpringLayout.WEST, iColumnName);
		sl_pnlMisc.putConstraint(SpringLayout.EAST, iColumnDelimiter, 0, SpringLayout.EAST, iColumnName);
		
				
		sl_pnlMisc.putConstraint(SpringLayout.NORTH, iDataType, 5, SpringLayout.SOUTH, iColumnDelimiter);
		sl_pnlMisc.putConstraint(SpringLayout.WEST, iDataType, 0, SpringLayout.WEST, iColumnName);
		sl_pnlMisc.putConstraint(SpringLayout.EAST, iDataType, 0, SpringLayout.EAST, iColumnName);
		
		sl_pnlMisc.putConstraint(SpringLayout.NORTH, iFormat, 6, SpringLayout.SOUTH, iDataType);
		sl_pnlMisc.putConstraint(SpringLayout.WEST, iFormat, 0, SpringLayout.WEST, iColumnName);
		sl_pnlMisc.putConstraint(SpringLayout.EAST, iFormat, 0, SpringLayout.EAST, iColumnName);
		
		sl_pnlMisc.putConstraint(SpringLayout.SOUTH, lblColumnName, -3, SpringLayout.SOUTH, iColumnName);
		sl_pnlMisc.putConstraint(SpringLayout.SOUTH, lblColumnDelimiter, -3, SpringLayout.SOUTH, iColumnDelimiter);
		sl_pnlMisc.putConstraint(SpringLayout.SOUTH, lblDataType, -3, SpringLayout.SOUTH, iDataType);
		sl_pnlMisc.putConstraint(SpringLayout.SOUTH, lblFormat, -3, SpringLayout.SOUTH, iFormat);
		
		
		pnlMisc.setLayout(sl_pnlMisc);
		
		pnlMisc.add(iColumnName);
		pnlMisc.add(iColumnDelimiter);
		pnlMisc.add(iDataType);
		pnlMisc.add(iFormat);
		
		pnlMisc.add(lblColumnName);
		pnlMisc.add(lblColumnDelimiter);
		pnlMisc.add(lblDataType);
		pnlMisc.add(lblFormat);
		
		JCheckBox iOptional = new JCheckBox("Необязательное");
		sl_pnlMisc.putConstraint(SpringLayout.NORTH, iOptional, 6, SpringLayout.SOUTH, iFormat);
		sl_pnlMisc.putConstraint(SpringLayout.WEST, iOptional, 0, SpringLayout.WEST, iColumnName);
		pnlMisc.add(iOptional);
		
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
