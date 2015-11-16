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

package com.foxling.graphit;

import java.awt.EventQueue;
import java.text.SimpleDateFormat;
import java.util.logging.ConsoleHandler;
import java.util.logging.Logger;
import javax.swing.UIManager;
import com.foxling.graphit.config.ConfigModel;


public class Core {
	private static ConfigModel configModel = new ConfigModel();
	private static final Logger LOG;

	public static final SimpleDateFormat F_TIME = new SimpleDateFormat ("HH:mm:ss");
	public static final SimpleDateFormat F_DATE = new SimpleDateFormat ("dd.MM.YYYY");
	public static final SimpleDateFormat F_DATETIME = new SimpleDateFormat ("dd.MM.YYYY HH:mm:ss");
	
	static {
		LOG = Logger.getLogger("core");
		LOG.addHandler(new ConsoleHandler());
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
	
	public static ConfigModel getConfigModel(){
		return Core.configModel;
	}
}
