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

package ru.foxling.graphit.logfile;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import ru.foxling.graphit.Core;
import ru.foxling.graphit.config.Field;
import ru.foxling.graphit.config.FieldDelimiter;


/** Log-file parsing class */
public class LogFile {
	private static final Logger LOG = Logger.getLogger(LogFile.class.getName());
	
	private String filename;
	private String intFilename;
	private String serialNo;
	private String frimware;
	private int counter;	
	private int linesCount;
	private ArrayList<Startup> startups = new ArrayList<Startup>();

	private int wStartupCount = 0;
	private Path filePath;
	private final Charset encoding = Charset.forName("Cp1251");
	private ArrayList<Field> fieldList;
	private int optionalFieldId = -1;
	private int hashsumFieldId = -1;
	
	/** Records index */
	private ArrayList<Record> records;
	
	public LogFile(String filename) {
		this.filename = filename;
		this.filePath = Paths.get(filename);
		this.fieldList = Core.getConfigModel().cloneFieldList();
		records = new ArrayList<Record>(25);
		for (int i = 0; i < fieldList.size(); i++) {
			Field field = fieldList.get(i);
			if (field.isOptional()) optionalFieldId = i;
			if (field.isHashsum()) {
				hashsumFieldId = i;
			}
		}
		readHeader();
	}
	
	public String getFileName() {
		return filename;
	}

	public String getIntFileName() {
		return intFilename;
	}

	public String getFrimware() {
		return frimware;
	}

	public int getCounter() {
		return counter;
	}

	public String getSerialNo() {
		return serialNo;
	}

	public ArrayList<Field> getFieldList() {
		return fieldList;
	}

	public ArrayList<Startup> getStartups() {
		return startups;
	}

	public int getWorkingStartupCount(){
		return wStartupCount;
	}
	
	public int getLinesCount() {
		return linesCount;
	}
	
	public ArrayList<Record> getRecords(){
		return records;
	}

	/** Parses log-file header */
	public void readHeader() {
		try (BufferedReader reader = Files.newBufferedReader(filePath, encoding)){			
			String str;
			while ((str = reader.readLine()) != null) {
				if (str.isEmpty()) break;
				
				if (str.contains("file")) {
					int i = str.indexOf(":");
					if (i + 1 < str.length())
						intFilename = str.substring(i + 1).trim();
				} else
					if (str.contains("serial no.")) {
						int i = str.indexOf(":");
						int j = str.indexOf("firmware ver.:");
						if (i + 1 < str.length())
							if (j == -1) {
								serialNo = str.substring(i + 1).trim();
							} else
								serialNo = str.substring(i + 1, j).trim();
							
						if (j != -1 || j + 14 > str.length())
							frimware = str.substring(j + 14).trim();
					} else
						if (str.contains("counter")) {
							int i = str.indexOf("#");
							if (i + 1 < str.length())
								counter = Integer.parseInt(str.substring(i + 1).trim());
						}
			}
		} catch (IOException e) {			
			e.printStackTrace();
		}
	}

