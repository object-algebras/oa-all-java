package ppTestMum;

import java.util.*;

import ppgen.PPMumAlg;
import anno.*;

public class Main {

	static <P, E> E makeBool(MumAlg<P, E> alg) {
		return alg.booleanNode(false);
	}
	
	static <P, E> E make2(MumAlg<P, E> alg) {
		return alg.ifNode(alg.booleanNode(true), alg.stringNode("It's true"), alg.stringNode("It's false"));
	}
	
	static <P, E> E make3(MumAlg<P, E> alg) {
		return alg.defineNode("a", alg.stringNode("variableA"));
	}

	public static void main(String[] args) {

		String m1pp = makeBool(new PPMumAlg());
		System.out.println(m1pp);

		String m2pp = make2(new PPMumAlg());
		System.out.println(m2pp);
		
		String m3pp = make3(new PPMumAlg());
		System.out.println(m3pp);
	}
}
