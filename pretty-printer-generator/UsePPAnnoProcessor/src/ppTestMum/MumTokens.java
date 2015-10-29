package ppTestMum;

import java.math.BigInteger;

import noa.annos.Level;
import noa.annos.Skip;
import noa.annos.Token;

public interface MumTokens {
	@Token("[0-9]+")
	@Level(100)
	static Object num(String src) {
		try {
			return Integer.parseInt(src);
		} catch (NumberFormatException e) {
			return new BigInteger(src, 10);
		}
	}

	@Token("('#f'|'#t')")
	@Level(90)
	static boolean bool(String src) {
		if (src.equals("#f"))
			return false;
		if (src.equals("#t"))
			return true;
		return false;
	}

	@Token("'\"' ( ~'\"' | '\\\\' '\"' )* '\"'")
	@Level(80)
	static String string(String src) {
		String text = src;
		StringBuilder b = new StringBuilder();
		for (int i = 1; i < text.length() - 2; i++) {
			char c = text.charAt(i);
			if (c == '\\') {
				char next = text.charAt(i + 1);
				i++;
				switch (next) {
				case '\\':
					b.append('\\');
					break;
				case '"':
					b.append('"');
					break;
				case 'n':
					b.append('\n');
					break;
				case 'r':
					b.append('\r');
					break;
				case 't':
					b.append('\t');
					break;
				case 'f':
					b.append('\f');
					break;
				case 'b':
					b.deleteCharAt(b.length() - 1);
					break;
				default:
					b.append(next);
					break;
				}
			} else {
				b.append(c);
			}
		}
		b.append(text.charAt(text.length() - 2));
		return b.toString();
	}

	@Token("~('#'|'\"'|'\\''|[()]|[ \\t\\r\\n]) ~('\"'|'\\''|[()]|[ \\t\\r\\n])*")
	@Level(70)
	static String symbol(String src) {
		return src;
	}

	@Token("';;' .*? ('\\n' | EOF)")
	@Skip
	@Level(60)
	void comment();

	@Token("[ \\r\\n\\t]+")
	@Skip
	@Level(50)
	void ws();
}
