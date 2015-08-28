package Original;

import java.util.ArrayList;
import java.util.List;

public class MainOriginal {
    public static void main(String args[]) {
        PPAlg pp = new PPAlg();
        QueryAlg<Integer> query = new QueryAlg<Integer>(new MonoidPlus());
        ReflExpAlg<String> ppExp = new ReflExpAlg<String>() {
            ReflAlg<String> alg() {
                return pp;
            }
        };
        ReflExpAlg<Integer> queryExp = new ReflExpAlg<Integer>() {
            public Integer Lit(int x) {
                return x;
            }

            ReflAlg<Integer> alg() {
                return query;
            }
        };
        Exp fact = new Exp();
        Exp.Type e = fact.Add(fact.Lit(3), fact.Lit(2));
        System.out.println(e.accept(ppExp));
        System.out.println(e.accept(queryExp));
    }
}

interface ReflAlg<E> {
    E KInt(Integer x);
    E KBool(Boolean x);
    E KString(String s);
    E Cons(String name, List<E> args);
}

interface ExpAlg<E> {
    E Lit(int x);
    E Add(E e1, E e2);
    E Var(String s);
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

// A generic pretty printer
final class PPAlg implements ReflAlg<String> {
    public String KInt(Integer x) {
        return x.toString();
    }

    public String KBool(Boolean x) {
        return x.toString();
    }

    public String KString(String s) {
        return s;
    }

    public String Cons(String name, List<String> args) {
        String s = "(";
        String sep = "";
        
        for (String arg : args) {
            s = s + sep + arg;
            sep = ", ";
        }
        
        return name + s + ")";
    }
}

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

// try merge algebras??

abstract class ReflExpAlg<E> implements ExpAlg<E> {
    abstract ReflAlg<E> alg();
    
    public E Lit(int x) {
        List<E> l = new ArrayList<E>();
        l.add(alg().KInt(x));
        return alg().Cons("Lit", l);
    }

    public E Add(E e1, E e2) {
        List<E> l = new ArrayList<E>();
        l.add(e1);
        l.add(e2);
        return alg().Cons("Add",l);
    }

    public E Var(String s) {
        List<E> l = new ArrayList<E>();
        l.add(alg().KString(s));
        return alg().Cons("Var", l);
    }
    
}

// Free Object Algebra
final class Exp implements ExpAlg<Exp.Type> {
    interface Type {
        <E> E accept(ExpAlg<E> alg);
    }

    public Type Lit(int x) {
        return new Type() {
            public <E> E accept(ExpAlg<E> alg) {    
                return alg.Lit(x);
            }
            
        };
    }

    public Type Add(Type e1, Type e2) {
            return new Type() {
                public <E> E accept(ExpAlg<E> alg) {
                    return alg.Add(e1.accept(alg), e2.accept(alg));
                }
            };
    }

    public Type Var(String s) {
        return new Type() {
            public <E> E accept(ExpAlg<E> alg) {
                return alg.Var(s);
            }
        };
    }
}