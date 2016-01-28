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

public enum FieldDelimiter {
	CRLF("{CR}{LF}", "\n\r"),
	CR("{CR}", "\n"),
	LF("{LF}", "\r"),
	SEMICOLON("Точка с запятой {;}", ";"),
	COLON("Двоеточие {:}", ":"),
	COMMA("Запятая {,}", ","),
	TAB("Табуляция {t}", "\t"),
	VBAR("Вертикальная черта {|}", "|"),
	SEMICOLON_SPACE(";{SPACE}", "; ");
	
	private final String caption;
	private final String value;
	private final int length;
	
	private FieldDelimiter(String caption, String value) {
		this.caption = caption;
		this.value = value;
		this.length = value.length();
	}

	public String getCaption() {
		return caption;
	}

	public String getValue() {
		return value;
	}
	
	public int getLength() {
		return length;
	}

	public static FieldDelimiter getDefaultFieldDelimiter() {
		return FieldDelimiter.SEMICOLON_SPACE;
	}

	public static FieldDelimiter getDefaultLineDelimiter() {
		return FieldDelimiter.CRLF;
	}
	
	public String toString(){
		return caption;
	}
}
