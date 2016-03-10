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

import java.util.EventListener;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.SimpleFormatter;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.EventListenerList;

public class LoggerMemoryHandler
extends Handler {
	private static final int DEFAULT_SIZE = 500;
	
	protected EventListenerList listenerList = new EventListenerList();
	
	private final LogRecord[] buffer;
	private int position;
	private Level level;
	private boolean overwrite;
	//private Formatter formatter;
	
	public LoggerMemoryHandler() {
		this(DEFAULT_SIZE, Level.ALL);
	}
	
	public LoggerMemoryHandler(Level level) {
		this(DEFAULT_SIZE, level);
	}
	
	public LoggerMemoryHandler(int bufferSize) {
		this(bufferSize, Level.ALL);
	}
	
	public LoggerMemoryHandler(int bufferSize, Level level) { 
		if (bufferSize <= 0 || level == null)
			throw new IllegalArgumentException();
	
		buffer = new LogRecord[bufferSize];
		this.level = level;
		setFormatter(new SimpleFormatter());
	}
	
	public void publish(LogRecord record) {
		if (record.getLevel().intValue() < level.intValue())
			return;
	
		buffer[position] = record;
		
		if (++position == buffer.length) {
			overwrite = true;
			position = 0;
		}
		
		fireBufferChanged(new ChangeEvent(this));
	}
	
	public int getSize() {
		return overwrite ? buffer.length : position;
	}
	
	public Level getLevel() {
		return this.level;
	}
	
	/*public String getFormattedMessage(LogRecord rec) {
		return formatter.formatMessage(rec);
	}*/
	
	public void flush() {
		for (int i = 0; i < getSize() - 1; i++)
			buffer[i] = null;
		
		overwrite = false;
		position = 0;
	}
	
	public void close() {
		flush();
	}
	
	public LogRecord getRecord(int index) {
		if (overwrite) {
			int ix = (index + position) % buffer.length;
			return buffer[ix];
		} else
			return buffer[index];
	}
	
	/* ---==== EVENT SECTION ====--- */
	
	public void fireBufferChanged(ChangeEvent evt) {
		for (ChangeListener listener : getListeners(ChangeListener.class))
			listener.stateChanged(evt);
	}
	
	public void addChangeListener(ChangeListener listener) {
		listenerList.add(ChangeListener.class, listener);
	}
	
	public void removeChangeListener(ChangeListener listener) {
		listenerList.remove(ChangeListener.class, listener); 
	}
	
	private <T extends EventListener> T[] getListeners(Class<T> listenerType) {
		return listenerList.getListeners(listenerType);
	}
}