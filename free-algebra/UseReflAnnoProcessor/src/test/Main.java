package test;

import java.util.List;

import refl.ExpAlgGen;
import refl.OtherAlgGen;
import refl.ReflExpAlg;
import refl.ReflOtherAlg;
import library.PPAlg;
import library.ReflAlg;

// Pretty printer 3 + 2

// Given ExpAlg, generate ReflExpAlg and Exp?

public class Main {
    public static void main(String args[]) {
        PPAlg pp = new PPAlg();
        
        //----------- PP for ExpAlg ------------
        ReflExpAlg<String> ppExp = new ReflExpAlg<String>() {
            public ReflAlg<String> alg() {
                return pp;
            }
        };

        ExpAlgGen fact = new ExpAlgGen();
        ExpAlgGen.Type e = fact.Add(fact.Lit(3), fact.Lit(2));
        System.out.println(e.accept(ppExp));



        QueryAlg<Integer> query = new QueryAlg<Integer>(new MonoidPlus());
        ReflExpAlg<Integer> queryExp = new ReflExpAlg<Integer>() {
            public Integer Lit(int x) {
                return x;
            }

            public ReflAlg<Integer> alg() {
                return query;
            }
        };
        System.out.println(e.accept(queryExp));
        
        
        
        //------------ PP for OtherAlg -------------
        ReflOtherAlg<String> ppOther = new ReflOtherAlg<String>() {
            public ReflAlg<String> alg() { return pp; }
        };
        
        OtherAlgGen o = new OtherAlgGen();
        OtherAlgGen.Type e2 = o.ifNode(o.bool(true), o.bool(false), o.bool(true));
        System.out.println(e2.accept(ppOther));        
    }
}





// producers?
/*
final class Producer<E> implements ReflAlg<E> {
    public E KInt(Integer x) {
        return null;
    }

    public E KBool(Boolean x) {
        return null;
    }

    public E KString(String s) {
        return null;
    }

    public E Cons(String name, List<E> args) {
        return null;
    }
}*/


interface Monoid<T> {
    T zero();
    T plus(T x, T y);
}

final class MonoidPlus implements Monoid<Integer> {
    public Integer zero() {
        return 0;
    }

    public Integer plus(Integer x, Integer y) {
        return x+y;
    }

}

final class QueryAlg<E> implements ReflAlg<E> {
    Monoid<E> m;

    public QueryAlg(Monoid<E> m){
        this.m = m;
    }

    public E KInt(Integer x) {
        return m.zero();
    }

    public E KBool(Boolean x) {
        return m.zero();
    }

    public E KString(String s) {
        return m.zero();
    }

    public E Cons(String name, List<E> args) {
        E r = m.zero();

        for (E arg : args) {
            r = m.plus(r, arg);
        }

        return r;
    }
}

//// try merge algebras??
//
//abstract class ReflExpAlg<E> implements ExpAlg<E> {
//    abstract ReflAlg<E> alg();
//
//    public E Lit(int x) {
//        List<E> l = new ArrayList<E>();
//        l.add(alg().KInt(x));
//        return alg().Cons("Lit", l);
//    }
//
//    public E Add(E e1, E e2) {
//        List<E> l = new ArrayList<E>();
//        l.add(e1);
//        l.add(e2);
//        return alg().Cons("Add",l);
//    }
//
//    public E Var(String s) {
//        List<E> l = new ArrayList<E>();
//        l.add(alg().KString(s));
//        return alg().Cons("Var", l);
//    }
//
//}

//// Free Object Algebra
//final class ExpAlgGen implements ExpAlg<ExpAlgGen.Type> {
//    interface Type {
//        <E> E accept(ExpAlg<E> alg);
//    }
//
//    public Type Lit(int x) {
//        return new Type() {
//            public <E> E accept(ExpAlg<E> alg) {
//                return alg.Lit(x);
//            }
//
//        };
//    }
//
//    public Type Add(Type e1, Type e2) {
//        return new Type() {
//            public <E> E accept(ExpAlg<E> alg) {
//                return alg.Add(e1.accept(alg), e2.accept(alg));
//            }
//        };
//    }
//
//    public Type Var(String s) {
//        return new Type() {
//            public <E> E accept(ExpAlg<E> alg) {
//                return alg.Var(s);
//            }
//        };
//    }
//}
