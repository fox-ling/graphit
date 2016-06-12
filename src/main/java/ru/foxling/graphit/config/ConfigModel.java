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

package ru.foxling.graphit.config;

import java.awt.Color;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EventListener;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
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

public class ConfigModel
implements Serializable {
	private static final long serialVersionUID = 838784836109476476L;
	private static Logger LOG = Logger.getLogger(ConfigModel.class.getName()); 
	public static ConfigModel instance;
	
	/** Good colors */
	private static final List<Color> COLORS = Arrays.asList(Color.RED, Color.GREEN, Color.BLUE, Color.BLACK, Color.CYAN, Color.PINK, Color.MAGENTA);
	
	private File file;
	private Map<String,String> properties;
	private LinkedList<String> recentFiles;
	private List<Field> fieldList;
	protected EventListenerList listenerList = new EventListenerList();
	
	private ConfigModel() {
		properties = new HashMap<String,String>();
		recentFiles = new LinkedList<String>();
		fieldList = new ArrayList<Field>();
		
		
		setDefaults();
		try {
			file = new File(System.getProperty("user.dir") + System.getProperty("file.separator") + "config.xml");
			if (file.exists()) {
				loadConfig();
			} else {
				try {
					File f = file.getParentFile();
					if (!f.exists())
						f.mkdirs();
					
					file.createNewFile();
				} catch (IOException | NullPointerException e) {
					LOG.log(Level.WARNING, "Не удалось создать файл с настройками по адресу:\n" + file.getAbsolutePath(), e);
					file = null;
				}
			}
		} catch (Exception e) {
			String msg = "Ошибка при инициализации настроек";
			LOG.log(Level.SEVERE, msg, e);
			JOptionPane.showMessageDialog(null, e.getMessage(), msg, JOptionPane.ERROR_MESSAGE);
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
	
	public static ConfigModel getInstance() {
		if (instance == null) {
			instance = new ConfigModel();
		}
		return instance;
	}

	public static void setInstance(ConfigModel instance) {
		ConfigModel.instance = instance;
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
	        	 eField.addContent(xmlElementFactory("role", field.getRole().name()));
	        	 
	        	 if (field.getRole() == FieldRole.DRAW && field.getColor() != null) {
	        		 String hexStr = Integer.toHexString(field.getColor().getRGB());
	        		 if (hexStr.length() == 7)
	        			 hexStr = "0" + hexStr;
	        		 eField.addContent(xmlElementFactory("color", hexStr));
	        	 }
	        	 
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
	        			 if (value == null || (value.source == null && value.value == null) || (value.caption == null && value.description == null))
	        				 return;
	        			 
	        			 Element item = new Element("item");
	        			 item.setAttribute("value", value.source == null ? value.value.toString() : value.source);
	        			 if (value.caption != null && !value.caption.isEmpty())
	        				item.setAttribute("caption", value.caption);
	        			 if (value.description != null && !value.description.isEmpty())
	        				item.setAttribute("description", value.description);
	        			 eValues.addContent(item);
	        		 });
	        		 if (!eValues.getChildren().isEmpty()) eField.addContent(eValues);
	        	 }
	        	 
	        	 eRoot.addContent(eField);
	         });
	         
	         XMLOutputter xmlOutput = new XMLOutputter(Format.getPrettyFormat().setEncoding("UTF-8"));
	         xmlOutput.output(doc, new OutputStreamWriter(new FileOutputStream(file), "UTF-8"));
	      }catch(IOException e){
	         e.printStackTrace();
	      }
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
	
	/** Remove bad links from the recent files list */
	public void removeMissingRecents() {
		boolean listChanged = false;
		for (int i = recentFiles.size() - 1; i >=0 ; i--) {
			String recent = recentFiles.get(i);
			if (!new File(recent).exists() && recentFiles.remove(recent))
				listChanged = true;
		}
		
		if (listChanged)
			firePropertyChanged(new PropertyEvent(this, "recent-file", null, PropertyEvent.DELETE));
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
				if (value == null)
					throw new IllegalArgumentException("Не предоставлено значения для опции " + property);
				switch (property) {
					case "name":
						setFieldName(field, value);
						break;
					case "description":
						setFieldDescription(field, value);
						break;
					case "datatype":
						DataType datatype = DataType.valueOf(value);
						if (datatype == null)
							throw new IllegalArgumentException("Неподдерживаемый тип данных");
						setFieldDatatype(field, datatype);
						break;
					case "delimiter":
						if (value.length() == 0)
							throw new IllegalArgumentException("Ограничитель не должен быть пустым");
						
						FieldDelimiter delimiter = FieldDelimiter.valueOf(value);
						setFieldDelimiter(field, delimiter);
						break;
					case "format":
						setFieldFormat(field, value);
						break;
					case "optional":
						boolean optional = parseBoolean(value);
						setFieldOptional(field, optional);
						break;
					case "bitmask":
						boolean bitmask = parseBoolean(value);
						setFieldBitmask(field, bitmask);
						break;
					case "hashsum":
						boolean hashsum = parseBoolean(value);
						setFieldHashsum(field, hashsum);
						break;
					case "role":
						FieldRole role = FieldRole.valueOf(value);
						if (role == null)
							throw new IllegalArgumentException("Неподдерживаемое состояние поля");
						setFieldRole(field, role);
						break;
					case "color":
						try {
							int l = value.length();
							if (l != 8 && l != 6)
								throw new Exception("В опции color ожидается hex-строка длинной 6(rrggbb) или 8(aarrggbb) символов без решётки; [a - alpha, r - red, g - green, b - blue] (Предоставлена строка '" + value + " [" + Integer.toString(l) + "]')");
							
							int r = Integer.parseInt(value.substring(l-6, l-4), 16);
							int g = Integer.parseInt(value.substring(l-4, l-2), 16);
							int b = Integer.parseInt(value.substring(l-2), 16);
							int a = 255;
							if (l == 8)
								a = Integer.parseInt(value.substring(0,2), 16);
							Color color = new Color(r,g,b,a);
							setFieldColor(field, color);
						} catch (NumberFormatException e) {
							setFieldColor(field, getNextColor());
							throw new Exception("Ошибка преобразования hex строки: " + e.getMessage(), e);
						} catch (Exception e) {
							setFieldColor(field, getNextColor());
							throw e;
						}
						break;
				}
			} catch (Exception e) {
				LOG.log(Level.WARNING, String.format("Ошибка при загрузке свойства <%s> поля [%s]. ", property, field.getName(), e.getMessage()), e);
			}
		}
		
		try {
			field.setParser(DefaultParser.getDefaultParser(field.getDatatype(), field.getFormat()));
		} catch (Exception e) {
			LOG.log(Level.WARNING, String.format("Ошибка при загрузке свойства поля [%s]. ", field.getName(), e.getMessage()), e);
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
					LOG.log(Level.WARNING, String.format("Ошибка при загрузке значения поля [%s]. ", field.getName(), e.getMessage()), e);
				}
			}
		
		try {
			validateField(field);
		} catch (Exception e) {
			e.printStackTrace();
			LOG.log(Level.WARNING, String.format("Поле %s не прошло валидацию: %s", field, e.getMessage()));
		}
		return field;
	}
	
	public List<Field> getFieldList() {
		return fieldList;
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
			LOG.log(Level.WARNING, "Не удалось создать новое поле", e);
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
			
			validateFieldName(field, name);
			
			field.setName(name);
			return true;
		} catch (Exception e) {
			LOG.log(Level.WARNING, "Не удалось изменить имя поля", e);
			return false;
		}
	}
	
	public void validateFieldName(Field field, String name) throws UniqueFieldException {
		for (Field f : getFieldList())
			if (!field.equals(f) && f.getName().equals(name)) {
				if (field.getRole() == FieldRole.DRAW && f.getRole() == FieldRole.DRAW) {
					throw new UniqueFieldException("Имена рисуемых полей должны быть уникальными.", field, f, name);
				} else
					LOG.log(Level.INFO, "Уже есть поле с именем " + name);
			}
	}

	public boolean setFieldDescription(Field field, String description) {
		try {
			if (field == null)
				throw new NullPointerException("Поле - NULL");
			
			field.setDescription(description);
			return true;
		} catch (Exception e) {
			LOG.log(Level.WARNING,"Не удалось изменить описание поля", e);
			return false;
		}
	}
	
	public boolean setFieldDatatype(Field field, DataType datatype) {
		try {
			if (field == null)
				throw new NullPointerException("Поле - NULL");
			
			DataType xDataType = field.getDatatype();
			
			field.setDatatype(datatype);
			
			if (!setFieldRole(field, field.getRole()) && // Trying to validate the field's role (checking if we can leave it unchanged)
					!setFieldRole(field, datatype == DataType.TIME_SEQUENCE ? FieldRole.X_AXIS : FieldRole.NONE)) /* ...then, trying to reset it */ { 
				// ... if unable to do anything, then rolling back
				field.setDatatype(xDataType);
				LOG.log(Level.WARNING, "Не удалось изменить тип данных поля из-за конфликта с ролью (см. предыдущие сообщения для подробностей)");
				return false;
			}
			return true;
		} catch (Exception e) {
			LOG.log(Level.WARNING, "Не удалось изменить тип данных поля", e);
			return false;
		}
	}

	public boolean setFieldDelimiter(Field field, FieldDelimiter delimiter) {
		try {
			if (field == null)
				throw new NullPointerException("Поле - NULL");
			
			field.setDelimiter(delimiter);
			return true;
		} catch (Exception e) {
			LOG.log(Level.WARNING, "Не удалось изменить тип данных поля", e);
			return false;
		}
	}
	
	public boolean setFieldFormat(Field field, ru.foxling.graphit.config.Format format) {
		try {
			if (field == null)
				throw new NullPointerException("Поле - NULL");
			
			field.setFormat(format);
			return true;
		} catch (Exception e) {
			LOG.log(Level.WARNING, "Не удалось изменить формат поля", e);
			return false;
		}
	}
	
	public boolean setFieldFormat(Field field, String format) {
		try {
			if (field == null)
				throw new NullPointerException("Поле - NULL");
			
			field.setFormat(format);
			return true;
		} catch (Exception e) {
			LOG.log(Level.WARNING, "Не удалось изменить формат поля", e);
			return false;
		}
	}
	
	public boolean setFieldOptional(Field field, boolean optional) {
		try {
			if (field == null)
				throw new NullPointerException("Поле - NULL");
			
			validateFieldOptional(field, optional);
			field.setOptional(optional);
			return true;
		} catch (UniqueFieldException e) {
			LOG.log(Level.WARNING, String.format("Не удалось изменить опциональность поля, т.к. в наборе полей уже есть необязательное поле - '%s'. Может быть только одно необязательное поле", e.getPrimalField()), e);
			return false;
		}
	}
	
	public void validateFieldOptional(Field field, boolean optional) throws UniqueFieldException {
		if (optional) {
			for (Field f : fieldList)
				if (!f.equals(field) && f.isOptional())
					throw new UniqueFieldException(field, f, "Необязательное");
		}
	}
	
	/** Sets the field's bitmask property
	 * @return <code>false</code> if encountered any exceptions during execution, <code>true</code> – otherwise */
	public boolean setFieldBitmask(Field field, boolean isBitmask) {
		try {
			if (field == null)
				throw new NullPointerException("Поле - NULL");

			field.setBitmask(isBitmask);
			return true;
		} catch (Exception e) {
			LOG.log(Level.WARNING, "Не удалось изменить свойство поля \"битовая маска\"", e);
			return false;
		}
	}
	
	public boolean setFieldHashsum(Field field, boolean hashsum) {
		try {
			if (field == null)
				throw new NullPointerException("Поле - NULL");
			
			validateFieldHashsum(field, hashsum);
			field.setHashsum(hashsum);
			return true;
		} catch (UniqueFieldException e) {
			LOG.log(Level.WARNING, String.format("Не удалось изменить свойство поля 'хэш-сумма', т.к. уже есть поле хранящее хэш-сумму - '%s'", e.getPrimalField()), e);
			return false;
		}
	}
	
	public void validateFieldHashsum(Field field, boolean hashsum) throws UniqueFieldException {
		if (hashsum)
			for (Field f : fieldList)
				if (!f.equals(field) && f.isHashsum())
					throw new UniqueFieldException(field, f, "Хэш-сумма");
	}
	
	public boolean setFieldColor(Field field, Color color) {
		try {
			if (field == null)
				throw new NullPointerException("Поле - NULL");
			
			field.setColor(color);
			return true;
		} catch (Exception e) {
			LOG.log(Level.WARNING, "Не удалось изменить цвет поля ", e);
			return false;
		}
	}
	
	/** @return Next free <i>(not used)</i> color */ 
	private Color getNextColor() {
		for (Color color : COLORS) {
			boolean inuse = false;
			for (Field field : fieldList) {
				if (field.getRole() == FieldRole.DRAW &&
						field.getColor() != null &&
						field.getColor().equals(color)) {
					inuse = true;
					break;
				}
			}
			if (!inuse)
				return color;
		}
		return getRandomColor();				
	}
	
	private Color getRandomColor() {
		Random rnd = new Random();
		return new Color(rnd.nextInt(255), rnd.nextInt(255), rnd.nextInt(255));
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
				throw new IllegalStateException(String.format("Поле \"%s\" является X-осью", field));
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
		try {
			if (field == null)
				throw new NullPointerException("Поле - NULL");
			
			validateFieldRole(field, role);
		} catch (UniqueFieldException e) {
			LOG.log(Level.WARNING, String.format("Не удалось изменить роль поля, т.к. в наборе полей уже есть поле отмеченное как ось X - \"%s\". Может быть только одна ось X.", e.getPrimalField()), e);
			return false;
		} catch (Exception e) {
			LOG.log(Level.WARNING, e.getMessage(), e);
			return false;
		}
		
		if (role == FieldRole.DRAW)
			field.setColor(getNextColor());
		
		field.setRole(role);
		return true;
	}
	
	/** @see {@link #role} */
	public void validateFieldRole(Field field, FieldRole role) throws IllegalStateException, UniqueFieldException {
		DataType type = field.getDatatype();  
		if (role == FieldRole.DRAW && type == DataType.STRING)
			throw new IllegalStateException("Строковые данные нельзя поместить на график");
		
		if (field.getDatatype() == DataType.TIME_SEQUENCE && role != FieldRole.X_AXIS)
			throw new IllegalStateException(String.format("Тип данных '%s' есть смысл использовать только для роли '%s'", DataType.TIME_SEQUENCE, FieldRole.X_AXIS));
		
		if (role == FieldRole.X_AXIS) {
			if (!Arrays.asList(DataType.DATE, DataType.TIME, DataType.DATETIME, DataType.TIME_SEQUENCE).contains(field.getDatatype()))
				throw new IllegalStateException("В качестве данных для оси X поддерживаются только временнЫе типы (date/time/datetime/time_sequence)");
		
			for (Field f : fieldList)
				if (!f.equals(field) && f.getRole() == FieldRole.X_AXIS)
					throw new UniqueFieldException(field, f, FieldRole.X_AXIS.toString());
		}
	}
	
	public void addFieldValueAt(Field field, Integer index, FieldValue value) {
		try {
			if (index == null) {
				field.addValue(value);
			} else
				field.addValueAt(index, value);
		} catch (Exception e) {
			LOG.log(Level.WARNING, "Не удалось добавить значение поля", e);
		}
		
	}
	
	public void removeFieldValue(Field field, int[] index) {
		if (index.length == 0)
			return;
		
		try {
			field.removeValues(index);
		} catch (Exception e) {
			LOG.log(Level.WARNING, "Не удалось удалить значение поля", e);
		}
	}
	
	public void validateField(Field field) throws UniqueFieldException, IllegalStateException {
		try {
			validateFieldName(field, field.getName());
			validateFieldHashsum(field, field.isHashsum());
			validateFieldOptional(field, field.isOptional());
			validateFieldRole(field, field.getRole());
			field.setValid(true);
		} catch(Exception e) {
			field.setValid(false);
			throw e;
		}
	}
	
	public boolean validateFieldList() {
		String msg = "";
		Field field = null;
		try {
			for (int i = 0; i < getFieldList().size(); i++) {
				field = getFieldList().get(i);
				validateField(field);
			}
			return true;
		} catch (UniqueFieldException e) {
			if (e.getMessage() != null) {
				msg = e.getMessage();
			} else
				msg = String.format("В наборе полей должно быть только одно поле со свойством \"%s\", но у поля [%s] это уже есть", e.getUniqueProperty(), e.getPrimalField());
		} catch (IllegalStateException e) {
			msg = e.getMessage();
		}
		if (field != null)
			LOG.log(Level.WARNING, "Ошибка при валидации поля \"" + field.toString() + "\": " + msg);
		return false;
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
	
	/** String to boolean converter
	 * @return <code>true</code> if <code><b>text</b></code> in ["true", "yes", "1"] */
	private boolean parseBoolean(String text) {
		return text != null && (text.equals("true") || text.equals("yes") || text.equals("1"));
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
	
	/*public ArrayList<Field> cloneFieldList(){
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
	}*/
}

