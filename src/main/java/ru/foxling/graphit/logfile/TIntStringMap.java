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

/** Unsafe but memory efficient <Int,String> kinda map.
 * It doesn't check key existence before adding, which
 * makes it more like tuple than a map  */
public class TIntStringMap {
	private final Charset charset;
	private TIntArrayList keys;
	private TByteArrayList data;
	private TIntArrayList offsets;
	
	public TIntStringMap(Charset charset) {
		this.charset = charset;
		keys = new TIntArrayList();
		data = new TByteArrayList();
		offsets = new TIntArrayList();
	}

	public void add(int key, String str) throws UnsupportedEncodingException {
		int offset = data.size();
		byte[] blist = str.getBytes(charset);
		keys.add(key);
		data.add(blist);
		this.offsets.add(offset);
	}
	
	public String get(int key) throws UnsupportedEncodingException {
		int iMax = offsets.size() - 1;
		int index = keys.indexOf(key);
		if (index == -1) 
			return null;
		
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
