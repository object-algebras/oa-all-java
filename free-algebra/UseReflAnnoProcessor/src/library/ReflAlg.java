package library;

import java.util.List;

public interface ReflAlg<E> {
    E KInt(Integer x);
    E KBool(Boolean x);
    E KString(String s);
    // E Plus(E e1, E e2);
    // E Prod(E e1, E e2);
    E Cons(String name, List<E> args); // E Cons(Method method, List<E> args);
}
