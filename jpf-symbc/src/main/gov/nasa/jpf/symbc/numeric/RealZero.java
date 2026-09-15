package gov.nasa.jpf.symbc.numeric;

import gov.nasa.jpf.symbc.numeric.ConstraintExpressionVisitor;

public class RealZero extends RealSpecialConstant {
    private final boolean positive;

    public static RealZero get(boolean positive, FpSort sort) {
        return new RealZero(positive, sort);
    }

    private RealZero(boolean positive, FpSort sort) {
        super(positive ? 0.0 : Double.longBitsToDouble(0x8000000000000000L), sort);
        this.positive = positive;
    }

    public boolean isPositive() {
        return positive;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof RealZero)) return false;
        RealZero other = (RealZero) obj;
        return this.positive == other.positive && this.sort == other.sort;
    }

    @Override
    public int hashCode() {
        return (positive ? 2 : 3) * 31 + sort.hashCode();
    }

    @Override
    public String toString() {
        String sign = positive ? "+" : "-";
        return (sort == FpSort.FLOAT ? "Float" : "Double") + sign + "Zero";
    }

    @Override
    public void accept(ConstraintExpressionVisitor visitor) {
        visitor.postVisit((RealConstant) this);
    }
}