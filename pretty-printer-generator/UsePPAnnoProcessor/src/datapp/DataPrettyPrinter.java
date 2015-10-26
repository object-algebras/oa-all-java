//This file is part of the Java™ Pretty Printer Library (JPPlib)
//Copyright (C) 2007 Martin Giese

//This program is free software: you can redistribute it and/or modify
//it under the terms of the GNU General Public License as published by
//the Free Software Foundation, either version 3 of the License, or
//(at your option) any later version.

//This program is distributed in the hope that it will be useful,
//but WITHOUT ANY WARRANTY; without even the implied warranty of
//MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//GNU General Public License for more details.

//You should have received a copy of the GNU General Public License
//along with this program.  If not, see <http://www.gnu.org/licenses/>.

package datapp;

import de.uka.ilkd.pp.*;

import java.util.*;

/** Utility class to pretty-print arbitrary objects.
 * There is a static method 
 * {@link #prettyPrint(Object)} which produces a pretty-printed
 * String representation of an arbitrary object, with useful
 * layouts for collections, arrays, and maps.  See the 
 * documentation of that method for details.
 * 
 * @author Martin Giese
 *
 */
public class DataPrettyPrinter {

	/** The maximum line width */
	public static final int DEFAULT_LINE_WIDTH = 80;

	/** The indentation, in particular for Map entries. */
	public static final int DEFAULT_INDENTATION = 2;

	/**
	 * Pretty print an object according to its type. See the
	 * documentation of {@link DataLayouter#print(Object)} for a
	 * desciption of the layout chosen for various data types.
	 * 
	 * @param o
	 *            the object to be pretty printed
	 * @return the pretty-printed String representation of <code>o</code>
	 */
	public static String prettyPrint(Object o) {
		StringBackend back = new StringBackend(DEFAULT_LINE_WIDTH);
		DataLayouter<NoExceptions> out = new DataLayouter<NoExceptions>(back, DEFAULT_INDENTATION);
		out.print(o);
		out.close();
		return back.getString();
	}
	
	/** Recursively consruct a tree with given arity and depth. */
	private static Tree<String> createTree(int arity,int depth) {
		return createTree("root",arity,depth);
	}
	
	private static Tree<String> createTree(String prefix,int arity,int depth) {
		Tree<String> result = new Tree<String>(prefix);
		if (depth>0) {
			for (int j=0;j<arity;j++) {
				result.addChild(createTree(prefix+"."+(j+1),arity,depth-1));
			}
		}
		return result;
	}
	
	/** Show of functionality of DataLayouter.  Constructs
	 * and prints a couple of data objects.
	 * 
	 * @param args Command line arguments, ignored
	 */
	public static void main(String[] args) {
		System.out.println("A short list\n");
		List imsevimse = Arrays.asList(new String[] { "imse", "vimse",
				"spindel" });
		System.out.println(prettyPrint(imsevimse));

		System.out.println("\nA nested array\n");
		int[][] pas = new int[10][];
		for(int i=0;i<10;i++) {
			pas[i] = new int[i+1];
			pas[i][0] = pas[i][i] = 1;
			for(int j=1;j<i;j++) {
				pas[i][j] = pas[i-1][j] + pas[i-1][j-1];
			}
		}
		System.out.println(prettyPrint(pas));

		System.out.println("\nThe System environment\n");
		System.out.println(prettyPrint(System.getenv()));

		System.out.println("\nA list of maps from Strings to stuff\n");
		List<Map<String, Object>> l = new ArrayList<Map<String, Object>>();
		for (int n = 1; n <= 15; n++) {
			Map<String, Object> m = new HashMap<String, Object>();
			m.put("number", n);
			m.put("square", n * n);
			List<Integer> mult = new ArrayList<Integer>();
			for (int i = 1; i <= 11; i++) {
				mult.add(n * i);
			}
			m.put("some multiples", mult);
			l.add(m);
		}
		System.out.println(prettyPrint(l));

		System.out.println("\nA tree\n");
		Tree<String> t = createTree(2,5);
		System.out.println(prettyPrint(t));
	}
}
