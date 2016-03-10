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

import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import javax.swing.JLabel;

public class LoggerLabelHandler
extends Handler {
	private JLabel label;
	private int height;
	private Level level;
	
	public LoggerLabelHandler(JLabel label){
		this(label, Level.ALL);
	}
	
	public LoggerLabelHandler(JLabel label, Level level) {
		if (label == null || level == null)
			throw new IllegalArgumentException();
		this.label = label;
		this.level = level;
		
		label.addPropertyChangeListener(e -> {
			if (e.getPropertyName() == "font")
				refreshHeight();
		});
		refreshHeight();
	}

	public void publish(LogRecord record)  {
		Level lvl = record.getLevel();
		if (lvl.intValue() < level.intValue())
		    return;

		label.setIcon(LoggerIcons.get(lvl,height));
		label.setText(record.getMessage());
	}
	
	@Override
	public void close() {}

	@Override
	public void flush() {}
	
	private void refreshHeight() {
		height = label.getFontMetrics(label.getFont()).getHeight();
	}
	

}
