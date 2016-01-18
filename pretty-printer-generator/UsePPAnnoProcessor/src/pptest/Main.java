package pptest;

import java.util.*;

import ppgen.PPExpAlg;
import ppgen.IPrint;
import anno.*;

import de.uka.ilkd.pp.*;

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
        PPExpAlg alg = new PPExpAlg();
        IPrint pp1 = make(alg);
		// Example: Manual specification of line width and indentation
        StringBackend s1 = pp1.print(20, 2);
        System.out.println(s1.getString());

        IPrint pp2 = make2(alg);
        StringBackend s2 = pp2.print();
        System.out.println(s2.getString());

        
        IPrint pp3 = alg.add(pp1, pp2);
        StringBackend s3 = pp3.print();
        System.out.println(s3.getString());
         
    }
}