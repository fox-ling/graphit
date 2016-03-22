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

import javax.swing.JFrame;
import javax.swing.JPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYDotRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYDataset;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import net.miginfocom.swing.MigLayout;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.SpinnerNumberModel;

public class ChartSettings extends JFrame {
	private static final long serialVersionUID = -6538143665281890150L;
	private JPanel contentPane;
	private JPanel controls;
	private JCheckBox iLines;
	private JRadioButton iCircle;
	private JRadioButton iSquare;
	private JCheckBox iShapes;
	private JCheckBox iShapesFilled;
	private ActionListener setChart = new ActionListener() {
		
		@Override
		public void actionPerformed(ActionEvent e) {
			JFreeChart chart = Chart.getInstance();
			if (chart == null)
				return;
			System.out.println(iDotSize.getValue());
			
			XYPlot plot = chart.getXYPlot();
			System.out.printf("Renerers.Count = %d;%n", plot.getRendererCount());
			System.out.printf("Datasets.Count = %d;%n", plot.getDatasetCount());
			for (int d = 1; d < plot.getDatasetCount(); d++) {
				XYDataset dataset = plot.getDataset(d);
				System.out.printf("dataset[%d].seriesCount = %d%n", d, dataset.getSeriesCount());
				for (int s = 0; s < dataset.getSeriesCount(); s++) {
					/*XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) plot.getRenderer(d);
					renderer.setSeriesLinesVisible(s, iLines.isSelected());
		        	//renderer.setSeriesPaint(s, color);
		        	renderer.setSeriesShapesVisible(s, iShapes.isSelected());
		        	renderer.setSeriesShapesFilled(s, iShapesFilled.isSelected());*/
					XYDotRenderer renderer = (XYDotRenderer) plot.getRenderer(d);
					Integer size = (Integer) iDotSize.getValue();
					renderer.setDotHeight(size);
					renderer.setDotWidth(size);
				}
			}
		}
	};
	private JSpinner iDotSize;

	public ChartSettings() {
		super("Chart Settings");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 445, 245);
		contentPane = new JPanel();
		contentPane.setLayout(new BorderLayout());
		setContentPane(contentPane);
		
		controls = new JPanel();
		contentPane.add(controls, BorderLayout.CENTER);
		controls.setLayout(new MigLayout("", "[grow][grow]", "[][][][][]"));
		
		iDotSize = new JSpinner();
		iDotSize.setModel(new SpinnerNumberModel(1, 1, 20, 1));
		iDotSize.addChangeListener(e -> setChart.actionPerformed(null));
		controls.add(iDotSize, "cell 0 0");
		
		iShapes = new JCheckBox("Shapes");
		iShapes.addActionListener(setChart);
		controls.add(iShapes, "cell 0 1");
		
		iShapesFilled = new JCheckBox("Shapes filled");
		iShapesFilled.addActionListener(setChart);
		controls.add(iShapesFilled, "cell 0 2");
		
		iCircle = new JRadioButton("Circle");
		iCircle.addActionListener(setChart);
		controls.add(iCircle, "cell 0 3");
		
		iSquare = new JRadioButton("Square");
		iSquare.addActionListener(setChart);
		controls.add(iSquare, "cell 0 4");
		
		JButton btnRefresh = new JButton("Refresh");
		btnRefresh.addActionListener(e -> refresh());
		contentPane.add(btnRefresh, BorderLayout.NORTH);
		
		ButtonGroup groupConfigLocation = new ButtonGroup();
	    groupConfigLocation.add(iCircle);
	    groupConfigLocation.add(iSquare);
	    
	    iLines = new JCheckBox("Lines");
		controls.add(iLines, "cell 1 1");
	}
	
	public void refresh() {
		JFreeChart chart = Chart.getInstance();
		controls.setEnabled(chart != null);
	}

}
