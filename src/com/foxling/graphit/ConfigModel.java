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

import java.awt.List;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.EventListener;
import java.util.EventObject;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Set;

import javax.swing.JOptionPane;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.foxling.graphit.ConfigModel.ConfigModelListener;

public class ConfigModel {
	public static void main(String[] args) {
		new ConfigModel();
	}
	
	private static final String FILENAME = "config.xml";
	private static final String FILE_SEPARATOR = System.getProperty("file.separator");
	/** List of file paths where configuration file could be placed in order of priority */
	private final LinkedHashSet<String> PATHS = new LinkedHashSet<String>(Arrays.asList(
		getWorkDirPath() + FILENAME,
		getAppDataPath() + FILENAME
	));
	
	private File file;
	private boolean inMemory = true;
	private Set<Field> fieldSet;
	private Set<ConfigModelListener> listeners; 
	
	public ConfigModel() {
		try {
			file = getConfigFile();
			if (file != null) {
				parseConfig();
			} else
				makeConfigFile();
			
			inMemory = false;
		} catch (Exception e) {
			inMemory = true;
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, e.getMessage(), "Ошибка при инициализации настроек", JOptionPane.ERROR_MESSAGE);
		}
		
		fieldSet = new LinkedHashSet<Field>();
		listeners = new HashSet<ConfigModelListener>();
	}
	
	/** Returns config file is exists, <code>null</code> otherwise */
	public File getConfigFile() {
		for (String path : PATHS) {
			File file = new File(path);
			if (file.exists())
				return file;
		}
		return null;
	}
	
	/** Tries to create a new config file at one of the locations {@link #PATHS}
	 * @throws RuntimeException if failed at all of them */
	public File makeConfigFile() throws RuntimeException {
		HashMap<String,String> errorText = new HashMap<String,String>(PATHS.size());
		Iterator<String> itPaths = PATHS.iterator();
		while (itPaths.hasNext()) {
			String path = itPaths.next();
			file = new File(path);
			try {
				File f = file.getParentFile();
				if (!f.exists())
					f.mkdirs();
				
				file.createNewFile();
				return file;
			} catch (IOException | NullPointerException e) {
				e.printStackTrace();
				errorText.put(path, e.getMessage());
			}
		}
		
		// ... nothing worked, throwing Exception 
		if (file == null || !file.exists()) {
			String msg = "Не удалось найти/создать файл с настройками:\n";
			for (String path : errorText.keySet()) {
				msg += "--" + path + "\n = " + errorText.get(path) + "\n\n";
			}
			throw new RuntimeException(msg);
		}
		
		return null;
	}
	
	/**	Returns storage path in current user's [Application Data] folder</br>
	 * <i>*depends on OS, ex.: ...\AppData\Roaming\<code>%package_name%</code>\</i> */
	public static String getAppDataPath(){
		return System.getenv("APPDATA")
				+ FILE_SEPARATOR
				+ ConfigModel.class.getPackage().getName()
				+ FILE_SEPARATOR;
	}
	
	/**	Returns workdir path */
	public static String getWorkDirPath() {
		return System.getProperty("user.dir") + FILE_SEPARATOR;
	}
	
	public void parseConfig(){
		
		try {
			SAXParserFactory factory = SAXParserFactory.newInstance();
			SAXParser saxParser = factory.newSAXParser();
			DefaultHandler handler = new DefaultHandler() {
				String e;
				HashMap<String, String> attr;
				
				@Override
				public void startElement(String uri, String localName, String qName, Attributes attr) throws SAXException {
					e = qName;
					switch (e) {
						case "field":
							for (int i = attr.getLength(); i >= 0 ; i--)
								this.attr.put(attr.getLocalName(i), attr.getValue(i));
							
							break;
						default:
							this.attr = null;
					}
				}
				
				@Override
				public void endElement(String uri, String localName, String qName) throws SAXException {
					//System.out.println("End Element :" + qName);
				}
				
				@Override
				public void characters(char ch[], int start, int length) throws SAXException {
					//System.out.println(e + "=" + new String(ch, start, length));
				}
			};
			saxParser.parse(file, handler);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			
		}
	}
	
	public void fireFieldSetChanged() {
		ConfigModelEvent evt = new ConfigModelEvent(this);
		for (ConfigModelListener listener : getConfigModelListeners()) {
			listener.fieldSetChanged(evt);
		}
	}
	
	public void addConfigModelListener(ConfigModelListener listener) {
		listeners.add(listener);
	}
	
	public void removeConfigModelListener(ConfigModelListener listener) {
		listeners.remove(listener); 
	}
	
	public Set<ConfigModelListener> getConfigModelListeners() {
		return listeners;
	}
	
	protected interface ConfigModelListener
	extends EventListener{
		public void fieldSetChanged(ConfigModelEvent evt);
	}
	
	protected class ConfigModelEvent
	extends EventObject {
		private static final long serialVersionUID = 7587760715994607198L;
	
		public ConfigModelEvent(Object arg0) {
			super(arg0);
		}
	}
}

