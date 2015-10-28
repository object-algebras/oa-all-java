package anno;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.type.TypeMirror;
import javax.tools.JavaFileObject;

import anno.utils.AnnoUtils;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

@SupportedAnnotationTypes(value = { "anno.PP" })
@SupportedSourceVersion(SourceVersion.RELEASE_6)
public class PPProcessor extends AbstractProcessor {
	public static final String TAB = "\t";
	public static final String TAB2 = "\t\t";
	public static final String TAB3 = "\t\t\t";
	public static final String TAB4 = "\t\t\t\t";

	private Filer filer;

	@Override
	public void init(ProcessingEnvironment env) {
		filer = env.getFiler();
	}

	private String[] toList(String message) {
		return message.split(",");
	}

	@Override
	public boolean process(Set<? extends TypeElement> annotations,
			RoundEnvironment env) {

		String folder = "ppgen";
		// Collect all the interfaces with PP
		for (Element element : env.getElementsAnnotatedWith(PP.class)) {
			// Initialization.
			TypeMirror tm = element.asType();
			String typeArgs = tm.accept(new DeclaredTypeVisitor(), element);
			String[] lTypeArgs = toList(typeArgs);

			String name = element.getSimpleName().toString();
			String res = createPPClass(folder, (TypeElement) element,
					lTypeArgs, typeArgs);

			try {
				JavaFileObject jfo;
				jfo = filer.createSourceFile(folder + "/" + nameGenPP(name),
						element);
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

	private String getName(Element e) {
		return e.getSimpleName().toString();
	}

	private int getNumOfTypeParams(TypeElement e) {
		return e.getTypeParameters().size();
	}

	private String produceClassHeader(int numOfParams) {
		String s = "<String";
		// Should iterate for numOfParams - 1 times.
		for (int count = 1; count < numOfParams; count++) {
			s += ", String";
		}
		s += ">";
		return s;
	}

	private String createPPClass(String folder, TypeElement te,
			String[] lTypeArgs, String typeArgs) {
		// This part is the headers.
		// Example:
		/*
		 * package ppgen; import pptest.ExpAlg; public class PPExpAlg implements
		 * ExpAlg<String> {
		 */
		String name = getName(te);
		int numOfTypeParams = getNumOfTypeParams(te);
		String res = "package " + folder + ";\n\n";
		res += "import " + getPackage(te) + "." + name + ";\n\n";
		res += "public class " + nameGenPP(name) + " implements " + name
				+ produceClassHeader(numOfTypeParams) + " {\n";

		// For each data type that we know to exist in the target language,
		// we'll generate the appropriate printing method. The actual generation
		// is done in the method "genPrintMethod"
		List<? extends Element> le = te.getEnclosedElements();
		for (Element e : le) {
			String methodName = e.getSimpleName().toString();
			String[] args = { methodName, typeArgs, name };
			// res += e.asType().accept(new PrintMethodVisitor(), args);
			res += genPrintMethod((ExecutableElement) e, typeArgs);
		}

		res += "}";
		return res;
	}

	private String genPrintMethod(ExecutableElement e, String typeArgs) {
		String[] lTypeArgs = typeArgs.split(",");
		String[] lListTypeArgs = new String[lTypeArgs.length];

		for (int listTypeArgsCount = 0; listTypeArgsCount < lTypeArgs.length; ++listTypeArgsCount) {
			lListTypeArgs[listTypeArgsCount] = "java.util.List<"
					+ lTypeArgs[listTypeArgsCount] + ">";
		}

		String res = "";
		// String is to say the thing we print shall of course be a string.
		res += TAB + "public String " + e.getSimpleName() + "(";
		List<? extends VariableElement> params = e.getParameters();
		// Determine the correct Java type of the parameter to be fed into this
		// printing method
		for (int tempParamCount = 0; tempParamCount < params.size(); ++tempParamCount) {
			if (AnnoUtils.arrayContains(lListTypeArgs, params.get(tempParamCount).asType()
					.toString()) != -1) {
				res += "java.util.List<String> p" + tempParamCount;
			} else if (AnnoUtils.arrayContains(lTypeArgs, params.get(tempParamCount)
					.asType().toString()) != -1) {
				res += "String p" + tempParamCount;
			} else {
				// Have to add space between parameters otherwise they'll all be
				// crammed together.
				res += params.get(tempParamCount).asType().toString() + " p" + tempParamCount;
			}
			if (tempParamCount < params.size() - 1)
				res += ", ";
		}
		res += ") {\n";
		res += TAB2 + "return ";

		// We already defined an annotation in our framework called "Syntax" for
		// each language. We're just extracting that information.
		String syn = e.getAnnotation(Syntax.class).value();
		String[] synList = syn.split(" ");

		// This i is used to record which parameter we're currently trying to
		// print. (ird)
		// The "synList" is to say we separate different symbols in the one line
		// of notation. synListCount is to record which one among the list we're currently
		// printing.

		// It seems that we start from synListCount = 2 bceause the first two things are
		// `form =`, mandatory components of the annotation.
		int paramCount = 0, synListCount = 2;
		while (synListCount < synList.length) {
			// If the symbol starts with ' then this symbols is a keyword.
			while (synListCount < synList.length && synList[synListCount].startsWith("\'")) {
				// substring(1, length() - 1) is to get rid of the ' ' at both
				// ends.
				String currentSyn = synList[synListCount].substring(1,
						synList[synListCount].length() - 1);
				// The \" is because we want to print keywords literally in the
				// final printed text.
				res += "\"" + currentSyn;
				// Note a space is added after the keyword, if synListCount is not a
				// starting parentheses or the last symbol.
				if (currentSyn.contains("(") || synListCount > synList.length - 2) {
					res += "\"";
				} else {

					res += " \"";
				}
				synListCount++;
				if (synListCount < synList.length)
					// Just the string concatenator
					res += " + ";
			}
			// It seems that the additional check is because synListCount could also be
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
					if (AnnoUtils.arrayContains(lListTypeArgs, params.get(paramCount)
							.asType().toString()) != -1) {
						// In this case the argument itself is a list. Thus we
						// use String.join to join various arguments together.
						// Note a space is added after each separator and before
						// the next param in the list.
						res += "String.join(\"" + separator + " \", "
								+ paramName + ")";
					} else {
						// TODO: error: list type does not match!
						// res += "Error here. List type mismatch occurence 1.";
					}
				}

				if (AnnoUtils.arrayContains(lListTypeArgs, params.get(paramCount)
						.asType().toString()) != -1) {
					// TODO: error: list type does not match!
					// res += "Error here. List type mismatch occurence 2.";
				} else if (AnnoUtils.arrayContains(lTypeArgs, params.get(paramCount)
						.asType().toString()) != -1) {
					// In this case it's just one single argument, not a list.
					res += paramName;
					// Have to add space between parameters otherwise they'll
					// all be crammed together.
					// We add a space unless the param is the last one or is
					// followed by )
					if (!(synListCount == synList.length - 1)
							&& !(synList[synListCount + 1].contains(")"))) {
						// The concatenation operator in Java.
						res += " + ";
						// This is a literal space.
						res += " \" \" ";
					}
				} else { // int, bool, float....
					// In this case it's a primitive type. We should just
					// directly print its literal representation.
					// The \"\" here is just a hack to force the param to be
					// displayed as String without having to call `toString`...
					res += "\"\" + " + paramName;

					// We add a space unless the literal is the last one or is
					// followed by )
					if (!(synListCount == synList.length - 1)
							&& !(synList[synListCount + 1].contains(")"))) {
						// The concatenation operator in Java.
						res += " + ";
						// This is a literal space.
						res += " \" \" ";
					}
				}
        // Preventative for arrayOutOfBounds error.
				if (paramCount < params.size() - 1) {
					paramCount++;
				}
				synListCount++;
				if (synListCount < synList.length)
					// This means we should still have other symbols in the
					// syntax definition. Conncet them with a +
					res += " + ";
			}
		}

		res += ";\n";
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

	private String getSeparator(String str) {
		// getSeparator( "exp@','+" ) ---> ","
		int i = str.indexOf("@");
		return str.substring(i + 2, i + 3);
	}

	@Override
	public SourceVersion getSupportedSourceVersion() {
		return SourceVersion.latestSupported();
	}

	private String getPackage(Element element) {
		return ((PackageElement) element.getEnclosingElement())
				.getQualifiedName().toString();
	}

}
