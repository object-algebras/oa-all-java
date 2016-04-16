package anno;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.type.TypeMirror;
import javax.tools.JavaFileObject;

//import noa.util.Alt;
//import noa.util.ConventionsPP;
//import noa.util.NormalAlt;
//import noa.util.*;
//import noa.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;

// Not working
import static anno.ConventionsPP.getRegularSymbol;
import static anno.ConventionsPP.getSepListSymbol;
import static anno.ConventionsPP.getSepListToken;
import static anno.ConventionsPP.isEOF;
import static anno.ConventionsPP.isLiteral;
import static anno.ConventionsPP.isNonTerminal;
import static anno.ConventionsPP.isRegular;
import static anno.ConventionsPP.isSepList;
import static anno.ConventionsPP.isToken;
import static anno.ConventionsPP.isZeroOrMoreSepList;
import static anno.ConventionsPP.labelFor;
import static anno.ConventionsPP.returnVariable;

// Alt and NormalAlt are for debugging and are not intended to be staying here permanently.
abstract class Alt implements Comparable<Alt>, ConventionsPP {
	private String nt;
	private int prec;

	public Alt(String nt, int prec) {
		this.nt = nt;
		this.prec = prec;
	}

	public int getLevel() {
		return prec;
	}

	public String getNT() {
		return nt;
	}

	@Override
	public int compareTo(Alt o) {
		return new Integer(o.getLevel()).compareTo(getLevel());
	}
}

class NormalAlt extends Alt implements ConventionsPP {

	private List<String> syms;
	private String cons;
	private int labelCounter;

	public NormalAlt(String nt, int prec, String cons, List<String> syms, int labelCounter) {
		super(nt, prec);
		this.cons = cons;
		this.syms = syms;
		this.labelCounter = labelCounter;
	}

	public boolean isInfix() {
		String op = syms.get(1);
		return syms.size() == 3 && (ConventionsPP.isToken(op) || ConventionsPP.isLiteral(op));
	}

	public String getOperator() {
		assert isInfix();
		return syms.get(1);
	}

	public String getLhs() {
		assert isInfix();
		return syms.get(0);
	}

	public String getRhs() {
		assert isInfix();
		return syms.get(2);
	}

	private boolean isNEWLINE(String s) {
		return s.equals("NEWLINE");
	}

	public String toString() {
		String prod = "";
		String args = "";
		// int labelCounter = 0;

		for (String s : syms) {
			// add some judgement on null
			if (ConventionsPP.isEOF(s)) {
				prod += s;
			} else if (s.equals("NEWLINE") | s.equals("INDENT") | s.equals("DEDENT")) {
				prod += s;
			} else if (ConventionsPP.isNonTerminal(s)) {
				prod += ConventionsPP.labelFor(labelCounter, s) + "=" + s + " ";
				args += "($" + ConventionsPP.labelFor(labelCounter, s) + ".ctx==null?" + "null:" + "($"
						+ ConventionsPP.labelFor(labelCounter, s) + "." + ConventionsPP.returnVariable(s) + ")),";
				labelCounter += 1;
			} else if (ConventionsPP.isRegular(s)) {
				String n = ConventionsPP.getRegularSymbol(s);
				if (n.equals("+")) {
					prod += ConventionsPP.labelFor(labelCounter, n) + "+=" + s.substring(0, s.length() - 1) + " ";
					args += args += "lift(\"" + ConventionsPP.returnVariable(n) + "\", $"
							+ ConventionsPP.labelFor(labelCounter, n) + "),";
				} else {
					prod += ConventionsPP.labelFor(labelCounter, n) + "+=" + s + " ";
					args += "lift(\"" + ConventionsPP.returnVariable(n) + "\", $"
							+ ConventionsPP.labelFor(labelCounter, n) + "),";
				}
				labelCounter += 1;
			} 			else if (ConventionsPP.isToken(s)) {
				prod += ConventionsPP.labelFor(labelCounter, s) + "=" + s + " ";
				args += s.toLowerCase() + "($" + ConventionsPP.labelFor(labelCounter, s) + ".text),";
				labelCounter += 1;
			} else if (ConventionsPP.isSepList(s)) {
				// generate seplist rules in 3 situations: token separator,
				// non-terminal separator and symbols
				String n = ConventionsPP.getSepListSymbol(s);
				String label = ConventionsPP.labelFor(labelCounter, n);
				String eltHead = label + "=" + n;
				String eltTail = label + "tail+=" + n;
				String sep = ConventionsPP.getSepListToken(s);
				if (ConventionsPP.isToken(sep)) {
					prod += "(" + eltHead + " (" + ConventionsPP.labelFor(labelCounter, sep) + "+=" + sep + " "
							+ eltTail + ")*)";
					args += "liftString($" + ConventionsPP.labelFor(labelCounter, sep) + "==null? null :$"
							+ ConventionsPP.labelFor(labelCounter, sep) + "), " + "($" + label + ".ctx==null||" + "$"
							+ label + "tail==null)?" + " null : (lift(\"" + ConventionsPP.returnVariable(n) + "\", $"
							+ label + "tail, " + "$" + label + "." + ConventionsPP.returnVariable(n) + ")),";
				} else if (ConventionsPP.isNonTerminal(sep)) {
					prod += "(" + eltHead + " (" + ConventionsPP.labelFor(labelCounter, sep) + "+=" + sep + " "
							+ eltTail + ")*)";
					args += " ($" + ConventionsPP.labelFor(labelCounter, sep) + "==null? " + "null : lift(\""
							+ ConventionsPP.returnVariable(sep) + "\", $" + ConventionsPP.labelFor(labelCounter, sep)
							+ ") ), " + "($" + label + ".ctx==null||" + "$" + label + "tail==null)?"
							+ " null : (lift(\"" + ConventionsPP.returnVariable(n) + "\", $" + label + "tail, " + "$"
							+ label + "." + ConventionsPP.returnVariable(n) + ")),";
				} else {
					prod += "(" + eltHead + " (" + sep + " " + eltTail + ")*)";
					args += " ($" + label + ".ctx==null||" + "$" + label + "tail==null)?" + " null : (lift(\""
							+ ConventionsPP.returnVariable(n) + "\", $" + label + "tail, " + "$" + label + "."
							+ ConventionsPP.returnVariable(n) + ")),";
				}
				if (ConventionsPP.isZeroOrMoreSepList(s)) {
					prod += "?";
				}
				// args += "lift(\"" + ConventionsPP.returnVariable(n) + "\", $"
				// + label +
				// "tail, " + "$" + label + "."
				// + ConventionsPP.returnVariable(n) + "),";
				labelCounter += 1;
			} else {
				prod += s + " ";
				labelCounter += 1;
			}
		}
		if (!args.isEmpty()) {
			// remove trailing comma
			args = args.substring(0, args.length() - 1);
		}
		// if (syms.size() == 0)
		// prod += "{}";
		// else
		prod += " {$" + ConventionsPP.returnVariable(getNT()) + " = " + BUILDER_FIELD + "." + cons + "(" + args + ");}";
		return prod;
	}

