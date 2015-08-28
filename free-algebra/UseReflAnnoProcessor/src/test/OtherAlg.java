package test;

import annotation.Refl;

@Refl
public interface OtherAlg<E> {
    E bool(boolean x);
    E ifNode(E e1, E e2, E e3);
}
