// This file should be generated automatically in the folder /generated. This is the handwritten version for testing purpose.
package ppTestMum;

import ppTestMum.MumAlg;

import de.uka.ilkd.pp.*;
interface _PPMumAlg extends MumAlg<IPrint, IPrint> {

  public static final int DEFAULT_LINE_WIDTH = 20;
  public static final int DEFAULT_INDENTATION = 2;

  abstract StringBackend back();
  abstract Layouter<NoExceptions> pp();
  default IPrint ifNode(IPrint p0, IPrint p1, IPrint p2) {
    return () -> {
      pp().beginI();
      pp().print("(");
      pp().print("if");
      pp().brk();
      p0.print();
      pp().brk();
      p1.print();
      pp().brk();
      p2.print();
      pp().print(")");

      pp().end();
    };
  }
/*
params.size(): 3
Original syn: form = '(' 'if' form form form ')'
Original synList: [form, =, '(', 'if', form, form, form, ')']
synList.length: 8
e.getParameters(): [e1, e2, e3]
e1: E
e2: E
e3: E
typeArgs: P,E
lListTypeArgs: java.util.List<P>, java.util.List<E>,
form = '(' 'if' form form form ')'

 */

  default IPrint defineNode(java.lang.String p0, IPrint p1) {
    return () -> {
      pp().beginI();
      pp().print("(");
      pp().print("define");
      pp().brk();
      pp().print("" + p0);
      pp().brk();
      p1.print();
      pp().print(")");

      pp().end();
    };
  }
/*
params.size(): 2
Original syn: form = '(' 'define' SYMBOL form ')'
Original synList: [form, =, '(', 'define', SYMBOL, form, ')']
synList.length: 7
e.getParameters(): [slot, e]
slot: java.lang.String
e: E
typeArgs: P,E
lListTypeArgs: java.util.List<P>, java.util.List<E>,
form = '(' 'define' SYMBOL form ')'

 */

  default IPrint quoteNode(IPrint p0) {
    return () -> {
      pp().beginI();
      pp().print("(");
      pp().print("quote");
      pp().brk();
      p0.print();
      pp().print(")");

      pp().end();
    };
  }
/*
params.size(): 1
Original syn: form = '(' 'quote' form ')'
Original synList: [form, =, '(', 'quote', form, ')']
synList.length: 6
e.getParameters(): [literalNode]
literalNode: E
typeArgs: P,E
lListTypeArgs: java.util.List<P>, java.util.List<E>,
form = '(' 'quote' form ')'

 */

  default IPrint lambdaNode(java.util.List<IPrint> p0, java.util.List<IPrint> p1) {
    return () -> {
      pp().beginI();
      pp().print("(");
      pp().print("lambda");
      pp().brk();
      pp().print("(");
      for (int count = 0; count < p0.size() - 1; count++) {
        p0.get(count).print();
        pp().print("");
        pp().brk();
      }
      p0.get(p0.size() - 1).print();
      pp().print(")");
      pp().brk();
      for (int count = 0; count < p1.size() - 1; count++) {
        p1.get(count).print();
        pp().print("");
        pp().brk();
      }
      p1.get(p1.size() - 1).print();
      pp().print(")");

      pp().end();
    };
  }
/*
params.size(): 2
Original syn: form = '(' 'lambda' '(' form@''* ')' form@''+ ')'
Original synList: [form, =, '(', 'lambda', '(', form@''*, ')', form@''+, ')']
synList.length: 9
e.getParameters(): [args, rtns]
args: java.util.List<E>
rtns: java.util.List<E>
typeArgs: P,E
lListTypeArgs: java.util.List<P>, java.util.List<E>,
form = '(' 'lambda' '(' form@''* ')' form@''+ ')'

 */

