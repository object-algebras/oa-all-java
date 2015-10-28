package anno;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.type.TypeMirror;
import javax.tools.JavaFileObject;

import anno.utils.AnnoUtils;

import java.io.IOException;
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

		for (int i = 0; i < lTypeArgs.length; ++i) {
			lListTypeArgs[i] = "java.util.List<" + lTypeArgs[i] + ">";
		}

		String res = "";
		// String is to say the thing we print shall of course be a string.
		res += TAB + "public String " + e.getSimpleName() + "(";
		List<? extends VariableElement> params = e.getParameters();
		// Determine the correct Java type of the parameter to be fed into this printing method
		for (int i = 0; i < params.size(); ++i) {
			if (AnnoUtils.arrayContains(lListTypeArgs, params.get(i).asType()
					.toString()) != -1) {
				res += "java.util.List<String> p" + i;
			} else if (AnnoUtils.arrayContains(lTypeArgs, params.get(i)
					.asType().toString()) != -1) {
				res += "String p" + i;
			} else {
				// Have to add space between parameters otherwise they'll all be crammed together.
				res += params.get(i).asType().toString() + " p" + i;
			}
			if (i < params.size() - 1)
				res += ", ";
		}
		res += ") {\n";
		res += TAB2 + "return ";

		// We already defined an annotation in our framework called "Syntax" for each language. We're just extracting that information.
		String syn = e.getAnnotation(Syntax.class).value();
		String[] synList = syn.split(" ");

		// i is used to record which parameter we're currently trying to print. (ird)
		// The "synList" is to say we separate different symbols in the one line of notation. j is to record which one among the list we're currently printing.
		
		// It seems that we start from j = 2 bceause the first two things are `form =`, mandatory components of the annotation.
		int i = 0, j = 2;
		while (j < synList.length) {
			// If the symbol starts with ' then this symbols is a keyword.
			while (j < synList.length && synList[j].startsWith("\'")) {
				// substring(1, length() - 1) is to get rid of the ' ' at both ends.
				// The \" is because we want to print keywords literally in the final printed text.
				res += "\"" + synList[j].substring(1, synList[j].length() - 1)
						+ "\"";
				j++;
				if (j < synList.length)
					// Just the string concatenator
					res += " + ";
			}
			// It seems that the additional check is because j could also be incremented inside of the while loop itself. (j++)
			if (j < synList.length) {
				String paramName = "p" + i;
				String str = synList[j];
				// So "@" indicates the place where separators are to appear following it.
				if (str.contains("@")) {
					String separator = getSeparator(synList[j]);
					if (AnnoUtils.arrayContains(lListTypeArgs, params.get(i)
							.asType().toString()) != -1) {
						// In this case the argument itself is a list. Thus we use String.join to join various arguments together.
						// Note a space is added after each separator and before the next param in the list.
						res += "String.join(\"" + separator + " \", "
								+ paramName + ")";
					} else {
						// TODO: error: list type does not match!
					}
				}

				if (AnnoUtils.arrayContains(lListTypeArgs, params.get(i)
						.asType().toString()) != -1) {
					// TODO: error: list type does not match!
				} else if (AnnoUtils.arrayContains(lTypeArgs, params.get(i)
						.asType().toString()) != -1) {
					// In this case it's just one single argument, not a list.
					// Have to add space between parameters otherwise they'll all be crammed together.
					res += paramName;
				} else { // int, bool, float....
					// In this case it's a primitive type. We should just directly print its literal representation.
					res += "\"\" + " + paramName;
				}
				i++;
				j++;
				if (j < synList.length)
					// This means we should still have other symbols in the syntax definition. Conncet them with a +
					res += " + ";
			}
		}

		res += ";\n";
		res += TAB + "}\n";

		/* print debugging info. */
		res += "/* \n";
		res += "params.size(): " + params.size() + "\n";
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
