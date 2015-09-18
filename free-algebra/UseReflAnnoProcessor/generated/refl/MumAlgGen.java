package refl;

import test.MumAlg;

public final class MumAlgGen implements MumAlg<MumAlgGen.TypeE ,MumAlgGen.TypeP> {
	public interface TypeE {
		<E,P> E accept(MumAlg<E,P> alg);
	}

	public interface TypeP {
		<E,P> P accept(MumAlg<E,P> alg);
	}

	public TypeE booleanNode(boolean p0) {
		return new TypeE() {
			public <E,P> E accept(MumAlg<E,P> alg) {
					return alg.booleanNode(p0);
			}
		};
	}
	public TypeP start(TypeE p0) {
		return new TypeP() {
			public <E,P> P accept(MumAlg<E,P> alg) {
					return alg.start(p0.accept(alg));
			}
		};
	}
}