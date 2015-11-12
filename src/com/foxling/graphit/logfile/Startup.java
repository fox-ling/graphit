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
package com.foxling.graphit.logfile;

import java.util.ArrayList;
import java.util.Date;

import com.foxling.graphit.Core;

/** Class that stores data of one of the startups (launches)*/
public class Startup {
	/** File's line no <br>
	 * Номер строки в файле */
	private int lineno;
	/** Launch date <br>
	 * Дата запуска */
	private Date date = null;
	/** Launch time <br>
	 * Время запуска */
	private Date time = null;
	/** Launch datetime <br>
	 *  Дата/время запуска */
	private Date datetime = null;
	
	private ArrayList<BadLine> badLines = new ArrayList<BadLine>();
	private ArrayList<Line> lines = new ArrayList<Line>();
	
	public Startup(int lineno){
		this.lineno = lineno;
	}
	
	public void addLine(Line line) {
		if (line == null)
			throw new IllegalArgumentException();
		
		lines.add(line);
	}
	
	public void addBadLine(BadLine badLine) {
		if (badLine == null)
			throw new IllegalArgumentException();
		
		badLines.add(badLine);
	}

	public int getLineNo() { return lineno; }
	public Date getDate() { return date; }
	public Date getTime() { return time; }
	public Date getDatetime() { return datetime; }
	public ArrayList<Line> getLines() { return lines; }
	public ArrayList<BadLine> getBadLines() { return badLines; }

	public void setDate(Date date) {
		this.date = date;
	}

	public void setTime(Date time) {
		this.time = time;
	}

	public void setDatetime(Date datetime) {
		this.datetime = datetime;
	}
}
