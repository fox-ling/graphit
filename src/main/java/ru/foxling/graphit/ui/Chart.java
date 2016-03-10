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

import java.awt.Color;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.time.Second;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYDataset;
import org.jfree.ui.RectangleInsets;
import ru.foxling.graphit.Core;
import ru.foxling.graphit.config.ConfigModel;
import ru.foxling.graphit.config.DataType;
import ru.foxling.graphit.config.Field;
import ru.foxling.graphit.config.FieldRole;
import ru.foxling.graphit.config.UniqueFieldException;
import ru.foxling.graphit.logfile.LogFile;
import ru.foxling.graphit.logfile.Record;
import ru.foxling.graphit.logfile.Startup;

public class Chart {
	private static final Logger LOG = Logger.getLogger(Chart.class.getName());
	private static final LocalDate EPOCH_DATE = LocalDate.ofEpochDay(0);
	private static JFreeChart chart;
	private static XYPlot plot;
	private static LogFile logFile;
	private static Field xField;
	private static List<Field> yFields = new LinkedList<>();
	private static int xFieldId;
	
	public static JFreeChart chartFactory(LogFile logFile) {
		Chart.yFields.clear();
		List<Field> yFields = new LinkedList<>();
		List<Field> fieldList = Core.getConfigModel().getFieldList(); 
		for (int i = 0; i < fieldList.size(); i++) {
			Field f = fieldList.get(i);
			
			if (f.getRole() == FieldRole.X_AXIS) {
				xFieldId = i;
				xField = f;
			} else if (f.getRole() == FieldRole.DRAW)
				yFields.add(f);
		}
		
		try {
			drawable(logFile);
		} catch (Exception e) {
			LOG.log(Level.SEVERE, "Построение графика невозможно", e);
		}
		Chart.logFile = logFile;
		
		TimeSeriesCollection dsLaunch = new TimeSeriesCollection();
		TimeSeries tsLaunch = new TimeSeries("Launch");
		for (Startup startup : logFile.getStartups()) {
			LocalDateTime date;
			if (xField.getDatatype() == DataType.TIME) {
				date = LocalDateTime.of(EPOCH_DATE, startup.getTime());
			} else if (xField.getDatatype() == DataType.DATETIME) {
				date = startup.getDatetime();
			} else if (xField.getDatatype() == DataType.DATE) {
				date = LocalDateTime.of(startup.getDate(), LocalTime.MIN);
			} else
				throw new IllegalStateException(String.format("Неподдерщиваемый тип данных для оси X (%s). Выберите DATE/TIME/DATETIME", xField.getDatatype().getValue()));
			
			tsLaunch.addOrUpdate(new Second(date.getSecond(), date.getMinute(), date.getHour(), date.getDayOfMonth(), date.getMonthValue(), date.getYear()), 0);
		}
		dsLaunch.addSeries(tsLaunch);
		chart = ChartFactory.createTimeSeriesChart(null, null, null, dsLaunch, false, false, false);
		plot = chart.getXYPlot();
	    plot.setBackgroundPaint(Color.white);
        plot.setDomainGridlinePaint(Color.lightGray);
        plot.setRangeGridlinePaint(Color.lightGray);
        plot.setAxisOffset(new RectangleInsets(5.0, 5.0, 5.0, 5.0));
        plot.setDomainCrosshairVisible(true);
        plot.setDomainCrosshairLockedOnData(true);
        
        ValueAxis axis = plot.getRangeAxis();
        axis.setVisible(false);
        
        // TimeSeries count (Launches count)
        int tsCount = dsLaunch.getSeriesCount();
        
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
        if (tsCount != 0)
        	renderer.setSeriesShapesVisible(0, false);
        renderer.setSeriesShapesFilled(0, true);
    	renderer.setSeriesShapesVisible(0, true);
    	renderer.setSeriesLinesVisible(0, false);
    	renderer.setSeriesPaint(0, Color.black);
    	
        plot.setRenderer(0, renderer);
        
        if (xField != null)
			for (Field yField : yFields)
				plotFactory(yField);
		
        return chart;
	}
	
	public static void drawField(Field field) {
		if (field == null)
			return;
		
		if (yFields.indexOf(field) == -1) {
			plotFactory(field);
		} else
			setFieldVisible(field, true);
	}
	
