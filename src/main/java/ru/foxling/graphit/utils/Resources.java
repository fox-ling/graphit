/*
 * Graphit - log file browser Copyright© 2017 Shamil Absalikov, foxling@live.com
 *
 * Graphit is free software: you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * Graphit is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If
 * not, see <http://www.gnu.org/licenses/>.
 */

package ru.foxling.graphit.utils;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

public class Resources {
  private static final Logger LOG = Logger.getLogger(Resources.class.getName());
  private static final String FRAME_ICON_FILENAME = "graphit-32.png";
  private static Image frameIcon;
  private static boolean frameIconError;

  public static String read(String resource) throws FileNotFoundException {
    Scanner scanner = new Scanner(getInputStream(resource), "UTF-8");
    scanner.useDelimiter("\\a");
    try {
      return scanner.next();
    } finally {
      scanner.close();
    }
  }

  public static InputStream getInputStream(String resource) throws FileNotFoundException {
    InputStream is = Resources.class.getClassLoader().getResourceAsStream(resource);
    if (is == null)
      throw new FileNotFoundException(resource);
    return is;
  }

  public static Image getFrameIcon() {
    if (!frameIconError && frameIcon == null) {
      frameIcon = getResourceImage(FRAME_ICON_FILENAME);
      frameIconError = frameIcon == null;
    }
    return frameIcon;
  }

  private static BufferedImage getResourceImage(String filename) {
    BufferedImage img = null;
    try {
      img = ImageIO.read(getInputStream(filename));
      if (img == null)
        LOG.log(Level.CONFIG, "Couldn't read " + filename);
    } catch (IOException e) {
      LOG.log(Level.CONFIG, "Could not find/read " + filename, e);
    }
    return img;
  }
}
