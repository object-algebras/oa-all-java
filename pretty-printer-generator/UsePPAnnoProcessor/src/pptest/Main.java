package pptest;

import java.util.*;

import ppgen.PPExpAlg;
import ppgen.IPrint;
import anno.*;

/*
 class PPExpAlg implements ExpAlg<String> {

 public String add(String l, String r) {
 return l + "+" + r;
 }

 public String mul(String l, String r) {
 return l + "*" + r;
 }

 public String lit(int n) {
 return "" + n;
 }
 }
 */

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
    System.out.println(p1.back.getString());
    
    PPExpAlg p2 = new PPExpAlg();
    IPrint pp2 = make2(p2);
    pp2.print();
    System.out.println(p2.back.getString());
	/******
	 * List<String> list = Arrays.asList("5", "6", "7", "8"); String joined
	 * = String.join("+", list); System.out.println(list);
	 * System.out.println(joined);
	 *******/

    }
}