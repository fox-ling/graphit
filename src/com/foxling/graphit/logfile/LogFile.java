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

package com.foxling.graphit.logfile;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import com.foxling.graphit.Core;


/** Log-file parsing class */
public class LogFile {
	private String fileName;
	private String intFileName;
	private String serialNo;
	private String frimware;
	private int counter;	
	private int linesCount;
	private ArrayList<Startup> startups = new ArrayList<Startup>();
	
	private int wStartupCount = 0;
	private Path filePath;
	private Charset encoding = Charset.forName("Cp1251");
	
	public LogFile(String aFilename) {
		fileName = aFilename;
		filePath = Paths.get(aFilename);
		readHeader();
	}
	
	public int getId(Date arg0){
		int x = -1;
		for (int i = 0; i < startups.size(); i++) {
			Startup currStartup = startups.get(i);
			for (int j = 0; j < currStartup.lines.size(); j++) {
				Line currLine = currStartup.lines.get(j);
				x++;
				if (currLine.time.equals(arg0))
					return x;
			}
		}
		return x;
	}
	
	public int getWorkingStartupCount(){
		return wStartupCount;
	}
	
	/** Parses log-file header */
	public void readHeader() {
		try (BufferedReader reader = Files.newBufferedReader(filePath, encoding)){			
			Integer i;
			String s;
			
			while ((s=reader.readLine()) != null) {
				if (s.isEmpty()) break;
				
				if (s.contains("file")) {
					i = s.indexOf(":");
					if (i+1 < s.length()) {
						s = s.substring(i+1);						
						this.intFileName=s.trim();						
					}
				} else
					if (s.contains("serial no.")) {
						i = s.indexOf(":");
						int j = s.indexOf("firmware ver.:");
						if (i + 1 < s.length()) {
							if (j == -1) {
								this.serialNo=s.substring(i+1);
							} else
								this.serialNo=s.substring(i+1, j);
							this.serialNo=this.serialNo.trim();						
						}
						if (j != -1 || j + 14 > s.length()) {
							s = s.substring(j+14);
							this.frimware = s.trim();
						}
					} else
						if (s.contains("counter")) {
							i = s.indexOf("#");
							if (i + 1 < s.length()) {
								s = s.substring(i+1);							
								this.counter=Integer.parseInt(s.trim());							
							}
						}
			}
		} catch (IOException e) {			
			e.printStackTrace();
		}
	}

	/** Parses log-file's startups and their lines */
	public void readFile() throws IOException {
		try (BufferedReader reader = Files.newBufferedReader(filePath, encoding)){
			String line;
			
			int lineNo = 0;
			int i;
			int j = -1;
			
			String date = "";
			String time = "";
			
			Startup startup = null;
			while ((line = reader.readLine()) != null) {
				lineNo+=1;
				if (!line.isEmpty()){
					if (line.charAt(0)=='@') {
						//Making startup object
						if (line.equals("@ startup")) {
							j+=1;
							startup = new Startup(lineNo); 
							startups.add(startup);
						} else
							// Parsing startup DateTime
							if (startup != null) {
								if (line.contains("system date")) {
									i=line.indexOf('#');
									if (i>-1 && line.length()>i) {
										try {
											date = line.substring(i+1);
											startup.date = Core.F_DATE.parse(date);
										} catch (Exception e) {
											e.printStackTrace();
										}
									}
								} else
									if (line.contains("system time")) {
										i=line.indexOf('#');
										if (i>-1 && line.length()>i) {
											try {
												time = line.substring(i+1);
												startup.time = Core.F_TIME.parse(time);
												if (startup.date != null)
													startup.datetime = Core.F_DATETIME.parse(date+" "+time);
											} catch (Exception e) {
												e.printStackTrace();
											}
										}
									}
							}
					} else
						//Parsing data line
						if (startup != null)
						try {
							startup.lines.add(new Line(lineNo, line, startup.badLines));
						} catch (IllegalArgumentException e) {
							e.printStackTrace();
						}
				}
			}
			this.linesCount=lineNo;
			
			//Count working startups:
			for (i = 0; i < startups.size(); i++) {
				if (startups.get(i).lines.size()>0) wStartupCount++;
			} 
		}
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
	
	
	
	private void parse() {
		/*
		int l = aLine.length();
		String[] value = null;
		try{
			if (l < 6) {
				fid = -128;
				throw new IllegalArgumentException("Line is too short, there is no way it could hold all the fields");
			}
			
			value = aLine.split("; ");
			int l1 = value.length; 
			if (l1 != 10 & l1 != 11 ) {
				fid = -127;
				throw new IllegalArgumentException("Fields count should be either 10 or 11");
			}
		
			fid = 0; this.time =		fTime.parse(value[0]);
			fid = 1; this.depth =		Integer.parseInt(value[1]);
			fid = 2; this.tension =		Float.parseFloat(value[2]);
			fid = 3; this.topPos =		Byte.parseByte(value[3]);
			fid = 4; this.voltage =		Short.parseShort(value[4]);
					 this.knob_state =	value[5];
			fid = 6; this.speed =		Byte.parseByte(value[6]);
			fid = 7; this.mode =		Byte.parseByte(value[7]);
			fid = 8; this.stroke =		value[8].charAt(0);
			
			if (l1 == 11){
				this.error =	value[9];
				this.hash =		value[10];
			} else {
				this.hash =		value[9];
			}
			
			this.authentic = (this.hash.equals(getCRC(aLine.substring(0, l-4))));
			if (!this.authentic) {
				BadLine badLine = new BadLine();
				badLine.lineID = lineID; 
				badLine.line = aLine;
				badLine.errorMsg = "Wrong hash";
				badLine.errorPos = aLine.lastIndexOf(DIVISOR)+DIVISOR_LEN;
				badLine.errorLen = 4;
				badLine.errorID = 9;
				garbage.add(badLine);
			}
		} catch (Exception e) {
			BadLine badLine = new BadLine();
			badLine.lineID = lineID; 
			badLine.line = aLine;
			badLine.errorMsg = e.getMessage();
			badLine.errorID = fid;
			
			if (fid < 0) {
				badLine.errorPos = 0;
				badLine.errorLen = aLine.length();
			} else {
				badLine.errorPos = fid * DIVISOR_LEN;
				for (int i = 0; i < fid; i++) {
					badLine.errorPos += value[i].length();
				}
				badLine.errorLen = value[fid].length();
			}	
	
			if (e.getClass().getName().equals("java.lang.NumberFormatException")) 
				badLine.errorMsg = "Unknown number format " + badLine.errorMsg;
	
			garbage.add(badLine);
			throw new IllegalArgumentException(e.getMessage());
		}*/
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
