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

package com.foxling.graphit.logfile;

public class BadLine {
	public int lineNumber = -1;
	public String line = "";
	public String errorMsg = "";
	/** Порядковый номер аргумента в котором возникла ошибка */
	public byte errorID = -1; 
	/** Позиция в строке */
	public int errorPos = -1;
	/** Длинна строкового значения аргумента */
	public int errorLen = -1;
}
