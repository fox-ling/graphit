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

import java.awt.Dimension;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JMenuItem;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.event.ChartProgressEvent;
import org.jfree.chart.event.ChartProgressListener;
import org.jfree.chart.plot.XYPlot;
import ru.foxling.graphit.Core;
import ru.foxling.graphit.LoggerLabelHandler;
import ru.foxling.graphit.config.ConfigModel;
import ru.foxling.graphit.config.DataType;
import ru.foxling.graphit.config.Field;
import ru.foxling.graphit.config.FieldRole;
import ru.foxling.graphit.logfile.LogFile;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JTable;

import java.awt.FlowLayout;

import javax.swing.JSplitPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.JMenuBar;
import javax.swing.JMenu;

import java.awt.event.InputEvent;

import javax.swing.JTextField;
import javax.swing.JPopupMenu;
import java.awt.Component;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JCheckBox;

import java.awt.Font;
import java.awt.Rectangle;
import java.awt.Color;
import net.miginfocom.swing.MigLayout;

import java.awt.BorderLayout;
import javax.swing.JLabel;
import javax.swing.border.EtchedBorder;

public class MainFrame
extends JFrame implements ChartProgressListener {
	private static final long serialVersionUID = 1L;
	private static final String APPNAME = "Graphit - ИСУ \"Оптима\" ";
	//private static final Logger LOG = Logger.getLogger(MainFrame.class.getName());
	
	private JPanel contentPane;
	private JMenu mFile = null;
	private LogFile logFile = null;
	private ChartPanel chartPanel = null;
	
	private JMenuItem miDetails;
	private JSplitPane splitPane;
	private JScrollPane spTable;
	private JTable table;
	private static TableModel blankTableModel;
	private JTextField tfCurrFile;
	private JPopupMenu popupMenu;
	private JMenu mRecent;
	private JCheckBox iLaunch;
	private JCheckBox cbTable;
	private ConfigController configController;
	private JPanel pAxes;
	private JMenu mSettings;
	private JMenu mYAxes;
	private JMenuItem miPreferences;
	private JPanel pnlStatusBar;
	private JLabel lblLogMessage;
	private JMenuItem miShowEventJournal;
	private LoggerLabelHandler loggerLabelHandler;
	
	
	public MainFrame() {
		super(APPNAME);
		
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 850, 600);
		
		JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);
		
		mFile = new JMenu("Файл");
		menuBar.add(mFile);
		
		final JMenuItem miOpen = new JMenuItem("Открыть");
		miOpen.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				final JFileChooser fc = new JFileChooser();
				if (fc.showOpenDialog(miOpen)==JFileChooser.APPROVE_OPTION){
					openLogFile(fc.getSelectedFile());
				}
			}
		});
		miOpen.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_MASK));
		mFile.add(miOpen);
		
		mRecent = new JMenu("Последние файлы");
		mRecent.setEnabled(false);
		mFile.add(mRecent);
		
		mFile.addSeparator();
		
		miDetails = new JMenuItem("Детали");
		miDetails.setEnabled(false);
		miDetails.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (logFile != null) {
					DetailsFrame fDetailsFrame = new DetailsFrame(logFile);
					fDetailsFrame.setVisible(true);
				}
			}
		});
		//miDetails.setIcon(getResourceIcon("ic_action_storage.png"));
		miDetails.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D, InputEvent.CTRL_MASK));
		mFile.add(miDetails);
		
		mRecent.addSeparator();
		JMenuItem mi = new JMenuItem("Очистить список");
		mi.addActionListener(e -> configController.removeRecentFiles());
		mRecent.add(mi);
		
		miShowEventJournal = new JMenuItem("Журнал событий");
		miShowEventJournal.addActionListener(e -> EventJournalFrame.launch());
		miShowEventJournal.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_J, InputEvent.CTRL_MASK));
		mFile.add(miShowEventJournal);
		
		mSettings = new JMenu("Опции");
		menuBar.add(mSettings);
		
		mYAxes = new JMenu("Оси Y");
		mSettings.add(mYAxes);
		mSettings.addSeparator();
		
		miPreferences = new JMenuItem("Настройки");
		miPreferences.addActionListener(e -> ConfigFrame.launch());
		miPreferences.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, InputEvent.CTRL_MASK));
		mSettings.add(miPreferences);
		
		tfCurrFile = new JTextField();
		tfCurrFile.setEditable(false);
		menuBar.add(tfCurrFile);
		tfCurrFile.setColumns(10);
		
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(1, 1, 1, 1));
		setContentPane(contentPane);
		
	    chartPanel = new ChartPanel(null);//chart.chart
	    chartPanel.setBackground(Color.WHITE);
		FlowLayout flowLayout = (FlowLayout) chartPanel.getLayout();
		flowLayout.setAlignment(FlowLayout.LEADING);
		
		table = new JTable();
		table.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
		table.setCellSelectionEnabled(true);
		table.setFillsViewportHeight(true);
		TableCellRenderer dtcr = table.getTableHeader().getDefaultRenderer();
		table.getTableHeader().setDefaultRenderer(new TableCellRenderer() {
			@Override
			public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
				JLabel label = (JLabel) dtcr.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
				label.setHorizontalAlignment(SwingConstants.CENTER);
				return label;
			}
		});
		
		table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			@Override
			/** Listener for row-change events (Chart trace) */
			public void valueChanged(ListSelectionEvent e) {
				if (!table.isFocusOwner() || e.getValueIsAdjusting()) return;
				
				ListSelectionModel rowSM = (ListSelectionModel) e.getSource();
				int selectedIndex = rowSM.getMinSelectionIndex();
				
				if (Chart.getInstance() != null) {
					LogFileTableModel model = (LogFileTableModel) table.getModel();
					try {
						long pos = -1;
						switch (Chart.getxField().getDatatype()) {
						case TIME: {
							LocalTime time = (LocalTime) model.getValueAt(selectedIndex, Chart.getxFieldId());
							ZonedDateTime zdt = ZonedDateTime.of(LocalDateTime.of(LocalDate.ofEpochDay(0), time), ZoneId.systemDefault());
							pos = zdt.toEpochSecond() * 1000;
							break;
						}
						case TIME_SEQUENCE:
						case DATETIME: {
							//TODO
							LocalDateTime datetime = (LocalDateTime) model.getValueAt(selectedIndex, Chart.getxFieldId());
							ZonedDateTime zdt = ZonedDateTime.of(datetime, ZoneId.systemDefault());
							pos = zdt.toEpochSecond() * 1000;
							break;
						}
						case DATE: {
							LocalDate date = (LocalDate) model.getValueAt(selectedIndex, Chart.getxFieldId());
							ZonedDateTime zdt = ZonedDateTime.of(LocalDateTime.of(date, LocalTime.MIN), ZoneId.systemDefault());
							pos = zdt.toEpochSecond() * 1000;
							break;
						}
						default:
							break;
						}
						
						if (pos > -1) {
							XYPlot xyplot = (XYPlot)Chart.getInstance().getPlot();
							xyplot.setDomainCrosshairValue(pos, true);
							
						}
					} catch (Exception e1) {
						e1.printStackTrace();
					}
				}
			}
		});
		
		spTable = new JScrollPane(table);
		spTable.setVisible(false);
		
		popupMenu = new JPopupMenu();
		addPopup(table, popupMenu);
		
		contentPane.setLayout(new BorderLayout(0, 0));
		
		
		JPanel toppanel = new JPanel();
		toppanel.setPreferredSize(new Dimension(560,367));
		toppanel.setLayout(new BorderLayout(0, 0));
		
		toppanel.add(chartPanel);
		
		splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, toppanel, spTable);
	    splitPane.setResizeWeight(.7);
	    contentPane.add(splitPane, BorderLayout.CENTER);
		
	    Runtime.getRuntime().addShutdownHook(new Thread(){
			@Override
			public void run()
			{
				ConfigModel.getInstance().saveConfig();
			}
	    });

		// Create the drag and drop listener
	    CustomDragDropListener customDragDropListener = new CustomDragDropListener();
	    new DropTarget(chartPanel, customDragDropListener);
	    
	    JPanel pTools = new JPanel();
	    pTools.setBackground(Color.WHITE);
	    toppanel.add(pTools, BorderLayout.SOUTH);
	    pTools.setLayout(new MigLayout("insets 0", "[][grow][67px]", "[23px]"));
	    
	    iLaunch = new JCheckBox("Запуск");
	    pTools.add(iLaunch, "cell 0 0");
	    iLaunch.setBackground(Color.WHITE);
	    iLaunch.setForeground(Color.BLACK);
	    iLaunch.setFont(new Font("Tahoma", Font.BOLD, 11));
	    
	    pAxes = new JPanel();
	    pAxes.setBackground(Color.WHITE);
	    FlowLayout flowLayout_1 = (FlowLayout) pAxes.getLayout();
	    flowLayout_1.setAlignment(FlowLayout.LEFT);
	    flowLayout_1.setVgap(0);
	    pTools.add(pAxes, "cell 1 0,grow");
	    
	    cbTable = new JCheckBox("Таблица");
	    cbTable.setEnabled(false);
	    cbTable.setBackground(Color.WHITE);
	    pTools.add(cbTable, "cell 2 0,alignx right,aligny top");
	    

	    pnlStatusBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 1, 1));
	    pnlStatusBar.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
	    pnlStatusBar.setBackground(Color.WHITE);
	    contentPane.add(pnlStatusBar, BorderLayout.SOUTH);
	    
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
	    pnlStatusBar.add(lblLogMessage);

	    this.loggerLabelHandler = new LoggerLabelHandler(lblLogMessage, Level.INFO);
	    
	    Logger.getLogger(Core.class.getPackage().getName()).addHandler(loggerLabelHandler);
	    configController = new ConfigController(ConfigModel.getInstance());
	}
	
	/** Drops current session */
	private void reset() {
		chartPanel.setChart(null);
		if (blankTableModel == null) {
			blankTableModel = new TableModel(){
				@Override
				public int getRowCount() { return 0; }
				@Override
				public int getColumnCount() { return 0; }
				@Override
				public String getColumnName(int columnIndex) { return null; }
				@Override
				public Class<?> getColumnClass(int columnIndex) { return null; }
				@Override
				public boolean isCellEditable(int rowIndex, int columnIndex) { return false; }
				@Override
				public Object getValueAt(int rowIndex, int columnIndex) { return null; }
				@Override
				public void setValueAt(Object aValue, int rowIndex, int columnIndex) {}
				@Override
				public void addTableModelListener(TableModelListener l) {}
				@Override
				public void removeTableModelListener(TableModelListener l) {}
				
			};
		}
		table.setModel(blankTableModel);
		logFile = null;
	}
	
	private void openLogFile(File aFile){
		logFile = new LogFile(ConfigModel.getInstance(), aFile.getPath());
		try {
			logFile.readFile();
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null, "Не удалось прочитать файл " + aFile.getName(), "Ошибка", JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
			return;
		}
		tfCurrFile.setText(logFile.getFileName());
			
		StringBuilder titleBuilder = new StringBuilder(APPNAME);
		
		if (logFile.getStartups().size() > 0 && logFile.getStartups().get(0).getDate() != null)
			titleBuilder.append(" Дата: ").append(logFile.getStartups().get(0).getDate().format(Core.F_DATE)).append(", ");
		
		if (logFile.getSerialNo() != null && !logFile.getSerialNo().isEmpty())
			titleBuilder.append("Сер.№ ").append(logFile.getSerialNo()).append(", ");
		
		if (logFile.getFrimware() != null && !logFile.getFrimware().isEmpty())
			titleBuilder.append("ПО ").append(logFile.getFrimware());
		
		String title = titleBuilder.toString();
		if (title.endsWith(", ")) title = title.substring(0, title.length() - 3);
		this.setTitle(title);
		
		table.setModel(new LogFileTableModel(ConfigModel.getInstance().getFieldList(), logFile.getParsedData()));
		List<Field> fieldList = ConfigModel.getInstance().getFieldList();
		
		Enumeration<TableColumn> cols = table.getColumnModel().getColumns();
		while (cols.hasMoreElements()) {
			TableColumn col = (TableColumn) cols.nextElement();
			Field field = fieldList.get(col.getModelIndex());
			if (field.getValueList().isEmpty()) {
				col.setCellRenderer(DefaultTableCellRenderers.forDataType(fieldList.get(col.getModelIndex()).getDatatype()));
			} else {
				col.setCellRenderer(new FieldValueRenderer(field));
			}
		}
		
		miDetails.setEnabled(true);
		iLaunch.setEnabled(true);
		cbTable.setEnabled(true);
		
		configController.refreshAxesList();
		configController.addRecentFile(aFile.getPath());
		System.out.println("Gonna make chart");
		JFreeChart chart = Chart.chartFactory(logFile);
		System.out.println("Chart's been completed");
		chart.addProgressListener(this);
		chartPanel.setChart(chart);
		System.out.println("Work's done");
	}
	
	private void setTableVisible(boolean visible) {
		spTable.setVisible(visible);
		if (visible) {
			splitPane.setDividerLocation(.75);
		} else {
			splitPane.setDividerLocation(1);
		}
	}
	
	class CustomDragDropListener
	implements DropTargetListener {
		@Override
		public void drop(DropTargetDropEvent dtde) {
			dtde.acceptDrop(DnDConstants.ACTION_COPY);
	        Transferable t = dtde.getTransferable();
	        if (t.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
	            try {
	                Object td = t.getTransferData(DataFlavor.javaFileListFlavor);
	                if (td instanceof List) {
	                	@SuppressWarnings("rawtypes")
						Object value = ((List) td).get(0);
	                	if (value instanceof File) {
                            File file = (File) value;
                            openLogFile(file);
                        }
	                }
	            } catch (UnsupportedFlavorException | IOException ex) {
	                ex.printStackTrace();
	            }
	        }
	        
	        dtde.dropComplete(true);
		}
		
		@Override public void dragEnter(DropTargetDragEvent dtde) {}
		@Override public void dragOver(DropTargetDragEvent dtde) {}
		@Override public void dropActionChanged(DropTargetDragEvent dtde) {}
		@Override public void dragExit(DropTargetEvent dte) {}
	}
	
	static void addPopup(Component component, final JPopupMenu popup) {
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
				popup.show(e.getComponent(), e.getX(), e.getY());
			}
		});
	}

	@Override
	public void chartProgress(ChartProgressEvent e) {
		//TODO
		if (e.getType() != 2) return;
		
		if (Chart.getInstance() != null && Chart.getxFieldId() > -1) {
			XYPlot xyplot = (XYPlot)Chart.getInstance().getPlot();
			long value = (long) xyplot.getDomainCrosshairValue();
			int index = logFile.getParsedData().getRowId(Chart.getxFieldId(), LocalDateTime.ofInstant(Instant.ofEpochMilli(value), ZoneId.systemDefault()));
			if (index != -1 && table.getSelectedRow() != index) {
				Rectangle rect = table.getCellRect(index, 0, true);
				table.scrollRectToVisible(rect);					
				table.setRowSelectionInterval(index, index);
				int col = table.getSelectedColumn() == -1 ? 0 : table.getSelectedColumn();
				table.setColumnSelectionInterval(col, col);
			}
		}
	}

	private class ConfigController {
		private ConfigModel configModel;
		private ArrayList<Link> links;
		
		private final int[] KEY_LIST = {
				KeyEvent.VK_1, KeyEvent.VK_2, KeyEvent.VK_3
				, KeyEvent.VK_4, KeyEvent.VK_5, KeyEvent.VK_6
				, KeyEvent.VK_7, KeyEvent.VK_8, KeyEvent.VK_9
				, KeyEvent.VK_0
		};
		
		private ActionListener alOpenRecent = e -> {
			File file = new File(((JMenuItem) e.getSource()).getText());
			if (file.exists()) {
				openLogFile(file);
			} else {
				Object[] options = {"Удалить из списка",
	                    "Удалить из списка все некорректные ссылки",
	                    "Ничего не делать"};
				int n = JOptionPane.showOptionDialog(null,
				    "Файл [" + file.getPath() +"] не найден.",
				    "Файл не найден",
				    JOptionPane.YES_NO_CANCEL_OPTION,
				    JOptionPane.QUESTION_MESSAGE,
				    null,
				    options,
				    options[0]);
				
				if (n == 0) {
					configModel.removeRecentFile(file.getPath());
				} else 
					if (n == 1)
						configModel.removeMissingRecents();
			}
		};
		
		private ActionListener alToggleYAxis = e -> {
			JCheckBoxMenuItem mi = (JCheckBoxMenuItem) e.getSource();
			Link link = getLink(mi);
			if (link == null || link.field == null) return;
			configModel.toggleYAxis(link.field, mi.isSelected());
			link.yMenuItem.setForeground(link.getColor());
		};
		
		public ConfigController(ConfigModel configModel) {
			this.configModel = configModel;
			links = new ArrayList<>();
			configModel.addPropertyListener(e -> {
				boolean visible;
				switch (e.getPropertyName()) {
				case "recent-file":
					updateRecentMenu();
					break;
				case "launch-visible":
					visible = configModel.getLaunchVisible();
					iLaunch.setSelected(visible);
					Chart.setLaunchVisible(visible);
					break;
				case "table-visible":
					visible = configModel.getTableVisible();
					cbTable.setSelected(visible);
					setTableVisible(visible);
					break;
				}
			});
			
			configModel.addFieldListener(e -> {
				Field field = (Field) e.getSource();
				
				if (field != null) {
					if (e.getPropertyName() != null) {
						switch (e.getPropertyName()) {
						case "role":
						case "color":
							if (field.getRole() == FieldRole.DRAW) {
								Chart.drawField(field);
							} else
								Chart.setFieldVisible(field, false);
							break;
						case "datatype":
						case "parser":
						case "format":
							reset();
							break;
						}
					} else
						reset();
				}
			});
			
			iLaunch.setSelected(configModel.getLaunchVisible());
			iLaunch.addActionListener(e -> {
				JCheckBox cb = (JCheckBox) e.getSource(); 
				configModel.setLaunchVisible(cb.isSelected());
			});
			

			setTableVisible(configModel.getTableVisible());
			cbTable.setSelected(configModel.getTableVisible());
			cbTable.addActionListener(e -> {
				JCheckBox cb = (JCheckBox) e.getSource(); 
				configModel.setTableVisible(cb.isSelected());
			});
			
			updateRecentMenu();
		}
		
		public Link getLink(JCheckBoxMenuItem menuItem) {
			if (menuItem == null)
				return null;
			
			for (Link link : links) {
				if (link.yMenuItem.equals(menuItem))
					return link;
			}
			
			return null;
		}
		
		/** Refreshes Axes list at the "Add Axis" button's popup menu*/
		public void refreshAxesList() {
			mYAxes.removeAll();
			links.clear();
			
			List<Field> fields = ConfigModel.getInstance().getFieldList(); 
			for (Field field : fields) {
				if (field.getDatatype() == DataType.STRING ||
						field.getRole() == FieldRole.X_AXIS)
							continue;
				Link link = new Link(field);
				links.add(link);
				mYAxes.add(link.yMenuItem);
				link.yMenuItem.addActionListener(alToggleYAxis);
			}
		}
		
		public void addRecentFile(String path) {
			configModel.addRecentFile(path);
		}
		
		public void removeRecentFiles() {
			configModel.removeRecentFiles();
		}
		
		private void updateRecentMenu() {
			int size = mRecent.getItemCount() - 2;
			LinkedList<String> paths = configModel.getRecentFiles(); 
			
			// Equalize counts of menu items and files in set
			for (int i = size; i > paths.size(); i--)
				mRecent.remove(paths.size());
			
			for (int i = size; i < paths.size(); i++) {
				JMenuItem mi = new JMenuItem();
				if (KEY_LIST.length > i) { 
					mi.setAccelerator(KeyStroke.getKeyStroke(KEY_LIST[i], InputEvent.ALT_MASK));
				} else
					mi.setAccelerator(null);
				mi.addActionListener(alOpenRecent);
				mRecent.add(mi, i);
			}
			
			// Update the captions
			for (int i = 0; i < paths.size(); i++) {
				JMenuItem mi = mRecent.getItem(i);
				mi.setText(paths.get(i));
			}
			
			mRecent.setEnabled(paths.size() > 0);
		}
		
		private class Link {
			public final Field field;
			public final JCheckBoxMenuItem yMenuItem;
			
			public Link(Field field) {
				this.field = field;
				
				yMenuItem = new JCheckBoxMenuItem(field.getName());
				yMenuItem.setFont(new Font("Tahoma", Font.BOLD, 11));
				yMenuItem.setForeground(getColor());
				yMenuItem.setSelected(field.getRole() == FieldRole.DRAW);
			}
			
			public Color getColor() {
				if (field != null &&
						field.getRole() == FieldRole.DRAW &&
						field.getColor() != null) {
							return field.getColor();
				} else
					return Color.BLACK;
			}
		}
	}
}