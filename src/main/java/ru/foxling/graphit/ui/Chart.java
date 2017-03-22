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
import java.time.temporal.ChronoUnit;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.ValueAxisPlot;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.Range;
import org.jfree.data.RangeType;
import org.jfree.data.time.Second;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYDataset;
import org.jfree.ui.RectangleInsets;
import ru.foxling.graphit.config.ConfigModel;
import ru.foxling.graphit.config.Field;
import ru.foxling.graphit.config.FieldRole;
import ru.foxling.graphit.logfile.LogFile;
import ru.foxling.graphit.logfile.ParsedData;
import ru.foxling.graphit.logfile.Startup;

public class Chart {
	private static final Logger LOG = Logger.getLogger(Chart.class.getName());
	private static final LocalDate EPOCH_DATE = LocalDate.ofEpochDay(0);
	private static JFreeChart instance;
	private static XYPlot plot;
	private static LogFile logFile;
	private static Field xField;
	private static List<Field> yFields = new LinkedList<>();
	private static int xFieldId;
	
	public static JFreeChart chartFactory(LogFile logFile) {
		instance = null;
		xField = null;
		xFieldId = -1;
		yFields.clear();
		List<Field> yFields = new LinkedList<>();
		List<Field> fieldList = ConfigModel.getInstance().getFieldList(); 
		for (int i = 0; i < fieldList.size(); i++) {
			Field f = fieldList.get(i);
			
			if (f.isValid()) {
				if (f.getRole() == FieldRole.X_AXIS) {
					xFieldId = i;
					xField = f;
				} else if (f.getRole() == FieldRole.DRAW)
					yFields.add(f);
			}
		}
		
		if (!drawable(logFile, xField, yFields)) {
			return null;
		}
		
		Chart.logFile = logFile;
		
		TimeSeriesCollection dsLaunch = new TimeSeriesCollection();
		TimeSeries tsLaunch = new TimeSeries("Launch");
		for (Startup startup : logFile.getStartups()) {
			LocalDateTime datetime;
			switch (xField.getDatatype()) {
			case TIME_SEQUENCE:
			case DATETIME:
				datetime = startup.getDatetime();
				break;
			case TIME:
				datetime = LocalDateTime.of(EPOCH_DATE, startup.getTime());
				break;
			case DATE:
				datetime = LocalDateTime.of(startup.getDate(), LocalTime.MIN);
				break;
			default:
				throw new IllegalStateException(String.format("Неподдерщиваемый тип данных для оси X (%s). Выберите DATE/TIME/DATETIME/TIME_SEQUENCE", xField.getDatatype().getValue()));
			}
			
			tsLaunch.addOrUpdate(new Second(datetime.getSecond(), datetime.getMinute(), datetime.getHour(), datetime.getDayOfMonth(), datetime.getMonthValue(), datetime.getYear()), -1);
		}
		dsLaunch.addSeries(tsLaunch);
		instance = ChartFactory.createTimeSeriesChart(null, null, null, dsLaunch, false, false, false);
		plot = instance.getXYPlot();
	    plot.setBackgroundPaint(Color.white);
        plot.setDomainGridlinePaint(Color.lightGray);
        plot.setRangeGridlinePaint(Color.lightGray);
        plot.setAxisOffset(new RectangleInsets(5.0, 5.0, 5.0, 5.0));
        plot.setDomainCrosshairVisible(true);
        plot.setDomainCrosshairLockedOnData(true);
        
        NumberAxis axis = (NumberAxis) plot.getRangeAxis();
        axis.setVisible(false);
        axis.setAutoRangeIncludesZero(true);
        
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
        
        if (!ConfigModel.getInstance().getLaunchVisible())
        	setLaunchVisible(false);
        
        if (xField != null)
			for (Field yField : yFields)
				plotFactory(yField);
		
        Runtime.getRuntime().gc();
        return instance;
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
		
		int xFieldId = ConfigModel.getInstance().getFieldList().indexOf(xField),
			yFieldId = ConfigModel.getInstance().getFieldList().indexOf(yField);
		if (xFieldId == -1 || yFieldId == -1) {
			LOG.log(Level.SEVERE, "Поля для графика не определились ({0}={1}; {2}={3})", new Object[]{ xField, xFieldId, yField, yFieldId });
			return false;
		}
		
		double low = Double.POSITIVE_INFINITY, high = Double.NEGATIVE_INFINITY;
		TimeSeriesCollection collection = new TimeSeriesCollection();
		
		LocalDateTime xDatetime = null;
		TimeSeries timeSeries = new TimeSeries(yField.getName());
		ParsedData data = logFile.getParsedData();
		for (int row = 0; row < data.size(); row++) {
			Object fieldValue = data.getValue(row, xFieldId);
			if (fieldValue == null) continue;
			
			LocalDateTime datetime;
			switch (xField.getDatatype()) {
			case TIME:
				datetime = LocalDateTime.of(EPOCH_DATE, (LocalTime) fieldValue);
				if (xDatetime != null && ChronoUnit.SECONDS.between(xDatetime, datetime) > 1)
					timeSeries.addOrUpdate(second(xDatetime.plus(1, ChronoUnit.SECONDS)), null);
				break;
			case DATE:
				datetime = LocalDateTime.of((LocalDate) fieldValue, LocalTime.MIN);
				if (xDatetime != null && ChronoUnit.DAYS.between(xDatetime, datetime) > 1)
					timeSeries.addOrUpdate(second(xDatetime.plus(1, ChronoUnit.SECONDS)), null);
				break;
			case DATETIME:
			case TIME_SEQUENCE:
				datetime = (LocalDateTime) fieldValue;
				if (xDatetime != null && ChronoUnit.SECONDS.between(xDatetime, datetime) > 1)
					timeSeries.addOrUpdate(second(xDatetime.plus(1, ChronoUnit.SECONDS)), null);
				break;
			default:
				throw new IllegalStateException(String.format("Неподдерщиваемый тип данных для оси X (%s). Выберите DATE/TIME/DATETIME/OVERFLOWING_TIME_SEQUENCE", xField.getDatatype().getValue()));
			}
			
			fieldValue = data.getValue(row, yFieldId);
			if (fieldValue == null) continue;
			double value = objectToDouble(fieldValue);
			if (value > high) high = value;
			if (value < low) low = value;
			if (value != Double.NaN) {
				timeSeries.addOrUpdate(second(datetime), value);
				xDatetime = datetime;
			}
		}
		if (!timeSeries.isEmpty())
			collection.addSeries(timeSeries);
		
		Color color = yField.getColor() != null ? yField.getColor() : Color.PINK;
		
		NumberAxis numberAxis = new MyNumberAxis(yField.getName(), new Range(low, high));
        numberAxis.setLabelPaint(color);
        numberAxis.setInverted(true);
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
	
	private static Second second(LocalDateTime datetime) {
		if (datetime == null)
			throw new IllegalArgumentException();
		
		return new Second(datetime.getSecond(), datetime.getMinute(), datetime.getHour(),
				datetime.getDayOfMonth(), datetime.getMonthValue(), datetime.getYear());
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
		XYPlot plot = instance.getXYPlot();
		XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) plot.getRenderer(0);
		for (int i = 0; i < plot.getDataset(0).getSeriesCount(); i++)
			renderer.setSeriesShapesVisible(i, visible);
	}

