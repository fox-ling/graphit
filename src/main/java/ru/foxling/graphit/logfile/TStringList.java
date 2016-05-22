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
import gnu.trove.list.array.TByteArrayList;
import gnu.trove.list.array.TIntArrayList;

/** Memory efficient string list */
public class TStringList {
	private final Charset charset;
	private TByteArrayList data;
	private TIntArrayList offsets;
	
	public TStringList(Charset charset) {
		this.charset = charset;
		data = new TByteArrayList();
		offsets = new TIntArrayList();
	}

	public void add(String str) throws UnsupportedEncodingException {
		int offset = data.size();
		byte[] blist = str.getBytes(charset);
		data.add(blist);
		offsets.add(offset);
	}
	
	public String get(int index) {
		int iMax = offsets.size() - 1;
		if (index > iMax)
			throw new IndexOutOfBoundsException();
		
		return new String(data.subList(offsets.get(index), index == iMax ? data.size() : offsets.get(index + 1)).toArray(), charset);
	}
	
	public int size() {
		return offsets.size();
	}
	
	public void remove(int offset, int length) {
		data.remove(offset, length);
		offsets.remove(offset, length);
	}
}