	/** Parses log-file's startups and their lines */
	public void readFile() throws IOException {
		records.clear();
		try (BufferedReader reader = Files.newBufferedReader(filePath, encoding)){
			String line;
			
			int lineNo = 0;
			int i;
			
			String date = "";
			String time = "";
			
			Startup startup = null;
			while ((line = reader.readLine()) != null) {
				lineNo++;
				if (!line.isEmpty()){
					if (line.charAt(0) == '@') {
						//Making startup object
						if (line.equals("@ startup")) {
							startup = new Startup(lineNo); 
							startups.add(startup);
						} else
							// Parsing startup DateTime
							if (startup != null) {
								if (line.contains("system date")) {
									i = line.indexOf('#');
									if (i > -1 && line.length() > i) {
										try {
											date = line.substring(i + 1);
											startup.setDate(LocalDate.parse(date, Core.F_DATE));
										} catch (Exception e) {
											e.printStackTrace();
										}
									}
								} else
									if (line.contains("system time")) {
										i = line.indexOf('#');
										if (i > -1 && line.length() > i) {
											try {
												time = line.substring(i + 1);
												startup.setTime(LocalTime.parse(time, Core.F_TIME));
												if (startup.getDate() != null)
													startup.setDatetime(LocalDateTime.of(startup.getDate(), startup.getTime()));
											} catch (Exception e) {
												e.printStackTrace();
											}
										}
									}
							}
					} else
						//Parsing data line
						if (startup != null) {
							Record rec = new Record(lineNo, line, fieldList.size());
							try {
								parse(rec);
								records.add(rec);
							} catch (ParseExceptionEx e) {
								rec.setParseError(e);
								LOG.log(Level.WARNING, e.getMessage(), e);
							}
							startup.addLine(rec);
						}
				}
			}
			linesCount = lineNo;
			
			//Working startups count:
			for (i = 0; i < startups.size(); i++)
				if (startups.get(i).getRecordset().size() > 0) wStartupCount++;
		}
	}
	
	private void parse(Record rec) throws ParseExceptionEx {
		/** Source string without hash sum */
		String	valueableStr = "";
		
		int fieldsCount = fieldList.size();
		
		/** The line's fields (string parts) */
		ArrayList<String> parts = new ArrayList<String>(fieldsCount);
		
		/** The fields' offsets */
		ArrayList<Integer> offsets = new ArrayList<Integer>(fieldsCount);
		
		// Gotta split the entire string on parts, before parsing,
		// otherwise there is a case when we won't know if it is
		// a value of the optional field or next one's
		int fid = -1;
		int offset = 0;
		
		String str = rec.getSourceStr();
		for (fid = 0; fid < fieldsCount; fid++) {
			Field field = fieldList.get(fid);
			FieldDelimiter delimiter = field.getDelimiter();
			if (fid != fieldsCount - 1) {
				int pos = str.indexOf(delimiter.getValue());
				if (pos == -1)
					if (field.isOptional()) {
						continue;
					} else
						throw new ParseExceptionEx(String.format("Ошибка при попытке разделить строку лог файла на отдельные поля: поле %s отсутствует (не найден разделитель \"%s\")", field, delimiter.getValue()), offset, rec.getSourceStr().length() - offset, fid);
				String part = str.substring(0, pos);
				parts.add(part);
				pos += delimiter.getLength();
				offset += pos;
				str = str.substring(pos);
			} else
				parts.add(str);
			
			offsets.add(offset);
			
			// save string without the hash sum field for later verification 
			if (hashsumFieldId == fid) {
				if (fid != 0)
					valueableStr = rec.getSourceStr().substring(0, offset);
				if (fid != fieldsCount - 1)
					valueableStr += rec.getSourceStr().substring(offset);
			}
			field = null;
		}
		
		// Check if actual parts count equals expected fields count
		if (parts.size() != fieldsCount) {
			if (optionalFieldId == -1) {
				throw new ParseExceptionEx(String.format("Несоответствие количеств ожидаемого (%d) и фактического полей (%d)", fieldsCount, parts.size()), 0, rec.getSourceStr().length());
			} else
				if (parts.size() == fieldsCount - 1) {
					parts.add(optionalFieldId, null); // If we missing only optional field value, then insert NULL at its position
					offsets.add(optionalFieldId, -1); // ... don't forget about offsets
				} else
					throw new ParseExceptionEx(String.format("Несоответствие количеств ожидаемого (%d-%d) и фактического полей (%d)", fieldsCount - 1, fieldsCount, parts.size()), 0, rec.getSourceStr().length());
		}
			
		// Parsing string parts --> fields' values
		offset = 0;
		for (fid = 0; fid < fieldsCount; fid++) {
			String part = parts.get(fid);
			if (fid == optionalFieldId && part == null)
				continue;
			Field field = fieldList.get(fid);
			try {
				Object value = field.getParser().parse(part);
				rec.setValue(fid, value);
			} catch (Exception e) {
				throw new ParseExceptionEx(String.format("Ошибка преобразования строки \"%s\" в тип %s", part, field.getDatatype().getValue()), offset, part.length(), fid);
			}
			offset = offsets.get(fid);
		}
		
		// Calculating hash sum of the string
		if (hashsumFieldId > -1)
			rec.setAuthentic(parts.get(hashsumFieldId).equals(getCRC(valueableStr)));
	}
	
