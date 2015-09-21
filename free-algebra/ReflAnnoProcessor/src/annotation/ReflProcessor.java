package annotation;

import java.io.IOException;
import java.util.*;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.type.TypeMirror;
import javax.tools.JavaFileObject;
import annotation.utils.Utils;

@SupportedAnnotationTypes(value={"annotation.Refl"})
@SupportedSourceVersion(SourceVersion.RELEASE_6)
public class ReflProcessor extends AbstractProcessor {
    private Filer filer;
    public static final String TAB = "\t";
    public static final String TAB2 = "\t\t";
    
    @Override
    public void init(ProcessingEnvironment env){
        filer = env.getFiler();
    }
    
 
    @Override
    public boolean process(Set<? extends TypeElement> annotations,
            RoundEnvironment env) {
        String folder = null;
        String classContent = null;
        String algName;
        JavaFileObject jfo = null;
        for (Element element: env.getElementsAnnotatedWith(Refl.class)) {            
            // Initialization.
            TypeMirror tm = element.asType();
            String typeArgs = tm.accept(new DeclaredTypeVisitor(), element);
            String[] lTypeArgs = toList(typeArgs);
            algName = element.getSimpleName().toString();
            
            // Create Reflective Algebra classes "ReflAlgName". E.g. ExpAlg -> ReflExpAlg
            // One issue here. Using "java.util.List" instead of "List".
            folder = "refl";

            if (lTypeArgs.length < 2) { //TODO: deal with multiple type arguments later.
                classContent = createReflClass(folder, element, lTypeArgs, typeArgs);
                jfo = null;
                try {
                    jfo = filer.createSourceFile(folder + "/" + "Refl" + algName, element);
                    jfo.openWriter().append(classContent).close();
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }
            }
            
            // Create Generated Free Algebra classes "AlgNameGen". E.g. ExpAlg -> ExpAlgGen
            classContent = createGenClass(folder, element, lTypeArgs, typeArgs);
            jfo = null;
            try{
                jfo = filer.createSourceFile(folder + "/" + algName + "Gen", element);
                jfo.openWriter().append(classContent).close();
            }catch(IOException ioe){
                ioe.printStackTrace();
            }
        }
        return true;        
    }
    
    String createReflClass(String folder, Element element, String[] lTypeArgs, String typeArgs) {
        String algName = element.getSimpleName().toString();
        String classContent = "package " + folder + ";\n\n"
                + "import java.util.List;\n"
                + "import java.util.ArrayList;\n"
                + "import library.ReflAlg;\n"
                + "import " + getPackage(element) + "." + algName + ";\n\n" 
                + "public abstract class " + "Refl" + algName + "<E> implements " + algName + "<E> {\n\n";
        classContent += TAB + "public abstract ReflAlg<E> alg();\n\n";
        List<? extends Element> le = element.getEnclosedElements();
        for (Element e: le){
            String methodName = e.getSimpleName().toString();
            String[] args = {methodName, typeArgs};
            classContent += e.asType().accept(new ReflTypeVisitor(), args);
        }
        classContent += "}";
        return classContent;
    }
    
