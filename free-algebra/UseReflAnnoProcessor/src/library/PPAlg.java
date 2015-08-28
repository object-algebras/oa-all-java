package library;

import java.util.List;

//A generic pretty printer
public final class PPAlg implements ReflAlg<String> {
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

//     String ss = if (args.size() == 1) return args.get(0); else args.get(0) + " " + name + " " + args.get(1);

     return name + s + ")";
 }
}
