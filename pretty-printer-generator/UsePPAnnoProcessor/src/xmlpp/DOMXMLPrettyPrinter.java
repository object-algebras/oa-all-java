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

import de.uka.ilkd.pp.*;
import java.io.*;

import javax.xml.parsers.DocumentBuilder; 
import javax.xml.parsers.DocumentBuilderFactory;  

import java.io.File;
import java.io.IOException;

import org.w3c.dom.*;

/** Pretty-prints an XML document.
 * This class is a demo for the JPPLib classes.  It reads
 * an XML file using DOM and pretty-prints it to System.out.
 * Some amount of care is taken regarding quoting, but
 * there are probably some mistakes.  The main point here is to show
 * the use of JPPLib, in particular when used with active traversal
 * of a data structure, as opposed to the event based implementation
 * in {@link SimpleXMLPrettyPrinter}
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
public class DOMXMLPrettyPrinter {

	public static final int INDENTATION= 3;

	private Layouter<IOException> pp;
	private Document document;
	
	/** A call to break is required before printing
	 * the next item.  This is needed because breaks are to
	 * be printed before every item, except text, but not
	 * at the beginning.
	 */
	private boolean insertBreak;

	public DOMXMLPrettyPrinter(Document document) {
		super();
		this.document = document;
		pp = Layouter.getWriterLayouter(new BufferedWriter(new OutputStreamWriter(System.out)));
	}

	void prettyPrint() throws IOException {
		pp.beginC(0);
        pp.print("<?xml version=\"1.0\"?>").nl();
		insertBreak = false;
		prettyPrint(document);
		pp.end();
		pp.close();
	}
	
	void prettyPrint(Node node) throws IOException {
		switch(node.getNodeType()) {
		case Node.DOCUMENT_NODE: 
			prettyPrint(((Document)node).getDocumentElement() );
			break;
		case Node.ELEMENT_NODE: 
			prettyPrintElement(node);
			break;

		case Node.ENTITY_REFERENCE_NODE: 
			prettyPrintEntityReference(node);
			break;
		case Node.CDATA_SECTION_NODE: 
			prettyPrintCDATASection(node);
			break;
		case Node.TEXT_NODE: 
			prettyPrintText(node);
			break;
		case Node.PROCESSING_INSTRUCTION_NODE: 
			prettyPrintProcessingInstruction(node);
			break;
		}
		insertBreak = true;
	}

	private void prettyPrintElement(Node node) throws IOException {
		if (insertBreak) {
			pp.brk(0,0);
		}
		pp.beginC(INDENTATION);
		pp.print("<");
		pp.print(node.getNodeName());
		prettyPrintAttributes(node.getAttributes());
		pp.print(">");
	
		NodeList children = node.getChildNodes();
		if (children != null)
		{
			int len = children.getLength();
			for (int i = 0; i < len; i++) {
				prettyPrint(children.item(i));
			}
		}
	
		pp.brk(0,-INDENTATION);
		pp.print("</");
		pp.print(node.getNodeName());
		pp.print(">").end();
	}

	private void prettyPrintAttributes(NamedNodeMap attrs) throws IOException {
		if (attrs.getLength()>0) {
			pp.print(" ").beginC(0);
			for (int i = 0; i < attrs.getLength(); i++)
			{
				Node attr = attrs.item(i);
				pp.print(attr.getNodeName() + 
						"=" + XMLUtils.quoteAttrValue(attr.getNodeValue()));
				if (i!=attrs.getLength()-1) {
					pp.brk(1,0);
				}
			}
			pp.end();
		}
	}

	private void prettyPrintEntityReference(Node node) throws IOException {
		pp.print("&");
		pp.print(node.getNodeName());
		pp.print(";").nl();
	}

	private void prettyPrintCDATASection(Node node) throws IOException {
		pp.print("<![CDATA[");
		pp.print(node.getNodeValue());
		pp.print("]]>").nl();
	}

	private void prettyPrintText(Node node) throws IOException {
		// collect words
		String quotedText = XMLUtils.quoteCharacterData(node.getNodeValue()).trim();
		String[] words=quotedText.split("\\s+");
		pp.beginIInd(0);
		// output words separated by blanks
		boolean brk = false;
		for(String word:words) {
			if (brk) {
				pp.brk(1,0);
			}
			pp.print(word);
			brk = true;
		}
		pp.end();
	}

	private void prettyPrintProcessingInstruction(Node node) throws IOException {
		pp.print("<?").nl();
		pp.print(node.getNodeName());
		String data = node.getNodeValue();
		pp.print(" ");
		pp.print(data);
		pp.print("?>").nl();
	}

	public static void main(String argv[])
    {
        if (argv.length != 1) {
            System.err.println("Usage: java xmlpp.DOMXMLPrettyPrinter filename");
            System.exit(1);
        }
        
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setIgnoringComments(true);
        factory.setIgnoringElementContentWhitespace(true);
        factory.setCoalescing(true);
        try {
        	DocumentBuilder builder = factory.newDocumentBuilder();
        	Document document = builder.parse( new File(argv[0]) );
        	
        	new DOMXMLPrettyPrinter(document).prettyPrint();
        } catch (Exception e) {
           e.printStackTrace();
        }
    }
}
