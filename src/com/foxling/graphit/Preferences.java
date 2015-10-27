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

package com.foxling.graphit;

import java.awt.EventQueue;
import java.util.Arrays;
import java.util.Date;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SpringLayout;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.EmptyBorder;

import org.joda.time.DateTime;

import java.awt.BorderLayout;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.print.attribute.standard.DateTimeAtCompleted;
import javax.swing.JButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JComboBox;
import javax.swing.JSpinner;
import javax.swing.JCheckBox;

public class Preferences extends JFrame {
	private static final long serialVersionUID = 3103016344816004897L;
	private JPanel contentPane;
	private JTextField iColumnName;
	private JComboBox<CBXItem<String>> iColumnDelimiter;		
	private JComboBox<CBXItem<Class>> iDataType;
	private JSpinner iScale;
	
	
	public Preferences() {
		super("Настройки");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 464, 400);
		
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(1, 1, 1, 1));
		setContentPane(contentPane);
		contentPane.setLayout(new BorderLayout(0, 0));
		
		JPanel panel = new JPanel();
		contentPane.add(panel);
		SpringLayout sl_panel = new SpringLayout();
		panel.setLayout(sl_panel);
		
		JLabel lblFields = new JLabel("Настройка полей лог файла.");
		sl_panel.putConstraint(SpringLayout.NORTH, lblFields, 10, SpringLayout.NORTH, panel);
		sl_panel.putConstraint(SpringLayout.WEST, lblFields, 10, SpringLayout.WEST, panel);
		panel.add(lblFields);
		
		JButton btnAddCol = new JButton("Добавить");
		sl_panel.putConstraint(SpringLayout.NORTH, btnAddCol, 6, SpringLayout.SOUTH, lblFields);
		sl_panel.putConstraint(SpringLayout.WEST, btnAddCol, 10, SpringLayout.WEST, panel);
		panel.add(btnAddCol);
		
		JButton btnRemoveCol = new JButton("Удалить");
		sl_panel.putConstraint(SpringLayout.NORTH, btnRemoveCol, 0, SpringLayout.NORTH, btnAddCol);
		sl_panel.putConstraint(SpringLayout.WEST, btnRemoveCol, 6, SpringLayout.EAST, btnAddCol);
		panel.add(btnRemoveCol);
		
		JScrollPane scrollPane = new JScrollPane();
		sl_panel.putConstraint(SpringLayout.NORTH, scrollPane, 6, SpringLayout.SOUTH, btnAddCol);
		sl_panel.putConstraint(SpringLayout.WEST, scrollPane, 0, SpringLayout.WEST, btnAddCol);
		sl_panel.putConstraint(SpringLayout.EAST, scrollPane, 0, SpringLayout.EAST, btnRemoveCol);
		sl_panel.putConstraint(SpringLayout.SOUTH, scrollPane, -6, SpringLayout.SOUTH, panel);
		panel.add(scrollPane);
		
		JList fieldList = new JList();
		scrollPane.setViewportView(fieldList);
		
		JPanel pnlMisc = new JPanel();
		sl_panel.putConstraint(SpringLayout.NORTH, pnlMisc, 0, SpringLayout.NORTH, btnAddCol);
		sl_panel.putConstraint(SpringLayout.WEST, pnlMisc, 6, SpringLayout.EAST, btnRemoveCol);
		sl_panel.putConstraint(SpringLayout.EAST, pnlMisc, -6, SpringLayout.EAST, panel);
		sl_panel.putConstraint(SpringLayout.SOUTH, pnlMisc, -6, SpringLayout.SOUTH, panel);
		panel.add(pnlMisc);
		
		JLabel lblColumnName = new JLabel("Имя");
		JLabel lblColumnDelimiter = new JLabel("Разделитель столбца");
		JLabel lblDataType = new JLabel("Тип данных");
		JLabel lblScale = new JLabel("Округление");
		
		initControls();
		
		iColumnName = new JTextField();
		//iColumnDelimiter = new JComboBox<CBXItem<String>>();		
		//iDataType = new JComboBox<CBXItem<Class>>();
		iScale = new JSpinner();
		
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
		
		sl_pnlMisc.putConstraint(SpringLayout.NORTH, iScale, 6, SpringLayout.SOUTH, iDataType);
		sl_pnlMisc.putConstraint(SpringLayout.WEST, iScale, 0, SpringLayout.WEST, iColumnName);
		sl_pnlMisc.putConstraint(SpringLayout.EAST, iScale, 0, SpringLayout.EAST, iColumnName);
		
		sl_pnlMisc.putConstraint(SpringLayout.SOUTH, lblColumnName, -3, SpringLayout.SOUTH, iColumnName);
		sl_pnlMisc.putConstraint(SpringLayout.SOUTH, lblColumnDelimiter, -3, SpringLayout.SOUTH, iColumnDelimiter);
		sl_pnlMisc.putConstraint(SpringLayout.SOUTH, lblDataType, -3, SpringLayout.SOUTH, iDataType);
		sl_pnlMisc.putConstraint(SpringLayout.SOUTH, lblScale, -3, SpringLayout.SOUTH, iScale);
		
		
		pnlMisc.setLayout(sl_pnlMisc);
		
		pnlMisc.add(iColumnName);
		pnlMisc.add(iColumnDelimiter);
		pnlMisc.add(iDataType);
		pnlMisc.add(iScale);
		
		pnlMisc.add(lblColumnName);
		pnlMisc.add(lblColumnDelimiter);
		pnlMisc.add(lblDataType);
		pnlMisc.add(lblScale);
		
		JCheckBox iOptional = new JCheckBox("Необязательное");
		sl_pnlMisc.putConstraint(SpringLayout.NORTH, iOptional, 6, SpringLayout.SOUTH, iScale);
		sl_pnlMisc.putConstraint(SpringLayout.WEST, iOptional, 0, SpringLayout.WEST, iColumnName);
		pnlMisc.add(iOptional);
	}
	
	private void initControls(){
		iColumnDelimiter = new JComboBox<CBXItem<String>>(new Vector<CBXItem<String>>(Arrays.asList(
				new CBXItem<String>("{CR}{LF}", ""),
				new CBXItem<String>("{CR}", ""),
				new CBXItem<String>("{LF}", ""),
				new CBXItem<String>("Точка с запятой {;}", ";"),
				new CBXItem<String>("Двоеточие {:}", ""),
				new CBXItem<String>("Запятая {,}", ""),
				new CBXItem<String>("Табуляция {t}", ""),
				new CBXItem<String>("Вертикальная черта {|}", "")
		)));
		
		iDataType = new JComboBox<CBXItem<Class>>(new Vector<CBXItem<Class>>(Arrays.asList(
				new CBXItem<Class>("Строка", String.class),
				new CBXItem<Class>("Целое число", Integer.class),
				new CBXItem<Class>("Float", Float.class),
				new CBXItem<Class>("Double", Double.class),
				new CBXItem<Class>("Дата/Время", DateTime.class),
				new CBXItem<Class>("Логический", Boolean.class)
		)));
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
					Preferences frame = new Preferences();
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
