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

package ru.foxling.graphit.config;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EventListener;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
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
import ru.foxling.graphit.Core;

public class ConfigModel
implements Serializable {
	public static void main(String[] args) {
		Core.getConfigModel().saveConfig();
	}
	
	private static final long serialVersionUID = 838784836109476476L;
	private static Logger LOG = Logger.getLogger(ConfigModel.class.getName()); 
	private static final String FILENAME = "config.xml";
	private static final String FILE_SEPARATOR = System.getProperty("file.separator");
	public static final String WORKDIR_PATH = getWorkDirPath() + FILENAME;
	public static final String APPDATA_PATH = getAppDataPath() + FILENAME; 
	
	/** List of file paths where configuration file could be placed in order of priority */
	private final LinkedHashSet<String> PATHS = new LinkedHashSet<String>(Arrays.asList(WORKDIR_PATH, APPDATA_PATH));
	
	private File file;
	private Map<String,String> properties;
	private LinkedList<String> recentFiles;
	private ArrayList<Field> fieldList;
	protected EventListenerList listenerList = new EventListenerList();
	
	public ConfigModel() {
		properties = new HashMap<String,String>();
		recentFiles = new LinkedList<String>();
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
		properties.put("field-delimiter", "SEMICOLON_SPACE");
		properties.put("line-delimiter", "CRLF");
		properties.put("date-format", "dd.MM.YYYY");
		properties.put("time-format", "HH:mm:ss");
		properties.put("datetime-format", "dd.MM.YYYY HH:mm:ss");
	}
	
	public String getProperty(String name) {
		return properties.get(name);
	}
	
	public void setProperty(String key, String value){
		String xValue = properties.get(key);
		if (xValue != null && xValue.equals(value))
			return;
		
		properties.put(key, value);
		firePropertyChanged(new PropertyEvent(this, key, value, xValue));
	}
	
	/*
	 *  -----=== File Management ===-----
	 */
	
	/** Returns config file is exists, <code>null</code> otherwise */
	private File getConfigFile() {
		for (String path : PATHS) {
			File file = new File(path);
			if (file.exists())
				return file;
		}
		return null;
	}
	
	public String getConfigFileLocation() {
		if (file == null) {
			return null;
		}
		
		return file.getAbsolutePath();
	}
	
	/** Sets config.xml location
	 * @param path config file location. Gotta provide one of the paths in {@link #PATHS} */
	private boolean setConfigFileLocation(File f) {
		String xLocation = null;
		if (file != null)
			xLocation = file.getAbsolutePath();
		
		if (!f.exists()) {
			LOG.severe("Не удалось установить файл настроек: " + f.getAbsolutePath() + " - не существует");
			return false;
		}
		
		file = f;
		firePropertyChanged(new PropertyEvent(this, "config-file-location", f.getAbsolutePath(), xLocation));
		return true;
	}
	
	/** Sets config.xml location
	 * @param path config file location. Gotta provide one of the paths in {@link #PATHS} */
	public boolean moveConfigFile(final String path) {
		if (path == null) {
			LOG.warning("Путь назначения = NULL");
			return false;
		}
		if (this.file == null) {
			LOG.warning("Исходный путь не установлен -- нечего переносить.");
			return false;
		}
		
		File f = new File(path);
		if (f.getAbsolutePath().equals(this.file.getAbsolutePath())) {
			LOG.info("Файл уже там: " + f.getAbsolutePath());
			return true;
		}
		
		if (!f.exists() || 
				JOptionPane.showConfirmDialog(null, "Файл config.xml уже существует по адресу: " + f.getAbsolutePath() + "\n\rЗаменить?"
						, "Внимание", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
			if (f.exists() && !f.delete()) {
				LOG.warning("Не удалось удалить существующий файл " + f.getAbsolutePath());
				return false;
			}
			File parent = f.getParentFile(); 
			if (!parent.exists() && !parent.mkdirs()) {
				LOG.warning("Не удалось создать папку " + parent.getAbsolutePath());
				return false;
			}
			if (file.renameTo(f) && setConfigFileLocation(f)) {
				LOG.info("Файл успешно перенесен: " + f.getAbsolutePath());
				return true;
			} else {
				LOG.warning("Не удалось перенести файл в " + f.getAbsolutePath());
				JOptionPane.showMessageDialog(null, "Не удалось перенести файл", "Ошибка", JOptionPane.ERROR_MESSAGE);
				return false;
			}
		}
		return false;
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
					case "field":
						try {
							Map<String,String> fieldProperties = new LinkedHashMap<String,String>(node.getChildren().size());
							List<FieldValue> fieldValues = null;
							List<Element> elements = node.getChildren();
							for (Element e : elements) {
								if (e.getName() == "values") {
									try {
										List<Element> items = e.getChildren();
										fieldValues = new ArrayList<FieldValue>(items.size());
										for (Element item : items){
											fieldValues.add(new FieldValue(null, item.getAttributeValue("caption"), item.getAttributeValue("description"), item.getAttributeValue("value")));
										}
									} catch (Exception ex) {
										fieldValues = null;
										LOG.log(Level.WARNING, "Ошибка при загрузке набора значений поля \"{0}\". {1}", new Object[] { fieldProperties.get("name"), ex });
									}
								} else
									fieldProperties.put(e.getName(), e.getText());
							}
							
							Field field = fieldFactory(fieldProperties, fieldValues);
							fieldList.add(field);
							fireFieldChanged(new FieldEvent(field, FieldEvent.INSERT));
						} catch (Exception e) {
							LOG.log(Level.WARNING, "Ошибка при загрузке конфигурации поля \"{0}\". {1}", new Object[] { properties.get("name"), e });
						}
						break;
					case "recent":
						node.getChildren().forEach(e -> recentFiles.add(e.getValue()));
						break;
					default:
						setProperty(node.getName(), node.getValue());
						break;
				}
			});
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
	         properties.forEach((key, value) -> eRoot.addContent(xmlElementFactory(key, value)));
	         
	         Element eRecent = new Element("recent");
	         recentFiles.forEach(e -> eRecent.addContent(xmlElementFactory("item", e)));
	         eRoot.addContent(eRecent);
	         
	         fieldList.forEach((field) -> {
	        	 Element eField = new Element("field");
	        	 eField.addContent(xmlElementFactory("name", field.getName()));
	        	 eField.addContent(xmlElementFactory("description", field.getDescription()));
	        	 eField.addContent(xmlElementFactory("delimiter", field.getDelimiter().name()));
	        	 eField.addContent(xmlElementFactory("datatype", field.getDatatype().getValue()));
	        	 eField.addContent(xmlElementFactory("format", field.getFormatValue()));
	        	 eField.addContent(xmlElementFactory("state", field.getRole().name()));
	        	 
	        	 if (field.isOptional())
	        		 eField.addContent(xmlElementFactory("optional", "1"));
	        	 
	        	 if (field.isBitmask())
	        		 eField.addContent(xmlElementFactory("bitmask", "1"));
	        	 
	        	 if (field.isHashsum())
	        		 eField.addContent(xmlElementFactory("hashsum", "1"));
	        	 
	        	 List<FieldValue> values = field.getValueList();
	        	 if (values != null && !values.isEmpty()) {
	        		 Element eValues = new Element("values");
	        		 values.forEach((value) -> {
	        			 Element item = new Element("item");
	        			 item.setAttribute("value", value.source);
	        			 if (value.caption != null && !value.caption.isEmpty())
	        				item.setAttribute("caption", value.caption);
	        			 if (value.description != null && !value.description.isEmpty())
	        				item.setAttribute("description", value.description);
	        			 eValues.addContent(item);
	        		 });
	        		 eField.addContent(eValues);
	        	 }
	        	 
	        	 eRoot.addContent(eField);
	         });
	         
	         XMLOutputter xmlOutput = new XMLOutputter();
	         xmlOutput.setFormat(Format.getPrettyFormat());
	         //xmlOutput.output(doc, System.out);
	         xmlOutput.output(doc, new FileWriter(file)); 
	      }catch(IOException e){
	         e.printStackTrace();
	      }
	}
	
	/**	Returns storage path in current user's [Application Data] folder</br>
	 * <i>*depends on OS, ex.: ...\AppData\Roaming\<code>%package_name%</code>\</i> */
	private static String getAppDataPath(){
		return System.getenv("APPDATA")
				+ FILE_SEPARATOR
				+ Core.class.getPackage().getName()
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
	
	public void addRecentFile(String path) {
		boolean move = recentFiles.remove(path);
		recentFiles.push(path);
		if (recentFiles.size() > 10) {
			recentFiles.removeLast();
			firePropertyChanged(new PropertyEvent(this, "recent-file", null, PropertyEvent.UPDATE));
		} else
			if (move) {
				firePropertyChanged(new PropertyEvent(this, "recent-file", path, PropertyEvent.UPDATE));
			} else
				firePropertyChanged(new PropertyEvent(this, "recent-file", path, PropertyEvent.INSERT));
	}
	
	public void removeRecentFile(String path) {
		if (recentFiles.remove(path))
			firePropertyChanged(new PropertyEvent(this, "recent-file", path, PropertyEvent.DELETE));
	}

	public void removeRecentFiles() {
		boolean fire = !recentFiles.isEmpty();
		recentFiles.clear();
		if (fire)
			firePropertyChanged(new PropertyEvent(this, "recent-file", null, PropertyEvent.DELETE));
	}
	
	public LinkedList<String> getRecentFiles() {
		return recentFiles;
	}
	
	
	/*
	 * -----=== FieldList Mgmt ===----- 
	 */
	
	/** The field factory creates an empty field and tries to set
	 * each properties and values are supplied. Exceptions don't interrupt
	 * execution. 
	 * @param properties field properties
	 * @param values field values
	 * @return new field */
	protected Field fieldFactory(Map<String,String> properties, List<FieldValue> values) {
		Field field = new Field();
		field.addFieldListener(this::fireFieldChanged); // Event Propagation
		
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
					case "state":
						field.setRole(value); break;
					case "optional":
						field.setOptional(value); break;
					case "bitmask":
						field.setBitmask(value); break;
					case "hashsum":
						field.setHashsum(value); break;
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
					try {
						value.validateValue(field.getParser());
					} catch (Exception e) {
						throw new Exception(String.format("Не удалось конвертировать строку '%s' в тип %s", value.source, field.getDatatype().getCaption()));
					}
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
	
	public FieldDelimiter getDefaultFieldDelimiter() {
		String property = getProperty("field-delimiter");
		if (property != null) {
			return FieldDelimiter.valueOf(property);
		} else
			return FieldDelimiter.getDefaultFieldDelimiter();
	}
	
	public FieldDelimiter getDefaultLineDelimiter() {
		String property = getProperty("line-delimiter");
		if (property != null) {
			return FieldDelimiter.valueOf(property);
		} else
			return FieldDelimiter.getDefaultLineDelimiter();
	}
	
	public void addFieldAfter(Field prevField) {
		try {
			Field field = new Field();
			
			if (prevField == null) {
				int size = fieldList.size(); 
				if (size > 0)
					prevField = fieldList.get(size - 1);
				
				fieldList.add(field);
			} else {
				int id = fieldList.indexOf(prevField);
				if (id == -1)
					throw new IllegalArgumentException("Предыдущего поля нет в списке полей");
				fieldList.add(id + 1, field);
			}
			
			if (fieldList.indexOf(field) == fieldList.size() - 1){
				field.setDelimiter(getDefaultLineDelimiter());
				if (prevField != null && prevField.getDelimiter() == getDefaultLineDelimiter())
					prevField.setDelimiter(getDefaultFieldDelimiter());
			} else
				field.setDelimiter(getDefaultFieldDelimiter());
			
			fireFieldChanged(new FieldEvent(field, FieldEvent.INSERT));
		} catch (Exception e) {
			LOG.log(Level.SEVERE, "Не удалось создать новое поле", e);
		}
	}
	
	public void removeField(Field field) {
		removeFields(Arrays.asList(field));
	}
	
	public void removeFields(List<Field> list) {
		for (Field field : list) {
			if (fieldList.remove(field))
				fireFieldChanged(new FieldEvent(field, FieldEvent.DELETE));
		}	
	}
	
	/*
	 * -----=== Field mgmt ===----- 
	 */
	
	public boolean setFieldName(Field field, String name) {
		try {
			if (field == null)
				throw new NullPointerException("Поле - NULL");
			
			if (field.getName().equals(name))
				return false;
			
			field.setName(name);
			return true;
		} catch (Exception e) {
			LOG.log(Level.SEVERE, "Не удалось изменить имя поля", e);
			return false;
		}
	}

	public void setFieldDescription(Field field, String description) {
		try {
			if (field == null)
				throw new NullPointerException("Поле - NULL");
			
			field.setDescription(description);
		} catch (Exception e) {
			LOG.log(Level.SEVERE,"Не удалось изменить описание поля", e);
		}
	}
	
	public void setFieldDatatype(Field field, DataType datatype) {
		try {
			if (field == null)
				throw new NullPointerException("Поле - NULL");
			
			field.setDatatype(datatype);
		} catch (Exception e) {
			LOG.log(Level.SEVERE, "Не удалось изменить тип данных поля", e);
		}
	}

	public void setFieldDelimiter(Field field, FieldDelimiter delimiter) {
		try {
			if (field == null)
				throw new NullPointerException("Поле - NULL");
			
			field.setDelimiter(delimiter);
		} catch (Exception e) {
			LOG.log(Level.SEVERE, "Не удалось изменить тип данных поля", e);
		}
	}
	
	public void setFieldFormat(Field field, ru.foxling.graphit.config.Format format) {
		try {
			if (field == null)
				throw new NullPointerException("Поле - NULL");
			
			field.setFormat(format);
		} catch (Exception e) {
			LOG.log(Level.SEVERE, "Не удалось изменить формат поля", e);
		}
	}
	
	public boolean setFieldFormat(Field field, String format) {
		try {
			if (field == null)
				throw new NullPointerException("Поле - NULL");
			
			field.setFormat(format);
			return true;
		} catch (Exception e) {
			LOG.log(Level.SEVERE, "Не удалось изменить формат поля", e);
			return false;
		}
	}
	
	public boolean setFieldOptional(Field field, boolean isOptional) {
		Field xField = null;
		try {
			if (field == null)
				throw new NullPointerException("Поле - NULL");
			
			if (isOptional)
				for (Field f : fieldList) {
					if (!f.equals(field) && f.isOptional()) {
						if (JOptionPane.showConfirmDialog(null, String.format("В наборе полей уже есть необязательное поле - \"%s\". "
								+ "Может быть только одно необязательное поле.\n\rУбрать флажок с поля %s? ", f, f)) == JOptionPane.YES_OPTION) {
							f.setOptional(false);
							xField = f;
						} else
							return false;
					}
				}
			
			field.setOptional(isOptional);
			return true;
		} catch (Exception e) {
			if (xField != null) // rolling back optional changing
				xField.setOptional(true);
			LOG.log(Level.SEVERE, "Не удалось изменить опциональность поля", e);
			return false;
		}
	}
	
	public void setFieldBitmask(Field field, boolean isBitmask) {
		try {
			if (field == null)
				throw new NullPointerException("Поле - NULL");
			
			field.setBitmask(isBitmask);
		} catch (Exception e) {
			LOG.log(Level.SEVERE, "Не удалось изменить свойство поля \"битовая маска\"", e);
		}
	}
	
	public boolean setFieldHashsum(Field field, boolean isHashsum) {
		Field xField = null;
		try {
			if (field == null)
				throw new NullPointerException("Поле - NULL");
			
			if (field.isHashsum() == isHashsum)
				return true;
			
			if (isHashsum)
				for (Field f : fieldList) {
					if (!f.equals(field) && f.isHashsum()) {
						if (JOptionPane.showConfirmDialog(null, String.format("В наборе полей уже есть хэш-сумма - поле \"%s\". "
								+ "Может быть только одно поле хранящее хэш-сумму.\n\rУбрать флажок с поля %s? ", f, f)) == JOptionPane.YES_OPTION) {
							f.setHashsum(false);
							xField = f;
						} else
							return false;
					}
				}
			
			field.setHashsum(isHashsum);
			return true;
		} catch (Exception e) {
			if (xField != null) // rolling back bitmask changing
				xField.setBitmask(true);
			LOG.log(Level.SEVERE, "Не удалось изменить свойство поля \"битовая маска\"", e);
			return false;
		}
	}
	
	/**
	 * @param field which axis you wanna toggle
	 * @param visible wanna draw it or not */
	public boolean toggleYAxis(Field field, boolean visible) {
		try {
			if (field == null)
				throw new NullPointerException("Поле - NULL");
			
			FieldRole role = field.getRole();
			if (visible && role == FieldRole.X_AXIS)
				throw new IllegalStateException(String.format("Поле \"%s\" является X-осью"));
			if (visible && role == FieldRole.DRAW ||
					!visible && role == FieldRole.NONE)
						return true;
			
			if (visible && role == FieldRole.NONE) {
				return setFieldRole(field, FieldRole.DRAW);
			} else
				if (!visible && role == FieldRole.DRAW)
					return setFieldRole(field, FieldRole.NONE);
			
			return true;
		} catch (Exception e) {
			LOG.log(Level.WARNING, "Не удалось изменить ось x", e);
			return false;
		}
	}
	
	public boolean setFieldRole(Field field, FieldRole role) {
		Field xField = null;
		try {
			if (field == null)
				throw new NullPointerException("Поле - NULL");
			
			if (role == FieldRole.X_AXIS)
				for (Field f : fieldList) {
					if (!f.equals(field) && f.getRole() == FieldRole.X_AXIS) {
						if (JOptionPane.showConfirmDialog(null, String.format("В наборе полей уже есть поле отмеченное как ось X - \"%s\". "
								+ "Может быть только одна ось X.\n\rУбрать признак с поля %s? ", f, f)) == JOptionPane.YES_OPTION) {
							f.setRole(FieldRole.NONE);
							xField = f;
						} else
							return false;
					}
				}
				
			field.setRole(role);
			return true;
		} catch (Exception e) {
			if (xField != null)
				try {     // try to rollback role changing
					xField.setRole(FieldRole.X_AXIS);
				} catch (Exception e2) {
					LOG.log(Level.SEVERE, "Не удалось откатить роль поля ", e);
				}
			LOG.log(Level.SEVERE, "Не удалось изменить роль поля", e);
			return false;
		}
	}
	
	public void addFieldValueAt(Field field, Integer index, FieldValue value) {
		try {
			if (index == null) {
				field.addValue(value);
			} else
				field.addValueAt(index, value);
		} catch (Exception e) {
			LOG.log(Level.SEVERE, "Не удалось добавить значение поля", e);
		}
		
	}
	
	public void removeFieldValue(Field field, int[] index) {
		if (index.length == 0)
			return;
		
		try {
			field.removeValues(index);
		} catch (Exception e) {
			LOG.log(Level.SEVERE, "Не удалось удалить значение поля", e);
		}
	}
	
	public boolean getLaunchVisible() {
		String value = getProperty("launch-visible");
		return value != null && value.equals("1");
	}
	
	public void setLaunchVisible(boolean visible) {
		if (visible) {
			setProperty("launch-visible", "1");
		} else
			setProperty("launch-visible", "0");
	}
	
	public boolean getTableVisible() {
		String value = getProperty("table-visible");
		return value != null && value.equals("1");
	}
	
	public void setTableVisible(boolean visible) {
		if (visible) {
			setProperty("table-visible", "1");
		} else
			setProperty("table-visible", "0");
	}
	
	
	/*
	 * ---=== Event Management ===---
	 */
	
	public void firePropertyChanged(PropertyEvent evt) {
		for (PropertyListener listener : getListeners(PropertyListener.class))
			listener.propertyChanged(evt);
	}
	
	public void addPropertyListener(PropertyListener listener) {
		listenerList.add(PropertyListener.class, listener);
	}
	
	public void removePropertyListener(PropertyListener listener) {
		listenerList.remove(PropertyListener.class, listener); 
	}
	
	public void fireFieldChanged(FieldEvent evt) {
		for (FieldListener listener : getListeners(FieldListener.class))
			listener.fieldChanged(evt);
	}
	
	public void addFieldListener(FieldListener listener) {
		listenerList.add(FieldListener.class, listener);
	}
	
	public void removeFieldListener(FieldListener listener) {
		listenerList.remove(FieldListener.class, listener); 
	}
	
	public <T extends EventListener> T[] getListeners(Class<T> listenerType) {
		return listenerList.getListeners(listenerType);
	}
	
	public ArrayList<Field> cloneFieldList(){
		try {
			byte[] state;
			
			ByteArrayOutputStream oStream = new ByteArrayOutputStream();
			ObjectOutputStream out = new ObjectOutputStream(oStream);
			out.writeObject(fieldList);
			out.flush();
			state = oStream.toByteArray();
			out.close();
			
			ByteArrayInputStream iStream = new ByteArrayInputStream(state);
			ObjectInputStream in = new ObjectInputStream(iStream);
			return (ArrayList<Field>) in.readObject();
		} catch(IOException | ClassNotFoundException ex) {
			ex.printStackTrace();
			return null;
		}
	}
}