	private static boolean drawable(LogFile logFile, Field xField, List<Field> yFields) {
		try {
			if (logFile == null)
				throw new IllegalStateException("Неизвестная ошибка (LogFile is NULL)");
			
			if (logFile.getParsedData().size() == 0)
				throw new IllegalStateException("В файле нет ни одной нормальной записи");
			
			if (xField == null) {
				throw new IllegalStateException("Не настроена ось X");
			}
			
			if (yFields == null || yFields.isEmpty()) {
				throw new IllegalStateException("Не настроено ни одной оси Y");
			}
			return true;
		} catch (Exception e) {
			LOG.log(Level.SEVERE, "Построение графика невозможно: " + e.getMessage());
			return false;
		}
	}

	public static JFreeChart getInstance() { return instance; }
	public static Field getxField() { return xField; }
	public static int getxFieldId() { return xFieldId; }
	
	static class MyNumberAxis
	extends NumberAxis {
		private Range dataRange;

		private static final long serialVersionUID = 1550712004602125049L;
		
		public MyNumberAxis(String label, Range dataRange) {
			super(label);
			this.dataRange = dataRange;
		}

		@Override
		protected void autoAdjustRange() {
			if (dataRange == null) {
				super.autoAdjustRange();
			} else
				autoAdjustRange2();
		}
		
		protected void autoAdjustRange2() {
			Plot plot = getPlot();
			if (plot == null) {
				return;  // no plot, no data
			}

			if (plot instanceof ValueAxisPlot) {
				//ValueAxisPlot vap = (ValueAxisPlot) plot;

				Range r = dataRange;
				if (r == null) {
					r = getDefaultAutoRange();
				}

				double upper = r.getUpperBound();
				double lower = r.getLowerBound();
				if (this.getRangeType() == RangeType.POSITIVE) {
					lower = Math.max(0.0, lower);
					upper = Math.max(0.0, upper);
				}
				else if (this.getRangeType() == RangeType.NEGATIVE) {
					lower = Math.min(0.0, lower);
					upper = Math.min(0.0, upper);
				}

				if (getAutoRangeIncludesZero()) {
					lower = Math.min(lower, 0.0);
					upper = Math.max(upper, 0.0);
				}
				double range = upper - lower;

				// if fixed auto range, then derive lower bound...
				double fixedAutoRange = getFixedAutoRange();
				if (fixedAutoRange > 0.0) {
					lower = upper - fixedAutoRange;
				}
				else {
					// ensure the autorange is at least <minRange> in size...
					double minRange = getAutoRangeMinimumSize();
					if (range < minRange) {
						double expand = (minRange - range) / 2;
						upper = upper + expand;
						lower = lower - expand;
						if (lower == upper) { // see bug report 1549218
							double adjust = Math.abs(lower) / 10.0;
							lower = lower - adjust;
							upper = upper + adjust;
						}
						if (this.getRangeType() == RangeType.POSITIVE) {
							if (lower < 0.0) {
								upper = upper - lower;
								lower = 0.0;
							}
						}
						else if (this.getRangeType() == RangeType.NEGATIVE) {
							if (upper > 0.0) {
								lower = lower - upper;
								upper = 0.0;
							}
						}
					}

					if (getAutoRangeStickyZero()) {
						if (upper <= 0.0) {
							upper = Math.min(0.0, upper + getUpperMargin() * range);
						}
						else {
							upper = upper + getUpperMargin() * range;
						}
						if (lower >= 0.0) {
							lower = Math.max(0.0, lower - getLowerMargin() * range);
						}
						else {
							lower = lower - getLowerMargin() * range;
						}
					}
					else {
						upper = upper + getUpperMargin() * range;
						lower = lower - getLowerMargin() * range;
					}
				}
				
				double margin = (upper - lower) * .05d; 
				setRange(new Range(lower - margin, upper + margin), false, false);
			}

		}
	}
}
