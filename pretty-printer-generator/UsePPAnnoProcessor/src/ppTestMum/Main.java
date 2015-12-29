package ppTestMum;

import java.util.*;

import ppgen.PPMumAlg;
import ppgen.IPrint;
import anno.*;

public class Main {

	static <P, E> E makeBool(MumAlg<P, E> alg) {
		return alg.booleanNode(false);
	}

	static <P, E> E makeLong(MumAlg<P, E> alg) {
		return alg.longNode(123);
	}

	static <P, E> E makeIf(MumAlg<P, E> alg) {
		return alg.ifNode(alg.booleanNode(true), alg.stringNode("It's true"), alg.stringNode("It's false"));
	}

	static <P, E> E makeDefine(MumAlg<P, E> alg) {
		return alg.defineNode("a", alg.stringNode("This variable refers to a string."));
	}

	static <P, E> E makeQuote(MumAlg<P, E> alg) {
		return alg.quoteNode(alg.mumblerSymbol("a"));
	}

	static <P, E> E makeList(MumAlg<P, E> alg) {
		ArrayList<E> list = new ArrayList<E>();
		list.add(alg.stringNode("x"));
		list.add(alg.longNode(6));

		return alg.listNode(list);
	}

	static <P, E> E makeLambda(MumAlg<P, E> alg) {
		ArrayList<E> argsList = new ArrayList<E>();
		argsList.add(alg.mumblerSymbol("x"));

		ArrayList<E> bodyList = new ArrayList<E>();
		
		ArrayList<E> list2 = new ArrayList<E>();
		
		list2.add(alg.mumblerSymbol("x"));
		list2.add(alg.mumblerSymbol("x"));
		
		bodyList.add(alg.invokeNode("*", list2));

		return alg.lambdaNode(argsList, bodyList);
	}
	
	static <P, E> E makeSymbol(MumAlg<P, E> alg) {
		return alg.mumblerSymbol("x");
	}
	
	static <P, E> E makeInvoke (MumAlg<P, E> alg) {
		ArrayList<E> argsList = new ArrayList<E>();
		argsList.add(alg.mumblerSymbol("y"));
		argsList.add(alg.longNode(6));
		return alg.invokeNode("funcX", argsList);
	}
	
	static <P, E> P makeStart (MumAlg<P, E> alg) {
		ArrayList<E> list = new ArrayList<E>();
		
		list.add(alg.stringNode("A string"));
		
		list.add(alg.booleanNode(false));
		list.add(alg.longNode(123));
		list.add(alg.ifNode(alg.booleanNode(true), alg.stringNode("It's true"), alg.stringNode("It's false")));
		list.add(alg.defineNode("a", alg.stringNode("This variable refers to a string.")));
		
		// listNode
		ArrayList<E> list2 = new ArrayList<E>();
		list2.add(alg.stringNode("x"));
		list2.add(alg.longNode(6));
		
		list.add(alg.listNode(list2));

		// lambdaNode
		ArrayList<E> argsList = new ArrayList<E>();
		argsList.add(alg.mumblerSymbol("x"));

		ArrayList<E> bodyList = new ArrayList<E>();
		
		ArrayList<E> list3 = new ArrayList<E>();
		
		list3.add(alg.mumblerSymbol("x"));
		list3.add(alg.mumblerSymbol("x"));
		
		bodyList.add(alg.invokeNode("*", list3));

		list.add(alg.lambdaNode(argsList, bodyList));
		
		// symbolNode
		list.add(alg.mumblerSymbol("x"));
		
		// invokeNode
		ArrayList<E> argsListInvoke = new ArrayList<E>();
		argsListInvoke.add(alg.mumblerSymbol("y"));
		argsListInvoke.add(alg.longNode(6));
		list.add(alg.invokeNode("funcX", argsListInvoke));
		
		return alg.start(list);
	}

	public static void main(String[] args) {
      PPMumAlg boolTestAlg = new PPMumAlg();
      IPrint boolTest = makeBool(boolTestAlg);
      boolTest.print();
      System.out.println(boolTestAlg.back().getString());

      PPMumAlg longTestAlg = new PPMumAlg();
      IPrint longTest = makeLong (longTestAlg);
      longTest.print();
      System.out.println(longTestAlg.back().getString());

      PPMumAlg ifTestAlg = new PPMumAlg();
      IPrint ifTest = makeIf(ifTestAlg);
      ifTest.print();
      System.out.println(ifTestAlg.back().getString());

      PPMumAlg defineTestAlg = new PPMumAlg();
      IPrint defineTest = makeDefine(defineTestAlg);
      defineTest.print();
      System.out.println(defineTestAlg.back().getString());

      PPMumAlg quoteTestAlg = new PPMumAlg();
      IPrint quoteTest = makeQuote(quoteTestAlg);
      quoteTest.print();
      System.out.println(quoteTestAlg.back().getString());

      PPMumAlg listTestAlg = new PPMumAlg();
      IPrint listTest = makeList(listTestAlg);
      listTest.print();
      System.out.println(listTestAlg.back().getString());

      PPMumAlg lambdaTestAlg = new PPMumAlg();
      IPrint lambdaTest = makeLambda(lambdaTestAlg);
      lambdaTest.print();
      System.out.println(lambdaTestAlg.back().getString());

      PPMumAlg symbolTestAlg = new PPMumAlg();
      IPrint symbolTest = makeSymbol(symbolTestAlg);
      symbolTest.print();
      System.out.println(symbolTestAlg.back().getString());

      PPMumAlg invokeTestAlg = new PPMumAlg();
      IPrint invokeTest = makeInvoke(invokeTestAlg);
      invokeTest.print();
      System.out.println(invokeTestAlg.back().getString());

      PPMumAlg startTestAlg = new PPMumAlg();
      IPrint startTest = makeStart(startTestAlg);
      startTest.print();
      System.out.println(startTestAlg.back().getString());

//		Layouter<IOException> startTest = makeStart(new PPMumAlg());
//		try {
//			startTest.flush();
////			startTest.close();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
	}
}
