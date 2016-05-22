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

package ru.foxling.graphit.logfile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Period;
import java.time.temporal.ChronoUnit;
import gnu.trove.list.array.TIntArrayList;

/** Trove's <code>TIntArrayList</code> based collection for storing ascending time value sequence,
 * that could overflow on a next day up to 24854 times (restricted due to memory consume optimization) <br>
 * Detects overflow when the previous inserted value is bigger than the current <br>
 * <i>* Stores time values as seconds of a day (truncate nanos) </i><br>  */
public class TimeSequence {
	private TIntArrayList data;
	private LocalDate beginDate;
	
	/** The last non-null time value added to data array */
	private int lastAdded = Integer.MIN_VALUE;
	
	/** Days offset */
	private short dateOffset = 0;
	
	/** *Sets begin date of the sequence to epoch day (1970-01-01) */
	TimeSequence() {
		beginDate = LocalDate.ofEpochDay(0);
		data = new TIntArrayList(25, Integer.MIN_VALUE);
	}
	
	/** Returns begin date of the time sequence */
	public LocalDate getBeginDate() {
		return beginDate;
	}

	/** Sets begin date of the time sequence */
	public void setBeginDate(LocalDate beginDate){
		if (beginDate == null)
			throw new IllegalArgumentException();
		
		this.beginDate = beginDate;
	}
	
	/** ReActualizes date offset <br>
	 * *Updates {@link #beginDate} or(and) {@link #dateOffset}
	 * @see #setBeginDate(LocalDate) */
	public void setCurrentDatetime(LocalDateTime datetime){
		if (datetime == null)
			throw new IllegalArgumentException();
		
		if (size() == 0) { 
			setBeginDate(datetime.toLocalDate());
		} else {
			if (lastAdded > datetime.toLocalTime().toSecondOfDay()) ++dateOffset;
			if (beginDate.equals(LocalDate.ofEpochDay(0))) {
				setBeginDate(datetime.minus(dateOffset, ChronoUnit.DAYS).toLocalDate());
			} else {
				if (datetime.toLocalDate().isBefore(beginDate))
					throw new IllegalStateException("The beginDate after current datetime");
				
				int new_offset = Period.between(beginDate, datetime.toLocalDate()).getDays();
				if (new_offset < dateOffset)
					throw new IllegalStateException(String.format("New offset (%d) is lesser than the current (%d)", new_offset, dateOffset));
				
				dateOffset = (short) new_offset;
			}
			lastAdded = datetime.toLocalTime().toSecondOfDay();
		}
	}
	
	/** Returns size of the time sequence.  */
	public int size() {
		return data.size();
	}
	
	/** Adds time value to the end of the sequence */
	public boolean add(LocalTime time){
		if (time == null) {
			return data.add(Integer.MIN_VALUE);
		} else {
			int value = time.toSecondOfDay();
			if (lastAdded != Integer.MIN_VALUE && lastAdded > value)
				dateOffset++;
			lastAdded = value;
			return data.add(dateOffset * 86400 + value);
		}
	}
	
	/** Returns the int-value at the specified offset */
	public int getValue(int offset) {
		return data.get(offset);
	}
	
	/** Returns the time at the specified offset */
	public LocalTime getTime(int offset) {
		int value = data.get(offset);
		return value == Integer.MIN_VALUE ? null : LocalTime.ofSecondOfDay(value % 86400);
	}
	
	/** Returns the datetime at the specified offset <br>
	 * @see #setBeginDate(LocalDate) */
	public LocalDateTime getDateTime(int offset) {
		int value = data.get(offset);
		if (value == Integer.MIN_VALUE) {
			return null; 
		} else
			return LocalDateTime.of(beginDate.plus(value / 86400, ChronoUnit.DAYS), LocalTime.ofSecondOfDay(value % 86400));
	}
	
	/** Performs a binary search for value in the time sequence */
	public int binarySearch(int value) {
		return data.binarySearch(value);
	}
	
	/** Removes <code>length</code> values from the list, starting at <code>offset</code>  */
	public void remove(int offset, int length) {
		data.remove(offset, length);
		lastAdded = Integer.MIN_VALUE;
		for (int i = size() - 1; i >= 0; i--) {
			if (data.get(i) != Integer.MIN_VALUE) {
				lastAdded = data.get(i);
				break;
			}
		}
	}
}