	private final static short[] KEY = {0x0001, 0x0002, 0x0004
		, 0x0008, 0x0010, 0x0020
		, 0x0040, 0x0080, 0x0100};
	
	private final static String[] KEY_DESC = {"Запись", "Спуск", "RS-485"
					, "Подъем", "Минус", "Стоп"
					, "Ввод", "Режим", "Плюс"};
	
	public String getKeyTooltip(String value) {
		if (value == null || value == "")
			return null;
		
		String result = "";
		try {
			short val = Short.parseShort(value, 16);
			result = getKeyTooltip(val);
		} catch (NumberFormatException e) {
			result = "Ошибка: не удалось определить 16-ричное число из строки " + value;
		}
		return result;
	}
	
	public String getKeyTooltip(short value) {
		if (value < 1)
			return "";
		
		String result = "<html>";
		for (int i = 0; i < KEY.length; i++) {
			if ((value & KEY[i]) >> i == 1)
				result += "0x"+Integer.toHexString(KEY[i])+": "+ KEY_DESC[i]+"<br>";
		}
		
		return result+"</html>";
	}
	
	
	private final static short[] ERROR = {0x0001, 0x0002, 0x0004, 0x0008
		, 0x0010, 0x0020
		, 0x0040, 0x0080 // Описаний для этих двух нет – добавил для удобства
		, 0x0100, 0x0200
		, 0x0400, 0x0800};

	private final static String[] ERROR_DESC = {"Восстановление после автоматического сброса по \"зависанию\""
				,"Недостаточно места на карте памяти"
				,"Ошибка карты памяти SD"
				,"Ошибка файловой системы на карте памяти SD"
				,"Ошибка тензоизмерительного модуля (датчик натяжения)"
				,"Обрыв внешней цепи датчика натяжения"
				,"unknown","unknown"
				,"Обнаружены много кратные нажатия одной клавиши"
				,"Обнаружено длительное удержание одной клавиши, возможно \"залипание\""
				,"Коэффициенты подстройки датчика натяжения повреждены, использованы значения по умолчанию"
				,"Обрыв внешней цепи датчика натяжения"};
	
	public String getErrorTooltip(String value) {
		if (value == null || value == "")
			return null;
		
		String result = "";
		try {
			short val = Short.parseShort(value, 16);
			result = getErrorTooltip(val);
		} catch (NumberFormatException e) {
			result = "Ошибка: не удалось определить 16-ричное число из строки " + value;
		}
		return result;
	}
	
	public String getErrorTooltip(short value) {
		if (value < 1)
			return "";
		
		String result = "<html>";
		for (int i = 0; i < ERROR.length; i++) {
			if ((value & ERROR[i]) >> i == 1)
				result += "0x"+Integer.toHexString(ERROR[i]) + ": " + ERROR_DESC[i] + "<br>";
		}
		
		return result+"</html>";
	}
	
