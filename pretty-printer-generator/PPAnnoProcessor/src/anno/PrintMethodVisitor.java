package anno;

import java.util.List;

import javax.lang.model.type.*;

public class PrintMethodVisitor implements TypeVisitor<String, String[]> {

    public static final String TAB = "\t";
    public static final String TAB2 = "\t\t";
    public static final String TAB3 = "\t\t\t";
    public static final String TAB4 = "\t\t\t\t";

    @Override
    public String visitExecutable(ExecutableType t, String[] p) {
	String methodName = p[0];
	String[] lTypeArgs = p[1].split(",");
	String[] lListTypeArgs = new String[lTypeArgs.length];

	for (int i = 0; i < lTypeArgs.length; ++i) {
	    lListTypeArgs[i] = "java.util.List<" + lTypeArgs[i] + ">";
	}

	List<? extends TypeMirror> lp = t.getParameterTypes();

	String res = "\tpublic String " + methodName + "(";

	for (int i = 0; i < lp.size(); ++i) {
	    // contains a list of type variables
	    if (Utils.arrayContains(lListTypeArgs, lp.get(i).toString()) != -1) {
		res += "java.util.List<String> p" + i;
	    } else if (Utils.arrayContains(lTypeArgs, lp.get(i).toString()) != -1) {
		res += "String p" + i;
	    } else {
		res += lp.get(i).toString() + " p" + i;
	    }
	    if (i < lp.size() - 1)
		res += ", ";
	}

	res += ") {\n";
	res += TAB2 + "return \"\";\n";
	res += TAB + "}\n";
	
	/*  debugging  */
	res += "/* \n";
	res += "methodName: " + methodName + "\n";
	res += "ExecutableType t: " + t.toString() + "\n";

	// res += "t.getAnnotation(Syntax.class): "
	// + t.getAnnotationsByType(Syntax.class);
	// Syntax syn = t.getAnnotation(Syntax.class);
	// if (syn != null) {
	// res += syn.toString() + ":" + syn.value() + "\n";
	// } else {
	// res += "no Syntax annotation!\n";
	// }

	res += "\n */ \n";
	
	
	return res;
    }

    @Override
    public String visit(TypeMirror t, String[] p) {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public String visit(TypeMirror t) {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public String visitPrimitive(PrimitiveType t, String[] p) {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public String visitNull(NullType t, String[] p) {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public String visitArray(ArrayType t, String[] p) {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public String visitDeclared(DeclaredType t, String[] p) {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public String visitError(ErrorType t, String[] p) {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public String visitTypeVariable(TypeVariable t, String[] p) {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public String visitWildcard(WildcardType t, String[] p) {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public String visitNoType(NoType t, String[] p) {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public String visitUnknown(TypeMirror t, String[] p) {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public String visitUnion(UnionType t, String[] p) {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public String visitIntersection(IntersectionType t, String[] p) {
	// TODO Auto-generated method stub
	return null;
    }
}