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

package ru.foxling.graphit;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;

import java.util.logging.Level;
import java.util.logging.LogRecord;
import javax.swing.JScrollPane;
import javax.swing.JTable;

public class EventViewerFrame extends JFrame {
	private static final long serialVersionUID = -5784368020743203402L;
	private JPanel contentPane;
	private JTable table;

	public static void launch() {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					EventViewerFrame frame = new EventViewerFrame();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	public EventViewerFrame() {
		super("Журнал событий");
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setBounds(100, 100, 886, 300);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);
		
		JScrollPane scrollPane = new JScrollPane();
		contentPane.add(scrollPane, BorderLayout.CENTER);
		
		table = new JTable(new LoggerMemoryHandlerTableModel(Core.getMemoryHandler()));
		table.getColumnModel().getColumn(0).setCellRenderer(new LogLevelCellRenderer());
		scrollPane.setViewportView(table);
		Core.getMemoryHandler().addChangeListener(e -> {
			System.out.println(Core.getMemoryHandler().getSize());
		});
	}
	
	/** AbstractTableModel that eats LogRecord rows */
	private class LoggerMemoryHandlerTableModel
	extends AbstractTableModel {
		private static final long serialVersionUID = 369059275490713848L;
		private LoggerMemoryHandler handler;
		private String[] columnNames = {"Уровень", "Дата/Время", "Источник", "Сообщение"};
		private Class<?>[] columnClasses = {Level.class, String.class, String.class, String.class};

		public LoggerMemoryHandlerTableModel(LoggerMemoryHandler handler) {
			if (handler == null)
				throw new IllegalArgumentException();
			
			this.handler = handler;
			handler.addChangeListener(e -> fireTableDataChanged());
		}
		
		@Override
		public String getColumnName(int columnIndex){
			return columnNames[columnIndex];
		}

		@Override
		public int getColumnCount() {
			return columnNames.length;
		}

		@Override
		public int getRowCount() {
			return handler.getSize();
		}

		@Override
		public Object getValueAt(int row, int col) {
			LogRecord rec = handler.getRecord(getRowCount() - 1 - row);
			switch (col) {
			case 0: return rec.getLevel();
			case 1: return LocalDateTime.ofInstant(Instant.ofEpochMilli(rec.getMillis()), ZoneId.systemDefault()).format(Core.F_DATETIME);
			case 2: return rec.getLoggerName();
			case 3: return handler.getFormattedMessage(rec);
			}
			return null;
		}
		
		@Override
		public Class<?> getColumnClass(int col) {
			return columnClasses[col];
		}
		
	}
	
	/** Table cell renderer that adds icons Logger.Level values (SEVERE/WARNING/INFO) */
	private class LogLevelCellRenderer
	extends DefaultTableCellRenderer {
	    private static final long serialVersionUID = 4264832765857567868L;
	    private int height;
	    
	    public LogLevelCellRenderer() {
			height = getFontMetrics(getFont()).getHeight();
		}

	    public void setValue(Object value) {
	    	if (value == null) {
	    		setText("");
	    		return;
	    	}
	    	if (value instanceof Level) {
	    		setIcon(Icons.get((Level) value, height));
	    		if (value == Level.SEVERE) {
	    			setText("Ошибка");
	    		} else if (value == Level.WARNING) {
	    			setText("Предупреждение");
	    		} else if (value == Level.INFO) {
	    			setText("Сведения");
	    		} else
	    			setText(value.toString());
	    	} else {
	    		setIcon(null);
	    		setText(value.toString());
	    	}
	    }
	}

}
