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

import static org.junit.Assert.*;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.WindowConstants;
import javax.swing.table.TableCellRenderer;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class EventJournalFrameTest{
	private EventJournalFrame frame;

	@Before
	public void setUp() throws Exception {
		frame = new EventJournalFrame();
		frame.setVisible(true);
		frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		Logger logger = Logger.getLogger(EventJournalFrame.class.getName());
		for (int j = 0; j < 3; j++)
			for (int i = 0; i < 3; i++) {
				Level level = Level.INFO;
				switch (i) {
				case 1: level = Level.WARNING; break;
				case 2: level = Level.SEVERE; break; 
				}
				try {
					thrower(i);
					logger.log(level, "All is well at i={0} and j={1}", new Object[]{i, j});
				} catch (Exception e) {
					logger.log(level, String.format("Some error at i=%d and j=%d", i, j), e);
				}
			}
	}
		
	public static void thrower(int i) throws Exception {
		String msg = String.format("ExceptionGenerator #%d", i);
		switch (i) {
		case 1: throw new IllegalArgumentException(msg);
		case 2: throw new IllegalStateException(msg);
		}
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void test() {
		assertTrue(true);
	}

}
