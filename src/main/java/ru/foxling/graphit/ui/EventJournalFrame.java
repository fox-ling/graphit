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

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;

import ru.foxling.graphit.Core;
import ru.foxling.graphit.LoggerIcons;
import ru.foxling.graphit.LoggerMemoryHandler;

import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;

public class EventJournalFrame extends JFrame {
	private static final long serialVersionUID = -5784368020743203402L;
	private static Logger LOG = Logger.getLogger(EventJournalFrame.class.getName());
	private static final String NEW_LINE = "\n\r";
	private JPanel contentPane;
	private JTable table;
	private JSplitPane splitPane;
	private JTextArea txtEventText;
	private LoggerMemoryHandler handler; 

	public static void launch() {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					EventJournalFrame frame = new EventJournalFrame();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	public EventJournalFrame() {
		super("Журнал событий");
		handler = Core.getMemoryHandler();
		if (handler == null)
			throw new IllegalStateException();
		
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setBounds(100, 100, 886, 600);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);
		
		splitPane = new JSplitPane();
		splitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
		contentPane.add(splitPane, BorderLayout.CENTER);
		
		JScrollPane scpTable = new JScrollPane();
		splitPane.setLeftComponent(scpTable);
		
		table = new JTable(new LoggerMemoryHandlerTableModel());
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.setDefaultRenderer(Level.class, new LogLevelCellRenderer());
		table.setDefaultRenderer(Throwable.class, new ThrowableCellRenderer());
		table.getSelectionModel().addListSelectionListener(new LoggerRecSelectionListener());
		scpTable.setViewportView(table);
		
		txtEventText = new JTextArea(8,0);
		JScrollPane scpEventText = new JScrollPane();
		scpEventText.setViewportView(txtEventText);
		splitPane.setRightComponent(scpEventText);
		splitPane.setResizeWeight(.6);
	}
	
	private String datetimeFromEpoch(long millis) {
		try {
			Instant instant = Instant.ofEpochMilli(millis);
			LocalDateTime datetime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
			return datetime.format(Core.F_DATETIME);
		} catch (Exception e) {
			LOG.log(Level.CONFIG, "Unable to parse EPOCH to string", e);
			return Long.toString(millis);
		}
	}
	
	private LogRecord getRecord(int index) {
		return handler.getRecord(handler.getSize() - 1 - index);
	}
	
	private String throwableToStr(Throwable thrown) {
		if (thrown == null)
			throw new IllegalArgumentException();
		
		StringBuilder sb = new StringBuilder(250);
		if (thrown != null) {
			sb.append(thrown.getClass().getName());
			if (thrown.getMessage() != null)
				sb.append(": ").append(thrown.getMessage());
			sb.append(NEW_LINE);
			for (StackTraceElement ste : thrown.getStackTrace())
				sb.append(String.format("\t at %s%n", ste.toString()));
			if (thrown.getCause() != null)
				sb.append("Caused by: ").append(throwableToStr(thrown.getCause()));
		}
		return sb.toString();
	}
	
	private class LoggerRecSelectionListener
	implements ListSelectionListener {
		@Override
		/** Listener for row-change events (Chart trace) */
		public void valueChanged(ListSelectionEvent e) {
			if (e.getValueIsAdjusting()) return;
			
			int index = ((ListSelectionModel) e.getSource()).getMinSelectionIndex();
			LogRecord rec = getRecord(index);
			
			StringBuilder sb = new StringBuilder(250);
			sb.append("Level: ").append(rec.getLevel()).append(NEW_LINE);
			sb.append("DateTime: ").append(datetimeFromEpoch(rec.getMillis())).append(NEW_LINE);
			sb.append("Source: ").append(rec.getSourceClassName()).append(":").append(rec.getSourceMethodName()).append(NEW_LINE);
			if (rec.getMessage() != null) {
				String msg = handler.getFormatter() == null ? rec.getMessage() : handler.getFormatter().formatMessage(rec);
				sb.append("Message: ").append(msg).append(NEW_LINE);
			}
			
			Throwable thrown = rec.getThrown();
			if (thrown != null) {
				sb.append("--- Thrown ---------------------------").append(NEW_LINE);
				sb.append(throwableToStr(thrown));
			}
			txtEventText.setText(sb.toString());
		}
	}
	
	/** AbstractTableModel that eats LogRecord rows */
	private class LoggerMemoryHandlerTableModel
	extends AbstractTableModel {
		private static final long serialVersionUID = 369059275490713848L;
		//private LoggerMemoryHandler handler;
		private String[] columnNames = {"Уровень", "Дата/Время", "Источник", "Сообщение", "Исключение"};
		private Class<?>[] columnClasses = {Level.class, String.class, String.class, String.class, Throwable.class};

		public LoggerMemoryHandlerTableModel() {
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
			LogRecord rec = getRecord(row);
			switch (col) {
			case 0: return rec.getLevel();
			case 1: return datetimeFromEpoch(rec.getMillis());
			case 2: return rec.getLoggerName();
			case 3: return handler.getFormatter().formatMessage(rec);
			case 4: return rec.getThrown();
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
	    		setIcon(null);
	    	} else {
	    		setIcon(LoggerIcons.get((Level) value, height));
	    		if (value == Level.SEVERE) {
	    			setText("Ошибка");
	    		} else if (value == Level.WARNING) {
	    			setText("Предупреждение");
	    		} else if (value == Level.INFO) {
	    			setText("Сведения");
	    		} else
	    			setText(value.toString());
	    	}
	    }
	}
	
	/** Table cell renderer that adds icons Logger.Level values (SEVERE/WARNING/INFO) */
	private class ThrowableCellRenderer
	extends DefaultTableCellRenderer {
	    private static final long serialVersionUID = 4264832765857567868L;

	    public void setValue(Object value) {
	    	if (value == null) {
	    		setText("");
	    	} else
	    		setText(((Throwable) value).getClass().getSimpleName());
	    }
	}

}
