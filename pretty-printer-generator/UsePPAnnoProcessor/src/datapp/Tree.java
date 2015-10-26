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

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

import de.uka.ilkd.pp.DataLayouter;
import de.uka.ilkd.pp.PrettyPrintable;

/** An variable-arity tree.
 * A simple representation where each node has a label of type 
 * <code>E</code>, and may have arbitrarily many children.  Trees
 * can be created using the constructor, and modified by adding
 * new children.
 * 
 * @author Martin Giese
 *
 * @param <E> type of (internal and leaf) nodes
 */
public class Tree<E> implements PrettyPrintable {

	private E label;
	private List<Tree<E>> children = new ArrayList<Tree<E>>();
	
	/** Construct a new Tree without children.
	 * There is currently no way to change the label of the node later,
	 * but children can be added from left to right.
	 * 
	 * @param label the label of the single node of the constructed tree
	 */
	public Tree(E label) {
		this.label = label;
	}
	
	/** Answer whether this is a leaf.
	 * This means the same as that this is a node without children.
	 * 
	 * @return <code>true</code> if this is a leaf.
	 */
	public boolean isLeaf() {
		return children.size() == 0;
	}
	
	/** Return the label of this node. */
	E getLabel() {
		return label;
	}
	
	/** Add a new rightmost child. */
	public void addChild(Tree<E> t) {
		children.add(t);
	}
	
	/** Get the children of this node.
	 * This is returned as an unmodifiable list.
	 * 
	 * @return this node's list of children
	 */
	public List<Tree<E>> getChildren() {
		return Collections.unmodifiableList(children);
	}
	
	/** Pretty-print the tree rooted at this node.
	 * The layout for trees is 
	 * <pre>
	 * root-label(child1-label, child2-label, ...)
	 * </pre>
	 * if it fits on one line, and
	 * <pre>
	 * root-label(
	 *    child1-label,
	 *    child2-label, 
	 *    ...)
	 * </pre>
	 * otherwise, where the labels are printed using the 
	 * {@link DataLayouter#print(Object)} method of <code>l</code>.
	 * 
	 * @param l the DataLayouter to which the tree will be printed
	 */
	public <Exc extends Exception> void prettyPrint(DataLayouter<Exc> l) 
	throws Exc {
		l.beginC(3).print(label);
		if (!isLeaf()) {
			l.print("(").brk(0, 0);
			boolean first = true;
			for (Tree<E> ch:children) {
				if (!first) {
					l.print(",").brk(1, 0);
				}
				ch.prettyPrint(l);
				// Alternatively: l.print(ch);
				first = false;
			}
			l.print(")");
		}
		l.end();
	}

}
