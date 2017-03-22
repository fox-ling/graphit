package ru.foxling.graphit.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.io.FileNotFoundException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import ru.foxling.graphit.utils.Resources;

public class LicenseFrame extends JFrame {
  private static final long serialVersionUID = 1885363672532506115L;
  private static final Logger LOG = Logger.getLogger(LicenseFrame.class.getName());
  private static final String LICENSE_FILE = "LICENSE";

  public static void main(String[] args) {
    LicenseFrame frame = new LicenseFrame();
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.setVisible(true);
  }

  public LicenseFrame() {
    super("Лицензионное соглашение");
    setBounds(100, 100, 600, 600);
    setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    setContentPane(createContentPane());
  }

  private Container createContentPane() {
    JPanel contentPane = new JPanel();
    contentPane.setLayout(new BorderLayout());
    contentPane.add(createLicenseWidget(), BorderLayout.CENTER);
    contentPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    return contentPane;
  }

  private Component createLicenseWidget() {
    JTextArea textArea = new JTextArea(); 
    textArea.setText(getLicenseText());
    textArea.setEditable(false);
    textArea.setBorder(BorderFactory.createEmptyBorder(7, 7, 7, 7));
    textArea.setFont(this.getFont());
    textArea.setWrapStyleWord(true);
    textArea.setLineWrap(true);
    textArea.setCaretPosition(0);
    return new JScrollPane(textArea);
  }

  private String getLicenseText() {
    String text;
    try {
      text = Resources.read(LICENSE_FILE);
    } catch (FileNotFoundException e) {
      text = "Системная ошибка: не удалось найти файл с текстом о программе";
      LOG.log(Level.WARNING, "Не удалось найти файл " + LICENSE_FILE, e);
    }
    return text;
  }
}
