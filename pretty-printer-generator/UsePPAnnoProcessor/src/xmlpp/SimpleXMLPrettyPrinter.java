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

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintStream;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.ParserAdapter;

import de.uka.ilkd.pp.Layouter;

/** Pretty-prints an XML document.
 * This class is a demo for the JPPLib classes.  It reads
 * an XML file using SAX and pretty-prints it to System.out.
 * Some amount of care is taken regarding quoting, but
 * there are probably some mistakes.  The main point here is to show
 * the use of JPPLib, in particular when used together with an
 * event-based presentation of the data, as opposed to active
 * traversal as in {@link DOMXMLPrettyPrinter}
 * 
 * <p>The layout of elements is like
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
public class SimpleXMLPrettyPrinter extends DefaultHandler {
 
	public static final int INDENTATION = 3;
	
	private PrintStream out;
	
	private Layouter<java.io.IOException> pp;

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
	
	public SimpleXMLPrettyPrinter(PrintStream out) {
		this.out = out;
		pp = Layouter.getWriterLayouter(new BufferedWriter(new OutputStreamWriter(this.out)));
	}

	@Override
	public void characters(char[] ch, int start, int length) 
	throws SAXException {
		try {
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
		} catch (IOException e) {
			throw new SAXException(e);
		}			
	}
	
	
	/** If a characters-block is still open, close it. */
	private void wrapUpCharacters() 
	throws IOException {
		if (lastSawCharacters) {
			pp.end();
			lastSawCharacters = false;
		}
	}
	
	@Override
	public void startElement(String namespace, 
			String localName, 
			String qName,
			Attributes atts) 
	throws SAXException {
		try {
			wrapUpCharacters();
			if (insertBreak) {
				pp.brk(0,0);
			}
			pp.beginC(INDENTATION).print("<"+localName);
			printAttributes(atts);
			pp.print(">");
			insertBreak = true;
		} catch (IOException e) {
			throw new SAXException(e);
		}
	}
	
	/** Pretty print attributes of an element. Line breaks are inserted between 
	 * attributes if necessary, but currently not between name and value of attributes. */
	public void printAttributes(Attributes atts)
	throws IOException {
		if (atts.getLength()>0) {
			pp.print(" ").beginC(0);
			for (int i=0;i<atts.getLength();i++) {
				pp.print(atts.getLocalName(i)
						+"="+XMLUtils.quoteAttrValue(atts.getValue(i)));
				if (i!=atts.getLength()-1) {
					pp.brk(1,0);
				}
			}
			pp.end();
		}
	}
	
	
	@Override
	public void endElement(String uri, String localName, String qName) 
	throws SAXException {
		try {
			wrapUpCharacters();
			if (insertBreak) {
				pp.brk(0,-INDENTATION);
			}
			pp.print("</"+localName+">").end();
			insertBreak = true;
		} catch (IOException e) {
			throw new SAXException(e);
		}
	}

	@Override
	public void processingInstruction(String target, String data) 
	throws SAXException {
		try {
			pp.print("<?"+target+" "+data+"?>").nl();
		} catch (IOException e) {
			throw new SAXException(e);
		}
	}
	
	public void process(String urlString) 
	throws Exception {
		SAXParserFactory spf = 
			SAXParserFactory.newInstance();
		SAXParser sp = spf.newSAXParser();
		ParserAdapter pa = 
			new ParserAdapter(sp.getParser());
		pa.setContentHandler(this);
        pp.beginC(0);
        pp.print("<?xml version=\"1.0\"?>").nl();
        insertBreak = false;
		pa.parse(urlString);
		if (insertBreak) {
			pp.brk(0,0);
		}
		wrapUpCharacters();
		pp.end().close();
	}

	public static void main(String[] args) {
		if (args.length!=1) {
			System.err.println("usage: java xmlpp.SimpleXMLPrettyPrinter input.xml");
			System.exit(1);
		}
		try {
            SimpleXMLPrettyPrinter xpp = new SimpleXMLPrettyPrinter(System.out);
            xpp.process(args[0]);
        }
        catch (Throwable t) {
            t.printStackTrace();
        }
	}

}