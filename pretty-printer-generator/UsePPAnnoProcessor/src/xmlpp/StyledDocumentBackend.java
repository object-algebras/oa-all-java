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

package xmlpp;

import de.uka.ilkd.pp.Backend;
import de.uka.ilkd.pp.NoExceptions;

import javax.swing.text.*;

/** A {@link de.uka.ilkd.pp.Backend} which appends all output to a 
 * {@link javax.swing.text.StyledDocument}.
 * If the parameter to the {@link #mark(Object o)} 
 * method is an instance of {@link javax.swing.text.AttributeSet}, following
 * characters sent to {@link #print(String)} will be printed using these attributes.
 * Initially, an empty set of attributes is used.
 * 
 * @author Martin Giese
 *
 */
public class StyledDocumentBackend implements Backend<NoExceptions> {
	protected StyledDocument out;
    protected int lineWidth;
    protected AttributeSet currentAttributes = SimpleAttributeSet.EMPTY;
    
    /** Create a new StyledDocumentBackend.  This will append all output to
     * the given StyledDocument <code>sb</code>.    */
    public StyledDocumentBackend(StyledDocument sd,int lineWidth) {
    	this.lineWidth = lineWidth;
    	this.out = sd;
    }

    /** Create a new StyledDocumentBackend.  This will accumulate output in
     * a fresh, private DefaultStyledDocument. */
    public StyledDocumentBackend(int lineWidth) {
    	this(new DefaultStyledDocument(),lineWidth);
    }

    /** Append a String <code>s</code> to the output.  <code>s</code> 
     * contains no newlines. */
    public void print(String s) {
    	try {
    		out.insertString(out.getLength(),s,currentAttributes);
    	} catch (BadLocationException e) {
    		System.err.println(e);
    		System.exit(1);
    	}
    }

    /** Start a new line. */
    public void newLine() {
    	print("\n");
    }

    /** Closes this backend */
    public void close() {
    	return;
    }

    /** Flushes any buffered output */
    public void flush() {
    	return;
    }

    /** Gets called to record a <code>mark()</code> call in the input. 
     * If <code>o</code> is an instance of {@link javax.swing.text.AttributeSet},
     * any further text is printed with these attributes. */
    public void mark(Object o) {
    	if (o instanceof AttributeSet) {
    		currentAttributes = (AttributeSet) o;
    	}
    }

    /** Returns the available space per line */
    public int lineWidth() {
    	return lineWidth;
    }

    /** Returns the space required to print the String <code>s</code> */
    public int measure(String s) {
    	return s.length();
    }

    /** Returns the accumulated output */
    public StyledDocument getDocument() {
    	return out;
    }
}
