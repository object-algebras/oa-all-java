package noa.util;

import static noa.util.Conventions.returnVariable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Rules implements Conventions {
    private Map<String, List<Alt>> rules;
    private String name;
    private String pkg;
    private Class<?> tokens;
    private Class<?> signature;

    public Rules(String name, String pkg, Class<?> tokens, Class<?> signature) {
        this.name = name;
        this.pkg = pkg;
        this.tokens = tokens;
        this.signature = signature;
        this.rules = new HashMap<String, List<Alt>>();
    }

    public void addAlt(Alt a) {
        if (!rules.containsKey(a.getNT())) {
            rules.put(a.getNT(), new ArrayList<>());
        }
        rules.get(a.getNT()).add(a);
    }

    public void groupByLevel() {
        for (String nt : rules.keySet()) {
            rules.put(nt, groupByLevel(rules.get(nt)));
        }
    }

    private List<Alt> groupByLevel(List<Alt> alts) {
        Map<Integer, List<Alt>> leveled = new HashMap<>();
        for (Alt a : alts) {
            if (!leveled.containsKey(a.getLevel())) {
                leveled.put(a.getLevel(), new ArrayList<>());
            }
            leveled.get(a.getLevel()).add(a);
        }

        for (Integer level : leveled.keySet()) {
            if (level != MAX_PRECEDENCE && leveled.get(level).size() > 1) {
                collapseLevel(leveled, level);
            }
        }

        return sortAlternatives(leveled);
    }

    private void collapseLevel(Map<Integer, List<Alt>> leveled, Integer level) {
        NormalAlt last = null;
        Map<String, String> map = new HashMap<>();
        for (Alt ia : leveled.get(level)) {
            NormalAlt a = (NormalAlt) ia;
            assertValidInfix(last, a);
            last = a;
            map.put(a.getOperator(), a.getCons());
        }
        leveled.put(level, Arrays.asList(new InfixAlt(last.getNT(), level, map)));
    }

    private List<Alt> sortAlternatives(Map<Integer, List<Alt>> leveled) {
        List<Alt> all = new ArrayList<>();
        for (Integer level : leveled.keySet()) {
            all.addAll(leveled.get(level));
        }
        Alt[] array = all.toArray(new Alt[] {});
        Arrays.sort(array);
        return Arrays.asList(array);
    }

    private void assertValidInfix(NormalAlt last, NormalAlt a) {
        if (!a.isInfix()) {
            throw new RuntimeException("Cannot have non-infix prods at same level of precedence");
        }
        if (last != null) {
            if (!last.getLhs().equals(a.getLhs()) || !last.getRhs().equals(a.getRhs())) {
                throw new RuntimeException("Infix prods at same level should have same lhs and rhs");
            }
        }
        if (!a.getLhs().equals(a.getNT()) || !a.getRhs().equals(a.getNT())) {
            throw new RuntimeException("Lhs/rhs must be same as result non-terminal");
        }
    }

    // change the grammar name to the name of the class
    public void generate(StringBuilder sb) {
        sb.append("grammar " + name + ";\n");
        sb.append("tokens {INDENT, DEDENT}\n");
        addHeader(sb);
        addLexerMembers(sb);
        addParserMembers(sb);

        for (String nt : rules.keySet()) {
            sb.append(nt + " returns [Object " + returnVariable(nt) + "]:\n");
            List<Alt> ntAlts = rules.get(nt);
            int numOfAlts = ntAlts.size();
            for (int i = 0; i < numOfAlts; i++) {

                // TODO: Test
                // System.out.println(ntAlts.get(i).toString());

                if (i != 0) {
                    sb.append("  | ");
                } else {
                    sb.append("    ");
                }
                sb.append(ntAlts.get(i) + "\n");
            }
            sb.append("  ;\n\n");
        }
    }

    private void addParserMembers(StringBuilder sb) {
        sb.append("@parser::members{\n");
        sb.append("private " + signature.getName() + " " + BUILDER_FIELD + ";\n");
        sb.append("public void setBuilder(" + signature.getName() + " " + BUILDER_FIELD + ") { this." + BUILDER_FIELD
                + " = " + BUILDER_FIELD + "; }\n");

        addLiftMethod(sb);

        sb.append("}\n\n");
    }

    private void addLexerMembers(StringBuilder sb) {
        sb.append("@lexer::members{\n");
        addPythonLexerMembers(sb);
        sb.append("}\n\n");
    }

    // some functions that are needed in python antlr rules
    private void addPythonLexerMembers(StringBuilder sb) {
        sb.append("private java.util.LinkedList<Token> tokens = new java.util.LinkedList<>();\n");
        sb.append("private java.util.Stack<Integer> indents = new java.util.Stack<>();\n");
        sb.append("private int opened = 0;\n");
        sb.append("private Token lastToken = null;\n");
        sb.append("\n@Override\n");
        sb.append("public void emit(Token t) {\n");
        sb.append("  super.setToken(t);\n");
        sb.append("  tokens.offer(t);\n");
        sb.append("}\n");
        sb.append("\n@Override\n");
        sb.append("public Token nextToken() {\n");
        sb.append("  if (_input.LA(1) == EOF && !this.indents.isEmpty()) {\n");
        sb.append("    for (int i = tokens.size() - 1; i >= 0; i--) {\n");
        sb.append("      if (tokens.get(i).getType() == EOF) {\n");
        sb.append("        tokens.remove(i);\n");
        sb.append("      }\n");
        sb.append("    }\n");
        sb.append("    this.emit(commonToken("+name+"Parser.NEWLINE, \"\\n\"));\n");
        sb.append("    while (!indents.isEmpty()) {\n");
        sb.append("      this.emit(createDedent());\n");
        sb.append("      indents.pop();\n");
        sb.append("    }\n");
        sb.append("    this.emit(commonToken("+name+"Parser.EOF, \"<EOF>\"));\n");
        sb.append("  }\n");
        sb.append("  Token next = super.nextToken();\n");
        sb.append("  if (next.getChannel() == Token.DEFAULT_CHANNEL) {\n");
        sb.append("    this.lastToken = next;\n");
        sb.append("  }\n");
        sb.append("  return tokens.isEmpty() ? next : tokens.poll();\n");
        sb.append("}\n");
        sb.append("\nprivate Token createDedent() {\n");
        sb.append("  CommonToken dedent = commonToken("+name+"Parser.DEDENT, \"\");\n");
        sb.append("  dedent.setLine(this.lastToken.getLine());\n");
        sb.append("  return dedent;\n");
        sb.append("}\n");
        sb.append("\nprivate CommonToken commonToken(int type, String text) {\n");
        sb.append("  int stop = this.getCharIndex() - 1;\n");
        sb.append("  int start = text.isEmpty() ? stop : stop - text.length() + 1;\n");
        sb.append("  return new CommonToken(this._tokenFactorySourcePair, type, DEFAULT_TOKEN_CHANNEL, start, stop);\n");
        sb.append("}\n");
        sb.append("\nstatic int getIndentationCount(String spaces) {\n");
        sb.append("  int count = 0;\n");
        sb.append("  for (char ch : spaces.toCharArray()) {\n");
        sb.append("    switch (ch) {\n");
        sb.append("      case '\\t':\n");
        sb.append("        count += 8 - (count % 8);\n");
        sb.append("        break;\n");
        sb.append("      default:\n");
        sb.append("        count++;\n");
        sb.append("    }\n");
        sb.append("  }\n");
        sb.append("  return count;\n");
        sb.append("}\n");
        sb.append("\nboolean atStartOfInput() {\n");
        sb.append("  return super.getCharPositionInLine() == 0 && super.getLine() == 1;\n");
        sb.append("}\n");
    }

    private void addLiftMethod(StringBuilder sb) {
        sb.append("private static <X> java.util.List<X> lift(String name, java.util.List<?> ctxs, X ...heads) {\n");
        sb.append("  java.util.List<X> l = new java.util.ArrayList<X>();\n");
        sb.append("  for (X h: heads) { l.add(h); }\n");
        sb.append("  for (Object ctx: ctxs) {\n");
        sb.append("    try {\n");
        sb.append("      l.add((X)ctx.getClass().getField(name).get(ctx));\n");
        sb.append("    } catch (Throwable e) {\n");
        sb.append("      throw new RuntimeException(e);\n");
        sb.append("    }\n");
        sb.append("  }\n");
        sb.append("  return l;\n");
        sb.append("}\n");

        sb.append("private static <X> java.util.List<X> lift(String name, java.util.List<?> ctxs) {\n");
        sb.append("  java.util.List<X> l = new java.util.ArrayList<X>();\n");
        sb.append("  for (Object ctx: ctxs) {\n");
        sb.append("    try {\n");
        sb.append("      l.add((X)ctx.getClass().getField(name).get(ctx));\n");
        sb.append("    } catch (Throwable e) {\n");
        sb.append("      throw new RuntimeException(e);\n");
        sb.append("    }\n");
        sb.append("  }\n");
        sb.append("  return l;\n");
        sb.append("}\n");

        sb.append("private static java.util.List<String> liftString(java.util.List<?> ctxs) {\n");
        sb.append("  java.util.List<String> l = new java.util.ArrayList<String>();\n");
        sb.append("  for (Object ctx: ctxs) {\n");
        sb.append("    try {\n");
        sb.append("      l.add(((Token)ctx).getText());\n");
        sb.append("    } catch (Throwable e) {\n");
        sb.append("      throw new RuntimeException(e);\n");
        sb.append("    }\n");
        sb.append("  }\n");
        sb.append("  return l;\n");
        sb.append("}\n");
    }

    private void addHeader(StringBuilder sb) {
        sb.append("@header{\n");
        sb.append("package " + pkg + ";\n");
        sb.append("import static " + tokens.getName() + ".*;\n");
        sb.append("}\n");
    }

}
