package test;

import java.util.List;
import refl.MumAlgGen;
import annotation.Refl;

@Refl
 public interface MumAlg<E, P> {

    E ifNode(E e1, E e2, E e3);
    //
    E defineNode(String slot, E e);
    //
    E quoteNode(E literalNode);
    //
    //	E lambdaNode(List<E> args, List<E> rtns);
    //
    //    E listNode(List<E> args);

	E booleanNode(boolean x);
    P start(E es);

    //    E longNode(Object x);
    //
    E mumblerSymbol(String symbolName);
    //
    E stringNode(String x);
    //
    //	P start(List<E> es);
    //
    //	E invokeNode(String func, List<E> args);

}

class Test {
    public static void main(String[] args) {
        MumAlgGen fact = new MumAlgGen();
        MumAlgGen.TypeP program = fact.start(fact.booleanNode(true));
        System.out.println(program);
    }
}