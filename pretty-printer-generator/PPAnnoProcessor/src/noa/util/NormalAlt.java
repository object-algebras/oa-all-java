package noa.util;

import static noa.util.Conventions.getRegularSymbol;
import static noa.util.Conventions.getSepListSymbol;
import static noa.util.Conventions.getSepListToken;
import static noa.util.Conventions.isEOF;
import static noa.util.Conventions.isLiteral;
import static noa.util.Conventions.isNonTerminal;
import static noa.util.Conventions.isRegular;
import static noa.util.Conventions.isSepList;
import static noa.util.Conventions.isToken;
import static noa.util.Conventions.isZeroOrMoreSepList;
import static noa.util.Conventions.labelFor;
import static noa.util.Conventions.returnVariable;

import java.util.List;

public class NormalAlt extends Alt implements Conventions {

    private List<String> syms;
    private String cons;
    private int labelCounter;

    public NormalAlt(String nt, int prec, String cons, List<String> syms, int labelCounter) {
        super(nt, prec);
        this.cons = cons;
        this.syms = syms;
        this.labelCounter = labelCounter;
    }

    public boolean isInfix() {
        String op = syms.get(1);
        return syms.size() == 3 && (isToken(op) || isLiteral(op));
    }

    public String getOperator() {
        assert isInfix();
        return syms.get(1);
    }

    public String getLhs() {
        assert isInfix();
        return syms.get(0);
    }

    public String getRhs() {
        assert isInfix();
        return syms.get(2);
    }

    private boolean isNEWLINE(String s) {
        return s.equals("NEWLINE");
    }

    public String toString() {
        String prod = "";
        String args = "";
        // int labelCounter = 0;

        for (String s : syms) {
            // add some judgement on null
            if (isEOF(s)) {
                prod += s;
            } else if (s.equals("NEWLINE") | s.equals("INDENT") | s.equals("DEDENT")) {
                prod += s;
            } else if (isNonTerminal(s)) {
                prod += labelFor(labelCounter, s) + "=" + s + " ";
                args += "($" + labelFor(labelCounter, s) + ".ctx==null?" + "null:" + "($" + labelFor(labelCounter, s)
                        + "." + returnVariable(s) + ")),";
                labelCounter += 1;
            } else if (isRegular(s)) {
                String n = getRegularSymbol(s);
                if (n.equals("+")) {
                    prod += labelFor(labelCounter, n) + "+=" + s.substring(0, s.length() - 1) + " ";
                    args += args += "lift(\"" + returnVariable(n) + "\", $" + labelFor(labelCounter, n) + "),";
                } else {
                    prod += labelFor(labelCounter, n) + "+=" + s + " ";
                    args += "lift(\"" + returnVariable(n) + "\", $" + labelFor(labelCounter, n) + "),";
                }
                labelCounter += 1;
            } else if (isToken(s)) {
                prod += labelFor(labelCounter, s) + "=" + s + " ";
                args += s.toLowerCase() + "($" + labelFor(labelCounter, s) + ".text),";
                labelCounter += 1;
            } else if (isSepList(s)) {
                // generate seplist rules in 3 situations: token separator, non-terminal separator and symbols
                String n = getSepListSymbol(s);
                String label = labelFor(labelCounter, n);
                String eltHead = label + "=" + n;
                String eltTail = label + "tail+=" + n;
                String sep = getSepListToken(s);
                if (isToken(sep)) {
                    prod += "(" + eltHead + " (" + labelFor(labelCounter, sep) + "+=" + sep + " " + eltTail + ")*)";
                    args += "liftString($" + labelFor(labelCounter, sep) + "==null? null :$" +labelFor(labelCounter, sep) + "), " + "($" + label + ".ctx==null||" + "$" + label + "tail==null)?" + " null : (lift(\""
                            + returnVariable(n) + "\", $" + label + "tail, " + "$" + label + "." + returnVariable(n)
                            + ")),";
                } else if (isNonTerminal(sep)) {
                    prod += "(" + eltHead + " (" + labelFor(labelCounter, sep) + "+=" + sep + " " + eltTail + ")*)";
                    args += " ($" +labelFor(labelCounter, sep) + "==null? " + "null : lift(\"" + returnVariable(sep) + "\", $" + labelFor(labelCounter, sep) + ") ), " + "($" + label + ".ctx==null||" + "$" + label + "tail==null)?" + " null : (lift(\""
                            + returnVariable(n) + "\", $" + label + "tail, " + "$" + label + "." + returnVariable(n)
                            + ")),"; 
                } else {
                    prod += "(" + eltHead + " (" + sep + " " + eltTail + ")*)";
                    args += " ($" + label + ".ctx==null||" + "$" + label + "tail==null)?" + " null : (lift(\""
                            + returnVariable(n) + "\", $" + label + "tail, " + "$" + label + "." + returnVariable(n)
                            + ")),";
                }
                if (isZeroOrMoreSepList(s)) {
                    prod += "?";
                }
                // args += "lift(\"" + returnVariable(n) + "\", $" + label +
                // "tail, " + "$" + label + "."
                // + returnVariable(n) + "),";
                labelCounter += 1;  
            } else {
                prod += s + " ";
                labelCounter += 1;
            }
        }
        if (!args.isEmpty()) {
            // remove trailing comma
            args = args.substring(0, args.length() - 1);
        }
        // if (syms.size() == 0)
        // prod += "{}";
        // else
        prod += " {$" + returnVariable(getNT()) + " = " + BUILDER_FIELD + "." + cons + "(" + args + ");}";
        return prod;
    }

    public String getCons() {
        return cons;
    }

}
