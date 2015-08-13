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

@SupportedAnnotationTypes(value={"anno.PP"})
@SupportedSourceVersion(SourceVersion.RELEASE_6)
public class PPProcessor extends AbstractProcessor {
    public static final String TAB = "\t";
    public static final String TAB2 = "\t\t";
    public static final String TAB3 = "\t\t\t";
    public static final String TAB4 = "\t\t\t\t";

    private Filer filer;

    @Override
    public void init(ProcessingEnvironment env){
	filer = env.getFiler();
    }

    private String[] toList(String message) {
	return message.split(",");
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations,
	    RoundEnvironment env) {

	String folder = "ppgen";
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

    private String createPPClass(String folder, TypeElement te,
	    String[] lTypeArgs, String typeArgs) {
	String name = getName(te);
	String res = "package " + folder + ";\n\n";
	res += "import " + getPackage(te) + "." + name + ";\n\n";
	res += "public class " + nameGenPP(name) + " implements " + name
		+ "<String> {\n";

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
	res += TAB + "public String " + e.getSimpleName() + "(";
	List<? extends VariableElement> params = e.getParameters();
	for (int i = 0; i < params.size(); ++i) {
	    if (AnnoUtils.arrayContains(lListTypeArgs, params.get(i).asType()
		    .toString()) != -1) {
		res += "java.util.List<String> p" + i;
	    } else if (AnnoUtils.arrayContains(lTypeArgs, params.get(i)
		    .asType().toString()) != -1) {
		res += "String p" + i;
	    } else {
		res += params.get(i).asType().toString() + " p" + i;
	    }
	    if (i < params.size() - 1)
		res += ", ";
	}
	res += ") {\n";
	res += TAB2 + "return ";

	String syn = e.getAnnotation(Syntax.class).value();
	String[] synList = syn.split(" ");

	int i = 0, j = 2;
	while (j < synList.length) {
	    while (j < synList.length && synList[j].startsWith("\'")) {
		res += "\"" + synList[j].substring(1, synList[j].length() - 1)
			+ "\"";
		j++;
		if (j < synList.length)
		    res += " + ";
	    }
	    if (j < synList.length) {
		String paramName = "p" + i;
		String str = synList[j];
		if (str.contains("@")) {
		    String separator = getSeparator(synList[j]);
		    if (AnnoUtils.arrayContains(lListTypeArgs, params.get(i)
			    .asType().toString()) != -1) {
			res += "String.join(\"" + separator + "\", "
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
		    res += paramName;
		} else { // int, bool, float....
		    res += "\"\" + " + paramName;
		}
		i++;
		j++;
		if (j < synList.length)
		    res += " + ";
	    }
	}

	res += ";\n";
	res += TAB + "}\n";

	/* print debugging info */
	res += "/* \n";
	res += "params.size(): " + params.size();
	res += "synList.length: " + synList.length;
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
	res += "\n */ \n";
	return res;
    }

    private String getSeparator(String str) {
	// getSeparator( "exp@','+" ) ---> ","
	int i = str.indexOf("@");
	return str.substring(i + 2, i + 3);
    }

    @Override
    public SourceVersion getSupportedSourceVersion(){
	return SourceVersion.latestSupported();
    }

    private String getPackage(Element element) {
	return ((PackageElement) element.getEnclosingElement())
		.getQualifiedName().toString();
    }

}

