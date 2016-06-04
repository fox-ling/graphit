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

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.logging.Level;
import java.util.logging.Logger;

import gnu.trove.list.array.TIntArrayList;

public class BadDataIndex {
	private static Logger LOG = Logger.getLogger(BadDataIndex.class.getName());
	
	private Charset charset;
	
	/** Log File's line no index */
	private TIntArrayList ixLogFile; 
	
	/** Source strings */
	private TStringList sourceData;
	
	/** Issue index, stores {@link #parsedData}/{@link #badRecords} indexes <br>
	 * <u>1-based index</u>*, where values > 0 are {@link #parsedData} indexes and values < 0 are {@link #badRecords} indexes <br><br>
	 * <i>*Note that collections themselves have zero-bases indexing. I've made <code>[value]</code> 1-based just to fit two indexes in one space</i> 
	 */
	private TIntArrayList ixIssue;

	public BadDataIndex(Charset charset) {
		this.charset = charset;
		ixLogFile = new TIntArrayList();
		sourceData = new TStringList(charset);
		ixIssue = new TIntArrayList();
	}
	
	/** Returns int[n][3] array <br>
	 * <b>Coulumns</b>:<br>
	 * 0 - Source line number<br>
	 * 1 - Issue type (1 - wrong hash; 2 - unparsed)<br>
	 * 2 - Issue-collection index (ParsedData / ArrayList with BadRecords)*/
	public int[][] getIndex() {
		int size = sourceData.size();
		int[][] index = new int[size][3];
		for (int i = 0; i < size; i++) {
			index[i][0] = ixLogFile.get(i);
			index[i][2] = ixIssue.get(i);
			if (index[i][2] > 0) { // Wrong hash
				index[i][2]--;
				index[i][1] = 1;
			} else
				if (index[i][2] < 0) { // Unparsed
					index[i][2] = -1 * index[i][2] - 1;
					index[i][1] = 2;
				}
		}
		return index;
	}
	
	public String getSourceLine(int index) {
		return sourceData.get(index);
	}
	
	public boolean addWrongHashLink(int lineNumber, String line, int collectionIndex) {
		try {
			sourceData.add(line);
		} catch (UnsupportedEncodingException e) {
			LOG.log(Level.WARNING, "Ошибка при добавлении ссылки на некорректный хэш - кодировка строки отличается от " + charset.name());
			return false;
		}
		ixLogFile.add(lineNumber);
		ixIssue.add(collectionIndex + 1);
		return true;
	}
	
	public boolean addUnparsedLink(int lineNumber, String line, int collectionIndex) {
		try {
			sourceData.add(line);
		} catch (UnsupportedEncodingException e) {
			LOG.log(Level.WARNING, "Ошибка при добавлении ссылки на некорректную строку - кодировка строки отличается от " + charset.name());
			return false;
		}
		ixLogFile.add(lineNumber);
		ixIssue.add(-1 * collectionIndex - 1);
		return true;
	}
}
