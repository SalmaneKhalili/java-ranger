package gov.nasa.jpf.symbc.numeric;

/**
 * Base class for all IEEE‑754 special values (NaN, infinities, signed zero).
 * Stores the precision sort (float/double) to enable correct SMT‑LIB translation later.
 */
public abstract class RealSpecialConstant extends RealConstant {
    protected final FpSort sort;

    public RealSpecialConstant(double value, FpSort sort) {
        super(value);
        this.sort = sort;
    }

    public FpSort getSort() {
        return sort;
    }
}