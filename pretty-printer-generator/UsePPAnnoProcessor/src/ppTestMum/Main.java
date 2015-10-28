package ppTestMum;

import java.util.*;

import ppgen.PPMumAlg;
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
		bodyList.add(alg.mumblerSymbol("*"));
		bodyList.add(alg.mumblerSymbol("x"));
		bodyList.add(alg.stringNode("x"));

//		alg.listNode()
		return alg.lambdaNode(argsList, bodyList);

//		return alg.lambdaNode(listNode(argsList), listNode(bodyList));
	}

	static <P, E> E makeSymbol(MumAlg<P, E> alg) {
		return alg.mumblerSymbol("x");
	}

	public static void main(String[] args) {
		String boolTest = makeBool(new PPMumAlg());
		System.out.println(boolTest);

		String longTest = makeLong(new PPMumAlg());
		System.out.println(longTest);

		String ifTest = makeIf(new PPMumAlg());
		System.out.println(ifTest);

		String defineTest = makeDefine(new PPMumAlg());
		System.out.println(defineTest);

		String quoteTest = makeQuote(new PPMumAlg());
		System.out.println(quoteTest);

		String listTest = makeList(new PPMumAlg());
		System.out.println(listTest);

		String lambdaTest = makeLambda(new PPMumAlg());
		System.out.println(lambdaTest);

		String symbolTest = makeSymbol(new PPMumAlg());
		System.out.println(symbolTest);
	}
}
