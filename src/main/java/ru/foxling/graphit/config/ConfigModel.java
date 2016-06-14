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
	 * -----=== FIELD MGMT ===----- 
	 */
	
	
	/* ---=== FIELD.NAME ===--- */

	/** Sets <code>name</code> of a <code>field</code>
	 * @return <code>true</code> if the property changed */
	public boolean setFieldName(Field field, String name) {
		if (field == null)
			throw new NullPointerException("Parameter 'field' is NULL");
		
		return field.setName(name);
	}
	
	
	/** Checks if a name fits a field
	 * @param field a field to check
	 * @param name a name to check
	 * @param trace do the issues' logging
	 * @returns validity */
	public boolean validateFieldName(Field field, String name, boolean trace) {
		if (field == null)
			throw new NullPointerException("Parameter 'field' is NULL");
		
		for (Field f : getFieldList()) {
			if (!field.equals(f) && f.getName().equals(name)) {
				if (field.getRole() == FieldRole.DRAW && f.getRole() == FieldRole.X_AXIS) {
					if (trace) {
						LOG.log(Level.WARNING, "Имена рисуемых полей должны быть уникальными.");
					}
					return false;
				} else
					if (trace) {
						LOG.log(Level.INFO, "Уже есть поле с именем " + name);
					}
			}
		}
		return true;
	}
	
	
	/* ---=== FIELD.DESCRIPTION ===---*/
	
	/** Sets <code>description</code> of a <code>field</code>
	 * @return <code>true</code> if the property changed */
	public boolean setFieldDescription(Field field, String description) {
		if (field == null)
			throw new NullPointerException("Parameter 'field' is NULL");
		
		return field.setDescription(description);
	}
	
	
	/* ---=== FIELD.DATATYPE ===--- */
	
	/** Sets <code>datatype</code> of a <code>field</code>
	 * @return <code>true</code> if the property changed */
	public boolean setFieldDatatype(Field field, DataType datatype) {
		if (field == null)
			throw new NullPointerException("Parameter 'field' is NULL");
		
		if (datatype == null)
			throw new NullPointerException("Parameter 'datatype' is NULL");
			
		return field.setDatatype(datatype);
	}
	
	
	/* ---=== FIELD.FORMAT ===--- */
	
	public boolean setFieldFormat(Field field, ru.foxling.graphit.config.Format format) {
		if (field == null) {
			throw new NullPointerException("Parameter 'field' is NULL");
		}
		
		return field.setFormat(format);
	}
	
	public boolean setFieldFormat(Field field, String format) {
		if (field == null) {
			throw new NullPointerException("Parameter 'field' is NULL");
		}
		
		return field.setFormat(format);
	}
	
	
	/* ---=== FIELD.DELIMITER ===--- */
	
	public boolean setFieldDelimiter(Field field, FieldDelimiter delimiter) {
		if (field == null) {
			throw new NullPointerException("Parameter 'field' is NULL");
		}
			
		return field.setDelimiter(delimiter);
	}
	
	
	/* ---=== FIELD.OPTIONAL ===--- */
	
	/** Sets parameter <code>'optional'</code> of a <code>field</code>
	 * @returns true if value has changed*/
	public boolean setFieldOptional(Field field, boolean optional) {
		if (field == null) {
			throw new NullPointerException("Parameter 'field' is NULL");
		}
		
		if (validateFieldOptional(field, optional, true)) {
			return field.setOptional(optional);
		}
		return false;
	}
	
	/** Checks if a field could be the optional field
	 * @param field a field to check
	 * @param optional optional flag to check
	 * @param trace do the issues' logging
	 * @returns validity */
	public boolean validateFieldOptional(Field field, boolean optional, boolean trace) {
		if (optional) {
			for (Field f : fieldList) {
				if (!f.equals(field) && f.isOptional()) {
					if (trace) {
						LOG.log(Level.WARNING, String.format("В наборе полей уже есть необязательное поле - '%s'. Может быть только одно необязательное поле", f));
					}
					return false;
				}
			}
		}
		return true;
	}
	
	
	/* ---=== FIELD.BITMASK ===--- */
	
	/** Sets the field's bitmask property
	 * @return <code>true</code> if value has changed */
	public boolean setFieldBitmask(Field field, boolean isBitmask) {
		if (field == null) {
			throw new NullPointerException("Parameter 'field' is NULL");
		}

		return field.setBitmask(isBitmask);
	}
	
	
	/* ---=== FIELD.HASHSUM ===--- */
	
	/** Sets the field's hashsum property
	 * @return <code>true</code> if value has changed */
	public boolean setFieldHashsum(Field field, boolean hashsum) {
		if (field == null)
			throw new NullPointerException("Parameter 'field' is NULL");
		
		validateFieldHashsum(field, hashsum, true);
		field.setHashsum(hashsum);
		return true;
	}
	
	/** Checks if a <code>field</code> could be the hashsum-field
	 * @param field a field to check
	 * @param hashsum a hashsum flag to check
	 * @param trace do the issues' logging
	 * @returns validity */
	public boolean validateFieldHashsum(Field field, boolean hashsum, boolean trace) {
		if (hashsum) {
			for (Field f : fieldList) {
				if (!f.equals(field) && f.isHashsum()) {
					if (trace) {
						LOG.log(Level.WARNING, String.format("В наборе полей уже есть необязательное поле - '%s'. Может быть только одно необязательное поле", f));
					}
					return false;
				}
			}
		}
		return true;
	}
	
	
	/* ---=== FIELD.ROLE ===--- */
	
	/** Sets the field's role property
	 * @return <code>true</code> if value has changed */
	public boolean setFieldRole(Field field, FieldRole role) {
		if (validateFieldRole(field, role, true) && field.setRole(role)) {
			if (role == FieldRole.DRAW) {
				field.setColor(getNextColor());
			}
			return true;
		} else
			return false;
	}
	
	/** Checks if a <code>field</code> could have a <code>role</code>
	 * @param field a field to check
	 * @param role a role to check
	 * @param trace do the issues' logging
	 * @returns validity */
	public boolean validateFieldRole(Field field, FieldRole role, boolean trace) {
		if (role == FieldRole.X_AXIS) {
			for (Field f : fieldList) {
				if (!f.equals(field) && f.getRole() == FieldRole.X_AXIS) {
					if (trace) {
						LOG.log(Level.WARNING, String.format("В наборе полей уже есть поле отмеченное как ось X - \"%s\". Может быть только одна ось X.", f));
					}
					return false;
				}
			}
		}
		return true;
	}

	/** Toggles visibility of a field on the plot */
	public boolean toggleYAxis(Field field, boolean visible) {
		if (field == null) {
			throw new NullPointerException("Parameter 'field' is NULL");
		}
		
		FieldRole role = field.getRole();
		if (visible && role == FieldRole.X_AXIS) {
			throw new IllegalStateException(String.format("Поле \"%s\" является X-осью", field));
		}
		
		if (visible && role == FieldRole.DRAW ||
				!visible && role == FieldRole.NONE) {
					return false;
		}
		
		if (visible && role == FieldRole.NONE) {
			return setFieldRole(field, FieldRole.DRAW);
		} else {
			if (!visible && role == FieldRole.DRAW) {
				return setFieldRole(field, FieldRole.NONE);
			}
		}
		
		return false;
	}
	
	
	/* ---=== FIELD.COLOR ===--- */
	
	/** Sets the field's color property
	 * @return <code>true</code> if value has changed */
	public boolean setFieldColor(Field field, Color color) {
		if (field == null) {
			throw new NullPointerException("Parameter 'field' is NULL");
		}
		
		return field.setColor(color);
	}
	
	/** @return Next free <i>(not used by any fields in fieldList)</i> color */ 
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
			if (!inuse) {
				return color;
			}
		}
		return getRandomColor();				
	}
	
	private Color getRandomColor() {
		Random rnd = new Random();
		return new Color(rnd.nextInt(255), rnd.nextInt(255), rnd.nextInt(255));
	}


	/* ---=== FIELD VALUES ===--- */
	
	public void addFieldValueAt(Field field, Integer index, FieldValue value) {
		try {
			if (index == null) {
				field.addValue(value);
			} else {
				field.addValueAt(index, value);
			}
		} catch (Exception e) {
			LOG.log(Level.WARNING, "Не удалось добавить значение поля", e);
		}
		
	}
	
	public void removeFieldValue(Field field, int[] index) {
		if (index.length == 0) {
			return;
		}
		
		try {
			field.removeValues(index);
		} catch (Exception e) {
			LOG.log(Level.WARNING, "Не удалось удалить значение поля", e);
		}
	}
	
	public boolean validateField(Field field) throws UniqueFieldException, IllegalStateException {
		// TODO: return -> boolean
		field.setValid(validateFieldName(field, field.getName(), true) &&
				validateFieldHashsum(field, field.isHashsum(), true) &&
				validateFieldOptional(field, field.isOptional(), true) &&
				field.validateRole(field.getRole(), true) && validateFieldRole(field, field.getRole(), true));
		
		return field.isValid();
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

