package gov.nasa.jpf.symbc.numeric;

/**
 * Helper for bytecode instructions to produce special value constants.
 */
public class SpecialValueFactory {
    public static RealNaN createNaN(boolean isDouble) {
        return isDouble ? RealNaN.DOUBLE_NAN : RealNaN.FLOAT_NAN;
    }

    public static RealInfinity createInfinity(boolean positive, boolean isDouble) {
        return RealInfinity.get(positive, isDouble ? FpSort.DOUBLE : FpSort.FLOAT);
    }

    public static RealZero createZero(boolean positive, boolean isDouble) {
        return RealZero.get(positive, isDouble ? FpSort.DOUBLE : FpSort.FLOAT);
    }
}