  default IPrint listNode(java.util.List<IPrint> p0) {
    return () -> {
      pp().beginI();
      pp().print("(");
      for (int count = 0; count < p0.size() - 1; count++) {
        p0.get(count).print();
        pp().print("");
        pp().brk();
      }
      p0.get(p0.size() - 1).print();
      pp().print(")");

      pp().end();
    };
  }
/*
params.size(): 1
Original syn: form = '(' form@''+ ')'
Original synList: [form, =, '(', form@''+, ')']
synList.length: 5
e.getParameters(): [args]
args: java.util.List<E>
typeArgs: P,E
lListTypeArgs: java.util.List<P>, java.util.List<E>,
form = '(' form@''+ ')'

 */

//	default IPrint booleanNode(boolean p0) {
//		return () -> {
//			pp().beginI();
//			pp().print("" + p0);
//
//			pp().end();
//		};
//	}
//	public abstract IPrint booleanNode (boolean p0);
  default IPrint booleanNode(boolean p0) {
    return () -> {
      pp().beginI();
      pp().print("" + p0);

      pp().end();
    };
  }
/*
params.size(): 1
Original syn: form = BOOL
Original synList: [form, =, BOOL]
synList.length: 3
e.getParameters(): [x]
x: boolean
typeArgs: P,E
lListTypeArgs: java.util.List<P>, java.util.List<E>,
form = BOOL

 */

  default IPrint longNode(java.lang.Object p0) {
    return () -> {
      pp().beginI();
      pp().print("" + p0);

      pp().end();
    };
  }
/*
params.size(): 1
Original syn: form = NUM
Original synList: [form, =, NUM]
synList.length: 3
e.getParameters(): [x]
x: java.lang.Object
typeArgs: P,E
lListTypeArgs: java.util.List<P>, java.util.List<E>,
form = NUM

 */

  default IPrint mumblerSymbol(java.lang.String p0) {
    return () -> {
      pp().beginI();
      pp().print("" + p0);

      pp().end();
    };
  }
/*
params.size(): 1
Original syn: form = SYMBOL
Original synList: [form, =, SYMBOL]
synList.length: 3
e.getParameters(): [symbolName]
symbolName: java.lang.String
typeArgs: P,E
lListTypeArgs: java.util.List<P>, java.util.List<E>,
form = SYMBOL

 */

  default IPrint stringNode(java.lang.String p0) {
    return () -> {
      pp().beginI();
      pp().print("" + p0);

      pp().end();
    };
  }
/*
params.size(): 1
Original syn: form = STRING
Original synList: [form, =, STRING]
synList.length: 3
e.getParameters(): [x]
x: java.lang.String
typeArgs: P,E
lListTypeArgs: java.util.List<P>, java.util.List<E>,
form = STRING

 */

  default IPrint start(java.util.List<IPrint> p0) {
    return () -> {
      pp().beginI();
      for (int count = 0; count < p0.size() - 1; count++) {
        p0.get(count).print();
        pp().print("");
        pp().brk();
      }
      p0.get(p0.size() - 1).print();

      pp().end();
    };
  }
/*
params.size(): 1
Original syn: file = form@''+
Original synList: [file, =, form@''+]
synList.length: 3
e.getParameters(): [es]
es: java.util.List<E>
typeArgs: P,E
lListTypeArgs: java.util.List<P>, java.util.List<E>,
file = form@''+

 */

  default IPrint invokeNode(java.lang.String p0, java.util.List<IPrint> p1) {
    return () -> {
      pp().beginI();
      pp().print("(");
      pp().print("" + p0);
      pp().brk();
      for (int count = 0; count < p1.size() - 1; count++) {
        p1.get(count).print();
        pp().print("");
        pp().brk();
      }
      p1.get(p1.size() - 1).print();
      pp().print(")");

      pp().end();
    };
  }
/*
params.size(): 2
Original syn: form = '(' SYMBOL form@''* ')'
Original synList: [form, =, '(', SYMBOL, form@''*, ')']
synList.length: 6
e.getParameters(): [func, args]
func: java.lang.String
args: java.util.List<E>
typeArgs: P,E
lListTypeArgs: java.util.List<P>, java.util.List<E>,
form = '(' SYMBOL form@''* ')'

 */

}

public class PPMumAlg implements _PPMumAlg {
  @Override
  public IPrint booleanNode(boolean x) {
	  String temp;
	  if (x == false) {
		  temp = "#f";
	  } else {
		  temp = "#t";
	  }
    return () -> {
      pp().beginI();
      pp().print("" + temp);

      pp().end();
    };
  }
  StringBackend back = new StringBackend(DEFAULT_LINE_WIDTH);
  Layouter<NoExceptions> pp = new Layouter<NoExceptions>(back, DEFAULT_INDENTATION);

	@Override
	public StringBackend back() {
    return back;
	}

	@Override
	public Layouter<NoExceptions> pp() {
    return pp;
	}
}
