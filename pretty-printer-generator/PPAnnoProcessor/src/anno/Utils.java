package anno;

import java.io.IOException;
import java.util.*;

import javax.annotation.processing.Filer;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.tools.JavaFileObject;

public class Utils {
    public static int arrayContains(String[] ls, String s) {
        int i = 0;
        for (String ts : ls) {
            if (s.equals(ts))
                return i;
            i++;
        }
        return -1;
    }
    public static void createSource(Filer filer, String name, Element element, String res) {
        JavaFileObject jfo = null;
        try {
            jfo = filer.createSourceFile(name, element);
            jfo.openWriter().append(res).close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }
    // get all unique elements in the classes/interfaces
    public static Set<Element> getUniqueElements(List<TypeElement> parents) {
        Set<Element> allElements = new HashSet<Element>();
        for (TypeElement te : parents) {
            List<? extends Element> enclosedElems = te.getEnclosedElements();
            allElements.addAll(enclosedElems);
        }
        return allElements;
    }

    // get all parents recursively
    public static List<TypeElement> getAllInterfaces(TypeElement element) {
        @SuppressWarnings("unchecked")
        List<DeclaredType> tmp = (List<DeclaredType>) element.getInterfaces();
        List<TypeElement> result = new ArrayList<TypeElement>();
        for (DeclaredType tm : tmp) {
            TypeElement te = (TypeElement) ((DeclaredType) tm).asElement();
            result.add(te);
            result.addAll(getAllInterfaces(te));
        }
        return result;
    }
}
