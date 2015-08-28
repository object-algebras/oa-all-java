package annotation;

import java.util.List;

import javax.lang.model.type.*;

public class GenTypeVisitor implements TypeVisitor<String, String[]> {

    public static final String TAB = "\t";
    public static final String TAB2 = "\t\t";
    public static final String TAB3 = "\t\t\t";
    public static final String TAB4 = "\t\t\t\t";
    
	int arrayContains(String[] ls, String s) {
		int i = 0;
		for (String ts: ls) {
			if (s.equals(ts)) return i;
			i++;
		}
		return -1;
	}
	
	@Override
	public String visitExecutable(ExecutableType t, String[] p) {
		String methodName = p[0];
		String[] lTypeArgs = p[1].split(",");
		String[] lListTypeArgs = new String[lTypeArgs.length];
		String algName = p[2];
		for (int i = 0; i < lTypeArgs.length; ++i){
			lListTypeArgs[i] = "java.util.List<" + lTypeArgs[i] + ">";
		}
		
		List<? extends TypeMirror> lp = t.getParameterTypes();
		String returnType = t.getReturnType().toString();
		
		String res = "\tpublic Type " + methodName + "(";
		
		for (int i = 0; i < lp.size(); ++i){
			// contains a list of type variables
			if (arrayContains(lListTypeArgs, lp.get(i).toString()) != -1){
				res += "java.util.List<E> p" + i;
			} else if (arrayContains(lTypeArgs, lp.get(i).toString()) != -1){
				res += "Type p" + i;
			} else {
				res += lp.get(i).toString() + " p" + i;
			}
			if (i < lp.size()-1) res += ", ";
		}
		
		res += ") {\n";
		res += TAB2 + "return new Type() {\n"
		        + TAB3 + "public <E> E accept(" + algName + "<E> alg) { \n"
		        + TAB4 + "return alg." + methodName + "(";
		boolean firstArg = true;
		for (int i = 0; i < lp.size(); ++i){
		    
		    //----------- debug code begin --------------
//		    res += "==========" + methodName + "============\n"
//		        + "lp.get(i): " + lp.get(i) + "\n"
//		        + "lp.get(i).getClass(): " + lp.get(i).getClass() + "\n"
//		        + "lp.get(i).getKind(): " + lp.get(i).getKind() + "\n"
//		        + "lp.get(i).getKind().name(): " + lp.get(i).getKind().name() + "\n"
//		        + "lp.get(i).getKind().isPrimitive(): " + lp.get(i).getKind().isPrimitive() + "\n\n"; 
		    //----------- debug code end --------------                
		    
		    if (arrayContains(lTypeArgs, lp.get(i).toString()) != -1){
		        res += (firstArg? "":", ") + "p" + i + ".accept(alg)";
		        firstArg = false;
			} else if (lp.get(i).getKind().isPrimitive()) {
			    res += (firstArg? "":", ") + "p" + i;
                firstArg = false;
			} else if (lp.get(i).toString().equals("java.lang.String")) {
                res += (firstArg? "":", ") + "p" + i;
                firstArg = false;
            }
		}
		res += ");\n" + 
		        TAB3 + "}\n" +
		        TAB2 + "};\n" + 
		        TAB + "}\n\n";
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