	// ===== CRC Section ===========================================================================
	private final static short[] auchCRCHi = {
		0x00, 0xC1, 0x81, 0x40, 0x01, 0xC0, 0x80, 0x41, 0x01, 0xC0, 
		0x80, 0x41, 0x00, 0xC1, 0x81, 0x40, 0x01, 0xC0, 0x80, 0x41, 
		0x00, 0xC1, 0x81, 0x40, 0x00, 0xC1, 0x81, 0x40, 0x01, 0xC0, 
		0x80, 0x41, 0x01, 0xC0, 0x80, 0x41, 0x00, 0xC1, 0x81, 0x40, 
		0x00, 0xC1, 0x81, 0x40, 0x01, 0xC0, 0x80, 0x41, 0x00, 0xC1, 
		0x81, 0x40, 0x01, 0xC0, 0x80, 0x41, 0x01, 0xC0, 0x80, 0x41, 
		0x00, 0xC1, 0x81, 0x40, 0x01, 0xC0, 0x80, 0x41, 0x00, 0xC1, 
		0x81, 0x40, 0x00, 0xC1, 0x81, 0x40, 0x01, 0xC0, 0x80, 0x41, 
		0x00, 0xC1, 0x81, 0x40, 0x01, 0xC0, 0x80, 0x41, 0x01, 0xC0, 
		0x80, 0x41, 0x00, 0xC1, 0x81, 0x40, 0x00, 0xC1, 0x81, 0x40, 
		0x01, 0xC0, 0x80, 0x41, 0x01, 0xC0, 0x80, 0x41, 0x00, 0xC1, 
		0x81, 0x40, 0x01, 0xC0, 0x80, 0x41, 0x00, 0xC1, 0x81, 0x40, 
		0x00, 0xC1, 0x81, 0x40, 0x01, 0xC0, 0x80, 0x41, 0x01, 0xC0, 
		0x80, 0x41, 0x00, 0xC1, 0x81, 0x40, 0x00, 0xC1, 0x81, 0x40, 
		0x01, 0xC0, 0x80, 0x41, 0x00, 0xC1, 0x81, 0x40, 0x01, 0xC0, 
		0x80, 0x41, 0x01, 0xC0, 0x80, 0x41, 0x00, 0xC1, 0x81, 0x40, 
		0x00, 0xC1, 0x81, 0x40, 0x01, 0xC0, 0x80, 0x41, 0x01, 0xC0, 
		0x80, 0x41, 0x00, 0xC1, 0x81, 0x40, 0x01, 0xC0, 0x80, 0x41, 
		0x00, 0xC1, 0x81, 0x40, 0x00, 0xC1, 0x81, 0x40, 0x01, 0xC0, 
		0x80, 0x41, 0x00, 0xC1, 0x81, 0x40, 0x01, 0xC0, 0x80, 0x41, 
		0x01, 0xC0, 0x80, 0x41, 0x00, 0xC1, 0x81, 0x40, 0x01, 0xC0, 
		0x80, 0x41, 0x00, 0xC1, 0x81, 0x40, 0x00, 0xC1, 0x81, 0x40, 
		0x01, 0xC0, 0x80, 0x41, 0x01, 0xC0, 0x80, 0x41, 0x00, 0xC1, 
		0x81, 0x40, 0x00, 0xC1, 0x81, 0x40, 0x01, 0xC0, 0x80, 0x41, 
		0x00, 0xC1, 0x81, 0x40, 0x01, 0xC0, 0x80, 0x41, 0x01, 0xC0, 
		0x80, 0x41, 0x00, 0xC1, 0x81, 0x40
		};