	public String getCons() {
		return cons;
	}

}
// import noa.annos.*;
// import noa.PGen;

// This is just an example of adding custom warning.

// @SupportedAnnotationTypes("fully.qualified.name.of.InternalAnnotationType")
// @SupportedSourceVersion(SourceVersion.RELEASE_6)
// public class CustomAnnotationProcessor extends AbstractProcessor {
//
// @Override
// public boolean process(Set<? extends TypeElement> annotations,
// RoundEnvironment roundEnv) {
// for (Element element :
// roundEnv.getElementsAnnotatedWith(InternalAnnotationType.class)) {
// InternalAnnotationType internalAnnotation =
// element.getAnnotation(InternalAnnotationType.class);
// String message = "The method " + element.getSimpleName()
// + " is marked internal and its use is discouraged";
// processingEnv.getMessager().printMessage(Diagnostic.Kind.WARNING, message);
// }
// return true;
// }
// }

@SupportedAnnotationTypes(value = { "anno.PP" })
@SupportedSourceVersion(SourceVersion.RELEASE_6)
public class PPProcessor extends AbstractProcessor {
	public static final String TAB = "\t";
	public static final String TAB2 = "\t\t";
	public static final String TAB3 = "\t\t\t";
	public static final String TAB4 = "\t\t\t\t";
	public static final String TAB5 = "\t\t\t\t\t";

	private Filer filer;
	// Not working at the moment.
	// private ProcessingEnvironment myEnv;

	@Override
	public void init(ProcessingEnvironment env) {
		// Not working at the moment.
		// myEnv = env;
		filer = env.getFiler();
	}

