package pptest;

import java.util.*;

import ppgen.PPExpAlg;
import ppgen.IPrint;
import anno.*;

public class Main {
    static <E> E make(ExpAlg<E> alg) {
        return alg.add(alg.lit(3), alg.mul(alg.lit(4), alg.lit(5)));
    }

    static <E> E make2(ExpAlg<E> alg) {
        ArrayList<E> list = new ArrayList<E>();
        list.add(alg.lit(5));
        list.add(alg.lit(6));
        list.add(alg.lit(7));
        list.add(alg.lit(8));
        return alg.avg(list);
    }

    public static void main(String[] args) {
        PPExpAlg p1 = new PPExpAlg();
        IPrint pp1 = make(p1);
        pp1.print();
        System.out.println(p1.back().getString());

        PPExpAlg p2 = new PPExpAlg();
        IPrint pp2 = make2(p2);
        pp2.print();
        System.out.println(p2.back().getString());

        /**
         * Wrong: expressions pp1 and pp2 cannot be reused.
         * PPExpAlg p3 = new PPExpAlg();
         * IPrint pp3 = p3.add(pp1, pp2);
         * pp3.print();
         * System.out.println(p3.back().getString());
         **/
    }
}