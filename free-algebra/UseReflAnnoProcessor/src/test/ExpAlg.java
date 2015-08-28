package test;

import annotation.Refl;

@Refl
public interface ExpAlg<E> {
    E Lit(int x);
    E Add(E e1, E e2);
    E Var(String s);
}