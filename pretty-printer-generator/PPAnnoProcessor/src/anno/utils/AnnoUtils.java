package anno.utils;

public interface AnnoUtils {
    public static final String TAB = "\t";
    public static final String TAB2 = "\t\t";
    public static final String TAB3 = "\t\t\t";
    public static final String TAB4 = "\t\t\t\t";

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
