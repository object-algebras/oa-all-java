package annotation;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.tools.JavaFileObject;

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
            classContent = createReflClass(folder, element, lTypeArgs, typeArgs);
            jfo = null;
            try{
                jfo = filer.createSourceFile(folder + "/" + "Refl" + algName, element);
                jfo.openWriter().append(classContent).close();
            }catch(IOException ioe){
                ioe.printStackTrace();
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
        String classContent = "package " + folder + ";\n\n"
                + "import " + getPackage(element) + "." + algName + ";\n\n" 
                + "public final class " + algGenName + " implements " + algName + "<" + algGenName + ".Type> {\n";
        classContent += TAB + "public interface Type {\n" +
                TAB2 + "<E> E accept(" + algName + "<E> alg);\n" +
                TAB + "}\n\n";
        List<? extends Element> le = element.getEnclosedElements();
        for (Element e: le){
            String methodName = e.getSimpleName().toString();
            String[] args = {methodName, typeArgs, algName};
            
            //----------- debug code begin --------------
//            classContent += "Element e: " + e + "\n";
//            classContent += "e.asType(): " + e.asType().toString() + "\n";
//            classContent += "typeArgs: " + typeArgs + "\n";
//            classContent += "lTypeArgs: " + lTypeArgs.toString() + "\n"; 
            //----------- debug code end --------------    
            
            
            classContent += e.asType().accept(new GenTypeVisitor(), args);
        }
        classContent += "}";
        return classContent;
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
