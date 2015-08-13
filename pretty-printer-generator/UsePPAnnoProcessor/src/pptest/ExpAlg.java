package pptest;

import java.util.List;

import anno.*;

@PP
public interface ExpAlg<E> {
    @Syntax("exp = exp '*' exp")
    @Level(20)
    E mul(E l, E r);

    @Syntax("exp = exp '+' exp")
    @Level(10)
    E add(E l, E r);

    // Refer to tokens (defined in Tokens.java)
    @Syntax("exp = NUM")
    E lit(int n);

    @Syntax("exp = 'avg' '(' exp@','+ ')'")
    E avg(List<E> es);
}

