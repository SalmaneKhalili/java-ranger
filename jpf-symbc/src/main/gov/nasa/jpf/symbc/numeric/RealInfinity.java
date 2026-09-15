package gov.nasa.jpf.symbc.numeric;

import gov.nasa.jpf.symbc.numeric.ConstraintExpressionVisitor;

public class RealInfinity extends RealSpecialConstant {
    private final boolean positive;

    public static RealInfinity get(boolean positive, FpSort sort) {
        return new RealInfinity(positive, sort);
    }

    private RealInfinity(boolean positive, FpSort sort) {
        super(positive ? Double.POSITIVE_INFINITY : Double.NEGATIVE_INFINITY, sort);
        this.positive = positive;
    }

    public boolean isPositive() {
        return positive;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof RealInfinity)) return false;
        RealInfinity other = (RealInfinity) obj;
        return this.positive == other.positive && this.sort == other.sort;
    }

    @Override
    public int hashCode() {
        return (positive ? 1 : 0) * 31 + sort.hashCode();
    }

    @Override
    public String toString() {
        String sign = positive ? "+" : "-";
        return (sort == FpSort.FLOAT ? "Float" : "Double") + sign + "Inf";
    }

    @Override
    public void accept(ConstraintExpressionVisitor visitor) {
        visitor.postVisit((RealConstant) this);
    }
}