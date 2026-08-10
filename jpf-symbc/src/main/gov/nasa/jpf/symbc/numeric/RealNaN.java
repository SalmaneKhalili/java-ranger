package gov.nasa.jpf.symbc.numeric;

import gov.nasa.jpf.symbc.numeric.ConstraintExpressionVisitor;

public class RealNaN extends RealSpecialConstant {
    public static final RealNaN FLOAT_NAN = new RealNaN(FpSort.FLOAT);
    public static final RealNaN DOUBLE_NAN = new RealNaN(FpSort.DOUBLE);

    private RealNaN(FpSort sort) {
        super(Double.NaN, sort);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof RealNaN && ((RealNaN) obj).sort == this.sort;
    }

    @Override
    public int hashCode() {
        return sort.hashCode() * 31;
    }

    @Override
    public String toString() {
        return sort == FpSort.FLOAT ? "FloatNaN" : "DoubleNaN";
    }

    @Override
    public void accept(ConstraintExpressionVisitor visitor) {
        visitor.postVisit((RealConstant) this);
    }
}