	private final static short[] auchCRCLo = {
		0x00, 0xC0, 0xC1, 0x01, 0xC3, 0x03, 0x02, 0xC2, 0xC6, 0x06, 
		0x07, 0xC7, 0x05, 0xC5, 0xC4, 0x04, 0xCC, 0x0C, 0x0D, 0xCD, 
		0x0F, 0xCF, 0xCE, 0x0E, 0x0A, 0xCA, 0xCB, 0x0B, 0xC9, 0x09, 
		0x08, 0xC8, 0xD8, 0x18, 0x19, 0xD9, 0x1B, 0xDB, 0xDA, 0x1A, 
		0x1E, 0xDE, 0xDF, 0x1F, 0xDD, 0x1D, 0x1C, 0xDC, 0x14, 0xD4, 
		0xD5, 0x15, 0xD7, 0x17, 0x16, 0xD6, 0xD2, 0x12, 0x13, 0xD3, 
		0x11, 0xD1, 0xD0, 0x10, 0xF0, 0x30, 0x31, 0xF1, 0x33, 0xF3, 
		0xF2, 0x32, 0x36, 0xF6, 0xF7, 0x37, 0xF5, 0x35, 0x34, 0xF4, 
		0x3C, 0xFC, 0xFD, 0x3D, 0xFF, 0x3F, 0x3E, 0xFE, 0xFA, 0x3A, 
		0x3B, 0xFB, 0x39, 0xF9, 0xF8, 0x38, 0x28, 0xE8, 0xE9, 0x29, 
		0xEB, 0x2B, 0x2A, 0xEA, 0xEE, 0x2E, 0x2F, 0xEF, 0x2D, 0xED, 
		0xEC, 0x2C, 0xE4, 0x24, 0x25, 0xE5, 0x27, 0xE7, 0xE6, 0x26, 
		0x22, 0xE2, 0xE3, 0x23, 0xE1, 0x21, 0x20, 0xE0, 0xA0, 0x60, 
		0x61, 0xA1, 0x63, 0xA3, 0xA2, 0x62, 0x66, 0xA6, 0xA7, 0x67, 
		0xA5, 0x65, 0x64, 0xA4, 0x6C, 0xAC, 0xAD, 0x6D, 0xAF, 0x6F, 
		0x6E, 0xAE, 0xAA, 0x6A, 0x6B, 0xAB, 0x69, 0xA9, 0xA8, 0x68, 
		0x78, 0xB8, 0xB9, 0x79, 0xBB, 0x7B, 0x7A, 0xBA, 0xBE, 0x7E, 
		0x7F, 0xBF, 0x7D, 0xBD, 0xBC, 0x7C, 0xB4, 0x74, 0x75, 0xB5, 
		0x77, 0xB7, 0xB6, 0x76, 0x72, 0xB2, 0xB3, 0x73, 0xB1, 0x71, 
		0x70, 0xB0, 0x50, 0x90, 0x91, 0x51, 0x93, 0x53, 0x52, 0x92, 
		0x96, 0x56, 0x57, 0x97, 0x55, 0x95, 0x94, 0x54, 0x9C, 0x5C, 
		0x5D, 0x9D, 0x5F, 0x9F, 0x9E, 0x5E, 0x5A, 0x9A, 0x9B, 0x5B, 
		0x99, 0x59, 0x58, 0x98, 0x88, 0x48, 0x49, 0x89, 0x4B, 0x8B, 
		0x8A, 0x4A, 0x4E, 0x8E, 0x8F, 0x4F, 0x8D, 0x4D, 0x4C, 0x8C, 
		0x44, 0x84, 0x85, 0x45, 0x87, 0x47, 0x46, 0x86, 0x82, 0x42, 
		0x43, 0x83, 0x41, 0x81, 0x80, 0x40
		};
	
	/** Counts CRC out of string */
	private static String getCRC(String line) {
		byte[] array = line.getBytes();
		
		short uchCRCHi = 0xFF ; // high CRC byte initialized 
		short uchCRCLo = 0xFF ; // low CRC byte initialized 
		short uIndex ; 			// will index into CRC lookup

		for (int i = 0; i < array.length; i++) {
			uIndex = (short) (uchCRCHi ^ array[i]);	// calculate the CRC
			uchCRCHi = (short) (uchCRCLo ^ auchCRCHi[uIndex]) ;
			uchCRCLo = auchCRCLo[uIndex] ;
		}
		
		return Integer.toString((uchCRCLo << 8 | uchCRCHi),16).toUpperCase();
	}
}