    String createGenClass(String folder, Element element, String[] lTypeArgs, String typeArgs) {
        String algName = element.getSimpleName().toString();
        String algGenName = algName + "Gen";
        String[] lTypeArgsType = new String[lTypeArgs.length];
        for (int i = 0; i < lTypeArgs.length; i++) {
            lTypeArgsType[i] = "Type" + lTypeArgs[i];
        }
        String sortString = "";
        for (int i = 0; i < lTypeArgsType.length; ++i) {
            sortString += algGenName + "." + lTypeArgsType[i];
            if (i != lTypeArgsType.length - 1)
                sortString += " ,";
        }
        String res = "package " + folder + ";\n\n"
                + "import " + getPackage(element) + "." + algName + ";\n\n" 
                + "public final class " + algGenName + " implements " + algName + "<" + sortString + "> {\n";

        for (int i = 0; i < lTypeArgsType.length; ++i) {
            res += TAB + "public interface " + lTypeArgsType[i] + " {\n" +
                    TAB2 + "<" + typeArgs + "> " + lTypeArgs[i] + " accept(" + algName + "<" + typeArgs + "> alg);\n" +
                    TAB + "}\n\n";
        }

        List<? extends ExecutableElement> le = (List<? extends ExecutableElement>) element.getEnclosedElements();
        for (ExecutableElement e : le) {
            String mName = e.getSimpleName().toString();
            //            String[] args = { mName, typeArgs, algName };
            String[] lListTypeArgs = new String[lTypeArgs.length];
            String mReturnType = e.getReturnType().toString();
            for (int i = 0; i < lTypeArgs.length; ++i) {
                lListTypeArgs[i] = "java.util.List<" + lTypeArgs[i] + ">";
            }
            
            List<? extends VariableElement> lp = e.getParameters();

            res += "\tpublic " + type2GenType(mReturnType, lTypeArgs, lListTypeArgs, lTypeArgsType) + " " + mName + "(";

            for (int k = 0; k < lp.size(); k++) {
                VariableElement p = lp.get(k);
                String genType = type2GenType(p.asType().toString(), lTypeArgs, lListTypeArgs, lTypeArgsType);
                res += genType + " p" + k;
                if (k < lp.size() - 1)
                    res += ", ";
            }

            res += ") {\n";
            res += "\t\treturn new " + type2GenType(mReturnType, lTypeArgs, lListTypeArgs, lTypeArgsType) + "() {\n";
            res += "\t\t\tpublic <" + typeArgs + "> " + mReturnType + " accept(" + algName + "<" + typeArgs
                    + "> alg) {\n";
            res += "\t\t\t\t\treturn alg." + mName + "(";

            boolean firstArg = true;
            for (int k = 0; k < lp.size(); ++k) {
                VariableElement p = lp.get(k);
                //----------- debug code begin --------------
                //              res += "==========" + methodName + "============\n"
                //                  + "lp.get(i): " + lp.get(i) + "\n"
                //                  + "lp.get(i).getClass(): " + lp.get(i).getClass() + "\n"
                //                  + "lp.get(i).getKind(): " + lp.get(i).getKind() + "\n"
                //                  + "lp.get(i).getKind().name(): " + lp.get(i).getKind().name() + "\n"
                //                  + "lp.get(i).getKind().isPrimitive(): " + lp.get(i).getKind().isPrimitive() + "\n\n"; 
                //----------- debug code end --------------                

                if (Utils.arrayContains(lTypeArgs, p.asType().toString()) != -1) {
                    res += (firstArg ? "" : ", ") + "p" + k + ".accept(alg)";
                    firstArg = false;
                } else if (p.asType().getKind().isPrimitive()) {
                    res += (firstArg ? "" : ", ") + "p" + k;
                    firstArg = false;
                } else if (p.asType().toString().equals("java.lang.String")) {
                    res += (firstArg ? "" : ", ") + "p" + k;
                    firstArg = false;
                }
            }

            res += ");\n";
            res += "\t\t\t}\n";
            res += "\t\t};\n";
            res += "\t}\n";

            //----------- debug code begin --------------
            //            classContent += "/* \n";
            //            classContent += "Element e: " + e + "\n";
            //            classContent += "e.asType(): " + e.asType().toString() + "\n";
            //            classContent += "typeArgs: " + typeArgs + "\n";
            //            classContent += "lTypeArgs: " + lTypeArgs.toString() + "\n";
            //            classContent += "*/ \n";
            //----------- debug code end --------------    
            
            
            //            classContent += e.asType().accept(new GenTypeVisitor(), args);

        }
        res += "}";
        return res;
    }
    
    private String type2GenType(String type, String[] lTypeArgs, String[] lListTypeArgs, String[] lTypeArgsType) {
        int i = Utils.arrayContains(lListTypeArgs, type);
        int j = Utils.arrayContains(lTypeArgs, type);
        String genType = type;
        if (i != -1) {
            genType = lListTypeArgs[i];
        } else if (j != -1) {
            genType = lTypeArgsType[j];
        }
        return genType;
    }
    
    private String[] toList(String message) {
        return message.split(",");
    }

    @Override
    public SourceVersion getSupportedSourceVersion(){
        return SourceVersion.latestSupported();
    }
    
    private String getPackage(Element element) {
        return ((PackageElement)element.getEnclosingElement()).getQualifiedName().toString();
    }
}
