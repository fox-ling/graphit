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

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import javax.swing.ImageIcon;
import javax.swing.UIManager;

public class Icons {
	private static final int ICONS_COUNT = 3;
	public static final String ICON_ERROR = "OptionPane.errorIcon";
	public static final String ICON_WARNING = "OptionPane.warningIcon";
	public static final String ICON_INFO = "OptionPane.informationIcon";
	
	private static Map<Integer,Map<String,ImageIcon>> icon_cache = new HashMap<>(2);
	
	public static ImageIcon get(Level level, int size) {
		if (level == Level.SEVERE) {
			return get(ICON_ERROR, size);
		} else if (level == Level.WARNING) {
			return get(ICON_WARNING, size);
		} else if (level == Level.INFO) {
			return get(ICON_INFO, size);
		}
		return null;
	}
	
	public static ImageIcon get(String key, int size) {
		if (key == null || key.isEmpty() || size < 1)
			throw new IllegalArgumentException();
		
		Map<String,ImageIcon> icons = icon_cache.get(size);
		if (icons == null) {
			icons = new HashMap<>(ICONS_COUNT);
			ImageIcon icon = make(key, size);
			icons.put(key, icon);
			return icon;
		} else {
			ImageIcon icon = icons.get(key);
			if (icon == null) {
				icon = make(key, size);
				icons.put(key, icon);
				return icon;
			} else
				return icon;
		}
	}
	
	private static ImageIcon make(String key, int size) {
		Image img = ((ImageIcon) UIManager.getIcon(key)).getImage();
		BufferedImage buffImg = new BufferedImage(size, size, BufferedImage.TRANSLUCENT);
		Graphics2D g2d = (Graphics2D) buffImg.createGraphics();
		g2d.addRenderingHints(new RenderingHints(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY));
		g2d.drawImage(img, 0, 0, size, size, null);
		g2d.dispose();
		return new ImageIcon(buffImg);
	}
}
