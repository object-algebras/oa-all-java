package annotation;

import java.util.List;

import javax.lang.model.type.*;

public class ReflTypeVisitor implements TypeVisitor<String, String[]> {
	
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
		for (int i = 0; i < lTypeArgs.length; ++i){
			lListTypeArgs[i] = "java.util.List<" + lTypeArgs[i] + ">";
		}
		
		List<? extends TypeMirror> lp = t.getParameterTypes();
		String returnType = t.getReturnType().toString();
		
		String res = "\tpublic ";
		
		// Make sense.?
		if (arrayContains(lListTypeArgs, returnType) != -1){
			res += "java.util.List<E> ";
		} else if (arrayContains(lTypeArgs, returnType) != -1){
			res += "E ";
		} else res += returnType + " ";
		
		res += methodName + "(";
		
		
		for (int i = 0; i < lp.size(); ++i){
			// contains a list of type variables
			if (arrayContains(lListTypeArgs, lp.get(i).toString()) != -1){
				res += "java.util.List<E> p" + i;
			} else if (arrayContains(lTypeArgs, lp.get(i).toString()) != -1){
				res += "E p" + i;
			} else {
				res += lp.get(i).toString() + " p" + i;
			}
			if (i < lp.size()-1) res += ", ";
		}
		
		res += ") {\n";
		res += "\t\tList<E> l = new ArrayList<E>();\n";
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
		        res += "\t\tl.add(p" + i + ");\n";
			} else if (lp.get(i).toString().equals("int")) {
			    res += "\t\tl.add(alg().KInt(p" + i + "));\n";
			} else if (lp.get(i).toString().equals("boolean")) {
                res += "\t\tl.add(alg().KBool(p" + i + "));\n";
            } else if (lp.get(i).toString().equals("java.lang.String")) {
                res += "\t\tl.add(alg().KString(p" + i + "));\n";
            }
		}
        res += "\t\treturn alg().Cons(\"" + methodName + "\", l);\n";
		res += "\t}\n\n";
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