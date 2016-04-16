package anno;

public interface ConventionsPP {
    static final String BUILDER_FIELD = "builder";
    static final int MAX_PRECEDENCE = Integer.MAX_VALUE;

    static boolean hasPlaceholder(String s) {
        return s.startsWith("_");
    }

    static boolean isEOF(String s) {
        return s.equals("EOF");
    }

    static boolean isNEWLINE(String s) {
        return s.equals("NEWLINE");
    }

    static boolean isToken(String s) {
        return s.matches("^[A-Z][a-zA-Z_]*$");
    }
    
    static boolean isTokenRegular(String s) {
        return s.matches("^[A-Z][a-zA-Z_]*[*+?]$");
    }

    static boolean isSepToken(String s) {
        return s.matches("^[a-zA-Z_]*$");
    }

    static boolean isLiteral(String op) {
        return op.matches("^'.*'[*+?]?$");
    }

    static boolean isNonTerminal(String s) {
        return s.matches("^[a-z][a-zA-Z_]*$");
    }

    static boolean isRegular(String s) {
        return s.matches("^[a-z][a-zA-Z_]*[*+?]$");
    }

    static String getRegularSymbol(String s) {
        return s.substring(0, s.length() - 1);
    }

    static boolean isSepList(String s) {
        return s.matches("^[a-z][a-zA-Z_]*@'.+'*[*+]$");
    }

    static boolean isZeroOrMoreSepList(String s) {
        return s.charAt(s.length() - 1) == '*';
    }

    static boolean isOneOrMoreSepList(String s) {
        return s.charAt(s.length() - 1) == '+';
    }

    static String getSepListSymbol(String s) {
        return s.substring(0, s.indexOf('@'));
    }

    // support non-terminal separator
    static String getSepListToken(String s) {
        String returnValue = s.substring(s.indexOf('@') + 1, s.length() - 1);
        if (returnValue.equals("''")) {
            return "";
        } else {
            String innerContent = returnValue.substring(1, returnValue.length() - 1);
            if (!isToken(innerContent) && !isNonTerminal(innerContent)) {
                if (innerContent.charAt(0) == '\'' && innerContent.charAt(innerContent.length() - 1) == '\'') {
                    return innerContent;
                } else {
                    return returnValue;
                }
            } else {
                return innerContent;
            }
        }
    }

    static String labelFor(int n, String sym) {
        return sym + "_" + n;
    }

    static String returnVariable(String nt) {
        return "_" + nt;
    }

}
