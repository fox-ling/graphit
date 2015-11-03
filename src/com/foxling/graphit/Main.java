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

package com.foxling.graphit;

import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Rectangle;

import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.SpringLayout;
import javax.swing.UIManager;

import com.foxling.graphit.LogFile;
import com.foxling.graphit.LogFile.Line;
import com.foxling.graphit.LogFile.Startup;

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

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.event.ChartProgressEvent;
import org.jfree.chart.event.ChartProgressListener;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.time.Second;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYDataset;
import org.jfree.ui.RectangleInsets;

import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import javax.swing.JTable;

import java.awt.FlowLayout;

import javax.swing.JSplitPane;
import javax.swing.ListSelectionModel;
import javax.swing.JMenuBar;
import javax.swing.JMenu;

import java.awt.event.InputEvent;

import javax.swing.JTextField;
import javax.swing.JPopupMenu;

import java.awt.Component;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JCheckBox;

import java.awt.Font;
import java.awt.Color;

public class Main
extends JFrame implements ChartProgressListener {
	private static final long serialVersionUID = 1L;
	private static final String APPNAME = "Graphit - ИСУ \"Оптима\" ";
	
	private JPanel contentPane;
	private JMenu mFile = null;
	private LogFile lf = null;
	//private Chart chart = null;
	private ChartPanel chartPanel = null;
	
	private Config config;
	private JMenuItem miDetails;
	private JSplitPane splitPane;
	private JScrollPane spTable;
	private JTable table;
	private boolean tableReady = false;
	/** Protection against Chart~Table onChange cycle.</br>
	 * *Chart onClick-event goes before Chart Cursor actually moves, so couldn't make it with onClick only */
	private boolean chartMSequence = false;
	private boolean doChartTrack = true;
	private boolean doTableTrack = true;
	private JTextField tfCurrFile;
	private JPopupMenu popupMenu;
	private JCheckBoxMenuItem miWrongHashOnly;
	private SimpleDateFormat fDate = new SimpleDateFormat ("dd.MM.YYYY");
	private SimpleDateFormat fTime = new SimpleDateFormat ("HH:mm:ss");
	private JMenu mRecent;
	private JCheckBox cbDepth;
	private JCheckBox cbTension;
	private JCheckBox cbLaunch;
	private JCheckBox cbTable;
	
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		JOptionPane.showMessageDialog(null, System.getProperty("java.class.path"));
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Main frame = new Main();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public Main() {
		super(APPNAME);
		
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 850, 491);
		
		JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);
		
		mFile = new JMenu("\u0424\u0430\u0439\u043B");
		menuBar.add(mFile);
		
		final JMenuItem miOpen = new JMenuItem("\u041E\u0442\u043A\u0440\u044B\u0442\u044C");
		miOpen.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				final JFileChooser fc = new JFileChooser();
				if (fc.showOpenDialog(miOpen)==JFileChooser.APPROVE_OPTION){
					openLogFile(fc.getSelectedFile());
				}
			}
		});
		miOpen.setIcon(new ImageIcon(Main.class.getResource("/com/foxling/graphit/ic_action_collection.png")));
		miOpen.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_MASK));
		mFile.add(miOpen);
		
		mRecent = new JMenu("\u041F\u043E\u0441\u043B\u0435\u0434\u043D\u0438\u0435 \u0444\u0430\u0439\u043B\u044B");
		mRecent.setEnabled(false);
		mFile.add(mRecent);
		
		mFile.addSeparator();
		
		miDetails = new JMenuItem("\u0414\u0435\u0442\u0430\u043B\u0438");
		miDetails.setEnabled(false);
		miDetails.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Details fDetails = new Details(lf);
				fDetails.setVisible(true);
			}
		});
		miDetails.setIcon(new ImageIcon(Main.class.getResource("/com/foxling/graphit/ic_action_storage.png")));
		miDetails.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D, InputEvent.CTRL_MASK));
		mFile.add(miDetails);
		
		
		tfCurrFile = new JTextField();
		tfCurrFile.setEditable(false);
		menuBar.add(tfCurrFile);
		tfCurrFile.setColumns(10);
		
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(1, 1, 1, 1));
		setContentPane(contentPane);
		SpringLayout sl_contentPane = new SpringLayout();
		contentPane.setLayout(sl_contentPane);
		
	    chartPanel = new ChartPanel(null);//chart.chart
	    chartPanel.setBackground(Color.WHITE);
	    chartPanel.addMouseListener(new MouseAdapter() {
	    	@Override
	    	public void mouseClicked(MouseEvent e) {
	    		chartMSequence = true;
	    	}
	    });
	    //chartPanel.getChart().addChangeListener(this);
		sl_contentPane.putConstraint(SpringLayout.EAST, chartPanel, 0, SpringLayout.EAST, contentPane);
		FlowLayout flowLayout = (FlowLayout) chartPanel.getLayout();
		flowLayout.setAlignment(FlowLayout.LEADING);
		
		table = new JTable(){
			private static final long serialVersionUID = 342278938918028779L;

			@Override
			public String getToolTipText(MouseEvent e) {
		        String tip = null;
		        java.awt.Point p = e.getPoint();
		        int rowIndex = rowAtPoint(p);
		        int colIndex = columnAtPoint(p);
		        int realColumnIndex = convertColumnIndexToModel(colIndex);

		        if (realColumnIndex == 6) {
		        	tip = lf.getKeyTooltip((String) getValueAt(rowIndex, colIndex));
		        } else
		        	if (realColumnIndex == 10) {
			        	tip = lf.getErrorTooltip((String) getValueAt(rowIndex, colIndex));
			        }
		        
		        return tip;
			}
		};
		table.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
		table.setCellSelectionEnabled(true);
		table.setFillsViewportHeight(true);
		
		ListSelectionModel rowSM = table.getSelectionModel();
		rowSM.addListSelectionListener(new ListSelectionListener() {
			@Override
			/** Listener for row-change events (Chart trace) */
			public void valueChanged(ListSelectionEvent e) {
				if (!doTableTrack || !table.isFocusOwner() || e.getValueIsAdjusting()) return;
				
				ListSelectionModel rowSM = (ListSelectionModel) e.getSource();
				int selectedIndex = rowSM.getMinSelectionIndex();
				
				if (chartPanel != null) {
					DefaultTableModel model = (DefaultTableModel) table.getModel();
					try {
						Date time = fTime.parse((String) (model.getValueAt(selectedIndex, 1)));
					
						JFreeChart jfreechart = chartPanel.getChart();
						if (jfreechart != null)	{
							XYPlot xyplot = (XYPlot)jfreechart.getPlot();
							xyplot.setDomainCrosshairValue(time.getTime());
						}
					} catch (ParseException e1) {
						e1.printStackTrace();
					}
				}
			}
		});
		
		spTable = new JScrollPane(table);
		spTable.setVisible(false);
		
		popupMenu = new JPopupMenu();
		addPopup(table, popupMenu);
		
		miWrongHashOnly = new JCheckBoxMenuItem("\u041E\u0442\u043E\u0431\u0440\u0430\u0436\u0430\u0442\u044C \u0442\u043E\u043B\u044C\u043A\u043E \u0441 \u043D\u0435\u043F\u0440\u0430\u0432\u0438\u043B\u044C\u043D\u044B\u043C \u0445\u044D\u0448\u0435\u043C");
		miWrongHashOnly.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				doChartTrack = !miWrongHashOnly.isSelected();
				fillTable(miWrongHashOnly.isSelected());
			}
		});
		popupMenu.add(miWrongHashOnly);
		
		
		JPanel toppanel = new JPanel();
		SpringLayout sl_tp = new SpringLayout();
		toppanel.setLayout(sl_tp);
		toppanel.setPreferredSize(new Dimension(560,367));
		
		JPanel pTools = new JPanel();
		pTools.setBackground(Color.WHITE);
		sl_tp.putConstraint(SpringLayout.WEST, pTools, 0, SpringLayout.WEST, toppanel);
		sl_tp.putConstraint(SpringLayout.EAST, pTools, 0, SpringLayout.EAST, toppanel);
		sl_tp.putConstraint(SpringLayout.NORTH, pTools, -22, SpringLayout.SOUTH, toppanel);
		sl_tp.putConstraint(SpringLayout.SOUTH, pTools, 0, SpringLayout.SOUTH, toppanel);
		toppanel.add(pTools);
		
		sl_tp.putConstraint(SpringLayout.WEST, chartPanel, 0, SpringLayout.WEST, toppanel);
		sl_tp.putConstraint(SpringLayout.EAST, chartPanel, 0, SpringLayout.EAST, toppanel);
		sl_tp.putConstraint(SpringLayout.NORTH, chartPanel, 0, SpringLayout.NORTH, toppanel);
		sl_tp.putConstraint(SpringLayout.SOUTH, chartPanel, 0, SpringLayout.NORTH, pTools);		
		SpringLayout sl_pTools = new SpringLayout();
		pTools.setLayout(sl_pTools);
		
		cbDepth = new JCheckBox("\u0413\u043B\u0443\u0431\u0438\u043D\u0430");
		cbDepth.setEnabled(false);
		cbDepth.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				Chart.setCollectionVisible(chartPanel.getChart(), 1, cbDepth.isSelected());
			}
		});
		cbDepth.setBackground(Color.WHITE);
		cbDepth.setForeground(Color.RED);
		cbDepth.setFont(new Font("Tahoma", Font.BOLD, 11));
		sl_pTools.putConstraint(SpringLayout.NORTH, cbDepth, 0, SpringLayout.NORTH, pTools);
		sl_pTools.putConstraint(SpringLayout.WEST, cbDepth, 0, SpringLayout.WEST, pTools);
		pTools.add(cbDepth);
		
		cbTension = new JCheckBox("\u041D\u0430\u0442\u044F\u0436\u0435\u043D\u0438\u0435");
		cbTension.setEnabled(false);
		cbTension.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Chart.setCollectionVisible(chartPanel.getChart(), 2, cbTension.isSelected());
			}
		});
		cbTension.setBackground(Color.WHITE);
		cbTension.setForeground(Color.GREEN);
		cbTension.setFont(new Font("Tahoma", Font.BOLD, 11));
		sl_pTools.putConstraint(SpringLayout.WEST, cbTension, 10, SpringLayout.EAST, cbDepth);
		sl_pTools.putConstraint(SpringLayout.SOUTH, cbTension, 0, SpringLayout.SOUTH, cbDepth);
		pTools.add(cbTension);
		
		cbLaunch = new JCheckBox("\u0417\u0430\u043F\u0443\u0441\u043A");
		cbLaunch.setEnabled(false);
		cbLaunch.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Chart.setCollectionVisible(chartPanel.getChart(), 0, cbLaunch.isSelected());
			}
		});
		cbLaunch.setBackground(Color.WHITE);
		cbLaunch.setFont(new Font("Tahoma", Font.BOLD, 11));
		sl_pTools.putConstraint(SpringLayout.WEST, cbLaunch, 10, SpringLayout.EAST, cbTension);
		sl_pTools.putConstraint(SpringLayout.SOUTH, cbLaunch, 0, SpringLayout.SOUTH, cbDepth);
		pTools.add(cbLaunch);
		
		cbTable = new JCheckBox("\u0422\u0430\u0431\u043B\u0438\u0446\u0430");
		cbTable.setEnabled(false);
		cbTable.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				setTableVisible(cbTable.isSelected());
			}
		});
		cbTable.setBackground(Color.WHITE);
		sl_pTools.putConstraint(SpringLayout.EAST, cbTable, 0, SpringLayout.EAST, pTools);
		pTools.add(cbTable);
		
		toppanel.add(chartPanel);
		
		
		splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, toppanel, spTable);
		sl_contentPane.putConstraint(SpringLayout.NORTH, splitPane, 0, SpringLayout.NORTH, contentPane);
	    sl_contentPane.putConstraint(SpringLayout.WEST, splitPane, 0, SpringLayout.WEST, contentPane);
	    sl_contentPane.putConstraint(SpringLayout.SOUTH, splitPane, 0, SpringLayout.SOUTH, contentPane);
	    sl_contentPane.putConstraint(SpringLayout.EAST, splitPane, 0, SpringLayout.EAST, contentPane);
	    splitPane.setResizeWeight(1);
	    contentPane.add(splitPane);
		
	    config = new Config();
	    
	    Runtime.getRuntime().addShutdownHook(new Thread(){
	        @Override
	        public void run()
	        {
	        	config.save();
	        }});

		// Create the drag and drop listener
	    CustomDragDropListener customDragDropListener = new CustomDragDropListener();
	    new DropTarget(chartPanel, customDragDropListener);
	}
	
	private void openLogFile(File aFile){
		try {
			lf = new LogFile(aFile.getPath());
			lf.readFile();
			tfCurrFile.setText(lf.fileName);
			
			
			String title = APPNAME;
			
			if (lf.startup.size() > 0 && lf.startup.get(0).date != null)
				title += " Дата: " + fDate.format(lf.startup.get(0).date)+", ";
			
			if (lf.serialNo != null && !lf.serialNo.isEmpty())
				title += "Сер.№ " + lf.serialNo + ", ";
			
			if (lf.frimware != null && !lf.frimware.isEmpty())
				title += "ПО " + lf.frimware;
			
			if (title.endsWith(", ")) title = title.substring(0, title.length()-3);
			
			this.setTitle(title);
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null, "Не удалось прочитать файл "+aFile.getName(), "Ошибка", JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
			return;
		}
		
		
		boolean[] tsSet = {cbLaunch.isSelected(), cbDepth.isSelected(), cbTension.isSelected()};
		chartPanel.setChart(Chart.createTSChart(lf, tsSet));
		chartPanel.getChart().addProgressListener(this);
		
		fillTable();
		if(cbTable.isSelected() && !spTable.isVisible())
			setTableVisible(cbTable.isSelected());
		
		miDetails.setEnabled(true);
		cbDepth.setEnabled(true);
		cbTension.setEnabled(true);
		cbLaunch.setEnabled(true);
		cbTable.setEnabled(true);
		
		config.recent.add(aFile.getPath());
	}
	
	private void setTableVisible(boolean visible) {
		spTable.setVisible(visible);
		if (visible) {
			splitPane.setDividerLocation(.75);
		} else {
			splitPane.setDividerLocation(1);
		}
	}
	
	private void fillTable(){
		fillTable(false);
	}
	
	private void fillTable(boolean wrongLinesOnly){
		doTableTrack = false;
		DefaultTableModel model;
		if (!tableReady) {
			final String[] colNames = { "#", "Время","Глубина","Натяжение","ДВП","Uтек"
					,"Клав","F","Режим","Ход","Ошибка","Хеш"};
			
			model = new DefaultTableModel(new Object[][] {}, colNames) {
				private static final long serialVersionUID = 1L;
				@SuppressWarnings("rawtypes")
				Class[] columnTypes = new Class[] {
						Integer.class, String.class, Integer.class, Float.class, String.class,
						Short.class, String.class, Byte.class, Byte.class, String.class, String.class, String.class
				};
				
				public Class getColumnClass(int columnIndex) {
					return columnTypes[columnIndex];
				}
				
				@Override
			    public boolean isCellEditable(int row, int column) {
			       return false;
			    }
			};
			tableReady = true;
		} else {
			model = (DefaultTableModel) table.getModel();
			model.setRowCount(0);
		}
		
		for (int i = 0; i < lf.startup.size(); i++) {
			Startup currStartup = lf.startup.get(i);
			for (int j = 0; j < currStartup.lines.size(); j++) {
				Line currLine = currStartup.lines.get(j);
				
				if (!wrongLinesOnly || !currLine.authentic ) {
					String topPos = "";
					switch (currLine.topPos) {
						case -1: topPos = "Обрыв"; break;
						case 0: topPos = "не ВП"; break;
						case 1: topPos = "ВП"; break;
						default: topPos = Byte.toString(currLine.topPos); break;
					}
					
					String mode = "";
					switch (currLine.mode) {
						case 0: mode = "Ручной"; break;
						case 1: mode = "Полуавтомат"; break;
						case 2: mode = "Автомат."; break;
						default: mode = Byte.toString(currLine.mode); break;
					}
					
					String stroke = "";
					switch (currLine.stroke) {
						case 'S': stroke = "Останов"; break;
						case 'U': stroke = "Вверх"; break;
						case 'D': stroke = "Вниз"; break;
						default: stroke = ""+currLine.stroke; break;
					}
					
					model.addRow(new Object[]{i,fTime.format(currLine.time)
									,currLine.depth,currLine.tension
									,topPos,currLine.voltage
									,currLine.knob_state,currLine.speed
									,mode,stroke
									,currLine.error,currLine.hash});
				}
			}
		}
			
		table.setModel(model);
		doTableTrack = true;
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
				popup.show(e.getComponent(), e.getX(), e.getY());
			}
		});
	}

	@Override
	public void chartProgress(ChartProgressEvent arg0) {
		if (arg0.getType() != 2 || !chartMSequence) return;
		chartMSequence = false;
		
		if (doChartTrack && chartPanel != null) {
			JFreeChart jfreechart = chartPanel.getChart();
			if (jfreechart != null)	{
				XYPlot xyplot = (XYPlot)jfreechart.getPlot();
				double d = xyplot.getDomainCrosshairValue();

				Date time = new Date((long) d);
				int i = lf.getId(time);
				if (i>-1) {
					Rectangle rect = table.getCellRect(i, 0, true);
					table.scrollRectToVisible(rect);					
					table.setRowSelectionInterval(i,i);
					table.setColumnSelectionInterval(1, 1);
				}
			}
		}
	}
	
	private static class Chart {
		private static final String sTension = "Натяжение";
		private static final String sDepth = "Глубина";
		
		/** boolean[] <b>dsVisible</b> = {..., ..., ...}<br>
		 * There are 3 datasets in chart, variable gotta contain visibility values for each of them */
		public static JFreeChart createTSChart(LogFile data, boolean[] dsVisible) {
			String chartTitle = "";
		    String xAxisLabel = "";
		    
		    Startup currStartup = null;
			Line currLine = null;

			TimeSeriesCollection dsLaunch = new TimeSeriesCollection();
			TimeSeriesCollection dsDepth = new TimeSeriesCollection();
			TimeSeriesCollection dsTension = new TimeSeriesCollection();
			
			boolean fixDone = false;
			Date xTime = null;
			TimeSeries tsLaunch = new TimeSeries("Launch");
			TimeSeries tsDepth = null;
			TimeSeries tsTension = null;
			TimeSeries tsFix = new TimeSeries("Launch zoom out fix");
			for (int i = 0; i < data.startup.size(); i++) {
				currStartup = data.startup.get(i);
				if (currStartup.time != null) {
					if (!fixDone) {
						tsFix.add(new Second(currStartup.time), 1);
						dsLaunch.addSeries(tsFix);
						fixDone = true;
					}
					tsLaunch.addOrUpdate(new Second(currStartup.time), 0);
				}
				
				tsDepth = new TimeSeries(sDepth+Integer.toString(i));
				tsTension = new TimeSeries(sTension+Integer.toString(i));
				int size = currStartup.lines.size();
				for (int j = 0; j < size; j++) {
					currLine = currStartup.lines.get(j);

					if (xTime != null && xTime.getTime() < currLine.time.getTime() - 1000) {
						Date dVoid = new Date(xTime.getTime() + 1000);
						tsDepth.add(new Second(dVoid), null);
						tsTension.add(new Second(dVoid), null);
					}
					
					tsDepth.addOrUpdate(new Second(currLine.time), currLine.depth);
					tsTension.addOrUpdate(new Second(currLine.time), currLine.tension);
					xTime = currLine.time;
				}
				
				if (size != 0) {
					dsDepth.addSeries(tsDepth);
					dsTension.addSeries(tsTension);
				}
			}
			dsLaunch.addSeries(tsLaunch);
			
			JFreeChart chart = ChartFactory.createTimeSeriesChart(chartTitle, xAxisLabel, sDepth, dsLaunch, false, false, false);

		    XYPlot plot = chart.getXYPlot();
		    plot.setBackgroundPaint(Color.white);
	        plot.setDomainGridlinePaint(Color.lightGray);
	        plot.setRangeGridlinePaint(Color.lightGray);
	        plot.setAxisOffset(new RectangleInsets(5.0, 5.0, 5.0, 5.0));
	        plot.setDomainCrosshairVisible(true);
	        plot.setDomainCrosshairLockedOnData(true);
	        
	        ValueAxis axis = plot.getRangeAxis();
	        axis.setVisible(false);
	        
	        /** TimeSeries count (Launches count) */
	        int tsCount = dsLaunch.getSeriesCount();
	        
	        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
	        
	        if (tsCount != 0)
	        	renderer.setSeriesShapesVisible(0, false);
	        
	        for (int i = 1; i < tsCount; i++) {
	        	renderer.setSeriesShapesFilled(i, true);
	        	renderer.setSeriesShapesVisible(i, dsVisible[0]);
	        	renderer.setSeriesLinesVisible(i, false);
	        	renderer.setSeriesPaint(i, Color.black);
	        	//renderer.setSeriesStroke(i, new BasicStroke(2.0F));
			}
	        plot.setRenderer(0, renderer);
	        
	        /** 2nd AXIS – Depth */
	        NumberAxis naDepth = new NumberAxis(sDepth);
	        naDepth.setAutoRangeIncludesZero(false);
	        naDepth.setLabelPaint(Color.red);
	        naDepth.setInverted(true);
	        naDepth.setAutoRangeStickyZero(false);
	        naDepth.setVisible(dsVisible[1]);
	        plot.setRangeAxis(1, naDepth);
	        plot.setRangeAxisLocation(1, AxisLocation.BOTTOM_OR_LEFT);
	        
	        XYDataset xydsDepth = dsDepth;
	        plot.setDataset(1, xydsDepth);
	        plot.mapDatasetToRangeAxis(1, 1);
	        
	        renderer = new XYLineAndShapeRenderer();
	        for (int i = 0; i < dsDepth.getSeriesCount(); i++) {
	        	renderer.setSeriesLinesVisible(i, dsVisible[1]);
	        	renderer.setSeriesPaint(i, Color.red);
	        	renderer.setSeriesShapesVisible(i, false);
	        	renderer.setSeriesShapesFilled(i, false);
			}
	        plot.setRenderer(1, renderer);
	        
	        /** 3rd AXIS – Tension */
	        NumberAxis naTension = new NumberAxis(sTension);
	        naTension.setAutoRangeIncludesZero(false);
	        naTension.setLabelPaint(Color.green);
	        naTension.setTickLabelPaint(Color.black);
	        naTension.setVisible(dsVisible[2]);
	        plot.setRangeAxis(2, naTension);
	        plot.setRangeAxisLocation(2, AxisLocation.BOTTOM_OR_LEFT);
	        
	        XYDataset xydsTension = dsTension;
	        plot.setDataset(2, xydsTension);
	        plot.mapDatasetToRangeAxis(2, 2);
	        
	        renderer = new XYLineAndShapeRenderer();
	        for (int i = 0; i < dsTension.getSeriesCount(); i++) {
	        	renderer.setSeriesLinesVisible(i, dsVisible[2]);
	        	renderer.setSeriesPaint(i, Color.GREEN);
	        	renderer.setSeriesShapesVisible(i, false);
	        	renderer.setSeriesShapesFilled(i, false);
			}
	        plot.setRenderer(2, renderer);

	        return chart;
		}
		
		public static void setCollectionVisible(JFreeChart chart, int collectionID, boolean visible) {
			XYPlot plot = chart.getXYPlot();
			
			if (collectionID != 0) {
				NumberAxis axis = (NumberAxis) plot.getRangeAxis(collectionID);
				axis.setVisible(visible);
			}
			
			XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) plot.getRenderer(collectionID);
			
			for (int i = 0; i < plot.getDataset(collectionID).getSeriesCount(); i++) {
				if (collectionID != 0)
					renderer.setSeriesLinesVisible(i, visible);
				
				if (collectionID == 0 && i != 0) {
					renderer.setSeriesShapesVisible(i, visible);
				}
			}
		}
	}

	
	private class Config {
		private File file = null;
		private boolean ready = false;
		
		AppState state = null; 
		private class AppState {
			/** CheckBox Set */
			private Object[][] chxSet = {{"cbDepth", cbDepth},
											{"cbTension", cbTension},
											{"cbLaunch", cbLaunch},
											{"cbTable", cbTable}}; 
			
			public void add(String name, boolean value) {
				for (int i = 0; i < chxSet.length; i++) {
					if (name.equals(chxSet[i][0]))
						((JCheckBox) chxSet[i][1]).setSelected(value);
				}
			}
				
			public void save(Properties props) {
				for (int i = 0; i < chxSet.length; i++) {
					if (((JCheckBox) chxSet[i][1]).isSelected()) 
						props.setProperty("state."+chxSet[i][0], "1");
				}
			}
			
			public void setDefaults() {
				for (int i = 0; i < chxSet.length; i++) {
					((JCheckBox) chxSet[i][1]).setSelected(true);
				}
			}
		}
		
		Recent recent = null;
		/** Recent files CLASS */
		private class Recent {
			private ArrayList<Item> items = new ArrayList<Item>();
			
			/** Adds entry in Recent List */
			public void add(String filename) {
				this.add(-1, filename, true);
			}
			
			/** Adds entry in Recent List,<br>
			 * <b>cid</b> - Config file ID <i>(for sorting, right after loading config-file)</i><br>
			 * <b>updateMenu</b> - do you need to update recent files menu? */
			public void add(int cid, String filename, boolean updateMenu) {
				int i = this.getId(filename);
				if (i != -1) {
					if (!updateMenu) { // === Loading from config-file ===
						Recent.Item currItem = this.items.get(i);
						if (currItem.cid > cid) currItem.cid = cid;
					}
					popupItem(i, updateMenu);
				} else {
					this.items.add(0, new Item(cid, filename));
					if (updateMenu) updateMenu();
				}
			}
			
			/** Remove item by ID */
			public boolean remove(int id) {
				if (this.items.size() >= id) {
					this.items.remove(id);
					updateMenu();
					return true;
				}
				return false;
			}
			
			/** Remove item by filename */
			public boolean remove(String filename) {
				int id = this.getId(filename);
				if (id != -1) {
					return remove(id);
				}
				return false;
			}
			
			/** Removes all non-existent links from "Recent List"*/
			public boolean removeNonExistent() {
				boolean result = false;
				for (int i = this.items.size()-1; i > -1; i--) {
					if (!new File(this.items.get(i).filename).exists()) {
						this.items.remove(i);
						result = true;
					}
				}

				if (result) updateMenu();
				
				return result;
			}
			
			/** Removes all items from Recent List, and updates menu*/
			public void removeAll() {
				this.items.clear();
				updateMenu();
			}
			
			/** Searches thru list for <b>filename</b> and returns one's ID <i>(ArrayList's, not Item.id)</i>  */
			public int getId(String filename) {
				for (int i = 0; i < this.items.size(); i++) {
					if (this.items.get(i).filename.equals(filename)) {
						return i;
					}
				}
				return -1;
			}
			
			/*
			public boolean popupItem(String filename) {
				int id = this.getId(filename);
				if (id != -1) {
					return popupItem(id, true);
				}
				return false;
			}*/
			
			/** Pulls item at top of the list and updates menu */
			public boolean popupItem(int id, boolean updateMenu) {
				if (id < 1 || this.items.size() <= id) return false;
				
				String filename = this.items.get(id).filename;
				this.items.remove(id);
				this.items.add(0, new Recent.Item(-1, filename));
				
				if (updateMenu) this.updateMenu();
				return true;
			}
			
			public void sort(){
				Collections.sort(this.items);
			}
			
			public class Item implements Comparable<Item>  {
				/** Config-file entry ID.<br>I use it only for sorting right after parsing config-file */
				int cid;
				public String filename;
				
				public Item(int id, String filename){
					this.cid = id;
					this.filename = filename;
				}
	
				@Override
				public int compareTo(Item arg0) {
					return this.cid - arg0.cid;
				}
			}
			
			/** Updates "Recent Files" menu */
			public void updateMenu() {
				mRecent.removeAll();
				if (this.items.size()==0) {
					mRecent.setEnabled(false);
					return;
				}
				
				int k = this.items.size();
				k = k > 10 ? 10 : k;
				final int[] keyList = {KeyEvent.VK_1, KeyEvent.VK_2, KeyEvent.VK_3
										, KeyEvent.VK_4, KeyEvent.VK_5, KeyEvent.VK_6
										, KeyEvent.VK_7, KeyEvent.VK_8, KeyEvent.VK_9
										, KeyEvent.VK_0};
				
				ActionListener alOpenRecent = new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						File file = new File(((JMenuItem) e.getSource()).getText());
						if (file.exists()) {
							openLogFile(file);
						} else {
							Object[] options = {"Удалить из списка",
				                    "Удалить из списка все некорректные ссылки",
				                    "Ничего не делать"};
							int n = JOptionPane.showOptionDialog(null,
							    "Файл ["+file.getPath()+"] не найден.",
							    "Файл не найден",
							    JOptionPane.YES_NO_CANCEL_OPTION,
							    JOptionPane.QUESTION_MESSAGE,
							    null,
							    options,
							    options[0]);
							
							if (n == 0) {
								recent.remove(file.getPath());
							} else 
								if (n == 1) {
									recent.removeNonExistent();
								}
						}
					}
				};
				
				for (int i = 0; i < k; i++) {
					Item currItem = this.items.get(i);
					
					JMenuItem mi = new JMenuItem(currItem.filename);
					mi.setAccelerator(KeyStroke.getKeyStroke(keyList[i], InputEvent.ALT_MASK));
					mi.addActionListener(alOpenRecent);
					mRecent.add(mi);
				}
				
				mRecent.addSeparator();
				
				JMenuItem mi = new JMenuItem("Очистить список");
				mi.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						recent.removeAll();
					}
				});
				mRecent.add(mi);
				
				mRecent.setEnabled(true);
			}
			
			public void save(Properties props) {
				for (int i = 0; i < this.items.size(); i++) {
					props.setProperty("recent."+Integer.toString(i), this.items.get(i).filename);
				}
			}
		}
		
		public Config() {
			String fname = System.getenv("APPDATA")+"\\"+this.getClass().getPackage().getName()+"\\config.txt";
			this.file = new File(fname);
			
			if (this.file.exists()) {
				this.ready = true;
			} else {
				File folder = this.file.getParentFile();
				try {
					if (!folder.exists()) {
						this.ready = folder.mkdirs() && file.createNewFile();
					} else {
						this.ready = file.exists() || file.createNewFile();
					}
				} catch (Exception e) {
					e.printStackTrace();
					this.ready = false;
				}
				
				this.setDefaults();
			}
			
			if (this.ready) {
				this.state = new AppState();
				this.recent = new Recent();
				load();
			}
		}
		
		/** Load preferences from file */
		private boolean load(){
			if (!this.file.exists())
				return false;
			
			Properties prop = new Properties();
			try {
				prop.load(new FileInputStream(this.file.getPath()));
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
			
			int id = -1;
			for(String key : prop.stringPropertyNames()) {
				String value = prop.getProperty(key);
				if (key.startsWith("recent.")) {
					try {
						id = Integer.parseInt(key.substring(7));
					} catch (Exception e) {
						id = Integer.MAX_VALUE;
					}
					this.recent.add(id, value, false);
				} else
					if (key.startsWith("state.") && value.equals("1")) {
							this.state.add(key.substring(6), true);
					} 
			}
			
			this.recent.sort();
			this.recent.updateMenu();
			return true;
		}
		
		public void save() {
			if (!this.ready) return;
			
			Properties props = new Properties();
			this.recent.save(props);
			this.state.save(props);
			try {
				OutputStream out = new FileOutputStream(this.file);
		        props.store(out, " Hello! ");
		    }
		    catch (Exception e ) {
		        e.printStackTrace();
		    }
		}
		
		/*
		public void traceRecent() {
			for (int i = 0; i < this.recent.items.size(); i++) {
				Recent.Item currRecent = this.recent.items.get(i);
				System.out.printf("%d = %s%n", currRecent.cid, currRecent.filename);
			}
		}
		*/
		
		public void setDefaults() {
			state.setDefaults();
		}
	}
}