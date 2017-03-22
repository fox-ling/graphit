/*
 * Graphit - log file browser Copyright© 2015 Shamil Absalikov, foxling@live.com
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

package ru.foxling.graphit.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.FileNotFoundException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import net.miginfocom.swing.MigLayout;
import ru.foxling.graphit.utils.Resources;

public class AboutFrame extends JFrame {
  private static final long serialVersionUID = 8464754776736057728L;
  private static final String INFO_RESOURCE_FILE = "about.txt";
  private static final Logger LOG = Logger.getLogger(AboutFrame.class.getName());

  public static void main(String[] args) {
    AboutFrame frame = new AboutFrame();
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.setVisible(true);
  }

  public AboutFrame() {
    super("О программе");
    setBounds(100, 100, 600, 175);
    setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    setContentPane(createContentPane());
  }

  private Container createContentPane() {
    JPanel contentPane = new JPanel();
    contentPane.setLayout(new MigLayout("", "[grow]", "[][]"));
    contentPane.add(createCompanyInfoWidget(), "growx,wrap");
    contentPane.add(createLicenseLabel(), "");
    return contentPane;
  }

  private Component createCompanyInfoWidget() {
    JTextArea textArea = new JTextArea(); 
    textArea.setText(getCompanyInfoText());
    textArea.setEditable(false);
    textArea.setBackground(this.getBackground());
    textArea.setForeground(this.getForeground());
    textArea.setFont(this.getFont());
    textArea.setWrapStyleWord(true);
    textArea.setLineWrap(true);
    textArea.setCaretPosition(0);
    
    JScrollPane scrollPane = new JScrollPane(textArea);
    scrollPane.setBorder(null);
    return scrollPane;
  }

  private String getCompanyInfoText() {
    String text;
    try {
      text = Resources.read(INFO_RESOURCE_FILE);
    } catch (FileNotFoundException e) {
      text = "Системная ошибка: не удалось найти файл с текстом о программе";
      LOG.log(Level.WARNING, "Не удалось найти файл " + INFO_RESOURCE_FILE, e);
    }
    return text;
  }

  private Component createLicenseLabel() {
    JLabel license = new JLabel("Лицензионное соглашение");
    license.setForeground(Color.BLUE);
    license.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    license.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {
        new LicenseFrame().setVisible(true);
      }
    });
    return license;
  }

}
