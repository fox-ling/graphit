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
import java.awt.Color;
import java.awt.Component;
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

import java.util.Random;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;

public class EventViewerFrame extends JFrame {
	private static final long serialVersionUID = -5784368020743203402L;
	private JPanel contentPane;
	private JTable table;
	private JSplitPane splitPane;
	private JTextArea textArea;
	private LoggerMemoryHandler handler;

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
	
	public static void main(String[] args) {
		EventViewerFrame frame = new EventViewerFrame();
		frame.setVisible(true);
		Logger logger = Logger.getLogger(EventViewerFrame.class.getName());
		for (int j = 0; j < 3; j++)
		for (int i = 0; i < 3; i++) {
			Level level = Level.INFO;
			switch (i) {
			case 1: level = Level.WARNING; break;
			case 2: level = Level.SEVERE; break; 
			}
			try {
				thrower(i);
				logger.log(level, "All is well at i={0} and j={1}", new Object[]{i, j});
			} catch (Exception e) {
				logger.log(level, String.format("Some error at i=%d and j=%d", i, j), e);
			}
		}
	}
	
	public static void thrower(int i) throws Exception {
		String msg = String.format("ExceptionGenerator #%d", i);
		switch (i) {
		case 1: throw new IllegalArgumentException(msg);
		case 2: throw new IllegalStateException(msg);
		}
	}

	public EventViewerFrame() {
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
		
		JScrollPane scrollPane = new JScrollPane();
		splitPane.setLeftComponent(scrollPane);
		
		table = new JTable(new LoggerMemoryHandlerTableModel());
		table.getColumnModel().getColumn(0).setCellRenderer(new LogRecordsCellRenderer());
		//table.getSelectionModel().addListSelectionListener();
		scrollPane.setViewportView(table);
		
		textArea = new JTextArea();
		splitPane.setRightComponent(textArea);
		
		this.handler = Core.getMemoryHandler();
	}
	
	private class LoggerRecSelectionListener
	implements ListSelectionListener {
		@Override
		/** Listener for row-change events (Chart trace) */
		public void valueChanged(ListSelectionEvent e) {
			if (e.getValueIsAdjusting()) return;
			
			ListSelectionModel rowSM = (ListSelectionModel) e.getSource();
			int selectedIndex = rowSM.getMinSelectionIndex();
			
			
		}
	}
	
	/** AbstractTableModel that eats LogRecord rows */
	private class LoggerMemoryHandlerTableModel
	extends AbstractTableModel {
		private static final long serialVersionUID = 369059275490713848L;
		//private LoggerMemoryHandler handler;
		private String[] columnNames = {"Уровень", "Дата/Время", "Источник", "Сообщение"};
		private Class<?>[] columnClasses = {Level.class, String.class, String.class, String.class};

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
	
	//private class ErrorCellRenderer
	
	/** Table cell renderer that adds icons Logger.Level values (SEVERE/WARNING/INFO) */
	private class LogRecordsCellRenderer
	extends DefaultTableCellRenderer {
	    private static final long serialVersionUID = 4264832765857567868L;
	    //private final DefaultTableCellRenderer DEFAULT_RENDERER = new DefaultTableCellRenderer();

	    private int height;
	    
	    public LogRecordsCellRenderer() {
			height = getFontMetrics(getFont()).getHeight();
		}
	    
	    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
			//Component renderer = DEFAULT_RENDERER.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			/*Color foreground, background;
			if (isSelected) {
				foreground = Color.YELLOW;
				background = Color.GREEN;
			}  else {
				if (row % 2 == 0) {
					foreground = Color.BLUE;
					background = Color.WHITE;
				}  else {
					foreground = Color.WHITE;
					background = Color.BLUE;
				}
			}
			setForeground(foreground);
			setBackground(background);*/
	    	
			return this;
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
