package annotation.utils;

public class Utils {
    public static int arrayContains(String[] ls, String s) {
        int i = 0;
        for (String ts : ls) {
            if (s.equals(ts))
                return i;
            i++;
        }
        return -1;
    }
}
