package ppTestMum;

import java.util.*;

import ppgen.PPMumAlg;
import ppgen.IPrint;
import anno.*;

import de.uka.ilkd.pp.*;

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

	static <P, E> E makeInvoke(MumAlg<P, E> alg) {
		ArrayList<E> argsList = new ArrayList<E>();
		argsList.add(alg.mumblerSymbol("y"));
		argsList.add(alg.longNode(6));
		return alg.invokeNode("funcX", argsList);
	}

	static <P, E> P makeStart(MumAlg<P, E> alg) {
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
		PPMumAlg alg = new PPMumAlg();
		IPrint boolTest = makeBool(alg);
		StringBackend boolString = boolTest.print();
		System.out.println(boolString.getString());

		IPrint longTest = makeLong(alg);
		StringBackend longString = longTest.print();
		System.out.println(longString.getString());

		IPrint ifTest = makeIf(alg);
		// Example: Manual specification of line width and indentation
		StringBackend ifString = ifTest.print(10, 8);
		System.out.println(ifString.getString());

		IPrint defineTest = makeDefine(alg);
		StringBackend defineString = defineTest.print();
		System.out.println(defineString.getString());

		IPrint quoteTest = makeQuote(alg);
		StringBackend quoteString = quoteTest.print();
		System.out.println(quoteString.getString());

		IPrint listTest = makeList(alg);
		StringBackend listString = listTest.print();
		System.out.println(listString.getString());

		IPrint lambdaTest = makeLambda(alg);
		StringBackend lambdaString = lambdaTest.print();
		System.out.println(lambdaString.getString());

		IPrint symbolTest = makeSymbol(alg);
		StringBackend symbolString = symbolTest.print();
		System.out.println(symbolString.getString());

		IPrint invokeTest = makeInvoke(alg);
		StringBackend invokeString = invokeTest.print();
		System.out.println(invokeString.getString());

		IPrint startTest = makeStart(alg);
		StringBackend startString = startTest.print();
		System.out.println(startString.getString());
	}
}
