package ppTestMum;

import java.util.List;

import anno.*;

@PP(MumTokens.class)
public interface MumAlg<P, E> {
	// // node
	// E lexicalReadNode(MaterializedFrame scope, FrameSlot slot);
	// E symbolNode(FrameSlot slot);
	// E readArgumentNode(int argumentIndex);
	//
	// // special forms
	@Syntax("form = '(' 'if' form form form ')' ")
	@Level(100)
	E ifNode(E e1, E e2, E e3);

	@Syntax("form = '(' 'define' SYMBOL form ')' ")
	@Level(90)
	E defineNode(String slot, E e);

	@Syntax("form = '(' 'quote' form ')' ")
	@Level(80)
	E quoteNode(E literalNode);

	@Syntax("form = '(' 'lambda' '(' form@''* ')' form@''+ ')' ")
	@Level(70)
	E lambdaNode(List<E> args, List<E> rtns);

	@Syntax("form = '(' form@''+ ')' ")
	@Level(1)
	E listNode(List<E> args);

	// literal

	@Syntax("form = BOOL")
	E booleanNode(boolean x);

	@Syntax("form = NUM")
	E longNode(Object x);

	// @Syntax("form = NUM")
	// E bigIntegerNode(BigInteger x);

	@Syntax("form = SYMBOL")
	E mumblerSymbol(String symbolName);

	@Syntax("form = STRING")
	E stringNode(String x);

	@Syntax("file = form@''+ ")
	P start(List<E> es);

	 @Syntax("form = '(' SYMBOL form@''* ')' ")
	 @Level(10)
	 E invokeNode(String func, List<E> args);

}
