package ru.foxling.graphit.utils;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Scanner;

public class Resources {
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
}
