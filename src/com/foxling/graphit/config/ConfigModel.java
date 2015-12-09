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

package com.foxling.graphit.config;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EventListener;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.swing.event.EventListenerList;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.jfree.util.Log;

import com.foxling.graphit.Core;

public class ConfigModel {
	public static void main(String[] args) {
		//Logger.getLogger("core").addHandler(new ConsoleHandler());
		Core.getConfigModel().saveConfig();
	}
	
	private static Logger LOG = Logger.getLogger(ConfigModel.class.getName()); 
	private static final String FILENAME = "config.xml";
	private static final String FILE_SEPARATOR = System.getProperty("file.separator");
	
	/** List of file paths where configuration file could be placed in order of priority */
	private final LinkedHashSet<String> PATHS = new LinkedHashSet<String>(Arrays.asList(
		getWorkDirPath() + FILENAME,
		getAppDataPath() + FILENAME
	));
	
	private File file;
	private Map<String,String> properties; 
	private List<Field> fieldList;
	protected EventListenerList listenerList = new EventListenerList();
	
	public ConfigModel() {
		properties = new HashMap<String,String>();
		fieldList = new ArrayList<Field>();
		
		setDefaults();
		try {
			file = getConfigFile();
			if (file != null) {
				loadConfig();
			} else {
				makeConfigFile();
				setDefaults();
			}
		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, e.getMessage(), "Ошибка при инициализации настроек", JOptionPane.ERROR_MESSAGE);
		}
	}
	
	public void setDefaults(){
		fieldList.clear();
		properties.clear();
		properties.put("field-delimiter", "; ");
		properties.put("line-delimiter", "{CR}{LF}");
		properties.put("date-format", "dd.MM.YYYY");
		properties.put("time-format", "HH:mm:ss");
		properties.put("datetime-format", "dd.MM.YYYY HH:mm:ss");
	}
	
	public String getProperty(String name) {
		return properties.get(name);
	}
	
	public void setProperty(String key, String value){
		properties.put(key, value);
	}
	
	/*
	 *  -----=== File Management ===-----
	 */
	
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
	
	public void loadConfig(){
		try {
			SAXBuilder saxBuilder = new SAXBuilder();
			Document document = saxBuilder.build(file);
			List<Element> nodes = document.getRootElement().getChildren();
			nodes.forEach(node -> {
				switch(node.getName()) {
					case "defaults":
						node.getChildren().forEach(e -> {
							setProperty(e.getName(), e.getValue());
						});
						break;
					case "field":
						try {
							Map<String,String> properties = new LinkedHashMap<String,String>(node.getChildren().size());
							List<FieldValue> values = null;
							List<Element> elements = node.getChildren();
							for (Element e : elements) {
								if (e.getName() == "values") {
									try {
										List<Element> items = e.getChildren();
										values = new ArrayList<FieldValue>(items.size());
										for (Element item : items){
											values.add(new FieldValue(null, item.getAttributeValue("text"), item.getAttributeValue("value")));
										}
									} catch (Exception ex) {
										values = null;
										LOG.log(Level.WARNING, "Ошибка при загрузке набора значений поля \"{0}\". {1}", new Object[] { properties.get("name"), ex });
									}
								} else
									properties.put(e.getName(), e.getText());
							}
							
							Field field = fieldFactory(properties, values);
							fieldList.add(field);
						} catch (Exception e) {
							LOG.log(Level.WARNING, "Ошибка при загрузке конфигурации поля \"{0}\". {1}", new Object[] { properties.get("name"), e });
						}
						break;
				}
			});
			fireFieldListChanged(new FieldListEvent(this, FieldListEvent.INSERT, null));
		} catch(JDOMException e) {
			e.printStackTrace();
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	public void saveConfig(){
		try {
	         Element eRoot = new Element("config");
	         Document doc = new Document(eRoot);
	         Element defaults = new Element("defaults");
	         properties.forEach((key, value) -> {
	        	 defaults.addContent(xmlElementFactory(key, value));
	         });
	         eRoot.addContent(defaults);

	         fieldList.forEach((field) -> {
	        	 Element eField = new Element("field");
	        	 eField.addContent(xmlElementFactory("name", field.getName()));
	        	 eField.addContent(xmlElementFactory("description", field.getDescription()));
	        	 eField.addContent(xmlElementFactory("delimiter", field.getDelimiter().name()));
	        	 eField.addContent(xmlElementFactory("datatype", field.getDatatype().getValue()));
	        	 eField.addContent(xmlElementFactory("format", field.getFormatValue()));
	        	 
	        	 if (field.isOptional())
	        		 eField.addContent(xmlElementFactory("optional", "1"));
	        	 
	        	 if (field.isBitmask())
	        		 eField.addContent(xmlElementFactory("bitmask", "1"));
	        	 
	        	 List<FieldValue> values = field.getValueList();
	        	 if (values != null && !values.isEmpty()) {
	        		 Element eValues = new Element("values");
	        		 values.forEach((value) -> {
	        			 Element item = new Element("item");
	        			 item.setAttribute("value", value.source);
	        			 item.setAttribute("text", value.caption);
	        			 eValues.addContent(item);
	        		 });
	        		 eField.addContent(eValues);
	        	 }
	        	 
	        	 eRoot.addContent(eField);
	         });
	         
	         XMLOutputter xmlOutput = new XMLOutputter();
	         xmlOutput.setFormat(Format.getPrettyFormat());
	         xmlOutput.output(doc, System.out);
	         //xmlOutput.output(doc, new FileWriter(file)); 
	      }catch(IOException e){
	         e.printStackTrace();
	      }
	}
	
	/**	Returns storage path in current user's [Application Data] folder</br>
	 * <i>*depends on OS, ex.: ...\AppData\Roaming\<code>%package_name%</code>\</i> */
	private static String getAppDataPath(){
		return System.getenv("APPDATA")
				+ FILE_SEPARATOR
				+ ConfigModel.class.getPackage().getName()
				+ FILE_SEPARATOR;
	}
	
	/**	Returns workdir path */
	private static String getWorkDirPath() {
		return System.getProperty("user.dir") + FILE_SEPARATOR;
	}
	
	private Element xmlElementFactory(String tagname, String value) {
		Element e = new Element(tagname);
		e.setText(value);
		return e;
	}
	
	/*
	 * -----=== Поля ===----- 
	 */
	
	/** The field factory creates an empty field and tries to set
	 * each properties and values are supplied. Exceptions don't interrupt
	 * execution. 
	 * @param properties field properties
	 * @param values field values
	 * @return new field */
	protected Field fieldFactory(Map<String,String> properties, List<FieldValue> values) {
		Field field = new Field();
		
		for (String property : properties.keySet()) {
			try {
				String value = properties.get(property);
				switch (property) {
					case "name":
						field.setName(value); break;
					case "description":
						field.setDescription(value); break;
					case "datatype":
						field.setDatatype(value); break;
					case "delimiter":
						field.setDelimiter(value); break;
					case "format":
						field.setFormat(value); break;
					case "optional":
						field.setOptional(value); break;
					case "bitmask":
						field.setBitmask(value); break;
				}
			} catch (Exception e) {
				LOG.log(Level.SEVERE, String.format("Ошибка при загрузке свойства поля [%s]. ", field.getName(), e.getMessage()), e);
			}
		}
		
		try {
			field.setParser(DefaultParser.getDefaultParser(field.getDatatype(), field.getFormat()));
		} catch (Exception e) {
			LOG.log(Level.SEVERE, String.format("Ошибка при загрузке свойства поля [%s]. ", field.getName(), e.getMessage()), e);
		}
		
		if (values != null)
			for (FieldValue value : values) {
				try {
					field.addValue(value);
					field.validateValue(value);
				} catch (Exception e) {
					LOG.log(Level.SEVERE, String.format("Ошибка при загрузке значения поля [%s]. ", field.getName(), e.getMessage()), e);
				}
			}
		
		return field;
	}
	
	public int getFieldSetSize(){
		return this.fieldList.size();
	}
	
	public Field getField(int index){
		return this.fieldList.get(index);
	}
	
	public void addFieldAfter(Field prevField) {
		try {
			Field field = new Field();
			
			int id = -1;
			if (prevField == null) {
				fieldList.add(field);
				id = fieldList.size() - 1;
			} else {
				id = fieldList.indexOf(prevField) + 1;
				if (id == 0)
					throw new IllegalArgumentException("Предыдущего поля нет в списке полей");
				fieldList.add(id, field);
			}
			
			fireFieldListChanged(new FieldListEvent(this, FieldListEvent.INSERT, field));
		} catch (Exception e) {
			Log.error("Не удалось создать новое поле", e);
		}
	}
	
	public void removeField(Field field) {
		removeFields(Arrays.asList(field));
	}
	
	public void removeFields(List<Field> list) {
		boolean result = false;
		for (Field field : list) {
			if (fieldList.remove(field))
				result = true;
		}
		if (result)
			fireFieldListChanged(new FieldListEvent(this, FieldListEvent.DELETE, null));
	}
	
	public void addFieldValueAt(Field field, Integer index, FieldValue value) {
		try {
			if (index == null) {
				field.addValue(value);
			} else
				field.addValueAt(index, value);
			fireFieldListChanged(new FieldListEvent(this, FieldListEvent.UPDATE, field));
		} catch (Exception e) {
			Log.error("Не удалось добавить значение поля", e);
		}
		
	}
	
	public void removeFieldValue(Field field, int[] index) {
		if (index.length == 0)
			return;
		
		try {
			field.removeValues(index);
			fireFieldListChanged(new FieldListEvent(this, FieldListEvent.UPDATE, field));
		} catch (Exception e) {
			Log.error("Не удалось удалить значение поля", e);
		}
	}
	
	
	/*
	 * ---=== Event Management ===---
	 */
	
	public void fireFieldListChanged(FieldListEvent evt) {
		for (FieldListListener listener : getListeners(FieldListListener.class)) {
			listener.fieldListChanged(evt);
		}
	}
	
	public void addFieldListListener(FieldListListener listener) {
		listenerList.add(FieldListListener.class, listener);
	}
	
	public void removeFieldListListener(FieldListListener listener) {
		listenerList.remove(FieldListListener.class, listener); 
	}
	
	public <T extends EventListener> T[] getListeners(Class<T> listenerType) {
		return listenerList.getListeners(listenerType);
	}
}

