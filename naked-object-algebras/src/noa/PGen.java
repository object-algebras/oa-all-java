package noa;

import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.antlr.v4.Tool;
import org.antlr.v4.tool.Grammar;
import org.antlr.v4.tool.ast.GrammarRootAST;

import com.sun.istack.internal.FragmentContentHandler;

import noa.annos.Fragment;
import noa.annos.Level;
import noa.annos.Skip;
import noa.annos.Syntax;
import noa.annos.Token;
import noa.util.Conventions;
import noa.util.NormalAlt;
import noa.util.Rules;

/*
 * TODO: <assoc=left> and <assoc=right> (non-assoc is not supported by antlr4)
 * TODO: make annotation processor version to automatically generate upon build.
 */

public class PGen {
	private Class<?> signature;
	private Class<?> tokensClass;
	private static String pGenName;

	public PGen(Class<?> tokens, Class<?> signature) {
		this.tokensClass = tokens;
		this.signature = signature;
	}

	public void generate(String name, String pkg, String path) {
        generate(name, pkg, path, true, null);
	}

	public void generate(String name, String pkg, String path, boolean log, String logFile) {
	    this.pGenName = name;
		Rules rules = new Rules(name, pkg, tokensClass, signature);
		addProductions(rules);

		StringBuilder sb = new StringBuilder();
		rules.groupByLevel();
		rules.generate(sb);
		generateTokens(sb);

		String antlrContent = sb.toString();
		if (log) {
//			System.out.println(antlrContent);
            try {
                FileWriter writer = new FileWriter(logFile);
                writer.write(antlrContent);
                writer.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
		}

		Tool t = new org.antlr.v4.Tool();
		GrammarRootAST g = t.parseGrammarFromString(antlrContent);
		Grammar theG = t.createGrammar(g);
		t.gen_listener = false;
		t.gen_visitor = false;
		t.gen_dependencies = false;
		theG.fileName = path;
		t.process(theG, true);
	}

	// add a new boolean field fragment
	// fragment is true if the token is a fragment in antlr
	static class Tk implements Comparable<Tk> {
		private String name;
		private boolean skip;
		private int level;
		private String def;
		private boolean fragment;

		Tk(String name, String def, boolean skip, boolean fragment, int level) {
			this.name = name;
			this.def = def;
			this.skip = skip;
			this.level = level;
			this.fragment = fragment;
		}

		@Override
		public int compareTo(Tk o) {
			return new Integer(o.level).compareTo(level);
		}

		@Override
		public String toString() {
		    if (name.equals("newline")) {
		        // add stack operations that is needed in Python newlines
		        return name.toUpperCase()
		                + "\n"
		                + ": ( {atStartOfInput()}?   SPACES\n"
		                + "  | ( '\\r'? '\\n' | '\\r' ) SPACES?\n"
		                + "  )\n"
		                + "{\n"
		                + "  String newLine = getText().replaceAll(\"[^\\r\\n]+\", \"\");\n"
		                + "  String spaces = getText().replaceAll(\"[\\r\\n]+\", \"\");\n"
		                + "  int next = _input.LA(1);\n"
		                + "  if (opened > 0 || next == '\\r' || next == '\\n' || next == '#') {\n"
		                + "    skip();\n"
		                + "  }\n"
		                + "  else {\n"
		                + "    emit(commonToken(NEWLINE, newLine));\n"
		                + "    int indent = getIndentationCount(spaces);\n"
		                + "    int previous = indents.isEmpty() ? 0 : indents.peek();\n"
		                + "    if (indent == previous) {\n"
		                + "      skip();\n"
		                + "    }\n"
		                + "    else if (indent > previous) {\n"
		                + "      indents.push(indent);\n"
		                + "      emit(commonToken("+pGenName+"Parser"+".INDENT, spaces));\n"
		                + "    }\n"
		                + "    else {\n"
		                + "      while(!indents.isEmpty() && indents.peek() > indent) {\n"
		                + "        this.emit(createDedent());\n"
		                + "        indents.pop();\n"
		                + "      }\n"
		                + "    }\n"
		                + "  }\n"
		                + "}\n"
		                + ";\n"
		                ;
		    }
		    else {
		        return (fragment?"fragment ":"") + name.toUpperCase() + ": " + def + (skip ? " -> skip" : "") + ";";
		    }
		}
	}

	private void generateTokens(StringBuilder sb) {
		Method[] ms = allMethodsOf(tokensClass);
		List<Tk> tokens = new ArrayList<Tk>();
		for (Method m : ms) {
			Token tk = m.getAnnotation(Token.class);
			if (tk == null) {
				continue;
			}
			boolean skip = m.getAnnotation(Skip.class) != null;
			Level l = m.getAnnotation(Level.class);
			int level = l != null ? l.value() : Conventions.MAX_PRECEDENCE;
			boolean fragment = m.getAnnotation(Fragment.class) != null;
			tokens.add(new Tk(m.getName(), tk.value(), skip, fragment, level));
		}
		Collections.sort(tokens);
		for (Tk tk : tokens) {
			sb.append(tk.toString() + "\n");
		}
	}

	private static Method[] allMethodsOf(Class<?> cls) {
		// Traverse explicitly because getMethods does not
		// return static methods in extended interfaces.
		Set<Method> ms = new HashSet<Method>();
		allMethods(cls, ms);
		return ms.toArray(new Method[] {});
	}

	private static void allMethods(Class<?> cls, Set<Method> ms) {
		for (Method m : cls.getMethods()) {
			ms.add(m);
		}
		assert cls.isInterface();
		for (Class<?> i : cls.getInterfaces()) {
			allMethods(i, ms);
		}
	}

	private void addProductions(Rules rules) {
		Method[] ms = signature.getMethods();
		for (Method m : ms) {
			Syntax anno = m.getAnnotation(Syntax.class);
			if (anno == null) {
				System.err.println("Warning: method without syntax/token anno: " + m);
				continue;
			}
			String alt = anno.value();
			String[] syms = alt.split(" ");

			// // replace "$" with " "
			// for (int i = 0; i < syms.length; ++i) {
			// syms[i] = syms[i].replace('$', ' ');
			// }

			List<String> realSyms = Arrays.asList(syms).subList(2, syms.length);
			Level precAnno = m.getAnnotation(Level.class);
			int prec = Conventions.MAX_PRECEDENCE;
			if (precAnno != null) {
				prec = precAnno.value();
			}
			rules.addAlt(new NormalAlt(syms[0], prec, m.getName(), realSyms));
		}
		// String[] syms = {};
		// List<String> realSysms = Arrays.asList(syms);
		// rules.addAlt(new NormalAlt("form", Conventions.MAX_PRECEDENCE, "",
		// realSysms));
	}

	// private String typeToNonTerminal(Type t) {
	// String typeName = t.getTypeName();
	// if (typeName.matches("^java\\.util\\.List<.*>$")) {
	// typeName = typeName.substring(typeName.lastIndexOf("<") + 1,
	// typeName.length() - 1);
	// }
	// return typeName.toLowerCase();
	// }

}
