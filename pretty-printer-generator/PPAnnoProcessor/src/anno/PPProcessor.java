package anno;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.type.TypeMirror;
import javax.tools.JavaFileObject;

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

//import noa.annos.*;
//import noa.PGen;

// This is just an example of adding custom warning.

//@SupportedAnnotationTypes("fully.qualified.name.of.InternalAnnotationType")
//@SupportedSourceVersion(SourceVersion.RELEASE_6)
//public class CustomAnnotationProcessor extends AbstractProcessor {
//
//    @Override
//    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
//        for (Element element : roundEnv.getElementsAnnotatedWith(InternalAnnotationType.class)) {
//            InternalAnnotationType internalAnnotation = element.getAnnotation(InternalAnnotationType.class);
//            String message = "The method " + element.getSimpleName()
//                       + " is marked internal and its use is discouraged";
//            processingEnv.getMessager().printMessage(Diagnostic.Kind.WARNING, message);
//        }
//        return true;
//    }
//}

@SupportedAnnotationTypes(value = { "anno.PP" })
@SupportedSourceVersion(SourceVersion.RELEASE_6)
public class PPProcessor extends AbstractProcessor {
	public static final String TAB = "\t";
	public static final String TAB2 = "\t\t";
	public static final String TAB3 = "\t\t\t";
	public static final String TAB4 = "\t\t\t\t";

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

			// Debugging
			// res += e + "\n";
			// res += e.getAnnotation(Syntax.class) + "\n";
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
		List<? extends VariableElement> params = e.getParameters();

		// Determine the correct Java type of the parameter to be fed into this
		// printing method
		for (int tempParamCount = 0; tempParamCount < params.size(); ++tempParamCount) {
			if (Utils.arrayContains(lListTypeArgs, params.get(tempParamCount).asType().toString()) != -1) {
				res += "java.util.List<IPrint> p" + tempParamCount;
			} else if (Utils.arrayContains(lTypeArgs, params.get(tempParamCount).asType().toString()) != -1) {
				res += "IPrint p" + tempParamCount;
			} else {
				// Have to add break between parameters otherwise they'll all be
				// crammed together.
				res += params.get(tempParamCount).asType().toString() + " p" + tempParamCount;
			}
			if (tempParamCount < params.size() - 1)
				res += ", ";
		}
		res += ") {\n";
		// Now let me also try to output what method is being currently invoked
		// to the standard output.
		res += TAB2 + "System.out.println(\"We're currently trying to invoke default method " + e.getSimpleName() + " with parameters " + e.getParameters()
				+ "\");\n";

		// This was the beginning of returning a string.
		res += TAB2 + "return (Layouter<NoExceptions> pp) -> {\n";
		res += TAB3 + "pp.beginI();\n";

		// We already defined an annotation in our framework called "Syntax" for
		// each language. We're just extracting that information.
		String syn = e.getAnnotation(Syntax.class).value();
		String[] synList = syn.split(" ");

		// It seems that we start from synListCount = 2 bceause the first two
		// things are
		// `form =`, mandatory components of the annotation.
		int paramCount = 0, synListCount = 2;
		while (synListCount < synList.length) {
			// If the symbol starts with ' then this symbols is a keyword.
			while (synListCount < synList.length && synList[synListCount].startsWith("\'")) {
				// substring(1, length() - 1) is to get rid of the ' ' at both
				// ends.
				String currentSyn = synList[synListCount].substring(1, synList[synListCount].length() - 1);

				res += TAB3 + "pp.print(" + "\"" + currentSyn + "\");\n";
				// Note a space is added after the keyword, if synListCount is
				// not a
				// starting parentheses or the last symbol.
				if (!(currentSyn.contains("(") || synListCount > synList.length - 2)) {
					res += TAB3 + "pp.brk();\n";
				}
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
					String separator = getSeparator(synList[synListCount]);
					// Here the arrayOutOfBounds error is thrown for function
					// invocation of Mumbler.
					// However for some reason this sometimes still fails for a
					// list of length 0? Check params empty?
					if (!params.isEmpty()
							&& Utils.arrayContains(lListTypeArgs, params.get(paramCount).asType().toString()) != -1) {
						// In this case the argument itself is a list of
						// printers.
						res += TAB3 + "for (int count = 0; count < " + paramName + ".size() - 1; count++) {\n";
						res += TAB4 + paramName + ".get(count).printLocal(pp);\n";
						res += TAB4 + "pp.print(\"" + separator + "\");\n";
						res += TAB4 + "pp.brk();\n";
						res += TAB3 + "}\n";
						// Print the last element of the list without printing
						// extra breaks.

						// However for some reason this sometimes still fails
						// for a list of length 0? Check params empty?
						res += TAB3 + "if (!" + paramName + ".isEmpty()) {\n";
						res += TAB4 + paramName + ".get(" + paramName + ".size() - 1).printLocal(pp);\n";
						res += TAB3 + "};\n";
					} else {
						// TODO: error: list type does not match!
						// res += "Error here. List type mismatch occurence 1.";
					}
				}

				// Currently we skip all the types that are not primitive types.
				// We had an EmptyList exception here. The only possible cause
				// can only be the params being empty.
				if (!params.isEmpty()
						&& Utils.arrayContains(lListTypeArgs, params.get(paramCount).asType().toString()) != -1) {
					// TODO: error: list type does not match!
					// res += "Error here. List type mismatch occurence 2.";
				} else if (!params.isEmpty()
						&& Utils.arrayContains(lTypeArgs, params.get(paramCount).asType().toString()) != -1) {
					// In this case it's just one single printer argument, not a
					// list.
					res += TAB3 + paramName + ".printLocal(pp);\n";
					// Have to add space between parameters otherwise they'll
					// all be crammed together.
					// We add a break unless the param is the last one or is
					// followed by )
					if (!(synListCount == synList.length - 1) && !(synList[synListCount + 1].contains(")"))) {
						res += TAB3 + "pp.brk();\n";
					}
				} else { // int, bool, float....
					// In this case it's a primitive type. We should just
					// directly print its literal representation.
					// The \"\" here is just a hack to force the param to be
					// displayed as String without having to call `toString`...

					// In these types we will ask the user to
					// manually implement things. The code is in the method
					// "genClassMethod"

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
				}
				// Preventative for arrayOutOfBounds error.
				if (paramCount < params.size() - 1) {
					paramCount++;
				}
				synListCount++;
			}
		}

		res += "\n";
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

	private String genClassMethod(ExecutableElement e, String typeArgs) {
		String[] lTypeArgs = typeArgs.split(",");
		String[] lListTypeArgs = new String[lTypeArgs.length];

		for (int listTypeArgsCount = 0; listTypeArgsCount < lTypeArgs.length; ++listTypeArgsCount) {
			lListTypeArgs[listTypeArgsCount] = "java.util.List<" + lTypeArgs[listTypeArgsCount] + ">";
		}

		String syn = e.getAnnotation(Syntax.class).value();
		String[] synList = syn.split(" ");
		List<? extends VariableElement> params = e.getParameters();
		String res = "";

		// Only those nodes with one parameter can be a primitive type. Check
		// it.
		if (params.size() > 1) {
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
					
					res += TAB2 + "System.out.println(\"We're currently trying to invoke overridden method " + e.getSimpleName() + " with parameters " + e.getParameters()
				+ "\");\n";
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