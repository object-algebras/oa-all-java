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

import java.awt.Font;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.text.AttributeSet;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.ParserAdapter;

import de.uka.ilkd.pp.Layouter;
import de.uka.ilkd.pp.NoExceptions;

/** Displays an XML document in a window.
 * This class is a demo for the JPPLib classes.  It reads
 * an XML file using SAX and displays a pretty printed output
 * in a JTextPane, with some colouring.
 * Some amount of care is taken regarding quoting, but
 * there are probably some mistakes.  The main point here is to show
 * the use of JPPLib.  In particular, the colour information is
 * passed through to the {@link javax.swing.text.StyledDocument} using the 
 * {@link de.uka.ilkd.pp.Layouter#mark(Object)} method.
 * 
 * <p>The Layout is like
 * <pre>
 * &lt;doc&gt;&lt;head&gt;&lt;/head&gt;&lt;body&gt;&lt;/body&gt;&lt;/doc&gt;
 * </pre>
 * as long as everything fits on one line.  Otherwise, elements
 * are broken, and sub-elements are indented, e.g.
 * <pre>
 * &lt;doc&gt;
 *     &lt;head&gt;&lt;/head&gt;
 *     &lt;body&gt;&lt;/body&gt;
 * &lt;/doc&gt;
 * </pre>
 * 
 * Line breaks are also inserted in between attributes if tags get too large.
 * 
 * @author Martin Giese
 *
 */
public class StyledXMLPrettyPrinter extends DefaultHandler {
 
	private static final Font FONT = new java.awt.Font("Monospaced",0,12);
	
	public static final int INDENTATION = 3;
	
	private Layouter<NoExceptions> pp;

	/** A call to break is required before printing
	 * the next item.  This is needed because the call to
	 * {@link de.uka.ilkd.pp.Layouter#brk(int, int)} needs to say how
	 * much indentation is required.  If the next element is an end tag,
	 * we need to outdent.
	 */
	private boolean insertBreak;
	
	/** The previous SAX event was a call to
	 * {@link #characters(char[], int, int)}. This is needed because
	 * SAX might deliver character data in small chunks, which we want 
	 * to print within a single (inconsistent) block.  The first invocation
	 * of characters will open the block, and set this flag.  Any non-character
	 * event will first check this flag, and close the block if necessary. */
	private boolean lastSawCharacters = false;
	
	public static final AttributeSet ATTR_EMPTY;
	public static final AttributeSet ATTR_BLUE;	
	public static final AttributeSet ATTR_GREEN;	
	public static final AttributeSet ATTR_GRAY;	

	static {
		MutableAttributeSet as;
		ATTR_EMPTY = SimpleAttributeSet.EMPTY;
		as = new SimpleAttributeSet();
		StyleConstants.setForeground(as,java.awt.Color.BLUE);
		ATTR_BLUE = as;
		as = new SimpleAttributeSet();
		StyleConstants.setForeground(as,java.awt.Color.GREEN);
		ATTR_GREEN = as;
		as = new SimpleAttributeSet();
		StyleConstants.setForeground(as,java.awt.Color.GRAY);
		ATTR_GRAY = as;
	}
	
	public StyledXMLPrettyPrinter(StyledDocumentBackend back) {
		pp = new Layouter<NoExceptions>(back,INDENTATION);
	}

	@Override
	public void characters(char[] ch, int start, int length) {
		// collect words
		String quotedText = XMLUtils.quoteCharacterData(ch, start, length).trim();
		String[] words=quotedText.split("\\s+");
		// if last element was arleady characters, continue with a
		// separating brk, otherwise, start new inconsisten block.
		if (lastSawCharacters) {
			pp.brk(1,0);
		} else {
			pp.beginIInd(0);
		}
		// output words separated by blanks
		boolean brk = false;
		for(String word:words) {
			if (brk) {
				pp.brk(1,0);
			}
			pp.print(word);
			brk = true;
		}
		lastSawCharacters = true;
	}

	/** If a characters-block is still open, close it. */
	private void wrapUpCharacters() {
		if (lastSawCharacters) {
			pp.end();
			lastSawCharacters = false;
		}
	}
	
	@Override
	public void startElement(String namespace, 
			String localName, 
			String qName,
			Attributes atts) {
		wrapUpCharacters();
		if (insertBreak) {
			pp.brk(0,0);
		}
		pp.mark(ATTR_BLUE);
		pp.beginC(INDENTATION).print("<"+localName);
		printAttributes(atts);
		pp.print(">");
		pp.mark(ATTR_EMPTY);
		insertBreak = true;
	}

	/** Pretty print attributes of an element. Line breaks are inserted between 
	 * attributes if necessary, but currently not between name and value of attributes. */
	public void printAttributes(Attributes atts) {
		if (atts.getLength()>0) {
			pp.print(" ").beginC(0);
			for (int i=0;i<atts.getLength();i++) {
				pp.print(atts.getLocalName(i)
						+"=");
				pp.mark(ATTR_GREEN);
				pp.print(XMLUtils.quoteAttrValue(atts.getValue(i)));
				pp.mark(ATTR_EMPTY);
				if (i!=atts.getLength()-1) {
					pp.brk(1,0);
				}
			}
			pp.end();
		}
	}
	
	@Override
	public void endElement(String uri, String localName, String qName) {
		wrapUpCharacters();
		if (insertBreak) {
			pp.brk(0,-INDENTATION);
		}
		pp.mark(ATTR_BLUE);
		pp.print("</"+localName+">").mark(ATTR_EMPTY).end();
		insertBreak = true;
	}

	@Override
	public void processingInstruction(String target, String data) {
		wrapUpCharacters();
		if (insertBreak) {
			pp.brk(0,0);
		}
		pp.mark(ATTR_GRAY);	
		pp.print("<?"+target+" "+data+"?>").mark(ATTR_EMPTY).nl();
	}
	
	public void process(String urlString) 
	throws Exception {
		SAXParser sp = SAXParserFactory.newInstance().newSAXParser();
		ParserAdapter pa = new ParserAdapter(sp.getParser());
		pa.setContentHandler(this);
        pp.beginC(0).mark(ATTR_GRAY).print("<?xml version=\"1.0\"?>");
        pp.mark(ATTR_EMPTY).nl();
        insertBreak = false;
		pa.parse(urlString);
		wrapUpCharacters();
		if (insertBreak) {
			pp.brk(0,0);
		}
		pp.end().close();
	}

    public static void createAndShowGUI(String input) {
    	try {
    		final JFrame frame = new JFrame(input);
    		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    		StyledDocumentBackend back = new StyledDocumentBackend(80);
    		final JTextPane textPane = new JTextPane(back.getDocument());
    		textPane.setFont(FONT);
    		textPane.setEditable(false);
    		textPane.setMargin(new java.awt.Insets(5, 5, 5, 5));
    		frame.getContentPane().add(new JScrollPane(textPane));

    		StyledXMLPrettyPrinter xpp = new StyledXMLPrettyPrinter(back);
    		xpp.process(input);

    		frame.pack();
    		frame.setVisible(true);
    	} catch (Exception e) {
    		e.printStackTrace(System.err);
    		System.exit(1);
    	}
    }

	public static void main(String[] args) {
		if (args.length!=1) {
			System.err.println("usage: java xmlpp.StyledXMLPrettyPrinter input.xml");
			System.exit(1);
		}
		final String input = args[0];
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
		      public void run() {
		        createAndShowGUI(input);
		      }
		    });
		
		}


}
