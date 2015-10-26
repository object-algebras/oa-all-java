// This file is part of the Java™ Pretty Printer Library (JPPlib)
// Copyright (C) 2007 Martin Giese
//
// This program is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.

// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.

// You should have received a copy of the GNU General Public License
// along with this program.  If not, see <http://www.gnu.org/licenses/>.

package xmlpp;

/** Some utilities for XML pretty printers */
public class XMLUtils {

	/** Replace critical characters by XML entities. */
	static String quoteCharacterData(char[] ch, int start, int length) {
		StringBuilder sb = new StringBuilder();
		for(int i=start;i<start+length;i++) {
			char c;
			switch (c=ch[i]) {
			case '<':
				sb.append("&lt;");
				break;
			case '>':
				sb.append("&gt;");
				break;
			case '&':
				sb.append("&amp;");
				break;
			default:
				sb.append(c);
			break;
			}
		}
		return sb.toString();
	}

	/** Replace critical characters by XML entities. */
	static String quoteCharacterData(String s) {
		return s.replaceAll("&","&amp;")
		         .replaceAll("<","&lt;")
		         .replaceAll(">","&gt;");
	}


	/** Perform entity-quoting of quotes within attribute values. */
	public static String quoteAttrValue(String s) {
		return "\""
			+s.replaceAll("&", "&amp;")
			  .replaceAll("\"", "&quot;")
			  .replaceAll("\'", "&apos;")
			    +"\"";
	}

}
