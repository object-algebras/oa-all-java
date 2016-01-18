// This file should be generated automatically in the folder /generated. This is the handwritten version for testing purpose. Please copy the file into /generated for testing.
package ppTestMum;
import de.uka.ilkd.pp.*;
public interface IPrint {
	static final int DEFAULT_LINE_WIDTH = 80;
	static final int DEFAULT_INDENTATION = 2;

	default StringBackend print(int lineWidth, int indentation) {
		StringBackend back = new StringBackend(lineWidth);
		Layouter<NoExceptions> pp = new Layouter<NoExceptions>(back, indentation);
		printLocal(pp);
		return back;
	}

	default StringBackend print() {
		StringBackend back = new StringBackend(DEFAULT_LINE_WIDTH);
		Layouter<NoExceptions> pp = new Layouter<NoExceptions>(back, DEFAULT_INDENTATION);
		printLocal(pp);
		return back;
	}

	void printLocal(Layouter<NoExceptions> pp);
}