	public static void setFieldVisible(Field field, boolean visible) {
		if (field == null)
			return;
		int id = yFields.indexOf(field) + 1;
		if (id == 0)
			return;
		
		Color color = field.getColor() != null ? field.getColor() : Color.PINK;
		
		NumberAxis axis = (NumberAxis) plot.getRangeAxis(id);
		axis.setLabelPaint(color);
		axis.setVisible(visible);
		
		XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) plot.getRenderer(id);
		
		
		for (int i = 0; i < plot.getDataset(id).getSeriesCount(); i++) {
			if (id != 0) {
				renderer.setSeriesLinesVisible(i, visible);
				renderer.setSeriesPaint(i, color);
			}
			
			if (id == 0 && i != 0) {
				renderer.setSeriesShapesVisible(i, visible);
			}
		}
	}
	
	private static boolean plotFactory(Field yField) {
		if (xField == null) {
			LOG.log(Level.SEVERE, "Ось X не определена");
			return false;
		}
		if (yField == null) {
			LOG.log(Level.SEVERE, "Ось Y не определена");
			return false;
		}
		
		int xFieldId = Core.getConfigModel().getFieldList().indexOf(xField),
			yFieldId = Core.getConfigModel().getFieldList().indexOf(yField);
		
		if (xFieldId == -1 || yFieldId == -1) {
			LOG.log(Level.SEVERE, "Поля для графика не определились ({0}={1}; {2}={3})", new Object[]{ xField, xFieldId, yField, yFieldId });
			return false;
		}
		
		TimeSeriesCollection collection = new TimeSeriesCollection();
		for (Startup startup : logFile.getStartups()) {
			TimeSeries timeSeries = new TimeSeries(yField.getName());
			for (Record rec : startup.getRecords()) {
				if (rec.isDirty()) continue;
				Object fieldValue = rec.getValue(xFieldId);
				if (fieldValue == null) continue;
				
				LocalDateTime datetime;
				if (xField.getDatatype() == DataType.TIME) {
					datetime = LocalDateTime.of(EPOCH_DATE, (LocalTime) fieldValue);
				} else if (xField.getDatatype() == DataType.DATE) {
					datetime = LocalDateTime.of((LocalDate) fieldValue, LocalTime.MIN);
				} else if (xField.getDatatype() == DataType.DATETIME) {
					datetime = (LocalDateTime) fieldValue;
				} else
					throw new IllegalStateException(String.format("Неподдерщиваемый тип данных для оси X (%s). Выберите DATE/TIME/DATETIME", xField.getDatatype().getValue()));
				
				fieldValue = rec.getValue(yFieldId);
				if (fieldValue == null) continue;
				double value = objectToDouble(fieldValue);
				if (value != Double.NaN) {
					timeSeries.addOrUpdate(
							new Second(datetime.getSecond(), datetime.getMinute(), datetime.getHour(),
										datetime.getDayOfMonth(), datetime.getMonthValue(), datetime.getYear()),
							value
					);
				}
			}
			if (!timeSeries.isEmpty())
				collection.addSeries(timeSeries);
		}
		
		Color color = yField.getColor() != null ? yField.getColor() : Color.PINK;
		
		NumberAxis numberAxis = new NumberAxis(yField.getName());
        numberAxis.setAutoRangeIncludesZero(false);
        numberAxis.setLabelPaint(color);
        numberAxis.setInverted(true);
        numberAxis.setAutoRangeStickyZero(false);
        numberAxis.setVisible(true);
        
        yFields.add(yField);
        int id = yFields.size();
        plot.setRangeAxis(id, numberAxis);
        plot.setRangeAxisLocation(id, AxisLocation.BOTTOM_OR_LEFT);
        
        XYDataset xydsDepth = collection;
        plot.setDataset(id, xydsDepth);
        plot.mapDatasetToRangeAxis(id, id);
        
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
        for (int i = 0; i < collection.getSeriesCount(); i++) {
        	renderer.setSeriesLinesVisible(i, true);
        	renderer.setSeriesPaint(i, color);
        	renderer.setSeriesShapesVisible(i, false);
        	renderer.setSeriesShapesFilled(i, false);
		}
        plot.setRenderer(id, renderer);
        return true;
	}
	
	private static double objectToDouble(Object value) {
		if (value instanceof Byte)
			return ((Byte) value).doubleValue();
		if (value instanceof Short)
			return ((Short) value).doubleValue();
		if (value instanceof Integer)
			return ((Integer) value).doubleValue();
		if (value instanceof Float)
			return ((Float) value).doubleValue();
		if (value instanceof Double)
			return ((Double) value).doubleValue();
		return Double.NaN;
	}

	public static void setLaunchVisible(boolean visible) {
		XYPlot plot = chart.getXYPlot();
		XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) plot.getRenderer(0);
		for (int i = 0; i < plot.getDataset(0).getSeriesCount(); i++)
			renderer.setSeriesShapesVisible(i, visible);
	}

	private static boolean drawable(LogFile logFile) throws IllegalStateException, UniqueFieldException {
		if (logFile == null)
			throw new IllegalStateException("Неизвестная ошибка (LogFile is NULL)");
		
		if (logFile.getStartups().size() == 0)
			throw new IllegalStateException("В файле отсутствуют запуски");
		
		if (logFile.getRecords().size() == 0)
			throw new IllegalStateException("В файле нет ни одной нормальной записи");
		ConfigModel configModel = Core.getConfigModel();
		boolean xAxis = false;
		for (Field field : configModel.getFieldList()) {
			if (field.getRole() == FieldRole.X_AXIS || field.getRole() == FieldRole.DRAW)
				configModel.validateField(field);
			
			if (field.getRole() == FieldRole.X_AXIS)
				xAxis = true;
		}
		if (!xAxis)
			throw new IllegalStateException("Не настроена ось X");
		return true;
	}

	public static JFreeChart getChart() { return chart; }
	public static Field getxField() { return xField; }
	public static int getxFieldId() { return xFieldId; }
}
