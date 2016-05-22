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

import java.awt.EventQueue;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.format.DateTimeFormatter;
import java.util.logging.Logger;
import javax.swing.UIManager;

import ru.foxling.graphit.ui.MainFrame;

public class Core {
	public static final DateTimeFormatter F_TIME = DateTimeFormatter.ofPattern("HH:mm:ss");
	public static final DateTimeFormatter F_DATE = DateTimeFormatter.ofPattern("dd.MM.yyyy");
	public static final DateTimeFormatter F_DATETIME = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");
	
	private static final Logger LOG = Logger.getLogger(Core.class.getPackage().getName());
	private static final LoggerMemoryHandler memHandler = new LoggerMemoryHandler(512);

	static {
		LOG.addHandler(memHandler);
	}
	
	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					MainFrame frame = new MainFrame();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	public static byte[] getResource(String name) throws IOException {
		InputStream iStream = Core.class.getClassLoader().getResourceAsStream(name);
		ByteArrayOutputStream oStream = new ByteArrayOutputStream();
		int l = 0;
		byte[] buffer = new byte[1024];
		while (-1 != (l = iStream.read(buffer)))
			oStream.write(buffer, 0, l);
		return oStream.toByteArray(); 
	}
	
	public static LoggerMemoryHandler getMemoryHandler() {
		return Core.memHandler;
	}
}
