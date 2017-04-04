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
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;

import ru.foxling.graphit.Core;
import ru.foxling.graphit.LoggerIcons;
import ru.foxling.graphit.LoggerMemoryHandler;
import ru.foxling.graphit.utils.Resources;

import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.JPopupMenu;

public class EventJournalFrame extends JFrame {
	private static final long serialVersionUID = -5784368020743203402L;
	private static JFileChooser fileChooser;
	
	private static Logger LOG = Logger.getLogger(EventJournalFrame.class.getName());
	private static final String NEW_LINE = "\n\r";
	private JPanel contentPane;
	private JTable table;
	private JSplitPane splitPane;
	private JTextArea txtEventText;
	private LoggerMemoryHandler handler; 
	private JPopupMenu pmTable;

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
        setIconImage(Resources.getFrameIcon());
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
		
		pmTable = new JPopupMenu();
		
		JMenuItem miClear = new JMenuItem("Очистить");
		miClear.addActionListener(e -> Core.getMemoryHandler().flush());
		pmTable.add(miClear);
		
		JMenuItem miSaveLog = new JMenuItem("Сохранить в файл");
		miSaveLog.addActionListener(e -> {
			if (fileChooser == null) {
				fileChooser = new JFileChooser(".");
				fileChooser.setFileFilter(new EndsWithFilter("Текстовый файл", ".txt"));
			}
			fileChooser.setSelectedFile(new File(fileChooser.getCurrentDirectory(), LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH-mm-ss"))+ " event log.txt"));
			if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
				File file = fileChooser.getSelectedFile();
				if (file.exists()) {
					if (JOptionPane.YES_OPTION != JOptionPane.showOptionDialog(this,
							"Файл " + file.getName() + " уже существует, перезаписать?",
							"Внимание",
						    JOptionPane.YES_NO_OPTION,
						    JOptionPane.WARNING_MESSAGE, null, null, null)) {
						LOG.log(Level.INFO, "Операция сохранения отменена");
						return;
					}
				}
				
				try {
					OutputStream outStream = new FileOutputStream(file);
					try(Writer writer = new OutputStreamWriter(outStream, "UTF-8")){
						LoggerMemoryHandler h = Core.getMemoryHandler();
						for (int i = 0; i < h.getSize(); i++) {
							writer.write(formatLogRecord(h.getRecord(i)));
							writer.write(NEW_LINE);
							writer.write("========================================================");
							writer.write(NEW_LINE);
						}
					}
				//} catch (FileNotFoundException e1) {
				} catch (Exception e2) {
					LOG.log(Level.WARNING, "Не удалось записать лог в файл " + file.getAbsolutePath(), e2);
				}
			}
		});
		pmTable.add(miSaveLog);

		MainFrame.addPopup(table, pmTable);
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
	
	private String formatLogRecord(LogRecord rec){
		StringBuilder sb = new StringBuilder(250);
		sb.append("DateTime: ").append(datetimeFromEpoch(rec.getMillis())).append(NEW_LINE);
		sb.append("Level: ").append(rec.getLevel()).append(NEW_LINE);
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
		return sb.toString();
	}
	
	private class LoggerRecSelectionListener
	implements ListSelectionListener {
		@Override
		/** Listener for row-change events (Chart trace) */
		public void valueChanged(ListSelectionEvent e) {
			if (e.getValueIsAdjusting()) return;
			
			int index = ((ListSelectionModel) e.getSource()).getMinSelectionIndex();
			txtEventText.setText(formatLogRecord(getRecord(index)));
			txtEventText.setCaretPosition(0);
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
	
	class EndsWithFilter extends FileFilter {
		private String ending;

		private String description;

		public EndsWithFilter(String description, String ending) {
			this.description = description;
			this.ending = ending;
		}

		public boolean accept(File file) {
			if (file.isDirectory()) {
				return true;
			}
			String path = file.getAbsolutePath();
			if (path.endsWith(ending))
				return true;
			return false;
		}

		public String getDescription() {
			return (description == null ? ending : description);
		}
	}
}
