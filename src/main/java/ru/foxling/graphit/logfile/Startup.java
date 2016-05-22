/* graphit - log file browser
 * Copyright© 2015 Shamil Absalikov, foxling@live.com
 *
 * graphit is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * graphit is distributed in the hope that it will be useful,
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

/** Class that stores data of one of the startups (launches)*/
public class Startup {
	/** File's line no <br>
	 * Номер строки в файле */
	private int lineno;
	
	/** Launch datetime <br>
	 *  Дата/время запуска */
	private LocalDateTime datetime = null;
	
	public Startup(int lineno){
		this.lineno = lineno;
	}

	public int getLineNo() { return lineno; }
	public LocalDate getDate() { return datetime.toLocalDate(); }
	public LocalTime getTime() { return datetime.toLocalTime(); }
	public LocalDateTime getDatetime() { return datetime; }

	public void setDate(LocalDate date) {
		if (datetime == null) {
			datetime = LocalDateTime.of(date, LocalTime.MIN);
		} else {
			datetime = LocalDateTime.of(date, datetime.toLocalTime());
		}
	}

	public void setTime(LocalTime time) {
		if (datetime == null) {
			datetime = LocalDateTime.of(LocalDate.ofEpochDay(0), time);
		} else {
			datetime = LocalDateTime.of(datetime.toLocalDate(), time);
		}
	}

	public void setDatetime(LocalDateTime datetime) {
		this.datetime = datetime;
	}
}