	private String[] toList(String message) {
		return message.split(",");
	}

	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment env) {

		String folder = "ppgen";

		String printerRes = "";

		// Add the factory interface IPrint here.
		printerRes += "package ppgen;\n";
		printerRes += "import de.uka.ilkd.pp.*;\n";
		printerRes += "public interface IPrint {\n";
		// Let's put it as 20 now for testing purpose.
		printerRes += TAB + "static final int DEFAULT_LINE_WIDTH = 80;\n";
		printerRes += TAB + "static final int DEFAULT_INDENTATION = 2;\n\n";
		printerRes += TAB + "default StringBackend print() {\n";
		printerRes += TAB2 + "StringBackend back = new StringBackend(DEFAULT_LINE_WIDTH);\n";
		printerRes += TAB2 + "Layouter<NoExceptions> pp = new Layouter<NoExceptions>(back, DEFAULT_INDENTATION);\n";
		printerRes += TAB2 + "printLocal(pp);\n";
		printerRes += TAB2 + "return back;\n";
		printerRes += TAB + "}\n\n";
		// The method with manual parameters
		printerRes += TAB + "default StringBackend print(int lineWidth, int indentation) {\n";
		printerRes += TAB2 + "StringBackend back = new StringBackend(lineWidth);\n";
		printerRes += TAB2 + "Layouter<NoExceptions> pp = new Layouter<NoExceptions>(back, indentation);\n";
		printerRes += TAB2 + "printLocal(pp);\n";
		printerRes += TAB2 + "return back;\n";
		printerRes += TAB + "}\n\n";
		printerRes += TAB + "void printLocal(Layouter<NoExceptions> pp);\n";
		printerRes += "}";

		try {
			// Also create the public interface Printer.
			JavaFileObject printerFile;
			printerFile = filer.createSourceFile(folder + "/IPrint", null);
			printerFile.openWriter().append(printerRes).close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		// Collect all the interfaces with PP
		for (Element element : env.getElementsAnnotatedWith(PP.class)) {

			// Initialization.
			TypeMirror tm = element.asType();
			String typeArgs = tm.accept(new DeclaredTypeVisitor(), element);
			String[] lTypeArgs = toList(typeArgs);

			String name = element.getSimpleName().toString();
			String res = createPPClass(folder, (TypeElement) element, lTypeArgs, typeArgs);
			// Debug String. This part seems to be correct: ppgenPythonAlg[E,
			// M]E,M
			// String res = folder + element.getSimpleName().toString() +
			// Arrays.toString(lTypeArgs) + typeArgs;

			// Not working at the moment. Null Pointer error.
			// PP pp = element.getAnnotation(PP.class);
			try {
				JavaFileObject jfo;
				jfo = filer.createSourceFile(folder + "/" + nameGenPP(name), element);
				jfo.openWriter().append(res).close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return true;
	}

	private String nameGenPP(String name) {
		return "PP" + name;
	}

	private String nameGenInterface(String name) {
		return "_" + nameGenPP(name);
	}

	private String getName(Element e) {
		return e.getSimpleName().toString();
	}

	private int getNumOfTypeParams(TypeElement e) {
		return e.getTypeParameters().size();
	}

	private String produceClassHeader(int numOfParams) {
		String s = "<IPrint";
		// Should iterate for numOfParams - 1 times.
		for (int count = 1; count < numOfParams; count++) {
			s += ", IPrint";
		}
		s += ">";
		return s;
	}

	private String createPPClass(String folder, TypeElement te, String[] lTypeArgs, String typeArgs) {
		String name = getName(te);
		int numOfTypeParams = getNumOfTypeParams(te);
		String res = "package " + folder + ";\n\n";
		res += "import " + getPackage(te) + "." + name + ";\n\n";
		res += "import " + "de.uka.ilkd.pp.*;\n";

		res += "interface " + nameGenInterface(name) + " extends " + name + produceClassHeader(numOfTypeParams)
				+ " {\n";

		// For each data type that we know to exist in the target language,
		// we'll generate the appropriate printing method in an interface. The
		// actual generation
		// is done in the method "genInterfaceMethod"

		// Here we'll now get all the elements from the parents, since we've
		// decomposed the thing.
		List<TypeElement> parents = Utils.getAllInterfaces(te);
		Set<Element> allElements = Utils.getUniqueElements(parents);

		// However it seems that we should also add the original elements in te
		// itself?... Otherwise it doesn't work.
		// Yeah, this is correct.
		List<? extends Element> le = te.getEnclosedElements();
		allElements.addAll(le);
		// This is trying to go through all single piece of grammar in the Alg
		// file.
		for (Element e : allElements) {
			String methodName = e.getSimpleName().toString();
			String[] args = { methodName, typeArgs, name };
			res += genInterfaceMethod((ExecutableElement) e, typeArgs);

			// // Debugging
			// // res += e + "\n";
			// // res += e.getAnnotation(Syntax.class) + "\n";
		}

		res += "}\n\n";

		res += "public class " + nameGenPP(name) + " implements " + nameGenInterface(name) + " {\n";

		// Here we generate the printing method for all things that need
		// overriding.
		for (Element e : allElements) {
			String methodName = e.getSimpleName().toString();
			String[] args = { methodName, typeArgs, name };
			// res += e.asType().accept(new PrintMethodVisitor(), args);
			res += genClassMethod((ExecutableElement) e, typeArgs);

			// Debugging
			// res += e + "\n";
			// res += e.getAnnotation(Syntax.class) + "\n";
		}

		res += "}\n";

		return res;
	}

	private String genInterfaceMethod(ExecutableElement e, String typeArgs) {
		// Example: typeArgs would be E, M, then the list would contain two
		// elements E and M
		String[] lTypeArgs = typeArgs.split(",");
		String[] lListTypeArgs = new String[lTypeArgs.length];

		// Example: List<E> and List<M>
		for (int listTypeArgsCount = 0; listTypeArgsCount < lTypeArgs.length; ++listTypeArgsCount) {
			lListTypeArgs[listTypeArgsCount] = "java.util.List<" + lTypeArgs[listTypeArgsCount] + ">";
		}

		String res = "";
		res += TAB + "default IPrint " + e.getSimpleName() + "(";
		// Notice that there is a possibility of this piece of grammar having
		// zero parameters.
		List<? extends VariableElement> params = e.getParameters();

		// This is the part where we create the arguments for the header of the
		// print function of this piece of grammar.
		// We go through all the parameters of this piece of grammar.
		for (int tempParamCount = 0; tempParamCount < params.size(); ++tempParamCount) {
			// In this case this parameter is List<E> or List<M>, then we'll
			// pass it in as List<IPrint>
			if (Utils.arrayContains(lListTypeArgs, params.get(tempParamCount).asType().toString()) != -1) {
				res += "java.util.List<IPrint> p" + tempParamCount;
			} else if (Utils.arrayContains(lTypeArgs, params.get(tempParamCount).asType().toString()) != -1) { // In
																												// this
																												// case
																												// it's
																												// a
																												// normal
																												// type,
																												// so
																												// it
																												// will
																												// only
																												// be
																												// one
																												// IPrint.
				res += "IPrint p" + tempParamCount;
			} else { // In the last case it's likely to be a primitive type. We
						// can just put its type directly out there.
				res += params.get(tempParamCount).asType().toString() + " p" + tempParamCount;
			}
			// This is to add the , between parameters in Java
			if (tempParamCount < params.size() - 1)
				res += ", ";
		}
		res += ") {\n";

		// Now let me also try to output what method is being currently invoked
		// to the standard output. Debugging.
		res += TAB2 + "System.out.println(\"We're currently trying to invoke default method " + e.getSimpleName()
				+ " with parameters " + e.getParameters() + "\");\n";

		// This was the beginning of the method body
		res += TAB2 + "return (Layouter<NoExceptions> pp) -> {\n";
		res += TAB3 + "pp.beginI();\n";

		// We already defined an annotation in our framework called "Syntax" for
		// each language. We're just extracting that information.
		Syntax anno = e.getAnnotation(Syntax.class);
		if (anno == null) {
			System.err.println("Warning: method without syntax/token anno: " + e);
			return "";
		}
		String syn = anno.value();
		String[] synList = syn.split(" ");

		// DEBUGGING: Trying to see how NOA produces the grammar production for
		// this piece of grammar.
		List<String> realSyms = Arrays.asList(synList).subList(2, synList.length);
		Level precAnno = e.getAnnotation(Level.class);
		int prec = ConventionsPP.MAX_PRECEDENCE;
		if (precAnno != null) {
			prec = precAnno.value();
		}
		// noa.util.NormalAlt na = new noa.util.NormalAlt(synList[0], prec,
		// e.getSimpleName().toString(), realSyms, 0);
		// noa.util.NormalAlt na = new noa.util.NormalAlt(synList[0], 100,
		// e.getSimpleName().toString(), realSyms, 0);
		NormalAlt na = new NormalAlt(synList[0], 100, e.getSimpleName().toString(), realSyms, 0);
		res += TAB3 + "// The string produced by NOA is: " + na.toString() + "\n";
		// labelCounter+=realSyms.size();

		// We start from synListCount = 2 bceause the first two
		// things are
		// `form =`, mandatory components of the annotation.
		int paramCount = 0, synListCount = 2;
		// We start processing all the symbols within the @Syntax annotation.
		while (synListCount < synList.length) {
			// // Debugging information
			res += TAB3 + "// We're currently processing symbol " + synList[synListCount] + "\n";
			String s = synList[synListCount];
			String paramName = "p" + paramCount;

			if (isEOF(s)) {
				res += "\n";
			} else if (s.equals("NEWLINE") || s.equals("NEWLINE?")) {
				res += "\n";
			} else if (s.equals("INDENT")) {
				res += TAB3 + "pp.beginI();\n";
			} else if (s.equals("DEDENT")) {
				res += TAB3 + "pp.end();\n";
			} else if (s.equals("(")) {
				// Handle the case of brackets in the grammar. e.g. 
				/*
				 * @Syntax("except_from_test = test ( 'as' NAME )?")
                 * E except_from_test(E test, String name);
				 */
				// The only possible ending is )?
				// Actually we should just do nothing during the printing it seems. If there's nothing to print then it will just print nothing? Or actually no, if there's nothing to print then we shouldn't print the symbol either... In which case the logic would be a bit more complicated.
				
				// We'll have to process the substring and see if there's actually anything to print. OK.
				
				// However this we can only determine at runtime...
//				int tempSynListCount = synListCount;
//				while (!synList[tempSynListCount].equals(")?")) {
//					tempSynListCount++;
//				}
//				// These are the symbols within ( )?
//				String[] optionalSynList = Arrays.copyOfRange(synList, synListCount+1, tempSynListCount-1);
//				// However how do we detect whether there's anything in them?... This is quite hard actually.
//				// Iterate through all those symbols, if they're not String, then see whether they're empty?
//				int tempParamCount = paramCount;
//				for (String sym : optionalSynList) {
//					if (sym.startsWith("'") && sym.endsWith("'")) {
//						continue;
//					} else if (isToken(sym)) {
//						tempParamCount++;
//						continue;
//					} else if (isNonTerminal(sym)) {
//						String tempParamName = "param" + tempParamCount;
////						res += TAB3 + "if (!(" + tempParamName + " == null || " + tempParamName + "."
//						// If we see that there is nothing to print, then we should just jump over this list. However the problem is that how should we perform the detection and jump at runtime?
//						res += TAB3 + "if ((" + tempParamName + " == null)) {\n";
//						res += TAB4 + "";
//						if (!(tempParamName == null || (Utils.arrayContains(lListTypeArgs, params.get(tempParamCount))) && tempParam))
//					}
//					
//				}
			} else if (s.equals(")?")) {
				// Do nothing
			} else if (s.equals("?")) {
				// This indicates the previous thing is optional... I think we shouldn't deal with it at all then.
			}
			else if (isNonTerminal(s)) { // NonTerminals start with lowercase
				// I think it should just correspond with the current parameter.
				// Could it be the parameter with the same id as the current
				// syn? No I don't think that's necessarily related.
				// params.get(paramCount);

				// In this case it's just one single printer argument,
				// not a
				// list.
				res += TAB3 + "if (!(" + paramName + " == null)) {\n";
				res += TAB4 + paramName + ".printLocal(pp);\n";
				res += TAB3 + "}\n";
				paramCount++;
			} else if (isRegular(s)) { // "regular" means nonTerminal+/?
				// String n = getRegularSymbol(s);
				// if (n.equals("+")) {
				//
				// } else { // else it's ?
				//
				// }
				// However I don't think being + or ? makes any difference here.
				// For the argument part it will just always be a List argument
				// of length either 1 or 0. Let's see if this assumption is
				// correct.
				// res += TAB3 + "// The type is " +
				// params.get(paramCount).asType().toString() + "\n";
				res += TAB3 + "System.out.println(\"In method + " + e.getSimpleName() + "\");\n";
				res += TAB3 + "if (!(" + paramName + " == null)) {\n";
				res += TAB4 + "System.out.println(\"The current param " + paramName + " has length: \" + " + paramName + ".size());\n";
				res += TAB3 + "}\n";
				res += TAB3 + "else {\n";
				res += TAB4 + "System.out.println(\"The current param " + paramName + " is null! \");\n";
				res += TAB3 + "}\n";
				
				res += TAB3 + "if (!(" + paramName + " == null) && !" + paramName + ".isEmpty()) {\n";
				res += TAB4 + "for (int count = 0; count < " + paramName + ".size() - 1; count++) {\n";
				res += TAB5 + paramName + ".get(count).printLocal(pp);\n";
				res += TAB5 + "pp.brk();\n";
				res += TAB4 + "}\n";
				// Print the last element of the list without printing
				// extra breaks.
				res += TAB4 + paramName + ".get(" + paramName + ".size() - 1).printLocal(pp);\n";
				res += TAB3 + "}\n";
				paramCount++;
			} else if (ConventionsPP.isTokenRegular(s)) {
				res += TAB3 + "System.out.println(\"In method + " + e.getSimpleName() + "\");\n";
				res += TAB3 + "if (!(" + paramName + " == null)) {\n";
				res += TAB4 + "System.out.println(\"The current param " + paramName + " has length: \" + " + paramName + ".size());\n";
				res += TAB3 + "}\n";
				res += TAB3 + "else {\n";
				res += TAB4 + "System.out.println(\"The current param " + paramName + " is null! \");\n";
				res += TAB3 + "}\n";
				
				res += TAB3 + "if (!(" + paramName + " == null) && !" + paramName + ".isEmpty()) {\n";
				res += TAB4 + "for (int count = 0; count < " + paramName + ".size() - 1; count++) {\n";
				String temp = "\"\" + " + paramName + ".get(count);\n";
				res += TAB5 + "pp.print(" + temp + ");\n";
				res += TAB5 + "pp.brk();\n";
				res += TAB4 + "}\n";
				// Print the last element of the list without printing
				// extra breaks.
				res += TAB4 + paramName + ".get(" + paramName + ".size() - 1).printLocal(pp);\n";
				String last = "\"\" + " + paramName + ".get(" + paramName + ".size() - 1);\n";
				res += TAB4 + "pp.print(" + last + ");\n";
				res += TAB3 + "}\n";
				paramCount++;
			}
			else if (isToken(s)) { // Token is something starting with upper
										// case. Supposedly they should all be
										// primitive types!
				// In this case it's a primitive type. We should just
				// directly print its literal representation.
				// The \"\" here is just a hack to force the param to be
				// displayed as String without having to call
				// `toString`...

				// In these types we will ask the user to
				// manually implement things. The code is in the method
				// "genClassMethod"

				// First we'll have to ensure there's actually some
				// param
				// out there. Otherwise this will be a mismatch. e.g.
				// newline
				if (!params.isEmpty()) {
					res += TAB3 + "// Please write manual printing method for this piece of grammar if necessary.\n";
					String temp = "\"\" + " + paramName;
					res += TAB3 + "pp.print(" + temp + ");\n";
					paramCount++;
				} else {
					// Should remind the user to manually write
					// something
					// here.
					// Will try to add warning later.
					res += TAB3 + "// Please write manual printing method for this piece of grammar.\n";
				}

				// We add a space unless the literal is the last one or
				// is
				// followed by )
				// However it seems there is another issue where even if
				// the previous argument is completely empty, we'd still
				// add a brk() here.
			} else if (isSepList(s)) {
				// generate seplist rules in 3 situations: token separator,
				// non-terminal separator and symbols
				String n = getSepListSymbol(s);
				// String label = labelFor(labelCounter, n);
				// String eltHead = label + "=" + n;
				// String eltTail = label + "tail+=" + n;
				String sep = getSepListToken(s);
				if (isToken(sep)) { // isToken means it's a terminal. Then it's
									// likely to be a list of strings.
					String nextParamName = "p" + (paramCount + 1);
					res += TAB3 + "if (!(" + nextParamName + " == null) && !" + paramName + ".isEmpty()) {\n";
					res += TAB4 + "for (int count = 0; count < " + nextParamName + ".size() - 1; count++) {\n";
					res += TAB5 + nextParamName + ".get(count).printLocal(pp);\n";
					res += TAB5 + "pp.brk();\n";
					res += TAB5 + "pp.print(" + paramName + ".get(count));\n";
					res += TAB5 + "pp.brk();\n";
					res += TAB4 + "}\n";
					// Print the last element of the list without printing
					// extra breaks.
					res += TAB4 + nextParamName + ".get(" + nextParamName + ".size() - 1).printLocal(pp);\n";
					res += TAB3 + "}\n";
					paramCount += 2;
				} else if (isNonTerminal(sep)) {
					// In this case the normal pattern is that the current param
					// is the list of separators, while the next param is the
					// list of things being separated.
					String nextParamName = "p" + (paramCount + 1);
					res += TAB3 + "if (!(" + nextParamName + " == null) && !" + paramName + ".isEmpty()) {\n";
					res += TAB4 + "for (int count = 0; count < " + nextParamName + ".size() - 1; count++) {\n";
					res += TAB5 + nextParamName + ".get(count).printLocal(pp);\n";
					res += TAB5 + "pp.brk();\n";
					res += TAB5 + paramName + ".get(count).printLocal(pp);\n";
					res += TAB5 + "pp.brk();\n";
					res += TAB4 + "}\n";
					// Print the last element of the list without printing
					// extra breaks.
					res += TAB4 + nextParamName + ".get(" + nextParamName + ".size() - 1).printLocal(pp);\n";
					res += TAB3 + "}\n";
					paramCount += 2;
				} else { // Else it's like to be just a symbol
							// In this case the argument itself is a list of
							// printers.
							// However actually this list can be just totally
							// empty.
							// For example (f), which just invokes the function
							// itself without any arguments. So we'll actually
							// have
							// to check this first otherwise there will be a
							// null
							// pointer exception.
				res += TAB3 + "System.out.println(\"In method + " + e.getSimpleName() + "\");\n";
				res += TAB3 + "if (!(" + paramName + " == null)) {\n";
				res += TAB4 + "System.out.println(\"The current param " + paramName + " has length: \" + " + paramName + ".size());\n";
				res += TAB3 + "}\n";
				res += TAB3 + "else {\n";
				res += TAB4 + "System.out.println(\"The current param " + paramName + " is null! \");\n";
				res += TAB3 + "}\n";
				
					res += TAB3 + "if (!(" + paramName + " == null) && !" + paramName + ".isEmpty()) {\n";
					res += TAB4 + "for (int count = 0; count < " + paramName + ".size() - 1; count++) {\n";
					res += TAB5 + paramName + ".get(count).printLocal(pp);\n";
//					if (isLiteral(sep) && sep.length() > 2 && sep.indexOf("'") != sep.lastIndexOf("'") && sep.indexOf("'") != sep.length() - 1) {
					if (isLiteral(sep)) {
						String tempSep = sep.substring(sep.indexOf("'") + 1, sep.lastIndexOf("'"));
						res += TAB5 + "pp.print(\"" + tempSep + "\");\n";
						res += TAB5 + "pp.brk();\n";
					} else if (sep.isEmpty()) {
						// If no separator, should just give a new line?
						res += TAB5 + "pp.nl();\n";
					} else {
						res += TAB5 + "pp.print(\"" + sep + "\");\n";
						res += TAB5 + "pp.brk();\n";
					}
					res += TAB4 + "}\n";
					// Print the last element of the list without printing
					// extra breaks.
					res += TAB4 + paramName + ".get(" + paramName + ".size() - 1).printLocal(pp);\n";
					res += TAB3 + "}\n";
					paramCount++;
				}
				if (isZeroOrMoreSepList(s)) {
					// Do we need to do anything?
					res += TAB3 + "// We found zeroOrMoreSepList, so?\n";
				}
			}
			
			else if (isLiteral(s)) { // Then it should really just be a keyword string, print it
						// as it is.
				String tempS = s.substring(s.indexOf("'") + 1, s.lastIndexOf("'"));
				res += TAB3 + "pp.print(" + "\"" + tempS + "\");\n";
			} else {
				// Is this a bug? Is this supposed to happen at all?
				res += TAB3 + "// We've likely encountered a bug here. What's the symbol? " + s + "\n";
				if (s.length() > 1) {
					s = s.substring(1, s.length() - 1);
				}
				res += TAB3 + "pp.print(" + "\"" + s + "\");\n";
				
			}
			// If it's not the last element we should probably add a break
			// Actually now I'm not sure whether ) is a good indicator. Let's
			// see.
//			if (!(synListCount == synList.length - 1) && !(synList[synListCount + 1].contains(")") && !(s.contains("(")))) {
			if (!(s.contains("(") || synListCount > synList.length - 2 || synList[synListCount + 1].contains(")"))) {
				res += TAB3 + "pp.brk();\n";
			}
			synListCount++;
		}
		res += TAB3 + "pp.end();\n";
		res += TAB2 + "};\n";
		res += TAB + "}\n";
		
	 /* print debugging info. */
	 res += "/* \n";
	 res += "params.size(): " + params.size() + "\n";
	 res += "Original syn: " + syn + "\n";
	 res += "Original synList: " + Arrays.toString(synList) + "\n";
	 res += "synList.length: " + synList.length + "\n";
	 res += "e.getParameters(): " + e.getParameters() + "\n";
	 for (VariableElement param : params) {
	 res += param.toString() + ": " + param.asType() + "\n";
	 }
	 res += "typeArgs: " + typeArgs + "\n";
	 res += "lListTypeArgs: ";
	 for (String t : lListTypeArgs) {
	 res += t + ", ";
	 }
	 res += "\n";
	 res += e.getAnnotation(Syntax.class).value() + "\n";
	 res += "\n */ \n\n";
		
		return res;
	}
	// Starting from this point is the old code.
	// while (synListCount < synList.length) {
	// // Debugging information
	// res += TAB3 + "// We're currently processing symbol " +
	// synList[synListCount] + "\n";
	//

	// If the symbol starts with ' then this symbols is a keyword. Still
	// added the length check to be sure.
	// However this doesn't make much sense. Why are we doing a while
	// loop? What's the meaning of this thing anyways.
	// while (synListCount < synList.length &&
	// synList[synListCount].startsWith("\'")) {
	// // substring(1, length() - 1) is to get rid of the ' ' at both
	// // ends.
	// String currentSyn = synList[synListCount].substring(1,
	// synList[synListCount].length() - 1);
	//
	// // If it's a keyword then we'll just directly print it out in
	// String form.
	// res += TAB3 + "pp.print(" + "\"" + currentSyn + "\");\n";
	// // Note a space is added after the keyword, if synListCount is
	// // not a
	// // starting parentheses or the last symbol.
	// if (!(currentSyn.contains("(") || synListCount > synList.length -
	// 2)) {
	// res += TAB3 + "pp.brk();\n";
	// }
	// synListCount++;
	// }

	// Let me rewrite it and see what happens.
	// if (synList[synListCount].startsWith("\'")) {
	// // substring(1, length() - 1) is to get rid of the ' ' at both
	// // ends.
	// String currentSyn = synList[synListCount].substring(1,
	// synList[synListCount].length() - 1);
	//
	// // If it's a keyword then we'll just directly print it out in
	// // String form.
	// res += TAB3 + "pp.print(" + "\"" + currentSyn + "\");\n";
	// // Note a space is added after the keyword, if synListCount is
	// // not a
	// // starting parentheses or the last symbol.
	// if (!(currentSyn.contains("(") || synListCount > synList.length - 2)) {
	// res += TAB3 + "pp.brk();\n";
	// }
	// // synListCount++;
	// } else { // Else it's not a keyword, we'll process it in other ways.
	// String paramName = "p" + paramCount;
	// String currentSyn = synList[synListCount];
	// // Separators will be placed after @
	// if (ConventionsPP.isSepList(currentSyn)) {
	// String separator = ConventionsPP.getSepListToken(currentSyn);
	// // We'll have to differentiate on the type of separators.
	// // "params" means the parameters for the piece of grammar.
	// // If this is a list type of argument.
	// if (!params.isEmpty()
	// && Utils.arrayContains(lListTypeArgs,
	// params.get(paramCount).asType().toString()) != -1) {
	// // In this case the argument itself is a list of
	// // printers.
	// // However actually this list can be just totally empty.
	// // For example (f), which just invokes the function
	// // itself without any arguments. So we'll actually have
	// // to check this first otherwise there will be a null
	// // pointer exception.
	// res += TAB3 + "if (!(" + paramName + " == null) && !" + paramName +
	// ".isEmpty()) {\n";
	// res += TAB4 + "for (int count = 0; count < " + paramName + ".size() - 1;
	// count++) {\n";
	// res += TAB5 + paramName + ".get(count).printLocal(pp);\n";
	// res += TAB5 + "pp.print(\"" + separator + "\");\n";
	// res += TAB5 + "pp.brk();\n";
	// res += TAB4 + "}\n";
	// // Print the last element of the list without printing
	// // extra breaks.
	// res += TAB4 + paramName + ".get(" + paramName + ".size() -
	// 1).printLocal(pp);\n";
	// res += TAB3 + "}\n";
	// } else {
	// // TODO: error: list type does not match!
	// // The error is that it claims to be a list type with @
	// // in it, but then isn't actually a list type.
	// // I think I know what the error is in this case: When
	// // the list is of primitive type, somehow the program
	// // just directly dismissed it.
	// // res += TAB3 + "// Error here. List type mismatch
	// // occurence 1.\n";
	// // res += TAB3 + "// The type is " +
	// // params.get(paramCount).asType().toString() + "\n";
	// res += TAB3 + "if (!(" + paramName + " == null) && !" + paramName +
	// ".isEmpty()) {\n";
	// res += TAB4 + "for (int count = 0; count < " + paramName + ".size() - 1;
	// count++) {\n";
	// res += TAB5 + "pp.print(" + paramName + ".get(count));\n";
	// res += TAB5 + "pp.print(\"" + separator + "\");\n";
	// res += TAB5 + "pp.brk();\n";
	// res += TAB4 + "}\n";
	// // Print the last element of the list without printing
	// // extra breaks.
	// res += TAB4 + "pp.print(" + paramName + ".get(" + paramName + ".size() -
	// 1));\n";
	// res += TAB3 + "}\n";
	// }
	// } else { // It's not a list separated by @, but it might be some
	// // other stuff out there.
	//
	// if (!params.isEmpty()
	// && Utils.arrayContains(lListTypeArgs,
	// params.get(paramCount).asType().toString()) != -1) {
	// // This is probably not an error... What was the
	// // original author thinking?
	// // This just probably means it's some other type of
	// // list, for example + or ?
	// // Let's print some debugging info about the currentSyn
	// res += TAB3 + "// The current symbol is " + currentSyn + "\n";
	// res += TAB3 + "if (!(" + paramName + " == null) && !" + paramName +
	// ".isEmpty()) {\n";
	// res += TAB4 + "for (int count = 0; count < " + paramName + ".size() - 1;
	// count++) {\n";
	// res += TAB5 + paramName + ".get(count).printLocal(pp);\n";
	// res += TAB5 + "pp.brk();\n";
	// res += TAB4 + "}\n";
	// // Print the last element of the list without printing
	// // extra breaks.
	// res += TAB3 + paramName + ".get(" + paramName + ".size() -
	// 1).printLocal(pp);\n";
	// res += TAB3 + "}\n";
	// } else if (!params.isEmpty()
	// && Utils.arrayContains(lTypeArgs,
	// params.get(paramCount).asType().toString()) != -1) {
	// // In this case it's just one single printer argument,
	// // not a
	// // list.
	// res += TAB3 + paramName + ".printLocal(pp);\n";
	// // Have to add space between parameters otherwise
	// // they'll
	// // all be crammed together.
	// // We add a break unless the param is the last one or is
	// // followed by )
	// if (!(synListCount == synList.length - 1) && !(synList[synListCount +
	// 1].contains(")"))) {
	// res += TAB3 + "pp.brk();\n";
	// }
	// } else { // int, bool, float....
	// // In this case it's a primitive type. We should just
	// // directly print its literal representation.
	// // The \"\" here is just a hack to force the param to be
	// // displayed as String without having to call
	// // `toString`...
	//
	// // In these types we will ask the user to
	// // manually implement things. The code is in the method
	// // "genClassMethod"
	//
	// // First we'll have to ensure there's actually some
	// // param
	// // out there. Otherwise this will be a mismatch. e.g.
	// // newline
	// if (!params.isEmpty()) {
	// String temp = "\"\" + " + paramName;
	// res += TAB3 + "pp.print(" + temp + ");\n";
	// } else {
	// // Should remind the user to manually write
	// // something
	// // here.
	// // Will try to add warning later.
	// res += TAB3 + "// Please write manual printing method for this piece of
	// grammar.";
	// }
	//
	// // We add a space unless the literal is the last one or
	// // is
	// // followed by )
	// // However it seems there is another issue where even if
	// // the previous argument is completely empty, we'd still
	// // add a brk() here.
	// if (!(synListCount == synList.length - 1) && !(synList[synListCount +
	// 1].contains(")"))) {
	// res += TAB3 + "pp.brk();\n";
	// }
	// }
	// }
	// // Preventative for arrayOutOfBounds error.
	// // Actually I don't think this logic is very correct. Why are we
	// // trying to increase paramCount here? Let me see what happens
	// // anyways.
	// if (paramCount < params.size() - 1) {
	// paramCount++;
	// } else { // else it means we've finished processing?
	// break;
	// }
	// }
	//
	// // It seems that the additional check is because synListCount could
	// // also be
	// // incremented inside of the while loop itself. (synListCount++)
	// // Wait this is still weird let's see if we can do better.
	// // if (synListCount < synList.length) {
	// // }
	// synListCount++;
	// }
	//
	// res += "\n";
	// res += TAB3 + "pp.end();\n";
	// res += TAB2 + "};\n";
	// res += TAB + "}\n";
	//
	// /* print debugging info. */
	// res += "/* \n";
	// res += "params.size(): " + params.size() + "\n";
	// res += "Original syn: " + syn + "\n";
	// res += "Original synList: " + Arrays.toString(synList) + "\n";
	// res += "synList.length: " + synList.length + "\n";
	// res += "e.getParameters(): " + e.getParameters() + "\n";
	// for (VariableElement param : params) {
	// res += param.toString() + ": " + param.asType() + "\n";
	// }
	// res += "typeArgs: " + typeArgs + "\n";
	// res += "lListTypeArgs: ";
	// for (String t : lListTypeArgs) {
	// res += t + ", ";
	// }
	// res += "\n";
	// res += e.getAnnotation(Syntax.class).value() + "\n";
	// res += "\n */ \n\n";
	//
	// return res;
	// }

	private String genClassMethod(ExecutableElement e, String typeArgs) {
		String[] lTypeArgs = typeArgs.split(",");
		String[] lListTypeArgs = new String[lTypeArgs.length];

		for (int listTypeArgsCount = 0; listTypeArgsCount < lTypeArgs.length; ++listTypeArgsCount) {
			lListTypeArgs[listTypeArgsCount] = "java.util.List<" + lTypeArgs[listTypeArgsCount] + ">";
		}

		String syn = e.getAnnotation(Syntax.class).value();
		// Sometimes the thing Syntax mysteriously has more than one spaces in between... \\s+ is more appropriate
		String[] synList = syn.split("\\s+");
		List<? extends VariableElement> params = e.getParameters();
		String res = "";

		// We're trying to generate class methods. Only those nodes with one
		// parameter can be a primitive type. Check it.
		// However maybe an empty params list also doesn't work?
		if (params.isEmpty() || params.size() > 1) {
			return "";
		}
		// It seems that we start from synListCount = 2 bceause the first two
		// things are
		// `form =`, mandatory components of the annotation.
		int paramCount = 0, synListCount = 2;
		while (synListCount < synList.length) {
			// If the symbol starts with ' then this symbols is a keyword.
			// However it seems that we won't need this thing in overriding
			// methods. So just ignore it, except for incrementing synListCount
			while (synListCount < synList.length && synList[synListCount].startsWith("\'")) {
				// Removed old code. Actually this part shouldn't be here
				// because a primitive type is not supposed to have more than
				// one symbols in the list.
				synListCount++;
			}
			// It seems that the additional check is because synListCount could
			// also be
			// incremented inside of the while loop itself. (synListCount++)
			if (synListCount < synList.length) {
				String paramName = "p" + paramCount;
				String str = synList[synListCount];
				// So "@" indicates the place where separators are to appear
				// following it.
				if (str.contains("@")) {
					// Just deleted all the code that was here.
				}

				if (!params.isEmpty()
						&& Utils.arrayContains(lListTypeArgs, params.get(paramCount).asType().toString()) != -1) {
					// Just deleted all the code that was here.
				} else if (!params.isEmpty()
						&& Utils.arrayContains(lTypeArgs, params.get(paramCount).asType().toString()) != -1) {
				} else { // int, bool, float....
					// In this case it's a primitive type. We'll ask the user to
					// override the printing method here.
					res += TAB + "@Override\n";
					res += TAB + "public IPrint " + e.getSimpleName() + "(";

					// It's giving some errors. So comment it out first.
					// String message = "The method " + e.getSimpleName()
					// + " prints a primitive type and should be manually
					// overridden";
					// myEnv.getMessager().printMessage(Diagnostic.Kind.NOTE,
					// message);
					// Determine the correct Java type of the parameter to be
					// fed into this
					// printing method
					for (int tempParamCount = 0; tempParamCount < params.size(); ++tempParamCount) {
						if (Utils.arrayContains(lListTypeArgs, params.get(tempParamCount).asType().toString()) != -1) {
							res += "java.util.List<IPrint> p" + tempParamCount;
						} else if (Utils.arrayContains(lTypeArgs,
								params.get(tempParamCount).asType().toString()) != -1) {
							res += "IPrint p" + tempParamCount;
						} else {
							// Have to add break between parameters otherwise
							// they'll all be
							// crammed together.
							res += params.get(tempParamCount).asType().toString() + " p" + tempParamCount;
						}
						if (tempParamCount < params.size() - 1)
							res += ", ";
					}
					res += ") {\n";
					// Now let me also try to output what method is being
					// currently invoked to the standard output.

					res += TAB2 + "System.out.println(\"We're currently trying to invoke overridden method "
							+ e.getSimpleName() + " with parameters " + e.getParameters() + "\");\n";
					res += TAB2 + "return (Layouter<NoExceptions> pp) -> {\n";
					res += TAB3 + "pp.beginI();\n";

					// First we'll have to ensure there's actually some param
					// out there. Otherwise this will be a mismatch.
					if (!params.isEmpty()) {
						String temp = "\"\" + " + paramName;
						res += TAB3 + "pp.print(" + temp + ");\n";
					} else {
						// Should remind the user to manually write something
						// here.
						// Will try to add warning later.
						res += TAB3 + "// Please write manual printing method for this piece of grammar.";
					}

					// We add a space unless the literal is the last one or is
					// followed by )
					if (!(synListCount == synList.length - 1) && !(synList[synListCount + 1].contains(")"))) {
						res += TAB3 + "pp.brk();\n";
					}
					res += TAB3 + "pp.end();\n";
					res += TAB2 + "};\n";
					res += TAB + "}\n";

				}
				// Preventative for arrayOutOfBounds error.
				if (paramCount < params.size() - 1) {
					paramCount++;
				}
				synListCount++;
			}
		}

		return res;

	}

	// We'll use a better version of this stuff.
	private String getSeparator(String str) {
		// getSeparator( "exp@','+" ) ---> ","
		int i = str.indexOf("@");
		// Note that if the thing is of form '', then it uses space as separator
		// by default and we shouldn't add anything extra.
		if (str.substring(i + 1, i + 3).equals("''")) {
			return "";
		} else {
			return str.substring(i + 2, i + 3);
		}
	}

	@Override
	public SourceVersion getSupportedSourceVersion() {
		return SourceVersion.latestSupported();
	}

	private String getPackage(Element element) {
		return ((PackageElement) element.getEnclosingElement()).getQualifiedName().toString();
	}

}
